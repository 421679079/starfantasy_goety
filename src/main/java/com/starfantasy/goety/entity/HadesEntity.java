package com.starfantasy.goety.entity;

import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.combat.ApollyonDeathEffects;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.goety.registry.HadesEntityRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.library.vfx.particle.WarningColor;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Collision-free, untargetable Hades visual bound to Apollyon's second phase. */
public final class HadesEntity extends Entity implements GeoEntity {
    private static final String OWNER_TAG = "ApollyonOwner";
    private static final String FIXED_X_TAG = "HadesFixedX";
    private static final String FIXED_Y_TAG = "HadesFixedY";
    private static final String FIXED_Z_TAG = "HadesFixedZ";
    private static final String CHARGING_TAG = "HadesCharging";
    private static final String SMASHING_TAG = "HadesSmashing";
    private static final String SMASH_AGE_TAG = "HadesSmashAge";
    private static final String RISE_AGE_TAG = "HadesRiseAge";
    private static final String DYING_TAG = "HadesDying";
    private static final String DEATH_TICKS_TAG = "HadesDeathTicks";
    private static final String COOPERATIVE_ATTACK_TAG = "HadesCooperativeAttack";
    private static final String COOPERATIVE_ATTACK_AGE_TAG = "HadesCooperativeAttackAge";

    public static final int DEATH_ANIMATION_TICKS = 80;
    private static final int RISE_TICKS = 80;
    private static final double RISE_DISTANCE = 20.0D;
    private static final int FORWARD_MOVE_TICKS = 10;
    private static final int RETURN_START_TICK = 60;
    private static final int RETURN_MOVE_TICKS = 10;
    private static final double SMASH_TRAVEL_DISTANCE = 10.0D;
    public static final int ATTACK_NONE = 0;
    public static final int ATTACK_ROUNDHOUSE = 1;
    public static final int ATTACK_CLAW_COMBO = 2;
    public static final int ATTACK_DIVE_RAY = 3;
    private static final int ATTACK_DIVE_RAY_OVERHEAD_FIRST = 4;
    private static final int ROUNDHOUSE_RED_HIT_TICK = 28;
    private static final int ROUNDHOUSE_PURPLE_HIT_TICK = 40;
    private static final int ROUNDHOUSE_SLASH_LEAD_TICKS = 2;
    private static final int ROUNDHOUSE_RETURN_TICK = 70;
    private static final int ROUNDHOUSE_END_TICK = 80;
    private static final int CLAW_FIRST_HIT_TICK = 22;
    private static final int CLAW_SECOND_START_TICK = 30;
    private static final int CLAW_SECOND_HIT_TICK = CLAW_SECOND_START_TICK + 12;
    private static final int CLAW_RETURN_TICK = CLAW_SECOND_START_TICK + 40;
    private static final int CLAW_END_TICK = CLAW_RETURN_TICK + 10;
    private static final double COOPERATIVE_DAMAGE = 80.0D;
    private static final float CLAW_BLAST_VISUAL_SIZE = 6.0F;
    private static final double ROUNDHOUSE_KNOCKBACK_SPEED = 2.2D;
    private static final double CLAW_KNOCKBACK_SPEED = 2.2D;
    private static final double ROUNDHOUSE_SAFE_RADIUS = 10.0D;
    private static final double ROUNDHOUSE_SECOND_SAFE_RADIUS = 15.0D;
    private static final double ROUNDHOUSE_WARNING_RADIUS = 20.0D;
    private static final double ROUNDHOUSE_SECOND_WARNING_RADIUS = 25.0D;
    private static final float ROUNDHOUSE_SLASH_RADIUS_RATIO = 0.8F;
    private static final double FRONT_DAMAGE_SOURCE_DISTANCE = 2.0D;
    private static final double CLAW_WARNING_WIDTH = 15.0D;
    private static final double CLAW_WARNING_LENGTH = 40.0D;
    private static final double[] CLAW_BLAST_DISTANCES = {
            5.0D, 10.0D, 15.0D, 20.0D, 25.0D, 30.0D, 35.0D
    };
    private static final int DIVE_RAY_SHOOT_HIT_TICK = 30;
    private static final int DIVE_RAY_OVERHEAD_HIT_TICK = 30;
    private static final int DIVE_RAY_SHOOT_FIRST_SWITCH_TICK = 60;
    private static final int DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK = 55;
    private static final int DIVE_RAY_SHOOT_DURATION_TICKS = 70;
    private static final int DIVE_RAY_OVERHEAD_DURATION_TICKS = 60;
    private static final double DIVE_RAY_SHOOT_WIDTH = 15.0D;
    // Denia's renderer draws the beam at 70% of its stored width. This value
    // therefore produces the requested twelve-block maximum visual diameter.
    private static final double DIVE_RAY_LASER_STORED_WIDTH = 12.0D / 0.7D;
    private static final double DIVE_RAY_OVERHEAD_WIDTH = 15.0D;
    private static final double DIVE_RAY_LENGTH = 40.0D;
    private static final double DIVE_RAY_OVERHEAD_LANE_OFFSET = 12.5D;
    private static final double DIVE_RAY_KNOCKBACK_SPEED = 2.2D;
    // playPyroclastExplosion takes the absolute particle size; Goety's base
    // pyroclast visual is size 3, so size 9 is a three-times-scale blast.
    private static final float DIVE_RAY_EXPLOSION_VISUAL_SIZE = 9.0F;
    private static final double[] DIVE_RAY_EXPLOSION_DISTANCES = {
            5.0D, 10.0D, 15.0D, 20.0D, 25.0D, 30.0D, 35.0D
    };
    private static final int ROUNDHOUSE_HIT_SHAKE_TICKS = 10;
    private static final float ROUNDHOUSE_HIT_SHAKE_INTENSITY = 1.3F;
    private static final int RED_WARNING_COLOR = 0xB00000;

