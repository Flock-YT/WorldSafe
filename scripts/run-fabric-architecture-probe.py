#!/usr/bin/env python3
"""Launch a production dedicated server with the *same* architecture probe JAR.

This is an architecture check, not a protection behavior test. It deliberately
does not mark integrated-server or feature coverage as tested.
"""

import argparse
import hashlib
import json
import os
from pathlib import Path
import queue
import shutil
import subprocess
import threading
import time
import urllib.request
import urllib.error


ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "fabric/build/architecture-cache"


def fetch(url):
    immutable = url.endswith(".sha1") or "meta.fabricmc.net/v2/versions/loader/" in url
    memo = CACHE / "http" / hashlib.sha256(url.encode("utf-8")).hexdigest()
    if immutable and memo.is_file():
        return memo.read_bytes()
    for attempt in range(3):
        try:
            with urllib.request.urlopen(url, timeout=30) as response:
                data = response.read()
            if immutable:
                memo.parent.mkdir(parents=True, exist_ok=True)
                temporary = memo.with_name(memo.name + "." + str(os.getpid()) + ".part")
                temporary.write_bytes(data)
                temporary.replace(memo)
            return data
        except (OSError, urllib.error.URLError):
            if attempt == 2:
                raise
            time.sleep(attempt + 1)


def cached(url, destination, digest, algorithm="sha1"):
    if destination.is_file():
        if hashlib.new(algorithm, destination.read_bytes()).hexdigest() == digest:
            return destination
    data = fetch(url)
    if hashlib.new(algorithm, data).hexdigest() != digest:
        raise ValueError("Download digest mismatch: " + url)
    destination.parent.mkdir(parents=True, exist_ok=True)
    temporary = destination.with_name(destination.name + "." + str(os.getpid()) + ".part")
    temporary.write_bytes(data)
    temporary.replace(destination)
    return destination


