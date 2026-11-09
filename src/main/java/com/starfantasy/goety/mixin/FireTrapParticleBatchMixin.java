package com.starfantasy.goety.mixin;

import com.starfantasy.goety.client.FireTrapEffect;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Works with both Goety's server particles and RevelationFix's local particles. */
@Mixin(value = ParticleEngine.class, remap = false)
public abstract class FireTrapParticleBatchMixin {
    @ModifyVariable(method = "m_107344_", at = @At("HEAD"), argsOnly = true)
    private Particle starfantasy$batchFireTrapWind(Particle particle) {
        return FireTrapEffect.batchNativeWind(particle);
    }
}
