package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.Leapleaf;
import com.Polarice3.Goety.common.entities.ally.ender.WatchlingServant;
import com.Polarice3.Goety.common.entities.ally.golem.IceGolem;
import com.Polarice3.Goety.common.entities.ally.illager.StormCasterServant;
import com.Polarice3.Goety.common.entities.hostile.servants.Inferno;
import com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith;
import com.Polarice3.Goety.common.entities.neutral.Owned;
import com.Polarice3.Goety.common.entities.util.SummonCircle;
import com.Polarice3.Goety.utils.BlockFinder;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Function;

/** Keeps all Apollyon summons on Goety's native ownership and summon paths. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class ApollyonSummonManager {
    private static final String MANAGED_MONOLITH_TAG =
            "starfantasy_apollyon_timed_monolith";
    private static final String MONOLITH_AGE_TAG =
            "starfantasy_apollyon_timed_monolith_age";
    private static final String MONOLITH_TRANSFORMING_TAG =
            "starfantasy_apollyon_timed_monolith_transforming";
    private static final String NO_REWARDS_TAG =
            "starfantasy_apollyon_summon_no_rewards";
    private static final int MONOLITH_LIFETIME_TICKS = 1200;
    private static final int SERVANT_LIFETIME_TICKS = 600;
    private static final double MONOLITH_DISTANCE = 14.0D;
    private static final double MONOLITH_DIAGONAL_OFFSET =
            MONOLITH_DISTANCE / Math.sqrt(2.0D);
    private static final double[][] MONOLITH_DIAGONAL_OFFSETS = {
            {MONOLITH_DIAGONAL_OFFSET, MONOLITH_DIAGONAL_OFFSET},
            {MONOLITH_DIAGONAL_OFFSET, -MONOLITH_DIAGONAL_OFFSET},
            {-MONOLITH_DIAGONAL_OFFSET, MONOLITH_DIAGONAL_OFFSET},
            {-MONOLITH_DIAGONAL_OFFSET, -MONOLITH_DIAGONAL_OFFSET}
    };

    private ApollyonSummonManager() {
    }

    public static void summonInfernos(ApollyonEntity boss) {
        summonServants(boss, 2,
                level -> new Inferno(ModEntityType.INFERNO.get(), level));
    }

    public static void summonIceGolem(ApollyonEntity boss) {
        summonServants(boss, 1,
                level -> new IceGolem(ModEntityType.ICE_GOLEM.get(), level));
    }

    public static void summonLeapleaf(ApollyonEntity boss) {
        summonServants(boss, 1,
                level -> new Leapleaf(ModEntityType.LEAPLEAF.get(), level));
    }

    public static void summonStormCaster(ApollyonEntity boss) {
        summonServants(boss, 1,
                level -> new StormCasterServant(ModEntityType.STORM_CASTER_SERVANT.get(), level));
    }

    public static void summonWatchlings(ApollyonEntity boss) {
        summonServants(boss, 2,
                level -> new WatchlingServant(ModEntityType.WATCHLING_SERVANT.get(), level));
    }

    public static void summonObsidianMonoliths(ApollyonEntity boss) {
        if (!(boss.m_9236_() instanceof ServerLevel level) || !boss.m_6084_()) {
            return;
        }
        Vec3 home = boss.arenaHomePosition();
        for (double[] offset : MONOLITH_DIAGONAL_OFFSETS) {
            BlockPos candidate = BlockPos.m_274561_(
                    home.f_82479_ + offset[0], home.f_82480_, home.f_82481_ + offset[1]);
            ObsidianMonolith monolith = new ObsidianMonolith(
                    ModEntityType.OBSIDIAN_MONOLITH.get(), level);
            // Preserve the four diagonal, home-relative positions. SummonPosition
            // only resolves a usable ground Y at the same candidate X/Z.
            BlockPos spawnPos = BlockFinder.SummonPosition(level, monolith, candidate);
            monolith.m_20035_(spawnPos, 0.0F, 0.0F);
            monolith.setTrueOwner(boss);
            monolith.m_6518_(level, level.m_6436_(spawnPos),
                    MobSpawnType.MOB_SUMMONED, null, null);
            monolith.getPersistentData().m_128379_(MANAGED_MONOLITH_TAG, true);
            monolith.getPersistentData().m_128405_(MONOLITH_AGE_TAG, 0);
            level.m_7967_(monolith);
        }
    }

    @SubscribeEvent
    public static void tickManagedMonolith(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ObsidianMonolith monolith)
                || !monolith.m_6084_()
                || monolith.m_9236_().f_46443_
                || !monolith.getPersistentData().m_128471_(MANAGED_MONOLITH_TAG)
                || monolith.getPersistentData().m_128471_(MONOLITH_TRANSFORMING_TAG)) {
            return;
        }
        if (monolith.getTrueOwner() instanceof ApollyonEntity owner
                && (!owner.m_6084_() || owner.isPlayingDeathAnimation())) {
            monolith.getPersistentData().m_128379_(MONOLITH_TRANSFORMING_TAG, true);
            monolith.silentDie(monolith.m_269291_().m_269264_());
            return;
        }
        if (monolith.getTrueOwner() instanceof ApollyonEntity boss
                && boss.m_6084_() && !monolith.isEmerging()) {
            // Goety hard-codes this refresh to Apostle. Mirror that native
            // ten-tick owner refresh for Apollyon-managed monoliths.
            boss.refreshMonolithPower();
        }
        int age = monolith.getPersistentData().m_128451_(MONOLITH_AGE_TAG) + 1;
        monolith.getPersistentData().m_128405_(MONOLITH_AGE_TAG, age);
        if (age < MONOLITH_LIFETIME_TICKS) {
            return;
        }

        monolith.getPersistentData().m_128379_(MONOLITH_TRANSFORMING_TAG, true);
        if (monolith.getTrueOwner() instanceof ApollyonEntity boss
                && boss.m_6084_()) {
            boss.addMonolithShield();
        }
        monolith.silentDie(monolith.m_269291_().m_269264_());
    }

    @SubscribeEvent
    public static void redirectMonolithAttackers(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(mob.m_9236_() instanceof ServerLevel level)
                || !mob.m_6084_()
                || !(mob.m_5448_() instanceof ApollyonEntity boss)
                || !boss.m_6084_()
                || boss.isPageantCombatLocked()
                || !boss.shouldRedirectAttackersToMonoliths()) {
            return;
        }

        // Goety's global redirect only recognizes Apostle, not Apollyon.
        // Scope this version to the arena instead of the mob's follow range.
        Vec3 home = boss.arenaHomePosition();
        double radius = ApollyonEntity.ARENA_RADIUS;
        double offsetX = mob.m_20185_() - home.f_82479_;
        double offsetZ = mob.m_20189_() - home.f_82481_;
        if (offsetX * offsetX + offsetZ * offsetZ > radius * radius
                || Math.abs(mob.m_20186_() - home.f_82480_) > 8.0D) {
            return;
        }
        for (ObsidianMonolith monolith : level.m_45976_(ObsidianMonolith.class,
                new AABB(home, home).m_82377_(radius, 8.0D, radius))) {
            double pillarX = monolith.m_20185_() - home.f_82479_;
            double pillarZ = monolith.m_20189_() - home.f_82481_;
            // Only the four managed combat pillars: never the pageant props.
            if (!monolith.m_6084_() || monolith.getTrueOwner() != boss
                    || pillarX * pillarX + pillarZ * pillarZ > radius * radius
                    || monolith.isEmerging()
                    || !monolith.getPersistentData().m_128471_(MANAGED_MONOLITH_TAG)
                    || monolith.getPersistentData().m_128471_(MONOLITH_TRANSFORMING_TAG)) {
                continue;
            }
            mob.m_6710_(monolith);
            mob.m_6274_().m_21882_(MemoryModuleType.f_26334_, monolith.m_20148_(), 600L);
            mob.m_6274_().m_21882_(MemoryModuleType.f_26372_, monolith, 600L);
            if (mob instanceof Warden warden) {
                warden.m_219387_(monolith, AngerLevel.ANGRY.m_219226_() + 20, false);
                warden.m_219459_(monolith);
            }
            return;
        }
    }

    @SubscribeEvent
    public static void suppressServantDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().m_128471_(NO_REWARDS_TAG)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void suppressServantExperience(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().m_128471_(NO_REWARDS_TAG)) {
            event.setDroppedExperience(0);
        }
    }

    private static void summonServants(ApollyonEntity boss, int count,
                                       Function<Level, ? extends Owned> factory) {
        if (!(boss.m_9236_() instanceof ServerLevel level) || !boss.m_6084_()) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            Owned servant = factory.apply(level);
            double angle = boss.m_217043_().m_188500_() * Math.PI * 2.0D;
            double radius = 3.0D + boss.m_217043_().m_188500_() * 4.0D;
            BlockPos candidate = BlockPos.m_274561_(
                    boss.m_20185_() + Math.cos(angle) * radius,
                    boss.m_20186_(),
                    boss.m_20189_() + Math.sin(angle) * radius);
            BlockPos spawnPos = BlockFinder.SummonRadius(candidate, servant, level, 5);
            queueGoetySummonCircle(boss, spawnPos, servant);
        }
    }

    private static void queueGoetySummonCircle(ApollyonEntity boss, BlockPos spawnPos,
                                                Owned servant) {
        if (!(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        servant.setLimitedLife(SERVANT_LIFETIME_TICKS);
        servant.getPersistentData().m_128379_(NO_REWARDS_TAG, true);
        // preMade + noPos is Goety's servant path: the circle owns finalization,
        // transfers Apollyon as true owner and copies his current target.
        SummonCircle circle = new SummonCircle(level, spawnPos, servant,
                true, true, boss);
        level.m_7967_(circle);
    }
}
