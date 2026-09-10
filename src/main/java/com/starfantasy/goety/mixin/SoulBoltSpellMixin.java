/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Polarice3.Goety.api.magic.SpellType
 *  com.Polarice3.Goety.common.items.ModItems
 *  com.Polarice3.Goety.common.magic.spells.SoulBoltSpell
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.spells.SoulBoltSpell;
import com.starfantasy.goety.registry.StaffItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={SoulBoltSpell.class}, remap=false)
public abstract class SoulBoltSpellMixin {
    @Redirect(method={"SpellResult"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"), remap=false)
    private boolean starfantasy$allowFinalStaffNecroBolt(ItemStack stack, Item item) {
        if (stack.m_41720_() == item) {
            return true;
        }
        if (item != ModItems.NAMELESS_STAFF.get()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
        return StaffItemRegistry.FINAL_STAFF.equals((Object)id);
    }

    @Redirect(method={"SpellResult"}, at=@At(value="INVOKE", target="Lcom/Polarice3/Goety/common/magic/spells/SoulBoltSpell;typeStaff(Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/api/magic/SpellType;)Z"), remap=false)
    private boolean starfantasy$filterFinalStaffSpellTypes(SoulBoltSpell spell, ItemStack stack, SpellType spellType) {
        if (this.isFinalStaff(stack) && (spellType == SpellType.WILD || spellType == SpellType.NETHER || spellType == SpellType.NECROMANCY)) {
            return false;
        }
        return spell.typeStaff(stack, spellType);
    }

    private boolean isFinalStaff(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
        return StaffItemRegistry.FINAL_STAFF.equals((Object)id);
    }
}
