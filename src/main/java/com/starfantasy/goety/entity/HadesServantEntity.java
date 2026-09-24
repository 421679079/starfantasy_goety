package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.starfantasy.goety.combat.ApollyonDeathEffects;
import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.combat.HadesJudgmentSouls;
import com.starfantasy.goety.config.ServantConfig;
import com.starfantasy.goety.magic.focus.BattleFocusCombat;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.goety.item.FadedHaloItem;
import com.starfantasy.goety.servant.ServantOwnershipData;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.starfantasy.goety.entity.riding.HadesRiderSeat;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Permanent, commandable Hades. Combat runs on the server, independently of the boss arena. */
public final class HadesServantEntity extends Summoned implements GeoEntity, PlayerRideable {
    public static final int ROUNDHOUSE = 1, CLAW_COMBO = 2, DIVE_RAY = 3, INFERNAL_JUDGMENT = 4;
    public static final int JUDGMENT_CLEAVE_TICK = 26;
    public static final int JUDGMENT_BURST_TICK = JUDGMENT_CLEAVE_TICK + ApollyonCleaveEffectEntity.BURST_START_TICK;
    public static final int JUDGMENT_END_TICK = JUDGMENT_BURST_TICK + 40;
    private static final int JUDGMENT_HITS = 20, JUDGMENT_COOLDOWN = 600;
    private static final double JUDGMENT_RADIUS = 15, JUDGMENT_FORWARD_OFFSET = 5;
    public static final double ATTACK_RANGE = 8.0;
    private static final double ROUNDHOUSE_MULTIPLIER = 4.8, CLAW_MULTIPLIER = 5,
            DIVE_MULTIPLIER = 5, LASER_MULTIPLIER = 3;
    private static final double LASER_LENGTH = 40, DIVE_LENGTH = 20, DIVE_WIDTH = 15,
            DIVE_SIDE_OFFSET = 3.75;
    // Keep the existing movement attribute as the baseline for movement-speed modifiers.
    public static final double MOVEMENT_SPEED = Math.sqrt(0.13);
    public static final double FLYING_SPEED = 5.612 / 20.0;
    public static final double RIDING_SPEED_MULTIPLIER = 4.0;
    private static final EntityDataAccessor<Integer> ATTACK =
            SynchedEntityData.m_135353_(HadesServantEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.m_135353_(HadesServantEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DEATH_AGE =
            SynchedEntityData.m_135353_(HadesServantEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> RIDER_MOVING =
            SynchedEntityData.m_135353_(HadesServantEntity.class, EntityDataSerializers.f_135035_);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SPIN = RawAnimation.begin().thenPlay("roundhouse").thenLoop("idle");
    private static final RawAnimation CLAW_ONE = RawAnimation.begin().thenPlay("claw1").thenLoop("idle");
    private static final RawAnimation CLAW_TWO = RawAnimation.begin().thenPlay("claw2").thenLoop("idle");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot").thenLoop("idle");
    private static final RawAnimation SWIPE = RawAnimation.begin().thenPlay("overhead_swipe").thenLoop("idle");
    private static final RawAnimation SMASH = RawAnimation.begin().thenPlayAndHold("smash");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Vec3 attackOrigin = Vec3.f_82478_, attackForward = new Vec3(0, 0, 1);
    private float attackYaw;
    private double cooldownWork;
    private int invulnerabilityTicks;
    private int judgmentSouls, judgmentCooldown;
    private Vec3 judgmentCenter = Vec3.f_82478_;
    private boolean recallDropped;
    private float riderForward, riderStrafe, riderVertical, riderYaw;
    private long lastRiderInput = Long.MIN_VALUE;
    private UUID inputRider;

    public HadesServantEntity(EntityType<? extends HadesServantEntity> type, Level level) {
        super(type, level);
        this.f_21342_ = new MoveControl(this);
        this.f_19850_ = true; // Prevent blocks being placed through the body; entity pushing stays disabled.
        this.f_19794_ = false;
        this.m_20242_(false);
        this.m_21441_(BlockPathTypes.WATER, 0);
        this.m_21441_(BlockPathTypes.LAVA, 0);
        this.m_21441_(BlockPathTypes.DAMAGE_FIRE, 0);
        this.m_21441_(BlockPathTypes.DANGER_FIRE, 0);
        this.setConfigurableAttributes();
        this.m_21153_(this.m_21233_());
        this.setHasLifespan(false);
        this.m_21530_();
    }

    @Override public int getSummonLimit(LivingEntity owner) {
        return ServantOwnershipData.summonLimit(this, owner);
    }

    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        ServantOwnershipData.track(this);
        HadesJudgmentSouls.track(this);
    }

    @Override public void setOwnerId(UUID owner) {
        HadesJudgmentSouls.untrack(this, this.getOwnerId());
        if (!java.util.Objects.equals(owner, this.getOwnerId())) this.judgmentSouls = 0;
        super.setOwnerId(owner);
        ServantOwnershipData.track(this);
        HadesJudgmentSouls.track(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, 600)
                .m_22268_(Attributes.f_22284_, 12).m_22268_(Attributes.f_22285_, 12)
                .m_22268_(Attributes.f_22279_, MOVEMENT_SPEED)
                .m_22268_(Attributes.f_22280_, FLYING_SPEED)
                .m_22268_(Attributes.f_22278_, 1)
                .m_22268_(Attributes.f_22277_, 48)
                .m_22268_(Attributes.f_22281_, 10).m_22268_(Attributes.f_22283_, 1);
    }

    @Override public void setConfigurableAttributes() {
        this.m_21051_(Attributes.f_22276_).m_22100_(ServantConfig.HEALTH.get());
        this.m_21051_(Attributes.f_22284_).m_22100_(ServantConfig.ARMOR.get());
        this.m_21051_(Attributes.f_22285_).m_22100_(ServantConfig.ARMOR.get());
        this.m_21051_(Attributes.f_22281_).m_22100_(ServantConfig.ATTACK_DAMAGE.get());
        this.m_21051_(Attributes.f_22278_).m_22100_(1);
        if (this.m_21223_() > this.m_21233_()) this.m_21153_(this.m_21233_());
    }

    @Override protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(ATTACK, 0);
        this.f_19804_.m_135372_(AGE, 0);
        this.f_19804_.m_135372_(DEATH_AGE, 0);
        this.f_19804_.m_135372_(RIDER_MOVING, false);
    }

    @Override protected void m_8099_() {
        super.m_8099_();
        this.f_21345_.m_25352_(2, new CombatGoal());
        this.f_21345_.m_25352_(8, new Summoned.WanderGoal<>(this, 1.0));
    }

    @Override protected PathNavigation m_6037_(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        return navigation;
    }

    @Override public MobType m_6336_() { return MobType.f_21641_; }
    @Override public boolean m_20068_() { return this.m_6688_() != null || !this.m_6084_(); }
    @Override public boolean m_6040_() { return true; }
    @Override public boolean canDrownInFluidType(FluidType type) { return false; }
    @Override public boolean m_6063_() { return false; }
    @Override public boolean isPushedByFluid(FluidType type) { return false; }
    @Override public void m_7311_(int ticks) { super.m_7311_(Math.min(0, ticks)); }

    @Override public void tryKill(Player player) {
        if (this.getKillChance() <= 0) {
            this.warnKill(player);
        } else {
            super.tryKill(player);
        }
    }

    @Override public boolean m_142535_(float fallDistance, float damageMultiplier, DamageSource source) {
        // The vanilla fall handler forwards the mount's fall distance to its passengers before hurt().
        this.f_19789_ = 0;
        return false;
    }

    // Keep the hit box pickable for melee/projectiles, but remove physical entity collisions.
    @Override public boolean m_6094_() { return false; }
    @Override public boolean m_7337_(Entity other) { return false; }
    @Override public boolean m_5829_() { return false; }
    @Override public void m_7334_(Entity other) { }
    @Override protected void m_7324_(Entity other) { }
    @Override protected void m_6138_() { }
    @Override public void m_147240_(double strength, double x, double z) { }

    @Override public InteractionResult m_6071_(Player player, InteractionHand hand) {
        // Goety's generic dismount handler only sees controlling passengers, not autonomous servant riders.
        ApollyonServantEntity mountedServant = this.mountedApollyonServant();
        if (player.m_6047_() && mountedServant != null
                && player.m_20148_().equals(this.getOwnerId())) {
            if (!this.m_9236_().f_46443_) mountedServant.m_8127_();
            return InteractionResult.m_19078_(this.m_9236_().f_46443_);
        }
        // Leave other sneaking, equipment, revival and native servant commands to Goety.
        if (hand == InteractionHand.MAIN_HAND && player.m_21120_(hand).m_41619_()
                && !player.m_6144_() && this.m_6084_() && !this.m_20160_()
                && player.m_20148_().equals(this.getOwnerId()) && !player.m_20159_()) {
            if (!this.m_9236_().f_46443_) {
                Vec3 seat = this.riderPosition(0);
                if (!this.m_9236_().m_45756_(player, player.m_20191_().m_82383_(seat.m_82546_(player.m_20182_())))) {
                    return InteractionResult.FAIL;
                }
                this.clearRiderInput();
                this.m_21573_().m_26573_();
                player.m_20329_(this);
                this.m_7332_(player);
            }
            return InteractionResult.m_19078_(this.m_9236_().f_46443_);
        }
        return super.m_6071_(player, hand);
    }

    @Override protected boolean m_7310_(Entity passenger) {
        if (this.m_20160_() || this.getOwnerId() == null) return false;
        return passenger instanceof Player && passenger.m_20148_().equals(this.getOwnerId())
                || passenger instanceof ApollyonServantEntity servant && servant.m_6084_()
                    && this.getOwnerId().equals(servant.getOwnerId());
    }

    public ApollyonServantEntity mountedApollyonServant() {
        Entity passenger = this.m_146895_();
        return passenger instanceof ApollyonServantEntity servant && servant.m_20202_() == this
                && this.getOwnerId() != null && this.getOwnerId().equals(servant.getOwnerId())
                ? servant : null;
    }

    public boolean hasSeatPassenger() {
        return this.m_6688_() != null || this.mountedApollyonServant() != null;
    }

    public boolean protectedByRiderMonolith() {
        ApollyonServantEntity servant = mountedApollyonServant();
        return servant != null && servant.hasMonolithProtection();
    }

    public boolean riderMonolithVisible() {
        ApollyonServantEntity servant = mountedApollyonServant();
        return servant != null && servant.monolithVisible();
    }

    @Override public LivingEntity m_6688_() {
        Entity first = this.m_146895_();
        return first instanceof Player player && player.m_20148_().equals(this.getOwnerId()) ? player : null;
    }

    // The server simulates movement. Clients send input, never vehicle coordinates.
    @Override public boolean m_6109_() { return !this.m_9236_().f_46443_; }

    public void acceptRiderInput(Player player, float forward, float strafe, float vertical, float yaw) {
        if (this.m_9236_().f_46443_ || this.m_6688_() != player || player.m_20202_() != this
                || !this.m_6084_() || !Float.isFinite(forward) || !Float.isFinite(strafe)
                || !Float.isFinite(vertical) || !Float.isFinite(yaw)) return;
        this.riderForward = Math.max(-1, Math.min(1, forward));
        this.riderStrafe = Math.max(-1, Math.min(1, strafe));
        this.riderVertical = Math.max(-1, Math.min(1, vertical));
        this.riderYaw = yaw % 360;
        this.lastRiderInput = this.m_9236_().m_46467_();
        this.inputRider = player.m_20148_();
    }

    private void clearRiderInput() {
        this.riderForward = this.riderStrafe = this.riderVertical = 0;
        this.inputRider = null;
        this.lastRiderInput = Long.MIN_VALUE;
        this.m_20256_(Vec3.f_82478_);
        this.f_19804_.m_135381_(RIDER_MOVING, false);
    }

    public boolean isRiderMoving() { return this.f_19804_.m_135370_(RIDER_MOVING); }

    public boolean useWalkAnimation(boolean groundMoving) {
        return this.hasSeatPassenger() ? this.isRiderMoving() : groundMoving;
    }

    @Override public void m_7023_(Vec3 input) {
        LivingEntity rider = this.m_6688_();
        if (rider == null) {
            if (this.attackType() != 0) {
                // Keep attacks stationary horizontally, while allowing an unmounted servant to land.
                this.m_7910_(0);
                this.m_21567_(0);
                this.m_20256_(new Vec3(0, this.m_20184_().f_82480_, 0));
                input = Vec3.f_82478_;
            }
            super.m_7023_(input);
            return;
        }
        if (this.m_9236_().f_46443_) return;
        this.m_21573_().m_26573_();
        boolean fresh = rider.m_20148_().equals(this.inputRider)
                && this.m_9236_().m_46467_() - this.lastRiderInput <= 10;
        Vec3 velocity = Vec3.f_82478_;
        if (fresh && this.attackType() == 0 && this.m_6084_()) {
            double yaw = Math.toRadians(this.riderYaw);
            velocity = new Vec3(this.riderStrafe * Math.cos(yaw) - this.riderForward * Math.sin(yaw),
                    this.riderVertical, this.riderForward * Math.cos(yaw) + this.riderStrafe * Math.sin(yaw));
            if (velocity.m_82556_() > 1) velocity = velocity.m_82541_();
            velocity = velocity.m_82490_(this.m_21133_(Attributes.f_22280_)
                    * this.m_21133_(Attributes.f_22279_) / MOVEMENT_SPEED * RIDING_SPEED_MULTIPLIER);
            this.m_146922_(this.riderYaw);
            this.m_5616_(this.riderYaw);
            this.m_5618_(this.riderYaw);
        }
        this.m_20256_(velocity);
        this.f_19789_ = 0;
        this.m_6478_(MoverType.SELF, velocity);
        this.f_19804_.m_135381_(RIDER_MOVING, velocity.m_82556_() > 1.0E-6);
        this.m_267651_(false);
    }

    @Override public void m_6478_(MoverType type, Vec3 movement) {
        this.f_19794_ = false;
        LivingEntity rider = this.m_6688_();
        if (rider != null) {
            // Also stop under ceilings that the rider reaches before the 10-block body does.
            movement = Entity.m_198894_(rider, movement, rider.m_20191_(), this.m_9236_(), java.util.List.of());
        }
        super.m_6478_(type, movement);
    }

    public Vec3 riderPosition(float partialTick) {
        return HadesRiderSeat.position(this, partialTick);
    }

    @Override protected void m_19956_(Entity passenger, Entity.MoveFunction move) {
        if (!this.m_20363_(passenger)) return;
        Vec3 seat = this.riderPosition(0).m_82520_(0, passenger.m_6049_(), 0);
        // Animation can swing the head sideways; clip its passenger against nearby blocks too.
        Vec3 delta = Entity.m_198894_(passenger, seat.m_82546_(passenger.m_20182_()),
                passenger.m_20191_(), this.m_9236_(), java.util.List.of());
        Vec3 safe = passenger.m_20182_().m_82549_(delta);
        move.m_20372_(passenger, safe.f_82479_, safe.f_82480_, safe.f_82481_);
        passenger.f_19789_ = 0;
    }

    @Override public Vec3 m_7688_(LivingEntity passenger) {
        // Dismount alongside the current animated seat, rather than at the entity's feet.
        Vec3 seat = passenger.m_20182_();
        for (Vec3 offset : new Vec3[]{new Vec3(2, 0, 0), new Vec3(-2, 0, 0),
                new Vec3(0, 0, 2), new Vec3(0, 0, -2), Vec3.f_82478_}) {
            Vec3 point = seat.m_82549_(offset);
            if (this.m_9236_().m_45756_(passenger,
                    passenger.m_20191_().m_82383_(point.m_82546_(passenger.m_20182_())))) return point;
        }
        return seat;
    }

    @Override public boolean m_7301_(MobEffectInstance effect) {
        return effect.m_19544_().m_19483_() != MobEffectCategory.HARMFUL && super.m_7301_(effect);
    }

    @Override public boolean m_6673_(DamageSource source) {
        return isEnvironmentalDamage(source) || super.m_6673_(source);
    }

    private static boolean isEnvironmentalDamage(DamageSource source) {
        // Match only mundane environmental sources, never broad fire/freeze spell tags.
        return source.m_276093_(DamageTypes.f_268612_) // Suffocation.
                || source.m_276093_(DamageTypes.f_268441_) // Starvation.
                || source.m_276093_(DamageTypes.f_268671_) // Falling.
                || source.m_276093_(DamageTypes.f_268722_) // Drowning.
                || source.m_276093_(DamageTypes.f_268613_) // Entity cramming.
                || source.m_276093_(DamageTypes.f_268631_) // Standing in fire.
                || source.m_276093_(DamageTypes.f_268468_) // Burning.
                || source.m_276093_(DamageTypes.f_268546_) // Lava contact.
                || source.m_276093_(DamageTypes.f_268434_); // Magma block contact.
    }

    private void clearHarmfulEffects() {
        for (MobEffectInstance effect : java.util.List.copyOf(this.m_21220_())) {
            if (effect.m_19544_().m_19483_() == MobEffectCategory.HARMFUL) {
                this.m_21195_(effect.m_19544_());
            }
        }
    }

    @Override public void m_8119_() {
        this.f_19794_ = false;
        if (!this.m_9236_().f_46443_ && this.m_6688_() == null && this.inputRider != null) this.clearRiderInput();
        this.m_20095_();
        if (!this.m_9236_().f_46443_) this.clearHarmfulEffects();
        // Advance before goal evaluation: the next attack is available exactly 40 / speed ticks later.
        if (!this.m_9236_().f_46443_ && this.m_6084_()
                && this.attackType() == 0 && this.cooldownWork > 0) {
            this.cooldownWork = Math.max(0, this.cooldownWork
                    - Math.max(0, this.m_21133_(Attributes.f_22283_)));
        }
        if (!this.m_9236_().f_46443_) {
            if (this.judgmentCooldown > 0) --this.judgmentCooldown;
            if (!this.hasCombatTarget()) this.judgmentSouls = 0;
        }
        super.m_8119_();
        if (this.m_9236_().f_46443_ || !this.m_6084_()) return;
        if (this.mountedApollyonServant() != null) {
            double horizontalMovement = this.m_20182_().m_82546_(new Vec3(this.f_19854_, this.f_19855_, this.f_19856_)).m_82556_();
            this.f_19804_.m_135381_(RIDER_MOVING, horizontalMovement > 1.0E-6);
        } else if (this.m_6688_() == null && this.isRiderMoving()) {
            this.f_19804_.m_135381_(RIDER_MOVING, false);
        }
        if (this.invulnerabilityTicks > 0) --this.invulnerabilityTicks;
        if (!this.hasCombatTarget()) this.judgmentSouls = 0;
        if (this.f_19797_ % 20 == 0) {
            this.m_5634_(ServantConfig.REGENERATION.get().floatValue());
        }
        if (this.attackType() != 0) {
            this.m_21573_().m_26573_();
            this.lockFacing();
            int age = this.attackAge() + 1;
            this.f_19804_.m_135381_(AGE, age);
            this.tickAttack(age);
        }
    }

    @Override public boolean m_6469_(DamageSource source, float amount) {
        if (this.protectedByRiderMonolith()
                && !com.starfantasy.goety.combat.ApollyonDamageRules.commandKill(source, amount)) {
            com.starfantasy.goety.combat.ApollyonServantMonoliths.redirectAttacker(this, source);
            return false;
        }
        if (this.m_6673_(source)) return false;
        boolean bypass = source.m_269533_(DamageTypeTags.f_268738_);
        if (!bypass && this.invulnerabilityTicks > 0) return false;
        // Replace vanilla's 20-tick escalating-damage window with an absolute custom window.
        int vanillaTime = this.f_19802_;
        this.f_19802_ = 0;
        float oldHealth = this.m_21223_();
        boolean hurt = super.m_6469_(source, amount);
        if (hurt) {
            this.f_19802_ = 0;
            if (!bypass && this.m_21223_() < oldHealth) {
                this.invulnerabilityTicks = ServantConfig.INVULNERABILITY_TICKS.get();
            }
        } else {
            this.f_19802_ = vanillaTime;
        }
        return hurt;
    }

    @Override protected void m_6475_(DamageSource source, float amount) {
        if (this.protectedByRiderMonolith()
                && !com.starfantasy.goety.combat.ApollyonDamageRules.commandKill(source, amount)) return;
        super.m_6475_(source, amount);
    }

    public int attackType() { return this.f_19804_.m_135370_(ATTACK); }
    public int attackAge() { return this.f_19804_.m_135370_(AGE); }
    public int deathAge() { return this.f_19804_.m_135370_(DEATH_AGE); }
    private boolean hasCombatTarget() {
        return this.m_6084_() && this.m_5448_() != null && this.canHarm(this.m_5448_());
    }

    @Override public void m_6710_(LivingEntity target) {
        super.m_6710_(target);
        if (!this.m_9236_().f_46443_ && !this.hasCombatTarget()) this.judgmentSouls = 0;
    }

    public void recordJudgmentSouls(int amount) {
        if (this.m_9236_().f_46443_) return;
        if (!this.hasCombatTarget()) {
            this.judgmentSouls = 0;
            return;
        }
        if (amount > 0) this.judgmentSouls = (int) Math.min(
                ServantConfig.JUDGMENT_SOUL_COST.get(), (long) this.judgmentSouls + amount);
    }

    private boolean judgmentReady() {
        return this.hasCombatTarget() && this.judgmentCooldown <= 0
                && this.judgmentSouls >= ServantConfig.JUDGMENT_SOUL_COST.get();
    }
    public boolean isShootAction() {
        return this.attackType() == DIVE_RAY && this.attackAge() < 60;
    }
    public int actionAge() {
        return this.attackAge() < 60 ? this.attackAge() : this.attackAge() - 60;
    }
    public int shootAfterglowAge() {
        return this.attackType() == DIVE_RAY && this.isShootAction()
                ? this.actionAge() - 30 : -1;
    }
    public boolean isShootCharging() {
        return this.attackType() == DIVE_RAY && this.isShootAction() && this.actionAge() < 30;
    }

    public boolean canHarm(LivingEntity target) {
        if (target == this || !target.m_6084_() || target.m_5833_()
                || target instanceof net.minecraft.world.entity.player.Player player
                && (player.m_7500_() || player.m_5833_())) return false;
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            if (ownerId.equals(target.m_20148_())) return false;
            if (target instanceof Owned owned && ownerId.equals(owned.getOwnerId())) return false;
            if (target instanceof OwnableEntity ownable && ownerId.equals(ownable.m_21805_())) return false;
        }
        return !this.m_7307_(target) && !target.m_7307_(this);
    }

