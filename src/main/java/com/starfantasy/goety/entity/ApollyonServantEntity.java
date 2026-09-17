package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.init.ModSounds;
import com.starfantasy.goety.combat.ApollyonFangFeastSpell;
import com.starfantasy.goety.combat.ApollyonFireTrapManager;
import com.starfantasy.goety.combat.ApollyonFrostImpactManager;
import com.starfantasy.goety.combat.ApollyonLightningStormManager;
import com.starfantasy.goety.combat.ApollyonMeteorManager;
import com.starfantasy.goety.combat.ApollyonVoidRayManager;
import com.starfantasy.goety.combat.ApollyonWildSurgeManager;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import net.minecraft.world.entity.OwnableEntity;
import com.starfantasy.goety.config.ServantConfig;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.combat.VoidRayKnockback;
import com.starfantasy.goety.combat.ApollyonDeathEffects;
import com.starfantasy.goety.combat.ApollyonServantTeleport;
import com.starfantasy.goety.animation.ApollyonPigAnimationController;
import com.starfantasy.goety.item.FadedHaloItem;
import com.starfantasy.goety.servant.ServantOwnershipData;
import net.minecraft.world.phys.AABB;

/** Permanent Goety servant with Apollyon's body traits and an independent combat controller. */
public final class ApollyonServantEntity extends Summoned implements GeoEntity {
    private static final int SPELL_CAST_TICKS = 40;
    private static final int PHASE_ONE_MULTISHOT_COOLDOWN_TICKS = 1200;
    private static final int PHASE_TWO_MULTISHOT_COOLDOWN_TICKS = 800;
    private static final float COOPERATIVE_SHIELD_HEALTH_RATIO = 0.2F;
    private static final float MAGIC_SHIELD_COST_MULTIPLIER = 4.0F;
    private static final int MULTISHOT_DURATION = 1200;
    private static final int MULTISHOT_SPELL_COOLDOWN_TICKS = 80;
    private static final int[] PHASE_TWO_SPELL_COOLDOWN_TICKS = {80, 120};
    private static final int FIRE_TRAP_CAST_TICKS = 60;
    private static final int FIRE_TRAP_SECOND_SPELL_EXTENSION_TICKS = 20;
    private static final int FIRE_TRAP_FIRST_WAVE_TICK = 40;
    private static final int LIGHTNING_STORM_WINDUP_TICKS = 40;
    private static final int LIGHTNING_STORM_EFFECT_TICKS = 60;
    private static final int LIGHTNING_STORM_CAST_TICKS =
            LIGHTNING_STORM_WINDUP_TICKS + LIGHTNING_STORM_EFFECT_TICKS;
    private static final int LIGHTNING_STORM_SECOND_SPELL_EXTENSION_TICKS = 20;
    private static final int FROST_IMPACT_WINDUP_TICKS = 40;
    private static final int FROST_IMPACT_CAST_TICKS = 115;
    private static final int FROST_IMPACT_SECOND_SPELL_EXTENSION_TICKS = 15;
    private static final int FROST_IMPACT_INTERVAL_TICKS = 10;
    private static final int WILD_SURGE_CAST_TICKS = 90;
    private static final int WILD_SURGE_EARTH_WARNING_TICK = 40;
    private static final int WILD_SURGE_EARTH_TICK = 60;
    private static final int WILD_SURGE_THORN_TICK = 90;
    private static final int VOID_RAY_CAST_TICKS = 100;
    private static final int VOID_RAY_WARNING_TICK = 40;
    private static final int VOID_RAY_FIRST_HIT_TICK = 70;
    private static final int VOID_RAY_SECOND_HIT_TICK = 90;
    private static final int[] POOL_SPELL_COOLDOWN_TICKS = {40, 80, 120};
    private static final double CASTING_PARTICLE_MIN_SPEED = 0.14D;
    private static final double CASTING_PARTICLE_MAX_SPEED = 0.22D;
    private static final int ARROW_INTERVAL_TICKS = 40;
    private static final int PHASE_TWO_ARROW_INTERVAL_TICKS = 30;
    private static final int PHASE_TWO_SPELLS_PER_CHAIN = 2;
    private static final double COMBAT_MOVE_SPEED = 1.0D;
    private static final double BOW_ATTACK_RANGE_SQR = 32.0D * 32.0D;
    private static final CastingSpell[] SPELL_POOL = {
            CastingSpell.FIRE_TRAP,
            CastingSpell.LIGHTNING_STORM,
            CastingSpell.FROST_IMPACT,
            CastingSpell.WILD_SURGE,
            CastingSpell.VOID_RAY,
            CastingSpell.FANG_FEAST
    };

    private static final double[] MULTISHOT_ANGLES = {
            -60.0D, -40.0D, -20.0D, 0.0D, 20.0D, 40.0D, 60.0D
    };
    private static final double[] SINGLE_SHOT_ANGLE = {0.0D};


