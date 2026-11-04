# Fabric build and code verification

WorldSafe produces one Bukkit JAR and one universal Fabric JAR. The Fabric build covers the 48 stable Minecraft releases pinned in `versions.lock.json`, from 1.14 through 26.3. Acceptance for this migration is **code-only**, following the updated task scope; a full in-game behavior matrix is not required or claimed.

## Architecture

- The Java 8 shared core is the `:shared` Java library project at `../shared`, included by both independent builds. It has no production dependencies and is bundled only in the outer release JAR, never in nested runtimes. It defines the 36 protection keys, minimum versions, immutable configuration, validation, dimension matching, and explosion decisions without platform types.
- The Java 8 Fabric shell contains the core and privately relocated SnakeYAML. It requires one nested `worldsafe_runtime` module. Every candidate has a distinct version and exact Minecraft dependency; Loader selects the compatible candidate.
- The lock's `loader` (0.20.1) pins build dependencies; `minimumLoader` (0.19.5) independently defines the runtime requirement for the shell and every nested module. Updating build tooling must not silently raise the runtime minimum.
- `generate-native.gradle` compiles required native Mixins for each pinned game. It isolates game types, mappings and Java requirements inside that module. Older modules are remapped before nesting; 26.x uses official, unmapped names. Fabric API is not required.
- Bukkit lives in `../bukkit` within the root Gradle build, retaining the Java 8 / Spigot 1.8.8 baseline, compatibility helpers and bStats integration. Both builds use the root Wrapper and read the same root `gradle.properties` version.
- Preserve the existing `me.lele.worldSafe` Java package spelling, including its uppercase `S`, on case-insensitive filesystems.

## Build

Run from the repository root with Gradle on JDK 25:

```sh
./gradlew --no-daemon -p fabric check shadowJar
python3 scripts/verify-fabric-code.py "fabric/build/libs/WorldSafe-Fabric-$(python3 scripts/project_version.py).jar"
```

The root `build` task also runs this independent build and final JAR verification, alongside Bukkit (which needs a JDK 17 toolchain). Use `./gradlew :fabric:build` (or `fabricBuild`) for Fabric alone, or `./gradlew :bukkit:build` (or `bukkitBuild`) for Bukkit alone without configuring the game build. The root build exposes a lightweight `fabric` lifecycle project using `platform.gradle`, so IDEA shows sibling `bukkit`, `fabric` and `shared` nodes while the actual Fabric build remains independent. Root `clean` cleans both platforms; `fabricClean` cleans only Fabric. Both remove the entire `fabric/build` directory, including optional game worlds and reports.

The default includes **all 48 locked releases**. A development-only subset can be selected with `-Ptargets=1.14,1.21.11,26.3`; it cannot pass the universal-artifact acceptance check. Do not run `clean` while preserving generated development worlds or reports under `build`.

`check` runs shared-core tests and isolation checks (outputs under `build/shared`), Fabric JUnit/Mockito policy and lifecycle tests, Python packaging/report tests, and offline native-hook checks. `verify-native.gradle` checks the final remapped modules against their corresponding game classes: native fields, method descriptors, callback signatures, required invoke sites, and complete Mixin registration. Missing hooks fail verification. It does not launch Minecraft.

`verify-fabric-code.py` requires every locked module, matches each embedded module hash to its native verification report, checks successful Fabric and shared-core unit-test results, and writes `build/code-report.json`. It also checks Java bytecode targets, namespace consistency, exact module selection, the minimum Loader requirement in both the shell and every runtime, entrypoints, signatures, and exclusion of game, Loader, metrics and test classes from the release artifact.

The release workflow waits for Bukkit verification, the Bukkit API matrix, and Fabric code verification on the same commit. It uploads the exact verified artifacts and their SHA-256 sidecars, plus the Fabric code report, without rebuilding during release. This migration does not bump the project version or publish a release.

## Configuration and lifecycle

Fabric uses `config/worldsafe/config.yml`. Only TNT and creeper block protection are configured by default. Protection keys and minimum versions match Bukkit. Native dimensions such as `minecraft:overworld` are accepted alongside `world`, `world_nether`, and `world_the_end`. Custom dimensions require a full `namespace:path` ID.

Invalid initial configuration aborts startup; failed reloads preserve the previous snapshot. State belongs to the server instance and is removed on shutdown. Remote client worlds have no local protection snapshot. Console, OP level 2 or above, and the integrated-server owner may administer WorldSafe. Fabric accepts `enabled-bstats` but uploads no metrics.

See the root [English](../README.md) and [Chinese](../README_CN.md) guides and the [workspace compatibility report](reports/compatibility.md).

## Optional game test tools

The `architectureDrivers` task and the `run-fabric-*-probe.py` / `run-fabric-matrix.py` scripts are optional development tools retained separately from the release artifact. They are not invoked by code acceptance or release CI. Historical probe results are partial and must not be presented as full in-game certification. `verify-fabric-report.py` checks that optional, stricter behavior-report format and is separate from the current code-only release gate.

## Loader startup verification

Both startup probes accept `--loader`; omitting it uses the lock's build Loader (0.20.1). Runs and reports are isolated under `build/architecture-runs/<loader>/` and `build/architecture-reports/<loader>/`. The integrated probe copies the world from the dedicated probe for the same game and Loader. Each report records the selected Loader and the universal JAR's SHA-256.

On 2026-11-04, the same WorldSafe 1.1.2 universal JAR (SHA-256 `b24ac103718e3deeb4443090de038b922f2dba79f1bdbcb7dd29ebe4cc534450`) passed the 48-module code/package checks and the following Minecraft 26.3 startup checks on JDK 25:

| Loader | Dedicated server | Client integrated server | Protection behavior |
| --- | --- | --- | --- |
| 0.19.5 | Passed | Passed | Not tested in this run |
| 0.20.1 | Passed | Passed | Not tested in this run |

Build the universal JAR first, then build only the external 26.3 test driver (this does not rebuild the universal JAR):

```sh
./gradlew --no-daemon -p fabric -Ptargets=26.3 :driver_mc26_3:jar
version="$(python3 scripts/project_version.py)"
for loader in 0.19.5 0.20.1; do
  # --accept-eula requires acceptance of the Minecraft server EULA.
  python3 scripts/run-fabric-architecture-probe.py --game 26.3 --loader "$loader" \
    --jar "fabric/build/libs/WorldSafe-Fabric-$version.jar" --java-home "$JAVA_HOME" --accept-eula
  python3 scripts/run-fabric-client-architecture-probe.py --game 26.3 --loader "$loader" \
    --jar "fabric/build/libs/WorldSafe-Fabric-$version.jar" --java-home "$JAVA_HOME" \
    --driver "fabric/build/targets/driver_mc26_3/build/libs/WorldSafe-Architecture-Driver-26.3-$version-mc.26.3.jar"
done
```

The client probe requires a graphical desktop. These checks cover mod resolution, exact native runtime selection, required Mixins, server startup and command execution. Without `--behavior`, they do not certify protection behavior or other game releases. Keep code verification, dedicated startup and integrated startup results separate; an unavailable client environment is unverified, not a pass.