    /** Snapshot aim once; later target movement must not rotate already-started attacks. */
    public void startAttack(int type) {
        LivingEntity target = this.m_5448_();
        this.startAttack(type, target == null ? this.m_20154_() : target.m_20182_().m_82546_(this.m_20182_()));
    }

    public boolean tryRiderAttack(Player player, float yaw) {
        if (this.m_9236_().f_46443_ || !Float.isFinite(yaw) || !player.m_6084_()
                || player.m_20202_() != this || this.m_6688_() != player
                || !this.m_6084_() || this.attackType() != 0 || this.cooldownWork > 0) return false;
        double angle = Math.toRadians(yaw % 360);
        this.startAttack(1 + this.m_217043_().m_188503_(3), new Vec3(-Math.sin(angle), 0, Math.cos(angle)));
        return this.attackType() != 0;
    }

    private void startAttack(int type, Vec3 aim) {
        if (this.m_9236_().f_46443_ || !this.m_6084_() || this.attackType() != 0
                || this.cooldownWork > 0 || type < ROUNDHOUSE || type > INFERNAL_JUDGMENT) return;
        if (this.judgmentReady()) type = INFERNAL_JUDGMENT;
        else if (type == INFERNAL_JUDGMENT) return;
        this.attackOrigin = this.m_20182_();
        this.attackForward = new Vec3(aim.f_82479_, 0, aim.f_82481_).m_82541_();
        if (this.attackForward.m_82553_() < 1.0E-6) this.attackForward = new Vec3(0, 0, 1);
        this.attackYaw = (float) Math.toDegrees(Math.atan2(-this.attackForward.f_82479_, this.attackForward.f_82481_));
        this.f_19804_.m_135381_(ATTACK, type);
        this.f_19804_.m_135381_(AGE, 0);
        this.m_21573_().m_26573_();
        this.lockFacing();
        if (type == DIVE_RAY) this.sound(ApollyonSoundRegistry.CAST_HADES.get(), 2);
        if (type == INFERNAL_JUDGMENT) {
            this.judgmentSouls = 0;
            this.judgmentCooldown = JUDGMENT_COOLDOWN;
            // The boss is ten blocks from its cleave at impact; our model is half size.
            this.judgmentCenter = this.attackOrigin.m_82549_(this.attackForward.m_82490_(JUDGMENT_FORWARD_OFFSET));
        }
    }

