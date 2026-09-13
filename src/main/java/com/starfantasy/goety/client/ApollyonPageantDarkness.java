package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Full-screen black overlay used only by Apollyon's final pageant act. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class ApollyonPageantDarkness {
    private static final int FADE_TICKS = 20;
    private static int remainingTicks;
    private static int totalTicks;
    private static ClientLevel ownerLevel;

    private ApollyonPageantDarkness() {
    }

    public static void setDuration(int duration) {
        ownerLevel = Minecraft.m_91087_().f_91073_;
        if (duration <= 0 || ownerLevel == null) {
            clear();
            return;
        }
        remainingTicks = Math.max(0, duration);
        totalTicks = remainingTicks;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            if (Minecraft.m_91087_().f_91073_ != ownerLevel || --remainingTicks == 0) {
                clear();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ownerLevel == null || Minecraft.m_91087_().f_91073_ != ownerLevel) {
            clear();
            return;
        }
        float alpha = blackoutAlpha(event.getPartialTick());
        if (alpha <= 0.0F) {
            return;
        }
        int alphaByte = Math.min(255, Math.max(0, Math.round(alpha * 255.0F)));
        event.getGuiGraphics().m_280509_(
                0, 0, event.getWindow().m_85445_(), event.getWindow().m_85446_(), alphaByte << 24);
    }

    private static float blackoutAlpha(float partialTick) {
        if (remainingTicks <= 0 || totalTicks <= 0) {
            return 0.0F;
        }
        float elapsedTicks = totalTicks - remainingTicks + partialTick;
        float fadeIn = clamp(elapsedTicks / FADE_TICKS);
        float fadeOut = clamp((remainingTicks - partialTick) / FADE_TICKS);
        return Math.min(fadeIn, fadeOut);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void clear() {
        remainingTicks = 0;
        totalTicks = 0;
        ownerLevel = null;
    }
}
