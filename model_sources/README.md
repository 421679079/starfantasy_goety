# 模型工程

本目录保存从 Doki 项目原样复制的 14 份 Blockbench `.bbmodel` 工程，均内嵌贴图。
它们是用于继续编辑的工程文件，不会作为运行模型直接加载。

- `apostle/apostle.bbmodel`：默认使徒。
- `apostle/apostle_the_*.bbmodel`：12 个头衔的使徒。
- `apollyon/apollyon.bbmodel`：亚波伦。

来源及授权：Apostles T / Apostle T-fix10，作者 xb4911（原始署名 4911），GPL-3.0-only。
完整声明见根目录 `THIRD_PARTY_NOTICES.md`，上游说明与许可证保存在
`src/main/resources/THIRD_PARTY/Apostle_T-fix10/`。

这些文件是 Doki 项目中保留的修改工程，不代表未经修改的上游原件。
本次迁移日期为 2026-09-10；工程正文没有改写。
工程里可能保留旧 Doki 命名空间或贴图相对路径，贴图已嵌入工程。
导出给当前 Goety 项目时使用 `starfantasy_goety` 资源命名空间。

当前运行几何体与贴图位于：

- `src/main/resources/assets/starfantasy_goety/geo/entity/apostle/`
- `src/main/resources/assets/starfantasy_goety/textures/entity/apostle/`
- `src/main/resources/assets/starfantasy_goety/geo/entity/apollyon/`
- `src/main/resources/assets/starfantasy_goety/textures/entity/apollyon/`

以上路径相对于仓库根目录。当前运行动画另见 `animations/entity/`；
亚波伦动画采用 Goety 中的版本，不应直接用 Doki 工程的动画覆盖。
