#!/usr/bin/env python3
"""Verify bytecode, isolation, and exact native module coverage of a Fabric JAR."""

import argparse
import hashlib
import io
import json
from pathlib import Path
import re
import struct
import zipfile


def internal_class_name(data):
    count = struct.unpack_from(">H", data, 8)[0]
    pool = {}
    offset, index = 10, 1
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            pool[index] = data[offset:offset + length].decode("utf-8", errors="replace")
            offset += length
        elif tag == 7:
            pool[index] = struct.unpack_from(">H", data, offset)[0]
            offset += 2
        elif tag in (8, 16, 19, 20):
            offset += 2
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag == 15:
            offset += 3
        else:
            raise ValueError("Unknown class constant-pool tag: " + str(tag))
        index += 1
    this_class = struct.unpack_from(">H", data, offset + 2)[0]
    return pool[pool[this_class]]


def inspect_classes(jar, maximum, bootstrap=False):
    for name in jar.namelist():
        if re.match(r"META-INF/[^/]+\.(SF|RSA|DSA|EC)$", name, re.IGNORECASE) or name.upper().startswith("META-INF/SIG-"):
            raise ValueError("Bundled signature: " + name)
        if not name.endswith(".class"):
            continue
        if name.startswith(("net/minecraft/", "net/fabricmc/", "org/bukkit/", "org/spongepowered/", "com/llamalad7/")):
            raise ValueError("Bundled platform class: " + name)
        if "/fabric/driver/" in name:
            raise ValueError("Bundled test driver: " + name)
        data = jar.read(name)
        magic, minor, major = struct.unpack(">IHH", data[:8])
        if magic != 0xCAFEBABE or major > maximum:
            raise ValueError("Incompatible class version: {} ({})".format(name, major))
        if internal_class_name(data) != name[:-6]:
            raise ValueError("Class entry/package case mismatch: " + name)
        if bootstrap and b"net/minecraft/" in data:
            raise ValueError("Minecraft reference in Java 8 bootstrap: " + name)
        shared = any(part in name for part in ("/core/", "/compat/MinecraftVersion", "/config/WorldSafeConfig"))
        if shared and any(symbol in data for symbol in (b"net/minecraft/", b"net/fabricmc/", b"org/bukkit/")):
            raise ValueError("Platform reference in shared core: " + name)
        if name.startswith(("org/bstats/", "me/lele/worldSafe/shaded/bstats/", "me/lele/worldSafe/metrics/")):
            raise ValueError("Bundled metrics implementation in Fabric: " + name)


def entrypoints(jar, metadata):
    entries = metadata.get("entrypoints", {}).get("main", [])
    if not entries:
        raise ValueError("Mod has no main entrypoint")
    for entry in entries:
        name = entry if isinstance(entry, str) else entry["value"]
        target = name.split("::", 1)[0].replace(".", "/") + ".class"
        if target not in jar.namelist():
            raise ValueError("Missing main entrypoint class: " + target)


def verify(path, lock, require_all=False):
    targets = {item["minecraft"]: item for item in lock["targets"]}
    selected = {}
    versions = set()
    with zipfile.ZipFile(path) as shell:
        metadata = json.loads(shell.read("fabric.mod.json"))
        if len(shell.namelist()) != len(set(shell.namelist())):
            raise ValueError("Duplicate root JAR entries")
        if metadata["id"] != "worldsafe" or "worldsafe_runtime" not in metadata["depends"]:
            raise ValueError("Shell must require its runtime")
        if metadata["depends"].get("java") != ">=8" or metadata["depends"].get("fabricloader") != ">=" + lock["loader"]:
            raise ValueError("Bootstrap Java/Loader dependencies differ from the lock")
        entrypoints(shell, metadata)
        if metadata.get("mixins"):
            raise ValueError("Shell must not register version-specific mixins")
        inspect_classes(shell, 52, bootstrap=True)
        for nested in metadata["jars"]:
            with zipfile.ZipFile(io.BytesIO(shell.read(nested["file"]))) as runtime:
                if len(runtime.namelist()) != len(set(runtime.namelist())):
                    raise ValueError("Duplicate runtime JAR entries")
                if any(name.endswith(".jar") for name in runtime.namelist()):
                    raise ValueError("Unexpected recursive runtime dependency")
                item = json.loads(runtime.read("fabric.mod.json"))
                entrypoints(runtime, item)
                game = item["custom"]["worldsafe:game"]
                if game not in targets or game in selected:
                    raise ValueError("Unknown or duplicate Minecraft target: " + game)
                if item["id"] != "worldsafe_runtime" or item["version"] in versions:
                    raise ValueError("Runtime mod IDs must agree and versions must be distinct")
                if item["version"] != metadata["version"] + "-mc." + game:
                    raise ValueError("Runtime and shell versions differ")
                if item["depends"]["minecraft"] != "=" + game:
                    raise ValueError("Runtime must match exactly one Minecraft release")
                spec = targets[game]
                if item["depends"]["java"] != ">=" + str(spec["java"]):
                    raise ValueError("Runtime Java dependency does not match the lock")
                config = json.loads(runtime.read("worldsafe.mixins.json"))
                if not config.get("mixins") or len(config["mixins"]) != len(set(config["mixins"])):
                    raise ValueError("Runtime must register a distinct set of native Mixins")
                if not config["required"] or config["injectors"]["defaultRequire"] != 1:
                    raise ValueError("Critical probe mixins must fail on a missing injection")
                for mixin in config.get("mixins", []) + config.get("client", []) + config.get("server", []):
                    entry = (config["package"] + "." + mixin).replace(".", "/") + ".class"
                    if entry not in runtime.namelist():
                        raise ValueError("Configured Mixin class is missing: " + entry)
                inspect_classes(runtime, spec["java"] + 44)
                selected[game] = nested["file"]
                versions.add(item["version"])
        if set(metadata["depends"]["minecraft"]) != {"=" + game for game in selected}:
            raise ValueError("Shell support list and embedded runtimes disagree")
        embedded = {name for name in shell.namelist() if name.endswith(".jar")}
        if embedded != set(selected.values()):
            raise ValueError("Unregistered or missing nested JAR")
        if require_all and set(selected) != set(targets):
            raise ValueError("Universal JAR is missing locked releases: " + ", ".join(sorted(set(targets) - set(selected))))
    return {"jarSha256": hashlib.sha256(path.read_bytes()).hexdigest(),
            "targets": list(selected), "bootstrapJava": 8, "allLockedTargetsPresent": set(selected) == set(targets)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("--require-all", action="store_true")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    lock = json.loads((root / "fabric/versions.lock.json").read_text())
    print(json.dumps(verify(args.jar, lock, args.require_all), indent=2))


if __name__ == "__main__":
    main()