def launch(args, report):
    lock = json.loads((ROOT / "fabric/versions.lock.json").read_text())
    spec = next(item for item in lock["targets"] if item["minecraft"] == args.game)
    if not args.accept_eula:
        raise ValueError("Pass --accept-eula only after accepting the Minecraft server EULA")
    java = args.java_home.resolve() / "bin/java"
    if not java.is_file():
        raise ValueError("Java executable not found: " + str(java))
    jar = args.jar.resolve()
    report["jarSha256"] = hashlib.sha256(jar.read_bytes()).hexdigest()
    server = spec["downloads"]["server"]
    game_jar = cached(server["url"], CACHE / args.game / "server.jar", server["sha1"])
    profile_url = "https://meta.fabricmc.net/v2/versions/loader/{}/{}/server/json".format(args.game, lock["loader"])
    profile = json.loads(fetch(profile_url))
    classpath = []
    for library in profile["libraries"]:
        parts = library["name"].split(":")
        group, artifact, version = parts[:3]
        classifier = "-" + parts[3] if len(parts) > 3 else ""
        relative = "{}/{}/{}/{}-{}{}.jar".format(group.replace(".", "/"), artifact, version, artifact, version, classifier)
        url = library["url"].rstrip("/") + "/" + relative
        digest = fetch(url + ".sha1").decode().split()[0]
        classpath.append(cached(url, CACHE / "libraries" / relative, digest))
    classpath.append(game_jar)
    work = ROOT / "fabric/build/architecture-runs" / (args.game + "-server")
    if work.exists():
        shutil.rmtree(work)
    (work / "mods").mkdir(parents=True)
    shutil.copy2(jar, work / "mods" / jar.name)
    if hashlib.sha256((work / "mods" / jar.name).read_bytes()).hexdigest() != report["jarSha256"]:
        raise ValueError("Universal JAR changed while preparing the server")
    if args.driver:
        driver = args.driver.resolve()
        shutil.copy2(driver, work / "mods" / driver.name)
    if args.behavior and not args.driver:
        raise ValueError("Behavior tests require --driver")
    (work / "eula.txt").write_text("eula=true\n")
    (work / "server.properties").write_text(
        "level-name=architecture-probe\nlevel-type=flat\n"
        "online-mode=false\nserver-ip=127.0.0.1\nserver-port=0\n"
        "generate-structures=false\nview-distance=2\nspawn-protection=0\n"
        "max-tick-time=60000\n", encoding="utf-8")
    command = [str(java), "-Xms256m", "-Xmx1536m", "-Djava.awt.headless=true",
               "-Dworldsafe.diagnostics=true", "-Dworldsafe.behaviorTests=" + str(args.behavior).lower(), "-cp",
               os.pathsep.join(str(path) for path in classpath), profile["mainClass"], "nogui"]
    report["command"] = command
    report["log"] = str(work / "server.log")
    lines = []
    events = queue.Queue()
    process = subprocess.Popen(command, cwd=work, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT, text=True, bufsize=1)

    def consume():
        for line in process.stdout:
            events.put(line)
        events.put(None)

    reader = threading.Thread(target=consume, daemon=True)
    reader.start()
    deadline = time.monotonic() + args.timeout
    command_sent = False
    stop_sent = False
    command_confirmed = False
    behavior_finished = not args.behavior
    timed_out = False
    try:
        while time.monotonic() < deadline:
            try:
                line = events.get(timeout=0.25)
            except queue.Empty:
                continue
            if line is None:
                break
            lines.append(line)
            if "WORLDSAFE_ARCHITECTURE" in line or "WORLDSAFE_BEHAVIOR" in line or "ERROR" in line:
                print(line.rstrip(), flush=True)
            if "Done (" in line and not command_sent:
                process.stdin.write("worldsafe help\n")
                process.stdin.flush()
                command_sent = True
            command_confirmed |= "stage=command-executed" in line
            behavior_finished |= "WORLDSAFE_BEHAVIOR completed" in line
            if command_confirmed and behavior_finished and not stop_sent:
                process.stdin.write("stop\n")
                process.stdin.flush()
                stop_sent = True
        else:
            timed_out = True
    finally:
        if process.poll() is None:
            process.terminate()
        try:
            process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
        reader.join(timeout=5)
        while not events.empty():
            line = events.get_nowait()
            if line is not None:
                lines.append(line)
        (work / "server.log").write_text("".join(lines), encoding="utf-8")
    log = "".join(lines)
    expected = ["selected=" + args.game] + ["game=" + args.game + " stage=" + stage for stage in
                ("runtime-initialized", "server-run", "commands-registered", "command-executed")]
    report["exitCode"] = process.returncode
    report["missingMarkers"] = [marker for marker in expected if marker not in log]
    report["timeout"] = timed_out
    report["passed"] = not timed_out and not report["missingMarkers"] and process.returncode == 0 and stop_sent
    if args.behavior:
        behavior_file = work / "worldsafe-behavior.json"
        report["behaviorReport"] = str(behavior_file)
        behavior = json.loads(behavior_file.read_text()) if behavior_file.is_file() else {}
        report["protectionFeaturesTested"] = bool(behavior.get("results"))
        report["fullCoverageComplete"] = behavior.get("complete", False)
        report["passed"] = report["passed"] and behavior.get("smokePassed", False)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--game", required=True)
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--java-home", required=True, type=Path)
    parser.add_argument("--timeout", type=int, default=180)
    parser.add_argument("--accept-eula", action="store_true")
    parser.add_argument("--driver", type=Path)
    parser.add_argument("--behavior", action="store_true")
    args = parser.parse_args()
    report = {"minecraft": args.game, "environment": "dedicated-server", "passed": False,
              "integratedServerTested": False, "protectionFeaturesTested": False}
    try:
        launch(args, report)
    except Exception as error:
        report["error"] = "{}: {}".format(type(error).__name__, error)
    destination = ROOT / "fabric/build/architecture-reports" / (args.game + "-server.json")
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "command"}, indent=2))
    raise SystemExit(0 if report["passed"] else 1)


if __name__ == "__main__":
    main()
