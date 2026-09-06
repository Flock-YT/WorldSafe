# WorldSafe 精简实服测试清单

本清单保留 `1.1.0-beta.1` 候选修复包的用户实测勾选，必测项已验收通过。正式版 `1.1.0` 的安装包见 [发布页](https://github.com/Flock-YT/WorldSafe/releases/tag/v1.1.0)；后续重新测试时使用对应版本的 JAR。

这份清单只要求观察游戏结果，不用采集事件日志、爆心坐标或构造缓存冲突。
自动化测试与实服验证的边界见 [验证记录](VERIFICATION.md)。

## 一次准备

- 使用可丢弃的测试世界，不在正式地图引爆。准备 **Spigot 1.8.8 + Java 8** 和 **Paper 1.21.11 + Java 21**，两服只安装本次交付的同一个 `WorldSafe-1.1.0-beta.1.jar`，重启加载。
- 不要重新下载 GitHub 上同名 beta JAR：历史附件没有替换，修复包是本次本地构建产物。
- OP 进服，确认控制台没有 WorldSafe 加载异常。使用普通难度；测试伤害时切生存、无护甲、无抗性或无敌效果，允许测试角色死亡。
- 把测试服的 `plugins/WorldSafe/config.yml` 替换为下列最小配置，执行 `/worldsafe reload`。世界名按实际 `level-name` 调整，下面是默认名称。旧服跳过不支持的新功能提示是正常的。

```yaml
enabled: true
enabled-bstats: false
bedExplosionProtection: [world_nether]
respawnAnchorExplosionPrevention: [world]
windChargeBlockDestructionProtection: [world]
```

其他功能省略即禁用。尤其不要同时启用 `bedExplosionCancel`、`respawnAnchorExplosionCancel`、`breezeWindChargeImpactCancel` 或 `decoratedPotProjectileProtection`，以免掩盖问题。

## A. 旧服：两次床爆炸

通过下界传送门到测试下界，找安全空地，创造模式准备；可用以下命令获得床和制作易破坏地面（站在同一位置执行）：

```mcfunction
/give @p minecraft:bed 4
/fill ~-4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:dirt
```

- [ ✅] 放床，切生存，空手右击**床头**；重新放床，再右击**床尾**。两次都应发生爆炸，附近泥土地形保留，角色仍能受爆炸伤害。床本身消失不算失败；只看土块，不把火焰当成方块保护失败。
- [✅ ] 对照：把配置的 `bedExplosionProtection` 改成 `[]`，`/worldsafe reload` 后再引爆一张床。此时测试下界已不在保护范围，应出现地形破坏。完成后恢复原配置并重载。

如果对照也不破坏地形，先排除其他插件、服务端设置或摆放位置问题，不能记为通过。

## B. 现代服：一排方块测风弹

在主世界空旷测试地面，创造模式站定，依次执行以下命令摆出一排目标；后续需要补放时回到同一位置：

```mcfunction
/fill ~2 ~-1 ~-1 ~10 ~-1 ~1 minecraft:stone
/setblock ~3 ~ ~ minecraft:decorated_pot
/setblock ~6 ~-1 ~ minecraft:end_stone
/setblock ~6 ~ ~ minecraft:chorus_flower[age=5]
/setblock ~9 ~ ~ minecraft:pointed_dripstone[vertical_direction=up,thickness=tip]
/give @p minecraft:wind_charge 64
```

每行沿目标走一遍，逐个检查三类方块；不必记录每发弹的坐标：

| 操作 | 陶罐保留 | 紫颂花保留 | 滴水石锥保留 |
| --- | --- | --- | --- |
| 玩家风弹直接击中目标 | [✅ ] | [✅ ] | [✅] |
| 玩家风弹打在目标紧邻地面 | [✅ ] | [ ✅] | [ ✅] |
| 旋风人风弹直接击中目标 | [✅ ] | [ ✅] | [✅ ] |
| 旋风人风弹打在目标紧邻位置 | [✅] | [ ✅] | [ ✅] |

旋风人用 `/summon minecraft:breeze ~ ~ ~8 {PersistenceRequired:1b}` 召唤；切生存，站到待测方块后方引导弹道，侧移让下一发落到附近。先看清弹道确实命中目标，打偏不计结果。结束用 `/kill @e[type=minecraft:breeze]` 清理（仅在测试服使用）。

- [ ✅] 冲击没有被整体取消：玩家对脚边发射一次确认仍会推动自己；旋风人对角色命中一次，确认原本存在的击退／伤害仍在。无原生伤害的命中不要求扣血。
- [✅ ] 对照：将 `windChargeBlockDestructionProtection` 改成 `[]` 后重载，在同一场地重复一个直击陶罐或紫颂花的有效破坏场景，然后恢复配置重载、补放方块、重复相同操作。应当“关闭会破坏、开启不破坏”。

某种材质／命中方式在关闭保护后也不破坏，就记“原版不破坏／不适用”，不要反复尝试，也不要据此声称事件路径已验证。尤其不要由陶罐结果推断滴水石锥结果。

## C. 现代服：床和重生锚顺带检查

- [ ✅] 下界放床并引爆一次：周围易破坏地形保留，角色仍会受伤。可用 `/give @p minecraft:red_bed 1`；下界的泥土地面可复用 A 的 `/fill` 命令。
- [ ✅] 主世界在泥土地面放重生锚，用萤石充能一次，改为空手右击引爆：周围地形保留，角色仍会受伤；重生锚本体消失不算失败。物品：`/give @p minecraft:respawn_anchor 1`、`/give @p minecraft:glowstone 1`。

## 反馈

只需发这四行；失败项补一句现象或一张截图：

```text
Spigot 1.8.8 / Java 8：加载、床头、床尾、未配置对照 = 通过 / 失败
Paper 1.21.11 / Java 21：加载、风弹四行、冲击、未配置对照 = 通过 / 失败
Paper 1.21.11：床、重生锚 = 通过 / 失败
不适用或失败说明：无 / ...
```

**可选，不影响本次必测完成：**实际使用的其他 Paper／Spigot 版本、26.2、恶魂反弹、自定义维度、TNT 等完全取消爆炸功能。上述两个代表服通过不等于所有服务器版本或可选场景均已验证。
