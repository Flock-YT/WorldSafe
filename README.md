**English** | [中文](README_CN.md)

# WorldSafe

<img src="assets/branding/worldsafe-logo.png" alt="WorldSafe logo" width="160" height="160">

WorldSafe prevents mobs, entities, and game mechanics from damaging worlds. It has Gradle builds for a Bukkit plugin and an independent Fabric mod sharing the same 36 protection definitions.

## Compatibility

- **Bukkit:** Minecraft **1.8.8–26.3**, including Bukkit, Spigot, and Paper. The plugin and shared core retain Java 8 bytecode and the Spigot 1.8.8 API baseline.
- **Fabric:** the pinned target is all **48 stable releases from 1.14 to 26.3**, in one universal JAR. The complete version matrix is verified by compilation, offline native-hook checks and code tests; see [Fabric verification](fabric/README.md). This is not a full in-game behavior certification. Snapshots, prereleases, and mechanics added by other mods are outside this target.

Features that are only available in newer versions are automatically skipped on older servers, while all other available features continue to work normally. If the configuration fails to reload, the plugin continues using the previous working settings.

## Installation

1. Put `WorldSafe-<version>.jar` in the server's `plugins` directory.
2. Restart the server.
3. Configure the list of worlds where the plugin should take effect in `plugins/WorldSafe/config.yml`.

