**English** | [中文](README_CN.md)

# WorldSafe

WorldSafe is a lightweight Bukkit plugin that prevents selected entities and game mechanics from damaging configured worlds.

## Compatibility

- One JAR supports **Minecraft 1.8.8 through 26.2**.
- The plugin is compiled as **Java 8 bytecode**. Run the server with the Java version required by that server release.
- Supported servers are CraftBukkit, Spigot, Paper, and forks that preserve Bukkit public API compatibility.
- Minecraft 1.8.0-1.8.7 are not supported.

WorldSafe compiles against the Spigot 1.8.8 API. Newer functionality is detected by material/entity names and cached public-API reflection. It does not use CraftBukkit, NMS, or server-internal packages.

If a configured feature is unavailable, only that feature is skipped. The console receives one clear warning for that load, for example:

```text
Skipping feature 'respawnAnchorExplosionCancel': requires Minecraft 1.16+; detected 1.12.2
```

All other supported protections continue to run. A malformed configuration reload keeps the previous configuration and listeners active.

## Installation

1. Put `WorldSafe-<version>.jar` in the server's `plugins` directory.
2. Restart the server.
3. Configure world lists in `plugins/WorldSafe/config.yml`.

## Commands And Permission

- `/worldsafe help` - Show command help.
- `/worldsafe reload` - Atomically reload the configuration.
- `worldsafe.admin` - Allows use of WorldSafe administration commands. Defaults to operators.

## Feature Availability

Use every group whose minimum version is not newer than your server.

### Minecraft 1.8.8+

Direct explosion cancellation:

- `bedExplosionCancel`
- `tntExplosionCancel`
- `creeperExplosionCancel`
- `endCrystalExplosionCancel`
- `ghastExplosionCancel`
- `witherExplosionCancel`

Prevent block destruction while retaining explosion damage:

- `bedExplosionProtection`
- `tntExplosionProtection`
- `creeperExplosionProtection`
- `endCrystalExplosionPrevention`
- `ghastExplosionProtection`
- `witherExplosionProtection`

Other protections:

- `cropTrampleProtection`
- `dragonEggTeleportationPrevention`
- `fireSpreadPrevention`
- `fireIgnitionPrevention`
- `enderDragonBlockDestructionProtection`
- `enderManBlockPickupProtection`
- `silverfishBlockChangeProtection`
- `rabbitCropEatingProtection`
- `sheepGrassEatingProtection`
- `villagerCropModificationProtection`
- `mobDoorBreakProtection`
- `snowGolemSnowTrailPrevention`

### Minecraft 1.13+

- `phantomDamagePrevention`

### Minecraft 1.14+

- `ravagerBlockDestructionProtection`
- `foxBerryHarvestProtection`
- `witherRoseFormationPrevention`

### Minecraft 1.16+

- `respawnAnchorExplosionCancel`
- `respawnAnchorExplosionPrevention`

### Minecraft 1.20.3+

- `decoratedPotProjectileProtection`

### Minecraft 1.21+

- `windChargeBlockDestructionProtection`
- `breezeWindChargeImpactCancel`
- `weavingCobwebFormationPrevention`

### Minecraft 26.2+

- `sulfurCubeExplosionCancel`
- `sulfurCubeExplosionProtection`

## Building

Use JDK 17 or newer for development while producing Java 8 bytecode:

```bash
mvn clean verify
./scripts/verify-release-jar.sh
```

The release verifier checks the embedded version, signatures, bundled API classes, and class major version 52.

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe is licensed under the MIT License.
