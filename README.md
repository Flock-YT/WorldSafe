# 🌍 WorldSafe

**WorldSafe** 是一个轻量级的Minecraft Bukkit插件，旨在防止部分实体及物品破坏地图。

## 📋 安装要求

- **Java 21**: 本插件需要 Java 21 运行时环境。

## ✨ 特性

- **轻量级**: 对性能影响最小,只加载需要的监听器,最大可能减少无关判断。
- **简易配置**: 使用方便，配置简单。

## 📖 使用方法

1. 下载插件并将其放置在 `plugins` 文件夹中。
2. 重启服务器以加载插件。
3. 在配置文件中进行必要的设置。

## 🛠️ 计划

目前插件正在进行陆续更新中，请关注后续更新！

# 目前实现的功能

## <span style="color: red;">🧱方块类

### 直接取消爆炸类
- ⏳ **bedExplosionCancel** - 禁止 床 爆炸
- ⏳ **respawnAnchorExplosionCancel** - 禁止 重生锚 爆炸
- ⏳ **tntExplosionCancel** - 禁止 TNT 爆炸

### 取消破坏方块但保留伤害类
- ✅ **bedExplosionProtection** - 禁止 床 爆炸破坏方块
- ✅ **respawnAnchorExplosionPrevention** - 禁止 重生锚 爆炸破坏方块
- ✅ **tntExplosionProtection** - 禁止 TNT 爆炸破坏方块

### 其他类
- ✅ **cropTrampleProtection** - 禁止 田 被踩坏
- ✅ **dragonEggTeleportationPrevention** - 禁止 龙蛋 瞬移

## <span style="color: red;">🧬实体类

### 直接取消爆炸类
- ✅ **creeperExplosionCancel** - 禁止 苦力怕 爆炸
- ✅ **endCrystalExplosionCancel** - 禁止 末地水晶 爆炸
- ✅ **ghastExplosionCancel** - 禁止 恶魂火球 爆炸
- ⏳ **witherExplosionCancel** - 禁止 凋零 爆炸

### 取消破坏方块但保留爆炸伤害类
- ✅ **creeperExplosionProtection** - 禁止 苦力怕 爆炸破坏方块
- ✅  **endCrystalExplosionPrevention** - 禁止 末地水晶 爆炸破坏方块
- ✅ **ghastExplosionProtection** - 禁止 恶魂火球 爆炸破坏方块
- ✅ **witherExplosionProtection** - 禁止 凋零 爆炸破坏方块

### 其他类
- ✅ **enderDragonBlockDestructionProtection** - 禁止 末影龙 破坏方块
- ✅ **enderManBlockPickupProtection** - 禁止 末影人 搬运方块

---

**版权声明**: 本插件由 [Eric.乐乐 & 追求at](#) 开发，遵循 [MIT 许可证](#)。