# 构建与发布说明

## 目标环境

- Java 17 字节码
- Minecraft 1.20.1
- Forge 47.x
- Goety 2.5.56.5 或更高版本
- GeckoLib 4.8.4 或更高版本
- Curios 5.14.1 或更高版本
- Star Fantasy Library 0.2.11 或更高版本

本仓库由整合包内的既有附属模组整理而来，当前源码仍使用 Forge 1.20.1 的 SRG
符号，应在匹配的 Forge 1.20.1 SRG 开发环境中编译。`build/`、旧 JAR、反编译目录和
本地依赖均属于生成文件，不纳入版本控制。

项目的首选修改形式始终是：

- `src/main/java`
- `src/main/resources`

## 教堂模块（0.1.259 起）

`src/church/java` 是新增的结构、祭坛和召唤逻辑，使用 Forge 1.20.1 官方 Mojang 映射。
`church.gradle` 将它单独编译并通过 `reobfJar` 转为 SRG；既有 `src/main/java` 仍在 SRG 环境编译，
避免为一个功能迁移整个工程的符号。

编译该模块时，需要在 Gradle 多项目设置中将本目录作为 `:starfantasy_goety`，指定
`buildFileName = 'church.gradle'`，并包含通用库项目 `:star_fantasy_library`。
`build/church-deps` 中放置编译基线 `starfantasy_goety-0.1.258.jar`、
`goety-2.5.52.4.jar`、`geckolib-4.8.4.jar` 和 `jade-11.13.2.jar`。基线 JAR 仅用于解析既有实体 API，不打入新 JAR。
Jade 仅为编译依赖；运行时可选，安装后会在祭坛信息框显示灰色使用提示。
运行 `:starfantasy_goety:jar` 后，将其 SRG 输出加入既有源码的编译 classpath；最后合并两份 class、
`src/main/resources` 与 LICENSE，生成完整发布 JAR。不要将带 `church` classifier 的中间 JAR
单独放入游戏。运行依赖要求 Star Fantasy Library 0.2.11 或更高版本。

冥界教堂的正式模板位于 `data/starfantasy_goety/structures/church`，共 99 个分块模板。
模板高度为 94 格，其中最上方 10 格为空气，用于清出屋顶空间。结构在
生成维度的海平面减 9 格处放置基底，桥面方块位于原版下界 Y=31，
玩家行走面与岩浆海面等高（Y=32）。仅在地面层外墙轮廓之外填充岩浆源，
衔接外墙边缘及入口下方；室内岩浆河保持原高度。外墙底部通向室内的空隙
在外侧用石砖封闭，防止外部岩浆倒灌。结构在
`top_layer_modification` 阶段放置，并注册通用库的装饰生成排除区域，阻止相邻区块的
地形装饰重新写入教堂。自然刷怪排除覆盖所有常规生物类别；祭坛和战斗召唤不受影响。
发布时必须完整保留这些模板及对应 worldgen、biome tag、祭坛资源；预览存档和导出工具不属于运行资源。

## 客户端资源边界

从 0.1.314 起，使徒萌化代码和资源直接纳入本模组。
模型、动画、贴图、声音统一位于 `assets/starfantasy_goety`，无需独立 Doki 模组。
已有亚波伦专用动画保留，运动会使徒始终使用内置模型。
原版使徒的外观及音效由客户端本地 `apostle.toml` 的 `enableApostleMoe` 控制。
原版使徒头衔通过实体同步数据传送；声音使用实体专属事件，避免全局覆盖 Goety 音效。
所有客户端渲染和声音监听均隔离在客户端加载路径。

发布 JAR 只应包含运行所需 class、资源、GPLv3 许可证及第三方声明，不应打入 Java 源码、
BBMODEL、第三方工程或构建缓存。

发布 JAR 必须包含 `CREDITS.md`：Hades 模型经授权改编自 Toro Toro 的
`[Toro] A Better Skeleton`，该模型保留其原许可及单独授权，不属于代码的 GPL 授权范围。
发布页面也应保留原作者、商品链接与改编说明。

## 可复现构建

仓库根目录现在提供 `settings.gradle`、`build.gradle` 和 `tools/build.ps1`。
`tools/build.ps1` 会自动构建通用库与教堂模块，生成主体的 SRG 编译参数，并在
`build/` 下对3个历史上使用官方映射的边界渲染类生成临时转换副本；原始源码不会被修改。
临时教堂网络声明位于 `tools/build-stubs`，只用于编译，最终合并时由主体实现覆盖。

在安装了 Minecraft 1.20.1 Forge 依赖的环境中运行：

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\tools\build.ps1 `
  -MinecraftRoot 'D:\MC\.minecraft' `
  -GradlePath 'C:\path\to\gradle-8.5\bin\gradle.bat'
```

也可以通过 `STARFANTASY_MC_ROOT` 和 `STARFANTASY_GRADLE` 环境变量提供这两个路径。
脚本使用工作区 `private-deps/goety` 中固定的 Goety、GeckoLib、Jade、Curios 和
教堂 bootstrap JAR；无需 Doki JAR。`src/mojang/java` 与教堂模块一起编译并重映射，
随后与主体 SRG class 合并。缺少依赖会直接报出缺失项。
