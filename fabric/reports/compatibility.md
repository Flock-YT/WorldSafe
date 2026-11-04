# Compatibility report

Acceptance scope: **code-only**, as requested. No full in-game behavior certification is claimed.

The workspace version remains `1.1.1`; no release was published.

- Bukkit: `mvn clean verify` passed 78 JUnit/Mockito tests; release JAR validation passed (Java 8, version, dependency and signature checks).
- Bukkit API/JDK compile matrix: all 19 entries passed, from Spigot 1.8.8 / JDK 17 through 26.3 / JDK 25.
- Fabric: 51 JUnit/Mockito tests and 17 Python packaging/report tests passed.
- Fabric: all 48 pinned modules compiled and passed offline checks against matching intermediary/official game classes, including fields, descriptors, callback signatures, required native invocation sites and Mixin registration.
- The universal JAR passed nested-module, exact-version selection, Java bytecode, entrypoint, package-case, signature and excluded-dependency checks.

## Artifacts

| Platform | File | SHA-256 |
| --- | --- | --- |
| Bukkit | `WorldSafe-1.1.1.jar` | `f7bd5530a4139e257b0b3cd82841e0d58177ed9f3a14847a70f375b0e898752e` |
| Fabric | `WorldSafe-Fabric-1.1.1.jar` | `6273f8ad593dff743e88c7078a6c7589a0f621a3902f3c7bf03c25e877e31d97` |

Checksums accompany both files in `dist/`. The Fabric verification JSON matches the final JAR hash. Release CI uploads the verified file without rebuilding it.

## Fabric version matrix

| Minecraft | Java target | Applicable features | Mixins | Injection declarations | Native references | Code result |
| --- | --- | --- | --- | --- | --- | --- |
| 1.14 | 8 | 28 / 36 | 62 | 67 | 121 | PASS |
| 1.14.1 | 8 | 28 / 36 | 63 | 68 | 121 | PASS |
| 1.14.2 | 8 | 28 / 36 | 63 | 68 | 121 | PASS |
| 1.14.3 | 8 | 28 / 36 | 63 | 68 | 121 | PASS |
| 1.14.4 | 8 | 28 / 36 | 63 | 68 | 121 | PASS |
| 1.15 | 8 | 28 / 36 | 65 | 69 | 126 | PASS |
| 1.15.1 | 8 | 28 / 36 | 65 | 69 | 126 | PASS |
| 1.15.2 | 8 | 28 / 36 | 65 | 69 | 126 | PASS |
| 1.16 | 8 | 30 / 36 | 68 | 72 | 124 | PASS |
| 1.16.1 | 8 | 30 / 36 | 68 | 72 | 124 | PASS |
| 1.16.2 | 8 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.16.3 | 8 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.16.4 | 8 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.16.5 | 8 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.17 | 16 | 30 / 36 | 67 | 71 | 122 | PASS |
| 1.17.1 | 16 | 30 / 36 | 67 | 71 | 122 | PASS |
| 1.18 | 17 | 30 / 36 | 67 | 71 | 122 | PASS |
| 1.18.1 | 17 | 30 / 36 | 67 | 71 | 122 | PASS |
| 1.18.2 | 17 | 30 / 36 | 67 | 71 | 122 | PASS |
| 1.19 | 17 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.19.1 | 17 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.19.2 | 17 | 30 / 36 | 69 | 73 | 126 | PASS |
| 1.19.3 | 17 | 30 / 36 | 70 | 74 | 128 | PASS |
| 1.19.4 | 17 | 30 / 36 | 70 | 74 | 128 | PASS |
| 1.20 | 17 | 30 / 36 | 70 | 74 | 128 | PASS |
| 1.20.1 | 17 | 30 / 36 | 70 | 74 | 128 | PASS |
| 1.20.2 | 17 | 30 / 36 | 70 | 74 | 128 | PASS |
| 1.20.3 | 17 | 31 / 36 | 71 | 75 | 127 | PASS |
| 1.20.4 | 17 | 31 / 36 | 71 | 75 | 127 | PASS |
| 1.20.5 | 21 | 31 / 36 | 71 | 75 | 127 | PASS |
| 1.20.6 | 21 | 31 / 36 | 71 | 75 | 127 | PASS |
| 1.21 | 21 | 34 / 36 | 76 | 80 | 131 | PASS |
| 1.21.1 | 21 | 34 / 36 | 76 | 80 | 131 | PASS |
| 1.21.2 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.3 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.4 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.5 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.6 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.7 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.8 | 21 | 34 / 36 | 82 | 85 | 141 | PASS |
| 1.21.9 | 21 | 34 / 36 | 82 | 85 | 143 | PASS |
| 1.21.10 | 21 | 34 / 36 | 82 | 85 | 143 | PASS |
| 1.21.11 | 21 | 34 / 36 | 83 | 86 | 156 | PASS |
| 26.1 | 25 | 34 / 36 | 83 | 87 | 158 | PASS |
| 26.1.1 | 25 | 34 / 36 | 83 | 87 | 158 | PASS |
| 26.1.2 | 25 | 34 / 36 | 83 | 87 | 158 | PASS |
| 26.2 | 25 | 36 / 36 | 83 | 87 | 160 | PASS |
| 26.3 | 25 | 36 / 36 | 84 | 88 | 152 | PASS |

[Machine-readable code report](code-verification.json) contains per-module hashes and applicable/not-applicable feature lists. [Feature availability CSV](feature-availability.csv) lists all 48 × 36 combinations. [Bukkit API matrix](bukkit-api-matrix.json) records API/JDK pairs and compile outcomes.

## Limits

Native behavior, graphics, LAN connections, and real-world mod interactions are not acceptance requirements for this task. Earlier development game probes were partial, used earlier builds, and are not the evidence for this final artifact. Optional game drivers remain outside both release JARs.
