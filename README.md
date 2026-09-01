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

Find your server version, then read the version groups from top to bottom. You can use every group whose version is not newer than your server.

<details open>
<summary><strong>Minecraft 1.8.8+</strong></summary>

### Direct Explosion Cancellation
*(If you configure an explosion cancellation option, you do not also need its block-protection counterpart.)*

- **`bedExplosionCancel`** - Prevent bed explosions. Do not add overworld worlds to this option.
- **`tntExplosionCancel`** - Fully prevent TNT and TNT minecart explosions.
- **`creeperExplosionCancel`** - Fully prevent creeper explosions.
- **`endCrystalExplosionCancel`** - Fully prevent end crystal explosions.
- **`ghastExplosionCancel`** - Prevent ghast fireball explosions and damage.
- **`witherExplosionCancel`** - Fully prevent wither and wither skull explosions.

### Prevent Block Destruction but Keep Damage

- **`bedExplosionProtection`** - Prevent bed explosions from destroying blocks while keeping explosion damage.
- **`tntExplosionProtection`** - Prevent TNT and TNT minecart explosions from destroying blocks.
- **`creeperExplosionProtection`** - Prevent creeper explosions from destroying blocks.
- **`endCrystalExplosionPrevention`** - Prevent end crystal explosions from destroying blocks.
- **`ghastExplosionProtection`** - Prevent ghast fireballs from destroying blocks while keeping damage.
- **`witherExplosionProtection`** - Prevent withers and wither skulls from destroying blocks.

### Other Map Protections

- **`cropTrampleProtection`** - Prevent farmland from being trampled into dirt.
- **`dragonEggTeleportationPrevention`** - Prevent dragon eggs from teleporting when touched.
- **`fireSpreadPrevention`** - Prevent fire from spreading and burning nearby blocks.
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

- **`windChargeBlockDestructionProtection`** - Prevent wind charges from breaking decorated pots, chorus flowers, and pointed dripstone while keeping impact damage.
- **`breezeWindChargeImpactCancel`** - Fully cancel breeze wind-charge impacts without affecting wind charges fired by players.
- **`weavingCobwebFormationPrevention`** - Prevent the weaving effect from creating cobwebs.

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### Direct Explosion Cancellation

- **`sulfurCubeExplosionCancel`** - Fully prevent sulfur cube explosions caused by consumed TNT.

### Prevent Block Destruction but Keep Damage

- **`sulfurCubeExplosionProtection`** - Prevent sulfur cube explosions from destroying blocks while keeping explosion damage.

</details>

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe is licensed under the MIT License.
