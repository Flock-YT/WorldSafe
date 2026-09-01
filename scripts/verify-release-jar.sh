#!/usr/bin/env bash

set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"

read_project_version() {
    mvn -q -Dstyle.color=never help:evaluate \
        -Dexpression=project.version \
        -DforceStdout 2>/dev/null | tr -d '\r'
}

expected_version="${2:-$(read_project_version)}"
jar_path="${1:-target/WorldSafe-${expected_version}.jar}"

if [[ "$jar_path" = /* ]]; then
    absolute_jar_path="$jar_path"
else
    absolute_jar_path="$project_root/$jar_path"
fi

if [[ ! -f "$absolute_jar_path" ]]; then
    echo "Release JAR not found: $jar_path" >&2
    exit 1
fi

jar_entries="$(jar tf "$absolute_jar_path")"
signature_pattern='^META-INF/([^/]+\.(SF|RSA|DSA|EC)|SIG-[^/]*)$'

if printf '%s\n' "$jar_entries" | grep -Eiq "$signature_pattern"; then
    echo "Forbidden signature metadata found in $jar_path:" >&2
    printf '%s\n' "$jar_entries" | grep -Ei "$signature_pattern" >&2
    exit 1
fi

if ! jarsigner_output="$(jarsigner -verify "$absolute_jar_path" 2>&1)"; then
    printf '%s\n' "$jarsigner_output" >&2
    echo "jarsigner verification failed for $jar_path" >&2
    exit 1
fi

if printf '%s\n' "$jarsigner_output" | grep -Eiq 'digest error|invalid signature|signature.*(error|invalid)'; then
    printf '%s\n' "$jarsigner_output" >&2
    echo "jarsigner reported a signature or digest error for $jar_path" >&2
    exit 1
fi

temporary_directory="$(mktemp -d)"
trap 'rm -rf "$temporary_directory"' EXIT

(
    cd "$temporary_directory"
    jar xf "$absolute_jar_path" plugin.yml
)

plugin_version="$(sed -nE "s/^[[:space:]]*version:[[:space:]]*['\"]?([^'\"[:space:]]+)['\"]?[[:space:]]*$/\1/p" "$temporary_directory/plugin.yml")"

if [[ "$plugin_version" != "$expected_version" ]]; then
    echo "plugin.yml version mismatch: expected $expected_version, found ${plugin_version:-<missing>}" >&2
    exit 1
fi

if printf '%s\n' "$jar_entries" | grep -Eq '^org/bukkit/'; then
    echo "Bukkit API classes must not be bundled in $jar_path." >&2
    exit 1
fi

if printf '%s\n' "$jar_entries" | grep -Eq '^(org/spongepowered/configurate|dev/rollczi/litecommands)/'; then
    echo "Removed compatibility-sensitive dependencies were bundled in $jar_path." >&2
    exit 1
fi

(
    cd "$temporary_directory"
    jar xf "$absolute_jar_path"
)

while IFS= read -r -d '' class_file; do
    major_version="$(javap -verbose "$class_file" 2>/dev/null | sed -nE 's/^[[:space:]]*major version:[[:space:]]*([0-9]+).*$/\1/p' | head -n 1)"
    if [[ -z "$major_version" ]]; then
        echo "Unable to inspect class version: $class_file" >&2
        exit 1
    fi
    if (( major_version > 52 )); then
        relative_class="${class_file#"$temporary_directory"/}"
        echo "Class $relative_class has major version $major_version; maximum allowed is 52 (Java 8)." >&2
        exit 1
    fi
done < <(find "$temporary_directory" -type f -name '*.class' -print0)

echo "Verified $jar_path (version $expected_version, Java 8 bytecode, no bundled Bukkit API or signatures)."
