声明：我的java水平 -> 0，整个项目都是dsh干的，我除了给她输入要求和测试之外啥都没干，就连这个readme都是她写的

# Create 站台号 · Create Platform Numbers

> 给 Create（机械动力）的铁路系统补上「站台号」：**列车站可以标站台号，时刻表可以限定停靠站台**。

| 项目 | 值 |
|---|---|
| Minecraft | 1.21.1 |
| 加载器 | NeoForge 21.1.x |
| 依赖 | Create 6.0.11（必需） |
| 本模组版本 | 0.1.1 |
| 端 | 客户端 + 服务端**都要装** |

---

## 为什么需要它

Create 的列车站只能起一个名字，而一个名字对应的是「整站」。现实里同一个车站有多条股道/站台，
同一个站台还可能正反两个方向都发车——光靠站名没法表达「这趟车要停 2 号台」。

Create 本身不校验站名唯一性（`StationBlockEntity#updateName` 只是把名字写进 `GlobalStation.name`），
所以正确的用法是：**同一个物理车站放多个 Track Station，站名取一样的，站台号各填各的**。本模组把
「站台号」这一维补上，并让时刻表能按它筛选。

---

## 功能

### 1. 车站设置站台号

打开列车站界面（右键 Track Station），**标题行右端、车站名右边**会出现 `站台 [____]`：

<!-- 截图待补：把车站界面截图存成 docs/station-gui.png 后取消下一行注释 -->
<!-- ![车站界面的站台号输入框](docs/station-gui.png) -->

- 只能填纯数字（`1`、`2`、`12`…），最多 9 位；**留空 = 未设置**（框内灰字「未设置」，只在空且未聚焦时显示）。
- 回车提交，直接关界面也会提交（与 Create 处理站名的节奏一致）。
- **允许重复**：不同车站可以用同一个站台号，正/反发共用站台、同一站台两条股道都靠这个表达。
- 数据存在车站上（与站名同一个对象 `GlobalStation`），随铁路存档持久化、随铁路同步流下发客户端。

### 2. 时刻表限制站台

编辑时刻表（Schedule）里某条「目的地」指令，编辑行现在是两个输入框：

<!-- 截图待补：把时刻表编辑界面截图存成 docs/schedule-editor.png 后取消下一行注释 -->
<!-- ![时刻表编辑行的站台限制](docs/schedule-editor.png) -->

| 匹配规则 | 行为 |
|---|---|
| 站台留空 | 全部站台都能停（**不改变原版行为**，老时刻表零影响） |
| 填 `2` | 只接受站台号为 `2` 的车站 |
| 填 `1,2` | 接受 `1` 或 `2`（半角逗号分隔） |
| 车站本身没填站台号 | **任何非空限制都不匹配它**（宁可拦下，也不停到没标站台的股道） |
| 站台号比较 | 按数值比，`01` 与 `1` 视为同一个 |

界面上的反馈：

- 时刻表条目卡片摘要变成 `北京站* #2`；
- 悬停该条目多一行「站台限制: 2」；
- 找不到符合条件的车站时，列车状态栏显示带站台的目标名，例如 `No station on graph matches '北京站* [2]'`。

### 3. 典型用法

| 场景 | 做法 |
|---|---|
| 一站两台 | 同一车站放两个 Track Station，站名都填 `北京站`，站台号 `1` / `2`；时刻表写 `北京站*` + `2` |
| 正/反发共用站台 | 两个方向的时刻表都限制同一个站台号，或直接留空 |
| 只有一条股道的老站 | 不填站台号，时刻表也别填——行为与之前完全一样 |

---

## 安装

1. 装好 **Minecraft 1.21.1 + NeoForge 21.1.x + Create 6.0.11**。
2. 把 `createplatform-0.1.1.jar` 放进**客户端和服务端**的 `mods/` 目录。
3. 启动后进游戏，右键任意列车站，标题行右端能看到「站台」输入框即安装成功。

