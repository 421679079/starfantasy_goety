# Credits

## Third-Party Assets

### Hades Model — [Toro] A Better Skeleton

- **Creator:** Toro Toro.
- **Original product:** [Toro] A Better Skeleton.
- **Product link:** https://mcmodels.net/products/10693/toro-better-skeleton
- **Used for:** The model adapted for Hades in Goety:StarFantasy.
- **License:** This third-party model is not covered by this mod's GPL-3.0-only license. Its original license and the separately granted permission continue to apply.
- **Redistribution:** Inclusion in this mod does not grant permission to extract, re-upload, redistribute, or reuse the model as a standalone asset or in another project. Such use requires authorization under the original license or separate permission from the rights holder.

For Apostle and Apollyon model credits and third-party code notices, see THIRD_PARTY_NOTICES.md.

## Third-Party Code References

### mhzy / Fantasy Ending — Combat Health Protection

- **Upstream contributors (project metadata):** MegaDarkness, XiaoWu, xiaobai4911, BaiXi, mx_wj.
- **Repository:** https://gitee.com/mega_32k/mhzy
- **Reference revision:** bf66895.
- **Reference:** `src/main/java/com/mega/uom/common/entity/boss/uom/UomWither.java`, including the independent hit-immunity timer and `UomEntityData` health-write limit.
- **Used for:** The design of Little Apollyon's final health-write guard (the shared Star Fantasy Library combat-health API and the Apollyon entity integration). The implementation uses a scoped data-write hook and this add-on's existing immunity/damage-cap settings. It does not replace the entity's data object or adopt Mhzy's removal/death protection.
- **License:** MIT; the unmodified upstream notice is included in `THIRD_PARTY/Mhzy/LICENSE.txt`. Its copyright placeholders are preserved as supplied by upstream.
- **Integration and adaptation:** Funits / Goety:StarFantasy.
