package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.common.items.magic.MagicFocus;
import com.Polarice3.Goety.api.magic.ISpell;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class BattleFocusItem extends MagicFocus {
    private final ISpell focusSpell;
    public BattleFocusItem(ISpell spell) { super(spell); this.focusSpell = spell; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.focusSpell.acceptedEnchantments().contains(enchantment);
    }
    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).keySet().stream()
                .allMatch(this.focusSpell.acceptedEnchantments()::contains);
    }
}
