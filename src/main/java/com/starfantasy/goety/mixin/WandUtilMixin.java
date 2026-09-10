package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.magic.SpellAttributeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WandUtil.class, remap = false)
public abstract class WandUtilMixin {
    @Inject(
            method = "enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private static void starfantasy$recognizeVirtualFocusEnchantments(
            LivingEntity caster,
            CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() && SpellAttributeHelper.hasVirtualFocusEnchantments(caster)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(
            method = "getLevels(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private static void starfantasy$addVirtualFocusEnchantmentLevels(
            Enchantment enchantment,
            LivingEntity caster,
            CallbackInfoReturnable<Integer> callback) {
        int bonus = SpellAttributeHelper.getVirtualEnchantmentLevel(caster, enchantment);
        if (bonus > 0) {
            callback.setReturnValue(callback.getReturnValue() + bonus);
        }
    }
}
