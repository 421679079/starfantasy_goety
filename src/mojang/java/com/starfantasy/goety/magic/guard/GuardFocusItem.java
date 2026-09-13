package com.starfantasy.goety.magic.guard;

import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class GuardFocusItem extends MagicFocus {
    public GuardFocusItem() { super(GuardSpell.INSTANCE); }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == ModEnchantments.DURATION.get();
    }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(book).keySet().stream()
                .allMatch(e -> e == ModEnchantments.DURATION.get());
    }
}