    private void lockFacing() {
        this.m_146922_(this.attackYaw);
        this.m_5616_(this.attackYaw);
        this.m_5618_(this.attackYaw);
        this.m_20256_(new Vec3(0, this.m_6688_() == null ? this.m_20184_().f_82480_ : 0, 0));
    }

    private void tickAttack(int age) {
        int type = this.attackType();
        if (type == ROUNDHOUSE) {
            Vec3 center = this.attackOrigin.m_82549_(this.attackForward.m_82490_(5));
            if (age == 26 || age == 38) HadesClawSlashEntity.spawn(this, center, age == 26 ? 8 : 10);
            if (age == 28 || age == 40) {
                this.sound(ApollyonSoundRegistry.ATTACK_HADES.get(), 3);
                this.damageDisk(center, 15);
            }
        } else if (type == CLAW_COMBO) {
            Vec3 center = this.attackOrigin.m_82549_(this.attackForward.m_82490_(10));
            for (int hit : new int[]{22, 42}) {
                Vec3 direction = rotate(this.attackForward, hit == 22 ? -45 : 45);
                if (age == hit) {
                    // Double the complete claw visual, while keeping the damage rectangle 20 blocks long.
                    HadesClawSlashEntity slash = HadesClawSlashEntity.spawnRectangle(
                            this, center, direction, 15, 40, hit == 22);
                    if (slash != null) slash.setVisualScale(1.0F);
                    this.damageRectangle(center, direction, 15, 20,
                            this.m_269291_().m_269333_(this), this.attackDamage(CLAW_MULTIPLIER));
                    this.sound(ApollyonSoundRegistry.ATTACK_HADES.get(), 3);
                }
                int index = age - hit;
                if (index >= 0 && index < 7) this.blast(center.m_82549_(direction.m_82490_(-10 + 2.5 * (index + 1))), 6);
            }
        } else if (type == INFERNAL_JUDGMENT) {
            this.tickJudgment(age);
        } else {
            boolean shoot = this.isShootAction();
            int actionAge = this.actionAge();
            double length = shoot ? LASER_LENGTH : DIVE_LENGTH;
            Vec3 center = this.attackOrigin.m_82549_(this.attackForward.m_82490_(length / 2));
            Vec3 right = new Vec3(-this.attackForward.f_82481_, 0, this.attackForward.f_82479_);
            if (actionAge == 30) {
                // Extend damage two blocks behind the origin without moving visuals or the far edge.
                Vec3 damageCenter = center.m_82549_(this.attackForward.m_82490_(-1));
                if (shoot) {
                    HadesDiveRayLaserEntity.spawn(this, this.attackOrigin.m_82520_(0, 2.5, 0),
                            this.attackForward, 6 / 0.7, LASER_LENGTH);
                    this.damageRectangle(damageCenter, this.attackForward, 7.5, LASER_LENGTH + 2,
                            this.m_269291_().m_269104_(this, this), this.attackDamage(LASER_MULTIPLIER));
                } else {
                    for (int side : new int[]{-1, 1}) {
                        Vec3 lane = center.m_82549_(right.m_82490_(side * DIVE_SIDE_OFFSET));
                        HadesClawSlashEntity.spawnRectangle(this, lane, this.attackForward, 7.5F, 20, side < 0);
                    }
                    // One continuous damage area; the two lanes are visual effects only.
                    this.damageRectangle(damageCenter, this.attackForward, DIVE_WIDTH, DIVE_LENGTH + 2,
                            this.m_269291_().m_269333_(this), this.attackDamage(DIVE_MULTIPLIER));
                }
                this.sound(shoot ? ApollyonSoundRegistry.SUMMON_APOSTLE.get()
                        : ApollyonSoundRegistry.ATTACK_HADES.get(), 3);
            }
            int index = actionAge - 30;
            if (!shoot && index >= 0 && index < 7) {
                Vec3 burst = this.attackOrigin.m_82549_(this.attackForward.m_82490_(2.5 * (index + 1)));
                for (int side : new int[]{-1, 1}) this.blast(burst.m_82549_(right.m_82490_(side * DIVE_SIDE_OFFSET)), 4.5F);
            }
        }
        int duration = type == INFERNAL_JUDGMENT ? JUDGMENT_END_TICK : type < DIVE_RAY ? 80 : 120;
        if (age >= duration) {
            this.f_19804_.m_135381_(ATTACK, 0);
            this.f_19804_.m_135381_(AGE, 0);
            this.cooldownWork = 40;
        }
    }

