# 第三方内容与许可证

此 Goety 子模块的代码按 GPL-3.0-only 发布，完整许可证见发布 JAR 根目录的 LICENSE。
第三方美术资源按各自声明授权；Hades 使用的授权模型不包含在 GPL 授权范围内，详见 CREDITS.md。
此变更不修改工作区其他独立模组的许可证。

## Star Fantasy Soulsweapons Tweaks — 无影君王弹幕表现

- 来源：Star Fantasy Soulsweapons Tweaks 0.1.122。
- 作者与版权：Copyright (c) 2026 Star Fantasy。
- 改编内容：自制弹幕渲染器；保留其冰弹、雷击环与雷球核心三张纹理，资源命名空间改为本模组。
- 许可：MIT，完整原文保存在 THIRD_PARTY/Soulsweapons_Tweaks/LICENSE.txt。
- 此部分不引入 Souls Weaponry 实体、类依赖或其特效资源。

## Apostles T / Apostle T-fix10

- 使徒模型资源修改自此项目。
- 作者：xb4911（原始 README 署名为 4911）。
- 项目页面：https://www.curseforge.com/minecraft/texture-packs/apostles-t
- 原许可证：GNU General Public License version 3。
- 原始说明与许可证保存在 THIRD_PARTY/Apostle_T-fix10/。

原 Goety:StarFantasy 的 MIT 版权声明保存在
THIRD_PARTY/StarFantasy_Goety/ORIGINAL_MIT_LICENSE.txt。

## Hades

- 原模型：[Toro] A Better Skeleton，作者 Toro Toro。
- 商品页面：https://mcmodels.net/products/10693/toro-better-skeleton
- 该第三方模型保留原许可及单独授权，不因模组代码采用 GPL-3.0-only 而改为 GPL 授权。
- 完整英文鸣谢与再分发说明见 CREDITS.md。

## mhzy / Fantasy Ending

- 源码：https://gitee.com/mega_32k/mhzy，参考版本 bf66895。
- 上游项目元数据署名：MegaDarkness、XiaoWu、xiaobai4911、BaiXi、mx_wj。
- 参考内容：UomWither 的独立受击无敌计时与 UomEntityData 的生命值写入限伤设计。
- 用途：已迁入 Star Fantasy Library 的通用生命值写入保护及亚小妹接入，沿用本附属的受击无敌和限伤配置；不包含上游的数据对象替换、防移除或强制保活逻辑。
- 集成与改编：Funits / Goety:StarFantasy。
- 上游许可：MIT，完整原文保存在 THIRD_PARTY/Mhzy/LICENSE.txt；版权占位文字按上游原文保留。

## Marium’s Soulslike Weaponry — 无影之王动画与死亡效果

- 对照版本：soulslike-weaponry-1.3.1-1.20.1-forge。
- 上游项目及作者：Marium / mariumbacchus，https://github.com/mariumbacchus/Soulslike-Weaponry 。
- 动画来源：混沌君主 `assets/soulsweapons/animations/entity/chaos_monarch.animation.json`，仓库分支 `1.20-1.20.1`；上游许可证为 CC0-1.0。
- 用途：无影之王 `assets/starfantasy_goety/animations/entity/ascian_king.animation.json` 的基础动作来源，已结合本项目骨骼和战斗时序改编，并增加 `attack1`～`attack4` 四连近战动作。
- 当前实际保留的上游基础动作：`idle`、`teleport`、`lightning_call`、`shoot`、`barrage`、`death`。未使用的 `swing_staff` 和 `spawn` 已删除。
- 动画的导入及改编：Funits / Goety:StarFantasy。
- 上游 JAR 的 `META-INF/mods.toml` 声明许可证为 CC0-1.0。
- 紫色火焰粒子纹理 `textures/particle/ascian_purple_flame.png` 来自上游 `assets/soulsweapons/textures/particle/purple_flame.png`，保持原图。
- 死亡放射光束沿用混沌君主的配色、尺寸和时钟参数，使用本项目已有光束辅助类；球面粒子复用本项目实现。
- 现有 `death_explosion.ogg` 与上游 `dawnbreaker_sound.ogg` 内容相同，本轮未重复导入音频。

## 无影君王战斗表现（0.8.49）

- 火柱、暗柱和双骨骷髅手掌沿用用户验收的项目预览，几何与像素火焰由项目生成。
- 冰锥、手掌在运行时引用 Minecraft 原版冰块和凋灵骷髅贴图，不重复分发原版贴图。
- `staff_flow.png` 是本工作区 bear_their_names/connected_flow.png 的灰白 128×128 衍生资源，沿用原素材权利归属；不将素材声明为 MIT。来源详见 starfantasy_slashblade/docs/bear-resource-provenance.json。
- `fire_burst.ogg` 由用户提供并明确授权用于火柱、暗柱及冰锥。
