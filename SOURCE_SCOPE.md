# 公开源码范围

当前快照为 **0.9.1**，运行依赖 Star Fantasy Library **0.2.29+**。本仓库用于查错和审阅 Goety 附属实现。

## 收录内容

- `src/main/java`：主体代码，使用既有 SRG 符号。
- `src/mojang/java` 与 `src/church/java`：使用官方 Mojang 映射的代码。
- `src/main/resources`：默认配置相关资源、配方、战利品、翻译、黑暗秘典条目、结构及允许公开的客户端资源。
- `model_sources/apostle` 与 `model_sources/apollyon`：原有可公开 Blockbench 工程，保留供模型审阅。
- 构建脚本与此前公开的辅助工具；工具不代表独立完整的开发环境。
- 许可证、作者鸣谢和第三方声明。无影之王相关改编动画的来源列于第三方声明中。

## 不收录内容

- Hades 专用授权美术资源；沿用公开仓库现有的 `assets/**/hades*`、`assets/**/*hades*` 排除规则，相关代码仍保留。来源见 [CREDITS.md](CREDITS.md)。
- 第三方依赖 JAR、编译产物、缓存、游戏实例、存档、个人配置、崩溃日志和开发交接资料。
- 内部 QA 源码目录、临时预览、音频处理文件及未公开的美术制作流水线。

公开模型工程与运行资源分别位于 `model_sources` 和 `src/main/resources/assets/starfantasy_goety`。