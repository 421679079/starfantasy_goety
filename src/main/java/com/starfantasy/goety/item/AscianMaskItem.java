package com.starfantasy.goety.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.starfantasy.goety.combat.HaloEquipRules;
import com.starfantasy.goety.registry.SpellAttributeRegistry;
import com.starfantasy.library.combat.StarFantasyCombatAttributes;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotAttribute;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Attribute-only head curio. No halo storage, inherited potion effects or equipment tick logic. */
public final class AscianMaskItem extends Item implements ICurioItem {
    public AscianMaskItem() { super(new Item.Properties().m_41487_(1)); }
    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return stack.m_255036_(1); }
    @Override public boolean canEquip(SlotContext slot, ItemStack stack) {
        return "head".equals(slot.identifier()) && HaloEquipRules.allows(slot);
    }
    @Override public Multimap<Attribute,AttributeModifier> getAttributeModifiers(SlotContext slot, UUID uuid, ItemStack stack) {
        var result=ImmutableMultimap.<Attribute,AttributeModifier>builder();
        if ("head".equals(slot.identifier()) && !slot.cosmetic()) {
            result.put(SlotAttribute.getOrCreate("head"),new AttributeModifier(uuid,"ascian_mask.head_slot",1,AttributeModifier.Operation.ADDITION));
        }
        result.put(SpellAttributeRegistry.SPELL_POWER.get(),new AttributeModifier(uuid,"ascian_mask.spell_power",1,AttributeModifier.Operation.ADDITION));
        result.put(SpellAttributeRegistry.COOLDOWN_REDUCTION.get(),new AttributeModifier(uuid,"ascian_mask.cooldown_reduction",.1,AttributeModifier.Operation.MULTIPLY_TOTAL));
        result.put(StarFantasyCombatAttributes.RESISTANCE.get(),new AttributeModifier(uuid,"ascian_mask.resistance",.1,AttributeModifier.Operation.MULTIPLY_TOTAL));
        return result.build();
    }
}