The default configuration contains only the main switch, the bStats switch, and example block protections for creepers and TNT. Add other configuration options from the [feature availability table](#feature-availability-table) as needed; features that are not configured are disabled by default.

## Fabric installation and configuration

Install Fabric Loader **0.19.5 or newer** for the exact game release, then put `WorldSafe-Fabric-<version>.jar` in `mods`. The build uses Loader 0.20.1; the shell and every nested runtime require only 0.19.5. Fabric API is not required. Connecting players do not need the mod on their clients. For singleplayer, install it locally; protection runs in that world’s integrated server and also applies when it is opened to LAN. Installing it on a client has no local protection effect while connected to a remote server.

Other mods must also match your game release. For example, a GCA build requiring Minecraft 26.4 cannot run on 26.3; use a GCA build for 26.3 or remove it. Changing WorldSafe's Loader requirement does not resolve that separate conflict. See [Loader startup verification](fabric/README.md#loader-startup-verification) for test scope and commands.

| Minecraft | Minimum Java |
| --- | --- |
| 1.14–1.16.5 | 8 |
| 1.17–1.17.1 | 16 |
| 1.18–1.20.4 | 17 |
| 1.20.5–1.21.11 | 21 |
| 26.1–26.3 | 25 |

Fabric uses `config/worldsafe/config.yml`. Configuration keys and feature minimum versions match Bukkit. Only TNT and creeper block protection are configured by default; unconfigured protections and dimensions are disabled. For example:

```yaml
enabled: true
enabled-bstats: false
tntExplosionProtection:
  - minecraft:overworld
  - minecraft:the_nether
  - minecraft:the_end
```

The legacy aliases `world`, `world_nether`, and `world_the_end` map to those three dimension IDs. Use a full ID such as `example:moon` for custom dimensions. An invalid initial Fabric configuration aborts server startup; a failed reload retains the previous immutable settings. Each integrated server loads fresh settings and clears its state when stopped.

Fabric permits console administration, OP level 2 or higher, and the singleplayer owner. Ordinary LAN visitors cannot administer WorldSafe. LuckPerms integration is not included. Fabric uploads no metrics: it accepts `enabled-bstats` for configuration compatibility and warns that `true` has no effect. Bukkit bStats behavior is unchanged.

## Project structure and adding platforms

- `bukkit/`: Bukkit / Spigot / Paper entrypoint, listeners, commands, configuration I/O and compatibility helpers.
- `fabric/`: independent Fabric build with a Java 8 bootstrap, version-specific runtimes and development-only test drivers.
- `shared/`: Java 8 core containing the feature catalog, configuration model, version matching and protection policies, with its own unit tests.
- Repository root: version, Wrapper, build coordination, CI and repository-wide checks.

Add future Forge or NeoForge support in a sibling platform directory that depends on `shared`. Keep events, permissions, commands and configuration I/O in platform modules; extract common adapters only when implementations actually repeat. The shared library is not released separately.

Run shared tests with `./gradlew :shared:test`. The root build writes shared outputs to `shared/build`; Fabric writes them to `fabric/build/shared`. Shared code uses the build JVM while emitting Java 8 bytecode: JDK 17 for Bukkit-only builds and JDK 25 for Fabric and the default build of both platforms.

## Building and verification

Both platforms use the root Gradle Wrapper. Its Gradle version must match `fabric/versions.lock.json` (currently 9.8.2) when building Fabric or running the root `build`. The single version source is root `gradle.properties`; Maven is not needed. Install Python 3 for the verification scripts and Git for release-detection tests. The independent `shared` Java library contains no platform types or production dependencies. Both platforms depend on it and bundle it into their release JARs.

```sh
# Both platforms, including tests and final JAR checks: run Gradle on JDK 25
./gradlew clean build

# Bukkit only: JDK 17, includes shared tests and release JAR checks
./gradlew bukkitClean bukkitBuild

# Fabric only: run Gradle on JDK 25, checks all 48 pinned modules and the final JAR
./gradlew fabricClean fabricBuild

# Clean both platforms, including Fabric test worlds: JDK 17 is sufficient
./gradlew clean
```

Bukkit outputs `bukkit/build/libs/WorldSafe-<version>.jar`; Fabric outputs `fabric/build/libs/WorldSafe-Fabric-<version>.jar`. Only shaded release JARs are produced. Bukkit compilation and tests default to a JDK 17 toolchain while emitting Java 8 bytecode; the root `build` therefore needs both JDK 17 and JDK 25 installed. Use Gradle toolchain discovery or `-Porg.gradle.java.installations.paths=<jdk17>,<jdk25>` to identify installations.

For a focused Bukkit test, use `./gradlew :bukkit:test --tests '<TestClass>'`. API compilation uses `./gradlew :bukkit:clean :bukkit:compileJava -PspigotVersion=<api-version> -PbukkitJava=<jdk-major>` with the API/JDK pairs from CI. A Bukkit-only build does not configure the independent Fabric game build. Root `build` builds and verifies both platforms; root `clean` deletes the root, Bukkit, shared and entire `fabric/build` directories without configuring Fabric, so cleaning only requires JDK 17. Back up any optional game worlds under `fabric/build` before running `clean` or `fabricClean`. Use `bukkitBuild` / `bukkitClean` and `fabricBuild` / `fabricClean` for individual platforms. In IDEA, the root run configurations contain only `WorldSafe [build]` / `WorldSafe [clean]`. The sibling `bukkit`, `fabric` and `shared` project nodes expose their own tasks; expand `bukkit` or `fabric` → `Tasks` → `build` to run that platform’s `build` / `clean`. The Fabric node uses `fabric/platform.gradle` to coordinate the independent build without configuring game runtimes during project import. The corresponding CLI commands are `./gradlew :bukkit:build` / `:bukkit:clean` and `./gradlew :fabric:build` / `:fabric:clean`.

Pushing a change to `version` in root `gradle.properties` on `master` triggers release checks. Changes to other properties with the same version do not publish; the Maven-to-Gradle migration compares against the old POM version without requiring Maven.
The Fabric version lock fixes game releases, mappings, Java targets, and build tools. Native modules have exact game dependencies and remain inside one universal JAR; external behavior test drivers are excluded. See [Fabric build and acceptance commands](fabric/README.md). Compilation and startup are reported separately from native behavior acceptance. Release uses the same JAR hash covered by code verification. Full in-game tests are optional and are not the acceptance gate for this migration.

## Commands and Permissions

- `/worldsafe help` - View command help.
- `/worldsafe reload` - Reload the configuration.
- Bukkit: `worldsafe.admin` permits administration and defaults to operators. Fabric uses the console/OP 2/singleplayer-owner rules above.

## Feature Availability Table

For Fabric, the platform baseline remains 1.14 even where the feature’s original minimum is older. First, confirm your server version, then read the version groups from top to bottom. You can use the features in any group whose version is not newer than your server version.

> For example, if you are running version 1.13, you can use all configuration options in both the 1.8.8+ and 1.13 groups,
> but you cannot use configuration options for 1.14+ or later versions (because some things in newer versions do not exist in older versions).

<details open>
<summary><strong>Minecraft 1.8.8+</strong></summary>

### Direct Explosion Cancellation

*(If you configure the full cancellation option for an explosion type, you do not need to configure the corresponding block protection option.)*

- **`bedExplosionCancel`** - Fully prevent standard bed explosions. On Bukkit, do not add the Overworld to this configuration; Fabric follows the vanilla dimension bed rules. Straw beds are not included because they do not explode.
- **`tntExplosionCancel`** - Fully prevent TNT and TNT minecart explosions.
- **`creeperExplosionCancel`** - Fully prevent creeper explosions.
- **`endCrystalExplosionCancel`** - Fully prevent end crystal explosions.
- **`ghastExplosionCancel`** - Prevent ghast fireball explosions and cancel fireball damage.
- **`witherExplosionCancel`** - Fully prevent wither and wither skull explosions.

### Prevent Block Destruction but Keep Damage

- **`bedExplosionProtection`** - Protect blocks around standard bed explosions while retaining explosion damage. Straw beds are not included because they do not explode.
- **`tntExplosionProtection`** - Prevent TNT and TNT minecart explosions from destroying blocks.
- **`creeperExplosionProtection`** - Prevent creeper explosions from destroying blocks.
- **`endCrystalExplosionPrevention`** - Prevent end crystal explosions from destroying blocks.
- **`ghastExplosionProtection`** - Prevent ghast fireballs from destroying blocks while keeping damage.
- **`witherExplosionProtection`** - Prevent withers and wither skulls from destroying blocks.

### Other Map Protections

- **`cropTrampleProtection`** - Prevent farmland from being trampled into dirt.
- **`dragonEggTeleportationPrevention`** - Prevent dragon eggs from teleporting when touched.
- **`fireSpreadPrevention`** - Prevent fire from spreading and burning nearby blocks. Fabric cancels the burn operation before it can prime TNT, preventing repeated TNT priming from a protected block; fire can still extinguish naturally.
- **`fireIgnitionPrevention`** - Prevent fireballs, lightning, explosions, end crystals, and burning arrows from igniting blocks.
- **`enderDragonBlockDestructionProtection`** - Prevent the Ender Dragon from destroying blocks.
- **`enderManBlockPickupProtection`** - Prevent endermen from picking up or moving blocks.
- **`silverfishBlockChangeProtection`** - Prevent silverfish from entering or breaking infested blocks.
- **`rabbitCropEatingProtection`** - Prevent rabbits from eating crops.
- **`sheepGrassEatingProtection`** - Prevent sheep from eating grass.
- **`villagerCropModificationProtection`** - Prevent villagers from harvesting or planting crops.
- **`mobDoorBreakProtection`** - Prevent mobs from breaking doors.
- **`snowGolemSnowTrailPrevention`** - Prevent snow golems from leaving snow trails.

</details>

<details>
<summary><strong>Minecraft 1.13+</strong></summary>

- **`phantomDamagePrevention`** - Prevent phantoms from damaging players or other entities.

</details>

<details>
<summary><strong>Minecraft 1.14+</strong></summary>

- **`ravagerBlockDestructionProtection`** - Prevent ravagers from destroying blocks.
- **`foxBerryHarvestProtection`** - Prevent foxes from harvesting sweet berries.
- **`witherRoseFormationPrevention`** - Prevent wither roses from forming.

</details>

<details>
<summary><strong>Minecraft 1.16+</strong></summary>

### Direct Explosion Cancellation

- **`respawnAnchorExplosionCancel`** - Prevent charged respawn anchors from exploding outside the Nether.

### Prevent Block Destruction but Keep Damage

- **`respawnAnchorExplosionPrevention`** - Prevent respawn anchor explosions from destroying blocks while keeping explosion damage.

</details>

<details>
<summary><strong>Minecraft 1.20.3+</strong></summary>

- **`decoratedPotProjectileProtection`** - Prevent projectiles from breaking decorated pots.

</details>

<details>
<summary><strong>Minecraft 1.21+</strong></summary>

- **`windChargeBlockDestructionProtection`** - Prevent wind charges fired by players and breezes from breaking decorated pots, chorus flowers, and pointed dripstone through explosion lists and direct-hit block-change events, while retaining impact damage and knockback. This does not depend on `decoratedPotProjectileProtection` and does not cancel the entire projectile hit. On Bukkit, coverage depends on whether the server provides the corresponding events; a type of hit that does not normally break blocks cannot be used as proof that the protection is working.
- **`breezeWindChargeImpactCancel`** - Fully cancel breeze wind charge impacts without affecting wind charges fired by players. On Bukkit/Spigot servers without Paper, this can only run in best-effort mode; damage or knockback may remain because Spigot does not provide the required early explosion hook.
- **`weavingCobwebFormationPrevention`** - Prevent the weaving effect from creating cobwebs.

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### Direct Explosion Cancellation

- **`sulfurCubeExplosionCancel`** - Fully prevent explosions caused when sulfur cubes consume TNT. On Bukkit/Spigot servers without Paper, this can only run in best-effort mode; damage or knockback may remain because Spigot does not provide the required early explosion hook.

### Prevent Block Destruction but Keep Damage

- **`sulfurCubeExplosionProtection`** - Prevent sulfur cube explosions from destroying blocks while keeping explosion damage.

</details>

The following bStats chart counts Bukkit installations only.

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe is licensed under the MIT License.
