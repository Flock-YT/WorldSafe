# Fabric build and code verification

WorldSafe produces one Bukkit JAR and one universal Fabric JAR. The Fabric build covers the 48 stable Minecraft releases pinned in `versions.lock.json`, from 1.14 through 26.3. Acceptance for this migration is **code-only**, following the updated task scope; a full in-game behavior matrix is not required or claimed.

## Architecture

- The Java 8 shared core lives in `../shared/src/main/java`. It defines the 36 protection keys, minimum versions, immutable configuration, validation, dimension matching, and explosion decisions without platform types.
- The Java 8 Fabric shell contains the core and privately relocated SnakeYAML. It requires one nested `worldsafe_runtime` module. Every candidate has a distinct version and exact Minecraft dependency; Loader selects the compatible candidate.
- `generate-native.gradle` compiles required native Mixins for each pinned game. It isolates game types, mappings and Java requirements inside that module. Older modules are remapped before nesting; 26.x uses official, unmapped names. Fabric API is not required.
- Bukkit has its own root Gradle build, retaining the Java 8 / Spigot 1.8.8 baseline, compatibility helpers and bStats integration. Both builds use the root Wrapper and read the same root `gradle.properties` version.
- Preserve the existing `me.lele.worldSafe` Java package spelling, including its uppercase `S`, on case-insensitive filesystems.

## Build

Run from the repository root with Gradle on JDK 25:

```sh
./gradlew --no-daemon -p fabric check shadowJar
python3 scripts/verify-fabric-code.py fabric/build/libs/WorldSafe-Fabric-1.1.1.jar
```

The root `buildAll` task also runs this independent build and final JAR verification, alongside Bukkit (which needs a JDK 17 toolchain). A Bukkit-only build does not configure Fabric.

The default includes **all 48 locked releases**. A development-only subset can be selected with `-Ptargets=1.14,1.21.11,26.3`; it cannot pass the universal-artifact acceptance check. Do not run `clean` while preserving generated development worlds or reports under `build`.

`check` runs Fabric JUnit/Mockito policy and lifecycle tests, Python packaging/report tests, and offline native-hook checks. `verify-native.gradle` checks the final remapped modules against their corresponding game classes: native fields, method descriptors, callback signatures, required invoke sites, and complete Mixin registration. Missing hooks fail verification. It does not launch Minecraft.

`verify-fabric-code.py` requires every locked module, matches each embedded module hash to its native verification report, checks successful unit-test results, and writes `build/code-report.json`. It also checks Java bytecode targets, namespace consistency, exact module selection, entrypoints, signatures, and exclusion of game, Loader, metrics and test classes from the release artifact.

The release workflow waits for Bukkit verification, the Bukkit API matrix, and Fabric code verification on the same commit. It uploads the exact verified artifacts and their SHA-256 sidecars, plus the Fabric code report, without rebuilding during release. This migration does not bump the project version or publish a release.

## Configuration and lifecycle

Fabric uses `config/worldsafe/config.yml`. Only TNT and creeper block protection are configured by default. Protection keys and minimum versions match Bukkit. Native dimensions such as `minecraft:overworld` are accepted alongside `world`, `world_nether`, and `world_the_end`. Custom dimensions require a full `namespace:path` ID.

Invalid initial configuration aborts startup; failed reloads preserve the previous snapshot. State belongs to the server instance and is removed on shutdown. Remote client worlds have no local protection snapshot. Console, OP level 2 or above, and the integrated-server owner may administer WorldSafe. Fabric accepts `enabled-bstats` but uploads no metrics.

See the root [English](../README.md) and [Chinese](../README_CN.md) guides and the [workspace compatibility report](reports/compatibility.md).

## Optional game test tools

The `architectureDrivers` task and the `run-fabric-*-probe.py` / `run-fabric-matrix.py` scripts are optional development tools retained separately from the release artifact. They are not invoked by code acceptance or release CI. Historical probe results are partial and must not be presented as full in-game certification. `verify-fabric-report.py` checks that optional, stricter behavior-report format and is separate from the current code-only release gate.