    private double attackDamage(double multiplier) {
        // Resolve the live attribute at impact time, including Strength and other modifiers.
        return this.m_21133_(Attributes.f_22281_) * multiplier;
    }

    private void tickJudgment(int age) {
        if (!(this.m_9236_() instanceof ServerLevel level)) return;
        if (age == JUDGMENT_CLEAVE_TICK) {
            ApollyonCleaveEffectEntity cleave = new ApollyonCleaveEffectEntity(
                    ApollyonEntityRegistry.APOLLYON_CLEAVE_EFFECT.get(), level);
            cleave.configureServant(this);
            Vec3 position = this.judgmentCenter.m_82520_(0, 0.05, 0);
            cleave.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
            level.m_7967_(cleave);
            StarFantasyVfx.stomp(this, this.judgmentCenter, 3, 1);
            StarFantasyVfx.slamShockwave(this, this.judgmentCenter, 20);
            StarFantasyVfx.areaShake(this, this.judgmentCenter, 64, 40, 0.75F);
        }
        if (age == JUDGMENT_BURST_TICK) {
            level.m_5594_(null, net.minecraft.core.BlockPos.m_274561_(
                    this.judgmentCenter.f_82479_, this.judgmentCenter.f_82480_, this.judgmentCenter.f_82481_),
                    ApollyonSoundRegistry.CAST_OBSIDIAN.get(), this.m_5720_(), 3, 1);
            StarFantasyVfx.areaImpactShake(this, this.judgmentCenter, 64, 0, 40, 20, 3);
        }
        if (age >= JUDGMENT_BURST_TICK && age < JUDGMENT_BURST_TICK + JUDGMENT_HITS) {
            BattleFocusCombat.sonicDamage(level, this, this.judgmentCenter, JUDGMENT_RADIUS,
                    (float) this.attackDamage(1), this::canHarm);
        }
    }

