package com.starfantasy.goety.magic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** A real single-school choice, independent of Final Staff's all-school priority setting. */
public final class AscianStaffSchool {
    public static final String TAG = "StarFantasyAscianSchool";
    private static final ResourceLocation ID = new ResourceLocation("starfantasy_goety", "ascian_staff");

    public static boolean isAscian(ItemStack stack) {
        return !stack.isEmpty() && ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static FinalStaffSchool get(ItemStack stack) {
        if (!isAscian(stack) || stack.getTag() == null) return FinalStaffSchool.NETHER;
        FinalStaffSchool selected = FinalStaffSchool.byId(stack.getTag().getString(TAG));
        return selected == null ? FinalStaffSchool.NETHER : selected;
    }

    public static void set(ItemStack stack, FinalStaffSchool school) {
        if (isAscian(stack) && school != null) stack.getOrCreateTag().putString(TAG, school.id);
    }

    private AscianStaffSchool() {}
}
