#!/usr/bin/env python3
"""Open a real client and verify its integrated server using a separate test mod."""

import argparse
import concurrent.futures
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import platform
import queue
import re
import shutil
import subprocess
import threading
import time
import zipfile


ROOT = Path(__file__).resolve().parent.parent
module_spec = importlib.util.spec_from_file_location("server_probe", ROOT / "scripts/run-fabric-architecture-probe.py")
helpers = importlib.util.module_from_spec(module_spec)
module_spec.loader.exec_module(helpers)
cached, fetch, CACHE = helpers.cached, helpers.fetch, helpers.CACHE


def allowed(rules, architecture):
    result = not rules
    os_name = {"Darwin": "osx", "Linux": "linux", "Windows": "windows"}[platform.system()]
    for rule in rules:
        operating_system = rule.get("os", {})
        if operating_system.get("name", os_name) != os_name:
            continue
        if not re.fullmatch(operating_system.get("arch", ".*"), architecture):
            continue
        if not re.search(operating_system.get("version", ".*"), platform.release()):
            continue
        if any(value for value in rule.get("features", {}).values()):
            continue
        result = rule["action"] == "allow"
    return result


def prepare(args, report):
    lock = json.loads((ROOT / "fabric/versions.lock.json").read_text())
    spec = next(item for item in lock["targets"] if item["minecraft"] == args.game)
    raw_metadata = cached(spec["metadata"]["url"], CACHE / args.game / "minecraft.json", spec["metadata"]["sha1"])
    metadata = json.loads(raw_metadata.read_text())
    client = spec["downloads"]["client"]
    client_jar = cached(client["url"], CACHE / args.game / "client.jar", client["sha1"])
    profile = json.loads(fetch("https://meta.fabricmc.net/v2/versions/loader/{}/{}/profile/json".format(args.game, args.loader)))
    work = helpers.probe_path("runs", args, "client")
    if work.exists():
        shutil.rmtree(work)
    (work / "mods").mkdir(parents=True)
    jar = args.jar.resolve()
    report["jarSha256"] = hashlib.sha256(jar.read_bytes()).hexdigest()
    for source in (jar, args.driver.resolve()):
        shutil.copy2(source, work / "mods" / source.name)
    if hashlib.sha256((work / "mods" / jar.name).read_bytes()).hexdigest() != report["jarSha256"]:
        raise ValueError("Universal JAR changed while preparing the client")
    original = helpers.probe_path("runs", args, "server") / "architecture-probe"
    if not (original / "level.dat").is_file():
        raise ValueError("Run the dedicated-server probe first to generate the test world")
    shutil.copytree(original, work / "saves/architecture-probe")
    (work / "options.txt").write_text("onboardAccessibility:false\npauseOnLostFocus:false\ndifficulty:2\nmaxFps:30\nrenderDistance:2\nsimulationDistance:5\nlang:en_us\nsoundCategory_master:0.0\n", encoding="utf-8")
    native_directory = work / "natives"
    native_directory.mkdir()
    classpath = []
    os_name = {"Darwin": "osx", "Linux": "linux", "Windows": "windows"}[platform.system()]
    for library in profile["libraries"]:
        group, artifact, version = library["name"].split(":")[:3]
        relative = "{}/{}/{}/{}-{}.jar".format(group.replace(".", "/"), artifact, version, artifact, version)
        url = library["url"].rstrip("/") + "/" + relative
        classpath.append(cached(url, CACHE / "libraries" / relative, fetch(url + ".sha1").decode().split()[0]))
    for library in metadata["libraries"]:
        if not allowed(library.get("rules", []), args.architecture):
            continue
        downloads = library.get("downloads", {})
        artifact = downloads.get("artifact")
        if artifact:
            path = cached(artifact["url"], CACHE / "libraries" / artifact["path"], artifact["sha1"])
            classpath.append(path)
            if "natives-" in library["name"]:
                extract_native(path, native_directory)
        classifier = library.get("natives", {}).get(os_name)
        if classifier:
            native = downloads["classifiers"][classifier.replace("${arch}", "64")]
            path = cached(native["url"], CACHE / "libraries" / native["path"], native["sha1"])
            extract_native(path, native_directory)
    classpath.append(client_jar)
    index = metadata["assetIndex"]
    assets = CACHE / "assets"
    index_path = cached(index["url"], assets / "indexes" / (index["id"] + ".json"), index["sha1"])
    objects = json.loads(index_path.read_text())["objects"]
    # Sound playback is unnecessary for this probe; retain fonts, languages,
    # and all other resources needed by an unmodified client to open the world.
    required = {item["hash"] for name, item in objects.items() if not name.endswith(".ogg")}

    def download_asset(digest):
        return cached("https://resources.download.minecraft.net/{}/{}".format(digest[:2], digest),
                      assets / "objects" / digest[:2] / digest, digest)

    print("Preparing {} client resources".format(len(required)), flush=True)
    with concurrent.futures.ThreadPoolExecutor(max_workers=12) as executor:
        list(executor.map(download_asset, required))
    command = [str(args.java_home.resolve() / "bin/java"), "-Xms256m", "-Xmx1536m"]
    command.append("-Dworldsafe.diagnostics=true")
    command.append("-Dworldsafe.behaviorTests=" + str(args.behavior).lower())
    if platform.system() == "Darwin":
        command.append("-XstartOnFirstThread")
    command.extend(["-Djava.library.path=" + str(native_directory),
                    "-Dorg.lwjgl.librarypath=" + str(native_directory),
                    "-cp", os.pathsep.join(str(path) for path in dict.fromkeys(classpath)),
                    profile["mainClass"], "--username", "WorldSafeProbe", "--version", args.game,
                    "--gameDir", str(work), "--assetsDir", str(assets), "--assetIndex", index["id"],
                    "--uuid", "00000000-0000-0000-0000-000000000001", "--accessToken", "0",
                    "--userType", "legacy", "--versionType", "release", "--width", "854", "--height", "480"])
    return work, command


