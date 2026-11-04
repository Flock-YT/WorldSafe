#!/usr/bin/env python3
"""Fail closed unless the entire locked behavior matrix tested this exact JAR."""

import argparse
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent


def version(value):
    parts = tuple(int(part) for part in value.split("."))
    return parts + (0,) * (3 - len(parts))


def verify(jar, report, lock):
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    expected = {item["minecraft"] for item in lock["targets"]}
    if report.get("jarSha256") != digest or not report.get("artifactUnchanged"):
        raise ValueError("Behavior report does not describe this unchanged JAR")
    if not report.get("complete") or not report.get("passed") or report.get("developmentSmokeOnly"):
        raise ValueError("Full behavior acceptance has not passed; development smoke reports cannot authorize a release")
    if set(report.get("targets", [])) != expected or len(report["targets"]) != len(expected):
        raise ValueError("Behavior target list differs from the version lock")
    source = (ROOT / "shared/src/main/java/me/lele/worldSafe/core/FeatureCatalog.java").read_text()
    features = dict(re.findall(r'add\(features, "([A-Za-z]+)", MinecraftVersion\.V([0-9_]+)\);', source))
    if len(features) != 36:
        raise ValueError("Cannot resolve the complete shared feature catalog")
    seen = set()
    for target in report.get("results", []):
        game = target.get("minecraft")
        if game not in expected or game in seen:
            raise ValueError("Unexpected or duplicate behavior target: " + str(game))
        seen.add(game)
        for environment in ("server", "client"):
            result = target.get("environments", {}).get(environment, {})
            behavior = result.get("behavior", {})
            if behavior.get("schemaVersion") != 2 or "untestedMechanisms" not in behavior:
                raise ValueError("Unsupported or incomplete native report schema: " + game + "/" + environment)
            if result.get("jarSha256") != digest or not result.get("passed") or not result.get("matrixPassed"):
                raise ValueError("Failed or mismatched run: " + game + "/" + environment)
            if result.get("timeout") or result.get("missingMarkers") or result.get("exitCode") != 0:
                raise ValueError("Incomplete run: " + game + "/" + environment)
            if not behavior.get("complete") or behavior.get("untestedFeatures") or behavior.get("untestedMechanisms"):
                raise ValueError("Incomplete feature behavior coverage: " + game + "/" + environment)
            applicable = {key for key, minimum in features.items() if version(game) >= version(minimum.replace("_", "."))}
            statuses = behavior.get("features", {})
            if set(statuses) != set(features):
                raise ValueError("Missing feature-by-feature availability report: " + game + "/" + environment)
            for key, minimum in features.items():
                status = statuses[key]
                if version(status.get("minimumVersion", "0.0")) != version(minimum.replace("_", ".")) \
                        or status.get("status") != ("PASSED" if key in applicable else "NOT_APPLICABLE"):
                    raise ValueError("Incorrect feature result: " + game + "/" + environment + "/" + key)
            if not applicable.issubset(set(behavior.get("featureKeysCovered", []))):
                raise ValueError("Missing applicable feature keys: " + game + "/" + environment)
            cases = behavior.get("results", [])
            if not cases or not all(case.get("passed") for case in cases):
                raise ValueError("A native behavior assertion failed: " + game + "/" + environment)
            for key in applicable:
                scenarios = {case.get("scenario") for case in cases if case.get("feature") == key}
                if not (scenarios & {"disabled", "disabled-positive-control"}) or "enabled" not in scenarios \
                        or not (scenarios & {"other-dimension", "unconfigured-dimension-positive-control"}):
                    raise ValueError("Missing positive/negative world controls: " + game + "/" + environment + "/" + key)
    if seen != expected:
        raise ValueError("Missing game/environment acceptance runs")
    return digest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("jar", type=Path)
    parser.add_argument("report", type=Path)
    args = parser.parse_args()
    lock = json.loads((ROOT / "fabric/versions.lock.json").read_text())
    digest = verify(args.jar, json.loads(args.report.read_text()), lock)
    print("Full Fabric acceptance passed for SHA-256 " + digest)


if __name__ == "__main__":
    main()
