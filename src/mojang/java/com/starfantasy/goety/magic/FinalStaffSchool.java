package com.starfantasy.goety.magic;

import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.item.FinalStaffDarkItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Selection belongs to an ItemStack, never to the shared registered Item. */
public enum FinalStaffSchool {
    ILL("ill", SpellType.ILL, 0xEAC76C),
    NECROMANCY("necromancy", SpellType.NECROMANCY, 0xE5D9BC),
    GEOMANCY("geomancy", SpellType.GEOMANCY, 0xD6A36B),
    WIND("wind", SpellType.WIND, 0xBDCFDE),
    STORM("storm", SpellType.STORM, 0xF1DD62),
    FROST("frost", SpellType.FROST, 0x90E1F7),
    WILD("wild", SpellType.WILD, 0x8FD979),
    ABYSS("abyss", SpellType.ABYSS, 0x74D7CF),
    VOID("void", SpellType.VOID, 0xB990EF),
    NETHER("nether", SpellType.NETHER, 0xFF9D63);

    public static final String TAG = "StarFantasyStaffSchool";
    public final String id;
    public final SpellType type;
    public final int color;

    FinalStaffSchool(String id, SpellType type, int color) {
        this.id = id;
        this.type = type;
        this.color = color;
    }

    public Component label() { return Component.translatable("school.starfantasy_goety." + id); }

    public static boolean isFinalStaff(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof FinalStaffDarkItem;
    }

    /** Null is reserved for invalid network selections. Missing or legacy ALL data uses NECROMANCY. */
    public static FinalStaffSchool byId(String id) {
        for (FinalStaffSchool school : values()) if (school.id.equals(id)) return school;
        return null;
    }

    public static FinalStaffSchool get(ItemStack stack) {
        if (!isFinalStaff(stack) || stack.getTag() == null) return NECROMANCY;
        FinalStaffSchool school = byId(stack.getTag().getString(TAG));
        return school == null ? NECROMANCY : school;
    }

    public static void set(ItemStack stack, FinalStaffSchool school) {
        if (!isFinalStaff(stack)) return;
        stack.getOrCreateTag().putString(TAG, school.id);
    }
}
