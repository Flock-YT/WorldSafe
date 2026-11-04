#!/usr/bin/env python3
"""Explicitly refresh the supported Fabric releases; never discover new targets."""

import concurrent.futures
import hashlib
import io
import json
from pathlib import Path
import urllib.request
import zipfile


VERSIONS = (
    "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4",
    "1.15", "1.15.1", "1.15.2",
    "1.16", "1.16.1", "1.16.2", "1.16.3", "1.16.4", "1.16.5",
    "1.17", "1.17.1", "1.18", "1.18.1", "1.18.2",
    "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
    "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5",
    "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3",
)


def fetch(url):
    with urllib.request.urlopen(url, timeout=60) as response:
        return response.read()


def lock_target(version, manifest):
    entry = manifest[version]
    if entry["type"] != "release":
        raise ValueError("Not a release: " + version)
    raw = fetch(entry["url"])
    if hashlib.sha1(raw).hexdigest() != entry["sha1"]:
        raise ValueError("Minecraft metadata digest mismatch: " + version)
    metadata = json.loads(raw)
    if metadata["id"] != version:
        raise ValueError("Minecraft metadata version mismatch: " + version)
    remap = version.startswith("1.")
    target = {
        "minecraft": version,
        "java": metadata.get("javaVersion", {}).get("majorVersion", 8),
        "namespace": "intermediary" if remap else "official",
        "metadata": {"url": entry["url"], "sha1": entry["sha1"]},
        "downloads": {
            side: {key: metadata["downloads"][side][key] for key in ("url", "sha1", "size")}
            for side in ("client", "server")
        },
    }
    if remap:
        mappings = json.loads(fetch("https://meta.fabricmc.net/v2/versions/yarn/" + version))
        mappings = [item for item in mappings if item["gameVersion"] == version]
        if not mappings:
            raise ValueError("Missing Yarn mappings: " + version)
        mapping = max(mappings, key=lambda item: item["build"])
        target["mappings"] = mapping["version"]
        base = "https://maven.fabricmc.net/net/fabricmc/yarn/{0}/yarn-{0}".format(mapping["version"])
        artifact = fetch(base + "-v2.jar")
        with zipfile.ZipFile(io.BytesIO(artifact)) as jar:
            rows = jar.read("mappings/mappings.tiny").decode().splitlines()
        classes = [row.split("\t") for row in rows if row.startswith("c\t")]
        intermediary_sources = sum("/class_" in row[1] for row in classes)
        intermediary_targets = sum("/class_" in row[2] for row in classes)
        # Early 1.14 v2 publications have reversed namespace columns. The
        # original v1 publication contains the correct, explicit namespaces.
        if intermediary_targets > intermediary_sources:
            artifact = fetch(base + ".jar")
            with zipfile.ZipFile(io.BytesIO(artifact)) as jar:
                header = jar.read("mappings/mappings.tiny").decode().splitlines()[0]
            if set(header.split("\t")[1:]) != {"official", "intermediary", "named"}:
                raise ValueError("Invalid original Yarn namespaces: " + version)
            target["mappingClassifier"] = None
        else:
            target["mappingClassifier"] = "v2"
        target["mappingsSha256"] = hashlib.sha256(artifact).hexdigest()
    return target


def main():
    manifest = json.loads(fetch("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
    manifest = {item["id"]: item for item in manifest["versions"]}
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
        targets = list(executor.map(lambda version: lock_target(version, manifest), VERSIONS))
    lock = {
        "schemaVersion": 1,
        "gradle": "9.8.2",
        "gradleSha256": fetch("https://downloads.gradle.org/distributions/gradle-9.8.2-bin.zip.sha256").decode().strip(),
        "loom": "1.17.21",
        "loader": "0.20.1",
        "minimumLoader": "0.19.5",
        "targets": targets,
    }
    output = Path(__file__).resolve().parent.parent / "fabric" / "versions.lock.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(lock, indent=2) + "\n", encoding="utf-8")
    print("Locked {} releases in {}".format(len(targets), output))


if __name__ == "__main__":
    main()
