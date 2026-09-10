package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Unremovable client darkness used only by Apollyon's final pageant act. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class ApollyonPageantDarkness {
    private static final int FADE_TICKS = 20;
    private static int remainingTicks;
    private static int totalTicks;

    private ApollyonPageantDarkness() {
    }

    public static void setDuration(int duration) {
        remainingTicks = Math.max(0, duration);
        totalTicks = remainingTicks;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            --remainingTicks;
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float darkness = darknessAmount();
        if (darkness <= 0.0F) {
            return;
        }
        float light = 1.0F - darkness;
        event.setRed(event.getRed() * light);
        event.setGreen(event.getGreen() * light);
        event.setBlue(event.getBlue() * light);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float darkness = darknessAmount();
        if (darkness <= 0.0F) {
            return;
        }
        event.setNearPlaneDistance(lerp(darkness, event.getNearPlaneDistance(), 0.0F));
        event.setFarPlaneDistance(lerp(darkness, event.getFarPlaneDistance(), 2.5F));
        event.setCanceled(true);
    }

    private static float darknessAmount() {
        if (remainingTicks <= 0 || totalTicks <= 0) {
            return 0.0F;
        }
        int elapsedTicks = totalTicks - remainingTicks;
        float fadeIn = clamp(elapsedTicks / (float) FADE_TICKS);
        float fadeOut = clamp(remainingTicks / (float) FADE_TICKS);
        return Math.min(fadeIn, fadeOut);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float lerp(float progress, float start, float end) {
        return start + (end - start) * progress;
    }
}
