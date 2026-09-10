package com.starfantasy.goety.effect;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Owns Apocalyptic Famine's server drain and blocks ordinary cleansing. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ApollyonFamineEffectEvents {
    private static final int FOOD_DRAIN_INTERVAL_TICKS = 20;
    private static final float DRAIN_PER_INTERVAL = 2.0F;
    private static final float MISSING_FOOD_DAMAGE_MULTIPLIER = 3.0F;
    private static final int MAX_FOOD = 20;
    private static final float MAX_SATURATION = 20.0F;
    private static final Set<UUID> AUTHORIZED_REMOVALS = new HashSet<>();

    private ApollyonFamineEffectEvents() {
    }

    private static void removeFamine(LivingEntity living) {
        if (living == null || !living.m_21023_(ApollyonEffectRegistry.FAMINE.get())) {
            return;
        }
        UUID uuid = living.m_20148_();
        AUTHORIZED_REMOVALS.add(uuid);
        try {
            living.m_21195_(ApollyonEffectRegistry.FAMINE.get());
        } finally {
            AUTHORIZED_REMOVALS.remove(uuid);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.m_9236_().f_46443_
                || !(living instanceof Player player)
                || !player.m_6084_()
                || !player.m_21023_(ApollyonEffectRegistry.FAMINE.get())) {
            return;
        }

        FoodData foodData = player.m_36324_();
        float saturation = Math.max(0.0F, foodData.m_38722_());
        int food = Math.max(0, foodData.m_38702_());
        if (food >= MAX_FOOD && (!ApollyonConfig.hardMode() || saturation >= MAX_SATURATION)) {
            removeFamine(player);
            return;
        }

        if (player.f_19797_ % FOOD_DRAIN_INTERVAL_TICKS != 0) {
            return;
        }

        float available = saturation + food;
        float saturationSpent = Math.min(saturation, DRAIN_PER_INTERVAL);
        foodData.m_38717_(saturation - saturationSpent);

        float remainingDrain = DRAIN_PER_INTERVAL - saturationSpent;
        int foodSpent = Math.min(food, (int) Math.ceil(remainingDrain));
        foodData.m_38705_(food - foodSpent);

        float missing = Math.max(0.0F, DRAIN_PER_INTERVAL - available);
        if (missing > 0.0F) {
            player.m_6469_(player.m_269291_().m_269064_(),
                    missing * MISSING_FOOD_DAMAGE_MULTIPLIER);
        }
    }

    @SubscribeEvent
    public static void onRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == ApollyonEffectRegistry.FAMINE.get()
                && event.getEntity().m_6084_()
                && !AUTHORIZED_REMOVALS.contains(event.getEntity().m_20148_())) {
            event.setCanceled(true);
        }
    }
}
