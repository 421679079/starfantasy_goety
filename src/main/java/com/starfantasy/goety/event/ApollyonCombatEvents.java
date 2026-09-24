package com.starfantasy.goety.event;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.compat.ApostleCompatibility;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Places Apollyon's configurable reductions before armor and its cap after all mitigation. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class ApollyonCombatEvents {
    private ApollyonCombatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onApollyonHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof com.starfantasy.goety.entity.ApollyonServantEntity servant) {
            event.setAmount(servant.applyIncomingDamageReductions(event.getSource(), event.getAmount()));
        } else if (event.getEntity() instanceof ApollyonEntity boss) {
            event.setAmount(boss.applyIncomingDamageReductions(
                    event.getSource(), event.getAmount()));
        } else if (event.getEntity() instanceof ApollyonPageantApostleEntity apostle
                && apostle.isPageantDamageable()) {
            event.setAmount(apostle.applyIncomingDamageReductions(
                    event.getSource(), event.getAmount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onApollyonDamage(LivingDamageEvent event) {
        // getEntity is the attacker, including the shooter of a projectile.
        if (event.getSource().m_7639_() instanceof LivingEntity attacker) {
            MobEffectInstance weakness = attacker.m_21124_(ApollyonEffectRegistry.WEAKNESS.get());
            if (weakness != null) {
                float multiplier = Math.max(0.0F,
                        1.0F - 0.1F * (weakness.m_19564_() + 1));
                event.setAmount(event.getAmount() * multiplier);
            }
        }
        if (event.getEntity() instanceof com.starfantasy.goety.entity.ApollyonServantEntity servant) {
            event.setAmount(servant.finishIncomingDamage(event.getSource(), event.getAmount()));
        } else if (event.getEntity() instanceof ApollyonEntity boss) {
            float finalDamage = boss.clampFinalDamage(event.getAmount());
            finalDamage = boss.absorbCooperativeShield(event.getSource(), finalDamage);
            if (boss.tryStartPageantFromFinalDamage(finalDamage)) {
                event.setAmount(0.0F);
                event.setCanceled(true);
            } else {
                event.setAmount(finalDamage);
            }
        } else if (event.getEntity() instanceof ApollyonPageantApostleEntity apostle
                && apostle.isPageantDamageable()) {
            event.setAmount(apostle.clampFinalDamage(event.getAmount()));
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onApollyonDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ApollyonEntity boss)) return;
        var home = boss.arenaHomePosition();
        for (ItemEntity drop : event.getDrops()) {
            drop.m_6034_(home.f_82479_, home.f_82480_ + 0.5D, home.f_82481_);
            drop.m_20256_(new net.minecraft.world.phys.Vec3(0.0D, 0.2D, 0.0D));
        }
    }

    @SubscribeEvent
    public static void onNetherApostleDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Apostle apostle)
                || !apostle.isInNether()
                || ApostleCompatibility.isRevelationApollyon(apostle)) {
            return;
        }
        int title = apostle.getTitleNumber();
        if (title < 0 || title >= HaloItemRegistry.APOSTLE_HALOS.size()) {
            return;
        }
        ItemStack halo = new ItemStack(HaloItemRegistry.APOSTLE_HALOS.get(title).get());
        event.getDrops().add(new ItemEntity(apostle.m_9236_(),
                apostle.m_20185_(), apostle.m_20186_(), apostle.m_20189_(), halo));
    }
}
