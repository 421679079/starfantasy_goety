package com.starfantasy.goety.combat;

import com.Polarice3.Goety.utils.ExplosionUtil;
import com.Polarice3.Goety.utils.LootingExplosion;
import com.starfantasy.goety.entity.HadesEntity;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

/** Shared server-side finale particles for Apollyon and his Hades visual. */
public final class ApollyonDeathEffects {
    public static final int APOLLYON_DEATH_TICKS = 80;
    private static final int PARTICLE_COUNT = 1000;

    private ApollyonDeathEffects() {
    }

    /** Identical rise, small explosions and return to the starting height for boss and servant. */
    public static boolean tickApollyon(LivingEntity entity, int age, double groundY) {
        if (age <= 72) {
            ExplosionUtil.lootExplode(entity.m_9236_(), entity,
                    entity.m_20208_(1.0D), entity.m_20187_(), entity.m_20262_(1.0D),
                    0.0F, false, Explosion.BlockInteraction.KEEP, LootingExplosion.Mode.LOOT);
            if (age > 8) entity.m_6478_(MoverType.SELF, new Vec3(0, 0.15D, 0));
        } else {
            double step = (groundY - entity.m_20186_()) / Math.max(1, APOLLYON_DEATH_TICKS - age + 1);
            entity.m_6478_(MoverType.SELF, new Vec3(0, step, 0));
        }
        return age >= APOLLYON_DEATH_TICKS;
    }

    public static void explodeApollyon(LivingEntity entity) {
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

    public static void explodeHadesServant(com.starfantasy.goety.entity.HadesServantEntity entity) {
        if (!(entity.m_9236_() instanceof ServerLevel level)) return;
        Vec3 center = entity.m_20182_().m_82520_(0.0D, 6.0D, 0.0D);
        level.m_5594_(null,
                BlockPos.m_274561_(center.f_82479_, center.f_82480_, center.f_82481_),
                ApollyonSoundRegistry.DEATH_EXPLOSION.get(),
                SoundSource.HOSTILE, 1.0F, 1.0F);
        spawnSphere(level, center, ParticleTypes.f_123744_, 1.0D);
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
