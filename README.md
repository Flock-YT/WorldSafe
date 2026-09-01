

**English** | [中文](README_CN.md)

# 🌍 WorldSafe

**WorldSafe** is a lightweight Minecraft Bukkit plugin designed to prevent certain entities and items from damaging the map.

## 📋 Installation Requirements

- **Java 17 or newer**
- **Minecraft Java Edition 1.19 or newer**

## ✨ Features

- **Lightweight**: Minimal performance impact, only loads necessary listeners, and minimizes irrelevant checks.
- **Easy Configuration**: User-friendly and simple configuration.
- **Extensible**: Add a new listener and register it in the `WorldSafe.FEATURES` map to make it configurable without touching the loading logic again.

## 📖 Usage

1. Download the plugin and place it in the `plugins` folder.
2. Restart the server to load the plugin.
3. Make necessary settings in the `plugins/WorldSafe/config.yml` configuration file.

## 🛠️ Roadmap

The plugin is continuously being updated. Please stay tuned for future updates. You can submit your requests in the issue section!

## 🔐 Permissions

Currently, there is only one permission: `worldsafe.admin`

## ➡️ Commands

### `/worldsafe help`
View plugin help

### `/worldsafe reload`
Reload the plugin configuration

# Implemented Features

Features are grouped by the minimum Minecraft version they require. A server can use all features listed for its version and every earlier version.

<details open>
<summary><strong>Minecraft 1.19+</strong></summary>

### Direct Explosion Cancellation
*(If you configure an explosion cancellation option, you do not also need its block-protection counterpart.)*

- ✅ **bedExplosionCancel** - Prevent bed explosions “(This configuration should not include the overworld)”
- ✅ **respawnAnchorExplosionCancel** - Prevent respawn anchor explosions
- ✅ **tntExplosionCancel** - Prevent TNT explosions
- ✅ **creeperExplosionCancel** - Prevent creeper explosions
- ✅ **endCrystalExplosionCancel** - Prevent end crystal explosions
- ✅ **ghastExplosionCancel** - Prevent ghast fireball explosions
- ✅ **witherExplosionCancel** - Prevent wither explosions

### Prevent Block Destruction but Keep Damage

- ✅ **bedExplosionProtection** - Prevent bed explosions from destroying blocks
- ✅ **respawnAnchorExplosionPrevention** - Prevent respawn anchor explosions from destroying blocks
- ✅ **tntExplosionProtection** - Prevent TNT explosions from destroying blocks
- ✅ **creeperExplosionProtection** - Prevent creeper explosions from destroying blocks
- ✅ **endCrystalExplosionPrevention** - Prevent end crystal explosions from destroying blocks
- ✅ **ghastExplosionProtection** - Prevent ghast fireball explosions from destroying blocks
- ✅ **witherExplosionProtection** - Prevent wither explosions from destroying blocks

### Other Map Protections

- ✅ **cropTrampleProtection** - Prevent crops from being trampled
- ✅ **dragonEggTeleportationPrevention** - Prevent dragon egg teleportation
- ✅ **fireSpreadPrevention** - Prevent fire from spreading between blocks
- ✅ **fireIgnitionPrevention** - Prevent fireballs, lightning, explosions, end crystals, and burning arrows from igniting blocks
- ✅ **enderDragonBlockDestructionProtection** - Prevent ender dragon from destroying blocks
- ✅ **enderManBlockPickupProtection** - Prevent endermen from picking up blocks
- ✅ **phantomDamagePrevention** - Prevent phantoms from causing damage
- ✅ **ravagerBlockDestructionProtection** - Prevent ravagers from destroying blocks
- ✅ **silverfishBlockChangeProtection** - Prevent silverfish from entering or breaking infested blocks
- ✅ **rabbitCropEatingProtection** - Prevent rabbits from eating crops
- ✅ **sheepGrassEatingProtection** - Prevent sheep from eating grass
- ✅ **villagerCropModificationProtection** - Prevent villagers from harvesting or planting crops
- ✅ **foxBerryHarvestProtection** - Prevent foxes from harvesting berries
- ✅ **mobDoorBreakProtection** - Prevent mobs from breaking doors
- ✅ **snowGolemSnowTrailPrevention** - Prevent snow golems from forming snow trails
- ✅ **witherRoseFormationPrevention** - Prevent wither rose formation

</details>

<details>
<summary><strong>Minecraft 1.20.3+</strong></summary>

- ✅ **decoratedPotProjectileProtection** - Prevent projectiles from breaking decorated pots

</details>

<details>
<summary><strong>Minecraft 1.21+</strong></summary>

- ✅ **windChargeBlockDestructionProtection** - Protect decorated pots, chorus flowers, and pointed dripstone from wind charges while retaining impact damage
- ✅ **breezeWindChargeImpactCancel** - Fully cancel breeze wind-charge impacts without affecting player wind charges
- ✅ **weavingCobwebFormationPrevention** - Prevent weaving from forming cobwebs

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### Direct Explosion Cancellation

- ✅ **sulfurCubeExplosionCancel** - Prevent sulfur cube TNT-fed explosions

### Prevent Block Destruction but Keep Damage

- ✅ **sulfurCubeExplosionProtection** - Prevent sulfur cube explosions from destroying blocks

</details>

---

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

**Copyright Notice**: This plugin is developed by [Eric.乐乐 & 追求at](#), and follows the [MIT License](#).