    private static final EntityDataAccessor<Boolean> CASTING = SynchedEntityData.m_135353_(ApollyonServantEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> CAST_DURATION = SynchedEntityData.m_135353_(ApollyonServantEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Long> CAST_STARTED_AT = SynchedEntityData.m_135353_(ApollyonServantEntity.class, EntityDataSerializers.f_244073_);
    private static final EntityDataAccessor<Float> SHIELD = SynchedEntityData.m_135353_(ApollyonServantEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> DEATH_AGE = SynchedEntityData.m_135353_(ApollyonServantEntity.class, EntityDataSerializers.f_135028_);
    private static final UUID DEFENSIVE_KNOCKBACK = UUID.fromString("08659df8-0da6-43b9-8be4-795962fd5fd8");
    private static final RawAnimation STANDBY_ANIMATION = RawAnimation.begin().thenLoop("standby");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final ApollyonServantTeleport combatTeleport = new ApollyonServantTeleport(this);
    private final List<CastingSpell> remainingPoolSpells = new ArrayList<>();
    private CastingSpell lastPoolSpell = CastingSpell.NONE;
    private int multishotCooldown, spellCooldown, meteorCooldown, invulnerabilityTicks;
    private long nextShotTick;
    private boolean voidRayMovementLocked;
    private boolean shieldHitInProgress, standbyLocked;
    private float standbyYaw;
    private double deathGroundY;
    private boolean recallDropped;
    private boolean pigDeathSoundPlayed;

    public ApollyonServantEntity(EntityType<? extends ApollyonServantEntity> type, Level level) {
        super(type, level);
        this.setConfigurableAttributes();
        this.m_21153_(this.m_21233_());
        this.setHasLifespan(false);
        this.m_21530_();
        this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        this.m_21409_(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override public int getSummonLimit(LivingEntity owner) {
        return ServantOwnershipData.summonLimit(this, owner);
    }

    @Override public void onAddedToWorld() {
        super.onAddedToWorld();
        ServantOwnershipData.track(this);
    }

    @Override public void setOwnerId(UUID owner) {
        super.setOwnerId(owner);
        ServantOwnershipData.track(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_().m_22268_(Attributes.f_22276_, ApollyonConfig.DEFAULT_BOSS_HEALTH)
                .m_22268_(Attributes.f_22284_, ApollyonConfig.DEFAULT_ARMOR)
                .m_22268_(Attributes.f_22285_, ApollyonConfig.DEFAULT_ARMOR)
                .m_22268_(Attributes.f_22278_, 0.75D)
                .m_22268_(Attributes.f_22279_, 0.3).m_22268_(Attributes.f_22277_, 32)
                .m_22268_(Attributes.f_22281_, 10).m_22268_(Attributes.f_22283_, 1);
    }

    @Override public void setConfigurableAttributes() {
        this.m_21051_(Attributes.f_22276_).m_22100_(ServantConfig.APOLLYON_HEALTH.get());
        this.m_21051_(Attributes.f_22284_).m_22100_(ServantConfig.APOLLYON_ARMOR.get());
        this.m_21051_(Attributes.f_22285_).m_22100_(ServantConfig.APOLLYON_ARMOR.get());
        this.m_21051_(Attributes.f_22281_).m_22100_(ServantConfig.APOLLYON_ATTACK_DAMAGE.get());
        this.m_21051_(Attributes.f_22283_).m_22100_(ServantConfig.APOLLYON_ATTACK_SPEED.get());
        this.m_21051_(Attributes.f_22278_).m_22100_(0.75D);
        if (this.m_21223_() > this.m_21233_()) this.m_21153_(this.m_21233_());
    }

    @Override protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(CASTING, false);
        this.f_19804_.m_135372_(CAST_DURATION, 0);
        this.f_19804_.m_135372_(CAST_STARTED_AT, -1L);
        this.f_19804_.m_135372_(SHIELD, 0.0F);
        this.f_19804_.m_135372_(DEATH_AGE, 0);
    }

    @Override protected void m_8099_() {
        super.m_8099_();
        this.f_21345_.m_25352_(0, new StandbyGoal());
        this.f_21345_.m_25352_(2, new CombatActionGoal());
        this.f_21345_.m_25352_(8, new Summoned.WanderGoal<>(this, 1.0D));
    }

    @Override public MobType m_6336_() { return MobType.f_21641_; }
    @Override public boolean m_6040_() { return true; }
    @Override public void m_7311_(int ticks) { super.m_7311_(Math.min(0, ticks)); }
    @Override public boolean m_7301_(MobEffectInstance effect) {
        return effect.m_19544_().m_19483_() != MobEffectCategory.HARMFUL && super.m_7301_(effect);
    }
    @Override public boolean m_142535_(float distance, float multiplier, DamageSource source) { return false; }
    @Override protected float m_6431_(Pose pose, EntityDimensions dimensions) { return 1.55F; }
    @Override public void tryKill(Player player) {
        if (this.getKillChance() <= 0) this.warnKill(player); else super.tryKill(player);
    }

    public boolean isHalfHealth() { return this.m_21223_() <= this.m_21233_() * 0.5F; }
    private boolean isCombatPhaseTwo() { return this.isHalfHealth(); }
    public boolean isCastingAction() { return this.f_19804_.m_135370_(CASTING); }
    private void setCasting(boolean value) {
        this.f_19804_.m_135381_(CASTING, value);
        if (!value) this.f_19804_.m_135381_(CAST_DURATION, 0);
    }

    public static boolean isPigName(String name) {
        return "亚小猪".equals(name) || "Little Apollyon Pig".equalsIgnoreCase(name);
    }

    public boolean isPigVariant() {
        var name = this.m_7770_();
        return name != null && isPigName(name.getString());
    }

    public int castingAnimationDuration() { return this.f_19804_.m_135370_(CAST_DURATION); }
    public long castingAnimationStart() { return this.f_19804_.m_135370_(CAST_STARTED_AT); }

    private void beginCastingAnimation(int duration, boolean immediateFirstTick) {
        this.f_19804_.m_135381_(CAST_DURATION, Math.max(1, duration));
        // The second spell in a chain advances once on the same tick it starts.
        this.f_19804_.m_135381_(CAST_STARTED_AT,
                this.m_9236_().m_46467_() - (immediateFirstTick ? 1L : 0L));
    }

    private void playPigActionSound() {
        if (this.isPigVariant()) this.m_5496_(SoundEvents.f_12233_, 2.0F, 1.0F);
    }

    @Override protected SoundEvent m_5592_() {
        // The pig death cry is emitted in die(), including dismissal and direct kills.
        return this.isPigVariant() ? null : super.m_5592_();
    }

    private void playPigDeathSound() {
        if (!this.m_9236_().f_46443_ && this.isPigVariant() && !this.pigDeathSoundPlayed) {
            this.pigDeathSoundPlayed = true;
            this.m_5496_(SoundEvents.f_12234_, 2.0F, 1.0F);
        }
    }

    public static int shotInterval(boolean halfHealth, double speed) {
        double safeSpeed = Double.isFinite(speed) ? Math.max(0.01D, speed) : 1.0D;
        return Math.max(1, (int) Math.ceil((halfHealth ? (double) PHASE_TWO_ARROW_INTERVAL_TICKS : ARROW_INTERVAL_TICKS) / safeSpeed));
    }
    public int shotIntervalTicks() { return shotInterval(this.isHalfHealth(), this.m_21133_(Attributes.f_22283_)); }
    private int bowDrawTicks() { return Math.max(1, Math.min(20, shotIntervalTicks() / 2)); }
    public float scaleOutgoingDamage(float base) { return (float) (base * Math.max(0.0D, this.m_21133_(Attributes.f_22281_)) / 10.0D); }

    public boolean isFriendlyEntity(Entity target) {
        if (target == null || target == this) return true;
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            if (ownerId.equals(target.m_20148_())) return true;
            if (target instanceof Owned owned && ownerId.equals(owned.getOwnerId())) return true;
            if (target instanceof OwnableEntity ownable && ownerId.equals(ownable.m_21805_())) return true;
        }
        LivingEntity owner = this.getTrueOwner();
        return this.m_7307_(target) || target.m_7307_(this)
                || owner != null && (owner.m_7307_(target) || target.m_7307_(owner));
    }

    @Override public boolean m_6779_(LivingEntity target) {
        return !this.isStaying() && target != null && !this.isFriendlyEntity(target) && super.m_6779_(target);
    }

    @Override public void overrideSetTarget(LivingEntity target) {
        super.overrideSetTarget(this.isStaying() ? null : target);
    }
    @Override public void setPriorityTarget(LivingEntity target) {
        super.setPriorityTarget(this.isStaying() ? null : target);
    }

    @Override public void setStaying(boolean staying) {
        super.setStaying(staying);
        this.updateKnockbackResistance();
    }

    private void holdStandby() {
        this.combatTeleport.clear();
        if (!this.standbyLocked) {
            this.standbyYaw = this.m_146908_();
            this.standbyLocked = true;
            if (!this.m_9236_().f_46443_) {
                ApollyonFireTrapManager.clearForBoss(this);
                ApollyonLightningStormManager.clearForBoss(this);
                ApollyonWildSurgeManager.clearManagedThornsForBoss(this);
            }
        }
        this.m_6710_(null);
        this.setPriorityTarget(null);
        this.setPriorityTime(0);
        this.m_6703_(null);
        this.m_21335_(null);
        this.setCasting(false);
        this.setVoidRayMovementLocked(false);
        this.m_5810_();
        this.m_21561_(false);
        this.m_21573_().m_26573_();
        this.m_21566_().m_24988_(0, 0);
        this.m_7910_(0);
        this.m_21567_(0);
        this.m_20256_(new Vec3(0, this.m_20184_().f_82480_, 0));
        this.m_146922_(this.standbyYaw);
        this.m_146926_(0);
        this.f_19859_ = this.standbyYaw;
        this.f_19860_ = 0;
        this.f_20883_ = this.f_20884_ = this.standbyYaw;
        this.f_20885_ = this.f_20886_ = this.standbyYaw;
    }

    @Override public void m_7023_(Vec3 input) {
        if (this.isStaying()) {
            this.m_20256_(new Vec3(0, this.m_20184_().f_82480_, 0));
            input = Vec3.f_82478_;
        }
        super.m_7023_(input);
    }

    private final class StandbyGoal extends Goal {
        StandbyGoal() { this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)); }
        @Override public boolean m_8036_() { return isStaying(); }
        @Override public boolean m_183429_() { return true; }
        @Override public void m_8037_() { holdStandby(); }
    }

    private void setVoidRayMovementLocked(boolean locked) {
        this.voidRayMovementLocked = locked;
        VoidRayKnockback.setCasting(this, locked);
    }

    private void updateKnockbackResistance() {
        if (this.m_9236_().f_46443_) return;
        var attribute = this.m_21051_(Attributes.f_22278_);
        if (attribute == null) return;
        boolean present = attribute.m_22111_(DEFENSIVE_KNOCKBACK) != null;
        if (this.isStaying() || this.hasCooperativeShield() || this.shieldHitInProgress) {
            if (!present) attribute.m_22118_(new AttributeModifier(DEFENSIVE_KNOCKBACK,
                    "starfantasy_goety.servant_defense", 1.0D, AttributeModifier.Operation.ADDITION));
        } else if (present) attribute.m_22120_(DEFENSIVE_KNOCKBACK);
    }

    public float getCooperativeShield() { return this.f_19804_.m_135370_(SHIELD); }
    public boolean hasCooperativeShield() { return getCooperativeShield() > 0.001F; }
    private void activatePhaseTwoShield() {
        this.f_19804_.m_135381_(SHIELD, this.m_21233_() * COOPERATIVE_SHIELD_HEALTH_RATIO);
        this.invulnerabilityTicks=0;
        this.f_19802_=0;
        this.updateKnockbackResistance();
    }
    public float absorbShield(DamageSource source, float damage) {
        if (damage <= 0 || !hasCooperativeShield()) return damage;
        float cost = source.m_269533_(DamageTypeTags.f_268731_) ? MAGIC_SHIELD_COST_MULTIPLIER : 1.0F;
        float absorbed = Math.min(damage, getCooperativeShield() / cost);
        this.f_19804_.m_135381_(SHIELD, Math.max(0.0F, getCooperativeShield() - absorbed * cost));
        this.updateKnockbackResistance();
        return damage - absorbed;
    }

    private void setPhaseTwoSpellDelay() {
        this.setSpellDelay(PHASE_TWO_SPELL_COOLDOWN_TICKS[this.m_217043_().m_188503_(PHASE_TWO_SPELL_COOLDOWN_TICKS.length)]);
    }

    public float applyIncomingDamageReductions(DamageSource source, float amount) {
        if (source.m_269533_(DamageTypeTags.f_268738_)) return amount;
        double reduced = amount * (1.0D - ServantConfig.APOLLYON_DAMAGE_REDUCTION.get());
        if (source.m_269533_(DamageTypeTags.f_268731_)) reduced *= 1.0D - ServantConfig.APOLLYON_MAGIC_RESISTANCE.get();
        // Hidden non-casting defense shares the boss's live configuration.
        if (!this.isCastingAction()) reduced *= ApollyonConfig.damageTakenMultiplier();
        return (float) reduced;
    }

    public float finishIncomingDamage(DamageSource source, float amount) {
        if (source.m_269533_(DamageTypeTags.f_268738_)) return amount;
        double cap = ServantConfig.APOLLYON_DAMAGE_CAP.get();
        if (cap > 0) amount = Math.min(amount, (float) cap);
        return this.absorbShield(source, amount);
    }

    @Override public boolean m_6469_(DamageSource source, float amount) {
        if (this.m_6673_(source)) return false;
        boolean bypass = source.m_269533_(DamageTypeTags.f_268738_);
        if (!bypass && this.invulnerabilityTicks > 0) return false;
        int vanillaTime = this.f_19802_;
        this.f_19802_=0;
        float oldHealth=this.m_21223_();
        boolean previousShieldHit = this.shieldHitInProgress;
        this.shieldHitInProgress = this.hasCooperativeShield();
        this.updateKnockbackResistance();
        boolean hurt;
        try {
            hurt = super.m_6469_(source, amount);
        } finally {
            this.shieldHitInProgress = previousShieldHit;
            this.updateKnockbackResistance();
        }
        if (hurt) {
            this.f_19802_=0;
            if (!bypass && this.m_21223_() < oldHealth) this.invulnerabilityTicks=ApollyonConfig.bossInvulnerabilityTime();
            this.combatTeleport.onHurt();
        } else this.f_19802_=vanillaTime;
        return hurt;
    }

    @Override public void m_8119_() {
        if (this.m_6084_() && this.isStaying()) this.holdStandby(); else this.standbyLocked = false;
        super.m_8119_();
        if (this.m_6084_() && this.isStaying()) this.holdStandby();
        if (this.m_9236_().f_46443_ || !this.m_6084_()) return;
        this.updateKnockbackResistance();
        if (invulnerabilityTicks > 0) --invulnerabilityTicks;
        if (this.f_19797_ % 20 == 0) this.m_5634_(ServantConfig.APOLLYON_REGENERATION.get().floatValue());
        if (multishotCooldown > 0) --multishotCooldown;
        if (spellCooldown > 0) --spellCooldown;
        LivingEntity target = this.m_5448_();
        if (target != null && this.isFriendlyEntity(target)) { this.m_6710_(null); target=null; }
        this.combatTeleport.tick(target);
        if (!this.isStaying() && isHalfHealth() && target != null && target.m_6084_()) {
            if (meteorCooldown <= 0) { ApollyonMeteorManager.spawn(this); meteorCooldown=10; }
            --meteorCooldown;
        } else meteorCooldown=0;
    }

    @Override public void m_6667_(DamageSource source) {
        if (this.deathAge() > 0) return;
        this.playPigDeathSound();
        this.combatTeleport.clear();
        this.deathGroundY = this.m_20186_();
        this.setStaying(false);
        this.setCasting(true);
        this.setVoidRayMovementLocked(false);
        this.m_6710_(null);
        this.setPriorityTarget(null);
        this.m_21561_(false);
        this.m_21573_().m_26573_();
        this.m_5810_();
        this.m_20256_(Vec3.f_82478_);
        this.m_20242_(true);
        this.f_19812_ = true;
        this.f_19804_.m_135381_(SHIELD, 0.0F);
        ApollyonFireTrapManager.clearForBoss(this);
        ApollyonLightningStormManager.clearForBoss(this);
        ApollyonWildSurgeManager.clearManagedThornsForBoss(this);
        super.m_6667_(source);
    }

    public int deathAge() { return this.f_19804_.m_135370_(DEATH_AGE); }

    @Override protected void m_6153_() {
        this.setCasting(true);
        this.m_20256_(Vec3.f_82478_);
        this.m_20242_(true);
        this.f_19812_ = true;
        if (this.m_9236_().f_46443_ || this.m_213877_()) return;
        int age = this.deathAge() + 1;
        this.f_20919_ = age;
        this.f_19804_.m_135381_(DEATH_AGE, age);
        boolean finished = this.isPigVariant()
                ? age >= ApollyonDeathEffects.APOLLYON_DEATH_TICKS
                : ApollyonDeathEffects.tickApollyon(this, age, this.deathGroundY);
        if (finished) {
            if (!this.recallDropped && this.getOwnerId() != null) {
                this.recallDropped = true;
                FadedHaloItem.returnToOwner(this);
            }
            ApollyonDeathEffects.explodeApollyon(this);
            this.m_9236_().m_7605_(this, (byte) 60);
            this.m_142687_(Entity.RemovalReason.KILLED);
        }
    }

    public void prepareRevival() {
        this.combatTeleport.reset();
        this.setHasLifespan(false);
        this.f_20919_ = this.f_19802_ = this.invulnerabilityTicks = 0;
        this.recallDropped = false;
        this.pigDeathSoundPlayed = false;
        this.f_19804_.m_135381_(DEATH_AGE, 0);
        this.f_19804_.m_135381_(SHIELD, 0.0F);
        this.setCasting(false);
        this.setVoidRayMovementLocked(false);
        this.setStaying(false);
        this.setPriorityTarget(null);
        this.setPriorityTime(0);
        this.m_6710_(null);
        this.m_20242_(false);
        this.f_19812_ = false;
        this.m_20256_(Vec3.f_82478_);
        this.f_19789_ = 0;
        this.setNoHealTime(0);
        this.setConfigurableAttributes();
        this.m_21153_(this.m_21233_());
        this.m_21530_();
        this.updateKnockbackResistance();
    }

    @Override public AABB m_6921_() {
        return this.deathAge() > 0 ? super.m_6921_().m_82377_(14, 14, 14) : super.m_6921_();
    }

    @Override public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128405_("ApollyonServantMultishotCooldown", multishotCooldown);
        tag.m_128405_("ApollyonServantSpellCooldown", spellCooldown);
        tag.m_128405_("ApollyonServantMeteorCooldown", meteorCooldown);
        tag.m_128356_("ApollyonServantNextShot", nextShotTick);
        tag.m_128350_("ApollyonServantShield", getCooperativeShield());
        tag.m_128405_("ApollyonServantInvulnerability", invulnerabilityTicks);
        tag.m_128405_("ApollyonServantDeathAge", this.deathAge());
        tag.m_128347_("ApollyonServantDeathGroundY", this.deathGroundY);
        tag.m_128379_("ApollyonServantRecallDropped", this.recallDropped);
    }
    @Override public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        this.m_21051_(Attributes.f_22278_).m_22100_(0.75D);
        this.standbyLocked = false;
        multishotCooldown=Math.max(0, tag.m_128451_("ApollyonServantMultishotCooldown"));
        spellCooldown=Math.max(0, tag.m_128451_("ApollyonServantSpellCooldown"));
        meteorCooldown=Math.max(0, tag.m_128451_("ApollyonServantMeteorCooldown"));
        nextShotTick=Math.max(0, tag.m_128454_("ApollyonServantNextShot"));
        this.f_19804_.m_135381_(SHIELD, Math.max(0, tag.m_128457_("ApollyonServantShield")));
        invulnerabilityTicks=Math.max(0, tag.m_128451_("ApollyonServantInvulnerability"));
        this.f_19804_.m_135381_(DEATH_AGE, Math.max(0, tag.m_128451_("ApollyonServantDeathAge")));
        this.deathGroundY = tag.m_128441_("ApollyonServantDeathGroundY")
                ? tag.m_128459_("ApollyonServantDeathGroundY") : this.m_20186_();
        this.recallDropped = tag.m_128471_("ApollyonServantRecallDropped");
        this.pigDeathSoundPlayed = this.deathAge() > 0;
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "standby", 0, state -> {
            if (!this.isPigVariant() && this.isStaying() && this.m_6084_()) {
                state.getController().setAnimation(STANDBY_ANIMATION);
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
        controllers.add(new ApollyonPigAnimationController(this));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animationCache; }

    private boolean hasMultishot() {
        return this.m_21023_(ApollyonEffectRegistry.MULTISHOT.get());
    }

    private double[] currentShotAngles() {
        return this.hasMultishot() ? MULTISHOT_ANGLES : SINGLE_SHOT_ANGLE;
    }

    private void fireVolley(LivingEntity target) {
        Vec3 start = this.arrowOrigin();
        Vec3 targetPoint = new Vec3(target.m_20185_(), target.m_20227_(0.55D), target.m_20189_());
        Vec3 baseDirection = direction(start, targetPoint);

        for (double angle : this.currentShotAngles()) {
            Vec3 shotDirection = rotateY(baseDirection, angle);
            ApollyonStarArrowEntity arrow = new ApollyonStarArrowEntity(this.m_9236_(), this);
            arrow.m_6034_(start.f_82479_, start.f_82480_, start.f_82481_);
            arrow.m_6686_(shotDirection.f_82479_, shotDirection.f_82480_, shotDirection.f_82481_, 2.4F, 1.0F);
            this.m_9236_().m_7967_(arrow);
        }

        this.m_5496_((SoundEvent) ModSounds.APOSTLE_SHOOT.get(), 2.0F,
                1.0F / (this.m_217043_().m_188501_() * 0.4F + 0.8F));
        this.playPigActionSound();
        this.consumeMultishotLevel();
        this.combatTeleport.onShot(target);
    }

    public Vec3 arrowOrigin() {
        Vec3 forward = this.m_20154_();
        double yaw = this.m_146908_() * Math.PI / 180.0D;
        double side = this.m_5737_() == HumanoidArm.RIGHT ? -0.15D : 0.15D;
        // The model's shoulder is about 1.3 blocks up, independently of its halo hitbox.
        return this.m_20182_().m_82520_(Math.cos(yaw) * side, 1.3D, Math.sin(yaw) * side)
                .m_82549_(forward.m_82490_(0.5D));
    }

    private void consumeMultishotLevel() {
        MobEffectInstance current = this.m_21124_(ApollyonEffectRegistry.MULTISHOT.get());
        if (current == null) {
            return;
        }

        int remainingDuration = current.m_19557_();
        int nextAmplifier = current.m_19564_() - 1;
        this.m_21195_(ApollyonEffectRegistry.MULTISHOT.get());
        if (nextAmplifier >= 0 && remainingDuration > 1) {
            this.m_7292_(new MobEffectInstance(
                    ApollyonEffectRegistry.MULTISHOT.get(), remainingDuration, nextAmplifier, false, true));
        }
    }

    private CastingSpell drawPoolSpell() {
        if (this.remainingPoolSpells.isEmpty()) {
            for (CastingSpell spell : SPELL_POOL) {
                this.remainingPoolSpells.add(spell);
            }
            for (int i = this.remainingPoolSpells.size() - 1; i > 0; --i) {
                int swapIndex = this.m_217043_().m_188503_(i + 1);
                CastingSpell spell = this.remainingPoolSpells.get(i);
                this.remainingPoolSpells.set(i, this.remainingPoolSpells.get(swapIndex));
                this.remainingPoolSpells.set(swapIndex, spell);
            }
            int firstDrawIndex = this.remainingPoolSpells.size() - 1;
            if (this.remainingPoolSpells.size() > 1
                    && this.remainingPoolSpells.get(firstDrawIndex) == this.lastPoolSpell) {
                int swapIndex = this.m_217043_().m_188503_(firstDrawIndex);
                CastingSpell spell = this.remainingPoolSpells.get(firstDrawIndex);
                this.remainingPoolSpells.set(
                        firstDrawIndex, this.remainingPoolSpells.get(swapIndex));
                this.remainingPoolSpells.set(swapIndex, spell);
            }
        }
        CastingSpell spell = this.remainingPoolSpells.remove(
                this.remainingPoolSpells.size() - 1);
        this.lastPoolSpell = spell;
        return spell;
    }

    private void setSpellDelay(int ticks) {
        // Goal ticks run inside super.tick(), before the counters below are decremented.
        // The extra tick preserves the requested number of complete idle ticks.
        this.spellCooldown = Math.max(0, ticks) + 1;
    }

    private void setPoolSpellDelay() {
        this.setSpellDelay(POOL_SPELL_COOLDOWN_TICKS[
                this.m_217043_().m_188503_(POOL_SPELL_COOLDOWN_TICKS.length)]);
    }

    private void showCastingSmoke() {
        this.showScatteredHandCastingParticle(ApollyonParticleRegistry.CASTING_SMOKE.get());
    }

    private void showFireTrapCastingParticles() {
        this.showScatteredHandCastingParticle((ParticleOptions) ModParticleTypes.BIG_FIRE.get());
    }

    private void showFrostImpactCastingParticles() {
        this.showScatteredHandCastingParticle((ParticleOptions) ModParticleTypes.FROST.get());
    }

    private void showWildSurgeCastingParticles() {
        this.showHandCastingParticle(
                (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get(), 0.45D, 1.0D, 0.45D);
    }

    private void showFangFeastCastingParticles() {
        this.showHandCastingParticle(
                (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get(), 0.5D, 0.5D, 0.5D);
    }

    private void showVoidRayCastingParticles() {
        this.showHandCastingParticle(
                (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get(), 0.65D, 0.15D, 0.9D);
    }

    private void showScatteredHandCastingParticle(ParticleOptions particle) {
        double azimuth = this.m_217043_().m_188500_() * Math.PI * 2.0D;
        double elevation = this.m_217043_().m_188500_() * Math.PI * 0.5D;
        double horizontal = Math.cos(elevation);
        double speed = Mth.m_14139_(this.m_217043_().m_188500_(),
                CASTING_PARTICLE_MIN_SPEED, CASTING_PARTICLE_MAX_SPEED);
        this.showHandCastingParticle(particle,
                Math.cos(azimuth) * horizontal * speed,
                Math.sin(elevation) * speed,
                Math.sin(azimuth) * horizontal * speed);
    }

    private void showHandCastingParticle(ParticleOptions particle, double velocityX,
                                         double velocityY, double velocityZ) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 hand = this.castingHandPosition();
        level.m_8767_(particle, hand.f_82479_, hand.f_82480_, hand.f_82481_,
                0, velocityX, velocityY, velocityZ, 1.0D);
    }

    public Vec3 castingHandPosition() {
        // Matches SpellCastingCultist's Apostle branch: main-hand side, with the
        // same small casting sway around body yaw.
        float angle = this.f_20883_ * ((float) Math.PI / 180.0F)
                + Mth.m_14089_((float) this.f_19797_ * 0.6662F) * 0.25F;
        double side = this.m_5737_() == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        return new Vec3(
                this.m_20185_() + Mth.m_14089_(angle) * 0.6D * side,
                this.m_20186_() + 1.8D,
                this.m_20189_() + Mth.m_14031_(angle) * 0.6D * side);
    }

    private static Vec3 direction(Vec3 from, Vec3 to) {
        return new Vec3(to.f_82479_ - from.f_82479_, to.f_82480_ - from.f_82480_,
                to.f_82481_ - from.f_82481_).m_82541_();
    }

    private static Vec3 rotateY(Vec3 direction, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                direction.f_82479_ * cos - direction.f_82481_ * sin,
                direction.f_82480_,
                direction.f_82479_ * sin + direction.f_82481_ * cos).m_82541_();
    }

    private enum CastingSpell {
        NONE,
        MULTISHOT,
        FIRE_TRAP,
        LIGHTNING_STORM,
        FROST_IMPACT,
        WILD_SURGE,
        VOID_RAY,
        FANG_FEAST;

        private boolean isPoolSpell() {
            return this == FIRE_TRAP || this == LIGHTNING_STORM
                    || this == FROST_IMPACT || this == WILD_SURGE
                    || this == VOID_RAY || this == FANG_FEAST;
        }
    }
    private final class CombatActionGoal extends Goal {
        private int castTicks;
        private int seeTime;
        private int strafingTime = -1;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private CastingSpell activeSpell = CastingSpell.NONE;
        private boolean activeSpellSkippedWindup;
        private int phaseTwoChainSpellsRemaining;
        private Vec3 lightningStormAnchor;
        private Vec3 wildSurgeAnchor;
        private Vec3 voidRayAnchor;
        private List<Vec3> wildSurgeThornPoints = List.of();
        private ApollyonFangFeastSpell fangFeast;

        private CombatActionGoal() {
            this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean m_8036_() {
            LivingEntity target = ApollyonServantEntity.this.m_5448_();
            return !ApollyonServantEntity.this.isStaying() && target != null && target.m_6084_()
                    && !ApollyonServantEntity.this.isFriendlyEntity(target)
                    && ApollyonServantEntity.this.m_21205_().m_41720_() instanceof BowItem;
        }

        @Override
        public boolean m_8045_() {
            return this.m_8036_();
        }

        @Override
        public boolean m_183429_() {
            return true;
        }

        @Override
        public void m_8056_() {
            ApollyonServantEntity.this.m_21561_(true);
        }

        @Override
        public void m_8041_() {
            nextShotTick = 0;
            this.activeSpell = CastingSpell.NONE;
            this.activeSpellSkippedWindup = false;
            this.castTicks = 0;
            this.phaseTwoChainSpellsRemaining = 0;
            this.seeTime = 0;
            this.strafingTime = -1;
            this.lightningStormAnchor = null;
            this.wildSurgeAnchor = null;
            this.voidRayAnchor = null;
            this.wildSurgeThornPoints = List.of();
            this.finishFangFeast(false);
            ApollyonServantEntity.this.setVoidRayMovementLocked(false);
            ApollyonServantEntity.this.setCasting(false);
            ApollyonServantEntity.this.m_21561_(false);
            ApollyonServantEntity.this.m_5810_();
        }

        @Override
        public void m_8037_() {
            LivingEntity target = ApollyonServantEntity.this.m_5448_();
            if (ApollyonServantEntity.this.isStaying() || target == null || !target.m_6084_()) return;
            if (combatTeleport.isPending()) return;
            if (this.activeSpell != CastingSpell.NONE) { this.tickSpell(target); return; }
            boolean canSee = ApollyonServantEntity.this.m_21574_().m_148306_(target);
            this.updateMovement(target, canSee);
            if (ApollyonServantEntity.this.m_6117_()) {
                if (!canSee && this.seeTime < -60) { ApollyonServantEntity.this.m_5810_(); return; }
                if (canSee && ApollyonServantEntity.this.m_21252_() >= bowDrawTicks()) {
                    ApollyonServantEntity.this.m_5810_();
                    fireVolley(target);
                    nextShotTick = m_9236_().m_46467_() + shotIntervalTicks();
                    if (shotIntervalTicks() <= bowDrawTicks()) {
                        ApollyonServantEntity.this.m_6672_(ProjectileUtil.m_37297_(ApollyonServantEntity.this, Items.f_42411_));
                    }
                }
                return;
            }
            if (spellCooldown <= 0 && canSee) {
                CastingSpell spell = multishotCooldown <= 0 ? CastingSpell.MULTISHOT : drawPoolSpell();
                this.phaseTwoChainSpellsRemaining = isHalfHealth() && spell.isPoolSpell() ? 1 : 0;
                this.startSpell(spell);
                return;
            }
            long now = m_9236_().m_46467_();
            // Include the draw animation in the complete shot-to-shot interval.
            if (canSee && now >= nextShotTick - bowDrawTicks()) {
                ApollyonServantEntity.this.m_6672_(ProjectileUtil.m_37297_(ApollyonServantEntity.this, Items.f_42411_));
            }
        }

        private void updateMovement(LivingEntity target, boolean canSee) {
            double distanceSqr = ApollyonServantEntity.this.m_20275_(
                    target.m_20185_(), target.m_20186_(), target.m_20189_());
            boolean wasSeeing = this.seeTime > 0;
            if (canSee != wasSeeing) {
                this.seeTime = 0;
            }
            this.seeTime += canSee ? 1 : -1;

            if (distanceSqr > BOW_ATTACK_RANGE_SQR || this.seeTime < 20) {
                ApollyonServantEntity.this.m_21573_().m_5624_(target, COMBAT_MOVE_SPEED);
                this.strafingTime = -1;
            } else {
                ApollyonServantEntity.this.m_21573_().m_26573_();
                ++this.strafingTime;
            }

            if (this.strafingTime >= 20) {
                if (ApollyonServantEntity.this.m_217043_().m_188501_() < 0.3F) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (ApollyonServantEntity.this.m_217043_().m_188501_() < 0.3F) {
                    this.strafingBackwards = !this.strafingBackwards;
                }
                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distanceSqr > BOW_ATTACK_RANGE_SQR * 0.75D) {
                    this.strafingBackwards = false;
                } else if (distanceSqr < BOW_ATTACK_RANGE_SQR * 0.25D) {
                    this.strafingBackwards = true;
                }
                ApollyonServantEntity.this.m_21566_().m_24988_(
                        this.strafingBackwards ? -0.5F : 0.5F,
                        this.strafingClockwise ? 0.5F : -0.5F);
                // ApostleBowGoal turns the whole mob toward its target while
                // strafing. LookControl alone only turns the head and leaves the
                // body following sideways movement, producing an extreme twist.
                ApollyonServantEntity.this.m_21391_(target, 30.0F, 30.0F);
            } else {
                ApollyonServantEntity.this.m_21563_().m_24960_(target, 30.0F, 30.0F);
            }
        }

        private void startSpell(CastingSpell spell) {
            this.startSpell(spell, false);
        }

        private void startSpell(CastingSpell spell, boolean skipWindup) {
            this.finishFangFeast(false);
            this.activeSpell = spell;
            this.activeSpellSkippedWindup = skipWindup;
            this.castTicks = skipWindup ? this.fastStartTick(spell) : 0;
            this.lightningStormAnchor = null;
            this.wildSurgeAnchor = null;
            this.voidRayAnchor = null;
            this.wildSurgeThornPoints = List.of();
            if (spell == CastingSpell.FANG_FEAST) {
                this.fangFeast = new ApollyonFangFeastSpell(ApollyonServantEntity.this);
            }
            ApollyonServantEntity.this.setVoidRayMovementLocked(spell == CastingSpell.VOID_RAY);
            ApollyonServantEntity.this.setCasting(true);
            ApollyonServantEntity.this.beginCastingAnimation(this.castDuration() - this.castTicks, skipWindup);
            ApollyonServantEntity.this.m_21557_(false);
            ApollyonServantEntity.this.m_21573_().m_26573_();
            this.lockVoidRayMovement();
            ApollyonServantEntity.this.m_5496_((SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(), 2.0F, 1.0F);
            ApollyonServantEntity.this.playPigActionSound();
            if (spell == CastingSpell.LIGHTNING_STORM) {
                // The hand core identifies the spell throughout its windup.
                // Only the actual lightning sequence waits for tick 40.
                ApollyonCastingLightningEntity.spawn(
                        ApollyonServantEntity.this,
                        skipWindup
                                ? LIGHTNING_STORM_EFFECT_TICKS
                                + LIGHTNING_STORM_SECOND_SPELL_EXTENSION_TICKS
                                : LIGHTNING_STORM_CAST_TICKS);
            }
        }

        private int castDuration() {
            int castDuration = switch (this.activeSpell) {
                case FIRE_TRAP -> FIRE_TRAP_CAST_TICKS;
                case LIGHTNING_STORM -> LIGHTNING_STORM_CAST_TICKS;
                case FROST_IMPACT -> FROST_IMPACT_CAST_TICKS;
                case WILD_SURGE -> WILD_SURGE_CAST_TICKS;
                case VOID_RAY -> VOID_RAY_CAST_TICKS;
                case FANG_FEAST -> ApollyonFangFeastSpell.CAST_TICKS;
                default -> SPELL_CAST_TICKS;
            };
            if (this.activeSpellSkippedWindup) {
                castDuration += switch (this.activeSpell) {
                    case FIRE_TRAP -> FIRE_TRAP_SECOND_SPELL_EXTENSION_TICKS;
                    case LIGHTNING_STORM -> LIGHTNING_STORM_SECOND_SPELL_EXTENSION_TICKS;
                    case FROST_IMPACT -> FROST_IMPACT_SECOND_SPELL_EXTENSION_TICKS;
                    default -> 0;
                };
            }
            return castDuration;
        }

        private int fastStartTick(CastingSpell spell) {
            return switch (spell) {
                // Frost creates its first chunk before incrementing castTicks.
                case FROST_IMPACT, LIGHTNING_STORM -> 40;
                // These spells resolve their first event after incrementing castTicks.
                case FIRE_TRAP, WILD_SURGE, VOID_RAY -> 39;
                // Fang Feast has two complete warnings inside its own 60-tick timeline.
                case FANG_FEAST -> 0;
                default -> 0;
            };
        }

        private void tickSpell(LivingEntity target) {
            ApollyonServantEntity.this.m_21563_().m_24960_(target, 30.0F, 30.0F);
            ApollyonServantEntity.this.m_21573_().m_26573_();
            this.lockVoidRayMovement();
            if (this.activeSpell == CastingSpell.FROST_IMPACT
                    && this.castTicks >= FROST_IMPACT_WINDUP_TICKS
                    && this.castTicks <= FROST_IMPACT_WINDUP_TICKS + 30
                    && (this.castTicks - FROST_IMPACT_WINDUP_TICKS)
                    % FROST_IMPACT_INTERVAL_TICKS == 0) {
                ApollyonFrostImpactManager.spawnChunk(
                        ApollyonServantEntity.this, target,
                        (this.castTicks - FROST_IMPACT_WINDUP_TICKS)
                                / FROST_IMPACT_INTERVAL_TICKS);
            }
            switch (this.activeSpell) {
                case MULTISHOT -> ApollyonServantEntity.this.showCastingSmoke();
                case FIRE_TRAP -> ApollyonServantEntity.this.showFireTrapCastingParticles();
                case FROST_IMPACT -> ApollyonServantEntity.this.showFrostImpactCastingParticles();
                case WILD_SURGE -> ApollyonServantEntity.this.showWildSurgeCastingParticles();
                case FANG_FEAST -> ApollyonServantEntity.this.showFangFeastCastingParticles();
                case VOID_RAY -> {
                    if (this.castTicks < VOID_RAY_WARNING_TICK) {
                        ApollyonServantEntity.this.showVoidRayCastingParticles();
                    }
                }
                case LIGHTNING_STORM, NONE -> {
                }
            }

            ++this.castTicks;
            if (this.activeSpell == CastingSpell.FANG_FEAST && this.fangFeast != null) {
                this.fangFeast.tick(this.castTicks, target);
            }
            if (this.activeSpell == CastingSpell.FIRE_TRAP
                    && this.castTicks == FIRE_TRAP_FIRST_WAVE_TICK) {
                ApollyonFireTrapManager.cast(ApollyonServantEntity.this, target);
            }
            if (this.activeSpell == CastingSpell.LIGHTNING_STORM) {
                this.tickLightningStorm(target);
            } else if (this.activeSpell == CastingSpell.WILD_SURGE) {
                if (this.castTicks == WILD_SURGE_EARTH_WARNING_TICK) {
                    this.wildSurgeAnchor = ApollyonWildSurgeManager.captureAnchor(
                            ApollyonServantEntity.this, target);
                    ApollyonWildSurgeManager.warnEarthRing(
                            ApollyonServantEntity.this, this.wildSurgeAnchor);
                } else if (this.castTicks == WILD_SURGE_EARTH_TICK) {
                    this.wildSurgeThornPoints = ApollyonWildSurgeManager.spawnEarthRingAndWarnThorns(
                            ApollyonServantEntity.this, this.wildSurgeAnchor);
                } else if (this.castTicks == WILD_SURGE_THORN_TICK) {
                    ApollyonWildSurgeManager.spawnThornRings(
                            ApollyonServantEntity.this, this.wildSurgeThornPoints);
                }
            } else if (this.activeSpell == CastingSpell.VOID_RAY) {
                if (this.castTicks == VOID_RAY_WARNING_TICK) {
                    this.voidRayAnchor = ApollyonVoidRayManager.captureAnchor(ApollyonServantEntity.this);
                    ApollyonVoidRayManager.warn(ApollyonServantEntity.this, this.voidRayAnchor,
                            0.0F, ApollyonVoidRayManager.FIRST_WARNING_TICKS);
                } else if (this.castTicks == VOID_RAY_FIRST_HIT_TICK) {
                    ApollyonVoidRayManager.detonate(
                            ApollyonServantEntity.this, this.voidRayAnchor, 0.0F);
                    ApollyonVoidRayManager.warn(ApollyonServantEntity.this, this.voidRayAnchor,
                            ApollyonVoidRayManager.SECOND_WAVE_ROTATION,
                            ApollyonVoidRayManager.SECOND_WARNING_TICKS);
                } else if (this.castTicks == VOID_RAY_SECOND_HIT_TICK) {
                    ApollyonVoidRayManager.detonate(ApollyonServantEntity.this, this.voidRayAnchor,
                            ApollyonVoidRayManager.SECOND_WAVE_ROTATION);
                }
            }

            int castDuration = this.castDuration();
            if (this.castTicks < castDuration) {
                return;
            }

            CastingSpell completedSpell = this.activeSpell;
            if (completedSpell == CastingSpell.FANG_FEAST) {
                this.finishFangFeast(true);
            }
            boolean phaseTwoPoolSpell = ApollyonServantEntity.this.isCombatPhaseTwo()
                    && completedSpell.isPoolSpell();
            boolean continuePhaseTwoChain = phaseTwoPoolSpell
                    && this.phaseTwoChainSpellsRemaining > 0;

            if (completedSpell == CastingSpell.MULTISHOT) {
                ApollyonServantEntity.this.m_7292_(new MobEffectInstance(
                        ApollyonEffectRegistry.MULTISHOT.get(), MULTISHOT_DURATION, 9, false, true));
                if (ApollyonServantEntity.this.isCombatPhaseTwo()) {
                    ApollyonServantEntity.this.activatePhaseTwoShield();
                    ApollyonServantEntity.this.setPhaseTwoSpellDelay();
                } else {
                    ApollyonServantEntity.this.setSpellDelay(MULTISHOT_SPELL_COOLDOWN_TICKS);
                }
                ApollyonServantEntity.this.multishotCooldown =
                        ApollyonServantEntity.this.isCombatPhaseTwo()
                                ? PHASE_TWO_MULTISHOT_COOLDOWN_TICKS
                                : PHASE_ONE_MULTISHOT_COOLDOWN_TICKS;
            } else if (completedSpell.isPoolSpell()) {
                if (!continuePhaseTwoChain) {
                    if (phaseTwoPoolSpell) {
                        ApollyonServantEntity.this.setPhaseTwoSpellDelay();
                    } else {
                        ApollyonServantEntity.this.setPoolSpellDelay();
                    }
                }
            }

            ApollyonServantEntity.this.m_5496_((SoundEvent) ModSounds.APOSTLE_CAST_SPELL.get(), 2.0F, 1.0F);
            if (continuePhaseTwoChain) {
                --this.phaseTwoChainSpellsRemaining;
                this.startSpell(ApollyonServantEntity.this.drawPoolSpell(), true);
                // Resolve the skipped-windup spell's first particles and effect on
                // the exact tick that the preceding spell finishes.
                this.tickSpell(target);
                return;
            }
            this.activeSpell = CastingSpell.NONE;
            this.activeSpellSkippedWindup = false;
            this.castTicks = 0;
            this.lightningStormAnchor = null;
            this.wildSurgeAnchor = null;
            this.voidRayAnchor = null;
            this.wildSurgeThornPoints = List.of();
            ApollyonServantEntity.this.setVoidRayMovementLocked(false);
            ApollyonServantEntity.this.setCasting(false);

        }

        private void finishFangFeast(boolean completed) {
            if (this.fangFeast != null) {
                this.fangFeast.finish(completed);
                this.fangFeast = null;
            }
        }

        private void lockVoidRayMovement() {
            if (!ApollyonServantEntity.this.voidRayMovementLocked) {
                return;
            }
            ApollyonServantEntity.this.m_21573_().m_26573_();
            ApollyonServantEntity.this.m_21566_().m_24988_(0.0F, 0.0F);
            Vec3 movement = ApollyonServantEntity.this.m_20184_();
            ApollyonServantEntity.this.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
        }

        private void tickLightningStorm(LivingEntity target) {
            int logicTick = this.castTicks - LIGHTNING_STORM_WINDUP_TICKS;
            if (logicTick <= 0) {
                return;
            }
            if (logicTick <= 30 && logicTick % 5 == 0) {
                ApollyonLightningStormManager.queueTrackingStrike(ApollyonServantEntity.this, target);
            }
            if (logicTick == 30) {
                this.lightningStormAnchor =
                        ApollyonLightningStormManager.captureAnchor(ApollyonServantEntity.this, target);
                ApollyonLightningStormManager.queueRing(
                        ApollyonServantEntity.this, this.lightningStormAnchor, 24.0D, 24);
            } else if (logicTick == 40) {
                ApollyonLightningStormManager.queueRing(
                        ApollyonServantEntity.this, this.lightningStormAnchor, 16.0D, 16);
            } else if (logicTick == 50) {
                ApollyonLightningStormManager.queueRing(
                        ApollyonServantEntity.this, this.lightningStormAnchor, 8.0D, 8);
            } else if (logicTick == 60) {
                ApollyonLightningStormManager.queueCenter(
                        ApollyonServantEntity.this, this.lightningStormAnchor);
            }
        }
    }
}
