package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith;
import com.Polarice3.Goety.utils.BlockFinder;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.starfantasy.goety.entity.HadesServantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Persist the lifetime on each pillar, independently of the owner's active AI. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ApollyonServantMonoliths {
    private static final String AGE = "ApollyonServantMonolithAge";
    private static final String EXPIRED = "ApollyonServantMonolithExpired";
    private static final String OWNER = "ApollyonServantMonolithOwner";
    private ApollyonServantMonoliths() { }

    public static void summon(ApollyonServantEntity servant) {
        if (!(servant.m_9236_() instanceof ServerLevel level)) return;
        double[][] offsets;
        if (servant.isCombatPhaseTwo()) {
            double d = 14.0 / Math.sqrt(2.0);
            offsets = new double[][]{{d,d},{d,-d},{-d,d},{-d,-d}};
        } else {
            double yaw = Math.toRadians(servant.m_146908_());
            double x = Math.cos(yaw)*10, z = Math.sin(yaw)*10;
            offsets = new double[][]{{x,z},{-x,-z}};
        }
        // A mobile servant has no arena home; anchor this cast to its current position.
        for (double[] offset : offsets) {
            ObsidianMonolith pillar = new ObsidianMonolith(ModEntityType.OBSIDIAN_MONOLITH.get(),level);
            BlockPos candidate = BlockPos.m_274561_(servant.m_20185_()+offset[0],servant.m_20186_(),servant.m_20189_()+offset[1]);
            BlockPos position = BlockFinder.SummonPosition(level,pillar,candidate);
            pillar.m_20035_(position,0,0);
            pillar.setTrueOwner(servant);
            pillar.shouldSpawnHeretics = false;
            pillar.m_6518_(level,level.m_6436_(position),MobSpawnType.MOB_SUMMONED,null,null);
            pillar.m_21051_(Attributes.f_22276_).m_22100_(20);
            pillar.m_21153_(20);
            pillar.getPersistentData().m_128405_(AGE,0);
            pillar.getPersistentData().m_128362_(OWNER,servant.m_20148_());
            if (level.m_7967_(pillar)) servant.trackMonolith(pillar.m_20148_());
        }
    }

    @SubscribeEvent public static void tick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof ObsidianMonolith pillar) || pillar.m_9236_().f_46443_
                || !pillar.m_6084_() || !pillar.getPersistentData().m_128441_(AGE)
                || pillar.getPersistentData().m_128471_(EXPIRED)) return;
        ServerLevel level=(ServerLevel)pillar.m_9236_();
        if (com.starfantasy.goety.servant.ServantOwnershipData.consumeRetiredPillar(level,pillar.m_20148_())) {
            pillar.m_146870_(); return;
        }
        java.util.UUID owner=owner(pillar);
        if (owner!=null && level.m_8791_(owner) instanceof ApollyonServantEntity servant) {
            if (!servant.m_6084_() || servant.deathAge()>0) { pillar.m_146870_(); servant.releaseMonolith(pillar.m_20148_()); return; }
            servant.trackMonolith(pillar.m_20148_());
            if (pillar.m_20280_(servant)>32.0*32.0 || !pillar.isEmerging() && !pillar.m_142582_(servant))
                pillar.teleportTowards(servant);
        }
        int age = pillar.getPersistentData().m_128451_(AGE)+1;
        pillar.getPersistentData().m_128405_(AGE,Math.min(800,age));
        if (age < 800) return;
        pillar.getPersistentData().m_128379_(EXPIRED,true);
        if (owner!=null && level.m_8791_(owner) instanceof ApollyonServantEntity servant) {
            if (servant.m_6084_()) servant.addMonolithShield();
        } else if (owner!=null && com.starfantasy.goety.servant.ServantOwnershipData.contains(level,owner)) {
            com.starfantasy.goety.servant.ServantOwnershipData.creditPillar(
                    level,owner);
        }
        pillar.silentDie(pillar.m_269291_().m_269064_());
    }

    private static java.util.UUID owner(ObsidianMonolith pillar) {
        return pillar.getPersistentData().m_128403_(OWNER)
                ?pillar.getPersistentData().m_128342_(OWNER):pillar.getOwnerId();
    }

    public static void clear(ApollyonServantEntity servant) {
        if (!(servant.m_9236_() instanceof ServerLevel level)) return;
        java.util.Set<java.util.UUID> remaining=new java.util.HashSet<>(servant.monolithIds());
        for (ServerLevel dimension:level.m_7654_().m_129785_()) {
            java.util.List<ObsidianMonolith> loaded=new java.util.ArrayList<>();
            for (var entity:dimension.m_8583_())
                if (entity instanceof ObsidianMonolith pillar && pillar.getPersistentData().m_128441_(AGE)
                        && servant.m_20148_().equals(owner(pillar))) loaded.add(pillar);
            for (var pillar:loaded) {
                remaining.remove(pillar.m_20148_());
                pillar.getPersistentData().m_128379_(EXPIRED,true);
                pillar.m_146870_();
            }
        }
        for (java.util.UUID id:remaining) com.starfantasy.goety.servant.ServantOwnershipData.retirePillar(level,id);
        for (java.util.UUID id:servant.monolithIds()) servant.releaseMonolith(id);
        com.starfantasy.goety.servant.ServantOwnershipData.claimPillars(level,servant.m_20148_());
    }

    @SubscribeEvent public static void died(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        release(event.getEntity());
    }
    private static void release(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof ObsidianMonolith pillar && pillar.m_9236_() instanceof ServerLevel level
                && pillar.getPersistentData().m_128441_(AGE) && owner(pillar)!=null
                && level.m_8791_(owner(pillar)) instanceof ApollyonServantEntity servant)
            servant.releaseMonolith(pillar.m_20148_());
    }
    @SubscribeEvent public static void removed(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) {
        var reason=event.getEntity().m_146911_();
        if (reason!=net.minecraft.world.entity.Entity.RemovalReason.KILLED
                && reason!=net.minecraft.world.entity.Entity.RemovalReason.DISCARDED) return;
        if (event.getEntity() instanceof ApollyonServantEntity servant) clear(servant);
        else release(event.getEntity());
    }
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void redirect(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof ApollyonServantEntity servant && !servant.isFriendlyEntity(event.getEntity())) {
            var pillar=servant.aggroMonolith();
            if (pillar!=null) event.setNewTarget(pillar);
        } else if (event.getNewTarget() instanceof HadesServantEntity hades) {
            ApollyonServantEntity servant = hades.mountedApollyonServant();
            if (servant != null && !servant.isFriendlyEntity(event.getEntity())) {
                var pillar = servant.aggroMonolith();
                if (pillar != null) event.setNewTarget(pillar);
            }
        }
    }

    public static void redirectAttacker(HadesServantEntity hades, DamageSource source) {
        ApollyonServantEntity servant = hades.mountedApollyonServant();
        if (servant == null) return;
        ObsidianMonolith pillar = servant.aggroMonolith();
        if (pillar == null) return;
        Entity attacker = source.m_7639_();
        if (!(attacker instanceof Mob)) attacker = source.m_7640_();
        if (attacker instanceof Warden warden) {
            redirectWarden(warden, servant, pillar);
            return;
        }
        if (attacker instanceof Mob mob && mob.m_6084_() && !servant.isFriendlyEntity(mob)
                && mob.m_5448_() != pillar) mob.m_6710_(pillar);
    }

    public static void redirectWarden(Warden warden, ApollyonServantEntity servant, ObsidianMonolith pillar) {
        if (!warden.m_6084_()) return;
        warden.m_219428_(servant);
        if (servant.m_20202_() instanceof HadesServantEntity hades
                && hades.mountedApollyonServant() == servant) warden.m_219428_(hades);
        warden.m_6710_(pillar);
        warden.m_6274_().m_21882_(MemoryModuleType.f_26334_, pillar.m_20148_(), 600L);
        warden.m_6274_().m_21882_(MemoryModuleType.f_26372_, pillar, 600L);
        warden.m_219387_(pillar, AngerLevel.ANGRY.m_219226_() + 20, false);
        warden.m_219459_(pillar);
    }

    @SubscribeEvent public static void redirectWardenTarget(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Warden warden) || warden.m_9236_().f_46443_) return;
        ApollyonServantEntity servant;
        if (warden.m_5448_() instanceof HadesServantEntity hades) servant = hades.mountedApollyonServant();
        else if (warden.m_5448_() instanceof ApollyonServantEntity rider) servant = rider;
        else return;
        if (servant == null) return;
        ObsidianMonolith pillar = servant.aggroMonolith();
        if (pillar != null) redirectWarden(warden, servant, pillar);
    }
}
