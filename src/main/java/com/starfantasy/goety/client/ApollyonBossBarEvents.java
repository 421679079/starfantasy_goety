package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.events.BossBarEvent;
import com.Polarice3.Goety.config.MainConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Draws Apollyon through the same textures and timing rules as Goety's Apostle bar. */
@Mod.EventBusSubscriber(
        modid = StarFantasyGoetyMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ApollyonBossBarEvents {
    private static final ResourceLocation FRAME =
            new ResourceLocation("goety", "textures/gui/boss_bar.png");
    private static final ResourceLocation HURT =
            new ResourceLocation("goety", "textures/gui/boss_bar_hurt.png");
    private static final ResourceLocation FILL =
            new ResourceLocation("goety", "textures/gui/boss_bar_1.png");

    private ApollyonBossBarEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void renderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!((Boolean) MainConfig.SpecialBossBar.get())) {
            return;
        }
        Mob mappedBoss = BossBarEvent.BOSS_BARS.get(event.getBossEvent().m_18860_());
        if (!(mappedBoss instanceof ApollyonEntity boss)) {
            return;
        }

        event.setCanceled(true);
        Minecraft minecraft = Minecraft.m_91087_();
        int screenWidth = minecraft.m_91268_().m_85445_();
        int x = screenWidth / 2 - 100;
        int y = event.getY();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        drawApostleBar(event.getGuiGraphics(), x, y, event.getPartialTick(), boss);

        Component name = boss.m_5446_();
        int nameWidth = minecraft.f_91062_.m_92852_(name);
        event.getGuiGraphics().m_280430_(minecraft.f_91062_, name,
                screenWidth / 2 - nameWidth / 2, y - 9, 0xFFFFFF);
        if (y < minecraft.m_91268_().m_85446_() / 3) {
            event.setIncrement(12 + minecraft.f_91062_.f_92710_);
        }
    }

    private static void drawApostleBar(GuiGraphics graphics, int x, int y,
                                       float partialTick, ApollyonEntity boss) {
        float percent = boss.m_21223_() / Math.max(1.0F, boss.m_21233_());
        int fillWidth = (int) (percent * 182.0F);
        int fillX = x + 9;
        int fillY = y + 4;
        int offset = (int) (((float) boss.f_19797_ + partialTick) % 364.0F);
        if (percent <= 0.25F) {
            offset = (int) (((float) boss.f_19797_ + partialTick) * 4.0F % 364.0F);
        } else if (percent <= 0.5F) {
            offset = (int) (((float) boss.f_19797_ + partialTick) * 2.0F % 364.0F);
        }

        if (fillWidth > 0) {
            graphics.m_280163_(FILL, fillX, fillY, offset, 0.0F,
                    fillWidth, 8, 364, 64);
            if (boss.f_20916_ >= 5) {
                int shake = boss.m_217043_().m_188503_(boss.f_20916_);
                int damage = boss.m_217043_().m_188503_(boss.f_20916_);
                RenderSystem.setShaderTexture(0, HURT);
                graphics.m_280163_(HURT, fillX, fillY, shake, damage,
                        fillWidth, 8, 256, 256);
            }
            if (boss.isSmited()) {
                float recovery = 1.0F - (float) boss.getAntiRegen()
                        / (float) Math.max(1, boss.getAntiRegenTotal());
                graphics.m_280163_(FILL, fillX, fillY, offset, 16.0F,
                        fillWidth, 8, 364, 64);
                graphics.m_280163_(FILL, fillX, fillY, offset, 0.0F,
                        (int) (recovery * fillWidth), 8, 364, 64);
            }
        }
        graphics.m_280163_(FRAME, x, y, 0.0F, 0.0F,
                200, 16, 256, 256);
    }
}
