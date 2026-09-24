package com.starfantasy.goety.event;

import com.Polarice3.Goety.api.entities.IOwned;
import com.starfantasy.goety.entity.ApostleServantEntity;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ApostleServantEvents {
    private ApostleServantEvents() { }

    public static ApostleServantEntity owner(Entity attacker) {
        Set<Entity> visited=new HashSet<>();
        while(attacker!=null && visited.add(attacker)) {
            if(attacker instanceof ApostleServantEntity servant) return servant;
            if(attacker instanceof IOwned owned) attacker=owned.getTrueOwner();
            else if(attacker instanceof Projectile projectile) attacker=projectile.m_19749_();
            else return null;
        }
        return null;
    }
    @SubscribeEvent
    public static void tornadoExpired(net.minecraftforge.event.entity.EntityLeaveLevelEvent event) {
        if(!event.getLevel().f_46443_ && event.getEntity() instanceof com.Polarice3.Goety.common.entities.projectiles.FireTornado tornado
                && tornado.getLifespan()>=tornado.getTotalLife() && tornado.getTrueOwner() instanceof ApostleServantEntity servant)
            servant.onTornadoExpired();
    }
    @SubscribeEvent
    public static void hellCloudBurn(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        if(event.getAmount()>0 && event.getSource().m_7640_() instanceof com.Polarice3.Goety.common.entities.projectiles.HellCloud
                && event.getSource().m_7639_() instanceof ApostleServantEntity servant && !servant.isFriendlyEntity(event.getEntity()))
            event.getEntity().m_147207_(new net.minecraft.world.effect.MobEffectInstance(
                    com.Polarice3.Goety.common.effects.GoetyEffects.BURN_HEX.get(),1200),servant);
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void scaleDamage(LivingHurtEvent event) {
        ApostleServantEntity servant=owner(event.getSource().m_7639_());
        if(servant==null) servant=owner(event.getSource().m_7640_());
        if(servant!=null) event.setAmount(event.getAmount()*servant.damageMultiplier());
    }
    @SubscribeEvent
    public static void expireSummons(LivingEvent.LivingTickEvent event) {
        LivingEntity entity=event.getEntity();
        if(!(entity.m_9236_() instanceof ServerLevel level)) return;
        if(entity instanceof com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith pillar
                && pillar.m_6084_() && pillar.getTrueOwner() instanceof ApostleServantEntity servant && servant.m_6084_()
                && (pillar.m_20280_(servant)>32.0*32.0 || !pillar.isEmerging() && !pillar.m_142582_(servant)))
            pillar.teleportTowards(servant);
        var tag=entity.getPersistentData();
        if(!tag.m_128441_("StarFantasyServantExpires")) return;
        if(entity instanceof com.Polarice3.Goety.common.entities.hostile.servants.Inferno) {
            // Existing summons already carry LifeTicks; remove the old forced-kill deadline.
            tag.m_128473_("StarFantasyServantExpires"); return;
        }
        if(level.m_46467_()>=tag.m_128454_("StarFantasyServantExpires")) {
            release(entity,level);
            if(entity instanceof com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith pillar)
                pillar.silentDie(entity.m_269291_().m_269064_());
            else entity.m_6074_();
        }
    }
    @SubscribeEvent
    public static void redirectTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if(event.getNewTarget() instanceof ApostleServantEntity servant && !servant.isFriendlyEntity(event.getEntity())) {
            var pillar=servant.aggroMonolith(); if(pillar!=null) event.setNewTarget(pillar);
        }
    }
    @SubscribeEvent
    public static void summonDied(LivingDeathEvent event) {
        if(event.getEntity().m_9236_() instanceof ServerLevel level) release(event.getEntity(),level);
    }
    private static void release(LivingEntity entity,ServerLevel level) {
        var tag=entity.getPersistentData();
        if(tag.m_128403_("StarFantasyServantOwner") && level.m_8791_(tag.m_128342_("StarFantasyServantOwner")) instanceof ApostleServantEntity owner)
            owner.minionRemoved(entity.m_20148_());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void protectAllies(LivingAttackEvent event) {
        ApostleServantEntity servant=owner(event.getSource().m_7639_());
        if(servant==null) servant=owner(event.getSource().m_7640_());
        if(servant!=null && servant.m_7307_(event.getEntity())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void noBossLoot(LivingDropsEvent event) {
        if (event.getEntity() instanceof ApostleServantEntity) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void noBossExperience(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof ApostleServantEntity) event.setDroppedExperience(0);
    }
}
