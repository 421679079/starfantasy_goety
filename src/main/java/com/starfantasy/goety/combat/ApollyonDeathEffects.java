package com.starfantasy.goety.combat;

import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.HadesEntity;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Shared server-side finale particles for Apollyon and his Hades visual. */
public final class ApollyonDeathEffects {
    private static final int PARTICLE_COUNT = 1000;

    private ApollyonDeathEffects() {
    }

    public static void explodeApollyon(ApollyonEntity entity) {
        if (!(entity.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = entity.m_20182_().m_82520_(0.0D, 1.0D, 0.0D);
        playSound(level, center);
        spawnSphere(level, center, ParticleTypes.f_123745_, 1.0D);
        StarFantasyVfx.areaShake(entity, center, 36.0D, 16, 1.3F);
    }

    public static void explodeHades(HadesEntity entity) {
        if (!(entity.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = entity.m_20182_().m_82520_(0.0D, 12.0D, 0.0D);
        playSound(level, center);
        spawnSphere(level, center, ParticleTypes.f_123744_, 1.0D);
    }

    private static void playSound(ServerLevel level, Vec3 center) {
        level.m_5594_(null,
                BlockPos.m_274561_(center.f_82479_, center.f_82480_, center.f_82481_),
                ApollyonSoundRegistry.DEATH_EXPLOSION.get(),
                SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    private static void spawnSphere(
            ServerLevel level, Vec3 center, ParticleOptions particle, double speed) {
        double phi = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int i = 0; i < PARTICLE_COUNT; ++i) {
            double velocityY = 1.0D - i / (PARTICLE_COUNT - 1.0D) * 2.0D;
            double radius = Math.sqrt(1.0D - velocityY * velocityY);
            double theta = phi * i;
            double velocityX = Math.cos(theta) * radius * speed;
            double velocityZ = Math.sin(theta) * radius * speed;
            level.m_8767_(particle,
                    center.f_82479_, center.f_82480_, center.f_82481_, 0,
                    velocityX, velocityY * speed, velocityZ, 1.0D);
        }
    }
}
