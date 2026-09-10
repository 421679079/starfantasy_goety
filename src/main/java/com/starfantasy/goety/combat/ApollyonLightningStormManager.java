package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.common.entities.util.MagicLightningTrap;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Owns the warning, timing and damage for Apollyon's lightning storm. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class ApollyonLightningStormManager {
    private static final int WARNING_TICKS = 20;
    private static final int DAMAGE_DELAY_TICKS = WARNING_TICKS + 1;
    private static final float MAGIC_LIGHTNING_RADIUS = 1.5F;
    private static final double MAGIC_LIGHTNING_DAMAGE_RANGE = MAGIC_LIGHTNING_RADIUS * 2.0D;
    private static final float LIGHTNING_DAMAGE = 30.0F;
    private static final int SPASMS_DURATION_TICKS = 200;
    private static final int SPASMS_AMPLIFIER = 3;

    private static final List<PendingStrike> PENDING_STRIKES = new ArrayList<>();

    /** Discard session-only state when the logical server stops. */
    public static void clearRuntimeState() {
        PENDING_STRIKES.clear();
    }

    private ApollyonLightningStormManager() {
    }

    public static Vec3 captureAnchor(ApollyonEntity boss, LivingEntity target) {
        if (boss == null || target == null) {
            return null;
        }
        return groundCenterAt(boss.m_9236_(), target.m_20185_(),
                Math.max(target.m_20186_(), boss.m_20186_()) + 8.0D,
                target.m_20189_(), Mth.m_14107_(target.m_20185_()), Mth.m_14107_(target.m_20189_()));
    }

    public static void queueTrackingStrike(ApollyonEntity boss, LivingEntity target) {
        Vec3 center = captureAnchor(boss, target);
        if (center != null) {
            queueStrike(boss, center);
        }
    }

    public static void queueRing(ApollyonEntity boss, Vec3 anchor, double radius, int count) {
        if (!canQueue(boss) || anchor == null || count <= 0) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            double angle = Math.PI * 2.0D * i / count;
            queueStrike(boss, anchor.m_82520_(
                    Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius));
        }
    }

    public static void queueCenter(ApollyonEntity boss, Vec3 anchor) {
        if (anchor != null) {
            queueStrike(boss, anchor);
        }
    }

    public static void clearForBoss(ApollyonEntity boss) {
        if (boss != null) {
            UUID bossUuid = boss.m_20148_();
            PENDING_STRIKES.removeIf(strike -> strike.bossUuid.equals(bossUuid));
        }
    }

    @SubscribeEvent
    public static void tickPendingStrikes(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }

        List<PendingStrike> due = new ArrayList<>();
        Iterator<PendingStrike> iterator = PENDING_STRIKES.iterator();
        while (iterator.hasNext()) {
            PendingStrike strike = iterator.next();
            if (!strike.dimension.equals(level.m_46472_())) {
                continue;
            }
            if (--strike.delayTicks > 0) {
                continue;
            }
            due.add(strike);
            iterator.remove();
        }

        for (PendingStrike strike : due) {
            detonate(level, strike);
        }
    }

    private static void queueStrike(ApollyonEntity boss, Vec3 desired) {
        if (!canQueue(boss)) {
            return;
        }
        Level level = boss.m_9236_();
        Vec3 center = groundCenterAt(level, desired.f_82479_, desired.f_82480_ + 8.0D,
                desired.f_82481_, Mth.m_14107_(desired.f_82479_), Mth.m_14107_(desired.f_82481_));
        StarFantasyVfx.redGroundWarningCircle(
                boss, center.m_82520_(0.0D, 0.06D, 0.0D), WARNING_TICKS,
                MAGIC_LIGHTNING_DAMAGE_RANGE);
        PENDING_STRIKES.add(new PendingStrike(boss, center, DAMAGE_DELAY_TICKS));
    }

    private static boolean canQueue(ApollyonEntity boss) {
        return boss != null && boss.m_6084_() && !boss.m_9236_().f_46443_;
    }

    private static void detonate(ServerLevel level, PendingStrike strike) {
        Entity entity = level.m_8791_(strike.bossUuid);
        if (!(entity instanceof ApollyonEntity boss) || !boss.m_6084_()) {
            return;
        }

        MagicLightningTrap sourceTrap = new MagicLightningTrap(level,
                strike.center.f_82479_, strike.center.f_82480_, strike.center.f_82481_);
        // Preserve Goety's native lightning-source geometry: the direct source is
        // the strike point, while Apollyon remains the causing entity.
        DamageSource source = ModDamageSource.lightning(sourceTrap, boss);
        double range = MAGIC_LIGHTNING_DAMAGE_RANGE;
        AABB area = new AABB(
                strike.center.f_82479_ - range,
                strike.center.f_82480_ - range,
                strike.center.f_82481_ - range,
                strike.center.f_82479_ + range,
                strike.center.f_82480_ + 6.0D + range,
                strike.center.f_82481_ + range);
        for (LivingEntity target : level.m_45976_(LivingEntity.class, area)) {
            if (shouldSkip(boss, target)) {
                continue;
            }
            if (target.m_6469_(source, boss.scaleOutgoingDamage(LIGHTNING_DAMAGE))) {
                target.m_7292_(new MobEffectInstance(
                        (MobEffect) GoetyEffects.SPASMS.get(),
                        SPASMS_DURATION_TICKS, SPASMS_AMPLIFIER));
            }
        }

        StarFantasyGoetyNetwork.sendApollyonLightningStrike(level, strike.center);
    }

    private static boolean shouldSkip(ApollyonEntity boss, LivingEntity target) {
        if (boss.isFriendlyEntity(target) || !target.m_6084_()) {
            return true;
        }
        return target instanceof Player player && (player.m_7500_() || player.m_5833_());
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

    private static final class PendingStrike {
        private final ResourceKey<Level> dimension;
        private final UUID bossUuid;
        private final Vec3 center;
        private int delayTicks;

        private PendingStrike(ApollyonEntity boss, Vec3 center, int delayTicks) {
            this.dimension = boss.m_9236_().m_46472_();
            this.bossUuid = boss.m_20148_();
            this.center = center;
            this.delayTicks = Math.max(1, delayTicks);
        }
    }
}