    private void damageDisk(Vec3 center, double radius) {
        double damage = this.attackDamage(ROUNDHOUSE_MULTIPLIER);
        for (LivingEntity target : this.targets(center, radius)) {
            double dx = target.m_20185_() - center.f_82479_, dz = target.m_20189_() - center.f_82481_;
            if (dx * dx + dz * dz <= radius * radius) {
                this.hurtTarget(target, this.m_269291_().m_269333_(this), damage);
            }
        }
    }

    private void damageRectangle(Vec3 center, Vec3 forward, double width, double length,
                                 DamageSource source, double damage) {
        for (LivingEntity target : this.targets(center, Math.hypot(width, length) / 2)) {
            Vec3 delta = target.m_20182_().m_82546_(center);
            double along = delta.f_82479_ * forward.f_82479_ + delta.f_82481_ * forward.f_82481_;
            double across = -delta.f_82479_ * forward.f_82481_ + delta.f_82481_ * forward.f_82479_;
            if (Math.abs(along) <= length / 2 && Math.abs(across) <= width / 2) this.hurtTarget(target, source, damage);
        }
    }

    private void hurtTarget(LivingEntity target, DamageSource source, double damage) {
        // Add the target's current maximum-health component once per hit, with the same damage source.
        target.m_6469_(source, (float) (damage + target.m_21233_() * 0.05));
    }

