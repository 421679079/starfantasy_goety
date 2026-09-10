package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.library.vfx.StarFantasyStarArrowVisual;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Goety death arrow with a client-rendered blood-red ribbon trail. */
public final class ApollyonDeathArrowEntity extends DeathArrow
        implements StarFantasyStarArrowVisual {
    private static final int TRAIL_POINTS = 16;
    private static final int TRAIL_COLOR = 0x572225;
    private static final double TRAIL_RESET_DISTANCE_SQR = 64.0D;

    private final double[] trailX = new double[TRAIL_POINTS];
    private final double[] trailY = new double[TRAIL_POINTS];
    private final double[] trailZ = new double[TRAIL_POINTS];
    private boolean trailInitialized;

    public ApollyonDeathArrowEntity(
            EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public ApollyonDeathArrowEntity(Level level, LivingEntity owner) {
        super(level, owner);
    }

    @Override
    public EntityType<?> m_6095_() {
        return ApollyonEntityRegistry.APOLLYON_DEATH_ARROW.get();
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.recordTrail();
    }

    @Override
    protected void m_7761_(LivingEntity target) {
        // doPostHurtEffects only runs after hurt accepts the hit, even if mitigation
        // reduces its final damage to zero. Rejected invulnerability-frame hits skip it.
        super.m_7761_(target);
        if (target instanceof ServerPlayer player
                && this.m_19749_() instanceof ApollyonPageantApostleEntity apostle) {
            apostle.onDeathArrowHitPlayer(player);
        }
    }

    @Override
    public int starFantasyStarArrowColor() {
        return TRAIL_COLOR;
    }

    @Override
    public int starFantasyStarArrowLifetime() {
        return 200;
    }

    @Override
    public float starFantasyStarArrowVisualScale() {
        return 0.45F;
    }

    @Override
    public int starFantasyStarArrowTrailCount() {
        return TRAIL_POINTS;
    }

    @Override
    public Vec3 starFantasyStarArrowTrailPoint(int index) {
        if (!this.trailInitialized) {
            this.resetTrail(this.m_20182_());
        }
        int clamped = Mth.m_14045_(index, 0, TRAIL_POINTS - 1);
        if (clamped > 0) {
            return new Vec3(
                    (this.trailX[clamped] + this.trailX[clamped - 1]) * 0.5D,
                    (this.trailY[clamped] + this.trailY[clamped - 1]) * 0.5D,
                    (this.trailZ[clamped] + this.trailZ[clamped - 1]) * 0.5D);
        }
        return new Vec3(this.trailX[0], this.trailY[0], this.trailZ[0]);
    }

    private void resetTrail(Vec3 point) {
        for (int i = 0; i < TRAIL_POINTS; ++i) {
            this.trailX[i] = point.f_82479_;
            this.trailY[i] = point.f_82480_;
            this.trailZ[i] = point.f_82481_;
        }
        this.trailInitialized = true;
    }

    private void recordTrail() {
        Vec3 point = this.m_20182_();
        if (!this.trailInitialized
                || point.m_82531_(this.trailX[0], this.trailY[0], this.trailZ[0])
                > TRAIL_RESET_DISTANCE_SQR) {
            this.resetTrail(point);
            return;
        }
        for (int i = TRAIL_POINTS - 1; i > 0; --i) {
            this.trailX[i] = this.trailX[i - 1];
            this.trailY[i] = this.trailY[i - 1];
            this.trailZ[i] = this.trailZ[i - 1];
        }
        this.trailX[0] = point.f_82479_;
        this.trailY[0] = point.f_82480_;
        this.trailZ[0] = point.f_82481_;
    }
}
