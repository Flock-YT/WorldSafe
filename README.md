**English** | [中文](README_CN.md)

# 🌍 WorldSafe

**WorldSafe** is a lightweight Minecraft Bukkit plugin designed to prevent certain entities and items from destroying the map.

## 📋 Installation Requirements

- **Java 21**: This plugin requires Java 21 runtime environment.

## ✨ Features

- **Lightweight**: Minimal impact on performance, only loads necessary listeners to minimize unrelated checks.
- **Easy Configuration**: User-friendly and simple to configure.

## 📖 Usage

1. Download the plugin and place it in the `plugins` folder.
2. Restart the server to load the plugin.
3. Make necessary settings in the configuration file.

## 🛠️ Planned Features

The plugin is currently undergoing updates. Please keep an eye on future updates!

# Currently Implemented Features

## 🧱 Block Types

### Direct Explosion Cancellation
*(If you configure the items in this category, the corresponding “Block Destruction Prevention with Explosion Damage Retained” category below can be left unconfigured)*

- ✅ **bedExplosionCancel** - Prevent bed explosions (This configuration prevents players from using beds; if the Overworld is included, players will not be able to sleep.)
- ✅ **respawnAnchorExplosionCancel** - Prevent respawn anchor explosions
- ✅ **tntExplosionCancel** - Prevent TNT explosions

### Block Destruction Prevention with Explosion Damage Retained

- ✅ **bedExplosionProtection** - Prevent bed explosions from destroying blocks
- ✅ **respawnAnchorExplosionPrevention** - Prevent respawn anchor explosions from destroying blocks
- ✅ **tntExplosionProtection** - Prevent TNT explosions from destroying blocks

### Other Types

- ✅ **cropTrampleProtection** - Prevent crops from being trampled
- ✅ **dragonEggTeleportationPrevention** - Prevent dragon egg teleportation

## 🧬 Entity Types

### Direct Explosion Cancellation
*(If you configure the items in this category, the corresponding “Block Destruction Prevention with Explosion Damage Retained” category below can be left unconfigured)*

- ✅ **creeperExplosionCancel** - Prevent creeper explosions
- ✅ **endCrystalExplosionCancel** - Prevent end crystal explosions
- ✅ **ghastExplosionCancel** - Prevent ghast fireball explosions
- ✅ **witherExplosionCancel** - Prevent wither explosions

### Block Destruction Prevention with Explosion Damage Retained

- ✅ **creeperExplosionProtection** - Prevent creeper explosions from destroying blocks
- ✅ **endCrystalExplosionPrevention** - Prevent end crystal explosions from destroying blocks
- ✅ **ghastExplosionProtection** - Prevent ghast fireball explosions from destroying blocks
- ✅ **witherExplosionProtection** - Prevent wither explosions from destroying blocks

### Other Types

- ✅ **enderDragonBlockDestructionProtection** - Prevent the Ender Dragon from destroying blocks
- ✅ **enderManBlockPickupProtection** - Prevent Endermen from picking up blocks

---

**Copyright Notice**: This plugin is developed by [Eric.乐乐 & 追求at](#), and follows the [MIT License](#).
