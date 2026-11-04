#!/usr/bin/env bash

set -euo pipefail

version="${1:?Usage: create-release.sh VERSION ARTIFACT_DIRECTORY EXPECTED_BUKKIT_SHA256 EXPECTED_FABRIC_SHA256}"
artifact_directory="${2:?Missing artifact directory}"
expected_sha256="${3:?Missing build SHA-256}"
expected_fabric_sha256="${4:?Missing Fabric build SHA-256}"

if [[ ! "$version" =~ ^[0-9A-Za-z][0-9A-Za-z._-]*$ ]]; then
  echo "Invalid release version: $version" >&2
  exit 1
fi
if [[ ! "$expected_sha256" =~ ^[0-9a-f]{64}$ || ! "$expected_fabric_sha256" =~ ^[0-9a-f]{64}$ ]]; then
  echo "Invalid build SHA-256." >&2
  exit 1
fi

artifact="WorldSafe-$version.jar"
fabric_artifact="WorldSafe-Fabric-$version.jar"
fabric_report="WorldSafe-Fabric-$version-verification.json"
cd "$artifact_directory"
shasum -a 256 -c "$artifact.sha256"
actual_sha256="$(shasum -a 256 "$artifact")"
actual_sha256="${actual_sha256%% *}"
if [[ "$actual_sha256" != "$expected_sha256" ]]; then
  echo "Downloaded JAR differs from the verified build." >&2
  exit 1
fi
shasum -a 256 -c "$fabric_artifact.sha256"
actual_fabric_sha256="$(shasum -a 256 "$fabric_artifact")"
actual_fabric_sha256="${actual_fabric_sha256%% *}"
if [[ "$actual_fabric_sha256" != "$expected_fabric_sha256" ]]; then
  echo "Downloaded Fabric JAR differs from the verified build." >&2
  exit 1
fi
python3 - "$fabric_report" "$expected_fabric_sha256" "$version" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as file:
    report = json.load(file)
if report.get("scope") != "code" or not report.get("passed") or report.get("jarSha256") != sys.argv[2] or report.get("version") != sys.argv[3]:
    raise SystemExit("Fabric code verification report does not match the release artifact")
PY

# Keep the array nonempty: Bash 3.2 treats empty arrays as unset under set -u.
release_args=(release create "v$version" "$artifact" "$artifact.sha256"
  "$fabric_artifact" "$fabric_artifact.sha256" "$fabric_report"
  --target "${GITHUB_SHA:?Missing release commit SHA}"
  --title "WorldSafe v$version"
  --generate-notes)
if [[ ! "$version" =~ ^[0-9]+(\.[0-9]+)*$ ]]; then
  release_args+=(--prerelease --latest=false)
fi

gh "${release_args[@]}"