    private java.util.List<LivingEntity> targets(Vec3 center, double radius) {
        return this.m_9236_().m_45976_(LivingEntity.class,
                new AABB(center.f_82479_ - radius, center.f_82480_ - 2, center.f_82481_ - radius,
                        center.f_82479_ + radius, center.f_82480_ + 5, center.f_82481_ + radius))
                .stream().filter(this::canHarm).toList();
    }

    private void blast(Vec3 center, float scale) {
        if (this.m_9236_() instanceof ServerLevel level) ApollyonPageantController.playPyroclastExplosion(level, center, scale, 3);
    }
    private void sound(SoundEvent sound, float volume) { this.m_5496_(sound, volume, 1); }
    private static Vec3 rotate(Vec3 v, double degrees) {
        double r = Math.toRadians(degrees);
        return new Vec3(v.f_82479_ * Math.cos(r) - v.f_82481_ * Math.sin(r), 0,
                v.f_82479_ * Math.sin(r) + v.f_82481_ * Math.cos(r));
    }

    @Override public void m_6667_(DamageSource source) {
        this.judgmentSouls = 0;
        if (!this.m_9236_().f_46443_) {
            this.m_20153_();
            this.clearRiderInput();
            this.attackYaw = this.m_146908_();
            this.f_19804_.m_135381_(ATTACK, 0);
            this.sound(SoundEvents.f_12556_, 1);
        }
        super.m_6667_(source);
    }

