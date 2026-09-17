package com.starfantasy.goety.combat;

import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.SlamParticleOption;
import com.Polarice3.Goety.client.particles.SphereExplodeParticleOption;
import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayLoopSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.MiscCapHelper;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.common.capabilities.soulenergy.FocusCooldown;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.entity.ApollyonCleaveEffectEntity;
import com.starfantasy.goety.entity.ApollyonFamineWaveEntity;
import com.starfantasy.goety.entity.ApollyonGloriousSphereEntity;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import com.starfantasy.goety.entity.ApollyonPageantBeamEntity;
import com.starfantasy.goety.entity.ApollyonPageantBlueIceEntity;
import com.starfantasy.goety.entity.ApollyonPageantIceChunkEntity;
import com.starfantasy.goety.entity.ApollyonPageantHaloEntity;
import com.starfantasy.goety.entity.ApollyonPageantMagmaEntity;
import com.starfantasy.goety.entity.ApollyonPageantMeteorEntity;
import com.starfantasy.goety.entity.ApollyonPageantObsidianMonolithEntity;
import com.starfantasy.goety.entity.ApollyonPageantOwned;
import com.starfantasy.goety.entity.ApollyonPageantSummonEntity;
import com.starfantasy.goety.entity.HadesEntity;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.library.combat.StarFantasyTrueKillHelper;
import com.starfantasy.library.vfx.entity.GroundRectangleWarningEntity;
import com.starfantasy.library.vfx.particle.WarningColor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Owns the first complete Apollyon pageant and all of its reset semantics. */
public final class ApollyonPageantController {
    public static final int INACTIVE = 0;
    public static final int FADE_OUT = 1;
    public static final int SUMMONING = 2;
    public static final int FIRST_TRIO = 3;
    public static final int GLORIOUS_STAGE = 4;
    public static final int SECOND_TRIO = 5;
    public static final int THIRD_DPS = 6;
    public static final int FOURTH_STAGE = 7;
    public static final int FIFTH_STAGE = 8;
    public static final int PENDING_RESTART = 9;

    private static final String STATE_TAG = "ApollyonPageantState";
    private static final String TEST_PHASE_TAG = "PageantTestPhase";
    private static final String CHECKPOINT_TAG = "ApollyonPageantCheckpoint";
    private static final int LEGACY_PHASE_TWO_STATE = 5;
    private static final int FADE_TICKS = 60;
    private static final int PROFANE_SUMMON_TICK = 40;
    private static final int PROFANE_EFFECT_TICK = 70;
    private static final int PROFANE_CAST_END_TICK = 80;
    private static final int FOLLOWUP_SUMMON_TICK = 100;
    private static final int FAMINE_DURATION_TICKS = 2400;
    private static final int METEOR_WARNING_TICK = 80;
    private static final int METEOR_SPAWN_TICK = 100;
    private static final int METEOR_IMPACT_TICK = 120;
    private static final int[] FIRE_TRAP_TICKS = {40, 50, 60, 70};

    private static final int GLORIOUS_SUMMON_TICK = 40;
    private static final int GLORIOUS_CAST_TICKS = 200;
    private static final int GLORIOUS_CAST_SOUND_INTERVAL = 50;
    private static final int GLORIOUS_RESOLVE_TICK =
            GLORIOUS_SUMMON_TICK + GLORIOUS_CAST_TICKS;
    private static final int GLORIOUS_STOP_CAST_TICK = GLORIOUS_RESOLVE_TICK + 10;
    private static final int GLORIOUS_DEPART_TICK = GLORIOUS_RESOLVE_TICK + 40;
    private static final int SECOND_TRIO_WARNING_TICKS = 40;
    private static final int SECOND_TRIO_BEAM_DURATION = 160;
    private static final int SECOND_TRIO_BEAM_END_TICK =
            SECOND_TRIO_WARNING_TICKS + SECOND_TRIO_BEAM_DURATION;
    private static final int SECOND_TRIO_DAMAGE_END_TICK = SECOND_TRIO_BEAM_END_TICK - 10;
    private static final int SECOND_TRIO_DEPART_TICK = SECOND_TRIO_BEAM_END_TICK;
    private static final int SECOND_TRIO_BEAM_HIT_COOLDOWN = 10;
    private static final int SECOND_TRIO_METEOR_INTERVAL = 10;
    private static final int SECOND_TRIO_SECTOR_WARNING_TICKS = 20;
    private static final int SECOND_TRIO_SECTOR_INTERVAL = 20;
    private static final int[] SECOND_TRIO_CONSTANT_START_AGES = {20, 100};
    private static final int THIRD_SUMMON_TICK = 40;
    private static final int THIRD_LINK_INTERVAL = 160;
    private static final int THIRD_ENRAGE_TICK = 800;
    private static final int THIRD_ENRAGE_WARNING_TICKS = 40;
    private static final int THIRD_METEOR_INTERVAL = 15;
    private static final int FOURTH_SUMMON_TICK = 40;
    private static final int FOURTH_SLIDE_TICKS = 20;
    private static final int FIFTH_RETURN_TICK = 40;
    private static final int FIFTH_INNER_ATTACH_TICKS = 20;
    private static final int FIFTH_DARKNESS_TICK = 100;
    private static final int FIFTH_HADES_SPAWN_TICK = 120;
    private static final int FIFTH_DARKNESS_END_TICK = 180;
    private static final int FIFTH_COUNTDOWN_START_TICK = 220;
    private static final int FIFTH_SMASH_TICK = 320;
    private static final int FIFTH_CLEAVE_TICK = FIFTH_SMASH_TICK + 26;
    private static final int FIFTH_JUDGMENT_TICK = FIFTH_CLEAVE_TICK + 60;
    private static final int FIFTH_END_TICK = FIFTH_JUDGMENT_TICK + 60;

    private static final double ACTOR_DISTANCE = 2.0D;
    private static final double METEOR_RADIUS = 6.0D;
    private static final float METEOR_DAMAGE = 60.0F;
    private static final int METEOR_WARNING_TICKS = 40;
    private static final double SAFE_WIDTH = 4.0D;
    private static final double SAFE_LENGTH = 12.0D;
    private static final int SAFE_COLOR = 0x20FF20;
    private static final float PAGEANT_LIGHTNING_SIZE = 8.0F;
    private static final float PYROCLAST_VISUAL_SIZE = 9.0F;
    private static final int PYROCLAST_COLOR = 16746757;
    private static final int PYROCLAST_CULT_COLOR = 11312015;
    private static final int SECOND_TRIO_WARNING_COLOR = 0xA020F0;
    private static final double SECOND_ACTOR_DISTANCE = 1.0D;
    private static final double SECOND_TRIO_WARNING_WIDTH = 2.0D;
    private static final double SECOND_TRIO_BEAM_LENGTH = 20.0D;
    private static final float SECOND_TRIO_BEAM_DAMAGE = 20.0F;
    private static final float SECOND_TRIO_BEAM_HARD_DAMAGE = 15.0F;
    private static final double SECOND_TRIO_MAX_ANGULAR_SPEED = 1.5D;
    private static final float SECOND_TRIO_SECTOR_RADIUS = 20.0F;
    private static final float SECOND_TRIO_SECTOR_ANGLE = 15.0F;
    private static final float SECOND_TRIO_SECTOR_SPACING = 45.0F;
    private static final int SECOND_TRIO_SECTOR_COUNT = 8;
    private static final float SECOND_TRIO_SECTOR_ROTATION_STEP = 15.0F;
    private static final double GLORIOUS_FAILURE_KNOCK_DISTANCE = 100.0D;
    private static final int GLORIOUS_SLIDE_TICKS = 20;
    private static final double THIRD_ACTOR_DISTANCE = 15.0D;
    private static final int THIRD_EXPLOSION_COLOR = 0x7868FF;
    private static final float THIRD_EXPLOSION_ALPHA = 0.76F;
    private static final double THIRD_EXPLOSION_SIZE = 2.0D / 3.0D;
    private static final int THIRD_EXPLOSION_LIFETIME = 50;
    private static final int THIRD_SHAKE_HOLD_TICKS = 40;
    private static final int THIRD_SHAKE_FADE_TICKS = 20;
    private static final float THIRD_SHAKE_INTENSITY = 3.0F;
    private static final double FOURTH_ACTOR_DISTANCE = 2.0D;
    private static final double FOURTH_VINE_KILL_RADIUS = 16.0D;
    private static final double FOURTH_ICE_RING_RADIUS = 18.0D;
    private static final int FOURTH_ICE_CANDIDATE_COUNT = 24;
    private static final int FOURTH_ICE_COUNT = 12;
    private static final double FOURTH_ICE_RADIUS = 6.0D;
    private static final float FOURTH_ICE_DAMAGE = 60.0F;
    private static final int FOURTH_STUN_TICKS = 100;
    private static final int FOURTH_ICE_EXPLOSION_WARNING_TICKS = 40;
    private static final double FOURTH_KNOCK_DISTANCE = 100.0D;
    private static final double[] FOURTH_VINE_RING_RADII = {0.0D, 5.0D, 10.0D, 15.0D};
    private static final int FOURTH_SLAM_COLOR = 0xFF2020;
    private static final int HALO_COUNT = 12;
    private static final double HADES_NORTH_DISTANCE = 20.0D;
    private static final float FIFTH_JUDGMENT_SHAKE_INTENSITY = 6.0F;

    private static final int DEATH_PARTICLE_COUNT = 1000;
    private static final double DEATH_PARTICLE_SPEED = 1.0D;
    private static final double ARENA_SHAKE_RADIUS = 24.0D;
    private static final int MAJOR_SHAKE_TICKS = 20;
    private static final float MAJOR_SHAKE_INTENSITY = 1.3F;

    private final ApollyonEntity boss;
    private int state = INACTIVE;
    private int stateTicks;
    private int directTransitionHealTicks;
    private boolean cleanupAfterLoad;
    private int testPhase = 1;
    private int checkpointPhase = 1;
    private final Vec3[] actorPositions = new Vec3[3];
    private UUID profaneUuid;
    private UUID pyreLordUuid;
    private UUID terribleUuid;
    private final List<UUID> meteorUuids = new ArrayList<>();
    private final List<UUID> magmaUuids = new ArrayList<>();
    private final List<UUID> safeWarningUuids = new ArrayList<>();
    private final List<Vec3> meteorAnchors = new ArrayList<>();
    private UUID gloriousUuid;
    private final UUID[] secondActorUuids = new UUID[3];
    private final UUID[] secondBeamUuids = new UUID[3];
    private final double[] secondActorBaseAngles = new double[3];
    private double secondOrbitDegrees;
    private final Map<UUID, PlayerSlide> gloriousSlides = new HashMap<>();
    private final Map<UUID, Integer> beamHitCooldowns = new HashMap<>();
    private final Vec3[] thirdActorPositions = new Vec3[2];
    private final UUID[] thirdActorUuids = new UUID[2];
    private UUID thirdMonolithUuid;
    private int thirdLinkedIndex = -1;
    private boolean thirdEnraged;
    private final Vec3[] fourthActorPositions = new Vec3[3];
    private final UUID[] fourthActorUuids = new UUID[3];
    private final UUID[] fourthIceUuids = new UUID[FOURTH_ICE_COUNT];
    private final Map<UUID, PlayerSlide> fourthSlides = new HashMap<>();
    private final Set<UUID> fourthSlideStopped = new HashSet<>();
    private final boolean[] occupiedOuterHaloAnchors = new boolean[HALO_COUNT];
    private final Set<UUID> darkenedPlayers = new HashSet<>();
    private long haloOrbitEpoch;
    private UUID hadesUuid;

    private static int lightningDamageTick() {
        return METEOR_IMPACT_TICK + terribleWarningTicks();
    }

    private static int lightningMiddleRingTick() {
        return lightningDamageTick() + 1;
    }

    private static int lightningOuterRingTick() {
        return lightningDamageTick() + 2;
    }

    private static int magmaExplosionTick() {
        return lightningDamageTick() + magmaWarningTicks();
    }

    private static int terribleWarningTicks() {
        return ApollyonConfig.hardMode() ? 60 : 100;
    }

    private static int magmaWarningTicks() {
        return ApollyonConfig.hardMode() ? 30 : 50;
    }

    private static int fourthFirstWarningTicks() {
        return ApollyonConfig.hardMode() ? 80 : 120;
    }

    private static int fourthAtrociousWarningTicks() {
        return ApollyonConfig.hardMode() ? 60 : 80;
    }

    private static int fourthFirstResolveTick() {
        return FOURTH_SUMMON_TICK + fourthFirstWarningTicks();
    }

    private static int fourthAtrociousSummonTick() {
        return fourthFirstResolveTick() + 20;
    }

    private static int fourthKnockbackTick() {
        return fourthAtrociousSummonTick() + fourthAtrociousWarningTicks();
    }

    private static int fourthIceExplosionTick() {
        return fourthKnockbackTick() + FOURTH_ICE_EXPLOSION_WARNING_TICKS;
    }

    public ApollyonPageantController(ApollyonEntity boss) {
        this.boss = boss;
    }

    public boolean tryStartFromFinalDamage(float finalDamage) {
        // Half health normally starts the pageant on the server tick. Keep this
        // final-damage guard so a single lethal hit cannot skip the transition.
        if (this.state != INACTIVE || !this.boss.isCombatPhaseOne()
                || finalDamage <= 0.0F
                || finalDamage < this.boss.m_21223_()) {
            return false;
        }
        if (this.isValidPageantTarget(this.boss.m_5448_())) {
            this.beginTransition();
        }
        return true;
    }

