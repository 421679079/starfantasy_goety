package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import com.starfantasy.goety.combat.ApollyonWildSurgeManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces damage only for Earth Fists explicitly spawned by Apollyon. */
@Mixin(value = EarthFist.class, remap = false)
public abstract class EarthFistMixin {
    @Inject(
            method = "dealDamageTo(Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void starfantasy$replaceManagedDamage(LivingEntity target, CallbackInfo callback) {
        EarthFist self = (EarthFist) (Object) this;
        if (ApollyonWildSurgeManager.replaceEarthFistDamage(self, target)
                || com.starfantasy.goety.combat.apostle.ApostleWildSurgeManager.replaceEarthFistDamage(self, target)) {
            callback.cancel();
        }
    }
}
