package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.entities.projectiles.BlossomThorn;
import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.utils.ModDamageSource;
import net.minecraft.world.entity.Mob;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonPageantThornEntity;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Owns Apollyon's managed Earth Fist and Blossom Thorn spell sequence. */
public final class ApollyonWildSurgeManager {
    private static final String MANAGED_EARTH_FIST_TAG =
            "starfantasy_apollyon_managed_earth_fist";
    private static final String MANAGED_BLOSSOM_THORN_TAG =
            "starfantasy_apollyon_managed_blossom_thorn";
    private static final int EARTH_WARNING_TICKS = 20;
    private static final int THORN_WARNING_TICKS = 30;
    private static final double EARTH_WARNING_RADIUS = 1.5D;
    private static final double THORN_WARNING_RADIUS = 0.8D;
    private static final double EARTH_RING_RADIUS = 10.0D;
    private static final int EARTH_RING_COUNT = 30;
    private static final int[] THORN_RING_RADII = {2, 4, 6, 8};
    private static final float EARTH_FIST_DAMAGE = 30.0F;
    private static final float BLOSSOM_THORN_DAMAGE = 30.0F;
    private static final int EARTH_FIST_STUN_TICKS = 80;
    private static final int ACID_VENOM_TICKS = 200;
    private static final int ACID_VENOM_AMPLIFIER = 3;
    private static final double FORCED_UPWARD_VELOCITY = 1.0D;

    private ApollyonWildSurgeManager() {
    }

    public static Vec3 captureAnchor(Mob boss, LivingEntity target) {
        if (boss == null || target == null) {
            return null;
        }
        return groundCenterAt(boss.m_9236_(), target.m_20185_(),
                Math.max(target.m_20186_(), boss.m_20186_()) + 8.0D,
                target.m_20189_(), Mth.m_14107_(target.m_20185_()),
                Mth.m_14107_(target.m_20189_()));
    }

    public static void warnEarthRing(Mob boss, Vec3 anchor) {
        for (Vec3 point : earthRingPoints(boss, anchor)) {
            if (ApollyonSpellSupport.warnings(boss)) {
                StarFantasyVfx.redGroundWarningCircle(
                        boss, point.m_82520_(0.0D, 0.06D, 0.0D),
                        EARTH_WARNING_TICKS, EARTH_WARNING_RADIUS);
            }
        }
    }

    public static List<Vec3> spawnEarthRingAndWarnThorns(Mob boss, Vec3 anchor) {
        if (boss == null || anchor == null
                || !(boss.m_9236_() instanceof ServerLevel level)) {
            return List.of();
        }
        for (Vec3 point : earthRingPoints(boss, anchor)) {
            EarthFist fist = new EarthFist(level, point, boss);
            fist.m_20049_(MANAGED_EARTH_FIST_TAG);
            fist.setWarmupDelayTicks(0);
            level.m_7967_(fist);
        }
        List<Vec3> thornPoints = thornRingPoints(boss, anchor);
        for (Vec3 point : thornPoints) {
            if (ApollyonSpellSupport.warnings(boss)) {
                StarFantasyVfx.purpleGroundWarningCircle(
                        boss, point.m_82520_(0.0D, 0.06D, 0.0D),
                        THORN_WARNING_TICKS, THORN_WARNING_RADIUS);
            }
        }
        return thornPoints;
    }