    public void tickServer() {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (this.state == INACTIVE) {
            if (!this.boss.isCombatPhaseOne() || !this.boss.m_6084_()
                    || this.boss.m_21223_() > this.boss.m_21233_() * 0.5F
                    || !this.isValidPageantTarget(this.boss.m_5448_())) {
                return;
            }
            this.beginTransition();
        }
        if (this.cleanupAfterLoad) {
            this.cleanupAfterLoad = false;
            this.clearEncounter(level, true);
        }

        List<ServerPlayer> players = this.boss.validArenaPlayers();
        LivingEntity target = this.ensureTarget();
        if (this.state == PENDING_RESTART) {
            this.boss.holdAtHomeForPageantRetry();
            if (target != null) {
                this.beginTransition();
            }
            return;
        }
        if (target == null) {
            this.resetToPending(level);
            return;
        }
        switch (this.state) {
            case FADE_OUT -> this.tickFade(level, players, target);
            case SUMMONING -> this.tickSummoning(level, players);
            case FIRST_TRIO -> this.tickFirstTrio(level, players, target);
            case GLORIOUS_STAGE -> this.tickGloriousStage(level, players, target);
            case SECOND_TRIO -> this.tickSecondTrio(level, players);
            case THIRD_DPS -> this.tickThirdDps(level, players, target);
            case FOURTH_STAGE -> this.tickFourthStage(level, players);
            case FIFTH_STAGE -> this.tickFifthStage(level, players);
            default -> {
            }
        }
    }

    public boolean isCombatLocked() {
        return this.state != INACTIVE;
    }

    public boolean isInvulnerable() {
        return this.isCombatLocked();
    }

    public ApollyonPageantApostleEntity redirectTarget(LivingEntity attacker) {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        if (this.state == GLORIOUS_STAGE) {
            ApollyonPageantApostleEntity glorious = actor(level, this.gloriousUuid);
            return isAttackableActor(glorious) ? glorious : null;
        }
        ApollyonPageantApostleEntity nearest = null;
        if (this.state == THIRD_DPS) {
            for (UUID actorUuid : this.thirdActorUuids) {
                ApollyonPageantApostleEntity candidate = actor(level, actorUuid);
                if (isAttackableActor(candidate) && (nearest == null
                        || attacker.m_20280_(candidate) < attacker.m_20280_(nearest))) {
                    nearest = candidate;
                }
            }
        }
        return nearest;
    }

    private static boolean isAttackableActor(ApollyonPageantApostleEntity actor) {
        return actor != null && actor.m_6084_() && actor.m_21223_() > 0.0F
                && actor.isPageantDamageable() && !actor.isMonolithProtected();
    }

    public boolean keepsBossHidden() {
        return this.state == SUMMONING || this.state == FIRST_TRIO
                || this.state == GLORIOUS_STAGE || this.state == SECOND_TRIO
                || this.state == THIRD_DPS || this.state == FOURTH_STAGE;
    }

    public boolean isLethalBoundary() {
        return this.state == SUMMONING || this.state == FIRST_TRIO
                || this.state == GLORIOUS_STAGE || this.state == SECOND_TRIO
                || this.state == THIRD_DPS || this.state == FOURTH_STAGE
                || this.state == FIFTH_STAGE;
    }

    public void discardFromKill() {
        if (this.boss.m_9236_() instanceof ServerLevel level) {
            this.boss.clearCombatForPageant();
            this.clearEncounter(level, true);
        }
        this.boss.m_146870_();
    }

    public int state() {
        return this.state;
    }

    public void read(CompoundTag tag, boolean hasExplicitCombatPhase) {
        this.testPhase = Mth.m_14045_(tag.m_128451_(TEST_PHASE_TAG), 1, 5);
        if (!tag.m_128441_(TEST_PHASE_TAG)) {
            this.testPhase = 1;
        }
        int savedState = tag.m_128451_(STATE_TAG);
        this.checkpointPhase = ApollyonConfig.hardMode() ? 1
                : tag.m_128441_(CHECKPOINT_TAG)
                    ? Mth.m_14045_(tag.m_128451_(CHECKPOINT_TAG), 1, 5)
                    : Math.max(1, phaseForState(savedState));
        if (!hasExplicitCombatPhase && savedState == LEGACY_PHASE_TWO_STATE) {
            // One-time migration from the old overloaded state machine, where 5
            // meant combat phase two rather than a pageant stage.
            this.boss.setCombatPhase(ApollyonEntity.COMBAT_PHASE_TWO);
            this.setState(INACTIVE, 1.0F);
        } else if (savedState == INACTIVE) {
            this.setState(INACTIVE, 1.0F);
        } else {
            // Active pageants always resume through their clean retry entrance.
            this.boss.setCombatPhase(ApollyonEntity.COMBAT_PHASE_ONE);
            this.setState(PENDING_RESTART, 0.0F);
            this.cleanupAfterLoad = true;
        }
    }

    public void write(CompoundTag tag) {
        tag.m_128405_(STATE_TAG, this.state);
        tag.m_128405_(TEST_PHASE_TAG, this.testPhase);
        tag.m_128405_(CHECKPOINT_TAG, ApollyonConfig.hardMode() ? 1 : this.checkpointPhase);
    }

    private void beginTransition() {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        this.clearEncounter(level, true);
        this.boss.clearCombatForPageant();
        if (!ApollyonConfig.transitionPageant()) {
            // Enter at the return cue, without the fade-out or summoning delay.
            // Healing runs alongside the final ceremony for its first 60 ticks.
            this.haloOrbitEpoch = level.m_46467_();
            this.setState(FIFTH_STAGE, 1.0F);
            this.stateTicks = FIFTH_RETURN_TICK;
            this.directTransitionHealTicks = FADE_TICKS;
            this.returnForFifthStage(level);
            return;
        }
        this.setState(FADE_OUT, 1.0F);
        this.boss.holdForPageantTransition();
    }

    private void tickFade(ServerLevel level, List<ServerPlayer> players, LivingEntity target) {
        ++this.stateTicks;
        this.boss.holdForPageantTransition();
        ServerParticleUtil.windParticle(level, ColorUtil.BLACK, 2.0F, 1.5F,
                this.boss.m_19879_(), this.boss.m_20182_());
        ServerParticleUtil.windParticle(level, ColorUtil.BLACK, 4.0F, 0.5F,
                this.boss.m_19879_(), this.boss.m_20182_());
        float progress = Math.min(1.0F, this.stateTicks / (float) FADE_TICKS);
        this.healTransition(FADE_TICKS - this.stateTicks + 1);
        this.boss.setPageantOpacity(1.0F - progress);
        if (this.stateTicks >= FADE_TICKS) {
            this.boss.m_21153_(this.boss.m_21233_());
            this.playDepartureEffect(level, this.boss.m_20182_());
            this.boss.snapHiddenForPageant();
            this.sendPageantMessage(
                    players,
                    "message.starfantasy_goety.apollyon.boundary_execution_warning",
                    ChatFormatting.RED);
            this.beginConfiguredPageant(level, target);
        }
    }

    private void healTransition(int remainingTicks) {
        float missingHealth = Math.max(0.0F, this.boss.m_21233_() - this.boss.m_21223_());
        this.boss.m_21153_(this.boss.m_21223_()
                + missingHealth / Math.max(1, remainingTicks));
    }

    private void beginConfiguredPageant(ServerLevel level, LivingEntity target) {
        this.haloOrbitEpoch = level.m_46467_();
        java.util.Arrays.fill(this.occupiedOuterHaloAnchors, false);
        int phase = this.restartPhase();
        if (phase >= 5) {
            this.beginFifthStage(level);
        } else if (phase >= 4) {
            this.beginFourthStage(level, target);
        } else if (phase >= 3) {
            this.beginThirdStage(level, target);
        } else if (phase >= 2) {
            this.beginGloriousStage(level);
        } else {
            this.beginSummoning(level, target);
        }
    }

    private void beginSummoning(ServerLevel level, LivingEntity target) {
        this.boss.clearCombatForPageant();
        this.boss.holdHiddenForPageant();
        this.boss.m_21153_(this.boss.m_21233_());
        Vec3 home = this.boss.arenaHomePosition();
        double baseAngle = target == null
                ? 0.0D
                : Math.atan2(target.m_20189_() - home.f_82481_,
                        target.m_20185_() - home.f_82479_);
        for (int i = 0; i < this.actorPositions.length; ++i) {
            double angle = baseAngle + Math.PI * 2.0D * i / this.actorPositions.length;
            double x = home.f_82479_ + Math.cos(angle) * ACTOR_DISTANCE;
            double z = home.f_82481_ + Math.sin(angle) * ACTOR_DISTANCE;
            this.actorPositions[i] = groundCenterAt(level, x, home.f_82480_ + 8.0D, z);
            int duration = i == 0 ? PROFANE_SUMMON_TICK : FOLLOWUP_SUMMON_TICK;
            ApollyonPageantSummonEntity.spawn(this.boss, this.actorPositions[i], duration);
        }
        this.playSummonStartSound(level, home);
        this.setState(SUMMONING, 0.0F);
    }

