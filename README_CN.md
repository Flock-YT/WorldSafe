[English](README.md) | **中文**

# WorldSafe

WorldSafe 是一个轻量级 Bukkit 插件，用于阻止指定实体和游戏机制破坏已配置的世界。

## 兼容范围

- 单个 JAR 正式支持 **Minecraft 1.8.8 至 26.2**。
- 插件使用 **Java 8 字节码**。运行服务端时，请使用该服务端版本自身要求的 Java 版本。
- 支持保持 Bukkit 公共 API 二进制兼容的 CraftBukkit、Spigot、Paper 及其衍生分支。
- 不支持 Minecraft 1.8.0-1.8.7。

WorldSafe 使用 Spigot 1.8.8 API 编译。新版功能通过材质/实体名称和启动时缓存的公共 API 反射实现，不使用 CraftBukkit、NMS 或服务端内部包。

如果配置的功能在当前版本不可用，只会跳过该功能，并在本次加载时输出一次明确警告，例如：

```text
Skipping feature 'respawnAnchorExplosionCancel': requires Minecraft 1.16+; detected 1.12.2
```

其他受支持的保护会继续运行。配置重载时若 YAML 无效，会保留原配置和原监听器。

## 安装

1. 将 `WorldSafe-<version>.jar` 放入服务端的 `plugins` 目录。
2. 重启服务端。
3. 在 `plugins/WorldSafe/config.yml` 中配置需要生效的世界列表。

## 命令与权限

- `/worldsafe help` - 查看命令帮助。
- `/worldsafe reload` - 原子重载配置。
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

## 构建

开发构建使用 JDK 17 或更高版本，产物仍为 Java 8 字节码：

```bash
mvn clean verify
./scripts/verify-release-jar.sh
```

发布校验会检查内嵌版本、签名元数据、误打包的 Bukkit API，以及所有类是否保持 class major 52。

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe 遵循 MIT 许可证。
