[English](README.md) | **中文**

# WorldSafe

<img src="assets/branding/worldsafe-logo.png" alt="WorldSafe logo" width="160" height="160">

WorldSafe 用于阻止生物、实体和游戏机制破坏世界，提供 Bukkit 插件和独立 Fabric 构建，共用全部 36 项保护定义。

## 兼容范围

- **Bukkit：**支持 **Minecraft 1.8.8～26.3**，适用于 Bukkit、Spigot 和 Paper。插件和公共核心继续使用 Java 8 字节码及 Spigot 1.8.8 API 基线。
- **Fabric：**锁定目标为 **1.14～26.3 的全部 48 个正式版**，使用同一个通用 JAR。全版本矩阵通过编译、离线原生钩子检查和代码测试验收，详见 [Fabric 验证说明](fabric/README.md)。这不代表已完成全版本游戏实测。快照、预发布版和其他模组新增机制不在首期目标内。

较新版本才有的功能会在旧版本服务端上自动跳过，其他可用的功能仍会正常运行。如果配置重载失败，插件会继续使用之前正常工作的设置。

## 安装

1. 将 `WorldSafe-<version>.jar` 放入服务端的 `plugins` 目录。
2. 重启服务端。
3. 在 `plugins/WorldSafe/config.yml` 中配置需要生效的世界列表。