    private static final EntityDataAccessor<Boolean> CHARGING =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> SMASHING =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> SMASH_AGE =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> RISE_AGE =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> DYING =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> DEATH_TICKS =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> COOPERATIVE_ATTACK =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> COOPERATIVE_ATTACK_AGE =
            SynchedEntityData.m_135353_(HadesEntity.class, EntityDataSerializers.f_135028_);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SMASH =
            RawAnimation.begin().thenPlay("smash").thenLoop("idle");
    private static final RawAnimation ROUNDHOUSE =
            RawAnimation.begin().thenPlay("roundhouse").thenLoop("idle");
    private static final RawAnimation CLAW_ONE =
            RawAnimation.begin().thenPlay("claw1").thenLoop("idle");
    private static final RawAnimation CLAW_TWO =
            RawAnimation.begin().thenPlay("claw2").thenLoop("idle");
    private static final RawAnimation SHOOT =
            RawAnimation.begin().thenPlay("shoot").thenLoop("idle");
    private static final RawAnimation OVERHEAD_SWIPE =
            RawAnimation.begin().thenPlay("overhead_swipe").thenLoop("idle");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache animationCache =
            GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private Vec3 fixedPosition = Vec3.f_82478_;
    private boolean clientFixedPositionInitialized;

    public HadesEntity(EntityType<? extends HadesEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static HadesEntity spawn(ApollyonEntity owner, Vec3 position) {
        return spawn(owner, position, false);
    }

    public static HadesEntity spawnRising(ApollyonEntity owner, Vec3 position) {
        return spawn(owner, position, true);
    }

    private static HadesEntity spawn(
            ApollyonEntity owner, Vec3 position, boolean riseFromBelow) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        HadesEntity hades = new HadesEntity(HadesEntityRegistry.HADES.get(), owner.m_9236_());
        hades.ownerUuid = owner.m_20148_();
        hades.fixedPosition = position;
        hades.f_19804_.m_135381_(RISE_AGE, riseFromBelow ? 0 : RISE_TICKS);
        hades.applyControlledPosition();
        hades.setFacingSouth();
        return owner.m_9236_().m_7967_(hades) ? hades : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(CHARGING, false);
        this.f_19804_.m_135372_(SMASHING, false);
        this.f_19804_.m_135372_(SMASH_AGE, 0);
        this.f_19804_.m_135372_(RISE_AGE, RISE_TICKS);
        this.f_19804_.m_135372_(DYING, false);
        this.f_19804_.m_135372_(DEATH_TICKS, 0);
        this.f_19804_.m_135372_(COOPERATIVE_ATTACK, ATTACK_NONE);
        this.f_19804_.m_135372_(COOPERATIVE_ATTACK_AGE, 0);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.isPlayingDeathAnimation()) {
            this.applyControlledPosition();
            this.setFacingSouth();
            if (!this.m_9236_().f_46443_) {
                int ticks = Math.min(DEATH_ANIMATION_TICKS, this.getDeathAnimationTicks() + 1);
                this.f_19804_.m_135381_(DEATH_TICKS, ticks);
                if (ticks >= DEATH_ANIMATION_TICKS) {
                    ApollyonDeathEffects.explodeHades(this);
                    this.m_142687_(Entity.RemovalReason.KILLED);
                }
            }
            return;
        }
        if (this.m_9236_().f_46443_) {
            this.initializeClientFixedPosition();
            this.applyControlledPosition();
            this.setFacingSouth();
            return;
        }

