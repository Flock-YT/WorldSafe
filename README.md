**English** | [中文](README_CN.md)

# WorldSafe

WorldSafe is a lightweight Bukkit plugin that prevents selected entities and game mechanics from damaging configured worlds.

## Compatibility

- Supports **Minecraft 1.8.8 through 26.2**.
- Works with commonly used Bukkit, Spigot, and Paper servers.

Features added in newer Minecraft versions are automatically ignored on older servers. Other available protections continue to work normally. If a configuration reload fails, the previous working settings remain active.

## Installation

1. Put `WorldSafe-<version>.jar` in the server's `plugins` directory.
2. Restart the server.
3. Configure world lists in `plugins/WorldSafe/config.yml`.

## Commands And Permission

- `/worldsafe help` - Show command help.
- `/worldsafe reload` - Reload the configuration.
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

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe is licensed under the MIT License.
