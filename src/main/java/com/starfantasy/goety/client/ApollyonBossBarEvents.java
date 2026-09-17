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
import org.lwjgl.opengl.GL11;

import java.util.Map;
import java.util.WeakHashMap;

/** Custom Apollyon sprites with Goety's Apostle scrolling, hurt and Smite effects. */
@Mod.EventBusSubscriber(
        modid = StarFantasyGoetyMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ApollyonBossBarEvents {
    private static final ResourceLocation FRAME =
            new ResourceLocation(StarFantasyGoetyMod.MODID, "textures/gui/apollyon_boss_bar.png");
    private static final ResourceLocation HURT =
            new ResourceLocation("goety", "textures/gui/boss_bar_hurt.png");
    private static final ResourceLocation FILL =
            new ResourceLocation(StarFantasyGoetyMod.MODID, "textures/gui/apollyon_boss_bar_fill.png");
    private static final ResourceLocation SMITE =
            new ResourceLocation("goety", "textures/gui/boss_bar_1.png");
    private static final double PHASE_FADE_TICKS = 10.0D;
    private static final int TEXTURE_SCALE = 4;
    private static final Map<ApollyonEntity, PhaseTransition> PHASE_TRANSITIONS = new WeakHashMap<>();

    private static final class PhaseTransition {
        private float from;
        private float target;
        private double started;

        private PhaseTransition(boolean secondPhase) {
            this.from = this.target = secondPhase ? 1.0F : 0.0F;
        }

        private float value(double now) {
            float progress = (float) Math.max(0.0D, Math.min(1.0D, (now - this.started) / PHASE_FADE_TICKS));
            return this.from + (this.target - this.from) * progress;
        }

        private float update(boolean secondPhase, double now) {
            float next = secondPhase ? 1.0F : 0.0F;
            if (next != this.target) {
                this.from = this.value(now);
                this.target = next;
                this.started = now;
            }
            return this.value(now);
        }
    }

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
            // The eye is inside the bar; only enlarged masks need extra clearance.
            event.setIncrement(19 + minecraft.f_91062_.f_92710_);
        }
    }

    private static void drawApostleBar(GuiGraphics graphics, int x, int y,
                                       float partialTick, ApollyonEntity boss) {
        // The combat-phase entity data is synced; pageant state does not select a skin.
        boolean secondPhase = boss.isCombatPhaseTwo();
        float blend = PHASE_TRANSITIONS.computeIfAbsent(boss, ignored -> new PhaseTransition(secondPhase))
                .update(secondPhase, (double) boss.f_19797_ + partialTick);
        // The liquid has real alpha even when no phase transition is playing.
        boolean wasBlending = GL11.glIsEnabled(GL11.GL_BLEND);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            if (blend <= 0.0F || blend >= 1.0F) {
                drawBarLayer(graphics, x, y, partialTick, boss, blend >= 1.0F);
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F - blend);
                drawBarLayer(graphics, x, y, partialTick, boss, false);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blend);
                drawBarLayer(graphics, x, y, partialTick, boss, true);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (blend > 0.0F) {
                ApollyonBossBarGlow.render(graphics, x, y,
                        (double) boss.f_19797_ + partialTick, blend);
            }
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            if (wasBlending) RenderSystem.enableBlend();
            else RenderSystem.disableBlend();
        }
    }

    private static void drawBarLayer(GuiGraphics graphics, int x, int y,
                                    float partialTick, ApollyonEntity boss, boolean secondPhase) {
        float percent = Math.max(0.0F, Math.min(1.0F,
                boss.m_21223_() / Math.max(1.0F, boss.m_21233_())));
        int fillWidth = (int) (percent * 182.0F);
        int fillX = x + 9;
        int fillY = y + 4;
        float fillV = secondPhase ? 8.0F : 0.0F;
        int offset = (int) (((float) boss.f_19797_ + partialTick) % 364.0F);
        if (percent <= 0.25F) {
            offset = (int) (((float) boss.f_19797_ + partialTick) * 4.0F % 364.0F);
        } else if (percent <= 0.5F) {
            offset = (int) (((float) boss.f_19797_ + partialTick) * 2.0F % 364.0F);
        }

        if (fillWidth > 0) {
            drawFill(graphics, fillX, fillY, offset, fillV, fillWidth);
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
                int recoveredWidth = (int) (Math.max(0.0F, Math.min(1.0F, recovery)) * fillWidth);
                // Reveal from the left just like Goety. Do not leave gold under
                // the recovered liquid: its new alpha would make it shine through.
                graphics.m_280163_(SMITE, fillX + recoveredWidth, fillY,
                        offset + recoveredWidth, 16.0F, fillWidth - recoveredWidth, 8, 364, 64);
            }
        }
        graphics.m_280411_(FRAME, x - 2, y - 4, 204, 24,
                0.0F, secondPhase ? 96.0F : 0.0F, 816, 96, 816, 192);
    }

    private static void drawFill(GuiGraphics graphics, int x, int y,
                                 float u, float v, int width) {
        graphics.m_280411_(FILL, x, y, width, 8, u * TEXTURE_SCALE, v * TEXTURE_SCALE,
                width * TEXTURE_SCALE, 8 * TEXTURE_SCALE, 364 * TEXTURE_SCALE, 16 * TEXTURE_SCALE);
    }
}