    public static void spawnThornRings(Mob boss, List<Vec3> thornPoints) {
        if (boss == null || thornPoints == null || thornPoints.isEmpty()
                || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        for (Vec3 point : thornPoints) {
            BlossomThorn thorn = new BlossomThorn(level,
                    point.f_82479_, point.f_82480_, point.f_82481_, 0, boss);
            thorn.m_20049_(MANAGED_BLOSSOM_THORN_TAG);
            if (level.m_7967_(thorn)) {
                damageBlossomArea(level, boss, thorn, point);
            }
        }
    }

    /** Spawns a damage-suppressed ring of native Goety vine visuals for pageant scripts. */
    public static void spawnPageantThornVisualRing(
            ApollyonEntity boss, Vec3 center, double radius, int count, float visualScale) {
        if (boss == null || center == null || count <= 0
                || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        double angleOffset = boss.m_217043_().m_188500_() * Math.PI * 2.0D;
        for (int i = 0; i < count; ++i) {
            double angle = count == 1
                    ? angleOffset : angleOffset + Math.PI * 2.0D * i / count;
            Vec3 desired = center.m_82520_(
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
            Vec3 point = groundCenterAt(level, desired.f_82479_,
                    desired.f_82480_ + 8.0D, desired.f_82481_,
                    Mth.m_14107_(desired.f_82479_), Mth.m_14107_(desired.f_82481_));
            ApollyonPageantThornEntity thorn = new ApollyonPageantThornEntity(
                    ApollyonEntityRegistry.APOLLYON_PAGEANT_THORN.get(), level);
            thorn.initialize(boss, point, visualScale);
            thorn.m_20049_(MANAGED_BLOSSOM_THORN_TAG);
            thorn.setExtraDamage(0.0F);
            thorn.m_20225_(true);
            level.m_7967_(thorn);
        }
    }

    public static void clearManagedThornsForBoss(Mob boss) {
        if (boss == null || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        AABB area = new AABB(ApollyonSpellSupport.home(boss), ApollyonSpellSupport.home(boss))
                .m_82400_(128.0D);
        for (BlossomThorn thorn : level.m_45976_(BlossomThorn.class, area)) {
            if (thorn.m_19880_().contains(MANAGED_BLOSSOM_THORN_TAG)
                    && thorn.getOwner() == boss) {
                thorn.m_146870_();
            }
        }
    }

    /** Called by the narrow EarthFist mixin; true means Goety's native hit was replaced. */
    public static boolean replaceEarthFistDamage(EarthFist fist, LivingEntity target) {
        if (fist == null || !fist.m_19880_().contains(MANAGED_EARTH_FIST_TAG)) {
            return false;
        }
        LivingEntity owner = fist.m_269323_();
        if (!(owner instanceof Mob boss) || !ApollyonSpellSupport.isCaster(boss) || shouldSkip(boss, target)) {
            return true;
        }
        if (target.m_6469_(ApollyonDamageSources.front(target, target.m_269291_().m_269333_(boss)),
                ApollyonSpellSupport.damage(boss, target, EARTH_FIST_DAMAGE, 0.10F))) {
            Vec3 movement = target.m_20184_();
            target.m_20256_(new Vec3(
                    movement.f_82479_,
                    Math.max(movement.f_82480_, FORCED_UPWARD_VELOCITY),
                    movement.f_82481_));
            target.f_19812_ = true;
            target.m_7292_(new MobEffectInstance(
                    (MobEffect) GoetyEffects.STUNNED.get(), EARTH_FIST_STUN_TICKS));
        } else {
            target.m_6478_(MoverType.SHULKER_BOX,
                    new Vec3(0.0D, fist.m_20206_(), 0.0D));
        }
        return true;
    }

    /** Called by the narrow BlossomThorn mixin; managed native hits are fully suppressed. */
    public static boolean replaceBlossomThornDamage(BlossomThorn thorn, LivingEntity target) {
        return thorn != null && thorn.m_19880_().contains(MANAGED_BLOSSOM_THORN_TAG);
    }

    private static void damageBlossomArea(ServerLevel level, Mob boss,
                                          BlossomThorn thorn, Vec3 center) {
        AABB searchBox = new AABB(
                center.f_82479_ - THORN_WARNING_RADIUS, center.f_82480_ - 1.0D,
                center.f_82481_ - THORN_WARNING_RADIUS,
                center.f_82479_ + THORN_WARNING_RADIUS, center.f_82480_ + 4.0D,
                center.f_82481_ + THORN_WARNING_RADIUS);
        for (LivingEntity target : level.m_45976_(LivingEntity.class, searchBox)) {
            if (shouldSkip(boss, target)
                    || !intersectsHorizontalCircle(
                    target.m_20191_(), center, THORN_WARNING_RADIUS)) {
                continue;
            }
            if (target.m_6469_(ApollyonDamageSources.front(target, ModDamageSource.acid(thorn, boss)),
                    ApollyonSpellSupport.damage(boss, target, BLOSSOM_THORN_DAMAGE, 0.10F))) {
                target.m_7292_(new MobEffectInstance(
                        (MobEffect) GoetyEffects.ACID_VENOM.get(),
                        ACID_VENOM_TICKS, ACID_VENOM_AMPLIFIER));
            }
        }
    }

    private static List<Vec3> earthRingPoints(Mob boss, Vec3 anchor) {
        return ringPoints(boss, anchor, EARTH_RING_RADIUS, EARTH_RING_COUNT, 0.0D);
    }

    private static List<Vec3> thornRingPoints(Mob boss, Vec3 anchor) {
        List<Vec3> points = new ArrayList<>();
        for (int radius : THORN_RING_RADII) {
            double angleOffset = boss.m_217043_().m_188500_() * Math.PI * 2.0D;
            points.addAll(ringPoints(boss, anchor, radius, radius * 2, angleOffset));
        }
        return points;
    }

    private static List<Vec3> ringPoints(Mob boss, Vec3 anchor,
                                         double radius, int count, double angleOffset) {
        List<Vec3> points = new ArrayList<>();
        if (boss == null || anchor == null || count <= 0) {
            return points;
        }
        for (int i = 0; i < count; ++i) {
            double angle = angleOffset + Math.PI * 2.0D * i / count;
            Vec3 desired = anchor.m_82520_(
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
            points.add(groundCenterAt(boss.m_9236_(), desired.f_82479_,
                    desired.f_82480_ + 8.0D, desired.f_82481_,
                    Mth.m_14107_(desired.f_82479_), Mth.m_14107_(desired.f_82481_)));
        }
        return points;
    }

    private static boolean shouldSkip(Mob boss, LivingEntity target) {
        if (target == null || ApollyonSpellSupport.friendly(boss, target) || !target.m_6084_()
                || target.m_20147_()) {
            return true;
        }
        return target instanceof Player player && (player.m_7500_() || player.m_5833_());
    }

    private static boolean intersectsHorizontalCircle(AABB box, Vec3 center, double radius) {
        double dx = distanceToInterval(center.f_82479_, box.f_82288_, box.f_82291_);
        double dz = distanceToInterval(center.f_82481_, box.f_82290_, box.f_82293_);
        return dx * dx + dz * dz <= radius * radius;
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

    private static Vec3 groundCenterAt(Level level, double preciseX, double searchY,
                                       double preciseZ, int blockX, int blockZ) {
        int startY = Math.min(level.m_151558_() - 1, Mth.m_14107_(searchY));
        int minY = level.m_141937_() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) {
                return new Vec3(preciseX, y + 0.06D, preciseZ);
            }
        }
        return new Vec3(preciseX, searchY + 0.06D, preciseZ);
    }
}
