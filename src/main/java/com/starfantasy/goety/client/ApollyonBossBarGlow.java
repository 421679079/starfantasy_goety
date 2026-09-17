package com.starfantasy.goety.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/** Nine rotating GUI rays, with no particle entities, textures or network traffic. */
final class ApollyonBossBarGlow {
    private ApollyonBossBarGlow() {
    }

    static void render(GuiGraphics graphics, int x, int y, double age, float strength) {
        if (strength <= 0.0F) return;
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        ShaderInstance previousShader = RenderSystem.getShader();
        try {
            // Finish GUI batches before using the shared immediate buffer; flush
            // may change depth/blend state, so capture those states before it.
            graphics.m_280262_();
            // GUI glow must not be occluded by prior HUD depth writes. Additive
            // blending brightens the sprite instead of tinting it with a dim veil.
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE,
                    GL11.GL_ZERO, GL11.GL_ONE);
            RenderSystem.setShader(GameRenderer::m_172811_);
            BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
            buffer.m_166779_(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
            Matrix4f matrix = graphics.m_280168_().m_85850_().m_252922_();
            emitRays(buffer, matrix, x, y, age, Math.min(1.0F, strength));
            BufferUploader.m_231202_(buffer.m_231175_());
        } finally {
            RenderSystem.setShader(() -> previousShader);
            RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
            RenderSystem.depthMask(depthWrite);
            if (depth) RenderSystem.enableDepthTest();
            if (cull) RenderSystem.enableCull();
            if (!blending) RenderSystem.disableBlend();
        }
    }

    private static void emitRays(BufferBuilder buffer, Matrix4f matrix,
                                  int x, int y, double age, float strength) {
        for (int anchor = 0; anchor < 3; anchor++) {
            boolean eye = anchor == 0;
            float centerX = x + (eye ? 100.0F : anchor == 1 ? 10.0F : 190.0F);
            float centerY = y + (eye ? 8.0F : 8.5F);
            int red = eye ? 255 : 190;
            int green = eye ? 35 : 55;
            int blue = eye ? 50 : 255;
            for (int ray = 0; ray < 3; ray++) {
                // About nine seconds per revolution, with different gem phases.
                double angle = age * (anchor == 2 ? -0.035D : 0.035D)
                        + anchor * 1.7D + ray * (Math.PI * 2.0D / 3.0D);
                float pulse = 0.82F + 0.18F * (float) Math.sin(age * 0.047D + anchor + ray);
                // Scale both ray dimensions: eye 2x, side gems 1.5x.
                float length = (eye ? 21.0F : 11.7F) * (0.90F + 0.10F * pulse);
                float halfWidth = eye ? 2.9F : 1.65F;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);
                float tipX = centerX + cos * length;
                float tipY = centerY + sin * length;
                float shoulderX = centerX + cos * length * 0.28F;
                float shoulderY = centerY + sin * length * 0.28F;
                int alpha = (int) (220.0F * pulse * strength);
                // A bright narrow base and transparent pointed tip form a ray,
                // rather than a barely-visible triangle fading across its width.
                vertex(buffer, matrix, centerX, centerY, red, green, blue, alpha);
                vertex(buffer, matrix, shoulderX - sin * halfWidth, shoulderY + cos * halfWidth,
                        red, green, blue, alpha / 2);
                vertex(buffer, matrix, tipX, tipY, red, green, blue, 0);
                vertex(buffer, matrix, centerX, centerY, red, green, blue, alpha);
                vertex(buffer, matrix, tipX, tipY, red, green, blue, 0);
                vertex(buffer, matrix, shoulderX + sin * halfWidth, shoulderY - cos * halfWidth,
                        red, green, blue, alpha / 2);
            }
        }
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y,
                               int red, int green, int blue, int alpha) {
        buffer.m_252986_(matrix, x, y, 0.1F).m_6122_(red, green, blue, alpha).m_5752_();
    }
}
