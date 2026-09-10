package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.projectiles.BlossomThorn;
import com.starfantasy.goety.combat.ApollyonWildSurgeManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces damage only for Blossom Thorns explicitly spawned by Apollyon. */
@Mixin(value = BlossomThorn.class, remap = false)
public abstract class BlossomThornMixin {
    @Inject(
            method = "dealDamageTo(Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void starfantasy$replaceManagedDamage(LivingEntity target, CallbackInfo callback) {
        BlossomThorn self = (BlossomThorn) (Object) this;
        if (ApollyonWildSurgeManager.replaceBlossomThornDamage(self, target)
                || com.starfantasy.goety.combat.apostle.ApostleWildSurgeManager.replaceBlossomThornDamage(self, target)) {
            callback.cancel();
        }
    }
}
