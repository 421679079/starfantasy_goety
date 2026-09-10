package com.starfantasy.goety.event;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.config.ApostleConfig;
import com.starfantasy.goety.combat.apostle.ApostleShield;
import com.starfantasy.goety.combat.apostle.ApostleSpellSupport;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ApostleCombatEvents {
    private ApostleCombatEvents() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Apostle boss && ApostleSpellSupport.original(boss)
                && !boss.isCasting())
            event.setAmount((float) (event.getAmount() * ApostleConfig.DAMAGE_TAKEN_MULTIPLIER.get()));
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Apostle boss) || !ApostleSpellSupport.original(boss)) return;
        float shield = ApostleShield.get(boss);
        if (shield <= 0 || event.getAmount() <= 0) return;
        float cost = event.getSource().m_269533_(DamageTypeTags.f_268731_) ? 4 : 1;
        float absorbed = Math.min(shield / cost, event.getAmount());
        ApostleShield.set(boss, shield - absorbed * cost);
        // Keep resistance for this hit even if it breaks the shield.
        ApostleShield.updateKnockback(boss, true);
        event.setAmount(Math.max(0, event.getAmount() - absorbed));
    }
}
