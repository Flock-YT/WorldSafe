**English** | [中文](README_CN.md)

# WorldSafe

<img src="assets/branding/worldsafe-logo.png" alt="WorldSafe logo" width="160" height="160">

WorldSafe is a lightweight Bukkit plugin that prevents various mobs/entities and game mechanics from damaging worlds.

## Compatibility

- Supports **Minecraft 1.8.8 through 26.3**.
- Works with commonly used Bukkit, Spigot, and Paper servers.

Features that are only available in newer versions are automatically skipped on older servers, while all other available features continue to work normally. If the configuration fails to reload, the plugin continues using the previous working settings.

## Installation

1. Put `WorldSafe-<version>.jar` in the server's `plugins` directory.
2. Restart the server.
3. Configure the list of worlds where the plugin should take effect in `plugins/WorldSafe/config.yml`.

The default configuration contains only the main switch, the bStats switch, and example block protections for creepers and TNT. Add other configuration options from the [feature availability table](#feature-availability-table) as needed; features that are not configured are disabled by default.

## Commands and Permissions

- `/worldsafe help` - View command help.
- `/worldsafe reload` - Reload the configuration.
- `worldsafe.admin` - Allows the use of WorldSafe administration commands; available only to operators by default.

## Feature Availability Table

First, confirm your server version, then read the version groups from top to bottom. You can use the features in any group whose version is not newer than your server version.

> For example, if you are running version 1.13, you can use all configuration options in both the 1.8.8+ and 1.13 groups,
> but you cannot use configuration options for 1.14+ or later versions (because some things in newer versions do not exist in older versions).

<details open>
<summary><strong>Minecraft 1.8.8+</strong></summary>

### Direct Explosion Cancellation

*(If you configure the full cancellation option for an explosion type, you do not need to configure the corresponding block protection option.)*

- **`bedExplosionCancel`** - Fully prevent standard bed explosions. Do not add the Overworld to this configuration. Straw beds are not included because they do not explode.
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

- **`windChargeBlockDestructionProtection`** - Prevent wind charges fired by players and breezes from breaking decorated pots, chorus flowers, and pointed dripstone through explosion lists and direct-hit block-change events, while retaining impact damage and knockback. This does not depend on `decoratedPotProjectileProtection` and does not cancel the entire projectile hit. Coverage depends on whether the server provides the corresponding events; a type of hit that does not normally break blocks cannot be used as proof that the protection is working.
- **`breezeWindChargeImpactCancel`** - Fully cancel breeze wind charge impacts without affecting wind charges fired by players. On non-Paper servers, this can only run in best-effort mode; damage or knockback may remain because Spigot does not provide the required early explosion hook.
- **`weavingCobwebFormationPrevention`** - Prevent the weaving effect from creating cobwebs.

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### Direct Explosion Cancellation

- **`sulfurCubeExplosionCancel`** - Fully prevent explosions caused when sulfur cubes consume TNT. On non-Paper servers, this can only run in best-effort mode; damage or knockback may remain because Spigot does not provide the required early explosion hook.

### Prevent Block Destruction but Keep Damage

- **`sulfurCubeExplosionProtection`** - Prevent sulfur cube explosions from destroying blocks while keeping explosion damage.

</details>

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe is licensed under the MIT License.
