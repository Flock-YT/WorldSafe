
[English](README.md) | **中文**

# 🌍 WorldSafe

**WorldSafe** 是一个轻量级的Minecraft Bukkit插件，旨在防止部分实体及物品破坏地图。

## 📋 安装要求

- **Java 17 或更高版本**
- **Minecraft Java 版 1.19 或更高版本**

## ✨ 特性

- **轻量级**: 对性能影响最小,只加载需要的监听器,最大可能减少无关判断。
- **简易配置**: 使用方便，配置简单。
- **易于扩展**: 新增监听器后只需在 `WorldSafe.FEATURES` 映射中注册，就可以直接在配置中使用，无需再修改加载逻辑。

## 📖 使用方法

1. 下载插件并将其放置在 `plugins` 文件夹中。
2. 重启服务器以加载插件。
3. 在配置文件中进行必要的设置。

## 🛠️ 计划

目前插件正在进行陆续更新中，请关注后续更新，你可以在issue中提出你的需求！

## 🔐 权限

目前仅有一个权限：`worldsafe.admin`

## ➡️ 指令

### `/worldsafe help`
查看插件帮助

### `/worldsafe reload`
重载插件配置

# 目前实现的功能

功能按照所需的最低 Minecraft 版本分类。服务器可以使用自身版本及之前所有版本分组中的功能。

<details open>
<summary><strong>Minecraft 1.19+</strong></summary>

### 直接取消爆炸
*(如果配置了某种爆炸的完整取消项，就不需要再配置对应的方块保护项。)*

- ✅ **bedExplosionCancel** - 禁止床爆炸（此配置请勿包含主世界）
- ✅ **respawnAnchorExplosionCancel** - 禁止重生锚爆炸
- ✅ **tntExplosionCancel** - 禁止TNT爆炸
- ✅ **creeperExplosionCancel** - 禁止苦力怕爆炸
- ✅ **endCrystalExplosionCancel** - 禁止末地水晶爆炸
- ✅ **ghastExplosionCancel** - 禁止恶魂火球爆炸
- ✅ **witherExplosionCancel** - 禁止凋零爆炸

### 禁止破坏方块但保留伤害

- ✅ **bedExplosionProtection** - 禁止床爆炸破坏方块
- ✅ **respawnAnchorExplosionPrevention** - 禁止重生锚爆炸破坏方块
- ✅ **tntExplosionProtection** - 禁止TNT爆炸破坏方块
- ✅ **creeperExplosionProtection** - 禁止苦力怕爆炸破坏方块
- ✅ **endCrystalExplosionPrevention** - 禁止末地水晶爆炸破坏方块
- ✅ **ghastExplosionProtection** - 禁止恶魂火球爆炸破坏方块
- ✅ **witherExplosionProtection** - 禁止凋零爆炸破坏方块

### 其他地图防护

- ✅ **cropTrampleProtection** - 禁止田被踩坏
- ✅ **dragonEggTeleportationPrevention** - 禁止龙蛋瞬移
- ✅ **fireSpreadPrevention** - 禁止火焰向周围方块蔓延
- ✅ **fireIgnitionPrevention** - 禁止火球、闪电、爆炸、末地水晶和燃烧箭点燃方块
- ✅ **enderDragonBlockDestructionProtection** - 禁止末影龙破坏方块
- ✅ **enderManBlockPickupProtection** - 禁止末影人搬运方块
- ✅ **phantomDamagePrevention** - 禁止幻翼造成伤害
- ✅ **ravagerBlockDestructionProtection** - 禁止劫掠兽破坏方块
- ✅ **silverfishBlockChangeProtection** - 禁止蠹虫钻入或拆除虫蚀方块
- ✅ **rabbitCropEatingProtection** - 禁止兔子啃食作物
- ✅ **sheepGrassEatingProtection** - 禁止绵羊吃草
- ✅ **villagerCropModificationProtection** - 禁止村民收割或种植作物
- ✅ **foxBerryHarvestProtection** - 禁止狐狸采摘浆果
- ✅ **mobDoorBreakProtection** - 禁止生物破门
- ✅ **snowGolemSnowTrailPrevention** - 禁止雪傀儡生成雪迹
- ✅ **witherRoseFormationPrevention** - 禁止生成凋零玫瑰

</details>

<details>
<summary><strong>Minecraft 1.20.3+</strong></summary>

- ✅ **decoratedPotProjectileProtection** - 禁止投射物击碎装饰陶罐

</details>

<details>
<summary><strong>Minecraft 1.21+</strong></summary>

- ✅ **windChargeBlockDestructionProtection** - 禁止风弹破坏装饰陶罐、紫颂花和滴水石，同时保留冲击伤害
- ✅ **breezeWindChargeImpactCancel** - 完整取消旋风人的风弹冲击，不影响玩家风弹
- ✅ **weavingCobwebFormationPrevention** - 禁止盘丝效果生成蜘蛛网

</details>

<details>
<summary><strong>Minecraft 26.2+</strong></summary>

### 直接取消爆炸

- ✅ **sulfurCubeExplosionCancel** - 禁止硫磺立方体吞入TNT后的爆炸

### 禁止破坏方块但保留伤害

- ✅ **sulfurCubeExplosionProtection** - 禁止硫磺立方体爆炸破坏方块

</details>

---

![WorldSafe Plugin Installation Chart](https://bstats.org/signatures/bukkit/WorldSafe.svg)

**版权声明**: 本插件由 [Eric.乐乐 & 追求at](#) 开发，遵循 [MIT 许可证](#)。
