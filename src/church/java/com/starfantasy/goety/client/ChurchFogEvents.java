package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Removes only vanilla air-distance fog while the camera is inside the church. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class ChurchFogEvents {
    private static final float NO_FOG_START = 1_000_000.0F;
    private static final float NO_FOG_END = NO_FOG_START + 1_024.0F;
    private static boolean insideChurch;

    private ChurchFogEvents() {
    }

    public static void setInsideChurch(boolean inside) {
        insideChurch = inside;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        var player = Minecraft.getInstance().player;
        if (!insideChurch || player == null || event.getType() != FogType.NONE
                || player.hasEffect(MobEffects.BLINDNESS)
                || player.hasEffect(MobEffects.DARKNESS)) {
            return;
        }
        event.setNearPlaneDistance(NO_FOG_START);
        event.setFarPlaneDistance(NO_FOG_END);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level == null) {
            insideChurch = false;
        }
    }
}
