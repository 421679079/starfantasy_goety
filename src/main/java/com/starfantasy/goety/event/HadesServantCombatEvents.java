package com.starfantasy.goety.event;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.config.ServantConfig;
import com.starfantasy.goety.entity.HadesServantEntity;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class HadesServantCombatEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof HadesServantEntity
                && !event.getSource().m_269533_(DamageTypeTags.f_268738_)) {
            double amount = event.getAmount() * (1.0 - ServantConfig.DAMAGE_REDUCTION.get());
            if (event.getSource().m_269533_(DamageTypeTags.f_268731_)) {
                amount *= 1.0 - ServantConfig.MAGIC_RESISTANCE.get();
            }
            event.setAmount((float) amount);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damage(LivingDamageEvent event) {
        if (event.getEntity() instanceof HadesServantEntity
                && !event.getSource().m_269533_(DamageTypeTags.f_268738_)
                && ServantConfig.DAMAGE_CAP.get() > 0.0) {
            event.setAmount(Math.min(event.getAmount(), ServantConfig.DAMAGE_CAP.get().floatValue()));
        }
    }
}
