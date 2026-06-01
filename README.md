# SongPlayerClientFabric

SongPlayerClientFabric 是一个用于 Minecraft Fabric 客户端的歌曲演奏模组。它可以读取 MIDI、Note Block Studio (`.nbs`) 等歌曲文件，自动搭建音符盒舞台，并在游戏内使用音符盒播放歌曲。

本仓库基于 SongPlayer 客户端功能扩展，除原有的歌曲播放、队列、播放列表、舞台搭建、歌曲物品等功能外，还加入了音符盒检测/搭建模式配置，方便在不同服务器环境和不同音符盒舞台结构下使用。

## 功能概览

- 支持从 `.minecraft/songs` 目录读取歌曲文件。
- 支持 MIDI 文件和各版本 Note Block Studio (`.nbs`) 文件。
- 支持通过 URL 下载歌曲并加入播放。
- 支持播放队列、跳过、循环、跳转到指定时间。
- 支持播放列表创建、编辑、删除、循环和随机播放。
- 支持自动搭建音符盒舞台。
- 支持多种舞台类型：默认、宽舞台、球形舞台。
- 支持自动清理最近搭建的舞台并尽量恢复原方块。
- 支持仅生存模式播放。
- 支持通过手持物品保存和分享歌曲数据。
- 支持自定义进入创造/生存模式的服务器命令。
- 支持音符盒检测/搭建模式切换：`NBT_DATA` 与 `BLOCK_BASED`。

## 安装

1. 安装 Minecraft Fabric Loader。
2. 安装 Fabric API。
3. 从 Releases 下载本模组 jar。
4. 将 jar 放入 `.minecraft/mods` 目录。
5. 启动游戏。

如果你使用的是第三方启动器，请确认当前实例的 Minecraft 版本、Fabric Loader 版本和 Fabric API 版本与模组兼容。

## 歌曲文件

把歌曲文件放到：

```text
.minecraft/songs
```

支持的常见格式包括：

- `.mid`
- `.midi`
- `.nbs`

SongPlayer 支持 `songs` 目录下的子目录。命令补全可以帮助你浏览子目录里的歌曲文件。符号链接目录也可以使用。

注意：文件名和路径中不建议包含空格。原版命令解析对空格不友好，使用不含空格的文件名最稳。

## 基本使用

在游戏聊天栏中输入 SongPlayer 命令。默认命令前缀是 `$`。

例如：

```text
$play test.mid
```

如果提供的是有效 MIDI 或 NBS 文件，客户端会尝试：

1. 切换到创造模式。
2. 自动放置歌曲需要的音符盒舞台。
3. 切回生存模式。
4. 开始播放歌曲。

如果当前已经有歌曲在播放，新歌曲会加入播放队列。

播放时客户端会启用 freecam。你的视角可以自由移动，但真实玩家位置会保持在音符盒舞台中心附近，因为音符盒必须在玩家可触及范围内才能被敲击播放。

## 命令列表

所有命令大小写不敏感。下文使用默认前缀 `$`，如果你修改了命令前缀，需要替换为自己的前缀。

### 帮助

```text
$help
$help <command>
```

不带参数时列出所有 SongPlayer 命令；带命令名时显示对应命令的说明和语法。

### 设置命令前缀

```text
$setPrefix <prefix>
```

别名：

```text
$prefix
```

设置 SongPlayer 命令前缀。默认前缀是 `$`。

### 播放歌曲

```text
$play <filename or url>
```

播放 `.minecraft/songs` 目录中的指定歌曲；如果参数是 URL，则会尝试下载该 URL 指向的歌曲并播放。

如果已经有歌曲播放中，新歌曲会加入队列。

### 停止播放

```text
$stop
```

停止当前播放/搭建，并清空播放队列。

### 跳过当前歌曲

```text
$skip
```

跳过当前歌曲，播放队列中的下一首。

### 跳转时间

```text
$goto <mm:ss>
```

跳转到歌曲中的指定时间。

### 循环播放

```text
$loop
```

开启或关闭当前歌曲循环播放。

### 当前状态

```text
$status
```

别名：

```text
$current
```

显示当前正在播放的歌曲状态。

### 播放队列

```text
$queue
```

别名：

```text
$showqueue
```

显示当前播放队列。

### 查看歌曲目录

```text
$songs
$songs <subdirectory>
```

别名：

```text
$list
```

不带参数时列出 `songs` 目录下的歌曲；带子目录时列出指定子目录下的歌曲。

## 播放列表

播放列表相关命令：

```text
$playlist play <playlist>
$playlist create <playlist>
$playlist list [<playlist>]
$playlist delete <playlist>
$playlist addSong <playlist> <song>
$playlist removeSong <playlist> <song>
$playlist renameSong <playlist> <index> <new name>
$playlist loop
$playlist shuffle
```

