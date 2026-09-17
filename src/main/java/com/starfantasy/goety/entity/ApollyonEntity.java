package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.entities.ai.StrollAroundLeaderGoal;
import com.Polarice3.Goety.client.particles.AbsorbTrailParticleOption;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.network.ModServerBossInfo;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.combat.ApollyonFangFeastSpell;
import com.starfantasy.goety.combat.ApollyonFireTrapManager;
import com.starfantasy.goety.combat.ApollyonDeathEffects;
import com.starfantasy.goety.combat.ApollyonFrostImpactManager;
import com.starfantasy.goety.combat.ApollyonLightningStormManager;
import com.starfantasy.goety.combat.ApollyonMeteorManager;
import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.combat.ApollyonSummonManager;
import com.starfantasy.goety.combat.ApollyonVoidRayManager;
import com.starfantasy.goety.combat.ApollyonWildSurgeManager;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import com.starfantasy.library.combat.StarFantasyTrueKillHelper;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.library.vfx.entity.GroundRectangleWarningEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A deliberately small Apostle-like combatant. It borrows the Cultist pose model,
 * but none of Apostle's phase, weather, summoning or world-changing mechanics.
 */
public final class ApollyonEntity extends Cultist implements com.starfantasy.library.combat.CombatHealthEntity, RangedAttackMob, GeoEntity,
        com.starfantasy.goety.combat.ApollyonDeathInventory.Encounter,
        com.starfantasy.goety.combat.ApollyonPageantAggro.Encounter {
    public static final int DEATH_ANIMATION_TICKS = ApollyonDeathEffects.APOLLYON_DEATH_TICKS;
    private static final String HOME_X_TAG = "ApollyonHomeX";
    private static final String CONFIGURED_HEALTH_TAG = "ApollyonConfiguredHealth";
    private static final String HOME_Y_TAG = "ApollyonHomeY";
    private static final String HOME_Z_TAG = "ApollyonHomeZ";
    private static final String MULTISHOT_COOLDOWN_TAG = "ApollyonMultishotCooldown";
    private static final String SPELL_COOLDOWN_TAG = "ApollyonGlobalSpellCooldown";
    private static final String COOPERATIVE_SHIELD_TAG = "ApollyonCooperativeShield";
    private static final String ARROW_COOLDOWN_TAG = "ApollyonArrowCooldown";
    private static final String ATTACK_DECISION_COOLDOWN_TAG = "ApollyonAttackDecisionCooldown";
    private static final String METEOR_COOLDOWN_TAG = "ApollyonMeteorCooldown";
    private static final String ANTI_REGEN_TAG = "ApollyonAntiRegen";
    private static final String ANTI_REGEN_TOTAL_TAG = "ApollyonAntiRegenTotal";
    private static final String MONOLITH_POWER_TICKS_TAG = "ApollyonMonolithPowerTicks";
    private static final String BOSS_INVULNERABILITY_TICKS_TAG = "ApollyonBossInvulnerabilityTicks";
    private static final String COMBAT_PHASE_TAG = "ApollyonCombatPhase";
    private static final String PHASE_TWO_HALF_HEALTH_ENRAGED_TAG =
            "ApollyonPhaseTwoHalfHealthEnraged";
    public static final int COMBAT_PHASE_ONE = 1;
    public static final int COMBAT_PHASE_TWO = 2;
    private static final int SPELL_CAST_TICKS = 40;
    private static final int PHASE_ONE_MULTISHOT_COOLDOWN_TICKS = 1200;
    private static final int PHASE_TWO_MULTISHOT_COOLDOWN_TICKS = 800;
    private static final float COOPERATIVE_SHIELD_HEALTH_RATIO = 0.2F;
    private static final UUID SHIELD_KNOCKBACK_MODIFIER = UUID.fromString("839c9952-d0c6-452b-b61f-5b2bf567c963");
    private static final float MAGIC_SHIELD_COST_MULTIPLIER = 4.0F;
    private static final int MULTISHOT_DURATION = 1200;
    private static final int ATTACK_DECISION_INTERVAL_TICKS = 20;
    private static final int METEOR_INTERVAL_TICKS = 15;
    private static final int PHASE_TWO_ENRAGED_METEOR_INTERVAL_TICKS = 10;
    /** Apollyon always uses the Apostle's second-phase teleport windup. */
    private static final int TELEPORT_WINDUP_TICKS = 20;
    private static final int MULTISHOT_SPELL_COOLDOWN_TICKS = 80;
    private static final int[] PHASE_TWO_SPELL_COOLDOWN_TICKS = {80, 120};
    private static final int[] PHASE_TWO_ENRAGED_SPELL_COOLDOWN_TICKS = {60, 90};
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
    /** Keep the five per-spell summon definitions ready without spawning them for now. */
    private static final boolean POOL_SPELL_SUMMONS_ENABLED = false;
    private static final double CASTING_PARTICLE_MIN_SPEED = 0.14D;
    private static final double CASTING_PARTICLE_MAX_SPEED = 0.22D;
    private static final int BOW_DRAW_TICKS = 20;
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
    private static final int SHOT_WARNING_TICKS = 10;
    private static final double SHOT_WARNING_WIDTH = 0.65D;
    private static final double SHOT_WARNING_LENGTH = 40.0D;
    private static final int SHOT_WARNING_COLOR = 0xB00000;
    private static final double[] MULTISHOT_ANGLES = {
            -60.0D, -40.0D, -20.0D, 0.0D, 20.0D, 40.0D, 60.0D
    };
    private static final double[] SINGLE_SHOT_ANGLE = {0.0D};

    public static final double ARENA_SIZE = 40.0D;
    public static final double ARENA_RADIUS = ARENA_SIZE * 0.5D;
    private static final double ARENA_ENTRY_HEIGHT = 8.0D;
    public static final double ARENA_VISUAL_HEIGHT = 4.0D;
    public static final float ARENA_WALL_ALPHA = 0.3F;
    private static final double ARENA_DISENGAGE_DISTANCE_SQR = 48.0D * 48.0D;
    private static final int HOME_RETURN_CHECK_TICKS = 20;
    private static final double ARENA_COLLISION_INSET = 0.02D;
    private static final int SAFE_BOUNDARY_KNOCKBACK_INTERVAL_TICKS = 10;
    private static final double SAFE_BOUNDARY_KNOCKBACK_HORIZONTAL = 0.8D;
    private static final double SAFE_BOUNDARY_KNOCKBACK_VERTICAL = 0.4D;
    private static final double HOME_RETURN_DISTANCE_SQR = 1.0D;
    private static final double RENDER_DISTANCE = 128.0D;

    private static final EntityDataAccessor<Boolean> CASTING =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> ANTI_REGEN =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> ANTI_REGEN_TOTAL =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Boolean> MONOLITH_POWER =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> ARENA_ACTIVE =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Float> HOME_X =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> HOME_Y =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> HOME_Z =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> COMBAT_PHASE =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> COOPERATIVE_SHIELD =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> PAGEANT_STATE =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> PAGEANT_OPACITY =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Boolean> PAGEANT_RETURN_SMOKE =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Boolean> PAGEANT_RETURN_RAINBOW =
            SynchedEntityData.m_135353_(ApollyonEntity.class, EntityDataSerializers.f_135035_);

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final ApollyonPageantController pageant = new ApollyonPageantController(this);
    public final ModServerBossInfo bossInfo;
    private int multishotCooldown;
    private int spellCooldown;
    private int arrowCooldown;
    private int attackDecisionCooldown;
    private int teleportCooldown;
    private int meteorCooldown;
    private boolean phaseTwoHalfHealthEnraged;
    private int monolithPowerTicks;
    private final com.starfantasy.library.combat.CombatHealthProtection healthProtection =
            new com.starfantasy.library.combat.CombatHealthProtection();
    private int damageProcessingDepth;
    private boolean shieldHitInProgress;
    private int receivedHits;
    private int teleportWindupTicks;
    private Vec3 pendingTeleportPosition;
    private boolean voidRayMovementLocked;
    private final List<GroundRectangleWarningEntity> shotWarnings = new ArrayList<>();
    private final List<CastingSpell> remainingPoolSpells = new ArrayList<>();
    private CastingSpell lastPoolSpell = CastingSpell.NONE;
    private final List<Integer> remainingHadesAttacks = new ArrayList<>();
    // Keep the player instance: respawning creates a new entity with the same UUID.
    private final Map<UUID, ServerPlayer> arenaPlayers = new HashMap<>();
    private final Map<UUID, Long> safeBoundaryKnockbackReadyTicks = new HashMap<>();
    private UUID boundaryMobUuid;
    private Vec3 arenaHome = Vec3.f_82478_;
    private boolean arenaHomeInitialized;
    private boolean phaseTwoInitializationPending;
    private double deathGroundY;
    private double lastConfiguredHealth = Double.NaN;

    public ApollyonEntity(EntityType<? extends ApollyonEntity> entityType, Level level) {
        super(entityType, level);
        this.m_21530_();
        this.bossInfo = new ModServerBossInfo(this, BossEvent.BossBarColor.RED, true, true);
        this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        this.applyConfiguredAttributes(true);
        this.healthProtection.enable();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.m_21552_()
                .m_22268_(Attributes.f_22276_, ApollyonConfig.DEFAULT_BOSS_HEALTH)
                .m_22268_(Attributes.f_22279_, 0.35D)
                .m_22268_(Attributes.f_22277_, 48.0D)
                .m_22268_(Attributes.f_22284_, ApollyonConfig.DEFAULT_ARMOR)
                .m_22268_(Attributes.f_22285_, ApollyonConfig.DEFAULT_ARMOR)
                .m_22268_(Attributes.f_22278_, 0.75D);
    }

    @Override
    public boolean m_6785_(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean m_8023_() {
        return true;
    }

    @Override
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < RENDER_DISTANCE * RENDER_DISTANCE;
    }

    @Override
    public AABB m_6921_() {
        return new AABB(
                this.m_20185_() - RENDER_DISTANCE,
                this.m_20186_() - RENDER_DISTANCE,
                this.m_20189_() - RENDER_DISTANCE,
                this.m_20185_() + RENDER_DISTANCE,
                this.m_20186_() + RENDER_DISTANCE,
                this.m_20189_() + RENDER_DISTANCE);
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(CASTING, false);
        this.f_19804_.m_135372_(ANTI_REGEN, 0);
        this.f_19804_.m_135372_(ANTI_REGEN_TOTAL, 0);
        this.f_19804_.m_135372_(MONOLITH_POWER, false);
        this.f_19804_.m_135372_(ARENA_ACTIVE, false);
        this.f_19804_.m_135372_(HOME_X, 0.0F);
        this.f_19804_.m_135372_(HOME_Y, 0.0F);
        this.f_19804_.m_135372_(HOME_Z, 0.0F);
        this.f_19804_.m_135372_(COMBAT_PHASE, COMBAT_PHASE_ONE);
        this.f_19804_.m_135372_(COOPERATIVE_SHIELD, 0.0F);
        this.f_19804_.m_135372_(PAGEANT_STATE, ApollyonPageantController.INACTIVE);
        this.f_19804_.m_135372_(PAGEANT_OPACITY, 1.0F);
        this.f_19804_.m_135372_(PAGEANT_RETURN_SMOKE, false);
        this.f_19804_.m_135372_(PAGEANT_RETURN_RAINBOW, false);
    }

    @Override
    protected void m_8099_() {
        super.m_8099_();
        this.f_21345_.m_262460_(goal -> goal instanceof StrollAroundLeaderGoal);
        this.f_21345_.m_25352_(1, new CombatActionGoal());
        this.f_21346_.m_25352_(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData m_6518_(ServerLevelAccessor level, DifficultyInstance difficulty,
                                  MobSpawnType spawnType, SpawnGroupData spawnData, CompoundTag dataTag) {
        SpawnGroupData result = super.m_6518_(level, difficulty, spawnType, spawnData, dataTag);
        this.setArenaHome(this.m_20182_());
        return result;
    }

    @Override
    public void m_8119_() {
        this.m_20095_();
        if (!this.m_9236_().f_46443_) {
            this.clearDistantAggro();
        }
        super.m_8119_();
        if (this.m_9236_().f_46443_) {
            if (this.f_19804_.m_135370_(PAGEANT_RETURN_RAINBOW)) {
                this.showPageantRainbowCastingParticle();
            } else if (this.getPageantState() == ApollyonPageantController.FADE_OUT
                    || this.f_19804_.m_135370_(PAGEANT_RETURN_SMOKE)) {
                this.showOriginalApostleTransitionSmoke();
            }
        }
        if (this.isPlayingDeathAnimation()) {
            return;
        }
        if (!this.pageant.isCombatLocked()
                && !(this.m_21205_().m_41720_() instanceof BowItem)) {
            this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        }

        if (!this.m_9236_().f_46443_) {
            if (this.phaseTwoInitializationPending) {
                this.phaseTwoInitializationPending = false;
                this.ensureArenaHome();
                this.enterCombatPhaseTwo();
            }
            this.healthProtection.tick();
            this.tickPhaseTwoHalfHealthState();
            if (this.f_19797_ % 20 == 0) {
                this.applyConfiguredAttributes(false);
            }
            if (this.f_19797_ % 5 == 0) {
                this.bossInfo.update();
            }
            this.bossInfo.m_142711_(this.m_21223_() / this.m_21233_());
            this.prepareArenaPlayersForPageant();
            this.pageant.tickServer();
            this.pageant.tickPersistentCompanion();
            LivingEntity target = this.m_5448_();
            this.tickArenaState(target);
            target = this.m_5448_();
            if (this.pageant.isCombatLocked()) {
                this.shotWarnings.removeIf(warning -> !warning.m_6084_());
                return;
            }
            if (this.getAntiRegen() > 0) {
                this.setAntiRegen(this.getAntiRegen() - 1, this.getAntiRegenTotal());
            } else if (this.f_19797_ % 20 == 0 && this.m_21223_() < this.m_21233_()) {
                this.m_5634_((float) ApollyonConfig.healthRegeneration());
            }
            if (this.multishotCooldown > 0) {
                --this.multishotCooldown;
            }
            if (this.spellCooldown > 0) {
                --this.spellCooldown;
            }
            if (this.arrowCooldown > 0) {
                --this.arrowCooldown;
            }
            if (this.attackDecisionCooldown > 0) {
                --this.attackDecisionCooldown;
            }
            if (this.teleportCooldown > 0) {
                --this.teleportCooldown;
            }
            this.tickPendingTeleport();
            if (this.monolithPowerTicks > 0) {
                --this.monolithPowerTicks;
            }
            this.setMonolithPower(this.monolithPowerTicks > 0);

            this.tickAmbienceMeteor(target);
            if (target != null && target.m_6084_() && this.m_6117_()
                    && this.m_21252_() == 10) {
                this.spawnShotWarnings(target);
            }
            this.shotWarnings.removeIf(warning -> !warning.m_6084_());
            if (target != null && target.m_6084_() && this.teleportCooldown <= 0
                    && (this.m_20280_(target) > 1024.0D || !this.m_21574_().m_148306_(target))) {
                this.tryCombatTeleport(target);
            }
        }
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        if (source.m_276093_(DamageTypes.f_286979_)) {
            this.pageant.discardFromKill();
            return true;
        }
        if (!this.isArenaDamageSource(source)) {
            return false;
        }
        if (this.pageant.isInvulnerable()) {
            return false;
        }
        if (source.m_276093_(DamageTypes.f_268612_)
                || source.m_276093_(DamageTypes.f_268671_)
                || this.isFriendlyDamageSource(source)) {
            return false;
        }
        if (!this.m_9236_().f_46443_ && source.m_7640_() instanceof LivingEntity attacker) {
            int smite = EnchantmentHelper.m_44836_(Enchantments.f_44978_, attacker);
            if (smite > 0) {
                int duration = Mth.m_14045_(smite, 1, 5) * 20;
                this.setAntiRegen(duration, duration);
            }
        }
        if (this.isMonolithPower()) {
            return false;
        }
        boolean shielded = this.hasCooperativeShield();
        if (this.healthProtection.invulnerabilityTicks() > 0 && !shielded) {
            return false;
        }
        boolean previousShieldHit = this.shieldHitInProgress;
        if (shielded) {
            this.f_19802_ = 0;
            this.healthProtection.setInvulnerabilityTicks(0);
            this.shieldHitInProgress = true;
        }
        boolean hurt;
        try {
            hurt = super.m_6469_(source, amount);
        } finally {
            if (shielded) {
                this.f_19802_ = 0;
                this.healthProtection.setInvulnerabilityTicks(0);
            }
            this.shieldHitInProgress = previousShieldHit;
            this.updateShieldKnockbackResistance();
        }
        if (hurt && !this.m_9236_().f_46443_ && ++this.receivedHits >= 4) {
            this.receivedHits = 0;
            LivingEntity target = this.m_5448_();
            if (target != null) {
                this.tryCombatTeleport(target);
            }
        }
        return hurt;
    }

    @Override
    public void m_6667_(DamageSource source) {
        if (this.f_20919_ > 0) {
            return;
        }
        if (!this.m_9236_().f_46443_) {
            this.deathGroundY = this.m_20186_();
            this.setArenaActive(false);
            this.setPageantOpacity(1.0F);
            this.setPageantReturnSmoke(false);
            this.setPageantReturnRainbow(false);
            this.bossInfo.m_8321_(false);
            this.pageant.beginBossDeath();
            this.clearCombatForPageant();
            this.m_6710_(null);
            this.m_21561_(false);
            this.m_6842_(false);
            this.m_20242_(true);
            this.setCasting(true);
        }
        super.m_6667_(source);
    }

    @Override
    protected void m_7625_(DamageSource source, boolean recentlyHit) {
        if (this.usesDefaultDeathDrops()) {
            super.m_7625_(source, recentlyHit);
        }
    }

    @Override
    protected void m_7472_(DamageSource source, int looting, boolean recentlyHit) {
        super.m_7472_(source, looting, recentlyHit);
        if (!this.usesDefaultDeathDrops()) {
            for (String entry : ApollyonConfig.deathDrops()) {
                this.dropConfiguredDeathEntry(entry);
            }
        }
        this.dropRandomApostleHalos();
    }

    private boolean usesDefaultDeathDrops() {
        return ApollyonConfig.deathDrops().equals(ApollyonConfig.defaultDeathDrops());
    }

    private void dropRandomApostleHalos() {
        int dropCount = Math.min(ApollyonConfig.randomApostleHaloDrops(),
                HaloItemRegistry.APOSTLE_HALOS.size());
        List<Item> choices = HaloItemRegistry.APOSTLE_HALOS.stream()
                .map(entry -> entry.get())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        for (int i = choices.size() - 1; i > 0; --i) {
            int swap = this.m_217043_().m_188503_(i + 1);
            Item item = choices.get(i);
            choices.set(i, choices.get(swap));
            choices.set(swap, item);
        }
        for (int i = 0; i < dropCount; ++i) {
            this.m_19983_(new ItemStack(choices.get(i)));
        }
    }

    private void dropConfiguredDeathEntry(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        String trimmed = entry.trim();
        if (trimmed.startsWith("#")) {
            return;
        }
        String idText = trimmed;
        int count = 1;
        int lastColon = idText.lastIndexOf(':');
        int firstColon = idText.indexOf(':');
        if (lastColon > firstColon) {
            String suffix = idText.substring(lastColon + 1);
            if (isDropCountSpec(suffix)) {
                idText = idText.substring(0, lastColon);
                count = this.randomDropCount(suffix);
            }
        }
        ResourceLocation id = ResourceLocation.m_135820_(idText);
        if (id == null) {
            return;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item != null && item != Items.f_42416_) {
            this.m_19983_(new ItemStack(item,
                    Mth.m_14045_(count, 1, item.m_41459_())));
        }
    }

    private static boolean isDropCountSpec(String value) {
        return value.matches("\\d+") || value.matches("\\d+-\\d+");
    }

    private int randomDropCount(String value) {
        if (!value.contains("-")) {
            return parsePositiveInt(value, 1);
        }
        String[] parts = value.split("-", 2);
        int min = parsePositiveInt(parts[0], 1);
        int max = parsePositiveInt(parts[1], min);
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
        return min + this.m_217043_().m_188503_(max - min + 1);
    }

    private static int parsePositiveInt(String value, int fallback) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    protected void m_6153_() {
        ++this.f_20919_;
        this.setCasting(true);
        this.m_20256_(Vec3.f_82478_);
        this.f_19812_ = true;
        if (this.m_9236_().f_46443_) {
            return;
        }

        if (ApollyonDeathEffects.tickApollyon(this, this.f_20919_, this.deathGroundY)) {
            ApollyonDeathEffects.explodeApollyon(this);
            this.m_9236_().m_7605_(this, (byte) 60);
            this.m_142687_(Entity.RemovalReason.KILLED);
        }
    }

    public boolean isPlayingDeathAnimation() {
        return this.f_20919_ > 0 || (this.m_21223_() <= 0.0F && this.isCasting());
    }

    public int getDeathAnimationTicks() {
        return this.f_20919_;
    }

    public int getDeathAnimationDurationTicks() {
        return DEATH_ANIMATION_TICKS;
    }

    /** Mirrors Goety Apostle's absolute, configurable post-hit invulnerability window. */
    @Override
    protected void m_6475_(DamageSource source, float amount) {
        boolean shielded = this.hasCooperativeShield() || this.shieldHitInProgress;
        if (this.healthProtection.invulnerabilityTicks() > 0 && !shielded) {
            return;
        }
        // Direct actuallyHurt calls must respect the encounter's existing protection too.
        if (this.pageant.isInvulnerable() || this.isMonolithPower()) return;
        ++this.damageProcessingDepth;
        try {
            super.m_6475_(source, amount);
        } finally {
            --this.damageProcessingDepth;
        }
        if (!shielded && source.m_7639_() != null) {
            this.healthProtection.setInvulnerabilityTicks(ApollyonConfig.bossInvulnerabilityTime());
        }
    }

    @Override
    public com.starfantasy.library.combat.CombatHealthProtection combatHealthProtection() {
        return this.healthProtection;
    }

    @Override
    public double combatDamageCap() { return ApollyonConfig.damageCap(); }

    @Override
    public int combatHitInvulnerabilityTicks() { return ApollyonConfig.bossInvulnerabilityTime(); }

    @Override
    public boolean combatHealthLocked() {
        return this.pageant.isInvulnerable() || this.isMonolithPower();
    }

    @Override
    public boolean bypassCombatHitImmunity() {
        return this.hasCooperativeShield() || this.shieldHitInProgress;
    }

    @Override
    public float absorbCombatHealthLoss(float loss) {
        // LivingDamageEvent already absorbed the shield on the normal damage path.
        return this.damageProcessingDepth == 0 ? this.absorbCooperativeShield(null, loss) : loss;
    }

    @Override
    public boolean handleCombatHealthLoss(float loss) {
        return this.tryStartPageantFromFinalDamage(loss);
    }

    @Override
    public boolean m_142535_(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean m_7301_(MobEffectInstance effect) {
        if (effect.m_19544_().m_19483_() == MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.m_7301_(effect);
    }

    public boolean isCastingAction() {
        return this.isCasting();
    }

    public float applyIncomingDamageReductions(DamageSource source, float amount) {
        if (this.m_9236_().f_46443_ || amount <= 0.0F) {
            return amount;
        }
        if (source != null && source.m_269533_(DamageTypeTags.f_268731_)) {
            double magicReduction = Mth.m_14008_(
                    ApollyonConfig.magicDamageReduction(), -1.0D, 1.0D);
            amount = (float) (amount * (1.0D - magicReduction));
        }
        double fixedReduction = Mth.m_14008_(
                ApollyonConfig.fixedDamageReduction(), 0.0D, 1.0D);
        amount = (float) (amount * (1.0D - fixedReduction));
        if (!this.isCasting()) {
            double multiplier = Mth.m_14008_(
                    ApollyonConfig.damageTakenMultiplier(), 0.0D, 1000.0D);
            amount = (float) (amount * multiplier);
        }
        return amount;
    }

    public float clampFinalDamage(float amount) {
        if (amount <= 0.0F) {
            return amount;
        }
        double cap = ApollyonConfig.damageCap();
        return cap <= 0.0D
                ? amount
                : Math.min(amount, (float) Mth.m_14008_(cap, 0.0D, 100000.0D));
    }

    /** Absorbs already-mitigated damage with the shared boss shield. */
    public float absorbCooperativeShield(DamageSource source, float finalDamage) {
        float shield = this.getCooperativeShield();
        if (this.m_9236_().f_46443_ || finalDamage <= 0.0F || shield <= 0.0F) {
            return finalDamage;
        }
        // Goety spell damage types use the vanilla witch-resistant tag, which is
        // also the existing definition of magic damage for Apollyon's config.
        float costMultiplier = source != null
                && source.m_269533_(DamageTypeTags.f_268731_)
                ? MAGIC_SHIELD_COST_MULTIPLIER : 1.0F;
        float absorbedDamage = Math.min(finalDamage, shield / costMultiplier);
        this.setCooperativeShield(shield - absorbedDamage * costMultiplier);
        return Math.max(0.0F, finalDamage - absorbedDamage);
    }

    public float getCooperativeShield() {
        return this.f_19804_.m_135370_(COOPERATIVE_SHIELD);
    }

    public boolean hasCooperativeShield() {
        return this.getCooperativeShield() > 0.001F;
    }

    private void setCooperativeShield(float amount) {
        this.f_19804_.m_135381_(COOPERATIVE_SHIELD, Math.max(0.0F, amount));
        if (this.hasCooperativeShield()) {
            this.f_19802_ = 0;
            this.healthProtection.setInvulnerabilityTicks(0);
        }
        this.updateShieldKnockbackResistance();
    }

    public void addMonolithShield() {
        if (!this.m_9236_().f_46443_ && this.m_6084_()) {
            this.setCooperativeShield(Math.min(this.m_21233_() * COOPERATIVE_SHIELD_HEALTH_RATIO,
                    this.getCooperativeShield() + this.m_21233_() * 0.05F));
        }
    }

    private void updateShieldKnockbackResistance() {
        if (this.m_9236_().f_46443_) return;
        AttributeInstance resistance = this.m_21051_(Attributes.f_22278_);
        if (resistance == null) return;
        boolean present = resistance.m_22111_(SHIELD_KNOCKBACK_MODIFIER) != null;
        // Keep the modifier through knockback processing even when this hit breaks the shield.
        if (this.hasCooperativeShield() || this.shieldHitInProgress) {
            if (!present) resistance.m_22118_(new AttributeModifier(SHIELD_KNOCKBACK_MODIFIER,
                    "starfantasy_goety.shield_knockback_resistance", 1.0D, AttributeModifier.Operation.ADDITION));
        } else if (present) {
            resistance.m_22120_(SHIELD_KNOCKBACK_MODIFIER);
        }
    }

    private void activatePhaseTwoShield() {
        this.setCooperativeShield(this.m_21233_() * COOPERATIVE_SHIELD_HEALTH_RATIO);
    }

    public float scaleOutgoingDamage(float baseDamage) {
        if (baseDamage <= 0.0F) {
            return baseDamage;
        }
        double multiplier = Mth.m_14008_(
                ApollyonConfig.damageMultiplier(), 0.0D, 1000.0D);
        return (float) (baseDamage * multiplier);
    }

    public boolean isFriendlyEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == this) {
            return true;
        }
        if (entity instanceof Owned owned && owned.getTrueOwner() == this) {
            return true;
        }
        if (entity instanceof ApollyonPageantOwned owned
                && this.m_20148_().equals(owned.pageantOwnerUuid())) {
            return true;
        }
        if (entity instanceof Projectile projectile) {
            Entity owner = projectile.m_19749_();
            if (owner != entity && this.isFriendlyEntity(owner)) {
                return true;
            }
        }
        return entity instanceof LivingEntity living && MobUtil.areAllies(this, living);
    }

    private boolean isFriendlyDamageSource(DamageSource source) {
        return source != null
                && (this.isFriendlyEntity(source.m_7639_())
                || this.isFriendlyEntity(source.m_7640_()));
    }

    private void applyConfiguredAttributes(boolean healToFull) {
        this.healthProtection.beginRestore();
        try {
            this.f_21364_ = ApollyonConfig.deathExperience();
            double configuredHealth = Mth.m_14008_(
                    ApollyonConfig.bossHealth(), 1.0D, 100000.0D);
            AttributeInstance maxHealth = this.m_21051_(Attributes.f_22276_);
            if (maxHealth != null) {
                double oldMaxHealth = Math.max(1.0D, this.m_21233_());
                float currentHealth = this.m_21223_();
                boolean wasFull = currentHealth >= oldMaxHealth - 0.5D;
                // Apply our base on creation/config changes, preserving other mods' later edits.
                boolean configChanged = Double.compare(lastConfiguredHealth, configuredHealth) != 0;
                if (configChanged) {
                    maxHealth.m_22100_(configuredHealth);
                    lastConfiguredHealth = configuredHealth;
                }
                float effectiveMaxHealth = this.m_21233_();
                if (healToFull || (configChanged && wasFull) || currentHealth > effectiveMaxHealth) {
                    this.m_21153_(effectiveMaxHealth);
                }
            }

            double configuredArmor = Mth.m_14008_(
                    ApollyonConfig.armor(), 0.0D, 1000.0D);
            AttributeInstance armor = this.m_21051_(Attributes.f_22284_);
            if (armor != null && Math.abs(armor.m_22115_() - configuredArmor) > 1.0E-4D) {
                armor.m_22100_(configuredArmor);
            }
            AttributeInstance toughness = this.m_21051_(Attributes.f_22285_);
            if (toughness != null
                    && Math.abs(toughness.m_22115_() - configuredArmor) > 1.0E-4D) {
                toughness.m_22100_(configuredArmor);
            }
        } finally {
            this.healthProtection.endRestore();
        }
    }

    @Override
    public void m_5634_(float amount) {
        if (!this.isSmited()) {
            super.m_5634_(amount);
        }
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

    @Override
    public void m_6593_(Component name) {
        super.m_6593_(name);
        this.bossInfo.m_6456_(this.m_5446_());
    }

    @Override
    public void m_6457_(ServerPlayer player) {
        super.m_6457_(player);
        this.bossInfo.m_6543_(player);
    }

    @Override
    public void m_6452_(ServerPlayer player) {
        super.m_6452_(player);
        this.bossInfo.m_6539_(player);
    }

    @Override
    protected boolean m_6129_() {
        return false;
    }

    public int getAntiRegen() {
        return this.f_19804_.m_135370_(ANTI_REGEN);
    }

    public int getAntiRegenTotal() {
        return this.f_19804_.m_135370_(ANTI_REGEN_TOTAL);
    }

    public boolean isSmited() {
        return this.getAntiRegen() > 0;
    }

    public boolean isMonolithPower() {
        return this.f_19804_.m_135370_(MONOLITH_POWER);
    }

    public boolean shouldRedirectAttackersToMonoliths() {
        // Match Goety Apostle's obsidianInvul > 5 targeting condition.
        return this.monolithPowerTicks > 5;
    }

    public void refreshMonolithPower() {
        if (!this.m_9236_().f_46443_) {
            this.monolithPowerTicks = 10;
            this.setMonolithPower(true);
        }
    }

    private void setMonolithPower(boolean powered) {
        if (this.f_19804_.m_135370_(MONOLITH_POWER) != powered) {
            this.f_19804_.m_135381_(MONOLITH_POWER, powered);
        }
    }

    private void setAntiRegen(int ticks, int totalTicks) {
        int clampedTicks = Math.max(0, ticks);
        this.f_19804_.m_135381_(ANTI_REGEN, clampedTicks);
        this.f_19804_.m_135381_(ANTI_REGEN_TOTAL,
                clampedTicks > 0 ? Math.max(1, totalTicks) : Math.max(0, totalTicks));
    }

    @Override
    public CultistArmPose getArmPose() {
        if (this.isPlayingDeathAnimation()) {
            return CultistArmPose.SPELL_AND_WEAPON;
        }
        if (this.m_21224_()) {
            return CultistArmPose.DYING;
        }
        if (this.getPageantState() == ApollyonPageantController.FADE_OUT) {
            return CultistArmPose.SPELL_AND_WEAPON;
        }
        if (this.isCasting()) {
            return CultistArmPose.SPELL_AND_WEAPON;
        }
        if (this.m_6117_() || this.m_5912_()) {
            return CultistArmPose.BOW_AND_ARROW;
        }
        return CultistArmPose.CROSSED;
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        this.healthProtection.beginRestore();
        try {
            super.m_7378_(tag);
            // Old saves have no marker; retain their saved attributes until the config changes.
            if (tag.m_128441_(CONFIGURED_HEALTH_TAG)) {
                this.lastConfiguredHealth = tag.m_128459_(CONFIGURED_HEALTH_TAG);
            }
            this.applyConfiguredAttributes(false);
            this.multishotCooldown = Math.max(0, tag.m_128451_(MULTISHOT_COOLDOWN_TAG));
            this.spellCooldown = Math.max(0, tag.m_128451_(SPELL_COOLDOWN_TAG));
            this.setCooperativeShield(Math.max(0.0F, tag.m_128457_(COOPERATIVE_SHIELD_TAG)));
            this.arrowCooldown = Math.max(0, tag.m_128451_(ARROW_COOLDOWN_TAG));
            this.attackDecisionCooldown = Math.max(0, tag.m_128451_(ATTACK_DECISION_COOLDOWN_TAG));
            this.meteorCooldown = Math.max(0, tag.m_128451_(METEOR_COOLDOWN_TAG));
            this.setAntiRegen(
                    Math.max(0, tag.m_128451_(ANTI_REGEN_TAG)),
                    Math.max(0, tag.m_128451_(ANTI_REGEN_TOTAL_TAG)));
            this.monolithPowerTicks = Math.max(0, tag.m_128451_(MONOLITH_POWER_TICKS_TAG));
            this.healthProtection.setInvulnerabilityTicks(tag.m_128451_(BOSS_INVULNERABILITY_TICKS_TAG));
            this.phaseTwoHalfHealthEnraged = tag.m_128441_(PHASE_TWO_HALF_HEALTH_ENRAGED_TAG)
                    ? tag.m_128471_(PHASE_TWO_HALF_HEALTH_ENRAGED_TAG)
                    : this.isCombatPhaseTwo() && this.m_21223_() <= this.m_21233_() * 0.5F;
            this.setMonolithPower(this.monolithPowerTicks > 0);
            if (this.m_8077_()) {
                this.bossInfo.m_6456_(this.m_5446_());
            }
            if (tag.m_128441_(HOME_X_TAG)
                    && tag.m_128441_(HOME_Y_TAG)
                    && tag.m_128441_(HOME_Z_TAG)) {
                this.setArenaHome(new Vec3(
                        tag.m_128459_(HOME_X_TAG),
                        tag.m_128459_(HOME_Y_TAG),
                        tag.m_128459_(HOME_Z_TAG)));
            } else {
                // /summon loads custom NBT before its command callback moves the entity
                // to the requested coordinates. Defer a missing home until the first
                // server tick (or incoming lethal hit), when that final position exists.
                this.arenaHomeInitialized = false;
            }
            boolean hasExplicitCombatPhase = tag.m_128441_(COMBAT_PHASE_TAG);
            if (hasExplicitCombatPhase) {
                this.setCombatPhase(tag.m_128451_(COMBAT_PHASE_TAG));
                // A direct phase-two /summon has no persisted home. Run the canonical
                // transition initializer on its first server tick, after the command
                // has applied the final summon position.
                this.phaseTwoInitializationPending = this.isCombatPhaseTwo()
                        && !tag.m_128441_(HOME_X_TAG)
                        && !tag.m_128441_(HOME_Y_TAG)
                        && !tag.m_128441_(HOME_Z_TAG);
            } else {
                this.setCombatPhase(COMBAT_PHASE_ONE);
                this.phaseTwoInitializationPending = false;
            }
            this.pageant.read(tag, hasExplicitCombatPhase);
            if (!hasExplicitCombatPhase && this.isCombatPhaseTwo()
                    && !tag.m_128441_(HOME_X_TAG)
                    && !tag.m_128441_(HOME_Y_TAG)
                    && !tag.m_128441_(HOME_Z_TAG)) {
                this.phaseTwoInitializationPending = true;
            }
        } finally {
            this.healthProtection.endRestore();
        }
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        tag.m_128347_(CONFIGURED_HEALTH_TAG, this.lastConfiguredHealth);
        tag.m_128405_(MULTISHOT_COOLDOWN_TAG, this.multishotCooldown);
        tag.m_128405_(SPELL_COOLDOWN_TAG, this.spellCooldown);
        tag.m_128350_(COOPERATIVE_SHIELD_TAG, this.getCooperativeShield());
        tag.m_128405_(ARROW_COOLDOWN_TAG, this.arrowCooldown);
        tag.m_128405_(ATTACK_DECISION_COOLDOWN_TAG, this.attackDecisionCooldown);
        tag.m_128405_(METEOR_COOLDOWN_TAG, this.meteorCooldown);
        tag.m_128405_(ANTI_REGEN_TAG, this.getAntiRegen());
        tag.m_128405_(ANTI_REGEN_TOTAL_TAG, this.getAntiRegenTotal());
        tag.m_128405_(MONOLITH_POWER_TICKS_TAG, this.monolithPowerTicks);
        tag.m_128405_(BOSS_INVULNERABILITY_TICKS_TAG, this.healthProtection.invulnerabilityTicks());
        tag.m_128405_(COMBAT_PHASE_TAG, this.getCombatPhase());
        tag.m_128379_(PHASE_TWO_HALF_HEALTH_ENRAGED_TAG,
                this.phaseTwoHalfHealthEnraged);
        this.ensureArenaHome();
        tag.m_128347_(HOME_X_TAG, this.arenaHome.f_82479_);
        tag.m_128347_(HOME_Y_TAG, this.arenaHome.f_82480_);
        tag.m_128347_(HOME_Z_TAG, this.arenaHome.f_82481_);
        this.pageant.write(tag);
    }

    public boolean isArenaActive() {
        return this.f_19804_.m_135370_(ARENA_ACTIVE);
    }

    public Vec3 arenaHomePosition() {
        return new Vec3(
                this.f_19804_.m_135370_(HOME_X),
                this.f_19804_.m_135370_(HOME_Y),
                this.f_19804_.m_135370_(HOME_Z));
    }

    private void ensureArenaHome() {
        if (!this.arenaHomeInitialized) {
            this.setArenaHome(this.m_20182_());
        }
    }

    private void setArenaHome(Vec3 home) {
        this.arenaHome = home == null ? this.m_20182_() : home;
        this.arenaHomeInitialized = true;
        this.f_19804_.m_135381_(HOME_X, (float) this.arenaHome.f_82479_);
        this.f_19804_.m_135381_(HOME_Y, (float) this.arenaHome.f_82480_);
        this.f_19804_.m_135381_(HOME_Z, (float) this.arenaHome.f_82481_);
    }

    private void setArenaActive(boolean active) {
        if (this.isArenaActive() == active) {
            return;
        }
        this.f_19804_.m_135381_(ARENA_ACTIVE, active);
        if (!active) {
            this.arenaPlayers.clear();
            this.safeBoundaryKnockbackReadyTicks.clear();
        }
    }

    private void tickArenaState(LivingEntity target) {
        this.ensureArenaHome();
        if (target != null && !this.isValidArenaTarget(target)) {
            this.clearDistantAggro();
            target = null;
        }
        boolean wasActive = this.isArenaActive();
        boolean active = target != null && target.m_6084_();

        this.setArenaActive(active);
        // Only the current non-player target needs checking; never scan nearby mobs.
        UUID currentMobUuid = active && !(target instanceof Player) ? target.m_20148_() : null;
        if (!java.util.Objects.equals(this.boundaryMobUuid, currentMobUuid)) {
            if (this.boundaryMobUuid != null) {
                this.safeBoundaryKnockbackReadyTicks.remove(this.boundaryMobUuid);
            }
            this.boundaryMobUuid = currentMobUuid;
        }
        if (currentMobUuid != null) {
            this.constrainArenaEntity(target);
        }
        if (!active) {
            if (this.f_19797_ % HOME_RETURN_CHECK_TICKS == 0
                    && !this.pageant.keepsBossHidden()
                    && this.m_20182_().m_82554_(this.arenaHomePosition()) > HOME_RETURN_DISTANCE_SQR) {
                this.teleportHome();
            }
            return;
        }

        if (!this.isSelfPositionInsideArena(this.m_20182_())) {
            this.teleportHome();
        }
        if (!wasActive) {
            this.applyArenaMembership();
            this.constrainArenaPlayers();
        }
        if (this.m_9236_() instanceof ServerLevel level) {
            com.starfantasy.goety.combat.ApollyonArenaFlight.tick(
                    level, this.arenaHomePosition(), ARENA_RADIUS, ARENA_DISENGAGE_DISTANCE_SQR);
        }
    }

    private void prepareArenaPlayersForPageant() {
        this.ensureArenaHome();
        if (!this.isArenaActive() && !this.pageant.isCombatLocked()) {
            return;
        }
        // Membership is resolved before the pageant decides whether the arena is
        // empty. A tracked player who was knocked beyond radius 20 is constrained
        // first and remains a participant while within the 48-block combat range.
        // Players beyond that range leave the encounter without being pulled back.
        this.applyArenaMembership();
        this.constrainArenaPlayers();
    }

    private boolean canEnterArena(LivingEntity target) {
        Vec3 home = this.arenaHomeInitialized ? this.arenaHomePosition() : this.m_20182_();
        double dx = target.m_20185_() - home.f_82479_;
        double dz = target.m_20189_() - home.f_82481_;
        return dx * dx + dz * dz <= ARENA_RADIUS * ARENA_RADIUS
                && Math.abs(target.m_20186_() - home.f_82480_) <= ARENA_ENTRY_HEIGHT;
    }

    public boolean isValidArenaTarget(LivingEntity target) {
        if (target == null || !target.m_6084_() || !this.isWithinArenaCombatRange(target)
                || target instanceof Player player && (player.m_7500_() || player.m_5833_())) {
            return false;
        }
        boolean alreadyJoined = target instanceof ServerPlayer player
                ? this.arenaPlayers.get(player.m_20148_()) == player
                : target == this.m_5448_();
        return alreadyJoined || this.canEnterArena(target);
    }

    private boolean isArenaDamageSource(DamageSource source) {
        Entity origin = source.m_7639_();
        if (origin == null) {
            origin = source.m_7640_();
        }
        // Resolve projectile shooters and Goety summon owners before using position.
        // Bound traversal also handles malformed ownership cycles without hanging a tick.
        for (int depth = 0; origin != null && depth < 8; ++depth) {
            Entity owner = origin instanceof Projectile projectile ? projectile.m_19749_()
                    : origin instanceof Owned owned ? owned.getTrueOwner() : null;
            if (owner == null || owner == origin) {
                break;
            }
            origin = owner;
        }
        Vec3 position = origin == null ? source.m_7270_() : origin.m_20182_();
        if (position == null || origin != null && origin.m_9236_() != this.m_9236_()) {
            return false;
        }
        this.ensureArenaHome();
        return this.isHorizontalPositionInsideArena(position, ARENA_RADIUS);
    }

    public boolean isWithinArenaCombatRange(LivingEntity target) {
        Vec3 home = this.arenaHomeInitialized ? this.arenaHomePosition() : this.m_20182_();
        return target != null && target.m_9236_() == this.m_9236_()
                && target.m_20182_().m_82557_(home) <= ARENA_DISENGAGE_DISTANCE_SQR;
    }

    @Override
    public boolean m_6779_(LivingEntity target) {
        return this.isValidArenaTarget(target) && super.m_6779_(target);
    }

    @Override
    public void m_6710_(LivingEntity target) {
        // Covers retaliation and cultist alerts as well as ordinary nearest-player AI.
        if (target == null || this.isValidArenaTarget(target)) {
            super.m_6710_(target);
        }
    }

    private void clearDistantAggro() {
        LivingEntity target = this.m_5448_();
        if (target != null && !this.isValidArenaTarget(target)) {
            this.m_6710_(null);
            this.m_21573_().m_26573_();
            this.clearPendingTeleport();
        }
        LivingEntity attacker = this.m_21188_();
        if (attacker != null && !this.isValidArenaTarget(attacker)) {
            this.m_6703_(null);
        }
        LivingEntity victim = this.m_21214_();
        if (victim != null && !this.isValidArenaTarget(victim)) {
            this.m_21335_(null);
        }
        if (this.f_20888_ != null && !this.isValidArenaTarget(this.f_20888_)) {
            this.m_6598_(null);
        }
    }

    private void applyArenaMembership() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Set<UUID> presentPlayers = new HashSet<>();
        for (ServerPlayer player : level.m_6907_()) {
            if (!player.m_6084_() || player.m_7500_() || player.m_5833_()) {
                continue;
            }
            presentPlayers.add(player.m_20148_());
            if (this.arenaPlayers.get(player.m_20148_()) != player) {
                this.arenaPlayers.remove(player.m_20148_());
                this.safeBoundaryKnockbackReadyTicks.remove(player.m_20148_());
            }
            if (this.canEnterArena(player)) {
                this.arenaPlayers.put(player.m_20148_(), player);
            }
        }
        this.arenaPlayers.keySet().retainAll(presentPlayers);
    }

    private void constrainArenaPlayers() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Iterator<Map.Entry<UUID, ServerPlayer>> iterator = this.arenaPlayers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ServerPlayer> entry = iterator.next();
            UUID playerUuid = entry.getKey();
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(playerUuid);
            if (player == null || player != entry.getValue() || player.m_9236_() != level) {
                iterator.remove();
                this.safeBoundaryKnockbackReadyTicks.remove(playerUuid);
                continue;
            }
            if (!player.m_6084_() || player.m_7500_() || player.m_5833_()
                    || !this.isWithinArenaCombatRange(player)) {
                iterator.remove();
                this.safeBoundaryKnockbackReadyTicks.remove(playerUuid);
                continue;
            }
            this.constrainArenaEntity(player);
        }
    }

    private void constrainArenaEntity(LivingEntity player) {
        if (!this.isWithinArenaCombatRange(player)) {
            return;
        }
        Vec3 home = this.arenaHomePosition();
        double offsetX = player.m_20185_() - home.f_82479_;
        double offsetZ = player.m_20189_() - home.f_82481_;
        double distanceSqr = offsetX * offsetX + offsetZ * offsetZ;
        if (distanceSqr <= ARENA_RADIUS * ARENA_RADIUS) {
            return;
        }
        double distance = Math.sqrt(distanceSqr);
        double normalX = offsetX / distance;
        double normalZ = offsetZ / distance;

        Vec3 velocity = player.m_20184_();
        double velocityX = velocity.f_82479_;
        double velocityZ = velocity.f_82481_;
        double outwardVelocity = velocityX * normalX + velocityZ * normalZ;
        if (outwardVelocity > 0.0D) {
            velocityX -= normalX * outwardVelocity;
            velocityZ -= normalZ * outwardVelocity;
        }

        double boundaryX = home.f_82479_ + normalX * ARENA_RADIUS;
        double boundaryZ = home.f_82481_ + normalZ * ARENA_RADIUS;
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.f_8906_.m_9774_(boundaryX, player.m_20186_(), boundaryZ,
                    player.m_146908_(), player.m_146909_());
        } else {
            player.m_6021_(boundaryX, player.m_20186_(), boundaryZ);
        }
        if (!this.isPageantBoundaryLethal()) {
            long now = player.m_9236_().m_46467_();
            long readyTick = this.safeBoundaryKnockbackReadyTicks.getOrDefault(
                    player.m_20148_(), Long.MIN_VALUE);
            if (now >= readyTick) {
                velocityX -= normalX * SAFE_BOUNDARY_KNOCKBACK_HORIZONTAL;
                velocityZ -= normalZ * SAFE_BOUNDARY_KNOCKBACK_HORIZONTAL;
                velocity = new Vec3(
                        velocityX,
                        Math.max(velocity.f_82480_, SAFE_BOUNDARY_KNOCKBACK_VERTICAL),
                        velocityZ);
                this.safeBoundaryKnockbackReadyTicks.put(
                        player.m_20148_(), now + SAFE_BOUNDARY_KNOCKBACK_INTERVAL_TICKS);
            } else {
                velocity = new Vec3(velocityX, velocity.f_82480_, velocityZ);
            }
        } else {
            velocity = new Vec3(velocityX, velocity.f_82480_, velocityZ);
        }
        player.m_20256_(velocity);
        player.f_19789_ = 0.0F;
        player.f_19864_ = true;
        player.f_19812_ = true;

        if (this.isPageantBoundaryLethal()) {
            if (ApollyonConfig.hardMode()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    StarFantasyTrueKillHelper.trueKillPlayer(
                            serverPlayer, Float.MAX_VALUE, "apollyon_boundary");
                } else {
                    player.m_6074_();
                }
            } else {
                // A sustained outward push must not deal void damage every tick.
                long now = player.m_9236_().m_46467_();
                long readyTick = this.safeBoundaryKnockbackReadyTicks.getOrDefault(
                        player.m_20148_(), Long.MIN_VALUE);
                if (now >= readyTick) {
                    this.safeBoundaryKnockbackReadyTicks.put(player.m_20148_(), now + 20);
                    player.f_19802_ = 0;
                    player.m_6469_(player.m_269291_().m_269341_(), 20.0F);
                    if (player.m_6084_()) {
                        player.m_147207_(new MobEffectInstance(
                                com.Polarice3.Goety.common.effects.GoetyEffects.DOOM.get(), 20, 9), this);
                    }
                }
            }
        }
    }

    private void tickAmbienceMeteor(LivingEntity target) {
        if (target == null || !target.m_6084_()) {
            this.meteorCooldown = 0;
            return;
        }
        if (this.meteorCooldown > 0) {
            --this.meteorCooldown;
            return;
        }
        ApollyonMeteorManager.spawn(this);
        int interval = this.phaseTwoHalfHealthEnraged
                ? PHASE_TWO_ENRAGED_METEOR_INTERVAL_TICKS
                : METEOR_INTERVAL_TICKS;
        if (ApollyonConfig.hardMode()) {
            interval = this.phaseTwoHalfHealthEnraged ? 5 : 10;
        }
        this.meteorCooldown = interval - 1;
    }

    private void tickPhaseTwoHalfHealthState() {
        if (this.phaseTwoHalfHealthEnraged || !this.isCombatPhaseTwo()
                || this.m_21223_() > this.m_21233_() * 0.5F) {
            return;
        }
        this.phaseTwoHalfHealthEnraged = true;
    }

    private boolean isCasting() {
        return this.f_19804_.m_135370_(CASTING);
    }

    private void setCasting(boolean casting) {
        this.f_19804_.m_135381_(CASTING, casting);
    }

    public boolean tryStartPageantFromFinalDamage(float finalDamage) {
        this.ensureArenaHome();
        return this.pageant.tryStartFromFinalDamage(finalDamage);
    }

    public boolean isPageantCombatLocked() {
        return this.pageant.isCombatLocked();
    }

    @Override
    public ApollyonPageantApostleEntity pageantRedirectTarget(LivingEntity attacker) {
        return this.pageant.redirectTarget(attacker);
    }

    public boolean isPageantBoundaryLethal() {
        // Rendering runs client-side, where the controller itself does not advance.
        // PAGEANT_STATE is synced and is therefore the authoritative visual state.
        int syncedState = this.getPageantState();
        return this.isCombatPhaseTwo()
                || syncedState == ApollyonPageantController.SUMMONING
                || syncedState == ApollyonPageantController.FIRST_TRIO
                || syncedState == ApollyonPageantController.GLORIOUS_STAGE
                || syncedState == ApollyonPageantController.SECOND_TRIO
                || syncedState == ApollyonPageantController.THIRD_DPS
                || syncedState == ApollyonPageantController.FOURTH_STAGE
                || syncedState == ApollyonPageantController.FIFTH_STAGE;
    }

    public int getPageantState() {
        return this.f_19804_.m_135370_(PAGEANT_STATE);
    }

    public int getCombatPhase() {
        return this.f_19804_.m_135370_(COMBAT_PHASE);
    }

    public boolean isCombatPhaseOne() {
        return this.getCombatPhase() == COMBAT_PHASE_ONE;
    }

    public boolean isCombatPhaseTwo() {
        return this.getCombatPhase() == COMBAT_PHASE_TWO;
    }

    public void setCombatPhase(int phase) {
        int normalizedPhase = phase == COMBAT_PHASE_TWO
                ? COMBAT_PHASE_TWO : COMBAT_PHASE_ONE;
        this.f_19804_.m_135381_(COMBAT_PHASE, normalizedPhase);
        if (normalizedPhase != COMBAT_PHASE_TWO) {
            this.phaseTwoHalfHealthEnraged = false;
        }
    }

    public void setPageantState(int state) {
        this.f_19804_.m_135381_(PAGEANT_STATE, state);
    }

    public float getPageantOpacity() {
        return this.f_19804_.m_135370_(PAGEANT_OPACITY);
    }

    public void setPageantOpacity(float opacity) {
        this.f_19804_.m_135381_(PAGEANT_OPACITY, Mth.m_14036_(opacity, 0.0F, 1.0F));
    }

    public List<ServerPlayer> validArenaPlayers() {
        List<ServerPlayer> players = new ArrayList<>();
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return players;
        }
        for (ServerPlayer player : this.arenaPlayers.values()) {
            if (level.m_7654_().m_6846_().m_11259_(player.m_20148_()) == player
                    && player.m_9236_() == level
                    && player.m_6084_() && !player.m_7500_() && !player.m_5833_()
                    && this.isWithinArenaCombatRange(player)) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public boolean isInventoryProtectedParticipant(UUID player) {
        // A dying player is no longer alive, so validArenaPlayers() would exclude them.
        // The current target also covers a lethal opening hit before the first membership tick.
        LivingEntity target = this.m_5448_();
        return (this.isArenaActive() || this.pageant.isCombatLocked() || target != null)
                && (this.arenaPlayers.containsKey(player)
                    || target instanceof ServerPlayer && target.m_20148_().equals(player));
    }

    public void clearCombatForPageant() {
        this.setCasting(false);
        this.setVoidRayMovementLocked(false);
        this.clearPendingTeleport();
        this.m_8061_(EquipmentSlot.MAINHAND, ItemStack.f_41583_);
        this.m_21195_(ApollyonEffectRegistry.MULTISHOT.get());
        this.monolithPowerTicks = 0;
        this.setMonolithPower(false);
        this.m_21573_().m_26573_();
        this.m_5810_();
        for (GroundRectangleWarningEntity warning : this.shotWarnings) {
            if (warning != null && warning.m_6084_()) {
                warning.m_146870_();
            }
        }
        this.shotWarnings.clear();
        ApollyonFireTrapManager.clearForBoss(this);
        ApollyonLightningStormManager.clearForBoss(this);
        if (this.m_9236_() instanceof ServerLevel level) {
            AABB area = new AABB(this.arenaHomePosition(), this.arenaHomePosition())
                    .m_82400_(128.0D);
            for (ApollyonStarArrowEntity arrow : level.m_45976_(ApollyonStarArrowEntity.class, area)) {
                if (arrow.m_19749_() == this) {
                    arrow.m_146870_();
                }
            }
            for (ApollyonIceChunkEntity chunk : level.m_45976_(ApollyonIceChunkEntity.class, area)) {
                if (chunk.m_269323_() == this) {
                    chunk.m_146870_();
                }
            }
            for (ApollyonCastingLightningEntity lightning
                    : level.m_45976_(ApollyonCastingLightningEntity.class, area)) {
                lightning.m_146870_();
            }
            for (ApollyonSectorEffectEntity sector
                    : level.m_45976_(ApollyonSectorEffectEntity.class, area)) {
                sector.m_146870_();
            }
            for (Owned summon : level.m_45976_(Owned.class, area)) {
                if (summon.getTrueOwner() == this) {
                    summon.m_146870_();
                }
            }
        }
        StarFantasyVfx.clearWarningsForOwner(this);
    }

    public void holdForPageantTransition() {
        this.m_6842_(false);
        this.m_21573_().m_26573_();
        this.m_5810_();
        this.setCasting(true);
        if (!(this.m_21205_().m_41720_() instanceof BowItem)) {
            this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        }
        Vec3 movement = this.m_20184_();
        this.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
    }

    private void showOriginalApostleTransitionSmoke() {
        for (int i = 0; i < 40; ++i) {
            double velocityX = this.m_217043_().m_188583_() * 0.2D;
            double velocityY = this.m_217043_().m_188583_() * 0.2D;
            double velocityZ = this.m_217043_().m_188583_() * 0.2D;
            this.m_9236_().m_7107_(ParticleTypes.f_123755_,
                    this.m_20185_(), this.m_20186_() + 0.5D, this.m_20189_(),
                    velocityX, velocityY, velocityZ);
        }
    }

    public void holdHiddenForPageant() {
        Vec3 home = this.arenaHomePosition();
        this.m_6842_(true);
        this.m_21573_().m_26573_();
        this.m_5810_();
        this.setCasting(false);
        this.m_20242_(true);
        this.m_6034_(home.f_82479_, home.f_82480_ + 32.0D, home.f_82481_);
        this.m_20256_(Vec3.f_82478_);
    }

    public void snapHiddenForPageant() {
        Vec3 home = this.arenaHomePosition();
        Vec3 hidden = new Vec3(home.f_82479_, home.f_82480_ + 32.0D, home.f_82481_);
        this.m_6842_(true);
        this.setCasting(false);
        this.m_20242_(true);
        this.m_19890_(hidden.f_82479_, hidden.f_82480_, hidden.f_82481_,
                this.m_146908_(), this.m_146909_());
        this.m_20256_(Vec3.f_82478_);
        StarFantasyGoetyNetwork.snapEntity(
                this, hidden, this.m_146908_(), this.m_146909_());
    }

    public void beginFinalPageantReturn() {
        Vec3 home = this.arenaHomePosition();
        this.m_6842_(false);
        this.m_20242_(false);
        this.setPageantOpacity(1.0F);
        this.setCasting(true);
        this.setSouthFacing();
        this.m_19890_(home.f_82479_, home.f_82480_, home.f_82481_, 0.0F, 0.0F);
        this.m_20256_(Vec3.f_82478_);
        this.setPageantReturnRainbow(false);
        this.setPageantReturnSmoke(true);
        StarFantasyGoetyNetwork.snapEntity(this, home, 0.0F, 0.0F);
    }

    public void holdForFinalPageant(boolean casting) {
        Vec3 home = this.arenaHomePosition();
        this.m_6842_(false);
        this.m_20242_(false);
        this.m_21573_().m_26573_();
        this.m_5810_();
        this.setPageantOpacity(1.0F);
        this.setCasting(casting);
        this.setSouthFacing();
        this.m_6034_(home.f_82479_, home.f_82480_, home.f_82481_);
        this.m_20256_(Vec3.f_82478_);
    }

    public void setPageantReturnSmoke(boolean active) {
        this.f_19804_.m_135381_(PAGEANT_RETURN_SMOKE, active);
    }

    public void setPageantReturnRainbow(boolean active) {
        this.f_19804_.m_135381_(PAGEANT_RETURN_RAINBOW, active);
    }

    public void stopFinalPageantCasting() {
        this.setCasting(false);
    }

    private void setSouthFacing() {
        this.m_146922_(0.0F);
        this.m_5618_(0.0F);
        this.f_20883_ = 0.0F;
        this.m_5616_(0.0F);
        this.f_20885_ = 0.0F;
        this.f_19859_ = 0.0F;
        this.f_20884_ = 0.0F;
        this.f_20886_ = 0.0F;
    }

    public void holdAtHomeForPageantRetry() {
        Vec3 home = this.arenaHomePosition();
        this.m_6842_(false);
        this.m_21573_().m_26573_();
        this.m_5810_();
        this.setCasting(false);
        this.setPageantReturnSmoke(false);
        this.setPageantReturnRainbow(false);
        this.m_20242_(false);
        this.m_6034_(home.f_82479_, home.f_82480_, home.f_82481_);
        this.m_20256_(Vec3.f_82478_);
        this.setPageantOpacity(1.0F);
        if (!(this.m_21205_().m_41720_() instanceof BowItem)) {
            this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        }
    }

    public void enterCombatPhaseTwo() {
        Vec3 home = this.arenaHomePosition();
        this.setCombatPhase(COMBAT_PHASE_TWO);
        this.m_6842_(false);
        this.m_6034_(home.f_82479_, home.f_82480_, home.f_82481_);
        this.m_20242_(false);
        this.m_20256_(Vec3.f_82478_);
        this.setCasting(false);
        this.setPageantReturnSmoke(false);
        this.setPageantReturnRainbow(false);
        this.setPageantOpacity(1.0F);
        this.m_8061_(EquipmentSlot.MAINHAND, new ItemStack(Items.f_42411_));
        this.m_21195_(ApollyonEffectRegistry.MULTISHOT.get());
        this.multishotCooldown = 0;
        this.spellCooldown = 0;
        this.arrowCooldown = 0;
        this.attackDecisionCooldown = 0;
        this.teleportCooldown = 0;
        this.meteorCooldown = 0;
        this.monolithPowerTicks = 0;
        this.receivedHits = 0;
        this.remainingPoolSpells.clear();
        this.lastPoolSpell = CastingSpell.NONE;
        this.remainingHadesAttacks.clear();
        this.phaseTwoHalfHealthEnraged = false;
        this.setCooperativeShield(0.0F);
        this.setMonolithPower(false);
        this.setAntiRegen(0, 0);
        this.applyConfiguredAttributes(true);
        this.m_21153_(this.m_21233_());
    }

    private boolean hasMultishot() {
        return this.m_21023_(ApollyonEffectRegistry.MULTISHOT.get());
    }

    private double[] currentShotAngles() {
        return this.hasMultishot() ? MULTISHOT_ANGLES : SINGLE_SHOT_ANGLE;
    }

    @Override
    public void m_6504_(LivingEntity target, float distanceFactor) {
        this.fireVolley(target);
        if (this.m_217043_().m_188503_(4) == 0) {
            this.tryCombatTeleport(target);
        }
    }

    private void setVoidRayMovementLocked(boolean locked) {
        this.voidRayMovementLocked = locked;
        com.starfantasy.goety.combat.VoidRayKnockback.setCasting(this, locked);
    }

    private void fireVolley(LivingEntity target) {
        Vec3 start = new Vec3(
                this.m_20185_(), this.m_20186_() + this.m_20192_() - 0.1D, this.m_20189_());
        Vec3 targetPoint = new Vec3(target.m_20185_(), target.m_20227_(0.55D), target.m_20189_());
        Vec3 baseDirection = direction(start, targetPoint);

        for (double angle : this.currentShotAngles()) {
            Vec3 shotDirection = rotateY(baseDirection, angle);
            ApollyonStarArrowEntity arrow = new ApollyonStarArrowEntity(this.m_9236_(), this);
            arrow.m_6686_(shotDirection.f_82479_, shotDirection.f_82480_, shotDirection.f_82481_, 2.4F, 1.0F);
            this.m_9236_().m_7967_(arrow);
        }

        this.m_5496_((SoundEvent) ModSounds.APOSTLE_SHOOT.get(), 2.0F,
                1.0F / (this.m_217043_().m_188501_() * 0.4F + 0.8F));
        this.consumeMultishotLevel();
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

    private int drawHadesAttack() {
        if (this.remainingHadesAttacks.isEmpty()) {
            this.remainingHadesAttacks.add(HadesEntity.ATTACK_ROUNDHOUSE);
            this.remainingHadesAttacks.add(HadesEntity.ATTACK_CLAW_COMBO);
            this.remainingHadesAttacks.add(HadesEntity.ATTACK_DIVE_RAY);
            for (int i = this.remainingHadesAttacks.size() - 1; i > 0; --i) {
                int swapIndex = this.m_217043_().m_188503_(i + 1);
                int attack = this.remainingHadesAttacks.get(i);
                this.remainingHadesAttacks.set(i, this.remainingHadesAttacks.get(swapIndex));
                this.remainingHadesAttacks.set(swapIndex, attack);
            }
        }
        return this.remainingHadesAttacks.remove(this.remainingHadesAttacks.size() - 1);
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

    private void setPhaseTwoSpellDelay() {
        int[] delays = this.phaseTwoHalfHealthEnraged
                ? PHASE_TWO_ENRAGED_SPELL_COOLDOWN_TICKS
                : PHASE_TWO_SPELL_COOLDOWN_TICKS;
        this.setSpellDelay(delays[this.m_217043_().m_188503_(delays.length)]);
    }

    private void setArrowDelay(int ticks) {
        this.arrowCooldown = Math.max(0, ticks) + 1;
    }

    private void setAttackDecisionDelay(int ticks) {
        this.attackDecisionCooldown = Math.max(0, ticks) + 1;
    }

    private int postShotDecisionDelay() {
        if (this.isCombatPhaseTwo()) {
            int interval = this.phaseTwoHalfHealthEnraged
                    ? PHASE_TWO_ARROW_INTERVAL_TICKS : ARROW_INTERVAL_TICKS;
            return Math.max(0, interval - BOW_DRAW_TICKS);
        }
        return ATTACK_DECISION_INTERVAL_TICKS;
    }

    private void spawnShotWarnings(LivingEntity target) {
        if (!(this.m_9236_() instanceof ServerLevel)) {
            return;
        }

        this.discardTrackedShotWarnings();
        for (double angle : this.currentShotAngles()) {
            GroundRectangleWarningEntity warning = StarFantasyVfx.groundRectangleWarningTrackingGroundAimed(
                    this,
                    target,
                    SHOT_WARNING_TICKS,
                    SHOT_WARNING_WIDTH,
                    SHOT_WARNING_LENGTH,
                    0.0D,
                    (float) angle,
                    SHOT_WARNING_COLOR,
                    false,
                    true,
                    true);
            if (warning != null) {
                this.shotWarnings.add(warning);
            }
        }
    }

    private void discardTrackedShotWarnings() {
        for (GroundRectangleWarningEntity warning : this.shotWarnings) {
            if (warning.m_6084_()) {
                warning.m_146870_();
            }
        }
        this.shotWarnings.clear();
    }

    private void showCastingSmoke() {
        this.showScatteredHandCastingParticle(ApollyonParticleRegistry.CASTING_SMOKE.get());
    }

    private void showPageantRainbowCastingParticle() {
        float hue = this.m_217043_().m_188501_();
        float sectorPosition = hue * 6.0F;
        int sector = (int) sectorPosition;
        float fraction = sectorPosition - sector;
        float low = 0.15F;
        float rising = low + (1.0F - low) * fraction;
        float falling = 1.0F - (1.0F - low) * fraction;
        float red;
        float green;
        float blue;
        switch (sector % 6) {
            case 0 -> { red = 1.0F; green = rising; blue = low; }
            case 1 -> { red = falling; green = 1.0F; blue = low; }
            case 2 -> { red = low; green = 1.0F; blue = rising; }
            case 3 -> { red = low; green = falling; blue = 1.0F; }
            case 4 -> { red = rising; green = low; blue = 1.0F; }
            default -> { red = 1.0F; green = low; blue = falling; }
        }
        Vec3 hand = this.castingHandPosition();
        ParticleOptions particle = (ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get();
        if (this.m_9236_() instanceof ServerLevel level) {
            level.m_8767_(particle,
                    hand.f_82479_, hand.f_82480_, hand.f_82481_,
                    0, red, green, blue, 1.0D);
        } else {
            this.m_9236_().m_7107_(particle,
                    hand.f_82479_, hand.f_82480_, hand.f_82481_, red, green, blue);
        }
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

    private boolean tryCombatTeleport(LivingEntity target) {
        if (this.voidRayMovementLocked || this.teleportCooldown > 0
                || this.isCasting() || this.isTeleportPending()
                || target == null || !(this.m_9236_() instanceof ServerLevel level)) {
            return false;
        }

        this.ensureArenaHome();
        Vec3 destination = this.findCombatTeleportDestination(level, target);
        if (destination == null) {
            destination = this.arenaHomePosition();
        }
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_PRE_TELEPORT.get(), 2.0F, 1.0F);
        if (Boolean.TRUE.equals(MobsConfig.ApostleDelayedTeleport.get())) {
            this.pendingTeleportPosition = destination;
            this.teleportWindupTicks = 0;
            return true;
        }
        return this.finishCombatTeleport(level, destination);
    }

    private Vec3 findCombatTeleportDestination(ServerLevel level, LivingEntity target) {
        for (int attempt = 0; attempt < 16; ++attempt) {
            double x = target.m_20185_() + (this.m_217043_().m_188500_() - 0.5D) * 24.0D;
            double y = target.m_20186_() + this.m_217043_().m_188503_(9) - 4;
            double z = target.m_20189_() + (this.m_217043_().m_188500_() - 0.5D) * 24.0D;
            Vec3 destination = this.resolveTeleportGround(level, x, y, z);
            if (destination != null && this.isSelfPositionInsideArena(destination)) {
                return destination;
            }
        }
        return null;
    }

    private Vec3 resolveTeleportGround(ServerLevel level, double x, double y, double z) {
        BlockPos cursor = BlockPos.m_274561_(x, y, z);
        if (!level.m_46805_(cursor)) {
            return null;
        }

        double groundY = y;
        boolean foundGround = false;
        while (cursor.m_123342_() > level.m_141937_()) {
            BlockPos below = cursor.m_7495_();
            if (level.m_8055_(below).m_280555_()) {
                foundGround = true;
                break;
            }
            groundY -= 1.0D;
            cursor = below;
        }
        if (!foundGround) {
            return null;
        }

        Vec3 destination = new Vec3(x, groundY, z);
        AABB destinationBounds = this.m_20191_().m_82386_(
                destination.f_82479_ - this.m_20185_(),
                destination.f_82480_ - this.m_20186_(),
                destination.f_82481_ - this.m_20189_());
        return level.m_45756_(this, destinationBounds) && !level.m_46855_(destinationBounds)
                ? destination
                : null;
    }

    private void tickPendingTeleport() {
        if (this.pendingTeleportPosition == null) {
            this.teleportWindupTicks = 0;
            return;
        }
        if (!this.m_6084_() || !(this.m_9236_() instanceof ServerLevel level)) {
            this.clearPendingTeleport();
            return;
        }
        if (!Boolean.TRUE.equals(MobsConfig.ApostleDelayedTeleport.get())) {
            Vec3 destination = this.pendingTeleportPosition;
            this.clearPendingTeleport();
            this.finishCombatTeleport(level, destination);
            return;
        }

        ++this.teleportWindupTicks;
        if (this.teleportWindupTicks >= TELEPORT_WINDUP_TICKS) {
            Vec3 destination = this.pendingTeleportPosition;
            this.clearPendingTeleport();
            this.finishCombatTeleport(level, destination);
            return;
        }
        this.spawnTeleportWindupParticles(level, this.pendingTeleportPosition);
    }

    private void spawnTeleportWindupParticles(ServerLevel level, Vec3 destination) {
        ServerParticleUtil.addParticlesAroundMiddleSelf(level, ParticleTypes.f_123762_, this);
        level.m_8767_(ParticleTypes.f_123755_,
                destination.f_82479_, destination.f_82480_ + 0.25D, destination.f_82481_,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        ServerParticleUtil.windParticle(level, ColorUtil.BLACK,
                2.0F, 0.25F, -1, destination);
        for (int i = 0; i < 16; ++i) {
            Vec3 trailTarget = destination.m_82520_(
                    this.m_20205_() * (2.0D * this.f_19796_.m_188500_() - 1.0D) * 0.5D,
                    this.m_20206_() * this.f_19796_.m_188500_(),
                    this.m_20205_() * (2.0D * this.f_19796_.m_188500_() - 1.0D) * 0.5D);
            level.m_8767_(new AbsorbTrailParticleOption(trailTarget, 0xFF7700, 10),
                    this.m_20208_(0.5D), this.m_20187_(), this.m_20262_(0.5D),
                    1, 0.0D, 0.0D, 0.0D, 1.0D);
        }
    }

    private boolean isSelfPositionInsideArena(Vec3 position) {
        this.ensureArenaHome();
        double safeRadius = Math.max(0.0D,
                ARENA_RADIUS - this.m_20205_() * 0.5D - ARENA_COLLISION_INSET);
        return this.isHorizontalPositionInsideArena(position, safeRadius);
    }

    private boolean isHorizontalPositionInsideArena(Vec3 position, double radius) {
        Vec3 home = this.arenaHomePosition();
        double offsetX = position.f_82479_ - home.f_82479_;
        double offsetZ = position.f_82481_ - home.f_82481_;
        return offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }

    private boolean teleportHome() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_PRE_TELEPORT.get(), 2.0F, 1.0F);
        return this.teleportHomeWithoutPrepare(level);
    }

    private boolean teleportHomeWithoutPrepare() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        return this.teleportHomeWithoutPrepare(level);
    }

    private boolean teleportHomeWithoutPrepare(ServerLevel level) {
        this.clearPendingTeleport();
        Vec3 oldPosition = this.m_20182_();
        Vec3 home = this.arenaHomePosition();
        this.m_21573_().m_26573_();
        this.m_6021_(home.f_82479_, home.f_82480_, home.f_82481_);
        this.m_20256_(Vec3.f_82478_);
        this.f_19789_ = 0.0F;
        this.spawnTeleportParticles(level, oldPosition, 0.0D);
        this.spawnTeleportParticles(level, this.m_20182_(), this.m_20206_() * 0.5D);
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_TELEPORT.get(), 2.0F, 1.0F);
        this.teleportCooldown = 80;
        return true;
    }

    private boolean finishCombatTeleport(ServerLevel level, Vec3 destination) {
        Vec3 oldPosition = this.m_20182_();
        if (!this.m_20984_(
                destination.f_82479_, destination.f_82480_, destination.f_82481_, true)) {
            this.teleportCooldown = 20;
            return false;
        }
        this.spawnTeleportParticles(level, oldPosition, 0.0D);
        this.spawnTeleportParticles(level, this.m_20182_(), this.m_20206_() * 0.5D);
        this.m_5496_((SoundEvent) ModSounds.APOSTLE_TELEPORT.get(), 2.0F, 1.0F);
        this.teleportCooldown = 80;
        return true;
    }

    private boolean isTeleportPending() {
        return this.pendingTeleportPosition != null;
    }

    private void clearPendingTeleport() {
        this.pendingTeleportPosition = null;
        this.teleportWindupTicks = 0;
    }

    private void spawnTeleportParticles(ServerLevel level, Vec3 position, double heightOffset) {
        level.m_8767_(ParticleTypes.f_123755_,
                position.f_82479_, position.f_82480_ + heightOffset, position.f_82481_,
                32, 0.8D, 1.4D, 0.8D, 0.04D);
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No Gecko animation controller: all combat poses come from Goety's Cultist model.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
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
            LivingEntity target = ApollyonEntity.this.m_5448_();
            return target != null && target.m_6084_()
                    && !ApollyonEntity.this.phaseTwoInitializationPending
                    && !ApollyonEntity.this.pageant.isCombatLocked()
                    && ApollyonEntity.this.m_21205_().m_41720_() instanceof BowItem;
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
            ApollyonEntity.this.m_21561_(true);
        }

        @Override
        public void m_8041_() {
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
            ApollyonEntity.this.setVoidRayMovementLocked(false);
            ApollyonEntity.this.setCasting(false);
            ApollyonEntity.this.m_21561_(false);
            ApollyonEntity.this.m_5810_();
        }

        @Override
        public void m_8037_() {
            LivingEntity target = ApollyonEntity.this.m_5448_();
            if (target == null || !target.m_6084_()) {
                return;
            }

            if (this.activeSpell != CastingSpell.NONE) {
                this.tickSpell(target);
                return;
            }

            boolean canSee = ApollyonEntity.this.m_21574_().m_148306_(target);
            this.updateMovement(target, canSee);

            if (ApollyonEntity.this.m_6117_()) {
                if (!canSee && this.seeTime < -60) {
                    ApollyonEntity.this.m_5810_();
                    ApollyonEntity.this.setAttackDecisionDelay(ATTACK_DECISION_INTERVAL_TICKS);
                } else if (canSee) {
                    int drawTicks = ApollyonEntity.this.m_21252_();
                    if (drawTicks >= BOW_DRAW_TICKS) {
                        ApollyonEntity.this.m_5810_();
                        ApollyonEntity.this.m_6504_(target, BowItem.m_40661_(drawTicks));
                        ApollyonEntity.this.setAttackDecisionDelay(
                                ApollyonEntity.this.postShotDecisionDelay());
                    }
                }
                return;
            }

            if (ApollyonEntity.this.attackDecisionCooldown > 0) {
                return;
            }

            if (ApollyonEntity.this.spellCooldown <= 0
                    && !ApollyonEntity.this.isTeleportPending()) {
                CastingSpell nextSpell;
                if (ApollyonEntity.this.multishotCooldown <= 0) {
                    nextSpell = CastingSpell.MULTISHOT;
                } else if (ApollyonEntity.this.isCombatPhaseTwo()) {
                    nextSpell = ApollyonEntity.this.drawPoolSpell();
                    this.phaseTwoChainSpellsRemaining = PHASE_TWO_SPELLS_PER_CHAIN - 1;
                } else {
                    nextSpell = ApollyonEntity.this.drawPoolSpell();
                }
                this.startSpell(nextSpell);
                return;
            }

            if (ApollyonEntity.this.arrowCooldown <= 0) {
                ApollyonEntity.this.m_6672_(ProjectileUtil.m_37297_(ApollyonEntity.this, Items.f_42411_));
                return;
            }

            ApollyonEntity.this.setAttackDecisionDelay(ATTACK_DECISION_INTERVAL_TICKS);
        }

        private void updateMovement(LivingEntity target, boolean canSee) {
            double distanceSqr = ApollyonEntity.this.m_20275_(
                    target.m_20185_(), target.m_20186_(), target.m_20189_());
            boolean wasSeeing = this.seeTime > 0;
            if (canSee != wasSeeing) {
                this.seeTime = 0;
            }
            this.seeTime += canSee ? 1 : -1;

            if (distanceSqr > BOW_ATTACK_RANGE_SQR || this.seeTime < 20) {
                ApollyonEntity.this.m_21573_().m_5624_(target, COMBAT_MOVE_SPEED);
                this.strafingTime = -1;
            } else {
                ApollyonEntity.this.m_21573_().m_26573_();
                ++this.strafingTime;
            }

            if (this.strafingTime >= 20) {
                if (ApollyonEntity.this.m_217043_().m_188501_() < 0.3F) {
                    this.strafingClockwise = !this.strafingClockwise;
                }
                if (ApollyonEntity.this.m_217043_().m_188501_() < 0.3F) {
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
                ApollyonEntity.this.m_21566_().m_24988_(
                        this.strafingBackwards ? -0.5F : 0.5F,
                        this.strafingClockwise ? 0.5F : -0.5F);
                // ApostleBowGoal turns the whole mob toward its target while
                // strafing. LookControl alone only turns the head and leaves the
                // body following sideways movement, producing an extreme twist.
                ApollyonEntity.this.m_21391_(target, 30.0F, 30.0F);
            } else {
                ApollyonEntity.this.m_21563_().m_24960_(target, 30.0F, 30.0F);
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
                this.fangFeast = new ApollyonFangFeastSpell(ApollyonEntity.this);
            }
            ApollyonEntity.this.setVoidRayMovementLocked(spell == CastingSpell.VOID_RAY);
            ApollyonEntity.this.setCasting(true);
            ApollyonEntity.this.m_21557_(false);
            ApollyonEntity.this.m_21573_().m_26573_();
            if (spell == CastingSpell.VOID_RAY) {
                ApollyonEntity.this.teleportHomeWithoutPrepare();
            }
            this.lockVoidRayMovement();
            ApollyonEntity.this.m_5496_((SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(), 2.0F, 1.0F);
            if (spell == CastingSpell.LIGHTNING_STORM) {
                // The hand core identifies the spell throughout its windup.
                // Only the actual lightning sequence waits for tick 40.
                ApollyonCastingLightningEntity.spawn(
                        ApollyonEntity.this,
                        skipWindup
                                ? LIGHTNING_STORM_EFFECT_TICKS
                                + LIGHTNING_STORM_SECOND_SPELL_EXTENSION_TICKS
                                : LIGHTNING_STORM_CAST_TICKS);
            }
            if (POOL_SPELL_SUMMONS_ENABLED) {
                switch (spell) {
                    case FIRE_TRAP -> ApollyonSummonManager.summonInfernos(ApollyonEntity.this);
                    case FROST_IMPACT -> ApollyonSummonManager.summonIceGolem(ApollyonEntity.this);
                    case WILD_SURGE -> ApollyonSummonManager.summonLeapleaf(ApollyonEntity.this);
                    case LIGHTNING_STORM -> ApollyonSummonManager.summonStormCaster(ApollyonEntity.this);
                    case VOID_RAY -> ApollyonSummonManager.summonWatchlings(ApollyonEntity.this);
                    default -> {
                    }
                }
            }
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
            ApollyonEntity.this.m_21563_().m_24960_(target, 30.0F, 30.0F);
            ApollyonEntity.this.m_21573_().m_26573_();
            this.lockVoidRayMovement();
            if (this.activeSpell == CastingSpell.FROST_IMPACT
                    && this.castTicks >= FROST_IMPACT_WINDUP_TICKS
                    && this.castTicks <= FROST_IMPACT_WINDUP_TICKS + 30
                    && (this.castTicks - FROST_IMPACT_WINDUP_TICKS)
                    % FROST_IMPACT_INTERVAL_TICKS == 0) {
                ApollyonFrostImpactManager.spawnChunk(
                        ApollyonEntity.this, target,
                        (this.castTicks - FROST_IMPACT_WINDUP_TICKS)
                                / FROST_IMPACT_INTERVAL_TICKS);
            }
            switch (this.activeSpell) {
                case MULTISHOT -> ApollyonEntity.this.showCastingSmoke();
                case FIRE_TRAP -> ApollyonEntity.this.showFireTrapCastingParticles();
                case FROST_IMPACT -> ApollyonEntity.this.showFrostImpactCastingParticles();
                case WILD_SURGE -> ApollyonEntity.this.showWildSurgeCastingParticles();
                case FANG_FEAST -> ApollyonEntity.this.showFangFeastCastingParticles();
                case VOID_RAY -> {
                    if (this.castTicks < VOID_RAY_WARNING_TICK) {
                        ApollyonEntity.this.showVoidRayCastingParticles();
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
                ApollyonFireTrapManager.cast(ApollyonEntity.this, target);
            }
            if (this.activeSpell == CastingSpell.LIGHTNING_STORM) {
                this.tickLightningStorm(target);
            } else if (this.activeSpell == CastingSpell.WILD_SURGE) {
                if (this.castTicks == WILD_SURGE_EARTH_WARNING_TICK) {
                    this.wildSurgeAnchor = ApollyonWildSurgeManager.captureAnchor(
                            ApollyonEntity.this, target);
                    ApollyonWildSurgeManager.warnEarthRing(
                            ApollyonEntity.this, this.wildSurgeAnchor);
                } else if (this.castTicks == WILD_SURGE_EARTH_TICK) {
                    this.wildSurgeThornPoints = ApollyonWildSurgeManager.spawnEarthRingAndWarnThorns(
                            ApollyonEntity.this, this.wildSurgeAnchor);
                } else if (this.castTicks == WILD_SURGE_THORN_TICK) {
                    ApollyonWildSurgeManager.spawnThornRings(
                            ApollyonEntity.this, this.wildSurgeThornPoints);
                }
            } else if (this.activeSpell == CastingSpell.VOID_RAY) {
                if (this.castTicks == VOID_RAY_WARNING_TICK) {
                    this.voidRayAnchor = ApollyonVoidRayManager.captureAnchor(ApollyonEntity.this);
                    ApollyonVoidRayManager.warn(ApollyonEntity.this, this.voidRayAnchor,
                            0.0F, ApollyonVoidRayManager.FIRST_WARNING_TICKS);
                } else if (this.castTicks == VOID_RAY_FIRST_HIT_TICK) {
                    ApollyonVoidRayManager.detonate(
                            ApollyonEntity.this, this.voidRayAnchor, 0.0F);
                    ApollyonVoidRayManager.warn(ApollyonEntity.this, this.voidRayAnchor,
                            ApollyonVoidRayManager.SECOND_WAVE_ROTATION,
                            ApollyonVoidRayManager.SECOND_WARNING_TICKS);
                } else if (this.castTicks == VOID_RAY_SECOND_HIT_TICK) {
                    ApollyonVoidRayManager.detonate(ApollyonEntity.this, this.voidRayAnchor,
                            ApollyonVoidRayManager.SECOND_WAVE_ROTATION);
                }
            }

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
            if (this.castTicks < castDuration) {
                return;
            }

            CastingSpell completedSpell = this.activeSpell;
            if (completedSpell == CastingSpell.FANG_FEAST) {
                this.finishFangFeast(true);
            }
            boolean phaseTwoPoolSpell = ApollyonEntity.this.isCombatPhaseTwo()
                    && completedSpell.isPoolSpell();
            boolean continuePhaseTwoChain = phaseTwoPoolSpell
                    && this.phaseTwoChainSpellsRemaining > 0;

            if (completedSpell == CastingSpell.MULTISHOT) {
                ApollyonEntity.this.m_7292_(new MobEffectInstance(
                        ApollyonEffectRegistry.MULTISHOT.get(), MULTISHOT_DURATION, 9, false, true));
                if (ApollyonEntity.this.isCombatPhaseTwo()) {
                    ApollyonEntity.this.activatePhaseTwoShield();
                    ApollyonEntity.this.pageant.startHadesCooperativeAttack(
                            ApollyonEntity.this.drawHadesAttack());
                    ApollyonEntity.this.setPhaseTwoSpellDelay();
                } else {
                    ApollyonSummonManager.summonObsidianMonoliths(ApollyonEntity.this);
                    ApollyonEntity.this.setSpellDelay(MULTISHOT_SPELL_COOLDOWN_TICKS);
                }
                ApollyonEntity.this.multishotCooldown =
                        ApollyonEntity.this.isCombatPhaseTwo()
                                ? PHASE_TWO_MULTISHOT_COOLDOWN_TICKS
                                : PHASE_ONE_MULTISHOT_COOLDOWN_TICKS;
            } else if (completedSpell.isPoolSpell()) {
                if (!continuePhaseTwoChain) {
                    if (phaseTwoPoolSpell) {
                        ApollyonEntity.this.pageant.startHadesCooperativeAttack(
                                ApollyonEntity.this.drawHadesAttack());
                        ApollyonEntity.this.setPhaseTwoSpellDelay();
                    } else {
                        ApollyonEntity.this.setPoolSpellDelay();
                    }
                }
            }

            ApollyonEntity.this.m_5496_((SoundEvent) ModSounds.APOSTLE_CAST_SPELL.get(), 2.0F, 1.0F);
            if (continuePhaseTwoChain) {
                --this.phaseTwoChainSpellsRemaining;
                this.startSpell(ApollyonEntity.this.drawPoolSpell(), true);
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
            ApollyonEntity.this.setVoidRayMovementLocked(false);
            ApollyonEntity.this.setCasting(false);
            ApollyonEntity.this.setAttackDecisionDelay(ATTACK_DECISION_INTERVAL_TICKS);
        }

        private void finishFangFeast(boolean completed) {
            if (this.fangFeast != null) {
                this.fangFeast.finish(completed);
                this.fangFeast = null;
            }
        }

        private void lockVoidRayMovement() {
            if (!ApollyonEntity.this.voidRayMovementLocked) {
                return;
            }
            ApollyonEntity.this.m_21573_().m_26573_();
            ApollyonEntity.this.m_21566_().m_24988_(0.0F, 0.0F);
            Vec3 movement = ApollyonEntity.this.m_20184_();
            ApollyonEntity.this.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
        }

        private void tickLightningStorm(LivingEntity target) {
            int logicTick = this.castTicks - LIGHTNING_STORM_WINDUP_TICKS;
            if (logicTick <= 0) {
                return;
            }
            if (logicTick <= 30 && logicTick % 5 == 0) {
                ApollyonLightningStormManager.queueTrackingStrike(ApollyonEntity.this, target);
            }
            if (logicTick == 30) {
                this.lightningStormAnchor =
                        ApollyonLightningStormManager.captureAnchor(ApollyonEntity.this, target);
                ApollyonLightningStormManager.queueRing(
                        ApollyonEntity.this, this.lightningStormAnchor, 24.0D, 24);
            } else if (logicTick == 40) {
                ApollyonLightningStormManager.queueRing(
                        ApollyonEntity.this, this.lightningStormAnchor, 16.0D, 16);
            } else if (logicTick == 50) {
                ApollyonLightningStormManager.queueRing(
                        ApollyonEntity.this, this.lightningStormAnchor, 8.0D, 8);
            } else if (logicTick == 60) {
                ApollyonLightningStormManager.queueCenter(
                        ApollyonEntity.this, this.lightningStormAnchor);
            }
        }
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
}
