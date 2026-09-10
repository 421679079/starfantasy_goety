package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.library.vfx.client.StarFantasyDeferredWorldRenderer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Draws Apollyon's circular home arena without creating a separate wall entity. */
final class ApollyonArenaBoundaryRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            StarFantasyGoetyMod.MODID, "textures/effect/apollyon_force_field_ring.png");
    private static final int RING_COUNT = 5;
    private static final float RING_CYCLE_TICKS = 60.0F;
    private static final float RING_MAX_HEIGHT = 2.0F;
    // The source ring core is at 87.89% of the texture's half-width.
    private static final float RING_QUAD_HALF_SIZE =
            (float) (ApollyonEntity.ARENA_RADIUS / 0.87890625D);

    void render(ApollyonEntity entity, float partialTick,
                PoseStack poseStack, MultiBufferSource buffer) {
        if (!entity.isArenaActive()
                || StarFantasyShaderCompat.isRenderingShaderShadowPass()) {
            return;
        }

        Vec3 renderPosition = new Vec3(
                Mth.m_14139_(partialTick, entity.f_19854_, entity.m_20185_()),
                Mth.m_14139_(partialTick, entity.f_19855_, entity.m_20186_()),
                Mth.m_14139_(partialTick, entity.f_19856_, entity.m_20189_()));
        Vec3 offset = entity.arenaHomePosition().m_82546_(renderPosition);
        boolean lethal = entity.isPageantBoundaryLethal();
        float age = entity.f_19797_ + partialTick;

        if (StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(poseStack, (restored, deferredBuffer) ->
                    renderImmediate(restored, deferredBuffer, offset, lethal, age));
            return;
        }
        renderImmediate(poseStack, buffer, offset, lethal, age);
    }

    private static void renderImmediate(
            PoseStack poseStack, MultiBufferSource buffer, Vec3 offset,
            boolean lethal, float age) {
        VertexConsumer consumer = buffer.m_6299_(
                ApollyonArenaBoundaryRenderType.get(TEXTURE));

        poseStack.m_85836_();
        poseStack.m_85837_(offset.f_82479_, offset.f_82480_, offset.f_82481_);
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        Matrix3f normal = poseStack.m_85850_().m_252943_();
        for (int ring = 0; ring < RING_COUNT; ++ring) {
            float ringAge = (age + ring * RING_CYCLE_TICKS / RING_COUNT)
                    % RING_CYCLE_TICKS;
            renderRing(consumer, matrix, normal,
                    ringAge / RING_CYCLE_TICKS, lethal);
        }
        poseStack.m_85849_();
    }

    private static void renderRing(
            VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
            float progress, boolean lethal) {
        float fadeIn = smoothstep(0.0F, 0.08F, progress);
        float fadeOut = 1.0F - smoothstep(0.58F, 1.0F, progress);
        float alpha = (lethal ? 0.88F : 0.68F) * fadeIn * fadeOut;
        if (alpha <= 0.001F) {
            return;
        }

        float height = 0.035F + progress * RING_MAX_HEIGHT;
        float half = RING_QUAD_HALF_SIZE;
        float red = lethal ? 1.0F : 0.22F;
        float green = lethal ? 0.12F : 0.68F;
        float blue = lethal ? 0.08F : 1.0F;
        vertex(consumer, matrix, normal, -half, height, -half,
                0.0F, 0.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, -half, height, half,
                0.0F, 1.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, half, height, half,
                1.0F, 1.0F, red, green, blue, alpha);
        vertex(consumer, matrix, normal, half, height, -half,
                1.0F, 0.0F, red, green, blue, alpha);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Mth.m_14036_((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               Matrix3f normal,
                               float x, float y, float z, float u, float v,
                               float red, float green, float blue, float alpha) {
        consumer.vertex(matrix,
                        x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

}