    @Override protected void m_6153_() {
        if (this.m_9236_().f_46443_) return;
        this.m_21573_().m_26573_();
        this.lockFacing();
        int age = this.deathAge() + 1;
        this.f_19804_.m_135381_(DEATH_AGE, age);
        if (age >= HadesEntity.DEATH_ANIMATION_TICKS) {
            if (!this.recallDropped && this.getOwnerId() != null) {
                this.recallDropped = true;
                FadedHaloItem.returnToOwner(this);
            }
            ApollyonDeathEffects.explodeHadesServant(this);
            this.m_142687_(Entity.RemovalReason.KILLED);
        }
    }

    @Override public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128347_("HadesCooldown", this.attackType() != 0 ? 40 : this.cooldownWork);
        tag.m_128405_("HadesInvulnerability", this.invulnerabilityTicks);
        tag.m_128405_("HadesJudgmentCooldown", this.judgmentCooldown);
        tag.m_128405_("HadesDeathAge", this.deathAge());
        tag.m_128379_("HadesRecallDropped", this.recallDropped);
    }
    @Override public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.cooldownWork = Math.max(0, tag.m_128459_("HadesCooldown"));
        this.invulnerabilityTicks = Math.max(0, tag.m_128451_("HadesInvulnerability"));
        this.judgmentCooldown = Math.max(0, tag.m_128451_("HadesJudgmentCooldown"));
        this.judgmentSouls = 0;
        this.f_19804_.m_135381_(DEATH_AGE, Math.max(0, tag.m_128451_("HadesDeathAge")));
        this.recallDropped = tag.m_128471_("HadesRecallDropped");
        this.attackYaw = this.m_146908_();
        // Upgrade existing 0.2.3 servants, including those stored inside a faded halo.
        this.setConfigurableAttributes();
        this.m_20242_(false);
        this.m_20095_();
        this.clearHarmfulEffects();
    }

    public void prepareRevival() {
        this.setHasLifespan(false);
        this.f_20919_ = 0;
        this.f_19802_ = 0;
        this.invulnerabilityTicks = 0;
        this.setNoHealTime(0);
        this.recallDropped = false;
        this.cooldownWork = 40;
        this.judgmentSouls = 0;
        this.f_19804_.m_135381_(DEATH_AGE, 0);
        this.f_19804_.m_135381_(ATTACK, 0);
        this.f_19804_.m_135381_(AGE, 0);
        this.setConfigurableAttributes();
        this.m_21153_(this.m_21233_());
        this.m_21530_();
    }

    @Override public AABB m_6921_() { return super.m_6921_().m_82377_(24, 22, 24); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new RidingAnimationController());
    }
    private PlayState animation(AnimationState<HadesServantEntity> state) {
        RawAnimation anim = !this.m_6084_() || this.deathAge() > 0 ? DEATH
                : this.attackType() == ROUNDHOUSE ? SPIN
                : this.attackType() == CLAW_COMBO ? this.attackAge() < 30 ? CLAW_ONE : CLAW_TWO
                : this.attackType() == DIVE_RAY ? this.isShootAction() ? SHOOT : SWIPE
                : this.attackType() == INFERNAL_JUDGMENT ? SMASH
                : this.useWalkAnimation(state.isMoving()) ? WALK : IDLE;
        state.getController().setTransitionLength(this.hasSeatPassenger() && this.attackType() == 0
                && this.m_6084_() ? 3 : 0);
        state.getController().setAnimation(anim);
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return this.animationCache; }

    /** Mounted loops share the seat's world clock, instead of restarting on client velocity changes. */
    private final class RidingAnimationController extends AnimationController<HadesServantEntity> {
        private float partialTick;
        RidingAnimationController() { super(HadesServantEntity.this, "main", 0, HadesServantEntity.this::animation); }

        @Override public void process(software.bernie.geckolib.core.animatable.model.CoreGeoModel<HadesServantEntity> model,
                AnimationState<HadesServantEntity> state,
                java.util.Map<String, software.bernie.geckolib.core.animatable.model.CoreGeoBone> bones,
                java.util.Map<String, software.bernie.geckolib.core.state.BoneSnapshot> snapshots,
                double seekTime, boolean crashWhenCantFindBone) {
            this.partialTick = state.getPartialTick();
            super.process(model, state, bones, snapshots, seekTime, crashWhenCantFindBone);
        }

        @Override protected double adjustTick(double tick) {
            double adjusted = super.adjustTick(tick);
            if (hasSeatPassenger() && attackType() == 0 && m_6084_()
                    && this.getAnimationState() == AnimationController.State.RUNNING) {
                double time = m_9236_().m_46467_() + this.partialTick - 1;
                return (time % 80 + 80) % 80;
            }
            return adjusted;
        }
    }

    private final class CombatGoal extends Goal {
        CombatGoal() { this.m_7021_(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK)); }
        @Override public boolean m_8036_() {
            // Reserve MOVE/LOOK while mounted even without a target, blocking follow/teleport/wander goals.
            return HadesServantEntity.this.m_6084_() && (m_6688_() != null || attackType() != 0
                    || m_5448_() != null && canHarm(m_5448_()));
        }
        @Override public boolean m_183429_() { return true; }
        @Override public void m_8037_() {
            if (attackType() != 0) return;
            LivingEntity target = m_5448_();
            if (target == null || !canHarm(target)) return;
            m_21563_().m_24960_(target, 30, 30);
            boolean inAttackHeight = target.m_20186_() >= m_20186_() - 2
                    && target.m_20186_() <= m_20186_() + 5;
            if (m_20280_(target) <= ATTACK_RANGE * ATTACK_RANGE && inAttackHeight && m_142582_(target)) {
                m_21573_().m_26573_();
                if (cooldownWork <= 0) {
                    int type = 1 + m_217043_().m_188503_(3);
                    startAttack(type);
                }
            } else if (!isStaying() && m_6688_() == null) {
                m_21573_().m_5624_(target, 1.0);
            }
        }
        @Override public void m_8041_() { m_21573_().m_26573_(); }
    }
}
