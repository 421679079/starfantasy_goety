package com.starfantasy.goety.entity;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist;
import com.Polarice3.Goety.init.ModSounds;
import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Scripted pageant actor using one of Doki Apostle's twelve visuals. */
public final class ApollyonPageantApostleEntity extends Cultist
        implements GeoEntity, ApollyonPageantOwned {
    public static final int RISEN = 0;
    public static final int ABHORRENT = 1;
    public static final int DEFILER = 2;
    public static final int DARK = 3;
    public static final int GREAT_SHADOW = 4;
    public static final int WITCH_KING = 5;
    public static final int PYRE_LORD = 6;
    public static final int PROFANE = 7;
    public static final int CRUEL = 8;
    public static final int TERRIBLE = 9;
    public static final int GLORIOUS = 10;
    public static final int ATROCIOUS = 11;

    private static final int ARCHER_BASE_SHOT_INTERVAL = 40;
    private static final int ARCHER_ENRAGED_SHOT_INTERVAL = 30;
    private static final float ARCHER_ENRAGED_DAMAGE_MULTIPLIER = 1.5F;
    private static final int ARCHER_WARNING_TICKS = 10;
    private static final double ARCHER_WARNING_WIDTH = 0.65D;
    private static final double ARCHER_WARNING_MIN_LENGTH = 16.0D;
    private static final double ARCHER_WARNING_MAX_LENGTH = 40.0D;
    private static final int ARCHER_WARNING_COLOR = 0xB00000;
    private static final double ARCHER_ROAM_RADIUS = 3.0D;
    private static final double ARCHER_MOVE_SPEED = 0.055D;

    private static final String OWNER_TAG = "PageantOwner";
    private static final String VARIANT_TAG = "PageantVariant";
    private static final String CASTING_TAG = "PageantCasting";
    private static final String BODY_YAW_TAG = "PageantBodyYaw";
    private static final String MOVING_TAG = "PageantMoving";
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> PARTICLES_ACTIVE =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> CASTING =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Float> BODY_YAW =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Boolean> MOVING =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> MONOLITH_PROTECTED =
            SynchedEntityData.m_135353_(
                    ApollyonPageantApostleEntity.class, EntityDataSerializers.f_135035_);

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;
    private Vec3 fixedPosition;
    private Vec3 roamTarget;
    private Vec3 monolithChannelPosition;
    private int roamTicks;
    private int shotCooldown = ARCHER_BASE_SHOT_INTERVAL;
    private int antiRegenTicks;
    private boolean thirdStageEnraged;

    public ApollyonPageantApostleEntity(
            EntityType<? extends ApollyonPageantApostleEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    @Override
    public MobType m_6336_() {
        return MobType.f_21641_;
    }

    @Override
    public void m_7311_(int ticks) {
        super.m_7311_(Math.min(0, ticks));
    }

    @Override
    public boolean m_6060_() {
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_()
                .m_22268_(Attributes.f_22276_, 20.0D)
                .m_22268_(Attributes.f_22279_, 0.0D)
                .m_22268_(Attributes.f_22284_, 0.0D)
                .m_22268_(Attributes.f_22285_, 0.0D)
                .m_22268_(Attributes.f_22278_, 0.0D)
                .m_22268_(Attributes.f_22277_, 48.0D);
    }

    public static ApollyonPageantApostleEntity spawn(
            ApollyonEntity owner, Vec3 position, int variant, boolean particlesActive) {
        return spawn(owner, position, variant, particlesActive, false);
    }

    public static ApollyonPageantApostleEntity spawn(
            ApollyonEntity owner, Vec3 position, int variant,
            boolean particlesActive, boolean damageable) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        EntityType<ApollyonPageantApostleEntity> actorType = damageable
                ? ApollyonEntityRegistry.APOSTLE.get()
                : ApollyonEntityRegistry.APOSTLE_ILLUSION.get();
        ApollyonPageantApostleEntity actor = new ApollyonPageantApostleEntity(
                actorType, owner.m_9236_());
        actor.ownerUuid = owner.m_20148_();
        actor.fixedPosition = position;
        Vec3 home = owner.arenaHomePosition();
        float bodyYaw = (float) Math.toDegrees(Math.atan2(
                -(position.f_82479_ - home.f_82479_),
                position.f_82481_ - home.f_82481_));
        actor.f_19804_.m_135381_(BODY_YAW, bodyYaw);
        actor.f_19804_.m_135381_(VARIANT, Mth.m_14045_(variant, 0, 11));
        actor.f_19804_.m_135381_(PARTICLES_ACTIVE, particlesActive);
        actor.f_19804_.m_135381_(CASTING, particlesActive);
        actor.f_19804_.m_135381_(MOVING, false);
        actor.applyTemporaryTitle();
        actor.applyConfiguredAttributes();
        actor.setupArcherEquipment();
        actor.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        actor.lockOutwardFacing();
        return owner.m_9236_().m_7967_(actor) ? actor : null;
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(VARIANT, 0);
        this.f_19804_.m_135372_(PARTICLES_ACTIVE, false);
        this.f_19804_.m_135372_(CASTING, false);
        this.f_19804_.m_135372_(BODY_YAW, 0.0F);
        this.f_19804_.m_135372_(MOVING, false);
        this.f_19804_.m_135372_(MONOLITH_PROTECTED, false);
    }

    @Override
    protected void m_8099_() {
        // Pageant actors are pose-only entities; the controller owns their complete timeline.
    }

    @Override
    public void m_8119_() {
        this.m_20095_();
        super.m_8119_();
        this.m_20242_(true);
        this.m_21573_().m_26573_();

        ApollyonEntity owner = this.owner();
        if (owner == null || !owner.m_6084_()) {
            if (!this.m_9236_().f_46443_) {
                this.m_146870_();
            }
            return;
        }
        if (!this.m_9236_().f_46443_ && this.isPageantArcher()) {
            this.tickArcher(owner);
        } else {
            this.m_20256_(Vec3.f_82478_);
            if (!this.m_9236_().f_46443_) {
                if (this.fixedPosition == null) {
                    this.fixedPosition = this.m_20182_();
                }
                this.m_6034_(this.fixedPosition.f_82479_, this.fixedPosition.f_82480_,
                        this.fixedPosition.f_82481_);
            }
            this.lockOutwardFacing();
        }
        if (!this.m_9236_().f_46443_ && this.particlesActive()) {
            this.spawnCastingParticle();
        }
    }

    @Override
    public CultistArmPose getArmPose() {
        if (this.isPageantArcher()) {
            return this.isCasting()
                    ? CultistArmPose.SPELLCASTING : CultistArmPose.BOW_AND_ARROW;
        }
        return this.isCasting() ? CultistArmPose.SPELLCASTING : CultistArmPose.CROSSED;
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        if (this.isMonolithProtected()) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && this.variant() == RISEN
                && source != null && source.m_7639_() instanceof LivingEntity attacker) {
            int smite = EnchantmentHelper.m_44836_(Enchantments.f_44978_, attacker);
            if (smite > 0) {
                this.antiRegenTicks = Math.min(200, smite * 40);
            }
        }
        return this.isPageantDamageable() && super.m_6469_(source, amount);
    }

    @Override
    public boolean m_6094_() {
        return this.isPageantDamageable();
    }

    @Override
    public boolean m_5829_() {
        return this.isPageantDamageable() && !this.isMonolithProtected();
    }

    @Override
    public void m_6667_(DamageSource source) {
        if (this.isPageantArcher()) {
            this.m_8061_(EquipmentSlot.MAINHAND, ItemStack.f_41583_);
        }
        super.m_6667_(source);
    }

    @Override
    protected void m_6668_(DamageSource source) {
        // dropAllDeathLoot: scripted pageant actors award neither items nor experience.
    }

    @Override
    public boolean m_6087_() {
        // isPickable: vanilla melee needs a selectable entity, not just a collision box.
        return this.m_6084_() && this.isPageantDamageable() && !this.isMonolithProtected();
    }

    @Override
    public boolean m_7307_(Entity other) {
        if (this.ownerUuid != null) {
            if (this.ownerUuid.equals(other.m_20148_())) {
                return true;
            }
            if (other instanceof ApollyonPageantOwned owned
                    && this.ownerUuid.equals(owned.pageantOwnerUuid())) {
                return true;
            }
        }
        return super.m_7307_(other);
    }

    @Override
    public boolean m_8023_() {
        // Temporary encounter titles must not make these actors persistent mobs.
        return false;
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(VARIANT, Mth.m_14045_(tag.m_128451_(VARIANT_TAG), 0, 11));
        boolean casting = tag.m_128471_(CASTING_TAG);
        this.f_19804_.m_135381_(CASTING, casting);
        this.f_19804_.m_135381_(PARTICLES_ACTIVE, casting);
        this.fixedPosition = this.m_20182_();
        this.f_19804_.m_135381_(BODY_YAW,
                tag.m_128441_(BODY_YAW_TAG)
                        ? tag.m_128457_(BODY_YAW_TAG)
                        : this.m_146908_());
        this.f_19804_.m_135381_(MOVING, tag.m_128471_(MOVING_TAG));
        this.f_19804_.m_135381_(MONOLITH_PROTECTED, false);
        this.applyTemporaryTitle();
        this.applyConfiguredAttributes();
        this.setupArcherEquipment();
        this.lockOutwardFacing();
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        // Titles are encounter-only labels. Never turn them into saved custom names.
        tag.m_128473_("CustomName");
        tag.m_128473_("CustomNameVisible");
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128405_(VARIANT_TAG, this.variant());
        tag.m_128379_(CASTING_TAG, this.isCasting());
        tag.m_128350_(BODY_YAW_TAG, this.pageantBodyYaw());
        tag.m_128379_(MOVING_TAG, this.isPageantMoving());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }

    public int variant() {
        return this.f_19804_.m_135370_(VARIANT);
    }

    public void startCasting(int visualDuration) {
        this.f_19804_.m_135381_(CASTING, true);
        this.f_19804_.m_135381_(PARTICLES_ACTIVE, true);
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(), 2.0F, 1.0F);
        if (this.variant() == TERRIBLE) {
            this.f_19804_.m_135381_(PARTICLES_ACTIVE, false);
            ApollyonCastingLightningEntity.spawn(this, visualDuration);
        }
    }

    public void finishCasting() {
        if (this.isCasting()) {
            this.playCastEffectSound();
        }
        this.stopCasting();
    }

    public void playCastEffectSound() {
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_CAST_SPELL.get(), 2.0F, 1.0F);
    }

    public void stopCasting() {
        this.f_19804_.m_135381_(CASTING, false);
        this.f_19804_.m_135381_(PARTICLES_ACTIVE, false);
    }

    public boolean isCasting() {
        return this.f_19804_.m_135370_(CASTING);
    }

    public float pageantBodyYaw() {
        return this.f_19804_.m_135370_(BODY_YAW);
    }

    public float interpolatedPageantBodyYaw(float partialTick) {
        return Mth.m_14189_(partialTick, this.f_20884_, this.f_20883_);
    }

    public boolean isPageantDamageable() {
        return this.m_6095_() == ApollyonEntityRegistry.APOSTLE.get();
    }

    public boolean isPageantArcher() {
        return this.variant() == RISEN || this.variant() == WITCH_KING;
    }

    public boolean isMonolithProtected() {
        return this.f_19804_.m_135370_(MONOLITH_PROTECTED);
    }

    public void setMonolithProtected(boolean protectedByMonolith) {
        if (!this.isPageantArcher()) {
            protectedByMonolith = false;
        }
        this.f_19804_.m_135381_(MONOLITH_PROTECTED, protectedByMonolith);
    }

    public void setMonolithChanneling(
            LivingEntity monolith, boolean channeling) {
        if (!this.isPageantArcher() || monolith == null) {
            channeling = false;
        }
        boolean changed = this.isCasting() != channeling;
        this.f_19804_.m_135381_(CASTING, channeling);
        this.f_19804_.m_135381_(PARTICLES_ACTIVE, false);
        this.monolithChannelPosition = channeling
                ? monolith.m_20182_().m_82520_(0.0D, monolith.m_20206_() * 0.5D, 0.0D)
                : null;
        if (changed) {
            this.shotCooldown = this.currentShotInterval();
            if (channeling) {
                this.m_5496_((SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(),
                        2.0F, 1.0F);
            }
        }
        if (channeling) {
            this.spawnMonolithChantBeam(monolith);
        }
    }

    public void setThirdStageEnraged(boolean enraged) {
        if (this.thirdStageEnraged == enraged) {
            return;
        }
        this.thirdStageEnraged = enraged;
        if (enraged) {
            this.shotCooldown = Math.min(
                    this.shotCooldown, ARCHER_ENRAGED_SHOT_INTERVAL);
        }
    }

    private UUID protectedRedirectTarget;

    public void setProtectedRedirectTarget(ApollyonPageantApostleEntity target) {
        this.protectedRedirectTarget = target == null ? null : target.m_20148_();
    }

    public ApollyonPageantApostleEntity protectedRedirectTarget() {
        if (!this.isMonolithProtected() || this.protectedRedirectTarget == null
                || !(this.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        Entity candidate = level.m_8791_(this.protectedRedirectTarget);
        return candidate instanceof ApollyonPageantApostleEntity other
                && other.m_6084_() && !other.isMonolithProtected() ? other : null;
    }

    public void setPageantTarget(LivingEntity target) {
        this.m_6710_(target);
    }

    public void onDeathArrowHitPlayer(LivingEntity target) {
        if (!this.isPageantArcher() || target == null) {
            return;
        }
        if (this.variant() == RISEN) {
            this.m_5634_(this.m_21233_() * 0.04F);
            if (ApollyonConfig.hardMode() && target.m_6084_()) {
                target.m_147207_(new MobEffectInstance(
                        ApollyonEffectRegistry.WEAKNESS.get(), 200, 2), this);
            }
        } else if (this.variant() == WITCH_KING && target.m_6084_()) {
            if (ApollyonConfig.hardMode()) {
                target.m_147207_(new MobEffectInstance(
                        GoetyEffects.CURSED.get(), 200, 0), this);
            }
            target.m_147207_(new MobEffectInstance(
                    GoetyEffects.SAPPED.get(), 200, 1), this);
        }
    }

    public boolean isPageantMoving() {
        return this.f_19804_.m_135370_(MOVING);
    }

    public void setPageantPosition(Vec3 position, float bodyYaw, boolean moving) {
        if (position == null) {
            return;
        }
        this.fixedPosition = position;
        this.f_19804_.m_135381_(BODY_YAW, bodyYaw);
        this.f_19804_.m_135381_(MOVING, moving);
        this.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        this.lockOutwardFacing();
    }

    public float applyIncomingDamageReductions(DamageSource source, float amount) {
        if (!this.isPageantDamageable() || amount <= 0.0F) {
            return amount;
        }
        if (source != null && source.m_269533_(DamageTypeTags.f_268731_)) {
            amount *= (float) (1.0D - Mth.m_14008_(
                    ApollyonConfig.apostleMagicDamageReduction(), -1.0D, 1.0D));
        }
        return amount * (float) (1.0D - Mth.m_14008_(
                ApollyonConfig.apostleDamageReduction(), 0.0D, 1.0D));
    }

    public float clampFinalDamage(float amount) {
        if (!this.isPageantDamageable() || amount <= 0.0F) {
            return amount;
        }
        double cap = ApollyonConfig.apostleDamageCap();
        float capped = cap <= 0.0D ? amount : Math.min(
                amount, (float) Mth.m_14008_(cap, 0.0D, 100000.0D));
        if (this.variant() == GLORIOUS) {
            return Math.min(capped, Math.max(0.0F, this.m_21223_() - 1.0F));
        }
        return capped;
    }

    private void tickArcher(ApollyonEntity owner) {
        if (this.fixedPosition == null) {
            this.fixedPosition = this.m_20182_();
        }
        double offsetX = this.m_20185_() - this.fixedPosition.f_82479_;
        double offsetZ = this.m_20189_() - this.fixedPosition.f_82481_;
        if (offsetX * offsetX + offsetZ * offsetZ > ARCHER_ROAM_RADIUS * ARCHER_ROAM_RADIUS) {
            this.m_6034_(this.fixedPosition.f_82479_, this.fixedPosition.f_82480_,
                    this.fixedPosition.f_82481_);
            this.roamTarget = null;
        }

        LivingEntity target = this.m_5448_();
        boolean active = owner.getPageantState() == ApollyonPageantController.THIRD_DPS
                && target != null && target.m_6084_();
        boolean channeling = this.isCasting() && this.monolithChannelPosition != null;
        this.tickArcherMovement(active && !channeling);
        if (channeling) {
            this.facePosition(this.monolithChannelPosition);
        } else if (target != null && target.m_6084_()) {
            this.faceTarget(target);
        }

        if (this.antiRegenTicks > 0) {
            --this.antiRegenTicks;
        } else if (this.variant() == RISEN && this.f_19797_ % 20 == 0) {
            this.m_5634_(this.m_21233_() * 0.02F);
        }
        if (!active || channeling) {
            return;
        }
        if (this.shotCooldown > 0) {
            --this.shotCooldown;
        }
        if (this.shotCooldown == ARCHER_WARNING_TICKS) {
            this.spawnShotWarning(target);
        }
        if (this.shotCooldown <= 0) {
            this.shootDeathArrow(target);
            this.shotCooldown = this.currentShotInterval();
        }
    }

    private void tickArcherMovement(boolean active) {
        if (!active) {
            this.f_19804_.m_135381_(MOVING, false);
            this.m_20256_(Vec3.f_82478_);
            return;
        }
        if (this.roamTarget == null || this.roamTicks-- <= 0
                || horizontalDistanceSqr(this.m_20182_(), this.roamTarget) < 0.04D) {
            double angle = this.m_217043_().m_188500_() * Math.PI * 2.0D;
            double radius = Math.sqrt(this.m_217043_().m_188500_()) * ARCHER_ROAM_RADIUS;
            this.roamTarget = new Vec3(
                    this.fixedPosition.f_82479_ + Math.cos(angle) * radius,
                    this.fixedPosition.f_82480_,
                    this.fixedPosition.f_82481_ + Math.sin(angle) * radius);
            this.roamTicks = 40 + this.m_217043_().m_188503_(41);
        }
        double dx = this.roamTarget.f_82479_ - this.m_20185_();
        double dz = this.roamTarget.f_82481_ - this.m_20189_();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length <= 1.0E-5D) {
            this.f_19804_.m_135381_(MOVING, false);
            this.m_20256_(Vec3.f_82478_);
            return;
        }
        double step = Math.min(ARCHER_MOVE_SPEED, length);
        this.m_6034_(this.m_20185_() + dx / length * step, this.fixedPosition.f_82480_,
                this.m_20189_() + dz / length * step);
        this.m_20256_(Vec3.f_82478_);
        this.f_19804_.m_135381_(MOVING, true);
    }

    private void faceTarget(LivingEntity target) {
        this.facePosition(target.m_20182_());
    }

    private void facePosition(Vec3 position) {
        float yaw = (float) Math.toDegrees(Math.atan2(
                -(position.f_82479_ - this.m_20185_()),
                position.f_82481_ - this.m_20189_()));
        this.f_19804_.m_135381_(BODY_YAW, yaw);
        this.lockOutwardFacing();
    }

    private void spawnShotWarning(LivingEntity target) {
        double dx = target.m_20185_() - this.m_20185_();
        double dz = target.m_20189_() - this.m_20189_();
        double length = Mth.m_14008_(Math.sqrt(dx * dx + dz * dz) + 8.0D,
                ARCHER_WARNING_MIN_LENGTH, ARCHER_WARNING_MAX_LENGTH);
        StarFantasyVfx.groundRectangleWarningTrackingGroundAimed(
                this, target, ARCHER_WARNING_TICKS, ARCHER_WARNING_WIDTH, length,
                0.0D, 0.0F, ARCHER_WARNING_COLOR, false, true);
    }

    private void shootDeathArrow(LivingEntity target) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        ApollyonDeathArrowEntity arrow = new ApollyonDeathArrowEntity(level, this);
        float damage = (float) ApollyonConfig.apostleBowDamage();
        if (this.thirdStageEnraged) {
            damage *= ARCHER_ENRAGED_DAMAGE_MULTIPLIER;
        }
        arrow.m_36745_(this, damage);
        double dx = target.m_20185_() - this.m_20185_();
        double dy = target.m_20227_(0.5D) - this.m_20227_(0.5D);
        double dz = target.m_20189_() - this.m_20189_();
        arrow.m_6686_(dx, dy, dz, 2.4F, 1.0F);
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_SHOOT.get(), 2.0F,
                1.0F / (this.m_217043_().m_188501_() * 0.4F + 0.8F));
        level.m_7967_(arrow);
    }

    private int currentShotInterval() {
        return this.thirdStageEnraged
                ? ARCHER_ENRAGED_SHOT_INTERVAL : ARCHER_BASE_SHOT_INTERVAL;
    }

    private void spawnMonolithChantBeam(LivingEntity monolith) {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 start = this.m_20182_().m_82520_(0.0D, this.m_20192_(), 0.0D);
        Vec3 end = monolith.m_20182_().m_82520_(
                0.0D, monolith.m_20206_() * 0.5D, 0.0D);
        Vec3 offset = end.m_82546_(start);
        double distance = offset.m_82553_();
        if (distance <= 1.0E-5D) {
            return;
        }
        Vec3 direction = offset.m_82490_(1.0D / distance);
        double progress = this.m_217043_().m_188500_();
        while (progress < distance) {
            progress += 0.5D;
            Vec3 position = start.m_82549_(direction.m_82490_(progress));
            level.m_8767_(ModParticleTypes.CHANT.get(),
                    position.f_82479_, position.f_82480_, position.f_82481_,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void setupArcherEquipment() {
        if (this.isPageantArcher()) {
            this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        }
    }

    private static double horizontalDistanceSqr(Vec3 first, Vec3 second) {
        double dx = first.f_82479_ - second.f_82479_;
        double dz = first.f_82481_ - second.f_82481_;
        return dx * dx + dz * dz;
    }

    private boolean particlesActive() {
        return this.f_19804_.m_135370_(PARTICLES_ACTIVE);
    }

    private void lockOutwardFacing() {
        float bodyYaw = this.pageantBodyYaw();
        this.m_146922_(bodyYaw);
        this.m_5618_(bodyYaw);
        this.f_20883_ = bodyYaw;
        this.m_5616_(bodyYaw);
        this.f_20885_ = bodyYaw;
        if (!this.m_9236_().f_46443_) {
            this.f_19859_ = bodyYaw;
            this.f_20884_ = bodyYaw;
            this.f_20886_ = bodyYaw;
        }
    }

    private void applyTemporaryTitle() {
        this.m_6593_(Component.m_237115_(switch (this.variant()) {
            case RISEN -> "entity.starfantasy_goety.apostle.title.risen";
            case ABHORRENT -> "entity.starfantasy_goety.apostle.title.abhorrent";
            case DEFILER -> "entity.starfantasy_goety.apostle.title.defiler";
            case DARK -> "entity.starfantasy_goety.apostle.title.dark";
            case GREAT_SHADOW -> "entity.starfantasy_goety.apostle.title.great_shadow";
            case WITCH_KING -> "entity.starfantasy_goety.apostle.title.witch_king";
            case PYRE_LORD -> "entity.starfantasy_goety.apostle.title.pyre_lord";
            case PROFANE -> "entity.starfantasy_goety.apostle.title.profane";
            case CRUEL -> "entity.starfantasy_goety.apostle.title.cruel";
            case TERRIBLE -> "entity.starfantasy_goety.apostle.title.terrible";
            case GLORIOUS -> "entity.starfantasy_goety.apostle.title.glorious";
            case ATROCIOUS -> "entity.starfantasy_goety.apostle.title.atrocious";
            default -> "entity.starfantasy_goety.apostle";
        }));
    }

    private ApollyonEntity owner() {
        if (this.ownerUuid == null || !(this.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.m_8791_(this.ownerUuid);
        return entity instanceof ApollyonEntity apollyon ? apollyon : null;
    }

    private void spawnCastingParticle() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        ParticleOptions particle;
        double velocityX;
        double velocityY;
        double velocityZ;
        if (this.variant() == PYRE_LORD || this.variant() == CRUEL) {
            particle = this.variant() == CRUEL
                    ? (ParticleOptions) ModParticleTypes.FROST.get()
                    : (ParticleOptions) ModParticleTypes.BIG_FIRE.get();
            double azimuth = this.m_217043_().m_188500_() * Math.PI * 2.0D;
            double elevation = this.m_217043_().m_188500_() * Math.PI * 0.5D;
            double horizontal = Math.cos(elevation);
            double speed = Mth.m_14139_(this.m_217043_().m_188500_(), 0.14D, 0.22D);
            velocityX = Math.cos(azimuth) * horizontal * speed;
            velocityY = Math.sin(elevation) * speed;
            velocityZ = Math.sin(azimuth) * horizontal * speed;
        } else if (this.variant() == TERRIBLE) {
            return;
        } else if (this.variant() == GLORIOUS) {
            particle = (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get();
            velocityX = 1.0D;
            velocityY = 0.85D;
            velocityZ = 0.15D;
        } else if (this.variant() == ATROCIOUS) {
            particle = (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get();
            velocityX = 1.0D;
            velocityY = 0.08D;
            velocityZ = 0.08D;
        } else if (this.variant() == DEFILER) {
            particle = (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get();
            velocityX = 0.45D;
            velocityY = 1.0D;
            velocityZ = 0.45D;
        } else if (this.variant() == ABHORRENT
                || this.variant() == DARK || this.variant() == GREAT_SHADOW) {
            particle = (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get();
            velocityX = 0.65D;
            velocityY = 0.15D;
            velocityZ = 0.9D;
        } else {
            particle = ApollyonParticleRegistry.PROFANE_SPELL.get();
            velocityX = 0.45D;
            velocityY = 1.0D;
            velocityZ = 0.45D;
        }
        Vec3 hand = this.castingHandPosition();
        level.m_8767_(particle, hand.f_82479_, hand.f_82480_, hand.f_82481_,
                0, velocityX, velocityY, velocityZ, 1.0D);
    }

    Vec3 castingHandPosition() {
        float angle = this.f_20883_ * ((float) Math.PI / 180.0F)
                + Mth.m_14089_((float) this.f_19797_ * 0.6662F) * 0.25F;
        return new Vec3(
                this.m_20185_() + Mth.m_14089_(angle) * 0.6D,
                this.m_20186_() + 1.8D,
                this.m_20189_() + Mth.m_14031_(angle) * 0.6D);
    }

    private void applyConfiguredAttributes() {
        double health = switch (this.variant()) {
            case GLORIOUS -> ApollyonConfig.apostleGloriousHealth();
            case RISEN -> ApollyonConfig.apostleRisenHealth();
            case WITCH_KING -> ApollyonConfig.apostleWitchKingHealth();
            default -> 20.0D;
        };
        health = Mth.m_14008_(health, 1.0D, 100000.0D);
        double armorValue = this.isPageantDamageable()
                ? Mth.m_14008_(ApollyonConfig.apostleArmor(), 0.0D, 1000.0D)
                : 0.0D;
        AttributeInstance maxHealth = this.m_21051_(Attributes.f_22276_);
        if (maxHealth != null) {
            maxHealth.m_22100_(health);
        }
        AttributeInstance armor = this.m_21051_(Attributes.f_22284_);
        if (armor != null) {
            armor.m_22100_(armorValue);
        }
        AttributeInstance toughness = this.m_21051_(Attributes.f_22285_);
        if (toughness != null) {
            toughness.m_22100_(armorValue);
        }
        AttributeInstance knockbackResistance = this.m_21051_(Attributes.f_22278_);
        if (knockbackResistance != null) {
            knockbackResistance.m_22100_(this.isPageantArcher() ? 1.0D : 0.0D);
        }
        this.m_21153_((float) health);
    }
}
