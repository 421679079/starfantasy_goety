package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Full-screen white flash used by the Infernal Judgment impact. */
@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class ApollyonPageantWhiteout {
    private static final int FADE_IN_TICKS = 20;
    private static final int HOLD_TICKS = 20;
    private static final int FADE_OUT_TICKS = 20;
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    private static int remainingTicks;

    private ApollyonPageantWhiteout() {
    }

    public static void start() {
        remainingTicks = TOTAL_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            --remainingTicks;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        float alpha = whiteoutAlpha(event.getPartialTick());
        if (alpha <= 0.0F) {
            return;
        }
        int alphaByte = Math.min(255, Math.max(0, Math.round(alpha * 255.0F)));
        int color = alphaByte << 24 | 0x00FFFFFF;
        event.getGuiGraphics().m_280509_(
                0, 0, event.getWindow().m_85445_(), event.getWindow().m_85446_(), color);
    }

    private static float whiteoutAlpha(float partialTick) {
        if (remainingTicks <= 0) {
            return 0.0F;
        }
        float elapsed = TOTAL_TICKS - remainingTicks + partialTick;
        if (elapsed < FADE_IN_TICKS) {
            return clamp(elapsed / FADE_IN_TICKS);
        }
        if (elapsed < FADE_IN_TICKS + HOLD_TICKS) {
            return 1.0F;
        }
        return clamp((TOTAL_TICKS - elapsed) / FADE_OUT_TICKS);
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
