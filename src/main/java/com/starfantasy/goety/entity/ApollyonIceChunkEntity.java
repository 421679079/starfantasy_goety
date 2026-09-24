package com.starfantasy.goety.entity;

import net.minecraft.world.entity.Mob;
import com.starfantasy.goety.combat.ApollyonSpellSupport;
import com.Polarice3.Goety.client.particles.GatherFrostParticleOption;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.projectiles.IceChunk;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.combat.ApollyonDamageSources;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Goety IceChunk visuals with Apollyon-owned movement, warning and frozen damage. */
public final class ApollyonIceChunkEntity extends IceChunk {
    private static final float FROST_DAMAGE = 30.0F;
    private static final int STUN_DURATION_TICKS = 40;
    private static final int FORMATION_TICKS = 15;
    private static final int TRACKING_TICKS = 30;
    private static final int TRACKING_END_TICK = FORMATION_TICKS + TRACKING_TICKS;
    private static final int HOLD_TICKS = 10;
    private static final int FALL_TICKS = 5;
    private static final int WARNING_DURATION_TICKS = HOLD_TICKS + FALL_TICKS;
    private static final int NATIVE_IMPACT_TICK = TRACKING_END_TICK + HOLD_TICKS + FALL_TICKS - 1;
    private static final double HORIZONTAL_SPEED = 0.5D;
    private static final double SPAWN_HEIGHT = 2.0D;
    private static final double TRACKING_HEIGHT = 5.0D;
    private static final double CLIENT_INTERPOLATION = 0.5D;
    private static final double WARNING_RADIUS = 2.5D;
    private static final double IMPACT_VERTICAL_BELOW = 1.0D;
    private static final double IMPACT_VERTICAL_ABOVE = 2.5D;
    private static final String TIMELINE_TAG = "ApollyonFrostTimeline";
    private static final String GROUND_Y_TAG = "ApollyonFrostGroundY";
    private static final String GROUND_READY_TAG = "ApollyonFrostGroundReady";

    private int timelineTick;
    private double groundY;
    private boolean groundReady;
    private double clientTargetX;
    private double clientTargetY;
    private double clientTargetZ;
    private boolean clientTargetReady;
    private boolean customImpactApplied;
    private boolean suppressNativeDamage;

