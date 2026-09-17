package com.starfantasy.goety.mixin;

import com.starfantasy.goety.combat.HaloProtection;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class HaloLivingProtectionMixin {
    // Both addEffect and forceAddEffect consult canBeAffected before inserting an effect.
    @Inject(method = "m_7301_(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$blockHaloEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> callback) {
        if (HaloProtection.blocksEffect((LivingEntity)(Object)this, effect.m_19544_())) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "m_142079_()Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void starfantasy$preventHaloPowderSnow(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && HaloProtection.preventsFreezing((LivingEntity)(Object)this)) {
            callback.setReturnValue(false);
        }
    }

}
