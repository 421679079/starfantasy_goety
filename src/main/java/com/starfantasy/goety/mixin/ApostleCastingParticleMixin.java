package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.SpellCastingCultist;
import com.starfantasy.goety.combat.apostle.ApostleSpellSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleOptions;

@Mixin(value = SpellCastingCultist.class, remap = false)
public abstract class ApostleCastingParticleMixin {
    @Redirect(method = "m_8119_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_7106_(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void starfantasy$goldenCasting(Level level, ParticleOptions particle,
            double x, double y, double z, double red, double green, double blue) {
        if ((Object) this instanceof Apostle boss && ApostleSpellSupport.enhanced(boss)) {
            if (boss.getTitleNumber() == 10) { red = 1; green = .82; blue = .12; }
            if (boss.getTitleNumber() == 1) { red = .65; green = .15; blue = .9; }
            if (boss.getTitleNumber() == 5) { red = .5; green = .5; blue = .5; }
            if (boss.getTitleNumber() == 4) {
                int color = net.minecraft.util.Mth.m_14169_((boss.f_19797_ % 40) / 40F, .8F, 1);
                red = ((color >> 16) & 255) / 255D;
                green = ((color >> 8) & 255) / 255D;
                blue = (color & 255) / 255D;
            }
        }
        level.m_7106_(particle, x, y, z, red, green, blue);
    }
}