    public ApollyonIceChunkEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.hovering = 0;
    }

    public void initialize(Mob owner, LivingEntity target, double groundY) {
        this.setOwner(owner);
        this.setTarget(target);
        this.groundY = groundY;
        this.groundReady = true;
    }

    @Override
    public void m_8119_() {
        if (this.timelineTick >= NATIVE_IMPACT_TICK) {
            // Enter Goety's native collision/impact path only for the final fall.
            // This preserves its hit sound, ice burst, shock and AoE shape while
            // damageTargets below keeps damage ownership in this addon.
            this.hovering = 101;
            if (!this.m_9236_().f_46443_ && !this.customImpactApplied) {
                this.customImpactApplied = true;
                this.suppressNativeDamage = true;
                this.applyCircularImpactDamage();
            }
            if (this.m_9236_().f_46443_ && this.timelineTick == NATIVE_IMPACT_TICK) {
                this.m_6034_(this.m_20185_(), this.groundY, this.m_20189_());
            } else if (this.m_20184_().f_82480_ > -TRACKING_HEIGHT / FALL_TICKS) {
                this.m_20256_(new Vec3(0.0D, -TRACKING_HEIGHT / FALL_TICKS, 0.0D));
            }
            super.m_8119_();
            ++this.timelineTick;
            return;
        }

        // Entity.tick() delegates to baseTick(); calling it directly lets the
        // custom flight ignore IceChunk's built-in 100-tick hover controller.
        this.m_6075_();
        if (!this.groundReady) {
            this.groundY = this.m_20186_() - SPAWN_HEIGHT;
            this.groundReady = true;
        }

        this.hovering = Math.min(20,
                (this.timelineTick + 1) * 20 / FORMATION_TICKS);
        if (this.m_9236_().f_46443_) {
            this.tickClientPosition();
        } else {
            this.tickServerEffects();
            this.tickServerMotion();
        }
        ++this.timelineTick;
    }

    private void tickServerMotion() {
        if (this.timelineTick < FORMATION_TICKS) {
            this.m_20256_(Vec3.f_82478_);
        } else if (this.timelineTick < TRACKING_END_TICK) {
            this.tickTracking();
        } else if (this.timelineTick < TRACKING_END_TICK + HOLD_TICKS) {
            this.m_20256_(Vec3.f_82478_);
        } else {
            this.tickFall();
        }
    }

    private void tickTracking() {
        LivingEntity target = this.getTarget();
        double stepX = 0.0D;
        double stepZ = 0.0D;
        if (target != null && target.m_6084_()) {
            double deltaX = target.m_20185_() - this.m_20185_();
            double deltaZ = target.m_20189_() - this.m_20189_();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            if (horizontalDistance > 1.0E-5D) {
                double step = Math.min(HORIZONTAL_SPEED, horizontalDistance);
                stepX = deltaX / horizontalDistance * step;
                stepZ = deltaZ / horizontalDistance * step;
            }
        }
        int trackingTick = this.timelineTick - FORMATION_TICKS + 1;
        double nextY = this.groundY + SPAWN_HEIGHT
                + (TRACKING_HEIGHT - SPAWN_HEIGHT) * trackingTick / TRACKING_TICKS;
        Vec3 movement = new Vec3(stepX, nextY - this.m_20186_(), stepZ);
        this.m_20256_(movement);
        this.m_6478_(MoverType.SELF, movement);
    }

    private void tickFall() {
        int fallTick = this.timelineTick - TRACKING_END_TICK - HOLD_TICKS + 1;
        double nextY = this.groundY + TRACKING_HEIGHT
                * (FALL_TICKS - fallTick) / FALL_TICKS;
        Vec3 movement = new Vec3(0.0D, nextY - this.m_20186_(), 0.0D);
        this.m_20256_(movement);
        this.m_6478_(MoverType.SELF, movement);
    }

    private void tickClientPosition() {
        if (!this.clientTargetReady) {
            return;
        }
        double nextX = this.m_20185_()
                + (this.clientTargetX - this.m_20185_()) * CLIENT_INTERPOLATION;
        double nextZ = this.m_20189_()
                + (this.clientTargetZ - this.m_20189_()) * CLIENT_INTERPOLATION;
        double nextY;
        if (this.timelineTick >= TRACKING_END_TICK + HOLD_TICKS) {
            int fallTick = this.timelineTick - TRACKING_END_TICK - HOLD_TICKS + 1;
            nextY = this.groundY + TRACKING_HEIGHT
                    * (FALL_TICKS - fallTick) / FALL_TICKS;
        } else {
            nextY = this.m_20186_()
                    + (this.clientTargetY - this.m_20186_()) * CLIENT_INTERPOLATION;
        }
        this.m_6034_(nextX, nextY, nextZ);
    }

    private void tickServerEffects() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (this.timelineTick == 0) {
            this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_IDLE.get(), 1.0F, 1.0F);
        }
        if (this.timelineTick < FORMATION_TICKS) {
            ServerParticleUtil.outerCircleParticles(level,
                    new GatherFrostParticleOption(this.m_20182_().m_82520_(0.0D, 1.0D, 0.0D)),
                    this, 4.0F);
        }
        if (this.timelineTick == TRACKING_END_TICK) {
            Entity owner = this.m_269323_();
            if (ApollyonSpellSupport.warnings(owner)) {
            StarFantasyVfx.redGroundWarningCircle(
                    owner == null ? this : owner,
                    new Vec3(this.m_20185_(), this.groundY + 0.06D, this.m_20189_()),
                    WARNING_DURATION_TICKS, WARNING_RADIUS);
        }
            this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_DROP.get(), 1.0F, 1.0F);
        }
    }

    @Override
    public void m_6453_(double x, double y, double z, float yRot, float xRot,
                        int interpolationSteps, boolean teleport) {
        if (!this.m_9236_().f_46443_ || !this.groundReady) {
            super.m_6453_(x, y, z, yRot, xRot, interpolationSteps, teleport);
            this.clientTargetX = x;
            this.clientTargetY = y;
            this.clientTargetZ = z;
            this.clientTargetReady = true;
            return;
        }
        this.clientTargetX = x;
        this.clientTargetY = y;
        this.clientTargetZ = z;
        this.clientTargetReady = true;
        this.m_146922_(yRot);
        this.m_146926_(xRot);
    }

    @Override
    public boolean isStarting() {
        return this.timelineTick <= FORMATION_TICKS;
    }

    private void applyCircularImpactDamage() {
        LivingEntity owner = this.m_269323_();
        if ((!(owner instanceof Mob boss) || !ApollyonSpellSupport.isCaster(boss)) || !boss.m_6084_()) {
            return;
        }
        double centerX = this.m_20185_();
        double centerZ = this.m_20189_();
        AABB searchArea = new AABB(
                centerX - WARNING_RADIUS, this.groundY - IMPACT_VERTICAL_BELOW,
                centerZ - WARNING_RADIUS,
                centerX + WARNING_RADIUS, this.groundY + IMPACT_VERTICAL_ABOVE,
                centerZ + WARNING_RADIUS);
        for (LivingEntity target : this.m_9236_().m_45976_(LivingEntity.class, searchArea)) {
            if (!intersectsWarningCircle(target.m_20191_(), centerX, centerZ, WARNING_RADIUS)) {
                continue;
            }
            this.hurtTarget(boss, target);
        }
    }

    private static boolean intersectsWarningCircle(AABB box, double centerX,
                                                   double centerZ, double radius) {
        double dx = distanceToInterval(centerX, box.f_82288_, box.f_82291_);
        double dz = distanceToInterval(centerZ, box.f_82290_, box.f_82293_);
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

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.timelineTick = Math.max(0, tag.m_128451_(TIMELINE_TAG));
        this.groundY = tag.m_128459_(GROUND_Y_TAG);
        this.groundReady = tag.m_128471_(GROUND_READY_TAG);
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_(TIMELINE_TAG, this.timelineTick);
        tag.m_128347_(GROUND_Y_TAG, this.groundY);
        tag.m_128379_(GROUND_READY_TAG, this.groundReady);
    }

    @Override
    public void damageTargets(LivingEntity target) {
        LivingEntity owner = this.m_269323_();
        if (this.suppressNativeDamage || (!(owner instanceof Mob boss) || !ApollyonSpellSupport.isCaster(boss))) {
            return;
        }
        this.hurtTarget(boss, target);
    }

    private void hurtTarget(Mob boss, LivingEntity target) {
        if (target == null || ApollyonSpellSupport.friendly(boss, target) || !target.m_6084_()) {
            return;
        }
        if (target.m_6469_(ApollyonDamageSources.front(target, ModDamageSource.indirectFreeze(this, boss)),
                ApollyonSpellSupport.damage(boss, target, FROST_DAMAGE, 0.10F))) {
            target.m_7292_(new MobEffectInstance(
                    (MobEffect) GoetyEffects.STUNNED.get(), STUN_DURATION_TICKS));
        }
    }
}