def extract_native(path, directory):
    with zipfile.ZipFile(path) as jar:
        for name in jar.namelist():
            if name.endswith((".dylib", ".jnilib", ".so", ".dll")):
                if ".." in Path(name).parts or Path(name).is_absolute():
                    raise ValueError("Invalid native resource path: " + name)
                output = directory / Path(name).name
                output.write_bytes(jar.read(name))


def launch(args, report):
    work, command = prepare(args, report)
    report["command"] = command
    report["log"] = str(work / "client.log")
    events = queue.Queue()
    lines = []
    process = subprocess.Popen(command, cwd=work, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, bufsize=1)

    def consume():
        for line in process.stdout:
            events.put(line)
        events.put(None)

    reader = threading.Thread(target=consume, daemon=True)
    reader.start()
    deadline = time.monotonic() + args.timeout
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
        (work / "client.log").write_text("".join(lines), encoding="utf-8")
    log = "".join(lines)
    expected = ["Loading Minecraft " + args.game + " with Fabric Loader " + args.loader + "\n",
                "selected=" + args.game] + ["stage=" + stage for stage in
                ("runtime-initialized", "commands-registered", "server-run", "command-executed",
                 "opening-singleplayer-world", "integrated-command-confirmed")]
    report["missingMarkers"] = [marker for marker in expected if marker not in log]
    report["exitCode"] = process.returncode
    report["timeout"] = timed_out
    report["passed"] = process.returncode == 0 and not timed_out and not report["missingMarkers"]
    if args.behavior:
        behavior_file = work / "worldsafe-behavior.json"
        report["behaviorReport"] = str(behavior_file)
        behavior = json.loads(behavior_file.read_text()) if behavior_file.is_file() else {}
        report["protectionFeaturesTested"] = bool(behavior.get("results"))
        report["fullCoverageComplete"] = behavior.get("complete", False)
        report["passed"] = report["passed"] and behavior.get("smokePassed", False)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    helpers.add_loader_argument(parser)
    parser.add_argument("--game", required=True)
    parser.add_argument("--jar", type=Path, required=True)
    parser.add_argument("--driver", type=Path, required=True)
    parser.add_argument("--java-home", type=Path, required=True)
    parser.add_argument("--architecture", default=platform.machine())
    parser.add_argument("--timeout", type=int, default=180)
    parser.add_argument("--behavior", action="store_true")
    args = parser.parse_args()
    report = {"minecraft": args.game, "loader": args.loader, "environment": "integrated-server", "passed": False,
              "protectionFeaturesTested": False}
    try:
        launch(args, report)
    except Exception as error:
        report["error"] = "{}: {}".format(type(error).__name__, error)
    destination = Path(str(helpers.probe_path("reports", args, "client")) + ".json")
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "command"}, indent=2))
    raise SystemExit(0 if report["passed"] else 1)


if __name__ == "__main__":
    main()