功能说明：

- `play`：播放指定播放列表。
- `create`：创建播放列表。
- `list`：列出播放列表，或列出某个播放列表内的歌曲。
- `delete`：删除播放列表。
- `addSong`：向播放列表添加歌曲。
- `removeSong`：从播放列表移除歌曲。
- `renameSong`：按索引重命名播放列表中的歌曲。
- `loop`：切换播放列表循环。
- `shuffle`：切换播放列表随机播放。

播放列表文件保存在：

```text
SongPlayer/playlists
```

## 服务器模式切换命令

SongPlayer 自动搭建舞台时通常需要在创造模式和生存模式之间切换。不同服务器可能使用不同的权限插件或命令别名，因此提供了可配置命令。

### 设置创造模式命令

```text
$setCreativeCommand <command>
```

别名：

```text
$sc
```

默认使用 `/gamemode creative`。如果服务器使用 Essentials 等插件，可以改成 `/gmc`。

### 设置生存模式命令

```text
$setSurvivalCommand <command>
```

别名：

```text
$ss
```

默认使用 `/gamemode survival`。如果服务器使用 Essentials 等插件，可以改成 `/gms`。

### 使用 Essentials 命令

```text
$useEssentialsCommands
```

别名：

```text
$essentials
$useEssentials
$essentialsCommands
```

等价于：

```text
$setCreativeCommand /gmc
$setSurvivalCommand /gms
```

### 使用原版命令

```text
$useVanillaCommands
```

别名：

```text
$vanilla
$useVanilla
$vanillaCommands
```

等价于：

```text
$setCreativeCommand /gamemode creative
$setSurvivalCommand /gamemode survival
```

## 舞台与演奏行为

### 假玩家显示

```text
$toggleFakePlayer
```

别名：

```text
$fakePlayer
$fp
```

播放歌曲时客户端会启用 freecam，你看到的位置和真实玩家位置可能不同。开启假玩家后，会显示一个假玩家来表示你的真实位置。默认关闭。

### 设置舞台类型

```text
$setStageType <DEFAULT | WIDE | SPHERICAL>
```

别名：

```text
$setStage
$stageType
```

可选舞台类型：

- `DEFAULT`：默认方形舞台，最多约 300 个音符盒。
- `WIDE`：宽型/柱状舞台，最多约 360 个音符盒。
- `SPHERICAL`：紧凑球形舞台，可以容纳全部 400 种可能的音符盒组合。

球形舞台设计感谢 Sk8kman 和 Lizard16。

### 调整破坏速度

```text
$breakSpeed set <speed>
$breakSpeed reset
```

设置或重置方块破坏速度，单位为 blocks/sec。

### 调整放置速度

```text
$placeSpeed set <speed>
$placeSpeed reset
```

设置或重置方块放置速度，单位为 blocks/sec。

### 切换演奏动作

```text
$toggleMovement <swing | rotate>
```

别名：

```text
$movement
```

控制播放时是否挥手，以及是否转向正在敲击的音符盒。

### 设置力度阈值

```text
$setVelocityThreshold <threshold>
```

别名：

```text
$velocityThreshold
$threshold
```

设置最低力度阈值。低于阈值的音符不会被播放。范围为 `0` 到 `100`。该设置适用于 MIDI 和 NBS；对于歌曲物品，阈值会在创建物品时写入。

### 自动清理舞台

```text
$toggleAutoCleanup
```

别名：

```text
$autoCleanup
```

开启或关闭自动清理舞台。开启后，播放结束时会尽量恢复自动搭建前的方块。

### 清理最近舞台

```text
$cleanupLastStage
```

清理最近一次搭建的舞台，并尽量恢复原始方块。

注意：

- 如果停止播放后重新开始播放，记录的方块修改会重置。
- 不会恢复流体或门等双格方块。
- 不会恢复方块实体数据。
- 对火把等依附在其他方块上的方块，恢复结果可能不完整。

### 开始播放公告

```text
$announcement <enable | disable | getMessage>
$announcement setMessage
```

设置开始播放歌曲时发送的公告消息。使用 `setMessage` 时，可以用 `[name]` 表示歌曲名。

示例：

```text
$announcement setMessage &6Now playing: &3[name]
```

### 仅生存模式

```text
$toggleSurvivalOnly
```

别名：

```text
$survivalOnly
```

开启或关闭仅生存模式。开启后，自动放置音符盒会被禁用，自动调音通过右键点击实现。

在该模式下，你需要自己放置所需乐器。如果尝试播放歌曲但条件不足，模组会提示缺少多少种乐器。