> **两端都必须安装**：站台号是追加在 Create 铁路同步流里下发的（`GlobalStation#write/read(FriendlyByteBuf)`），
> 只装一端会因为字段不对称而报错/不同步。

依赖声明（`META-INF/neoforge.mods.toml`）：

```toml
[[dependencies.createplatform]]
modId="create"
type="required"
ordering="AFTER"
versionRange="[6.0.11,)"
```

---

## 兼容性

| 对象 | 状态 |
|---|---|
| Create 6.0.11（编译基线 `6.0.11-282`） | ✅ 已实测（dev 端真机启动 + 游戏内手测） |
| Create `6.0.11-295` / `6.0.11-319` | ⚠️ 同一 6.0.11 线，mixin 目标按字节码核对过，但未实测 |
| Create 6.0.10 及更早 / 0.5.1 | ❌ 不支持（目标类与方法不同） |
| NeoForge 21.1.x | ✅ 基线 21.1.249 |
| Fabric / Forge | ❌ 不做（只发 NeoForge） |
| 其它模组 | ⚠️ 未做联测。站台框刻意放在标题行右端，就是为了避开其它模组往车站界面**底栏**加控件（早期版本放底栏时被占用过） |

因为 mixin 直接织入 Create 的内部类，**换 Create 小版本后请先跑一次**：

```bash
./gradlew runClient
```

Mixin 失效会明确报 `Critical injection failure`（`defaultRequire: 1`），不会静默失效。

---

## 构建

需要 **JDK 21**（Gradle 9.2.1 由 wrapper 提供，无需本机安装 Gradle）：

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

产物：`build/libs/createplatform-0.1.1.jar`

开发环境自测：

```bash
gradlew.bat runClient    # 带模组的客户端
gradlew.bat runServer    # 带模组的服务端（首次需把 eula.txt 改 true）
```

`build.gradle` 里 Create / Ponder / Flywheel / Registrate 都是 `runtimeOnly`（仅开发环境用），
**不会打进产物**；编译期只需要 `compileOnly` 的 Create 与 Ponder（Ponder 仅用于补齐
`StationBlockEntity` 的类层级）。

> 若本机 `JAVA_HOME` 指向了一个已失效的 JDK，可在项目根放一个 `jdk-home.local`（单行写 JDK 21 路径，
> 已被 `.gitignore` 忽略），`gradlew.bat` 会在这个文件存在且 `JAVA_HOME` 不可用时优先使用它。

---

## 实现说明（给想改的人）

```
src/main/java/com/henry/createplatform/
├── Createplatform.java                 @Mod 入口 + 网络包注册
├── api/
│   ├── StationPlatform.java            「站台号」鸭子接口（由 mixin 织进 GlobalStation）
│   └── Platforms.java                  归一化/解析/匹配规则（纯逻辑，双端共用）
├── net/SetStationPlatformPayload.java  客户端 → 服务端：设置站台号
├── client/                             纯客户端逻辑（输入框构造、补全重绑、渲染）
└── mixin/                              8 个 mixin，见下表
src/main/resources/
├── META-INF/neoforge.mods.toml         模组元数据 + [[mixins]] + 依赖
├── createplatform.mixins.json          mixin 配置（common / client 分开）
└── assets/createplatform/lang/         zh_cn / en_us
```

| Mixin | 目标 | 端 | 作用 |
|---|---|---|---|
| `GlobalStationMixin` | Create `GlobalStation` | 双端 | 加站台号字段 + NBT 读写（键 `CreatePlatform`）+ 同步流读写 |
| `DestinationInstructionMixin` | Create `DestinationInstruction` | 双端 | 带站台过滤的目的地匹配；卡片摘要加 `#站台` |
| `TextScheduleInstructionTooltipMixin` | Create `TextScheduleInstruction` | 双端 | 悬停提示补一行站台限制 |
| `ScreenMixin` | MC `Screen` | 客户端 | 暴露 `children`/`renderables`，供别处挂控件 |
| `AbstractStationScreenMixin` | Create `AbstractStationScreen` | 客户端 | 暴露 `station`/`blockEntity` 字段 |
| `StationScreenMixin` | Create `StationScreen` | 客户端 | 建站台输入框、接管标题行点击、回车/关界面下发 |
| `TextScheduleInstructionMixin` | Create `TextScheduleInstruction` | 客户端 | 把唯一文本框拆成「站名 + 站台」两个（仅目的地指令） |
| `ScheduleScreenMixin` | Create `ScheduleScreen` | 客户端 | 把目的地补全重绑回站名框（站台框不弹补全） |

