package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.library.registry.StarFantasyLibrarySoundRegistry;
import com.starfantasy.library.vfx.ScreenBurstOptions;
import com.starfantasy.library.vfx.client.ScreenEffectHandler;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.multiplayer.ClientLevel;
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
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS;

    private static int remainingTicks;
    private static ClientLevel ownerLevel;

    private ApollyonPageantWhiteout() {
    }

    public static void start() {
        ownerLevel = Minecraft.m_91087_().f_91073_;
        remainingTicks = TOTAL_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && remainingTicks > 0) {
            Minecraft minecraft = Minecraft.m_91087_();
            if (ownerLevel == null || minecraft.f_91073_ != ownerLevel) {
                remainingTicks = 0;
                ownerLevel = null;
                return;
            }
            if (--remainingTicks == 0) {
                ScreenEffectHandler.burstSolidColor(UUID.randomUUID(), new ScreenBurstOptions(0, true, -1), 0xFFFFFF);
                minecraft.m_91106_().m_120367_(SimpleSoundInstance.m_119755_(StarFantasyLibrarySoundRegistry.IMAGE_SHATTER.get(), 1.0F, 1.0F));
                ownerLevel = null;
            }
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
        return 0.0F;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
