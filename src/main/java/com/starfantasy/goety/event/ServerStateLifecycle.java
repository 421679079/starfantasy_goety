package com.starfantasy.goety.event;

import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ServerStateLifecycle {
    private ServerStateLifecycle() {
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        com.starfantasy.goety.combat.ApollyonFireTrapManager.clearRuntimeState();
        com.starfantasy.goety.combat.ApollyonLightningStormManager.clearRuntimeState();
        com.starfantasy.goety.combat.apostle.ApostleFireTrapManager.clearRuntimeState();
        com.starfantasy.goety.combat.apostle.ApostleLightningStormManager.clearRuntimeState();
    }
}