几个非直觉的决定：

- **站台号挂在 `GlobalStation` 而不是方块实体**：Create 的站名就存在这里，铁路存档 + `TrackGraphSync`
  都走这条路径；放方块实体里会在车站未加载时读不到。
- **目的地指令只在「填了站台」时才接管**：`start()` 的 HEAD 注入先读站台限制，为空直接 `return`
  让 Create 原版代码跑，保证老时刻表零行为变化。
- **站名/站台两个输入框的宽度是算出来的**：编辑行受 `SCHEDULE_EDITOR` 弹窗约束（确认按钮在 x+224），
  站名 84px + 站台 54px 是既不压按钮也不互相重叠的组合。

---

## 已知限制

- 站台号是「标签」，只参与时刻表的车站筛选与显示，**不会改变列车停靠的物理位置**（Create 的停靠点是车站
  方块本身，没有股道概念）。
- 只有「目的地」指令支持站台限制；「取件」「改名」指令不涉及停站，没有加。
- 站名接近 25 字上限时，居中的站名文字可能与右上角站台框重叠，属位置取舍。
- 未与其它模组联测；未做多人服务器（独立服 + 真实客户端）联测。
- 未验证 Create `6.0.11-295` / `-319`（见「兼容性」）。

---

## 版本历史

| 版本 | 变更 |
|---|---|
| 0.1.0 | 首个可运行版本：车站站台号（存 `GlobalStation` + 同步）、时刻表站台限制、卡片/提示/状态栏反馈 |
| 0.1.1 | 站台框从底栏挪到标题行右端（底栏会与其它模组冲突）；改成标准文本框；占位提示改用 `setHint`；修好站名补全被站台框抢走的问题 |
| — | 曾尝试把分隔符放宽到全角逗号/任意非数字，属误判，已完整回退（当前只接受 ASCII 数字 + 半角逗号） |

---

## 发布前待办

- [ ] README 里的两张截图（`docs/station-gui.png`、`docs/schedule-editor.png`）待补，补好后把 README 里
      对应的两行注释取消
- [ ] 在 Create `6.0.11-319` 上跑一次 `runClient` 确认 mixin 仍然命中
- [ ] 多人服务器联测

---

## 许可

[MIT](LICENSE) © 2026 yu

本模组通过 Mixin 织入 Create 的内部类，不含也不分发 Create 的任何代码；运行需要用户自行安装 Create
（其许可见 [Creators-of-Create/Create](https://github.com/Creators-of-Create/Create)）。

---

## In English

**Create Platform Numbers** is a NeoForge 1.21.1 addon for Create 6.0.11 that adds a per-station
*platform number* and a platform filter to train schedules.

- Name a station as usual; the new text field in the station GUI's title row stores a numeric
  platform id (empty = unset, duplicates across stations allowed).
- Schedule "Destination" instructions gain a second input: leave it empty to accept any platform, or
  enter one/more comma-separated ids (`1,2`). Stations without a platform id never match a non-empty
  filter, so a train cannot accidentally stop on an unlabelled track.
- The platform id lives on Create's `GlobalStation` (same place as the station name), so it persists
  in the railway save and is synced to clients. **Install on both client and server.**

Built against Create `6.0.11-282`; same-line builds (`-295`, `-319`) are expected to work but have
not been tested. Uses Mixin, so a Create version bump may require re-verification — run `runClient`
and watch for `Critical injection failure`.
