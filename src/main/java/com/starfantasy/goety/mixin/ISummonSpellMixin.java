/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Polarice3.Goety.api.magic.ISummonSpell
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.magic.ISummonSpell;
import com.starfantasy.goety.registry.StaffItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={ISummonSpell.class}, remap=false)
public abstract class ISummonSpellMixin {
    @Redirect(method={"summonParticles"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"), remap=false)
    private boolean starfantasy$allowFinalStaffNamelessBranch(ItemStack stack, Item item) {
        if (stack.m_41720_() == item) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
        return StaffItemRegistry.FINAL_STAFF.equals((Object)id);
    }
}
