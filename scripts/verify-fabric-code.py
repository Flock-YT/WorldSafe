#!/usr/bin/env python3
"""Collect code-only acceptance evidence for the exact universal Fabric JAR."""

import argparse
import hashlib
import importlib.util
import json
import re
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parent.parent
module = importlib.util.spec_from_file_location("fabric_package", ROOT / "scripts/verify-fabric-probe.py")
packaging = importlib.util.module_from_spec(module)
module.loader.exec_module(packaging)


def collect(jar, build, lock):
    package = packaging.verify(jar, lock, require_all=True)
    project_version = ET.parse(ROOT / "pom.xml").getroot().findtext("{http://maven.apache.org/POM/4.0.0}version")
    results = []
    source = (ROOT / "shared/src/main/java/me/lele/worldSafe/core/FeatureCatalog.java").read_text()
    features = {key: value.replace("_", ".") for key, value in re.findall(
        r'add\(features, "([A-Za-z]+)", MinecraftVersion\.V([0-9_]+)\);', source)}
    if len(features) != 36:
        raise ValueError("The shared feature catalog is incomplete")
    def version(value):
        parts = tuple(int(part) for part in value.split("."))
        return parts + (0,) * (3 - len(parts))
    with zipfile.ZipFile(jar) as universal:
        metadata = json.loads(universal.read("fabric.mod.json"))
        if metadata["version"] != project_version:
            raise ValueError("Fabric artifact does not match the root Maven version")
        for target in lock["targets"]:
            game = target["minecraft"]
            report_file = build / "targets" / ("mc" + game.replace(".", "_")) / "build/reports/native-hooks.json"
            proof = json.loads(report_file.read_text())
            digest = hashlib.sha256(universal.read("META-INF/jars/worldsafe-runtime-" + game + ".jar")).hexdigest()
            if not proof.get("passed") or proof.get("minecraft") != game or proof.get("java") != target["java"] \
                    or proof.get("namespace") != target["namespace"] or proof.get("runtimeJarSha256") != digest:
                raise ValueError("Native ABI verification does not match the embedded module: " + game)
            if any(proof.get(key, 0) <= 0 for key in ("mixins", "injections", "shadows", "references")):
                raise ValueError("Native verification inspected no hooks: " + game)
            if not proof.get("fireBurnHook", {}).get("passed"):
                raise ValueError("Fire-burn cancellation was not verified: " + game)
            proof["applicableFeatures"] = [key for key, minimum in features.items() if version(game) >= version(minimum)]
            proof["notApplicableFeatures"] = [key for key, minimum in features.items() if version(game) < version(minimum)]
            results.append(proof)
    tests = []
    for report_file in sorted((build / "test-results/test").glob("TEST-*.xml")):
        suite = ET.parse(report_file).getroot()
        if int(suite.get("failures", "0")) or int(suite.get("errors", "0")) or int(suite.get("skipped", "0")):
            raise ValueError("Fabric unit tests failed or were skipped: " + suite.get("name", "unknown"))
        tests.append({"class": suite.get("name"), "tests": int(suite.get("tests", "0"))})
    if not tests or sum(item["tests"] for item in tests) == 0:
        raise ValueError("Fabric unit-test results are missing")
    return {"schemaVersion": 1, "scope": "code", "passed": True, "version": project_version,
            "jarSha256": package["jarSha256"], "targets": package["targets"],
            "bootstrapJava": package["bootstrapJava"], "unitTests": tests, "nativeModules": results,
            "featureMinimumVersions": features,
            "inGameAcceptanceRequired": False,
            "limitations": ["Offline compilation, ABI/injection checks and unit tests do not certify all in-game behavior."]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("--build", type=Path, default=ROOT / "fabric/build")
    parser.add_argument("--output", type=Path, default=ROOT / "fabric/build/code-report.json")
    args = parser.parse_args()
    lock = json.loads((ROOT / "fabric/versions.lock.json").read_text())
    report = collect(args.jar, args.build, lock)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + "\n")
    print("Verified {} native modules and {} Fabric unit tests; SHA-256 {}".format(
        len(report["nativeModules"]), sum(item["tests"] for item in report["unitTests"]), report["jarSha256"]))


if __name__ == "__main__":
    main()