    private void tickSummoning(ServerLevel level, List<ServerPlayer> players) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();
        if (this.stateTicks == PROFANE_SUMMON_TICK) {
            ApollyonPageantApostleEntity profane = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.actorPositions[0],
                    ApollyonPageantApostleEntity.PROFANE, false);
            this.profaneUuid = uuid(profane);
            if (profane != null) {
                this.spawnActorArrivalSmoke(level, profane.m_20182_());
                profane.startCasting(PROFANE_CAST_END_TICK - PROFANE_SUMMON_TICK);
                this.playSummonApostleSound(level, this.actorPositions[0]);
            }
        }
        if (this.stateTicks == PROFANE_EFFECT_TICK) {
            ApollyonPageantApostleEntity profane = actor(level, this.profaneUuid);
            this.playProfaneCastSound(level, profane);
            this.applyFamine(players);
            this.spawnFamineWave(level);
            this.sendPageantMessage(
                    players,
                    "message.starfantasy_goety.apollyon.famine_warning",
                    ChatFormatting.GREEN);
        }
        if (this.stateTicks == PROFANE_CAST_END_TICK) {
            ApollyonPageantApostleEntity profane = actor(level, this.profaneUuid);
            if (profane != null) {
                profane.stopCasting();
            }
        }
        if (this.stateTicks == FOLLOWUP_SUMMON_TICK) {
            this.departActor(level, this.profaneUuid);
            this.profaneUuid = null;

            ApollyonPageantApostleEntity pyre = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.actorPositions[1],
                    ApollyonPageantApostleEntity.PYRE_LORD, false);
            this.pyreLordUuid = uuid(pyre);
            if (pyre != null) {
                this.spawnActorArrivalSmoke(level, pyre.m_20182_());
                pyre.startCasting(METEOR_IMPACT_TICK);
            }
            ApollyonPageantApostleEntity terrible = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.actorPositions[2],
                    ApollyonPageantApostleEntity.TERRIBLE, false);
            this.terribleUuid = uuid(terrible);
            if (terrible != null) {
                this.spawnActorArrivalSmoke(level, terrible.m_20182_());
            }
            if (pyre != null || terrible != null) {
                this.playSummonApostleSound(level, this.boss.arenaHomePosition());
            }
            this.setState(FIRST_TRIO, 0.0F);
        }
    }

    private void tickFirstTrio(
            ServerLevel level, List<ServerPlayer> players, LivingEntity target) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();

        for (int fireTick : FIRE_TRAP_TICKS) {
            if (this.stateTicks == fireTick) {
                ApollyonFireTrapManager.castPageantTriple(this.boss, target);
                break;
            }
        }
        if (this.stateTicks == METEOR_WARNING_TICK) {
            this.meteorAnchors.clear();
            for (Vec3 point : ApollyonFireTrapManager.pageantTriplePositions(
                    this.boss.arenaHomePosition(), target.m_20182_())) {
                Vec3 anchor = groundCenterAt(level, point.f_82479_,
                        point.f_82480_ + 8.0D, point.f_82481_);
                this.meteorAnchors.add(anchor);
                StarFantasyVfx.groundWarningCircleOwnedStatic(
                        this.boss, anchor.m_82520_(0.0D, 0.06D, 0.0D),
                        METEOR_WARNING_TICKS, METEOR_RADIUS, WarningColor.RED);
            }
        }
        if (this.stateTicks == METEOR_SPAWN_TICK) {
            for (Vec3 anchor : this.meteorAnchors) {
                this.meteorUuids.add(uuid(ApollyonPageantMeteorEntity.spawn(this.boss, anchor)));
            }
        }
        if (this.stateTicks == METEOR_IMPACT_TICK) {
            this.impactMeteor(level, players);
        }
        if (this.stateTicks == lightningDamageTick()) {
            ApollyonPageantApostleEntity terrible = actor(level, this.terribleUuid);
            if (terrible != null) {
                terrible.finishCasting();
            }
            this.resolveTerribleLightning(level, players);
            this.shakeArena();
            this.spawnLightningRing(level, 0.0D, 1);
            for (Vec3 anchor : this.meteorAnchors) {
                StarFantasyVfx.groundWarningCircleOwnedStatic(
                        this.boss, anchor.m_82520_(0.0D, 0.08D, 0.0D),
                        magmaWarningTicks(), METEOR_RADIUS, WarningColor.RED);
            }
        } else if (this.stateTicks == lightningMiddleRingTick()) {
            this.spawnLightningRing(level, 7.0D, 8);
        } else if (this.stateTicks == lightningOuterRingTick()) {
            this.spawnLightningRing(level, 14.0D, 15);
        } else if (this.stateTicks >= magmaExplosionTick()) {
            this.finishFirstPageant(level);
        }
    }

    private LivingEntity ensureTarget() {
        LivingEntity current = this.boss.m_5448_();
        if (this.isValidPageantTarget(current)) {
            return current;
        }
        List<LivingEntity> candidates = arenaLivingTargets(this.boss).stream()
                .filter(this::isValidPageantTarget).toList();
        LivingEntity fallback = candidates.stream()
                .filter(ServerPlayer.class::isInstance).findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));
        this.boss.m_6710_(fallback);
        return fallback;
    }

    private boolean isValidPageantTarget(LivingEntity target) {
        if (target == null || !target.m_6084_() || target == this.boss
                || target.m_9236_() != this.boss.m_9236_()
                || !this.boss.isValidArenaTarget(target)
                || target instanceof ArmorStand || target instanceof ApollyonPageantOwned
                || target instanceof Owned owned && owned.getTrueOwner() == this.boss
                || this.boss.m_7307_(target)) {
            return false;
        }
        if (target instanceof ServerPlayer player) {
            return !player.m_7500_() && !player.m_5833_();
        }
        // Passive scenery must not keep the encounter running. Include hostile
        // mobs, melee-capable neutral mobs, and mobs already engaged in combat.
        return target instanceof Mob mob && (mob instanceof Enemy
                || mob.m_5448_() != null
                || mob.m_21051_(Attributes.f_22281_) != null
                    && mob.m_21133_(Attributes.f_22281_) > 0.0D);
    }

    private void applyFamine(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            FoodData foodData = player.m_36324_();
            foodData.m_38717_(0.0F);
            foodData.m_38705_(Math.min(foodData.m_38702_(), 4));
            player.m_147207_(new MobEffectInstance(
                    ApollyonEffectRegistry.FAMINE.get(), FAMINE_DURATION_TICKS,
                    0, false, false, true), this.boss);
        }
    }

    private void spawnFamineWave(ServerLevel level) {
        Entity profane = entity(level, this.profaneUuid);
        Vec3 center = profane == null ? this.actorPositions[0] : profane.m_20182_();
        ApollyonFamineWaveEntity.spawn(this.boss, center.m_82520_(0.0D, 0.04D, 0.0D));
        for (int i = 0; i < 48; ++i) {
            double angle = level.m_213780_().m_188500_() * Math.PI * 2.0D;
            double speed = 0.16D + level.m_213780_().m_188500_() * 0.28D;
            double radius = level.m_213780_().m_188500_() * 1.5D;
            level.m_8767_(ApollyonParticleRegistry.PROFANE_SPELL.get(),
                    center.f_82479_ + Math.cos(angle) * radius,
                    center.f_82480_ + 0.25D + level.m_213780_().m_188500_() * 1.5D,
                    center.f_82481_ + Math.sin(angle) * radius,
                    0, Math.cos(angle) * speed,
                    0.15D + level.m_213780_().m_188500_() * 0.3D,
                    Math.sin(angle) * speed, 1.0D);
        }
    }

    private void impactMeteor(ServerLevel level, List<ServerPlayer> players) {
        for (UUID meteorUuid : this.meteorUuids) {
            this.discard(level, meteorUuid);
        }
        this.meteorUuids.clear();
        if (this.meteorAnchors.isEmpty()) {
            return;
        }
        for (Vec3 anchor : this.meteorAnchors) {
            playPyroclastExplosion(level, anchor);
            this.damageMeteorArea(level, anchor);
            this.magmaUuids.add(uuid(ApollyonPageantMagmaEntity.spawn(this.boss, anchor)));
        }
        this.shakeArena();

        ApollyonPageantApostleEntity terrible = actor(level, this.terribleUuid);
        ApollyonPageantApostleEntity pyre = actor(level, this.pyreLordUuid);
        if (pyre != null) {
            pyre.finishCasting();
        }
        if (terrible != null) {
            terrible.startCasting(terribleWarningTicks());
        }
        Vec3 home = this.boss.arenaHomePosition();
        StarFantasyVfx.groundWarningCircleOwnedStatic(
                this.boss, home.m_82520_(0.0D, 0.06D, 0.0D),
                terribleWarningTicks(), ApollyonEntity.ARENA_RADIUS, dangerousWarningColor());

        for (Vec3 anchor : this.meteorAnchors) {
            Vec3 outward = horizontalDirection(home, anchor);
            float yaw = (float) Math.toDegrees(Math.atan2(-outward.f_82479_, outward.f_82481_));
            GroundRectangleWarningEntity warning = StarFantasyVfx.groundRectangleWarning(
                    this.boss, anchor.m_82520_(0.0D, 0.14D, 0.0D),
                    terribleWarningTicks(), SAFE_WIDTH, SAFE_LENGTH, yaw,
                    SAFE_COLOR, false, true);
            this.safeWarningUuids.add(uuid(warning));
        }
        this.sendPageantMessage(
                players,
                "message.starfantasy_goety.apollyon.lightning_warning",
                ChatFormatting.AQUA);
    }

    private void resolveTerribleLightning(ServerLevel level, List<ServerPlayer> players) {
        for (LivingEntity target : this.arenaLivingTargets(level)) {
            if (!this.isInMeteorSafeZone(target)) {
                this.damagePageantTarget(target, ModDamageSource.lightning(this.boss, this.boss),
                        (MobEffect) GoetyEffects.SPASMS.get());
            }
        }
    }

    private List<LivingEntity> arenaLivingTargets(ServerLevel level) {
        return arenaLivingTargets(this.boss);
    }

    public static List<LivingEntity> arenaLivingTargets(ApollyonEntity boss) {
        // Hades is a visual Entity, so the LivingEntity query excludes him automatically.
        if (!(boss.m_9236_() instanceof ServerLevel level)) {
            return List.of();
        }
        Vec3 home = boss.arenaHomePosition();
        double radius = ApollyonEntity.ARENA_RADIUS;
        return level.m_45976_(LivingEntity.class,
                new AABB(home, home).m_82377_(radius, 8.0D, radius)).stream()
                .filter(target -> target.m_6084_() && target != boss
                        && !(target instanceof ServerPlayer player
                            && (player.m_7500_() || player.m_5833_()))
                        && intersectsHorizontalCircle(target.m_20191_(), home, radius))
                .toList();
    }

    private void damagePageantTarget(LivingEntity target, DamageSource source, MobEffect effect) {
        if (ApollyonConfig.hardMode()) {
            if (target instanceof ServerPlayer player) {
                StarFantasyTrueKillHelper.trueKillPlayer(player, Float.MAX_VALUE, "apollyon_boundary");
            } else {
                target.m_6074_();
            }
            return;
        }
        // These three pageant punishments (lightning, monolith, scorpion vines)
        // originate behind each victim; preserve damage events and death prevention.
        if (target.m_6469_(ApollyonDamageSources.rear(target, source), target.m_21233_() * 5.0F) && target.m_6084_()) {
            int duration = effect == GoetyEffects.SPASMS.get() ? 300
                    : effect == GoetyEffects.ACID_VENOM.get() ? 200 : 1200;
            target.m_147207_(new MobEffectInstance(effect, duration, 3), this.boss);
            if (effect == GoetyEffects.SAPPED.get()) {
                target.m_147207_(new MobEffectInstance(GoetyEffects.STUNNED.get(), 100), this.boss);
            }
        }
    }

    private static WarningColor dangerousWarningColor() {
        return ApollyonConfig.hardMode() ? WarningColor.PURPLE : WarningColor.RED;
    }

    private void sendPageantMessage(
            List<ServerPlayer> players, String translationKey, ChatFormatting color,
            Object... arguments) {
        Component message = (arguments == null || arguments.length == 0
                ? Component.m_237115_(translationKey)
                : Component.m_237110_(translationKey, arguments)).m_130940_(color);
        for (ServerPlayer player : players) {
            player.m_5661_(message, true);
            player.m_213846_(message);
        }
    }

    private boolean isInMeteorSafeZone(LivingEntity target) {
        for (Vec3 anchor : this.meteorAnchors) {
            Vec3 direction = horizontalDirection(this.boss.arenaHomePosition(), anchor);
            double dx = target.m_20185_() - anchor.f_82479_;
            double dz = target.m_20189_() - anchor.f_82481_;
            double forward = dx * direction.f_82479_ + dz * direction.f_82481_;
            double sideways = Math.abs(-dx * direction.f_82481_ + dz * direction.f_82479_);
            if (forward >= 0.0D && forward <= SAFE_LENGTH && sideways <= SAFE_WIDTH * 0.5D) {
                return true;
            }
        }
        return false;
    }

    private void spawnLightningRing(ServerLevel level, double radius, int count) {
        Vec3 home = this.boss.arenaHomePosition();
        for (int i = 0; i < count; ++i) {
            double angle = count == 1 ? 0.0D : Math.PI * 2.0D * i / count;
            Vec3 point = home.m_82520_(
                    Math.cos(angle) * radius, 0.08D, Math.sin(angle) * radius);
            StarFantasyGoetyNetwork.sendApollyonLightningStrike(
                    level, point, PAGEANT_LIGHTNING_SIZE);
        }
    }

    private void finishFirstPageant(ServerLevel level) {
        for (Vec3 anchor : this.meteorAnchors) {
            playPyroclastExplosion(level, anchor);
            this.damageMeteorArea(level, anchor);
        }
        this.departActor(level, this.pyreLordUuid);
        this.departActor(level, this.terribleUuid);
        this.pyreLordUuid = null;
        this.terribleUuid = null;
        for (UUID magmaUuid : this.magmaUuids) {
            this.discard(level, magmaUuid);
        }
        this.magmaUuids.clear();
        this.clearEncounter(level, false);
        this.beginGloriousStage(level);
    }

    private void beginGloriousStage(ServerLevel level) {
        this.boss.clearCombatForPageant();
        this.boss.holdHiddenForPageant();
        this.boss.m_21153_(this.boss.m_21233_());
        Vec3 home = this.boss.arenaHomePosition();
        ApollyonPageantSummonEntity.spawn(this.boss, home, GLORIOUS_SUMMON_TICK);
        this.playSummonStartSound(level, home);
        this.setState(GLORIOUS_STAGE, 0.0F);
    }

    private void tickGloriousStage(
            ServerLevel level, List<ServerPlayer> players, LivingEntity target) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();
        if (this.stateTicks == GLORIOUS_SUMMON_TICK) {
            ApollyonPageantApostleEntity glorious = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.boss.arenaHomePosition(),
                    ApollyonPageantApostleEntity.GLORIOUS, false, true);
            this.gloriousUuid = uuid(glorious);
            if (glorious != null) {
                this.spawnActorArrivalSmoke(level, glorious.m_20182_());
                glorious.startCasting(GLORIOUS_CAST_TICKS);
                this.playSummonApostleSound(level, glorious.m_20182_());
            }
            StarFantasyVfx.groundWarningCircleOwnedStatic(
                    this.boss, this.boss.arenaHomePosition().m_82520_(0.0D, 0.06D, 0.0D),
                    GLORIOUS_CAST_TICKS, ApollyonEntity.ARENA_RADIUS, WarningColor.YELLOW);
            Component message = Component.m_237115_(
                    "message.starfantasy_goety.apollyon.glorious_warning")
                    .m_130940_(ChatFormatting.YELLOW);
            for (ServerPlayer player : players) {
                player.m_5661_(message, true);
                player.m_213846_(message);
            }
        }
        int gloriousCastAge = this.stateTicks - GLORIOUS_SUMMON_TICK;
        if (gloriousCastAge > 0 && gloriousCastAge < GLORIOUS_CAST_TICKS
                && gloriousCastAge % GLORIOUS_CAST_SOUND_INTERVAL == 0) {
            ApollyonPageantApostleEntity glorious = actor(level, this.gloriousUuid);
            if (glorious != null) {
                glorious.m_5496_((SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(), 2.0F, 1.0F);
            }
        }
        if (this.stateTicks >= GLORIOUS_SUMMON_TICK
                && this.stateTicks < GLORIOUS_DEPART_TICK
                && (this.stateTicks - GLORIOUS_SUMMON_TICK)
                % SECOND_TRIO_METEOR_INTERVAL == 0) {
            ApollyonMeteorManager.spawn(this.boss);
        }
        if (this.stateTicks == GLORIOUS_RESOLVE_TICK) {
            this.resolveGlorious(level, players, target);
        }
        if (this.stateTicks >= GLORIOUS_RESOLVE_TICK
                && this.stateTicks < GLORIOUS_RESOLVE_TICK + GLORIOUS_SLIDE_TICKS) {
            this.tickGloriousSlides(this.stateTicks - GLORIOUS_RESOLVE_TICK);
        }
        if (this.stateTicks == GLORIOUS_STOP_CAST_TICK) {
            ApollyonPageantApostleEntity glorious = actor(level, this.gloriousUuid);
            if (glorious != null) {
                glorious.stopCasting();
            }
        }
        if (this.stateTicks >= GLORIOUS_DEPART_TICK) {
            this.departActor(level, this.gloriousUuid);
            this.gloriousUuid = null;
            this.beginSecondTrio(level);
        }
    }

    private void resolveGlorious(
            ServerLevel level, List<ServerPlayer> players, LivingEntity target) {
        ApollyonPageantApostleEntity glorious = actor(level, this.gloriousUuid);
        Vec3 home = this.boss.arenaHomePosition();
        Vec3 soundPosition = glorious == null ? home : glorious.m_20182_();
        level.m_5594_(null, BlockPos.m_274561_(
                        soundPosition.f_82479_, soundPosition.f_82480_, soundPosition.f_82481_),
                ApollyonSoundRegistry.CAST_GLORIOUS.get(),
                SoundSource.HOSTILE, 2.0F, 1.0F);
        StarFantasyVfx.horizontalRoarWave(
                this.boss, home.m_82520_(0.0D, 0.06D, 0.0D),
                0.1D, 10, ApollyonEntity.ARENA_RADIUS);
        ApollyonGloriousSphereEntity.spawn(this.boss, home.m_82520_(0.0D, 1.0D, 0.0D));

        boolean reducedKnockback = glorious != null
                && glorious.m_21223_() <= glorious.m_21233_() * 0.1F;
        this.gloriousSlides.clear();
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            double dx = player.m_20185_() - home.f_82479_;
            double dz = player.m_20189_() - home.f_82481_;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double directionX = distance > 1.0E-7D ? dx / distance : 1.0D;
            double directionZ = distance > 1.0E-7D ? dz / distance : 0.0D;
            double knockDistance = reducedKnockback
                    ? Math.max(0.0D, ApollyonEntity.ARENA_RADIUS - distance) * 0.8D
                    : GLORIOUS_FAILURE_KNOCK_DISTANCE;
            this.gloriousSlides.put(player.m_20148_(), new PlayerSlide(
                    directionX, directionZ, knockDistance));
            player.m_20256_(Vec3.f_82478_);
            player.f_19812_ = true;
            player.f_19864_ = true;
        }

        double baseAngle = target == null
                ? 0.0D
                : Math.atan2(target.m_20189_() - home.f_82481_,
                        target.m_20185_() - home.f_82479_);
        for (int i = 0; i < this.actorPositions.length; ++i) {
            double angle = baseAngle + Math.PI * 2.0D * i / this.actorPositions.length;
            this.secondActorBaseAngles[i] = angle;
            this.actorPositions[i] = groundCenterAt(level,
                    home.f_82479_ + Math.cos(angle) * SECOND_ACTOR_DISTANCE,
                    home.f_82480_ + 8.0D,
                    home.f_82481_ + Math.sin(angle) * SECOND_ACTOR_DISTANCE);
            ApollyonPageantSummonEntity.spawn(
                    this.boss, this.actorPositions[i], GLORIOUS_DEPART_TICK - GLORIOUS_RESOLVE_TICK);
        }
        this.playSummonStartSound(level, home);
    }

    private void tickGloriousSlides(int slideTick) {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, PlayerSlide> entry : this.gloriousSlides.entrySet()) {
            Entity entity = level.m_8791_(entry.getKey());
            if (!(entity instanceof LivingEntity player) || !player.m_6084_()) {
                continue;
            }
            PlayerSlide slide = entry.getValue();
            double stepWeight = slideTick < 10
                    ? 1.0D
                    : Math.max(0.0D, (19.0D - slideTick) / 10.0D);
            double speed = slide.distance() * stepWeight / 14.5D;
            Vec3 currentMovement = player.m_20184_();
            player.m_20256_(new Vec3(
                    slide.directionX() * speed,
                    currentMovement.f_82480_,
                    slide.directionZ() * speed));
            player.f_19789_ = 0.0F;
            player.f_19864_ = true;
            player.f_19812_ = true;
        }
    }

    private void beginSecondTrio(ServerLevel level) {
        this.releaseGloriousSlides(level);
        this.secondOrbitDegrees = 0.0D;
        this.beamHitCooldowns.clear();
        this.setState(SECOND_TRIO, 0.0F);
        int[] variants = {
                ApollyonPageantApostleEntity.DARK,
                ApollyonPageantApostleEntity.GREAT_SHADOW,
                ApollyonPageantApostleEntity.ABHORRENT
        };
        for (int i = 0; i < variants.length; ++i) {
            ApollyonPageantApostleEntity actor = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.actorPositions[i], variants[i], false);
            this.secondActorUuids[i] = uuid(actor);
            if (actor == null) {
                continue;
            }
            this.spawnActorArrivalSmoke(level, actor.m_20182_());
            actor.startCasting(SECOND_TRIO_BEAM_END_TICK);
            float yaw = actor.pageantBodyYaw();
            StarFantasyVfx.groundRectangleWarning(
                    this.boss, actor.m_20182_().m_82520_(0.0D, 0.08D, 0.0D),
                    SECOND_TRIO_WARNING_TICKS,
                    SECOND_TRIO_WARNING_WIDTH, SECOND_TRIO_BEAM_LENGTH,
                    yaw, SECOND_TRIO_WARNING_COLOR, false, true);
        }
        this.playSummonApostleSound(level, this.boss.arenaHomePosition());
        ApollyonMeteorManager.spawn(this.boss);
    }

    private void tickSecondTrio(ServerLevel level, List<ServerPlayer> players) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();
        if (this.stateTicks > 0 && this.stateTicks < SECOND_TRIO_BEAM_END_TICK
                && this.stateTicks % SECOND_TRIO_METEOR_INTERVAL == 0) {
            ApollyonMeteorManager.spawn(this.boss);
        }
        if (this.stateTicks == SECOND_TRIO_WARNING_TICKS) {
            boolean loopSoundStarted = false;
            for (int i = 0; i < this.secondActorUuids.length; ++i) {
                ApollyonPageantApostleEntity actor = actor(level, this.secondActorUuids[i]);
                ApollyonPageantBeamEntity beam = ApollyonPageantBeamEntity.spawn(
                        this.boss, actor, SECOND_TRIO_BEAM_DURATION);
                this.secondBeamUuids[i] = uuid(beam);
                if (beam != null && !loopSoundStarted) {
                    ModNetwork.sentToTrackingEntity(beam, new SPlayLoopSoundPacket(
                            beam, ModSounds.CORRUPT_BEAM_LOOP.get(), 2.0F, 1.0F));
                    loopSoundStarted = true;
                }
            }
        }
        if (this.stateTicks >= SECOND_TRIO_WARNING_TICKS
                && this.stateTicks < SECOND_TRIO_BEAM_END_TICK) {
            this.tickSecondTrioSectorAttacks(
                    this.stateTicks - SECOND_TRIO_WARNING_TICKS);
            this.tickSecondTrioOrbit(level);
            if (this.stateTicks < SECOND_TRIO_DAMAGE_END_TICK) {
                this.damageSecondTrioBeams(level, players);
            }
        }
        if (this.stateTicks == SECOND_TRIO_BEAM_END_TICK) {
            for (int i = 0; i < this.secondActorUuids.length; ++i) {
                ApollyonPageantApostleEntity actor = actor(level, this.secondActorUuids[i]);
                if (actor != null) {
                    actor.stopCasting();
                    actor.setPageantPosition(actor.m_20182_(), actor.pageantBodyYaw(), false);
                }
                this.discard(level, this.secondBeamUuids[i]);
                this.secondBeamUuids[i] = null;
            }
            this.clearSecondTrioMeteors(level);
        }
        if (this.stateTicks >= SECOND_TRIO_DEPART_TICK) {
            for (int i = 0; i < this.secondActorUuids.length; ++i) {
                this.departActor(level, this.secondActorUuids[i]);
                this.secondActorUuids[i] = null;
            }
            this.beginThirdStage(level, this.ensureTarget());
        }
    }

    private void tickSecondTrioSectorAttacks(int beamAge) {
        Vec3 home = this.boss.arenaHomePosition();
        for (int constantStartAge : SECOND_TRIO_CONSTANT_START_AGES) {
            if (beamAge == constantStartAge - SECOND_TRIO_SECTOR_WARNING_TICKS) {
                this.warnSecondTrioSectors(home, 0.0F);
            }
            for (int wave = 0; wave < 3; ++wave) {
                int attackAge = constantStartAge + wave * SECOND_TRIO_SECTOR_INTERVAL;
                if (beamAge != attackAge) {
                    continue;
                }
                float rotation = wave * SECOND_TRIO_SECTOR_ROTATION_STEP;
                ApollyonVoidRayManager.detonatePattern(
                        this.boss, home, rotation,
                        SECOND_TRIO_SECTOR_RADIUS, SECOND_TRIO_SECTOR_ANGLE,
                        SECOND_TRIO_SECTOR_SPACING, SECOND_TRIO_SECTOR_COUNT);
                if (wave < 2) {
                    this.warnSecondTrioSectors(
                            home, rotation + SECOND_TRIO_SECTOR_ROTATION_STEP);
                }
            }
        }
    }

    private void warnSecondTrioSectors(Vec3 home, float rotation) {
        ApollyonVoidRayManager.warnPattern(
                this.boss, home, rotation, SECOND_TRIO_SECTOR_WARNING_TICKS,
                SECOND_TRIO_SECTOR_RADIUS, SECOND_TRIO_SECTOR_ANGLE,
                SECOND_TRIO_SECTOR_SPACING, SECOND_TRIO_SECTOR_COUNT);
    }

    private void tickSecondTrioOrbit(ServerLevel level) {
        int beamAge = this.stateTicks - SECOND_TRIO_WARNING_TICKS;
        double angularSpeed;
        if (beamAge < 20) {
            angularSpeed = Mth.m_14139_(beamAge / 19.0D,
                    0.0D, SECOND_TRIO_MAX_ANGULAR_SPEED);
        } else if (beamAge < 60) {
            angularSpeed = SECOND_TRIO_MAX_ANGULAR_SPEED;
        } else if (beamAge < 100) {
            angularSpeed = Mth.m_14139_((beamAge - 60.0D) / 39.0D,
                    SECOND_TRIO_MAX_ANGULAR_SPEED,
                    -SECOND_TRIO_MAX_ANGULAR_SPEED);
        } else if (beamAge < 140) {
            angularSpeed = -SECOND_TRIO_MAX_ANGULAR_SPEED;
        } else {
            angularSpeed = Mth.m_14139_((beamAge - 140.0D) / 19.0D,
                    -SECOND_TRIO_MAX_ANGULAR_SPEED, 0.0D);
        }
        this.secondOrbitDegrees += angularSpeed;
        Vec3 home = this.boss.arenaHomePosition();
        double rotation = Math.toRadians(this.secondOrbitDegrees);
        for (int i = 0; i < this.secondActorUuids.length; ++i) {
            ApollyonPageantApostleEntity actor = actor(level, this.secondActorUuids[i]);
            if (actor == null) {
                continue;
            }
            double angle = this.secondActorBaseAngles[i] + rotation;
            Vec3 position = new Vec3(
                    home.f_82479_ + Math.cos(angle) * SECOND_ACTOR_DISTANCE,
                    this.actorPositions[i].f_82480_,
                    home.f_82481_ + Math.sin(angle) * SECOND_ACTOR_DISTANCE);
            float yaw = (float) Math.toDegrees(Math.atan2(
                    -(position.f_82479_ - home.f_82479_),
                    position.f_82481_ - home.f_82481_));
            actor.setPageantPosition(position, yaw, true);
        }
    }

    private void damageSecondTrioBeams(
            ServerLevel level, List<ServerPlayer> players) {
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            int nextHitTick = this.beamHitCooldowns.getOrDefault(player.m_20148_(), 0);
            if (this.stateTicks < nextHitTick) {
                continue;
            }
            boolean hit = false;
            for (UUID actorUuid : this.secondActorUuids) {
                ApollyonPageantApostleEntity actor = actor(level, actorUuid);
                if (actor != null && intersectsBeam(player, actor)) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                boolean hardMode = ApollyonConfig.hardMode();
                DamageSource source = hardMode
                        ? player.m_269291_().m_269341_()
                        : player.m_269291_().m_269425_();
                player.m_6469_(source, hardMode
                        ? SECOND_TRIO_BEAM_HARD_DAMAGE : SECOND_TRIO_BEAM_DAMAGE);
                this.beamHitCooldowns.put(
                        player.m_20148_(), this.stateTicks + SECOND_TRIO_BEAM_HIT_COOLDOWN);
            }
        }
    }

    private static boolean intersectsBeam(
            LivingEntity player, ApollyonPageantApostleEntity actor) {
        float radians = actor.pageantBodyYaw() * ((float) Math.PI / 180.0F);
        double directionX = -Mth.m_14031_(radians);
        double directionZ = Mth.m_14089_(radians);
        double dx = player.m_20185_() - actor.m_20185_();
        double dz = player.m_20189_() - actor.m_20189_();
        double forward = dx * directionX + dz * directionZ;
        double sideways = Math.abs(-dx * directionZ + dz * directionX);
        double playerRadius = player.m_20205_() * 0.5D;
        return forward >= -playerRadius
                && forward <= SECOND_TRIO_BEAM_LENGTH + playerRadius
                && sideways <= SECOND_TRIO_WARNING_WIDTH * 0.5D + playerRadius;
    }

    private void clearSecondTrioMeteors(ServerLevel level) {
        AABB area = new AABB(
                this.boss.arenaHomePosition(), this.boss.arenaHomePosition()).m_82400_(128.0D);
        for (com.starfantasy.goety.entity.ApollyonStarArrowEntity arrow
                : level.m_45976_(com.starfantasy.goety.entity.ApollyonStarArrowEntity.class, area)) {
            if (arrow.m_19749_() == this.boss) {
                arrow.m_146870_();
            }
        }
        StarFantasyVfx.clearWarningsForOwner(this.boss);
    }

    private void beginThirdStage(ServerLevel level, LivingEntity target) {
        this.boss.clearCombatForPageant();
        this.boss.holdHiddenForPageant();
        Vec3 home = this.boss.arenaHomePosition();
        Vec3 forward = target == null
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : horizontalDirection(home, target.m_20182_());
        Vec3 left = new Vec3(-forward.f_82481_, 0.0D, forward.f_82479_);
        for (int i = 0; i < this.thirdActorPositions.length; ++i) {
            double side = i == 0 ? 1.0D : -1.0D;
            this.thirdActorPositions[i] = groundCenterAt(
                    level,
                    home.f_82479_ + left.f_82479_ * THIRD_ACTOR_DISTANCE * side,
                    home.f_82480_ + 8.0D,
                    home.f_82481_ + left.f_82481_ * THIRD_ACTOR_DISTANCE * side);
            ApollyonPageantSummonEntity.spawn(
                    this.boss, this.thirdActorPositions[i], THIRD_SUMMON_TICK);
        }
        ApollyonPageantObsidianMonolithEntity monolith =
                ApollyonPageantObsidianMonolithEntity.spawn(
                        this.boss, groundCenterAt(level, home.f_82479_,
                                home.f_82480_ + 8.0D, home.f_82481_));
        this.thirdMonolithUuid = uuid(monolith);
        this.thirdLinkedIndex = -1;
        this.thirdEnraged = false;
        this.playSummonStartSound(level, home);
        Component message = Component.m_237115_(
                "message.starfantasy_goety.apollyon.third_stage_warning")
                .m_130940_(ChatFormatting.DARK_PURPLE);
        for (ServerPlayer player : this.boss.validArenaPlayers()) {
            player.m_5661_(message, true);
            player.m_213846_(message);
        }
        this.setState(THIRD_DPS, 0.0F);
    }

    private void tickThirdDps(
            ServerLevel level, List<ServerPlayer> players, LivingEntity target) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();
        if (this.stateTicks == THIRD_SUMMON_TICK) {
            int[] variants = {
                    ApollyonPageantApostleEntity.RISEN,
                    ApollyonPageantApostleEntity.WITCH_KING
            };
            for (int i = 0; i < variants.length; ++i) {
                ApollyonPageantApostleEntity archer = ApollyonPageantApostleEntity.spawn(
                        this.boss, this.thirdActorPositions[i], variants[i], false, true);
                this.thirdActorUuids[i] = uuid(archer);
                if (archer != null) {
                    archer.setPageantTarget(target);
                    this.spawnActorArrivalSmoke(level, archer.m_20182_());
                }
            }
            this.playSummonApostleSound(level, this.boss.arenaHomePosition());
            this.thirdLinkedIndex = this.boss.m_217043_().m_188503_(2);
            this.applyThirdLink(level);
        }

        if (this.stateTicks >= THIRD_SUMMON_TICK) {
            this.updateThirdDeaths(level);
            int aliveActors = this.thirdAliveActorCount(level);
            if (aliveActors == 0) {
                this.breakThirdMonolith(level);
                this.beginFourthStage(level, target);
                return;
            }
            if (!this.thirdEnraged && aliveActors == 1) {
                this.beginThirdEnrage(level);
            }
            for (UUID actorUuid : this.thirdActorUuids) {
                ApollyonPageantApostleEntity archer = actor(level, actorUuid);
                if (archer != null && archer.m_6084_()) {
                    archer.setPageantTarget(target);
                }
            }
            int meteorInterval = this.thirdEnraged ? 5 : THIRD_METEOR_INTERVAL;
            if ((this.stateTicks - THIRD_SUMMON_TICK) % meteorInterval == 0) {
                ApollyonMeteorManager.spawn(this.boss);
            }
            if (!this.thirdEnraged && this.stateTicks > THIRD_SUMMON_TICK
                    && (this.stateTicks - THIRD_SUMMON_TICK) % THIRD_LINK_INTERVAL == 0) {
                this.thirdLinkedIndex = 1 - this.thirdLinkedIndex;
            }
            this.applyThirdLink(level);
        }

        if (this.stateTicks == THIRD_ENRAGE_TICK) {
            StarFantasyVfx.groundWarningCircleOwnedStatic(
                    this.boss,
                    this.boss.arenaHomePosition().m_82520_(0.0D, 0.06D, 0.0D),
                    THIRD_ENRAGE_WARNING_TICKS,
                    ApollyonEntity.ARENA_RADIUS,
                    dangerousWarningColor());
        }
        if (this.stateTicks >= THIRD_ENRAGE_TICK + THIRD_ENRAGE_WARNING_TICKS) {
            this.resolveThirdEnrage(level, players);
        }
    }

    private void updateThirdDeaths(ServerLevel level) {
        for (int i = 0; i < this.thirdActorUuids.length; ++i) {
            UUID actorUuid = this.thirdActorUuids[i];
            if (actorUuid == null) {
                continue;
            }
            ApollyonPageantApostleEntity archer = actor(level, actorUuid);
            if (archer != null && archer.m_6084_() && archer.m_21223_() > 0.0F) {
                continue;
            }
            if (archer != null) {
                this.departActor(level, actorUuid);
            } else if (this.thirdActorPositions[i] != null) {
                int variant = i == 0
                        ? ApollyonPageantApostleEntity.RISEN
                        : ApollyonPageantApostleEntity.WITCH_KING;
                this.spawnDepartedHalo(level, this.thirdActorPositions[i], variant);
            }
            this.thirdActorUuids[i] = null;
        }
    }

    private void applyThirdLink(ServerLevel level) {
        ApollyonPageantObsidianMonolithEntity monolith =
                thirdMonolith(level, this.thirdMonolithUuid);
        boolean rolesActive = !this.thirdEnraged && monolith != null
                && this.thirdAliveActorCount(level) == 2;
        ApollyonPageantApostleEntity linked = null;
        for (int i = 0; i < this.thirdActorUuids.length; ++i) {
            ApollyonPageantApostleEntity archer = actor(level, this.thirdActorUuids[i]);
            boolean selected = rolesActive && i == this.thirdLinkedIndex
                    && archer != null && archer.m_6084_() && archer.m_21223_() > 0.0F;
            if (archer != null) {
                archer.setMonolithProtected(selected);
                archer.setProtectedRedirectTarget(selected
                        ? actor(level, this.thirdActorUuids[1 - i]) : null);
                archer.setThirdStageEnraged(this.thirdEnraged);
                archer.setMonolithChanneling(
                        monolith, rolesActive && !selected);
            }
            if (selected) {
                linked = archer;
            }
        }
        if (monolith != null) {
            monolith.setLinkedApostle(linked);
        }
    }

    private int thirdAliveActorCount(ServerLevel level) {
        int alive = 0;
        for (UUID actorUuid : this.thirdActorUuids) {
            ApollyonPageantApostleEntity archer = actor(level, actorUuid);
            if (archer != null && archer.m_6084_() && archer.m_21223_() > 0.0F) {
                ++alive;
            }
        }
        return alive;
    }

    private void beginThirdEnrage(ServerLevel level) {
        this.thirdEnraged = true;
        this.thirdLinkedIndex = -1;
        this.applyThirdLink(level);
    }

    private void breakThirdMonolith(ServerLevel level) {
        ApollyonPageantObsidianMonolithEntity monolith =
                thirdMonolith(level, this.thirdMonolithUuid);
        if (monolith != null) {
            BlockPos base = BlockPos.m_274561_(
                    monolith.m_20185_(), monolith.m_20186_(), monolith.m_20189_());
            for (int y = 0; y < 4; ++y) {
                level.m_46796_(2001, base.m_6630_(y),
                        Block.m_49956_(Blocks.f_50080_.m_49966_()));
            }
            monolith.m_146870_();
        }
        this.thirdMonolithUuid = null;
    }

    private void resolveThirdEnrage(ServerLevel level, List<ServerPlayer> players) {
        Vec3 home = this.boss.arenaHomePosition();
        level.m_5594_(null, BlockPos.m_274561_(
                        home.f_82479_, home.f_82480_, home.f_82481_),
                ApollyonSoundRegistry.CAST_OBSIDIAN.get(),
                SoundSource.HOSTILE, 3.0F, 1.0F);
        StarFantasyVfx.finalExplosion(
                level, home, THIRD_EXPLOSION_COLOR, THIRD_EXPLOSION_ALPHA,
                THIRD_EXPLOSION_SIZE, true, THIRD_EXPLOSION_LIFETIME);
        StarFantasyVfx.areaImpactShake(
                this.boss, home, 64.0D, 0, THIRD_SHAKE_HOLD_TICKS,
                THIRD_SHAKE_FADE_TICKS, THIRD_SHAKE_INTENSITY);
        this.breakThirdMonolith(level);
        for (int i = 0; i < this.thirdActorUuids.length; ++i) {
            this.departActor(level, this.thirdActorUuids[i]);
            this.thirdActorUuids[i] = null;
        }
        for (LivingEntity target : this.arenaLivingTargets(level)) {
            this.damagePageantTarget(target,
                    this.boss.m_269291_().m_269036_(this.boss, this.boss),
                    (MobEffect) GoetyEffects.SAPPED.get());
        }
        LivingEntity survivor = this.ensureTarget();
        if (survivor == null) {
            this.resetToPending(level);
        } else {
            this.beginFourthStage(level, survivor);
        }
    }

    private void beginFourthStage(ServerLevel level, LivingEntity target) {
        this.releaseFourthSlides(level);
        this.clearSecondTrioMeteors(level);
        this.boss.clearCombatForPageant();
        this.boss.holdHiddenForPageant();
        Vec3 home = this.boss.arenaHomePosition();
        double baseAngle = target == null
                ? 0.0D
                : Math.atan2(target.m_20189_() - home.f_82481_,
                        target.m_20185_() - home.f_82479_);
        for (int i = 0; i < this.fourthActorPositions.length; ++i) {
            double angle = baseAngle + Math.PI * 2.0D * i / this.fourthActorPositions.length;
            double distance = i == 2 ? 0.0D : FOURTH_ACTOR_DISTANCE;
            this.fourthActorPositions[i] = groundCenterAt(
                    level,
                    home.f_82479_ + Math.cos(angle) * distance,
                    home.f_82480_ + 8.0D,
                    home.f_82481_ + Math.sin(angle) * distance);
            int duration = i < 2
                    ? FOURTH_SUMMON_TICK : fourthAtrociousSummonTick();
            ApollyonPageantSummonEntity.spawn(
                    this.boss, this.fourthActorPositions[i], duration);
        }
        this.playSummonStartSound(level, home);
        this.setState(FOURTH_STAGE, 0.0F);
    }

    private void tickFourthStage(ServerLevel level, List<ServerPlayer> players) {
        ++this.stateTicks;
        this.boss.holdHiddenForPageant();
        if (this.stateTicks == FOURTH_SUMMON_TICK) {
            this.spawnFourthFirstActors(level);
            this.startFourthFirstWarnings(level);
        }
        if (this.stateTicks == fourthFirstResolveTick()) {
            this.resolveFourthFirstAttacks(level, players);
        }
        for (int i = 0; i < FOURTH_VINE_RING_RADII.length; ++i) {
            if (this.stateTicks == fourthFirstResolveTick() + i) {
                double radius = FOURTH_VINE_RING_RADII[i];
                int count = Mth.m_14107_(radius * 1.5D) + 1;
                ApollyonWildSurgeManager.spawnPageantThornVisualRing(
                        this.boss, this.boss.arenaHomePosition(),
                        radius, count, 2.0F);
            }
        }
        if (this.stateTicks == fourthAtrociousSummonTick()) {
            this.spawnFourthAtrocious(level, players);
        }
        if (this.stateTicks == fourthKnockbackTick()) {
            this.resolveFourthKnockback(level, players);
        }
        if (this.stateTicks >= fourthKnockbackTick()
                && this.stateTicks < fourthKnockbackTick() + FOURTH_SLIDE_TICKS) {
            this.tickFourthSlides(this.stateTicks - fourthKnockbackTick());
        }
        if (this.stateTicks == fourthKnockbackTick() + FOURTH_SLIDE_TICKS) {
            this.releaseFourthSlides(level);
        }
        if (this.stateTicks >= fourthIceExplosionTick()) {
            this.resolveFourthIceExplosions(level, players);
            this.departActor(level, this.fourthActorUuids[2]);
            this.fourthActorUuids[2] = null;
            this.beginFifthStage(level);
        }
    }

    private void spawnFourthFirstActors(ServerLevel level) {
        int[] variants = {
                ApollyonPageantApostleEntity.DEFILER,
                ApollyonPageantApostleEntity.CRUEL
        };
        for (int i = 0; i < variants.length; ++i) {
            ApollyonPageantApostleEntity actor = ApollyonPageantApostleEntity.spawn(
                    this.boss, this.fourthActorPositions[i], variants[i], false);
            this.fourthActorUuids[i] = uuid(actor);
            if (actor != null) {
                this.spawnActorArrivalSmoke(level, actor.m_20182_());
                actor.startCasting(fourthFirstWarningTicks());
            }
        }
        this.playSummonApostleSound(level, this.boss.arenaHomePosition());
    }

    private void startFourthFirstWarnings(ServerLevel level) {
        Vec3 home = this.boss.arenaHomePosition();
        StarFantasyVfx.groundWarningCircleOwnedStatic(
                this.boss, home.m_82520_(0.0D, 0.06D, 0.0D),
                fourthFirstWarningTicks(), FOURTH_VINE_KILL_RADIUS,
                WarningColor.PURPLE);

        int[] candidates = new int[FOURTH_ICE_CANDIDATE_COUNT];
        for (int i = 0; i < candidates.length; ++i) {
            candidates[i] = i;
        }
        for (int i = candidates.length - 1; i > 0; --i) {
            int swapIndex = this.boss.m_217043_().m_188503_(i + 1);
            int value = candidates[i];
            candidates[i] = candidates[swapIndex];
            candidates[swapIndex] = value;
        }
        double angleOffset = this.boss.m_217043_().m_188500_() * Math.PI * 2.0D;
        for (int i = 0; i < this.fourthIceUuids.length; ++i) {
            double angle = angleOffset + Math.PI * 2.0D
                    * candidates[i] / FOURTH_ICE_CANDIDATE_COUNT;
            Vec3 position = groundCenterAt(
                    level,
                    home.f_82479_ + Math.cos(angle) * FOURTH_ICE_RING_RADIUS,
                    home.f_82480_ + 8.0D,
                    home.f_82481_ + Math.sin(angle) * FOURTH_ICE_RING_RADIUS);
            ApollyonPageantIceChunkEntity chunk =
                    ApollyonPageantIceChunkEntity.spawn(this.boss, position);
            this.fourthIceUuids[i] = uuid(chunk);
            if (chunk != null) {
                StarFantasyVfx.groundWarningCircleOwnedStatic(
                        this.boss, position.m_82520_(0.0D, 0.07D, 0.0D),
                        fourthFirstWarningTicks(), FOURTH_ICE_RADIUS,
                        WarningColor.RED);
            }
        }
    }

    private void resolveFourthFirstAttacks(
            ServerLevel level, List<ServerPlayer> players) {
        Vec3 home = this.boss.arenaHomePosition();
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            if (intersectsHorizontalCircle(
                    player.m_20191_(), home, FOURTH_VINE_KILL_RADIUS)) {
                this.damagePageantTarget(player, ModDamageSource.acid(this.boss, this.boss),
                        (MobEffect) GoetyEffects.ACID_VENOM.get());
            }
        }
        for (int i = 0; i < this.fourthIceUuids.length; ++i) {
            ApollyonPageantIceChunkEntity chunk =
                    fourthIce(level, this.fourthIceUuids[i]);
            if (chunk == null) {
                continue;
            }
            Vec3 groundPosition = chunk.groundPosition();
            chunk.playDoubleImpact(level);
            this.damageFourthIceArea(players, chunk);
            chunk.m_146870_();
            ApollyonPageantBlueIceEntity obstacle =
                    ApollyonPageantBlueIceEntity.spawn(this.boss, groundPosition);
            this.fourthIceUuids[i] = uuid(obstacle);
        }
        for (int i = 0; i < 2; ++i) {
            ApollyonPageantApostleEntity actor = actor(level, this.fourthActorUuids[i]);
            if (actor != null) {
                actor.finishCasting();
            }
        }
    }

    private void spawnFourthAtrocious(
            ServerLevel level, List<ServerPlayer> players) {
        for (int i = 0; i < 2; ++i) {
            this.departActor(level, this.fourthActorUuids[i]);
            this.fourthActorUuids[i] = null;
        }
        ApollyonPageantApostleEntity atrocious = ApollyonPageantApostleEntity.spawn(
                this.boss, this.fourthActorPositions[2],
                ApollyonPageantApostleEntity.ATROCIOUS, false);
        this.fourthActorUuids[2] = uuid(atrocious);
        if (atrocious != null) {
            this.spawnActorArrivalSmoke(level, atrocious.m_20182_());
            atrocious.startCasting(fourthAtrociousWarningTicks());
            this.playSummonApostleSound(level, atrocious.m_20182_());
        }
        StarFantasyVfx.groundWarningCircleOwnedStatic(
                this.boss,
                this.boss.arenaHomePosition().m_82520_(0.0D, 0.06D, 0.0D),
                fourthAtrociousWarningTicks(), ApollyonEntity.ARENA_RADIUS,
                WarningColor.YELLOW);
        this.sendPageantMessage(
                players,
                "message.starfantasy_goety.apollyon.fourth_stage_warning",
                ChatFormatting.RED);
    }

    private void resolveFourthKnockback(
            ServerLevel level, List<ServerPlayer> players) {
        ApollyonPageantApostleEntity atrocious =
                actor(level, this.fourthActorUuids[2]);
        if (atrocious != null) {
            atrocious.finishCasting();
        }
        Vec3 home = this.boss.arenaHomePosition();
        StarFantasyVfx.stomp(this.boss, home, 3.0F, 1.0F);
        level.m_8767_(new SlamParticleOption(
                        new ColorUtil(FOURTH_SLAM_COLOR), 20.0F, 20),
                home.f_82479_, home.f_82480_ + 0.08D, home.f_82481_,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        ApollyonGloriousSphereEntity.spawnRed(
                this.boss, home.m_82520_(0.0D, 1.0D, 0.0D));
        this.shakeArena();

        this.fourthSlides.clear();
        this.fourthSlideStopped.clear();
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            double dx = player.m_20185_() - home.f_82479_;
            double dz = player.m_20189_() - home.f_82481_;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double directionX = distance > 1.0E-7D ? dx / distance : 1.0D;
            double directionZ = distance > 1.0E-7D ? dz / distance : 0.0D;
            this.fourthSlides.put(player.m_20148_(), new PlayerSlide(
                    directionX, directionZ, FOURTH_KNOCK_DISTANCE));
            player.m_20256_(Vec3.f_82478_);
            player.f_19812_ = true;
            player.f_19864_ = true;
        }
        for (UUID iceUuid : this.fourthIceUuids) {
            ApollyonPageantBlueIceEntity obstacle = fourthBlueIce(level, iceUuid);
            if (obstacle != null) {
                StarFantasyVfx.groundWarningCircleOwnedStatic(
                        this.boss,
                        obstacle.m_20182_().m_82520_(0.0D, 0.08D, 0.0D),
                        FOURTH_ICE_EXPLOSION_WARNING_TICKS,
                        FOURTH_ICE_RADIUS, WarningColor.RED);
            }
        }
    }

    private void tickFourthSlides(int slideTick) {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        for (Map.Entry<UUID, PlayerSlide> entry : this.fourthSlides.entrySet()) {
            Entity entity = level.m_8791_(entry.getKey());
            if (!(entity instanceof LivingEntity player) || !player.m_6084_()) {
                continue;
            }
            if (this.fourthSlideStopped.contains(entry.getKey())) {
                Vec3 movement = player.m_20184_();
                player.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
                continue;
            }
            PlayerSlide slide = entry.getValue();
            double stepWeight = slideTick < 10
                    ? 1.0D
                    : Math.max(0.0D, (19.0D - slideTick) / 10.0D);
            double requestedDistance = slide.distance() * stepWeight / 14.5D;
            double allowedDistance = this.clipFourthSlideDistance(
                    level, player, slide.directionX(), slide.directionZ(),
                    requestedDistance);
            if (allowedDistance < requestedDistance - 1.0E-4D) {
                this.fourthSlideStopped.add(entry.getKey());
            }
            Vec3 movement = player.m_20184_();
            player.m_20256_(new Vec3(
                    slide.directionX() * allowedDistance,
                    movement.f_82480_,
                    slide.directionZ() * allowedDistance));
            player.f_19789_ = 0.0F;
            player.f_19864_ = true;
            player.f_19812_ = true;
        }
    }

    private double clipFourthSlideDistance(
            ServerLevel level, LivingEntity player,
            double directionX, double directionZ, double requestedDistance) {
        if (requestedDistance <= 0.0D) {
            return 0.0D;
        }
        double moveX = directionX * requestedDistance;
        double moveZ = directionZ * requestedDistance;
        double earliestFraction = 1.0D;
        AABB playerBox = player.m_20191_();
        double halfWidth = player.m_20205_() * 0.5D + 0.05D;
        for (UUID iceUuid : this.fourthIceUuids) {
            ApollyonPageantBlueIceEntity obstacleEntity =
                    fourthBlueIce(level, iceUuid);
            if (obstacleEntity == null) {
                continue;
            }
            AABB obstacle = obstacleEntity.m_20191_();
            if (playerBox.f_82292_ <= obstacle.f_82289_
                    || playerBox.f_82289_ >= obstacle.f_82292_) {
                continue;
            }
            double entry = segmentAabbEntry(
                    player.m_20185_(), player.m_20189_(), moveX, moveZ,
                    obstacle.f_82288_ - halfWidth,
                    obstacle.f_82291_ + halfWidth,
                    obstacle.f_82290_ - halfWidth,
                    obstacle.f_82293_ + halfWidth);
            if (entry >= 0.0D) {
                earliestFraction = Math.min(earliestFraction, entry);
            }
        }
        if (earliestFraction >= 1.0D) {
            return requestedDistance;
        }
        return Math.max(0.0D, requestedDistance * earliestFraction - 0.05D);
    }

    private static double segmentAabbEntry(
            double startX, double startZ, double moveX, double moveZ,
            double minX, double maxX, double minZ, double maxZ) {
        double enter = 0.0D;
        double exit = 1.0D;
        if (Math.abs(moveX) < 1.0E-9D) {
            if (startX < minX || startX > maxX) {
                return -1.0D;
            }
        } else {
            double first = (minX - startX) / moveX;
            double second = (maxX - startX) / moveX;
            enter = Math.max(enter, Math.min(first, second));
            exit = Math.min(exit, Math.max(first, second));
        }
        if (enter > exit) {
            return -1.0D;
        }
        if (Math.abs(moveZ) < 1.0E-9D) {
            if (startZ < minZ || startZ > maxZ) {
                return -1.0D;
            }
        } else {
            double first = (minZ - startZ) / moveZ;
            double second = (maxZ - startZ) / moveZ;
            enter = Math.max(enter, Math.min(first, second));
            exit = Math.min(exit, Math.max(first, second));
        }
        return enter <= exit && exit >= 0.0D && enter <= 1.0D
                ? Math.max(0.0D, enter) : -1.0D;
    }

    private void resolveFourthIceExplosions(
            ServerLevel level, List<ServerPlayer> players) {
        for (int i = 0; i < this.fourthIceUuids.length; ++i) {
            ApollyonPageantBlueIceEntity obstacle =
                    fourthBlueIce(level, this.fourthIceUuids[i]);
            if (obstacle != null) {
                obstacle.playDoubleImpact(level);
                this.damageFourthIceArea(players, obstacle);
                obstacle.m_146870_();
            }
            this.fourthIceUuids[i] = null;
        }
    }

    private void damageFourthIceArea(
            List<ServerPlayer> players, Entity iceSource) {
        DamageSource source = ModDamageSource.indirectFreeze(iceSource, this.boss);
        Vec3 center = iceSource.m_20182_();
        for (LivingEntity player : arenaLivingTargets(this.boss)) {
            if (!player.m_6084_()
                    || !intersectsHorizontalCircle(
                    player.m_20191_(), center, FOURTH_ICE_RADIUS)) {
                continue;
            }
            if (player.m_6469_(ApollyonDamageSources.front(player, source), FOURTH_ICE_DAMAGE)) {
                player.m_7292_(new MobEffectInstance(
                        (MobEffect) GoetyEffects.STUNNED.get(), FOURTH_STUN_TICKS));
            }
        }
    }

    private void beginFifthStage(ServerLevel level) {
        this.boss.clearCombatForPageant();
        this.boss.holdHiddenForPageant();
        Vec3 home = this.boss.arenaHomePosition();
        ApollyonPageantSummonEntity.spawn(this.boss, home, FIFTH_RETURN_TICK);
        this.playSummonStartSound(level, home);
        this.setState(FIFTH_STAGE, 0.0F);
    }

    private void tickFifthStage(ServerLevel level, List<ServerPlayer> players) {
        ++this.stateTicks;
        if (this.directTransitionHealTicks > 0) {
            this.healTransition(this.directTransitionHealTicks--);
        }
        if (this.stateTicks < FIFTH_RETURN_TICK) {
            this.boss.holdHiddenForPageant();
        } else {
            this.boss.holdForFinalPageant(this.stateTicks <= FIFTH_JUDGMENT_TICK);
        }

        if (this.stateTicks == FIFTH_RETURN_TICK) {
            this.returnForFifthStage(level);
        }
        if (this.stateTicks == FIFTH_DARKNESS_TICK) {
            this.syncDarkness(players, FIFTH_DARKNESS_END_TICK - this.stateTicks);
        } else if (this.stateTicks > FIFTH_DARKNESS_TICK
                && this.stateTicks < FIFTH_DARKNESS_END_TICK) {
            this.syncDarkness(players, FIFTH_DARKNESS_END_TICK - this.stateTicks);
        }
        if (this.stateTicks == FIFTH_HADES_SPAWN_TICK) {
            this.discardPageantHalos(level);
            this.spawnBoundHades(level, true);
            this.boss.setPageantReturnSmoke(false);
            this.boss.setPageantReturnRainbow(true);
            this.playSoundAtHome(level, ApollyonSoundRegistry.SUMMON_HADES.get(), 4.0F);
            StarFantasyVfx.areaShake(
                    this.boss, this.boss.arenaHomePosition(), 64.0D, 80, 3.0F);
        }
        if (this.stateTicks == FIFTH_DARKNESS_END_TICK) {
            this.clearDarkness(level);
            for (ServerPlayer player : players) {
                this.clearAllFocusCooldowns(player);
            }
            this.sendPageantMessage(
                    players,
                    "message.starfantasy_goety.apollyon.focus_reset",
                    ChatFormatting.GREEN);
        }
        if (this.stateTicks == FIFTH_COUNTDOWN_START_TICK) {
            HadesEntity hades = hades(level, this.hadesUuid);
            if (hades != null) {
                hades.startCharging();
            }
        }
        if (this.stateTicks >= FIFTH_COUNTDOWN_START_TICK
                && this.stateTicks <= FIFTH_COUNTDOWN_START_TICK + 80
                && (this.stateTicks - FIFTH_COUNTDOWN_START_TICK) % 20 == 0) {
            int seconds = 5 - (this.stateTicks - FIFTH_COUNTDOWN_START_TICK) / 20;
            this.sendPageantMessage(
                    players,
                    "message.starfantasy_goety.apollyon.judgment_countdown",
                    ChatFormatting.RED,
                    seconds);
            this.playSoundAtHome(level, ApollyonSoundRegistry.CAST_HADES.get(), 3.0F);
        }
        if (this.stateTicks == FIFTH_SMASH_TICK) {
            HadesEntity hades = hades(level, this.hadesUuid);
            if (hades != null) {
                hades.startSmash();
            }
        }
        if (this.stateTicks == FIFTH_CLEAVE_TICK) {
            this.startJudgmentCleave(level);
        }
        if (this.stateTicks == FIFTH_JUDGMENT_TICK) {
            this.resolveJudgment(level, players);
            this.boss.stopFinalPageantCasting();
        }
        if (this.stateTicks >= FIFTH_END_TICK) {
            this.finishPageant(level);
        }
    }

    private void returnForFifthStage(ServerLevel level) {
        this.boss.beginFinalPageantReturn();
        this.spawnActorArrivalSmoke(level, this.boss.arenaHomePosition());
        this.playSummonApostleSound(level, this.boss.arenaHomePosition());
        this.playSoundAtHome(level,
                (SoundEvent) ModSounds.APOSTLE_PREPARE_SPELL.get(), 3.0F);
        this.prepareInnerHalos(level);
    }

    private void prepareInnerHalos(ServerLevel level) {
        List<ApollyonPageantHaloEntity> halos = this.pageantHalos(level);
        boolean[] variants = new boolean[HALO_COUNT];
        for (int i = halos.size() - 1; i >= 0; --i) {
            ApollyonPageantHaloEntity halo = halos.get(i);
            int variant = halo.variant();
            if (variants[variant]) {
                halo.m_146870_();
                halos.remove(i);
            } else {
                variants[variant] = true;
            }
        }
        Vec3 home = this.boss.arenaHomePosition();
        double baseAngle = (level.m_46467_() - this.haloOrbitEpoch)
                * ApollyonPageantHaloEntity.OUTER_DEGREES_PER_TICK;
        for (int variant = 0; variant < HALO_COUNT; ++variant) {
            if (variants[variant]) {
                continue;
            }
            double angle = baseAngle + variant * 30.0D;
            double radians = Math.toRadians(angle);
            Vec3 start = new Vec3(
                    home.f_82479_ + Math.cos(radians) * ApollyonPageantHaloEntity.OUTER_RADIUS,
                    home.f_82480_,
                    home.f_82481_ + Math.sin(radians) * ApollyonPageantHaloEntity.OUTER_RADIUS);
            ApollyonPageantHaloEntity halo = ApollyonPageantHaloEntity.spawnOuter(
                    this.boss, start, variant, angle);
            if (halo != null) {
                halos.add(halo);
            }
        }
        for (int i = halos.size() - 1; i > 0; --i) {
            int swap = this.boss.m_217043_().m_188503_(i + 1);
            ApollyonPageantHaloEntity value = halos.get(i);
            halos.set(i, halos.get(swap));
            halos.set(swap, value);
        }
        for (int i = 0; i < Math.min(HALO_COUNT, halos.size()); ++i) {
            halos.get(i).beginInnerOrbit(i, FIFTH_INNER_ATTACH_TICKS);
        }
    }

    private void startJudgmentCleave(ServerLevel level) {
        Vec3 home = this.boss.arenaHomePosition();
        ApollyonCleaveEffectEntity cleave = new ApollyonCleaveEffectEntity(
                ApollyonEntityRegistry.APOLLYON_CLEAVE_EFFECT.get(), level);
        cleave.m_6034_(home.f_82479_, home.f_82480_ + 0.05D, home.f_82481_);
        level.m_7967_(cleave);
        StarFantasyVfx.stomp(this.boss, home, 3.0F, 1.0F);
        StarFantasyVfx.slamShockwave(this.boss, home, 20.0D);
        StarFantasyVfx.areaShake(this.boss, home, 64.0D, 40, 1.5F);
    }

    private void resolveJudgment(ServerLevel level, List<ServerPlayer> players) {
        Vec3 home = this.boss.arenaHomePosition();
        level.m_5594_(null, BlockPos.m_274561_(
                        home.f_82479_, home.f_82480_, home.f_82481_),
                ApollyonSoundRegistry.CAST_OBSIDIAN.get(),
                SoundSource.HOSTILE, 3.0F, 1.0F);
        StarFantasyVfx.areaImpactShake(
                this.boss, home, 64.0D, 0, 40, 20,
                FIFTH_JUDGMENT_SHAKE_INTENSITY);
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            if (player instanceof ServerPlayer serverPlayer) {
                StarFantasyGoetyNetwork.startPageantWhiteout(serverPlayer);
            }
            if (MiscCapHelper.getShields(player) > 0) {
                MiscCapHelper.setShields(player, 0);
                MiscCapHelper.setShieldTime(player, 0);
            } else if (player instanceof ServerPlayer serverPlayer) {
                StarFantasyTrueKillHelper.trueKillPlayer(
                        serverPlayer, Float.MAX_VALUE, "apollyon_judgment");
            } else {
                // Other living entities get one ordinary kill attempt, without forced cleanup.
                player.m_6074_();
            }
        }
    }

    private void finishPageant(ServerLevel level) {
        this.clearDarkness(level);
        this.clearEncounter(level, false);
        this.setState(INACTIVE, 1.0F);
        this.boss.m_21153_(this.boss.m_21233_());
        this.boss.enterCombatPhaseTwo();
        this.ensureBoundHades(level);
    }

    private void damageMeteorArea(ServerLevel level, Vec3 center) {
        DamageSource source = ModDamageSource.hellfire(this.boss, this.boss);
        for (LivingEntity player : this.arenaLivingTargets(level)) {
            if (intersectsHorizontalCircle(player.m_20191_(), center, METEOR_RADIUS)
                    && player.m_20191_().f_82292_ >= center.f_82480_ - 1.0D
                    && player.m_20191_().f_82289_ <= center.f_82480_ + 6.0D) {
                player.m_6469_(ApollyonDamageSources.front(player, source), METEOR_DAMAGE);
            }
        }
    }

    public static void playPyroclastExplosion(ServerLevel level, Vec3 center) {
        playPyroclastExplosion(level, center, PYROCLAST_VISUAL_SIZE);
    }

    public static void playPyroclastExplosion(
            ServerLevel level, Vec3 center, float visualSize) {
        playPyroclastExplosion(level, center, visualSize, 4.0F);
    }

    public static void playPyroclastExplosion(
            ServerLevel level, Vec3 center, float visualSize, float volume) {
        ColorUtil color = new ColorUtil(PYROCLAST_COLOR);
        float safeVisualSize = Math.max(0.1F, visualSize);
        level.m_8767_(new CircleExplodeParticleOption(color, safeVisualSize, 1),
                center.f_82479_, center.f_82480_, center.f_82481_,
                0, 0.0D, 0.0D, 0.0D, 0.0D);
        level.m_8767_(new SphereExplodeParticleOption(color, safeVisualSize, 1),
                center.f_82479_, center.f_82480_, center.f_82481_,
                0, 0.0D, 0.0D, 0.0D, 0.0D);
        ColorUtil cultColor = new ColorUtil(PYROCLAST_CULT_COLOR);
        for (int i = 0; i < 8; ++i) {
            level.m_8767_((ParticleOptions) ModParticleTypes.BIG_CULT_SPELL.get(),
                    center.f_82479_ + (level.m_213780_().m_188500_() - 0.5D),
                    center.f_82480_ + level.m_213780_().m_188500_(),
                    center.f_82481_ + (level.m_213780_().m_188500_() - 0.5D),
                    0, cultColor.red, cultColor.green, cultColor.blue, 1.0D);
            level.m_8767_((ParticleOptions) ModParticleTypes.BIG_FIRE_GROUND.get(),
                    center.f_82479_ + (level.m_213780_().m_188500_() - 0.5D) * 0.5D,
                    center.f_82480_,
                    center.f_82481_ + (level.m_213780_().m_188500_() - 0.5D) * 0.5D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.m_5594_(null, BlockPos.m_274561_(center.f_82479_, center.f_82480_, center.f_82481_),
                SoundEvents.f_11913_, SoundSource.HOSTILE, volume,
                (1.0F + (level.m_213780_().m_188501_() - level.m_213780_().m_188501_()) * 0.2F) * 0.7F);
    }

    private void departActor(ServerLevel level, UUID actorUuid) {
        Entity actor = entity(level, actorUuid);
        if (actor == null) {
            return;
        }
        this.playDepartureEffect(level, actor.m_20182_());
        if (actor instanceof ApollyonPageantApostleEntity apostle) {
            this.spawnDepartedHalo(level, actor.m_20182_(), apostle.variant());
        }
        actor.m_146870_();
    }

    private void spawnDepartedHalo(ServerLevel level, Vec3 position, int variant) {
        List<Integer> vacancies = new ArrayList<>();
        for (int i = 0; i < this.occupiedOuterHaloAnchors.length; ++i) {
            if (!this.occupiedOuterHaloAnchors[i]) {
                vacancies.add(i);
            }
        }
        if (vacancies.isEmpty()) {
            return;
        }
        int slot = vacancies.get(this.boss.m_217043_().m_188503_(vacancies.size()));
        this.occupiedOuterHaloAnchors[slot] = true;
        double angle = (level.m_46467_() - this.haloOrbitEpoch)
                * ApollyonPageantHaloEntity.OUTER_DEGREES_PER_TICK + slot * 30.0D;
        ApollyonPageantHaloEntity.spawnOuter(this.boss, position, variant, angle);
    }

    private List<ApollyonPageantHaloEntity> pageantHalos(ServerLevel level) {
        AABB area = new AABB(
                this.boss.arenaHomePosition(), this.boss.arenaHomePosition()).m_82400_(128.0D);
        List<ApollyonPageantHaloEntity> result = new ArrayList<>();
        for (ApollyonPageantHaloEntity halo
                : level.m_45976_(ApollyonPageantHaloEntity.class, area)) {
            if (this.boss.m_20148_().equals(halo.ownerUuid())) {
                result.add(halo);
            }
        }
        return result;
    }

    private void discardPageantHalos(ServerLevel level) {
        for (ApollyonPageantHaloEntity halo : this.pageantHalos(level)) {
            halo.m_146870_();
        }
    }

    private void syncDarkness(List<ServerPlayer> players, int duration) {
        for (ServerPlayer player : players) {
            if (this.darkenedPlayers.add(player.m_20148_())) {
                StarFantasyGoetyNetwork.setPageantDarkness(player, duration);
            }
        }
    }

    private void clearDarkness(ServerLevel level) {
        for (UUID playerUuid : this.darkenedPlayers) {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(playerUuid);
            if (player != null) {
                StarFantasyGoetyNetwork.setPageantDarkness(player, 0);
            }
        }
        this.darkenedPlayers.clear();
    }

    private void clearAllFocusCooldowns(ServerPlayer player) {
        FocusCooldown cooldown = SEHelper.getFocusCoolDown(player);
        if (cooldown == null) {
            return;
        }
        for (Item item : new ArrayList<>(cooldown.getCooldowns().keySet())) {
            cooldown.removeCooldown(player, player.m_9236_(), item);
        }
        for (String key : new ArrayList<>(cooldown.getSpecificCooldowns().keySet())) {
            cooldown.removeSpecificCooldown(player, player.m_9236_(), key);
        }
    }

    public void tickPersistentCompanion() {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (this.boss.isCombatPhaseTwo()) {
            this.ensureBoundHades(level);
        } else if (this.state == INACTIVE) {
            this.removeBoundHades(level);
        }
    }

    /** Starts one phase-two Hades attack, restoring the bound visual if needed. */
    public void startHadesCooperativeAttack(int attackType) {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)
                || !this.boss.isCombatPhaseTwo()) {
            return;
        }
        this.ensureBoundHades(level);
        HadesEntity hades = hades(level, this.hadesUuid);
        if (hades != null) {
            Vec3 home = this.boss.arenaHomePosition();
            this.playSoundAtHome(level, ApollyonSoundRegistry.SUMMON_HADES.get(), 4.0F);
            StarFantasyVfx.areaShake(this.boss, home, 64.0D, 20, 1.3F);
            hades.startCooperativeAttack(attackType);
        }
    }

    /** Stops the encounter and starts the bound Hades visual's synchronized finale. */
    public void beginBossDeath() {
        if (!(this.boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        this.clearDarkness(level);
        this.clearEncounter(level, false);
        this.setState(INACTIVE, 1.0F);
        if (this.boss.isCombatPhaseTwo()) {
            this.ensureBoundHades(level);
        }
        HadesEntity hades = hades(level, this.hadesUuid);
        if (hades != null) {
            hades.beginDeathAnimation();
        }
    }

    private void spawnBoundHades(ServerLevel level) {
        this.spawnBoundHades(level, false);
    }

    private void spawnBoundHades(ServerLevel level, boolean riseFromBelow) {
        this.removeBoundHades(level);
        Vec3 home = this.boss.arenaHomePosition();
        Vec3 position = new Vec3(
                home.f_82479_, home.f_82480_, home.f_82481_ - HADES_NORTH_DISTANCE);
        HadesEntity hades = riseFromBelow
                ? HadesEntity.spawnRising(this.boss, position)
                : HadesEntity.spawn(this.boss, position);
        this.hadesUuid = uuid(hades);
    }

    private void ensureBoundHades(ServerLevel level) {
        HadesEntity cached = hades(level, this.hadesUuid);
        if (cached != null && this.boss.m_20148_().equals(cached.ownerUuid())) {
            return;
        }
        AABB area = new AABB(
                this.boss.arenaHomePosition(), this.boss.arenaHomePosition()).m_82400_(128.0D);
        for (HadesEntity hades : level.m_45976_(HadesEntity.class, area)) {
            if (this.boss.m_20148_().equals(hades.ownerUuid())) {
                this.hadesUuid = hades.m_20148_();
                return;
            }
        }
        this.spawnBoundHades(level);
    }

    private void removeBoundHades(ServerLevel level) {
        AABB area = new AABB(
                this.boss.arenaHomePosition(), this.boss.arenaHomePosition()).m_82400_(128.0D);
        for (HadesEntity hades : level.m_45976_(HadesEntity.class, area)) {
            if (this.boss.m_20148_().equals(hades.ownerUuid())) {
                hades.m_146870_();
            }
        }
        this.hadesUuid = null;
    }

    private void playDepartureEffect(ServerLevel level, Vec3 position) {
        Vec3 center = position.m_82520_(0.0D, 1.0D, 0.0D);
        level.m_5594_(null, BlockPos.m_274561_(center.f_82479_, center.f_82480_, center.f_82481_),
                ApollyonSoundRegistry.DEATH_EXPLOSION.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
        double phi = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int i = 0; i < DEATH_PARTICLE_COUNT; ++i) {
            double velocityY = 1.0D - i / (DEATH_PARTICLE_COUNT - 1.0D) * 2.0D;
            double radius = Math.sqrt(1.0D - velocityY * velocityY);
            double theta = phi * i;
            double velocityX = Math.cos(theta) * radius * DEATH_PARTICLE_SPEED;
            double velocityZ = Math.sin(theta) * radius * DEATH_PARTICLE_SPEED;
            level.m_8767_(ParticleTypes.f_123745_,
                    center.f_82479_, center.f_82480_, center.f_82481_, 0,
                    velocityX, velocityY * DEATH_PARTICLE_SPEED, velocityZ, 1.0D);
        }
    }

    private void spawnActorArrivalSmoke(ServerLevel level, Vec3 actorPosition) {
        Vec3 center = actorPosition.m_82520_(0.0D, 1.0D, 0.0D);
        double phi = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int i = 0; i < DEATH_PARTICLE_COUNT; ++i) {
            double velocityY = 1.0D - i / (DEATH_PARTICLE_COUNT - 1.0D) * 2.0D;
            double radius = Math.sqrt(1.0D - velocityY * velocityY);
            double theta = phi * i;
            double velocityX = Math.cos(theta) * radius * DEATH_PARTICLE_SPEED;
            double velocityZ = Math.sin(theta) * radius * DEATH_PARTICLE_SPEED;
            level.m_8767_(ParticleTypes.f_123755_,
                    center.f_82479_, center.f_82480_, center.f_82481_, 0,
                    velocityX, velocityY * DEATH_PARTICLE_SPEED, velocityZ, 1.0D);
        }
    }

    private void playSummonApostleSound(ServerLevel level, Vec3 position) {
        level.m_5594_(null, BlockPos.m_274561_(
                        position.f_82479_, position.f_82480_, position.f_82481_),
                ApollyonSoundRegistry.SUMMON_APOSTLE.get(),
                SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    private void playSummonStartSound(ServerLevel level, Vec3 position) {
        level.m_5594_(null, BlockPos.m_274561_(
                        position.f_82479_, position.f_82480_, position.f_82481_),
                ApollyonSoundRegistry.SUMMON_START.get(),
                SoundSource.HOSTILE, 2.0F, 1.0F);
    }

    private void playSoundAtHome(ServerLevel level, SoundEvent sound, float volume) {
        Vec3 home = this.boss.arenaHomePosition();
        level.m_5594_(null, BlockPos.m_274561_(
                        home.f_82479_, home.f_82480_, home.f_82481_),
                sound, SoundSource.HOSTILE, volume, 1.0F);
    }

    private void playProfaneCastSound(
            ServerLevel level, ApollyonPageantApostleEntity profane) {
        Vec3 position = profane == null ? this.actorPositions[0] : profane.m_20182_();
        level.m_5594_(null, BlockPos.m_274561_(
                        position.f_82479_, position.f_82480_, position.f_82481_),
                ApollyonSoundRegistry.CAST_PROFANE.get(),
                SoundSource.HOSTILE, 3.0F, 1.0F);
    }

    private void shakeArena() {
        StarFantasyVfx.areaShake(this.boss, this.boss.arenaHomePosition(),
                ARENA_SHAKE_RADIUS, MAJOR_SHAKE_TICKS, MAJOR_SHAKE_INTENSITY);
    }

    private void resetToPending(ServerLevel level) {
        this.boss.clearCombatForPageant();
        this.clearEncounter(level, true);
        this.boss.m_6710_(null);
        this.setState(PENDING_RESTART, 0.0F);
        this.boss.holdAtHomeForPageantRetry();
    }

    private void clearEncounter(ServerLevel level, boolean clearPersistentState) {
        this.directTransitionHealTicks = 0;
        this.releaseGloriousSlides(level);
        this.releaseFourthSlides(level);
        StarFantasyVfx.clearWarningsForOwner(this.boss);
        StarFantasyVfx.clearWarnings(this.boss, this.boss.arenaHomePosition(), 64.0D);
        ApollyonFireTrapManager.clearForBoss(this.boss);
        ApollyonLightningStormManager.clearForBoss(this.boss);
        ApollyonWildSurgeManager.clearManagedThornsForBoss(this.boss);

        AABB cleanupArea = new AABB(
                this.boss.arenaHomePosition(), this.boss.arenaHomePosition()).m_82400_(128.0D);
        for (Entity entity : level.m_45976_(Entity.class, cleanupArea)) {
            if (entity instanceof ApollyonPageantOwned owned
                    && this.boss.m_20148_().equals(owned.pageantOwnerUuid())) {
                entity.m_146870_();
            } else if (entity instanceof GroundRectangleWarningEntity
                    && horizontalDistanceSqr(entity.m_20182_(), this.boss.arenaHomePosition())
                    <= ApollyonEntity.ARENA_RADIUS * ApollyonEntity.ARENA_RADIUS) {
                entity.m_146870_();
            }
        }
        for (com.starfantasy.goety.entity.ApollyonStarArrowEntity arrow
                : level.m_45976_(
                        com.starfantasy.goety.entity.ApollyonStarArrowEntity.class,
                        cleanupArea)) {
            if (arrow.m_19749_() == this.boss) {
                arrow.m_146870_();
            }
        }
        for (UUID warningUuid : this.safeWarningUuids) {
            this.discard(level, warningUuid);
        }
        this.safeWarningUuids.clear();

        if (clearPersistentState) {
            this.boss.setPageantReturnSmoke(false);
            this.boss.setPageantReturnRainbow(false);
            this.clearDarkness(level);
            this.discardPageantHalos(level);
            this.removeBoundHades(level);
            java.util.Arrays.fill(this.occupiedOuterHaloAnchors, false);
            this.haloOrbitEpoch = 0L;
        }
        this.profaneUuid = null;
        this.pyreLordUuid = null;
        this.terribleUuid = null;
        this.meteorUuids.clear();
        this.magmaUuids.clear();
        this.meteorAnchors.clear();
        this.gloriousUuid = null;
        this.beamHitCooldowns.clear();
        this.secondOrbitDegrees = 0.0D;
        for (int i = 0; i < this.secondActorUuids.length; ++i) {
            this.secondActorUuids[i] = null;
            this.secondBeamUuids[i] = null;
            this.secondActorBaseAngles[i] = 0.0D;
        }
        for (int i = 0; i < this.actorPositions.length; ++i) {
            this.actorPositions[i] = null;
        }
        for (int i = 0; i < this.thirdActorUuids.length; ++i) {
            this.thirdActorUuids[i] = null;
            this.thirdActorPositions[i] = null;
        }
        this.thirdMonolithUuid = null;
        this.thirdLinkedIndex = -1;
        this.thirdEnraged = false;
        for (int i = 0; i < this.fourthActorUuids.length; ++i) {
            this.fourthActorUuids[i] = null;
            this.fourthActorPositions[i] = null;
        }
        for (int i = 0; i < this.fourthIceUuids.length; ++i) {
            this.fourthIceUuids[i] = null;
        }
    }

    private void setState(int state, float opacity) {
        this.state = state;
        this.stateTicks = 0;
        if (state == INACTIVE || ApollyonConfig.hardMode()) {
            this.checkpointPhase = 1;
        } else {
            int phase = phaseForState(state);
            // End-of-stage damage must resolve before a surviving group earns progress.
            if (phase > 0 && this.ensureTarget() != null) {
                this.checkpointPhase = phase;
            }
        }
        this.boss.setPageantState(state);
        this.boss.setPageantOpacity(opacity);
    }

    private int restartPhase() {
        // Preserve the explicit developer starting phase; normal encounters default to 1.
        return ApollyonConfig.hardMode() ? this.testPhase
                : Math.max(this.testPhase, this.checkpointPhase);
    }

    private static int phaseForState(int state) {
        return switch (state) {
            case SUMMONING, FIRST_TRIO -> 1;
            case GLORIOUS_STAGE, SECOND_TRIO -> 2;
            case THIRD_DPS -> 3;
            case FOURTH_STAGE -> 4;
            case FIFTH_STAGE -> 5;
            default -> 0;
        };
    }

    private static Vec3 groundCenterAt(ServerLevel level, double x, double searchY, double z) {
        int blockX = Mth.m_14107_(x);
        int blockZ = Mth.m_14107_(z);
        int startY = Math.min(level.m_151558_() - 1, Mth.m_14107_(searchY));
        int minY = level.m_141937_() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) {
                return new Vec3(x, y + 0.01D, z);
            }
        }
        return new Vec3(x, searchY, z);
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to) {
        double dx = to.f_82479_ - from.f_82479_;
        double dz = to.f_82481_ - from.f_82481_;
        double length = Math.sqrt(dx * dx + dz * dz);
        return length < 1.0E-7D ? new Vec3(0.0D, 0.0D, 1.0D)
                : new Vec3(dx / length, 0.0D, dz / length);
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
        return value > max ? value - max : 0.0D;
    }

    private static double horizontalDistanceSqr(Vec3 first, Vec3 second) {
        double dx = first.f_82479_ - second.f_82479_;
        double dz = first.f_82481_ - second.f_82481_;
        return dx * dx + dz * dz;
    }

    private static UUID uuid(Entity entity) {
        return entity == null ? null : entity.m_20148_();
    }

    private static Entity entity(ServerLevel level, UUID uuid) {
        return uuid == null ? null : level.m_8791_(uuid);
    }

    private static ApollyonPageantApostleEntity actor(ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        return entity instanceof ApollyonPageantApostleEntity actor ? actor : null;
    }

    private static ApollyonPageantObsidianMonolithEntity thirdMonolith(
            ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        return entity instanceof ApollyonPageantObsidianMonolithEntity monolith
                ? monolith : null;
    }

    private static ApollyonPageantIceChunkEntity fourthIce(
            ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        return entity instanceof ApollyonPageantIceChunkEntity ice ? ice : null;
    }

    private static ApollyonPageantBlueIceEntity fourthBlueIce(
            ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        return entity instanceof ApollyonPageantBlueIceEntity ice ? ice : null;
    }

    private static HadesEntity hades(ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        return entity instanceof HadesEntity hades ? hades : null;
    }

    private static void discard(ServerLevel level, UUID uuid) {
        Entity entity = entity(level, uuid);
        if (entity != null && entity.m_6084_()) {
            entity.m_146870_();
        }
    }

    private void releaseGloriousSlides(ServerLevel level) {
        for (UUID playerUuid : this.gloriousSlides.keySet()) {
            Entity entity = level.m_8791_(playerUuid);
            if (entity instanceof LivingEntity player) {
                Vec3 movement = player.m_20184_();
                player.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
                player.f_19812_ = true;
                player.f_19864_ = true;
            }
        }
        this.gloriousSlides.clear();
    }

    private void releaseFourthSlides(ServerLevel level) {
        for (UUID playerUuid : this.fourthSlides.keySet()) {
            Entity entity = level.m_8791_(playerUuid);
            if (entity instanceof LivingEntity player) {
                Vec3 movement = player.m_20184_();
                player.m_20256_(new Vec3(0.0D, movement.f_82480_, 0.0D));
                player.f_19812_ = true;
                player.f_19864_ = true;
            }
        }
        this.fourthSlides.clear();
        this.fourthSlideStopped.clear();
    }

    private record PlayerSlide(
            double directionX, double directionZ, double distance) {
    }
}
