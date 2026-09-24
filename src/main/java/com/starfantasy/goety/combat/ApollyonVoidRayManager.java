package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.starfantasy.goety.entity.ApollyonSectorEffectEntity;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Owns the warning, one-shot damage and client-expanded visuals for Void Ray. */
public final class ApollyonVoidRayManager {
    public static final float RADIUS = 20.0F;
    public static final float SECTOR_ANGLE = 15.0F;
    public static final float SECTOR_SPACING = 30.0F;
    public static final int SECTOR_COUNT = 12;
    public static final int FIRST_WARNING_TICKS = 30;
    public static final int SECOND_WARNING_TICKS = 20;
    public static final int RAY_VISUAL_TICKS = 15;
    public static final float SECOND_WAVE_ROTATION = 15.0F;

    private static final int WARNING_COLOR = 0xA020F0;
    private static final float DAMAGE = 15.0F;
    private static final int VOID_TOUCHED_TICKS = 200;
    private static final int VOID_TOUCHED_AMPLIFIER = 1;
    private static final double HITBOX_HEIGHT = 5.0D;

    private ApollyonVoidRayManager() {
    }

    public static Vec3 captureAnchor(Mob boss) {
        if (boss == null || !(boss.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        if (!(boss instanceof ApollyonServantEntity servant)
                || !(servant.m_20202_() instanceof HadesServantEntity hades)
                || hades.mountedApollyonServant() != servant)
            return new Vec3(boss.m_20185_(), boss.m_20186_(), boss.m_20189_());
        BlockPos cursor = BlockPos.m_274561_(boss.m_20185_(), boss.m_20186_(), boss.m_20189_());
        if (!level.m_46805_(cursor)) return null;
        while (cursor.m_123342_() >= level.m_141937_()) {
            var state = level.m_8055_(cursor);
            var shape = state.m_60812_(level, cursor);
            if (!shape.m_83281_()) {
                double groundY = cursor.m_123342_() + shape.m_83297_(Direction.Axis.Y);
                if (groundY <= boss.m_20186_() + 1.0E-4D)
                    return new Vec3(boss.m_20185_(), groundY, boss.m_20189_());
            }
            cursor = cursor.m_7495_();
        }
        return null;
    }

    public static void warn(Mob boss, Vec3 anchor, float rotation, int duration) {
        warnPattern(boss, anchor, rotation, duration,
                RADIUS, SECTOR_ANGLE, SECTOR_SPACING, SECTOR_COUNT);
    }

    public static void warnPattern(
            Mob boss, Vec3 anchor, float rotation, int duration,
            float radius, float sectorAngle, float sectorSpacing, int sectorCount) {
        if (boss == null || anchor == null || boss.m_9236_().f_46443_) {
            return;
        }
        if (ApollyonSpellSupport.warnings(boss)) {
            StarFantasyVfx.groundSectorWarningBatch(
                boss, anchor, duration, radius, sectorAngle, WARNING_COLOR,
                sectorYaws(rotation, sectorSpacing, sectorCount));
        }
    }

    public static void detonate(Mob boss, Vec3 anchor, float rotation) {
        detonatePattern(boss, anchor, rotation,
                RADIUS, SECTOR_ANGLE, SECTOR_SPACING, SECTOR_COUNT);
    }

    public static void detonatePattern(
            Mob boss, Vec3 anchor, float rotation,
            float radius, float sectorAngle, float sectorSpacing, int sectorCount) {
        if (boss == null || anchor == null
                || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }

        ApollyonSectorEffectEntity.spawn(level, anchor,
                ApollyonSectorEffectEntity.MODE_VOID_RAY, RAY_VISUAL_TICKS,
                radius, sectorAngle, sectorSpacing, sectorCount,
                rotation, WARNING_COLOR);
        boss.m_5496_((SoundEvent) ModSounds.CORRUPT_BEAM_START.get(), 3.0F, 1.0F);

        AABB searchBox = new AABB(
                anchor.f_82479_ - radius, anchor.f_82480_,
                anchor.f_82481_ - radius,
                anchor.f_82479_ + radius, anchor.f_82480_ + HITBOX_HEIGHT,
                anchor.f_82481_ + radius);
        for (LivingEntity target : level.m_45976_(LivingEntity.class, searchBox)) {
            if (shouldSkip(boss, target)
                    || !intersectsPattern(target.m_20191_(), anchor, rotation,
                    radius, sectorAngle, sectorSpacing, sectorCount)) {
                continue;
            }

            hurtTarget(boss, target);
        }
    }

    private static void hurtTarget(Mob caster, LivingEntity target) {
        DamageSource source;
        float damage;
        if (caster instanceof ApollyonServantEntity servant) {
            source = servant.m_269291_().m_269104_(servant, servant);
            // Shared servant scaling is attack / 10: 50 base damage means attack * 5.
            damage = servant.scaleOutgoingDamage(50.0F);
        } else {
            // The boss deliberately retains anonymous fell-out-of-world damage.
            source = target.m_269291_().m_269341_();
            damage = ApollyonSpellSupport.damage(caster, target, DAMAGE, 0.05F);
        }
        if (target.m_6469_(source, damage)) {
            target.m_7292_(new MobEffectInstance(
                    (MobEffect) GoetyEffects.VOID_TOUCHED.get(),
                    VOID_TOUCHED_TICKS, VOID_TOUCHED_AMPLIFIER));
        }
    }

    private static boolean shouldSkip(Mob boss, LivingEntity target) {
        if (target == null || ApollyonSpellSupport.friendly(boss, target) || !target.m_6084_()
                || target.m_20147_()) {
            return true;
        }
        return target instanceof Player player && (player.m_7500_() || player.m_5833_());
    }

    /** Uses the same two-degree sector tessellation as the renderer. */
    private static boolean intersectsPattern(
            AABB worldBox, Vec3 anchor, float rotation,
            float radius, float sectorAngle, float sectorSpacing, int sectorCount) {
        double minX = worldBox.f_82288_ - anchor.f_82479_;
        double maxX = worldBox.f_82291_ - anchor.f_82479_;
        double minZ = worldBox.f_82290_ - anchor.f_82481_;
        double maxZ = worldBox.f_82293_ - anchor.f_82481_;
        double nearestX = distanceToInterval(0.0D, minX, maxX);
        double nearestZ = distanceToInterval(0.0D, minZ, maxZ);
        if (nearestX * nearestX + nearestZ * nearestZ > radius * radius) {
            return false;
        }

        int segments = Math.max(1, Mth.m_14167_(
                sectorAngle / ApollyonSectorEffectEntity.TESSELLATION_DEGREES));
        for (int sector = 0; sector < sectorCount; ++sector) {
            double center = rotation + sector * sectorSpacing;
            double start = center - sectorAngle * 0.5D;
            for (int segment = 0; segment < segments; ++segment) {
                double angleA = start + sectorAngle * segment / segments;
                double angleB = start + sectorAngle * (segment + 1) / segments;
                double ax = radialX(angleA, radius);
                double az = radialZ(angleA, radius);
                double bx = radialX(angleB, radius);
                double bz = radialZ(angleB, radius);
                if (triangleIntersectsRectangle(
                        0.0D, 0.0D, ax, az, bx, bz,
                        minX, minZ, maxX, maxZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean triangleIntersectsRectangle(
            double x0, double z0, double x1, double z1, double x2, double z2,
            double minX, double minZ, double maxX, double maxZ) {
        if (insideRectangle(x0, z0, minX, minZ, maxX, maxZ)
                || insideRectangle(x1, z1, minX, minZ, maxX, maxZ)
                || insideRectangle(x2, z2, minX, minZ, maxX, maxZ)
                || pointInTriangle(minX, minZ, x0, z0, x1, z1, x2, z2)
                || pointInTriangle(minX, maxZ, x0, z0, x1, z1, x2, z2)
                || pointInTriangle(maxX, minZ, x0, z0, x1, z1, x2, z2)
                || pointInTriangle(maxX, maxZ, x0, z0, x1, z1, x2, z2)) {
            return true;
        }
        double[][] triangleEdges = {{x0, z0, x1, z1}, {x1, z1, x2, z2}, {x2, z2, x0, z0}};
        double[][] rectangleEdges = {
                {minX, minZ, maxX, minZ}, {maxX, minZ, maxX, maxZ},
                {maxX, maxZ, minX, maxZ}, {minX, maxZ, minX, minZ}
        };
        for (double[] triangleEdge : triangleEdges) {
            for (double[] rectangleEdge : rectangleEdges) {
                if (segmentsIntersect(triangleEdge[0], triangleEdge[1],
                        triangleEdge[2], triangleEdge[3],
                        rectangleEdge[0], rectangleEdge[1],
                        rectangleEdge[2], rectangleEdge[3])) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean pointInTriangle(double px, double pz,
                                           double x0, double z0,
                                           double x1, double z1,
                                           double x2, double z2) {
        double d0 = cross(x1 - x0, z1 - z0, px - x0, pz - z0);
        double d1 = cross(x2 - x1, z2 - z1, px - x1, pz - z1);
        double d2 = cross(x0 - x2, z0 - z2, px - x2, pz - z2);
        boolean hasNegative = d0 < -1.0E-7D || d1 < -1.0E-7D || d2 < -1.0E-7D;
        boolean hasPositive = d0 > 1.0E-7D || d1 > 1.0E-7D || d2 > 1.0E-7D;
        return !(hasNegative && hasPositive);
    }

    private static boolean segmentsIntersect(double ax, double az, double bx, double bz,
                                             double cx, double cz, double dx, double dz) {
        if (Math.max(ax, bx) + 1.0E-7D < Math.min(cx, dx)
                || Math.max(cx, dx) + 1.0E-7D < Math.min(ax, bx)
                || Math.max(az, bz) + 1.0E-7D < Math.min(cz, dz)
                || Math.max(cz, dz) + 1.0E-7D < Math.min(az, bz)) {
            return false;
        }
        double abC = cross(bx - ax, bz - az, cx - ax, cz - az);
        double abD = cross(bx - ax, bz - az, dx - ax, dz - az);
        double cdA = cross(dx - cx, dz - cz, ax - cx, az - cz);
        double cdB = cross(dx - cx, dz - cz, bx - cx, bz - cz);
        return abC * abD <= 1.0E-7D && cdA * cdB <= 1.0E-7D;
    }

    private static double cross(double ax, double az, double bx, double bz) {
        return ax * bz - az * bx;
    }

    private static boolean insideRectangle(double x, double z,
                                           double minX, double minZ,
                                           double maxX, double maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static double radialX(double degrees, double radius) {
        return Math.sin(Math.toRadians(degrees)) * radius;
    }

    private static double radialZ(double degrees, double radius) {
        return Math.cos(Math.toRadians(degrees)) * radius;
    }

    private static double distanceToInterval(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0D;
    }

    private static float[] sectorYaws(
            float rotation, float sectorSpacing, int sectorCount) {
        float[] yaws = new float[sectorCount];
        for (int sector = 0; sector < sectorCount; ++sector) {
            yaws[sector] = rotation + sector * sectorSpacing;
        }
        return yaws;
    }

}
