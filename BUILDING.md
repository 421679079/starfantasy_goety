# 构建环境说明

此仓库是 **0.9.1 公开审阅快照**。它保留实际代码组织和构建入口，但未提供完整开发 workspace、第三方依赖或 Hades 授权美术资源，不能直接独立构建完整发行版。

## 目标环境

- Java 17、Minecraft 1.20.1、Forge 47.x。
- Goety 2.5.56.5+、GeckoLib 4.8.4+、Curios 5.14.1+。
- Star Fantasy Library 0.2.29+。
- Jade 为可选兼容项，编译相关集成代码时需要对应 API。

## 源码组织

`src/main/java` 使用既有 SRG 符号；`src/church/java` 和 `src/mojang/java` 使用官方 Mojang 映射。不能把全部源码直接当作同一种映射编译。

`tools/build.ps1` 是完整 workspace 的构建入口，负责不同映射源码的编译、重映射与合并。`tools/build-stubs` 仅提供编译边界声明，不应作为运行实现发布。
`build.gradle` 与 `church.gradle` 描述 Mojang 映射部分的编译设置。

## 完整环境所需内容

脚本要求同级 `star_fantasy_library` 项目、workspace 的 Gradle 多项目设置，以及 `private-deps/goety` 中的编译依赖和 bootstrap JAR。公开仓库未附带这些文件。仅补齐依赖还不能恢复被排除的授权美术资源。

在具备完整环境与相关资源授权后，使用 Java 17 执行：

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\tools\build.ps1 `
  -MinecraftRoot '<Minecraft目录>' `
  -GradlePath '<Gradle可执行文件路径>' `
  -Version '0.9.1'
```

也可通过 `STARFANTASY_MC_ROOT` 和 `STARFANTASY_GRADLE` 提供路径。缺少依赖时脚本会报告缺失项；本仓库不包含 Gradle Wrapper 二进制。

正式发行包仅应包含运行 class、资源和许可证声明。冥界教堂的 99 个结构模板、配套 worldgen 和 biome tag 必须保留；不要将 BBMODEL、预览文件、测试代码或中间构建 JAR 打入游戏包。

辅助工具可能依赖完整开发环境，不能用它们的存在推断公开快照可独立完成全部回归验证。
