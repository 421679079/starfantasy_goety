package com.starfantasy.goety.event;

import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.goety.config.AscianKingConfig;
import com.starfantasy.goety.combat.ascian.*;
import com.starfantasy.library.posture.PerfectGuardEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="starfantasy_goety")
public final class AscianCombatEvents {
    @SubscribeEvent public static void guard(PerfectGuardEvent event) {
        if(event.getSource().m_7639_() instanceof AscianKingEntity boss && event.getSource().m_7640_()==boss)
            boss.combat.recordPerfectGuard(event.getPlayer());
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void hurt(LivingHurtEvent event) {
        if(event.getEntity() instanceof AscianKingEntity boss) {
            if(event.getAmount()>0.0F)boss.combat.recordClash(event.getSource());
            float amount=event.getAmount();
            if(event.getSource().m_269533_(DamageTypeTags.f_268731_))amount*=1-AscianKingConfig.MAGIC_REDUCTION.get();
            if(!boss.combat.stunned())amount*=1-AscianKingConfig.FIXED_REDUCTION.get();
            event.setAmount(amount);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void damage(LivingDamageEvent event) {
        if(event.getEntity() instanceof AscianKingEntity boss) {
            double cap=AscianKingConfig.DAMAGE_CAP.get();
            if(cap>0)event.setAmount(Math.min(event.getAmount(),(float)cap));
            event.setAmount((float)(event.getAmount()*AscianRangedProtection.multiplier(
                    AscianRangedProtection.distance(boss,event.getSource()),AscianKingConfig.RANGED_PROTECTION_DISTANCE.get())));
            if(boss.combat.transitioning())event.setAmount(0);
        }
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void nativeSpell(LivingAttackEvent event) {
        Entity direct=event.getSource().m_7640_();
        if(direct==null||!direct.getPersistentData().m_128403_(AscianSpells.OWNER))direct=event.getSource().m_7639_();
        if(direct==null||!direct.getPersistentData().m_128403_(AscianSpells.OWNER))return;
        event.setCanceled(true);
        // Old managed Goety rifts and visual fangs must never apply native damage.
    }
    @SubscribeEvent public static void drops(LivingDropsEvent event){if(event.getEntity().getPersistentData().m_128471_(AscianSpells.NO_LOOT))event.setCanceled(true);}
    @SubscribeEvent public static void xp(LivingExperienceDropEvent event){if(event.getEntity().getPersistentData().m_128471_(AscianSpells.NO_LOOT))event.setDroppedExperience(0);}
}