        ApollyonEntity owner = this.owner();
        if (owner == null || !owner.m_6084_()) {
            this.m_146870_();
            return;
        }
        int state = owner.getPageantState();
        if (state != ApollyonPageantController.FIFTH_STAGE
                && !owner.isCombatPhaseTwo()) {
            this.m_146870_();
            return;
        }
        if (this.f_19804_.m_135370_(SMASHING) && this.smashAge() < smashMotionEndTick()) {
            this.f_19804_.m_135381_(SMASH_AGE, this.smashAge() + 1);
        }
        if (this.isCooperativeAttacking()) {
            int attackAge = this.cooperativeAttackAge() + 1;
            this.f_19804_.m_135381_(COOPERATIVE_ATTACK_AGE, attackAge);
            this.tickCooperativeAttack(owner, attackAge);
        }
        if (this.riseAge() < RISE_TICKS) {
            this.f_19804_.m_135381_(RISE_AGE, this.riseAge() + 1);
        }
        this.applyControlledPosition();
        this.setFacingSouth();
    }

    public void startCharging() {
        this.f_19804_.m_135381_(CHARGING, true);
    }

    public void startSmash() {
        this.f_19804_.m_135381_(CHARGING, false);
        this.f_19804_.m_135381_(SMASHING, true);
        this.f_19804_.m_135381_(SMASH_AGE, 0);
        this.applyControlledPosition();
    }

    public void startCooperativeAttack(int attackType) {
        if (this.m_9236_().f_46443_ || this.isPlayingDeathAnimation()) {
            return;
        }
        int selected;
        if (attackType == ATTACK_CLAW_COMBO) {
            selected = ATTACK_CLAW_COMBO;
        } else if (attackType == ATTACK_DIVE_RAY) {
            selected = this.m_9236_().m_213780_().m_188503_(2) == 0
                    ? ATTACK_DIVE_RAY : ATTACK_DIVE_RAY_OVERHEAD_FIRST;
        } else {
            selected = ATTACK_ROUNDHOUSE;
        }
        this.f_19804_.m_135381_(CHARGING, false);
        this.f_19804_.m_135381_(SMASHING, false);
        this.f_19804_.m_135381_(SMASH_AGE, 0);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK, selected);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK_AGE, 0);
        ApollyonEntity owner = this.owner();
        if (owner != null) {
            if (selected == ATTACK_ROUNDHOUSE) {
                StarFantasyVfx.groundWarningAnnulusOwnedStatic(
                        owner, this.roundhouseCenter(owner), ROUNDHOUSE_RED_HIT_TICK,
                        ROUNDHOUSE_SAFE_RADIUS, ROUNDHOUSE_WARNING_RADIUS, WarningColor.RED);
            } else if (selected == ATTACK_CLAW_COMBO) {
                Vec3 direction = this.clawDirection(owner, true);
                StarFantasyVfx.groundRectangleWarning(
                        owner, owner.arenaHomePosition().m_82520_(0.0D, 0.06D, 0.0D),
                        CLAW_FIRST_HIT_TICK, CLAW_WARNING_WIDTH, CLAW_WARNING_LENGTH,
                        yawFromDirection(direction), RED_WARNING_COLOR, true, false, true);
            } else {
                this.startDiveRayActionWarning(owner, this.isDiveRayShootAction());
            }
        }
        this.applyControlledPosition();
    }

    public void beginDeathAnimation() {
        if (this.m_9236_().f_46443_ || this.isPlayingDeathAnimation()) {
            return;
        }
        this.f_19804_.m_135381_(CHARGING, false);
        this.f_19804_.m_135381_(SMASHING, false);
        this.f_19804_.m_135381_(SMASH_AGE, 0);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK, ATTACK_NONE);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK_AGE, 0);
        this.f_19804_.m_135381_(DYING, true);
        this.f_19804_.m_135381_(DEATH_TICKS, 0);
        this.m_20256_(Vec3.f_82478_);
    }

    public boolean isPlayingDeathAnimation() {
        return this.f_19804_.m_135370_(DYING);
    }

    public int getDeathAnimationTicks() {
        return this.f_19804_.m_135370_(DEATH_TICKS);
    }

    public int getDeathAnimationDurationTicks() {
        return DEATH_ANIMATION_TICKS;
    }

    public boolean isCharging() {
        return this.f_19804_.m_135370_(CHARGING);
    }

    public int smashAge() {
        return this.f_19804_.m_135370_(SMASH_AGE);
    }

    public int chargeAfterglowAge() {
        return this.f_19804_.m_135370_(SMASHING) ? this.smashAge() : -1;
    }

    public boolean isCooperativeAttacking() {
        return this.f_19804_.m_135370_(COOPERATIVE_ATTACK) != ATTACK_NONE;
    }

    public int cooperativeAttackType() {
        return this.f_19804_.m_135370_(COOPERATIVE_ATTACK);
    }

    public int cooperativeAttackAge() {
        return this.f_19804_.m_135370_(COOPERATIVE_ATTACK_AGE);
    }

    public boolean isDiveRayShootCharging() {
        int actionAge = this.diveRayActionAge();
        return this.isDiveRayShootAction()
                && actionAge >= 0 && actionAge < DIVE_RAY_SHOOT_HIT_TICK;
    }

    public int diveRayShootAfterglowAge() {
        int actionAge = this.diveRayActionAge();
        return this.isDiveRayShootAction() && actionAge >= DIVE_RAY_SHOOT_HIT_TICK
                ? actionAge - DIVE_RAY_SHOOT_HIT_TICK : -1;
    }

    private int riseAge() {
        return this.f_19804_.m_135370_(RISE_AGE);
    }

    @Override
    public void m_6453_(double x, double y, double z, float yaw, float pitch,
                        int interpolationSteps, boolean teleport) {
        if (this.m_9236_().f_46443_
                && (this.f_19804_.m_135370_(SMASHING)
                || this.isCooperativeAttacking() || this.riseAge() < RISE_TICKS)) {
            this.m_146922_(yaw);
            this.m_146926_(pitch);
            return;
        }
        super.m_6453_(x, y, z, yaw, pitch, interpolationSteps, teleport);
    }

    public UUID ownerUuid() {
        return this.ownerUuid;
    }

    private ApollyonEntity owner() {
        if (this.ownerUuid == null || !(this.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.m_8791_(this.ownerUuid);
        return entity instanceof ApollyonEntity apollyon ? apollyon : null;
    }

    private void setFacingSouth() {
        this.m_146922_(0.0F);
        this.m_146926_(0.0F);
        this.f_19859_ = 0.0F;
    }

    private void initializeClientFixedPosition() {
        if (this.clientFixedPositionInitialized) {
            return;
        }
        double offset = this.f_19804_.m_135370_(SMASHING)
                ? smashForwardOffset(this.smashAge())
                : cooperativeForwardOffset(
                        this.cooperativeAttackType(), this.cooperativeAttackAge());
        double riseOffset = riseVerticalOffset(this.riseAge());
        this.fixedPosition = this.m_20182_().m_82520_(0.0D, -riseOffset, -offset);
        this.clientFixedPositionInitialized = true;
    }

    private void applyControlledPosition() {
        double offset = this.f_19804_.m_135370_(SMASHING)
                ? smashForwardOffset(this.smashAge())
                : cooperativeForwardOffset(
                        this.cooperativeAttackType(), this.cooperativeAttackAge());
        double riseOffset = riseVerticalOffset(this.riseAge());
        this.m_6034_(this.fixedPosition.f_82479_, this.fixedPosition.f_82480_ + riseOffset,
                this.fixedPosition.f_82481_ + offset);
        this.m_20256_(Vec3.f_82478_);
    }

    private static double riseVerticalOffset(int age) {
        double progress = Math.max(0.0D, Math.min(1.0D, age / (double) RISE_TICKS));
        return -RISE_DISTANCE * (1.0D - progress);
    }

    private static double smashForwardOffset(int age) {
        if (age <= 0) {
            return 0.0D;
        }
        if (age < FORWARD_MOVE_TICKS) {
            return SMASH_TRAVEL_DISTANCE * age / FORWARD_MOVE_TICKS;
        }
        if (age < RETURN_START_TICK) {
            return SMASH_TRAVEL_DISTANCE;
        }
        if (age < smashMotionEndTick()) {
            return SMASH_TRAVEL_DISTANCE * (smashMotionEndTick() - age)
                    / RETURN_MOVE_TICKS;
        }
        return 0.0D;
    }

    private static int smashMotionEndTick() {
        return RETURN_START_TICK + RETURN_MOVE_TICKS;
    }

    private static double cooperativeForwardOffset(int attackType, int age) {
        if (attackType == ATTACK_ROUNDHOUSE) {
            return stagedMovement(age, 5.0D, FORWARD_MOVE_TICKS,
                    ROUNDHOUSE_RETURN_TICK, ROUNDHOUSE_END_TICK);
        }
        if (attackType == ATTACK_CLAW_COMBO) {
            return stagedMovement(age, 15.0D, FORWARD_MOVE_TICKS,
                    CLAW_RETURN_TICK, CLAW_END_TICK);
        }
        return 0.0D;
    }

    private static double stagedMovement(
            int age, double distance, int forwardTicks, int returnTick, int endTick) {
        if (age <= 0) {
            return 0.0D;
        }
        if (age < forwardTicks) {
            return distance * age / forwardTicks;
        }
        if (age < returnTick) {
            return distance;
        }
        if (age < endTick) {
            return distance * (endTick - age) / (double) (endTick - returnTick);
        }
        return 0.0D;
    }

    private void tickCooperativeAttack(ApollyonEntity owner, int age) {
        int attack = this.cooperativeAttackType();
        if (attack == ATTACK_ROUNDHOUSE) {
            this.tickRoundhouse(owner, age);
            if (age >= ROUNDHOUSE_END_TICK) {
                this.finishCooperativeAttack();
            }
        } else if (attack == ATTACK_CLAW_COMBO) {
            this.tickClawCombo(owner, age);
            if (age >= CLAW_END_TICK) {
                this.finishCooperativeAttack();
            }
        } else if (isDiveRayAttack(attack)) {
            this.tickDiveRay(owner, age);
            if (age >= diveRayEndTick(attack)) {
                this.finishCooperativeAttack();
            }
        } else {
            this.finishCooperativeAttack();
        }
    }

    private void tickRoundhouse(ApollyonEntity owner, int age) {
        Vec3 center = this.roundhouseCenter(owner);
        if (age == ROUNDHOUSE_RED_HIT_TICK - ROUNDHOUSE_SLASH_LEAD_TICKS) {
            this.spawnRoundhouseSlash(center,
                    (float) ROUNDHOUSE_WARNING_RADIUS * ROUNDHOUSE_SLASH_RADIUS_RATIO);
        } else if (age == ROUNDHOUSE_RED_HIT_TICK) {
            this.playRoundhouseImpact(center);
            this.damageAnnulus(owner, center, ROUNDHOUSE_SAFE_RADIUS,
                    ROUNDHOUSE_WARNING_RADIUS,
                    ROUNDHOUSE_KNOCKBACK_SPEED);
            StarFantasyVfx.groundWarningAnnulusOwnedStatic(
                    owner, center, ROUNDHOUSE_PURPLE_HIT_TICK - ROUNDHOUSE_RED_HIT_TICK,
                    ROUNDHOUSE_SECOND_SAFE_RADIUS, ROUNDHOUSE_SECOND_WARNING_RADIUS,
                    WarningColor.RED);
        } else if (age == ROUNDHOUSE_PURPLE_HIT_TICK - ROUNDHOUSE_SLASH_LEAD_TICKS) {
            this.spawnRoundhouseSlash(center,
                    (float) ROUNDHOUSE_SECOND_WARNING_RADIUS
                            * ROUNDHOUSE_SLASH_RADIUS_RATIO);
        } else if (age == ROUNDHOUSE_PURPLE_HIT_TICK) {
            this.playRoundhouseImpact(center);
            this.damageAnnulus(owner, center, ROUNDHOUSE_SECOND_SAFE_RADIUS,
                    ROUNDHOUSE_SECOND_WARNING_RADIUS,
                    ROUNDHOUSE_KNOCKBACK_SPEED);
        }
    }

    private void spawnRoundhouseSlash(Vec3 center, float radius) {
        HadesClawSlashEntity.spawn(this, center, radius);
    }

    private void playRoundhouseImpact(Vec3 center) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        StarFantasyVfx.areaShake(
                this, center, 64.0D,
                ROUNDHOUSE_HIT_SHAKE_TICKS, ROUNDHOUSE_HIT_SHAKE_INTENSITY);
        level.m_5594_(null, BlockPos.m_274561_(
                        center.f_82479_, center.f_82480_, center.f_82481_),
                ApollyonSoundRegistry.ATTACK_HADES.get(),
                SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    private void tickClawCombo(ApollyonEntity owner, int age) {
        if (age == CLAW_FIRST_HIT_TICK) {
            Vec3 direction = this.clawDirection(owner, true);
            this.playClawImpact(owner, direction, true);
            this.damageRectangle(owner, owner.arenaHomePosition(), direction,
                    CLAW_WARNING_WIDTH, CLAW_WARNING_LENGTH, CLAW_KNOCKBACK_SPEED);
        } else if (age == CLAW_SECOND_START_TICK) {
            Vec3 direction = this.clawDirection(owner, false);
            StarFantasyVfx.groundRectangleWarning(
                    owner, owner.arenaHomePosition().m_82520_(0.0D, 0.06D, 0.0D),
                    CLAW_SECOND_HIT_TICK - CLAW_SECOND_START_TICK,
                    CLAW_WARNING_WIDTH, CLAW_WARNING_LENGTH,
                    yawFromDirection(direction), RED_WARNING_COLOR, true, false, true);
        } else if (age == CLAW_SECOND_HIT_TICK) {
            Vec3 direction = this.clawDirection(owner, false);
            this.playClawImpact(owner, direction, false);
            this.damageRectangle(owner, owner.arenaHomePosition(), direction,
                    CLAW_WARNING_WIDTH, CLAW_WARNING_LENGTH, CLAW_KNOCKBACK_SPEED);
        }

        int firstExplosionIndex = age - CLAW_FIRST_HIT_TICK;
        if (firstExplosionIndex >= 0
                && firstExplosionIndex < CLAW_BLAST_DISTANCES.length) {
            this.playClawSweepExplosionStep(
                    owner.arenaHomePosition(), this.clawDirection(owner, true),
                    firstExplosionIndex);
        }
        int secondExplosionIndex = age - CLAW_SECOND_HIT_TICK;
        if (secondExplosionIndex >= 0
                && secondExplosionIndex < CLAW_BLAST_DISTANCES.length) {
            this.playClawSweepExplosionStep(
                    owner.arenaHomePosition(), this.clawDirection(owner, false),
                    secondExplosionIndex);
        }
    }

    private void tickDiveRay(ApollyonEntity owner, int age) {
        int attack = this.cooperativeAttackType();
        int secondActionTick = attack == ATTACK_DIVE_RAY
                ? DIVE_RAY_SHOOT_FIRST_SWITCH_TICK
                : DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK;
        if (age == secondActionTick) {
            this.startDiveRayActionWarning(owner, this.isDiveRayShootAction());
        }

        boolean shoot = this.isDiveRayShootAction();
        int actionAge = this.diveRayActionAge();
        int hitTick = shoot ? DIVE_RAY_SHOOT_HIT_TICK : DIVE_RAY_OVERHEAD_HIT_TICK;
        if (actionAge == hitTick) {
            this.resolveDiveRayHit(owner, shoot);
        }
        int explosionIndex = actionAge - hitTick;
        if (!shoot && explosionIndex >= 0
                && explosionIndex < DIVE_RAY_EXPLOSION_DISTANCES.length) {
            this.playDiveRayExplosionStep(owner, explosionIndex);
        }
    }

    private void startDiveRayActionWarning(ApollyonEntity owner, boolean shoot) {
        Vec3 direction = this.diveRayDirection(owner);
        float yaw = yawFromDirection(direction);
        if (shoot) {
            this.playDiveRayChargeSound(direction);
            StarFantasyVfx.groundRectangleWarning(
                    owner, this.fixedPosition.m_82520_(0.0D, 0.06D, 0.0D),
                    DIVE_RAY_SHOOT_HIT_TICK,
                    DIVE_RAY_SHOOT_WIDTH, DIVE_RAY_LENGTH, yaw,
                    RED_WARNING_COLOR, false, false, true);
            return;
        }
        Vec3 right = horizontalRight(direction);
        for (double side : new double[]{-1.0D, 1.0D}) {
            Vec3 origin = this.fixedPosition
                    .m_82549_(right.m_82490_(DIVE_RAY_OVERHEAD_LANE_OFFSET * side))
                    .m_82520_(0.0D, 0.06D, 0.0D);
            StarFantasyVfx.groundRectangleWarning(
                    owner, origin, DIVE_RAY_OVERHEAD_HIT_TICK,
                    DIVE_RAY_OVERHEAD_WIDTH, DIVE_RAY_LENGTH, yaw,
                    RED_WARNING_COLOR, false, false, true);
        }
    }

    private void resolveDiveRayHit(ApollyonEntity owner, boolean shoot) {
        Vec3 home = owner.arenaHomePosition();
        Vec3 direction = this.diveRayDirection(owner);
        if (shoot) {
            this.damageRectangle(owner, home, direction,
                    DIVE_RAY_SHOOT_WIDTH, DIVE_RAY_LENGTH,
                    DIVE_RAY_KNOCKBACK_SPEED);
            HadesDiveRayLaserEntity.spawn(
                    this, this.fixedPosition.m_82520_(0.0D, 5.0D, 0.0D),
                    direction, DIVE_RAY_LASER_STORED_WIDTH, DIVE_RAY_LENGTH);
        } else {
            Vec3 right = horizontalRight(direction);
            Vec3 leftLane = home.m_82549_(
                    right.m_82490_(-DIVE_RAY_OVERHEAD_LANE_OFFSET));
            Vec3 rightLane = home.m_82549_(
                    right.m_82490_(DIVE_RAY_OVERHEAD_LANE_OFFSET));
            HadesClawSlashEntity.spawnRectangle(
                    this, leftLane, direction,
                    (float) DIVE_RAY_OVERHEAD_WIDTH,
                    (float) DIVE_RAY_LENGTH, true);
            HadesClawSlashEntity.spawnRectangle(
                    this, rightLane, direction,
                    (float) DIVE_RAY_OVERHEAD_WIDTH,
                    (float) DIVE_RAY_LENGTH, false);
            this.damageRectangle(owner, leftLane,
                    direction, DIVE_RAY_OVERHEAD_WIDTH, DIVE_RAY_LENGTH,
                    DIVE_RAY_KNOCKBACK_SPEED);
            this.damageRectangle(owner, rightLane,
                    direction, DIVE_RAY_OVERHEAD_WIDTH, DIVE_RAY_LENGTH,
                    DIVE_RAY_KNOCKBACK_SPEED);
        }
        this.playDiveRayImpact(owner, shoot);
    }

    private void playDiveRayChargeSound(Vec3 direction) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 charge = this.fixedPosition
                .m_82549_(direction.m_82490_(5.0D))
                .m_82520_(0.0D, 5.0D, 0.0D);
        level.m_5594_(null, BlockPos.m_274561_(
                        charge.f_82479_, charge.f_82480_, charge.f_82481_),
                ApollyonSoundRegistry.CAST_HADES.get(),
                SoundSource.HOSTILE, 3.0F, 1.0F);
    }

    private void playDiveRayImpact(ApollyonEntity owner, boolean shoot) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 home = owner.arenaHomePosition();
        StarFantasyVfx.areaShake(
                this, home, 64.0D,
                ROUNDHOUSE_HIT_SHAKE_TICKS, ROUNDHOUSE_HIT_SHAKE_INTENSITY);
        Vec3 soundPosition = shoot ? this.fixedPosition : home;
        level.m_5594_(null, BlockPos.m_274561_(
                        soundPosition.f_82479_, soundPosition.f_82480_,
                        soundPosition.f_82481_),
                shoot ? ApollyonSoundRegistry.SUMMON_APOSTLE.get()
                        : ApollyonSoundRegistry.ATTACK_HADES.get(),
                SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    private void playDiveRayExplosionStep(ApollyonEntity owner, int index) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 direction = this.diveRayDirection(owner);
        Vec3 center = this.fixedPosition.m_82549_(
                direction.m_82490_(DIVE_RAY_EXPLOSION_DISTANCES[index]));
        Vec3 right = horizontalRight(direction);
        ApollyonPageantController.playPyroclastExplosion(
                level,
                center.m_82549_(right.m_82490_(-DIVE_RAY_OVERHEAD_LANE_OFFSET)),
                DIVE_RAY_EXPLOSION_VISUAL_SIZE);
        ApollyonPageantController.playPyroclastExplosion(
                level,
                center.m_82549_(right.m_82490_(DIVE_RAY_OVERHEAD_LANE_OFFSET)),
                DIVE_RAY_EXPLOSION_VISUAL_SIZE);
    }

    private Vec3 diveRayDirection(ApollyonEntity owner) {
        return this.horizontalDirection(this.fixedPosition, owner.arenaHomePosition());
    }

    private boolean isDiveRayShootAction() {
        int attack = this.cooperativeAttackType();
        int age = this.cooperativeAttackAge();
        if (attack == ATTACK_DIVE_RAY) {
            return age < DIVE_RAY_SHOOT_FIRST_SWITCH_TICK;
        }
        return attack == ATTACK_DIVE_RAY_OVERHEAD_FIRST
                && age >= DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK;
    }

    private int diveRayActionAge() {
        int attack = this.cooperativeAttackType();
        int age = this.cooperativeAttackAge();
        if (attack == ATTACK_DIVE_RAY) {
            return age < DIVE_RAY_SHOOT_FIRST_SWITCH_TICK
                    ? age : age - DIVE_RAY_SHOOT_FIRST_SWITCH_TICK;
        }
        if (attack == ATTACK_DIVE_RAY_OVERHEAD_FIRST) {
            return age < DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK
                    ? age : age - DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK;
        }
        return -1;
    }

    private static boolean isDiveRayAttack(int attack) {
        return attack == ATTACK_DIVE_RAY
                || attack == ATTACK_DIVE_RAY_OVERHEAD_FIRST;
    }

    private static int diveRayEndTick(int attack) {
        return attack == ATTACK_DIVE_RAY
                ? DIVE_RAY_SHOOT_FIRST_SWITCH_TICK + DIVE_RAY_OVERHEAD_DURATION_TICKS
                : DIVE_RAY_OVERHEAD_FIRST_SWITCH_TICK + DIVE_RAY_SHOOT_DURATION_TICKS;
    }

    private void playClawImpact(
            ApollyonEntity owner, Vec3 direction, boolean first) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 home = owner.arenaHomePosition();
        HadesClawSlashEntity.spawnRectangle(
                this, home, direction, (float) CLAW_WARNING_WIDTH,
                (float) CLAW_WARNING_LENGTH, first);
        StarFantasyVfx.areaShake(
                this, home, 64.0D,
                ROUNDHOUSE_HIT_SHAKE_TICKS, ROUNDHOUSE_HIT_SHAKE_INTENSITY);
        level.m_5594_(null, BlockPos.m_274561_(
                        home.f_82479_, home.f_82480_, home.f_82481_),
                ApollyonSoundRegistry.ATTACK_HADES.get(),
                SoundSource.HOSTILE, 4.0F, 1.0F);
    }

    /** Advances one visual burst per tick along the claw sweep. */
    private void playClawSweepExplosionStep(
            Vec3 home, Vec3 direction, int index) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 start = home.m_82546_(direction.m_82490_(CLAW_WARNING_LENGTH * 0.5D));
        Vec3 center = start.m_82549_(
                direction.m_82490_(CLAW_BLAST_DISTANCES[index]));
        ApollyonPageantController.playPyroclastExplosion(
                level, center.m_82520_(0.0D, 0.08D, 0.0D), CLAW_BLAST_VISUAL_SIZE);
    }

    private Vec3 roundhouseCenter(ApollyonEntity owner) {
        Vec3 towardHome = this.horizontalDirection(
                this.fixedPosition, owner.arenaHomePosition());
        return this.fixedPosition.m_82549_(towardHome.m_82490_(10.0D))
                .m_82520_(0.0D, 0.06D, 0.0D);
    }

    private Vec3 clawDirection(ApollyonEntity owner, boolean first) {
        Vec3 towardHome = this.horizontalDirection(
                this.fixedPosition, owner.arenaHomePosition());
        return rotateY(towardHome, first ? -45.0D : 45.0D);
    }

    private void damageAnnulus(
            ApollyonEntity owner, Vec3 center, double innerRadius, double outerRadius,
            double knockbackSpeed) {
        double innerRadiusSqr = innerRadius * innerRadius;
        double outerRadiusSqr = outerRadius * outerRadius;
        for (LivingEntity player : ApollyonPageantController.arenaLivingTargets(owner)) {
            double dx = player.m_20185_() - center.f_82479_;
            double dz = player.m_20189_() - center.f_82481_;
            double distanceSqr = dx * dx + dz * dz;
            if (distanceSqr > innerRadiusSqr && distanceSqr <= outerRadiusSqr) {
                player.f_19802_ = 0;
                if (player.m_6469_(this.frontFacingMobAttack(owner, player),
                        (float) (ApollyonConfig.hardMode() ? 100.0D : COOPERATIVE_DAMAGE))) {
                    this.applyKnockback(player,
                            this.horizontalDirection(center, player.m_20182_()), knockbackSpeed);
                }
            }
        }
    }

    /**
     * Hades's telegraphed arena attacks use the player's facing for directional
     * guard checks rather than Hades's world position. Attacker attribution and
     * the original damage type are preserved.
     */
    private DamageSource frontFacingMobAttack(
            ApollyonEntity owner, LivingEntity player) {
        DamageSource base = owner.m_9236_().m_269111_().m_269333_(owner);
        return this.frontFacingDamageSource(player, base);
    }

    private DamageSource frontFacingDamageSource(
            LivingEntity player, DamageSource base) {
        if (!(player instanceof ServerPlayer)) {
            return base;
        }
        float yawRadians = player.m_146908_() * ((float) Math.PI / 180.0F);
        Vec3 forward = new Vec3(
                -Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 sourcePosition = player.m_20182_()
                .m_82549_(forward.m_82490_(FRONT_DAMAGE_SOURCE_DISTANCE))
                .m_82520_(0.0D, player.m_20206_() * 0.5D, 0.0D);
        return new FrontFacingDamageSource(base, sourcePosition);
    }

    private static final class FrontFacingDamageSource extends DamageSource {
        private final Vec3 sourcePosition;

        private FrontFacingDamageSource(
                DamageSource base, Vec3 sourcePosition) {
            super(base.m_269150_(), base.m_7639_(), base.m_7640_());
            this.sourcePosition = sourcePosition;
        }

        @Override
        public Vec3 m_7270_() {
            return this.sourcePosition;
        }

        @Override
        public Vec3 m_269181_() {
            return this.sourcePosition;
        }
    }

    private void damageRectangle(
            ApollyonEntity owner, Vec3 center, Vec3 forward,
            double width, double length, double knockbackSpeed) {
        Vec3 right = new Vec3(-forward.f_82481_, 0.0D, forward.f_82479_);
        DamageSource base = owner.m_9236_().m_269111_().m_269333_(owner);
        for (LivingEntity player : ApollyonPageantController.arenaLivingTargets(owner)) {
            Vec3 delta = player.m_20182_().m_82546_(center);
            double along = delta.f_82479_ * forward.f_82479_
                    + delta.f_82481_ * forward.f_82481_;
            double across = delta.f_82479_ * right.f_82479_
                    + delta.f_82481_ * right.f_82481_;
            if (Math.abs(along) <= length * 0.5D
                    && Math.abs(across) <= width * 0.5D) {
                player.f_19802_ = 0;
                if (player.m_6469_(this.frontFacingDamageSource(player, base),
                        (float) (ApollyonConfig.hardMode() ? 100.0D : COOPERATIVE_DAMAGE))) {
                    this.applyKnockback(player, forward, knockbackSpeed);
                }
            }
        }
    }

    private void applyKnockback(LivingEntity player, Vec3 direction, double speed) {
        if (ApollyonConfig.hardMode()) {
            speed = 12.0D;
        }
        Vec3 horizontal = direction.f_82479_ * direction.f_82479_
                + direction.f_82481_ * direction.f_82481_ < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D) : direction.m_82541_();
        player.m_20256_(new Vec3(
                horizontal.f_82479_ * speed, 0.4D, horizontal.f_82481_ * speed));
        player.f_19789_ = 0.0F;
        player.f_19864_ = true;
        player.f_19812_ = true;
    }

    private Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        Vec3 direction = new Vec3(
                to.f_82479_ - from.f_82479_, 0.0D, to.f_82481_ - from.f_82481_);
        return direction.m_82553_() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D) : direction.m_82541_();
    }

    private static Vec3 rotateY(Vec3 direction, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                direction.f_82479_ * cos - direction.f_82481_ * sin,
                0.0D,
                direction.f_82479_ * sin + direction.f_82481_ * cos).m_82541_();
    }

    private static Vec3 horizontalRight(Vec3 direction) {
        return new Vec3(-direction.f_82481_, 0.0D, direction.f_82479_);
    }

    private static float yawFromDirection(Vec3 direction) {
        return (float) Math.toDegrees(Math.atan2(
                -direction.f_82479_, direction.f_82481_));
    }

    private void finishCooperativeAttack() {
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK, ATTACK_NONE);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK_AGE, 0);
        this.applyControlledPosition();
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6094_() {
        return false;
    }

    @Override
    public boolean m_5829_() {
        return false;
    }

    @Override
    public boolean m_6063_() {
        return false;
    }

    @Override
    public boolean m_6087_() {
        return false;
    }

    @Override
    public boolean m_7337_(Entity other) {
        return false;
    }

    @Override
    public void m_7334_(Entity other) {
    }

    @Override
    public void m_5997_(double x, double y, double z) {
    }

    @Override
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
    }

    @Override
    public AABB m_6921_() {
        return super.m_6921_().m_82377_(32.0D, 48.0D, 32.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.fixedPosition = new Vec3(
                tag.m_128459_(FIXED_X_TAG), tag.m_128459_(FIXED_Y_TAG),
                tag.m_128459_(FIXED_Z_TAG));
        this.f_19804_.m_135381_(CHARGING, tag.m_128471_(CHARGING_TAG));
        this.f_19804_.m_135381_(SMASHING, tag.m_128471_(SMASHING_TAG));
        this.f_19804_.m_135381_(SMASH_AGE, Math.max(0, tag.m_128451_(SMASH_AGE_TAG)));
        this.f_19804_.m_135381_(RISE_AGE,
                tag.m_128441_(RISE_AGE_TAG)
                        ? Math.max(0, Math.min(RISE_TICKS, tag.m_128451_(RISE_AGE_TAG)))
                        : RISE_TICKS);
        this.f_19804_.m_135381_(DYING, tag.m_128471_(DYING_TAG));
        this.f_19804_.m_135381_(DEATH_TICKS,
                Math.max(0, Math.min(DEATH_ANIMATION_TICKS,
                        tag.m_128451_(DEATH_TICKS_TAG))));
        int savedAttack = tag.m_128451_(COOPERATIVE_ATTACK_TAG);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK,
                savedAttack == ATTACK_ROUNDHOUSE
                        || savedAttack == ATTACK_CLAW_COMBO
                        || isDiveRayAttack(savedAttack)
                        ? savedAttack : ATTACK_NONE);
        this.f_19804_.m_135381_(COOPERATIVE_ATTACK_AGE,
                Math.max(0, tag.m_128451_(COOPERATIVE_ATTACK_AGE_TAG)));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128347_(FIXED_X_TAG, this.fixedPosition.f_82479_);
        tag.m_128347_(FIXED_Y_TAG, this.fixedPosition.f_82480_);
        tag.m_128347_(FIXED_Z_TAG, this.fixedPosition.f_82481_);
        tag.m_128379_(CHARGING_TAG, this.f_19804_.m_135370_(CHARGING));
        tag.m_128379_(SMASHING_TAG, this.f_19804_.m_135370_(SMASHING));
        tag.m_128405_(SMASH_AGE_TAG, this.smashAge());
        tag.m_128405_(RISE_AGE_TAG, this.riseAge());
        tag.m_128379_(DYING_TAG, this.isPlayingDeathAnimation());
        tag.m_128405_(DEATH_TICKS_TAG, this.getDeathAnimationTicks());
        tag.m_128405_(COOPERATIVE_ATTACK_TAG, this.cooperativeAttackType());
        tag.m_128405_(COOPERATIVE_ATTACK_AGE_TAG, this.cooperativeAttackAge());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<HadesEntity> state) {
        RawAnimation animation;
        if (this.isPlayingDeathAnimation()) {
            animation = DEATH;
        } else if (this.f_19804_.m_135370_(SMASHING)) {
            animation = SMASH;
        } else if (this.isCooperativeAttacking()) {
            animation = this.cooperativeAnimation();
        } else {
            animation = this.isCharging() ? WALK : IDLE;
        }
        state.getController().setAnimation(animation);
        return PlayState.CONTINUE;
    }

    private RawAnimation cooperativeAnimation() {
        if (this.cooperativeAttackType() == ATTACK_ROUNDHOUSE) {
            return ROUNDHOUSE;
        }
        if (this.cooperativeAttackType() == ATTACK_CLAW_COMBO) {
            int age = this.cooperativeAttackAge();
            if (age < CLAW_SECOND_START_TICK) {
                return CLAW_ONE;
            }
            return CLAW_TWO;
        }
        if (isDiveRayAttack(this.cooperativeAttackType())) {
            return this.isDiveRayShootAction() ? SHOOT : OVERHEAD_SWIPE;
        }
        return this.isCharging() ? WALK : IDLE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
