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

默认配置仅保留总开关、bStats 开关，以及苦力怕和 TNT 方块保护示例。其他配置项请按需从[功能版本表](#功能版本表)添加；未配置的功能默认禁用。已有服务端配置文件不会被覆盖。

## 命令与权限

- `/worldsafe help` - 查看命令帮助。
- `/worldsafe reload` - 重载配置。
- `worldsafe.admin` - 允许使用 WorldSafe 管理命令，默认仅 OP 拥有。

## 功能版本表

请先确认你的服务端版本，然后从上往下查看版本分组。只要分组中的版本不高于你的服务端版本，就可以使用其中的功能。

<details open>
<summary><strong>Minecraft 1.8.8+</strong></summary>

### 直接取消爆炸
*(如果配置了某种爆炸的完整取消项，就不需要再配置对应的方块保护项。)*

- **`bedExplosionCancel`** - 完全禁止床爆炸。此配置请勿添加主世界。
- **`tntExplosionCancel`** - 完全禁止 TNT 和 TNT 矿车爆炸。
- **`creeperExplosionCancel`** - 完全禁止苦力怕爆炸。
- **`endCrystalExplosionCancel`** - 完全禁止末地水晶爆炸。
- **`ghastExplosionCancel`** - 禁止恶魂火球爆炸并取消火球伤害。
- **`witherExplosionCancel`** - 完全禁止凋零和凋零头颅爆炸。

### 禁止破坏方块但保留伤害

- **`bedExplosionProtection`** - 保护床爆炸周围的方块，但保留爆炸伤害；床本身仍可能消失。有意义的爆炸前快照优先；无可用快照时，使用两秒交互缓存，按朝向和床头／床尾数据关联经过校验的床两端。结构数据不可读取时仅记录点击位置，不猜测邻居；不推断数据包自定义维度的床规则。
- **`tntExplosionProtection`** - 禁止 TNT 和 TNT 矿车爆炸破坏方块。
- **`creeperExplosionProtection`** - 禁止苦力怕爆炸破坏方块。
- **`endCrystalExplosionPrevention`** - 禁止末地水晶爆炸破坏方块。
- **`ghastExplosionProtection`** - 禁止恶魂火球破坏方块，但保留火球伤害。
- **`witherExplosionProtection`** - 禁止凋零和凋零头颅破坏方块。

### 其他地图保护

- **`cropTrampleProtection`** - 防止耕地被踩踏成泥土。
- **`dragonEggTeleportationPrevention`** - 防止触碰龙蛋时龙蛋瞬移。
- **`fireSpreadPrevention`** - 防止火焰蔓延和烧毁附近方块。
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

- **`windChargeBlockDestructionProtection`** - 通过爆炸列表和直击方块变化事件，防止玩家及旋风人风弹破坏装饰陶罐、紫颂花和滴水石锥，保留冲击伤害与击退。不依赖 `decoratedPotProjectileProtection`，不取消整个弹射物命中。覆盖范围取决于服务端是否提供对应事件；某种命中原本就不破坏方块，不能用来证明保护有效。
- **`breezeWindChargeImpactCancel`** - 完全取消旋风人的风弹冲击，不影响玩家发射的风弹。非 Paper 服务端仅能以最佳努力模式运行；由于 Spigot 不提供所需的提前爆炸钩子，仍可能保留伤害或击退。
- **`weavingCobwebFormationPrevention`** - 防止盘丝效果生成蜘蛛网。

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### 直接取消爆炸

- **`sulfurCubeExplosionCancel`** - 完全禁止硫磺立方体吞入 TNT 后产生的爆炸。非 Paper 服务端仅能以最佳努力模式运行；由于 Spigot 不提供所需的提前爆炸钩子，仍可能保留伤害或击退。

### 禁止破坏方块但保留伤害

- **`sulfurCubeExplosionProtection`** - 防止硫磺立方体爆炸破坏方块，但保留爆炸伤害。

</details>

## 验证与发布

发布必须等待同一提交的必需 API 兼容检查成功，只发布通过检查的 Spigot 1.8.8 基线 JAR，并在下载构建产物后再次核对 SHA-256。纯数字点分版本沿用 GitHub 自动选择 Latest 的策略；带限定符的版本（包括 alpha、beta、rc、snapshot）标记为预发行且不设为 Latest。API 编译通过不等于服务端事件行为已经实测。

实服验证请使用[精简中文清单](docs/MANUAL_TEST_CN.md)，自动化结果见[验证记录](docs/VERIFICATION.md)。不同服务端使用同一个基线 JAR，不要使用兼容矩阵重新编译的产物。

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

WorldSafe 遵循 MIT 许可证。
