[English](README.md) | **中文**

# WorldSafe

WorldSafe 是一个轻量级 Bukkit 插件，用于阻止指定实体和游戏机制破坏已配置的世界。

## 兼容范围

- 支持 **Minecraft 1.8.8 至 26.2**。
- 支持常见的 Bukkit、Spigot 和 Paper 服务端。

较新版本才有的功能会在旧版本服务端上自动跳过，其他可用的保护功能仍会正常运行。如果配置重载失败，插件会继续使用之前正常工作的设置。

## 安装

1. 将 `WorldSafe-<version>.jar` 放入服务端的 `plugins` 目录。
2. 重启服务端。
3. 在 `plugins/WorldSafe/config.yml` 中配置需要生效的世界列表。

## 命令与权限

- `/worldsafe help` - 查看命令帮助。
- `/worldsafe reload` - 重载配置。
- `worldsafe.admin` - 允许使用 WorldSafe 管理命令，默认仅 OP 拥有。

## 功能版本表

只要分组的最低版本不高于你的服务端版本，即可使用该组功能。

### Minecraft 1.8.8+

直接取消爆炸：

- `bedExplosionCancel`
- `tntExplosionCancel`
- `creeperExplosionCancel`
- `endCrystalExplosionCancel`
- `ghastExplosionCancel`
- `witherExplosionCancel`

保留爆炸伤害但阻止方块破坏：

- `bedExplosionProtection`
- `tntExplosionProtection`
- `creeperExplosionProtection`
- `endCrystalExplosionPrevention`
- `ghastExplosionProtection`
- `witherExplosionProtection`

其他保护：

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

WorldSafe 遵循 MIT 许可证。
