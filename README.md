**English** | [中文](README_CN.md)

# 🌍 WorldSafe

**WorldSafe** is a lightweight Minecraft Bukkit plugin designed to prevent certain entities and items from damaging the map.

## 📋 Installation Requirements

- **Java 21**: This plugin requires Java 21 runtime environment.

## ✨ Features

- **Lightweight**: Minimal performance impact, only loads necessary listeners, and minimizes irrelevant checks.
- **Easy Configuration**: User-friendly and simple configuration.

## 📖 Usage

1. Download the plugin and place it in the `plugins` folder.
2. Restart the server to load the plugin.
3. Make necessary settings in the configuration file.

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

## 🧱 Block Classes

### Direct Explosion Cancellation
*(If you configure items in this class, there's no need to configure items in the "Cancel block destruction but keep damage" class below)*

- ✅ **bedExplosionCancel** - Prevent bed explosions (this configuration prevents players from using beds, which will cause them to be unable to sleep in the overworld)
- ✅ **respawnAnchorExplosionCancel** - Prevent respawn anchor explosions
- ✅ **tntExplosionCancel** - Prevent TNT explosions

### Cancel Block Destruction but Keep Damage

- ✅ **bedExplosionProtection** - Prevent bed explosions from destroying blocks
- ✅ **respawnAnchorExplosionPrevention** - Prevent respawn anchor explosions from destroying blocks
- ✅ **tntExplosionProtection** - Prevent TNT explosions from destroying blocks

### Other Classes

- ✅ **cropTrampleProtection** - Prevent crops from being trampled
- ✅ **dragonEggTeleportationPrevention** - Prevent dragon egg teleportation

## 🧬 Entity Classes

### Direct Explosion Cancellation
*(If you configure items in this class, there's no need to configure items in the "Cancel block destruction but keep explosion damage" class below)*

- ✅ **creeperExplosionCancel** - Prevent creeper explosions
- ✅ **endCrystalExplosionCancel** - Prevent end crystal explosions
- ✅ **ghastExplosionCancel** - Prevent ghast fireball explosions
- ✅ **witherExplosionCancel** - Prevent wither explosions

### Cancel Block Destruction but Keep Explosion Damage

- ✅ **creeperExplosionProtection** - Prevent creeper explosions from destroying blocks
- ✅ **endCrystalExplosionPrevention** - Prevent end crystal explosions from destroying blocks
- ✅ **ghastExplosionProtection** - Prevent ghast fireball explosions from destroying blocks
- ✅ **witherExplosionProtection** - Prevent wither explosions from destroying blocks

### Other Classes

- ✅ **enderDragonBlockDestructionProtection** - Prevent ender dragon from destroying blocks
- ✅ **enderManBlockPickupProtection** - Prevent endermen from picking up blocks
- ✅ **phantomDamagePrevention** - Prevent phantoms from causing damage

---

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

**Copyright Notice**: This plugin is developed by [Eric.乐乐 & 追求at](#), and follows the [MIT License](#).
