package com.starfantasy.goety.magic;

import com.Polarice3.Goety.api.magic.SpellType;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

/** Only explicitly audited, mutually exclusive choices belong here. */
public final class FinalStaffPriority {
    private FinalStaffPriority() {}

    public enum SummonFamily {
        ZOMBIE(Map.of(
                SpellType.FROST, "frozen_zombie_servant", SpellType.STORM, "frayed_servant",
                SpellType.WILD, "jungle_zombie_servant", SpellType.NETHER, "zpiglin_servant",
                SpellType.ABYSS, "drowned_servant")),
        SKELETON(Map.of(
                SpellType.FROST, "stray_servant", SpellType.STORM, "rattled_servant",
                SpellType.WILD, "mossy_skeleton_servant", SpellType.NETHER, "wither_skeleton_servant",
                SpellType.ABYSS, "sunken_skeleton_servant")),
        SLIME(Map.of(
                SpellType.ABYSS, "tropical_slime_servant", SpellType.NECROMANCY, "crypt_slime_servant",
                SpellType.NETHER, "magma_cube_servant")),
        HUNTING(Map.of(
                SpellType.NECROMANCY, "skeleton_wolf", SpellType.STORM, "stormhound",
                SpellType.WIND, "twilight_goat", SpellType.ABYSS, "snapper",
                SpellType.FROST, "winter_wolf", SpellType.NETHER, "hellhound")),
        MAULING(Map.of(
                SpellType.ABYSS, "gnasher", SpellType.FROST, "polar_bear_servant",
                SpellType.NETHER, "hoglin_servant"));

        private final Map<SpellType, String> variants;
        SummonFamily(Map<SpellType, String> variants) { this.variants = variants; }
        public String preferredEntity(ItemStack stack) {
            return FinalStaffSchool.isFinalStaff(stack) ? variants.get(FinalStaffSchool.get(stack).type) : null;
        }
    }

    /** Preserve the old ultimate-staff necro bolt when the preferred variant is unavailable. */
    public static SpellType soulBoltChoice(ItemStack stack, boolean hasNetherSet) {
        SpellType preferred = FinalStaffSchool.get(stack).type;
        if (preferred == SpellType.WILD) return SpellType.WILD;
        if (preferred == SpellType.NETHER && hasNetherSet) return SpellType.NETHER;
        return SpellType.NECROMANCY;
    }
}