默认配置仅保留总开关、bStats 开关，以及苦力怕和 TNT 方块保护示例。其他配置项请按需从[功能版本表](#功能版本表)添加；未配置的功能默认禁用。

## Fabric 安装与配置

为对应游戏版本安装 **Fabric Loader 0.19.5 或更高版本**，将 `WorldSafe-Fabric-<version>.jar` 放入 `mods`。构建使用 Loader 0.20.1，外层模组和全部内嵌 runtime 的最低运行要求均为 0.19.5。无需 Fabric API。连接服务器的玩家无需安装客户端模组；单人游戏需要本地安装，保护运行于该存档的集成服务器，也适用于开放局域网。客户端连接远程服务器时不会产生本地保护效果。

其他模组也必须匹配游戏版本。例如，要求 Minecraft 26.4 的 GCA 不能在 26.3 上运行，需要更换为支持 26.3 的 GCA 或移除它；修改 WorldSafe 的 Loader 要求不会消除该独立冲突。启动验证范围和命令见 [Loader 启动验证](fabric/README.md#loader-startup-verification)。

| Minecraft | 最低 Java 版本 |
| --- | --- |
| 1.14～1.16.5 | 8 |
| 1.17～1.17.1 | 16 |
| 1.18～1.20.4 | 17 |
| 1.20.5～1.21.11 | 21 |
| 26.1～26.3 | 25 |

Fabric 配置位于 `config/worldsafe/config.yml`，配置键和功能最低版本与 Bukkit 一致。默认只配置 TNT 和苦力怕方块保护，其他功能及未配置维度默认关闭。例如：

```yaml
enabled: true
enabled-bstats: false
tntExplosionProtection:
  - minecraft:overworld
  - minecraft:the_nether
  - minecraft:the_end
```

兼容旧别名 `world`、`world_nether`、`world_the_end`，分别对应上述三个维度 ID。自定义维度使用 `example:moon` 这样的完整 ID。Fabric 首次配置无效会中止服务器启动，重载失败会保留旧设置。每个集成服务器实例独立加载配置，停止时清理状态。

Fabric 允许控制台、OP 等级 2 及以上玩家和单人世界房主管理，普通局域网访客没有管理权限，首期不集成 LuckPerms。Fabric 不上传统计数据；兼容读取 `enabled-bstats`，设为 `true` 时会提示该平台暂不生效。Bukkit 的 bStats 行为不变。

## 项目结构与平台接入

- `bukkit/`：Bukkit / Spigot / Paper 入口、监听器、命令、配置读写和兼容辅助类。
- `fabric/`：独立的 Fabric 构建，包含 Java 8 bootstrap、按游戏版本隔离的 runtime，以及不参与发布的测试 driver。
- `shared/`：Java 8 核心，包含功能目录、配置模型、版本判断和保护策略；纯核心测试位于此模块。
- 根目录：统一版本、Wrapper、构建协调、CI 和仓库级验证。

以后增加 Forge、NeoForge 等平台时，新增同级平台目录并依赖 `shared`。平台事件、权限、命令和配置文件读写留在平台模块；出现实际重复实现后再提取公共适配代码。共享库不单独发布。

共享测试使用 `./gradlew :shared:test`。根构建的共享产物位于 `shared/build`，Fabric 构建的共享产物位于 `fabric/build/shared`，互不覆盖。共享代码使用当前构建 JVM 编译并输出 Java 8 字节码；单独构建 Bukkit 使用 JDK 17，Fabric 和默认的两端构建使用 JDK 25。

## 构建与验证

两个平台统一使用根目录 Gradle Wrapper，无需安装 Maven。构建 Fabric 或运行根 `build` 时，Wrapper 的 Gradle 版本必须与 `fabric/versions.lock.json` 一致（目前为 9.8.2）。根 `gradle.properties` 是唯一版本来源。验证脚本需要 Python 3，发布检测测试需要 Git。共享核心是独立的 `shared` Java 库，不引用平台类型或生产依赖；两端通过项目依赖引用并将它打入发布 JAR。

```sh
# 同时构建两端，包含测试和最终 JAR 校验：使用 JDK 25 运行 Gradle
./gradlew clean build

# 单独构建 Bukkit：JDK 17，包含共享测试和发布 JAR 校验
./gradlew bukkitClean bukkitBuild

# 单独构建 Fabric：使用 JDK 25，检查全部 48 个锁定版本和最终 JAR
./gradlew fabricClean fabricBuild

# 清理两端的构建产物（包括 Fabric 测试存档）：JDK 17 即可
./gradlew clean
```

Bukkit 产物为 `bukkit/build/libs/WorldSafe-<版本>.jar`，Fabric 产物为 `fabric/build/libs/WorldSafe-Fabric-<版本>.jar`，仅生成包含所需依赖的发布 JAR。Bukkit 默认使用 JDK 17 工具链编译和测试，继续输出 Java 8 字节码；因此根 `build` 需要同时安装 JDK 17 和 JDK 25。可使用 Gradle 自动发现工具链，或通过 `-Porg.gradle.java.installations.paths=<jdk17>,<jdk25>` 指定安装位置。

指定 Bukkit 测试使用 `./gradlew :bukkit:test --tests '<TestClass>'`。API 编译使用 `./gradlew :bukkit:clean :bukkit:compileJava -PspigotVersion=<API版本> -PbukkitJava=<JDK版本>`，配对关系与 CI 矩阵一致。单独构建 Bukkit 不配置独立的 Fabric 游戏构建。根 `build` 构建并校验两端；根 `clean` 删除根目录、Bukkit、共享模块及整个 `fabric/build` 构建目录，无需配置 Fabric，清理使用 JDK 17 即可。运行 `clean` 或 `fabricClean` 前请备份 `fabric/build` 下需要保留的游戏测试存档。单平台操作使用 `bukkitBuild` / `bukkitClean` 和 `fabricBuild` / `fabricClean`。IDEA 根运行配置仅保留 `WorldSafe [build]` / `WorldSafe [clean]`；下方 `bukkit`、`fabric`、`shared` 是同级项目节点。展开 `bukkit` 或 `fabric` → `Tasks` → `build`，可执行对应平台的 `build` / `clean`。Fabric 节点通过 `fabric/platform.gradle` 协调独立构建，项目导入时不配置游戏版本模块。对应命令为 `./gradlew :bukkit:build` / `:bukkit:clean` 和 `./gradlew :fabric:build` / `:fabric:clean`。

向 `master` 推送根 `gradle.properties` 中的版本号变更会触发发布检查；只修改其他属性且版本不变时不会发布。本次迁移会与旧 POM 中的版本比较，无需 Maven，也不会因构建工具切换而自动发布。
Fabric 清单固定游戏版本、映射、Java 目标和构建工具。内部模块精确匹配游戏版本，最终只交付一个通用 JAR，独立行为测试模组不进入发布产物。具体命令见 [Fabric 构建和验收说明](fabric/README.md)。编译、启动和实际功能验收分别报告；发布使用代码验证过的同一份 JAR，并核对哈希；游戏实测保留为可选工具，不作为本次验收门槛。

## 命令与权限

- `/worldsafe help` - 查看命令帮助。
- `/worldsafe reload` - 重载配置。
- Bukkit：`worldsafe.admin` 允许管理，默认仅 OP 拥有；Fabric 使用上文的控制台、OP 2 和房主规则。

## 功能版本表

Fabric 的平台最低版本始终为 1.14，即使某项功能的原始最低版本更早。请先确认你的服务端版本，然后从上往下查看版本分组。只要分组中的版本不高于你的服务端版本，就可以使用其中的功能。

> 例如，如果你运行的是 1.13 版本，那你就可以使用 1.8.8+ 和 1.13 版本这两个版本当中的所有配置，
> 但是你不能使用 1.14+ 及以上版本的配置（因为新版本当中的一些东西在旧版本里面并不存在）

<details open>
<summary><strong>Minecraft 1.8.8+</strong></summary>

### 直接取消爆炸

*(如果配置了某种爆炸的完整取消项，就不需要再配置对应的方块保护项。)*

- **`bedExplosionCancel`** - 完全禁止普通床爆炸。Bukkit 端此配置请勿添加主世界；Fabric 按原版维度的床规则判定。稻草床不会爆炸，因此不属于此功能的处理范围。
- **`tntExplosionCancel`** - 完全禁止 TNT 和 TNT 矿车爆炸。
- **`creeperExplosionCancel`** - 完全禁止苦力怕爆炸。
- **`endCrystalExplosionCancel`** - 完全禁止末地水晶爆炸。
- **`ghastExplosionCancel`** - 禁止恶魂火球爆炸并取消火球伤害。
- **`witherExplosionCancel`** - 完全禁止凋零和凋零头颅爆炸。

### 禁止破坏方块但保留伤害

- **`bedExplosionProtection`** - 保护普通床爆炸周围的方块，但保留爆炸伤害。稻草床不会爆炸，因此不属于此功能的处理范围。
- **`tntExplosionProtection`** - 禁止 TNT 和 TNT 矿车爆炸破坏方块。
- **`creeperExplosionProtection`** - 禁止苦力怕爆炸破坏方块。
- **`endCrystalExplosionPrevention`** - 禁止末地水晶爆炸破坏方块。
- **`ghastExplosionProtection`** - 禁止恶魂火球破坏方块，但保留火球伤害。
- **`witherExplosionProtection`** - 禁止凋零和凋零头颅破坏方块。

### 其他地图保护

- **`cropTrampleProtection`** - 防止耕地被踩踏成泥土。
- **`dragonEggTeleportationPrevention`** - 防止触碰龙蛋时龙蛋瞬移。
- **`fireSpreadPrevention`** - 防止火焰蔓延和烧毁附近方块。Fabric 会在燃烧操作开始前取消，阻止火焰引燃 TNT，避免保留的 TNT 方块被反复引燃生成实体；火焰仍可自然熄灭。
- **`fireIgnitionPrevention`** - 防止火球、闪电、爆炸、末地水晶和燃烧的箭点燃方块。
- **`enderDragonBlockDestructionProtection`** - 防止末影龙破坏方块。
- **`enderManBlockPickupProtection`** - 防止末影人拾取或移动方块。
- **`silverfishBlockChangeProtection`** - 防止蠹虫钻入或破坏虫蚀方块。
- **`rabbitCropEatingProtection`** - 防止兔子啃食作物。
- **`sheepGrassEatingProtection`** - 防止绵羊吃草。
- **`villagerCropModificationProtection`** - 防止村民收割或种植作物。
- **`mobDoorBreakProtection`** - 防止生物破门。
- **`snowGolemSnowTrailPrevention`** - 防止雪傀儡留下雪迹。

</details>

<details>
<summary><strong>Minecraft 1.13+</strong></summary>

- **`phantomDamagePrevention`** - 防止幻翼对玩家或其他实体造成伤害。

</details>

<details>
<summary><strong>Minecraft 1.14+</strong></summary>

- **`ravagerBlockDestructionProtection`** - 防止劫掠兽破坏方块。
- **`foxBerryHarvestProtection`** - 防止狐狸采摘甜浆果。
- **`witherRoseFormationPrevention`** - 防止生成凋零玫瑰。

</details>

<details>
<summary><strong>Minecraft 1.16+</strong></summary>

### 直接取消爆炸

- **`respawnAnchorExplosionCancel`** - 防止有充能的重生锚在下界以外的维度爆炸。

### 禁止破坏方块但保留伤害

- **`respawnAnchorExplosionPrevention`** - 防止重生锚爆炸破坏方块，但保留爆炸伤害。

</details>

<details>
<summary><strong>Minecraft 1.20.3+</strong></summary>

- **`decoratedPotProjectileProtection`** - 防止投射物击碎装饰陶罐。

</details>

<details>
<summary><strong>Minecraft 1.21+</strong></summary>

- **`windChargeBlockDestructionProtection`** - 通过爆炸列表和直击方块变化事件，防止玩家及旋风人风弹破坏装饰陶罐、紫颂花和滴水石锥，保留冲击伤害与击退。不依赖 `decoratedPotProjectileProtection`，不取消整个弹射物命中。Bukkit 的覆盖范围取决于服务端是否提供对应事件；某种命中原本就不破坏方块，不能用来证明保护有效。
- **`breezeWindChargeImpactCancel`** - 完全取消旋风人的风弹冲击，不影响玩家发射的风弹。Bukkit/Spigot 平台的非 Paper 服务端仅能以最佳努力模式运行；由于 Spigot 不提供所需的提前爆炸钩子，仍可能保留伤害或击退。
- **`weavingCobwebFormationPrevention`** - 防止盘丝效果生成蜘蛛网。

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### 直接取消爆炸

- **`sulfurCubeExplosionCancel`** - 完全禁止硫磺立方体吞入 TNT 后产生的爆炸。Bukkit/Spigot 平台的非 Paper 服务端仅能以最佳努力模式运行；由于 Spigot 不提供所需的提前爆炸钩子，仍可能保留伤害或击退。

### 禁止破坏方块但保留伤害

- **`sulfurCubeExplosionProtection`** - 防止硫磺立方体爆炸破坏方块，但保留爆炸伤害。

</details>

下方 bStats 图表仅统计 Bukkit 安装。

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe 遵循 MIT 许可证。
