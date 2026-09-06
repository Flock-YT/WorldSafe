# WorldSafe 1.1.0

## 中文

WorldSafe 1.1.0 正式版。支持 Minecraft 1.8.8 至 26.2，插件保持 Java 8 字节码兼容；服务端本身仍需使用其版本要求的 Java。

- 修复玩家和旋风人风弹直接命中方块时的保护缺口，覆盖陶罐、紫颂花及滴水石锥的对应事件路径，保留原有冲击行为。
- 修复旧接口下床两端坐标不同导致的爆炸来源漏判；两端关联记录统一消费。
- 有效爆炸前快照优先于旧交互缓存，避免床和重生锚来源误判。
- 默认配置仅保留总开关、bStats 开关及苦力怕／TNT 方块保护示例。其他功能按 README 自行添加，省略即禁用；不会覆盖已有配置。
- 修复 macOS Bash 3.2 发布脚本兼容性。发布必须等待同一提交的兼容矩阵及产物检查通过，并校验最终 JAR 的 SHA-256。

### 升级

停服后备份旧 JAR 和配置，替换为 `WorldSafe-1.1.0.jar`，再启动服务器。不要同时保留多个 WorldSafe JAR。配置键不变，无需重建现有配置。

### 验证边界

候选修复包的必测实服清单已由用户确认全部符合预期，包括床、重生锚、玩家／旋风人风弹、伤害／冲击保留和世界隔离。该反馈不代表所有服务端版本、分支或自定义维度均已实测。自动化测试和编译矩阵也不替代真实服务器事件验证。

## English

WorldSafe 1.1.0 is the stable release, supporting Minecraft 1.8.8 through 26.2 with Java 8-compatible plugin bytecode. Use the Java runtime required by your server version.

- Fix direct-hit wind-charge block protection for player and breeze projectiles while retaining impact behavior.
- Resolve both verified bed halves in the legacy explosion-source fallback and consume their cached interaction together.
- Prefer meaningful pre-explosion snapshots over stale interaction records.
- Simplify the default configuration to the main/bStats switches and creeper/TNT protection examples. Other features remain opt-in; existing configurations are preserved.
- Fix release-script compatibility with macOS Bash 3.2. Releases require same-commit compatibility checks and verified artifact digests.

Stop the server, back up your JAR and configuration, replace the old plugin with `WorldSafe-1.1.0.jar`, then restart. Do not keep multiple WorldSafe JARs. Configuration keys are unchanged.

The user reported that all required real-server checks on the candidate fix passed. This is not a claim of runtime verification across every supported server version or custom dimension.
