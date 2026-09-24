package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.util.FireBlastTrap;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.world.entity.Mob;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class ApostleFireTrapManager {
    private static final String MANAGED_TRAP_TAG = "starfantasy_apostle_managed_fire_trap";
    private static final int WARNING_TICKS = 20;
    private static final int DAMAGE_DELAY_TICKS = WARNING_TICKS + 1;
    private static final double TRAP_RADIUS = 2.5D;
    private static final int[] CROSS_DISTANCES = {3, 6, 9};
    private static final int RING_COUNT = 18;
    private static final double RING_RADIUS = 9.0D;
    private static final int RING_DELAY_TICKS = 20;

    private static final List<PendingRing> PENDING_RINGS = new ArrayList<>();
    private static final List<PendingTrap> PENDING_TRAPS = new ArrayList<>();

    /** Discard session-only state when the logical server stops. */
    public static void clearRuntimeState() {
        PENDING_RINGS.clear();
        PENDING_TRAPS.clear();
    }

    private ApostleFireTrapManager() {
    }

    public static void cast(Mob boss, LivingEntity target) {
        if (boss == null || target == null || boss.m_9236_().f_46443_
                || !boss.m_6084_() || !target.m_6084_()) {
            return;
        }

        Vec3 base = groundCenterAt(boss.m_9236_(), target.m_20185_(),
                Math.max(target.m_20186_(), boss.m_20186_()) + 8.0D,
                target.m_20189_(), Mth.m_14107_(target.m_20185_()), Mth.m_14107_(target.m_20189_()));
        Set<BlockPos> spawned = new LinkedHashSet<>();
        spawnTrapOnce(boss, base, spawned);
        for (int distance : CROSS_DISTANCES) {
            spawnTrapOnce(boss, base.m_82520_(distance, 0.0D, 0.0D), spawned);
            spawnTrapOnce(boss, base.m_82520_(-distance, 0.0D, 0.0D), spawned);
            spawnTrapOnce(boss, base.m_82520_(0.0D, 0.0D, distance), spawned);
            spawnTrapOnce(boss, base.m_82520_(0.0D, 0.0D, -distance), spawned);
        }
        if (com.starfantasy.goety.combat.apostle.ApostleSpellSupport.secondPhase(boss)) PENDING_RINGS.add(new PendingRing(boss, base, RING_DELAY_TICKS));
    }

    public static void clearForBoss(Mob boss) {
        if (boss == null || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        UUID bossUuid = boss.m_20148_();
        PENDING_RINGS.removeIf(ring -> ring.bossUuid.equals(bossUuid));
        PENDING_TRAPS.removeIf(trap -> trap.bossUuid.equals(bossUuid));
        Vec3 home = boss.m_20182_();
        AABB area = new AABB(home, home).m_82400_(128.0D);
        for (FireBlastTrap trap : level.m_45976_(FireBlastTrap.class, area)) {
            if (trap.m_19880_().contains(MANAGED_TRAP_TAG) && trap.getOwner() == boss) {
                trap.m_146870_();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelNativeTrapDamage(LivingAttackEvent event) {
        if (managedTrap(event.getSource().m_7639_()) != null
                || managedTrap(event.getSource().m_7640_()) != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickPendingActions(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        List<PendingRing> dueRings = new ArrayList<>();
        Iterator<PendingRing> ringIterator = PENDING_RINGS.iterator();
        while (ringIterator.hasNext()) {
            PendingRing ring = ringIterator.next();
            if (!ring.dimension.equals(level.m_46472_())) {
                continue;
            }
            if (--ring.delayTicks > 0) {
                continue;
            }
            dueRings.add(ring);
            ringIterator.remove();
        }

        List<PendingTrap> dueTraps = new ArrayList<>();
        Iterator<PendingTrap> trapIterator = PENDING_TRAPS.iterator();
        while (trapIterator.hasNext()) {
            PendingTrap trap = trapIterator.next();
            if (!trap.dimension.equals(level.m_46472_())) {
                continue;
            }
            if (--trap.delayTicks > 0) {
                continue;
            }
            dueTraps.add(trap);
            trapIterator.remove();
        }

        for (PendingRing ring : dueRings) {
            Entity entity = level.m_8791_(ring.bossUuid);
            if (entity instanceof Mob boss && boss.m_6084_()) {
                spawnRing(boss, ring.center);
            }
        }
        for (PendingTrap trap : dueTraps) {
            detonate(level, trap);
        }
    }

    private static void detonate(ServerLevel level, PendingTrap trap) {
        Entity entity = level.m_8791_(trap.bossUuid);
        if (!(entity instanceof Mob boss) || !boss.m_6084_()) {
            return;
        }

        DamageSource source = ModDamageSource.hellfire(boss, boss);
        AABB searchBox = new AABB(trap.center, trap.center).m_82400_(TRAP_RADIUS);
        for (LivingEntity target : level.m_45976_(LivingEntity.class, searchBox)) {
            if (shouldSkip(boss, target)
                    || !intersectsSphere(target.m_20191_(), trap.center, TRAP_RADIUS)) {
                continue;
            }
            target.m_6469_(source, ApostleSpellSupport.damage(4));
        }
    }

    private static void spawnRing(Mob boss, Vec3 base) {
        Set<BlockPos> spawned = new LinkedHashSet<>();
        for (int i = 0; i < RING_COUNT; ++i) {
            double angle = Math.PI * 2.0D * i / RING_COUNT;
            spawnTrapOnce(boss, base.m_82520_(
                    Math.cos(angle) * RING_RADIUS,
                    0.0D,
                    Math.sin(angle) * RING_RADIUS), spawned);
        }
    }

    private static void spawnTrapOnce(Mob boss, Vec3 desired, Set<BlockPos> spawned) {
        Level level = boss.m_9236_();
        Vec3 center = groundCenterAt(level, desired.f_82479_, desired.f_82480_ + 8.0D, desired.f_82481_,
                Mth.m_14107_(desired.f_82479_), Mth.m_14107_(desired.f_82481_));
        BlockPos key = BlockPos.m_274561_(center.f_82479_, center.f_82480_, center.f_82481_);
        if (!spawned.add(key)) {
            return;
        }

        if (com.starfantasy.goety.combat.apostle.ApostleSpellSupport.showWarnings(boss)) StarFantasyVfx.redGroundWarningCircle(
                boss, center.m_82520_(0.0D, 0.06D, 0.0D), WARNING_TICKS, TRAP_RADIUS);
        if (spawnVisual(boss, center)) {
            PENDING_TRAPS.add(new PendingTrap(boss, center, DAMAGE_DELAY_TICKS));
        }
    }

    private static boolean spawnVisual(Mob boss, Vec3 center) {
        if (!(boss.m_9236_() instanceof ServerLevel level)) {
            return false;
        }

        FireBlastTrap trap = new FireBlastTrap(level,
                center.f_82479_, center.f_82480_, center.f_82481_);
        trap.m_20049_(MANAGED_TRAP_TAG);
        trap.m_20256_(Vec3.f_82478_);
        trap.setOwner(boss);
        trap.setAreaOfEffect(0.0F);
        trap.setExtraDamage(0.0F);
        trap.setBurning(0);
        trap.setImmediate(false);
        return level.m_7967_(trap);
    }

    private static boolean shouldSkip(Mob boss, LivingEntity target) {
        if (ApostleSpellSupport.friendly(boss, target) || !target.m_6084_()) {
            return true;
        }
        return target instanceof Player player && (player.m_7500_() || player.m_5833_());
    }

    private static Entity managedTrap(Entity entity) {
        return entity != null && entity.m_19880_().contains(MANAGED_TRAP_TAG) ? entity : null;
    }

    private static Vec3 groundCenterAt(Level level, double preciseX, double searchY, double preciseZ,
                                       int blockX, int blockZ) {
        int startY = Math.min(level.m_151558_() - 1, Mth.m_14107_(searchY));
        int minY = level.m_141937_() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) {
                return new Vec3(preciseX, y + 0.06D, preciseZ);
            }
        }
        return new Vec3(preciseX, searchY + 0.06D, preciseZ);
    }

    private static boolean intersectsSphere(AABB box, Vec3 center, double radius) {
        double dx = distanceToInterval(center.f_82479_, box.f_82288_, box.f_82291_);
        double dy = distanceToInterval(center.f_82480_, box.f_82289_, box.f_82292_);
        double dz = distanceToInterval(center.f_82481_, box.f_82290_, box.f_82293_);
        return dx * dx + dy * dy + dz * dz <= radius * radius;
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

    private static final class PendingRing {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private int delayTicks;

        private PendingRing(Mob boss, Vec3 center, int delayTicks) {
            this.dimension = boss.m_9236_().m_46472_();
            this.bossUuid = boss.m_20148_();
            this.center = center;
            this.delayTicks = Math.max(1, delayTicks);
        }
    }

    private static final class PendingTrap {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private int delayTicks;

        private PendingTrap(Mob boss, Vec3 center, int delayTicks) {
            this.dimension = boss.m_9236_().m_46472_();
            this.bossUuid = boss.m_20148_();
            this.center = center;
            this.delayTicks = Math.max(1, delayTicks);
        }
    }
}
