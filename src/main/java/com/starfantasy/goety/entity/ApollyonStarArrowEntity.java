package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.library.vfx.StarFantasyStarArrowVisual;
import com.starfantasy.library.registry.StarFantasyLibraryParticleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Self-contained Apollyon projectile. The flight and hit logic belongs to this mod;
 * only the shared Star Fantasy library renderer/effect API is used for visuals.
 */
public final class ApollyonStarArrowEntity extends AbstractArrow
        implements StarFantasyStarArrowVisual {
    private static final float EXPLOSION_RADIUS = 2.0F;
    private static final float EXPLOSION_VISUAL_SIZE = 3.0F;
    private static final float DAMAGE_PER_TYPE = 15.0F;
    private static final int LIFETIME = 200;
    private static final int TRAIL_POINTS = 16;
    public static final int EXPLOSION_TRAIL_TICKS = 20;
    public static final int METEOR_FALL_TICKS = 40;
    public static final double METEOR_FALL_SPEED = 0.5D;
    private static final double TRAIL_RESET_DISTANCE_SQR = 64.0D;

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.m_135353_(
                    ApollyonStarArrowEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> METEOR =
            SynchedEntityData.m_135353_(
                    ApollyonStarArrowEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> EXPLODED =
            SynchedEntityData.m_135353_(
                    ApollyonStarArrowEntity.class, EntityDataSerializers.f_135035_);

    private final double[] trailX = new double[TRAIL_POINTS];
    private final double[] trailY = new double[TRAIL_POINTS];
    private final double[] trailZ = new double[TRAIL_POINTS];
    private boolean trailInitialized;
    private int explosionTrailAge;
    private int meteorFlightTicks;
    private double meteorImpactX;
    private double meteorImpactY;
    private double meteorImpactZ;

    public ApollyonStarArrowEntity(EntityType<? extends ApollyonStarArrowEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
        this.f_36705_ = Pickup.DISALLOWED;
    }

    public ApollyonStarArrowEntity(Level level, LivingEntity owner) {
        super(ApollyonEntityRegistry.APOLLYON_STAR_ARROW.get(), owner, level);
        this.m_20242_(true);
        this.f_36705_ = Pickup.DISALLOWED;
        this.f_19804_.m_135381_(COLOR, this.f_19796_.m_188503_(0x1000000));
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(COLOR, 0xF3AE18);
        this.f_19804_.m_135372_(METEOR, false);
        this.f_19804_.m_135372_(EXPLODED, false);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        if (this.isExploded()) {
            this.tickExplosionTrail();
            return;
        }
        if (this.isMeteor()) {
            this.tickMeteorFlight();
            return;
        }
        if (this.f_19797_ >= LIFETIME) {
            this.m_146870_();
            return;
        }
        if (this.m_9236_().f_46443_) {
            this.recordTrail();
        }
    }

    @Override
    protected void m_5790_(EntityHitResult result) {
        if (this.isMeteor() || this.isFriendlyToOwner(result.m_82443_())) {
            return;
        }
        this.explode();
    }

    @Override
    protected boolean m_5603_(Entity entity) {
        return !this.isMeteor() && !this.isFriendlyToOwner(entity) && super.m_5603_(entity);
    }

    @Override
    protected void m_8060_(BlockHitResult result) {
        if (!this.isMeteor()) {
            this.explode();
        }
    }

    @Override
    protected ItemStack m_7941_() {
        return ItemStack.f_41583_;
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("ApollyonStarColor", this.starFantasyStarArrowColor());
        tag.m_128379_("ApollyonStarMeteor", this.isMeteor());
        tag.m_128379_("ApollyonStarExploded", this.isExploded());
        tag.m_128405_("ApollyonStarExplosionTrailAge", this.explosionTrailAge);
        tag.m_128405_("ApollyonStarMeteorFlightTicks", this.meteorFlightTicks);
        tag.m_128347_("ApollyonStarMeteorImpactX", this.meteorImpactX);
        tag.m_128347_("ApollyonStarMeteorImpactY", this.meteorImpactY);
        tag.m_128347_("ApollyonStarMeteorImpactZ", this.meteorImpactZ);
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_("ApollyonStarColor")) {
            this.f_19804_.m_135381_(COLOR, tag.m_128451_("ApollyonStarColor") & 0xFFFFFF);
        }
        this.f_19804_.m_135381_(METEOR, tag.m_128471_("ApollyonStarMeteor"));
        this.f_19804_.m_135381_(EXPLODED, tag.m_128471_("ApollyonStarExploded"));
        this.explosionTrailAge = Math.max(0, tag.m_128451_("ApollyonStarExplosionTrailAge"));
        this.meteorFlightTicks = Math.max(0, tag.m_128451_("ApollyonStarMeteorFlightTicks"));
        this.meteorImpactX = tag.m_128459_("ApollyonStarMeteorImpactX");
        this.meteorImpactY = tag.m_128459_("ApollyonStarMeteorImpactY");
        this.meteorImpactZ = tag.m_128459_("ApollyonStarMeteorImpactZ");
    }

    public float getDamageRadius() {
        return EXPLOSION_RADIUS;
    }

    public static float explosionRadius() {
        return EXPLOSION_RADIUS;
    }

    public void configureMeteor(Vec3 impact) {
        this.f_19804_.m_135381_(METEOR, true);
        this.meteorFlightTicks = 0;
        this.meteorImpactX = impact.f_82479_;
        this.meteorImpactY = impact.f_82480_;
        this.meteorImpactZ = impact.f_82481_;
        this.m_20256_(impact.m_82546_(this.m_20182_()).m_82490_(1.0D / METEOR_FALL_TICKS));
    }

    public boolean isMeteor() {
        return this.f_19804_.m_135370_(METEOR);
    }

    public boolean isExploded() {
        return this.f_19804_.m_135370_(EXPLODED);
    }

    public int getExplosionTrailAge() {
        return this.explosionTrailAge;
    }

    @Override
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
    }

    @Override
    public AABB m_6921_() {
        return super.m_6921_().m_82400_(96.0D);
    }

    private void explode() {
        if (this.isExploded()) {
            return;
        }
        this.f_19804_.m_135381_(EXPLODED, true);
        this.explosionTrailAge = 0;
        this.m_20256_(Vec3.f_82478_);
        if (this.m_9236_().f_46443_) {
            return;
        }

        this.spawnLongDistanceExplosionVisual();
        this.m_9236_().m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(),
                SoundEvents.f_11913_, SoundSource.PLAYERS, 0.75F, 1.2F);
        this.hurtNearbyEntities();
    }

    private void spawnLongDistanceExplosionVisual() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.m_6907_()) {
            level.m_8624_(
                    player,
                    StarFantasyLibraryParticleRegistry.SWORD_EXPLOSION.get(),
                    true,
                    this.m_20185_(),
                    this.m_20186_(),
                    this.m_20189_(),
                    0,
                    EXPLOSION_VISUAL_SIZE,
                    0.0D,
                    0.0D,
                    1.0D);
        }
    }

    private void tickMeteorFlight() {
        this.m_20256_(new Vec3(0.0D, -METEOR_FALL_SPEED, 0.0D));
        if (this.m_9236_().f_46443_) {
            this.recordTrail();
            return;
        }
        if (++this.meteorFlightTicks >= METEOR_FALL_TICKS) {
            this.m_6034_(this.meteorImpactX, this.meteorImpactY, this.meteorImpactZ);
            this.explode();
        }
    }

    private void tickExplosionTrail() {
        this.m_20256_(Vec3.f_82478_);
        if (this.m_9236_().f_46443_) {
            this.recordTrail();
        }
        if (++this.explosionTrailAge >= EXPLOSION_TRAIL_TICKS
                && !this.m_9236_().f_46443_) {
            this.m_146870_();
        }
    }

    private void hurtNearbyEntities() {
        Entity owner = this.m_19749_();
        float damage = owner instanceof ApollyonEntity boss
                ? boss.scaleOutgoingDamage(DAMAGE_PER_TYPE)
                : DAMAGE_PER_TYPE;
        AABB area = this.m_20191_().m_82400_(EXPLOSION_RADIUS);
        for (LivingEntity target : this.m_9236_().m_45976_(LivingEntity.class, area)) {
            if (!target.m_6084_() || this.isFriendlyToOwner(target)) {
                continue;
            }
            if (target.m_20182_().m_82554_(this.m_20182_()) > EXPLOSION_RADIUS * EXPLOSION_RADIUS) {
                continue;
            }

            target.f_19802_ = 0;
            boolean damaged = target.m_6469_(
                    this.m_269291_().m_269104_(this, owner), damage);
            target.f_19802_ = 0;
            damaged |= target.m_6469_(
                    this.m_269291_().m_269036_(this, owner), damage);
            target.f_19802_ = 0;
            damaged |= target.m_6469_(
                    this.m_269291_().m_269418_(this, owner), damage);
            if (damaged) {
                this.applyAllApostleTitleEffects(target, owner);
            }
        }
    }

    private boolean isFriendlyToOwner(Entity entity) {
        Entity owner = this.m_19749_();
        if (entity == owner) {
            return true;
        }
        return owner instanceof ApollyonEntity boss && boss.isFriendlyEntity(entity);
    }

    private void applyAllApostleTitleEffects(LivingEntity target, Entity owner) {
        apply(target, MobEffects.f_19602_, 1, owner);
        apply(target, MobEffects.f_19614_, 200, owner);
        apply(target, MobEffects.f_19615_, 200, owner);
        apply(target, MobEffects.f_216964_, 200, owner);
        apply(target, MobEffects.f_19613_, 200, owner);
        apply(target, GoetyEffects.BURN_HEX.get(), 200, owner);
        apply(target, MobEffects.f_19612_, 200, owner);
        apply(target, MobEffects.f_19597_, 200, owner);
        apply(target, GoetyEffects.SAPPED.get(), 200, owner);
        target.m_20254_(5);

    }

    private static void apply(LivingEntity target, MobEffect effect, int duration, Entity source) {
        target.m_147207_(new MobEffectInstance(effect, duration, 0), source);
    }

    @Override
    public int starFantasyStarArrowColor() {
        return this.f_19804_.m_135370_(COLOR);
    }

    @Override
    public int starFantasyStarArrowLifetime() {
        return LIFETIME;
    }

    @Override
    public float starFantasyStarArrowVisualScale() {
        return 1.0F;
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