### 飞行穿墙

```text
$toggleFlightNoclip
```

别名：

```text
$flightNoclip
```

开启或关闭飞行穿墙。开启后，播放歌曲期间本地玩家在飞行时可以穿过方块。

## 音符盒检测/搭建模式

本仓库加入了音符盒检测/搭建模式配置，用于控制自动舞台构建和音符盒状态识别方式。

### 查看当前模式

```text
$setNoteblockBuildMode
```

别名：

```text
$noteblockMode
$buildMode
$setNoteblockDetectionMode
$detectionMode
```

不带参数执行时，会显示当前模式和可用模式。

### 设置为 NBT 数据模式

```text
$setNoteblockBuildMode NBT_DATA
```

`NBT_DATA` 是默认模式。它通过音符盒自身状态/NBT 数据判断音色和音调。

特点：

- 识别精确。
- 可以获取完整音符盒状态。
- 适合由 SongPlayer 自动放置并带有完整状态数据的音符盒舞台。
- 与原有 SongPlayer 行为最接近。

### 设置为方块检测模式

```text
$setNoteblockBuildMode BLOCK_BASED
```

`BLOCK_BASED` 会根据音符盒下方的方块类型判断乐器音色，更贴近 Minecraft 原版音符盒机制。

特点：

- 更直观，按实际方块布局判断音色。
- 适合需要根据现有舞台方块结构判断乐器的场景。
- 对手动搭建或服务器已有音符盒舞台更友好。

### 方块到乐器的常见映射

| 下方方块类型 | 乐器 |
| --- | --- |
| 木头、木板、木楼梯等木质方块 | Bass |
| 沙子、红沙、沙砾 | Snare |
| 玻璃类方块 | Hat |
| 石头类方块 | Basedrum |
| 粘土 | Flute |
| 金块 | Bell |
| 羊毛 | Guitar |
| 浮冰 | Chime |
| 骨块 | Xylophone |
| 铁块 | Iron Xylophone |
| 灵魂沙、灵魂土 | Cow Bell |
| 南瓜、雕刻南瓜 | Didgeridoo |
| 绿宝石块 | Bit |
| 干草块 | Banjo |
| 荧石 | Pling |
| 其他方块 | Harp |

### 配置保存

检测模式会保存到配置文件中，字段类似：

```json
{
  "noteblockDetectionMode": "NBT_DATA"
}
```

你可以在游戏内用命令切换，不需要手动编辑配置文件。

### 注意事项

- `NBT_DATA` 更适合自动搭建的标准舞台。
- `BLOCK_BASED` 更符合原版音符盒音色机制，但需要下方方块布局正确。
- `BLOCK_BASED` 模式需要查询世界方块状态，在大型舞台上可能有轻微性能开销。
- 如果使用仅生存模式，你仍然需要确保手动放置的音符盒和下方方块满足歌曲需求。

## 歌曲物品

```text
$songItem create <song or url>
$songItem setSongName <name>
```

别名：

```text
$item
```

歌曲物品会把歌曲数据写入物品 NBT。右键这类物品时，SongPlayer 会识别其中的歌曲数据并询问是否播放。

说明：

- 创建后的歌曲物品可以被安装了兼容版本 SongPlayer 的玩家使用。
- 模组会自动生成物品名称和 lore。
- 名称和 lore 可以被修改或删除，不影响歌曲数据。
- SongPlayer 只读取 `SongItemData` 标签。

## 测试命令

```text
$testSong
```

开发期间使用的测试命令，会按顺序播放全部 400 种可能的音符盒声音。

## 工作机制

SongPlayer 会根据歌曲需求计算需要哪些音符盒。普通模式下，它会尝试切换到创造模式，直接放置带有乐器和音调数据的音符盒，因此不需要玩家逐个手动调音。

演奏时，玩家真实位置需要处在音符盒舞台中心附近，因为客户端需要在触及范围内敲击音符盒。为了不限制视角移动，模组会启用 freecam，让你可以自由观察周围环境。

如果服务器不允许默认的 `/gamemode creative` 和 `/gamemode survival`，请使用 `$setCreativeCommand` 和 `$setSurvivalCommand` 设置服务器可用的命令。

## 致谢

**Ayunami2000**：提出直接放置带 NBT 数据音符盒、避免手动调音的思路。

**Sk8kman**：SongPlayer 3.0 的多个改动受到其 fork 启发，尤其是替代舞台设计，同时也促成了播放列表和可切换动作等功能。

**Lizard16**：由 Sk8kman 提及的球形舞台设计作者。

## 许可证

请查看仓库中的 `LICENSE` 文件。
