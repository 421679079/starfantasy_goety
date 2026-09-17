package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.HadesEntity;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Denia-style death rays, recolored and scaled for Apollyon and Hades. */
final class ApollyonDeathLight {
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0D) / 2.0D);
    private static final int APOLLYON_MAX_BEAMS = 10;
    private static final int HADES_MAX_BEAMS = 15;
    private static final int[] APOLLYON_CORE = {255, 235, 235};
    private static final int[] APOLLYON_INNER = {255, 80, 80};
    private static final int[] APOLLYON_MID = {210, 20, 35};
    private static final int[] APOLLYON_OUTER = {80, 0, 5};
    private static final int[] HADES_CORE = {255, 235, 255};
    private static final int[] HADES_INNER = {190, 75, 255};
    private static final int[] HADES_MID = {105, 20, 220};
    private static final int[] HADES_OUTER = {35, 0, 80};

    private ApollyonDeathLight() {
    }

    static void renderApollyon(ApollyonEntity entity, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer) {
        if (entity.isPlayingDeathAnimation()) {
            renderApollyon(entity.getDeathAnimationTicks(), partialTick, poseStack, buffer);
        }
    }

    static void renderApollyon(int deathAge, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer) {
        render(deathAge, ApollyonEntity.DEATH_ANIMATION_TICKS,
                partialTick, poseStack, buffer, 1.2D, 0.85F, APOLLYON_MAX_BEAMS, 731L,
                APOLLYON_CORE, APOLLYON_INNER, APOLLYON_MID, APOLLYON_OUTER);
    }

    static void renderHades(HadesEntity entity, float partialTick,
                            PoseStack poseStack, MultiBufferSource buffer) {
        if (entity.isPlayingDeathAnimation()) {
            render(entity.getDeathAnimationTicks(), entity.getDeathAnimationDurationTicks(),
                    partialTick, poseStack, buffer, 12.0D, 2.0F, HADES_MAX_BEAMS, 1163L,
                    HADES_CORE, HADES_INNER, HADES_MID, HADES_OUTER);
        }
    }

    static void renderHadesServant(com.starfantasy.goety.entity.HadesServantEntity entity,
                                   float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        if (entity.deathAge() > 0) {
            render(entity.deathAge(), HadesEntity.DEATH_ANIMATION_TICKS, partialTick,
                    poseStack, buffer, 6.0D, 1.0F, HADES_MAX_BEAMS, 1163L,
                    HADES_CORE, HADES_INNER, HADES_MID, HADES_OUTER);
        }
    }

    private static void render(
            int deathTicks, int durationTicks, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, double yOffset,
            float scale, int maxBeams, long seed,
            int[] core, int[] inner, int[] mid, int[] outer) {
        if (deathTicks <= 0) {
            return;
        }
        float progress = Mth.m_14036_(
                (deathTicks + partialTick) / Math.max(1.0F, durationTicks),
                0.0F, 1.0F);
        float fadeOut = Math.min(
                progress > 0.8F ? (progress - 0.8F) / 0.2F : 0.0F, 1.0F);
        Random random = new Random(seed);
        VertexConsumer consumer = buffer.m_6299_(ApollyonDeathLightRenderType.get());
        poseStack.m_85836_();
        poseStack.m_85837_(0.0D, yOffset, 0.0D);
        poseStack.m_85841_(scale, scale, scale);

        int beams = Mth.m_14167_((progress + progress * progress) * (maxBeams * 0.5F));
        for (int i = 0; i < beams; ++i) {
            poseStack.m_252781_(Axis.f_252529_.m_252977_(random.nextFloat() * 360.0F));
            poseStack.m_252781_(Axis.f_252436_.m_252977_(random.nextFloat() * 360.0F));
            poseStack.m_252781_(Axis.f_252403_.m_252977_(random.nextFloat() * 360.0F));
            poseStack.m_252781_(Axis.f_252529_.m_252977_(random.nextFloat() * 360.0F));
            poseStack.m_252781_(Axis.f_252436_.m_252977_(random.nextFloat() * 360.0F));
            poseStack.m_252781_(Axis.f_252403_.m_252977_(
                    random.nextFloat() * 360.0F + progress * 90.0F));
            float length = random.nextFloat() * 10.0F + 4.0F + fadeOut * 8.0F;
            float width = random.nextFloat() * 1.2F + 0.65F + fadeOut * 1.5F;
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            int alpha = (int) (255.0F * (1.0F - fadeOut));
            renderCore(consumer, matrix, alpha, core);
            renderLeft(consumer, matrix, length, width, inner);
            renderRight(consumer, matrix, length, width, mid);
            renderCore(consumer, matrix, alpha, core);
            renderRight(consumer, matrix, length, width, mid);
            renderBack(consumer, matrix, length, width, outer);
            renderCore(consumer, matrix, alpha, core);
            renderBack(consumer, matrix, length, width, outer);
            renderLeft(consumer, matrix, length, width, inner);
        }
        poseStack.m_85849_();
    }

    private static void renderCore(
            VertexConsumer consumer, Matrix4f matrix, int alpha, int[] rgb) {
        consumer.m_252986_(matrix, 0.0F, 0.0F, 0.0F)
                .m_6122_(rgb[0], rgb[1], rgb[2], alpha).m_5752_();
    }

    private static void renderLeft(
            VertexConsumer consumer, Matrix4f matrix, float y, float x, int[] rgb) {
        consumer.m_252986_(matrix, -HALF_SQRT_3 * x, y, -0.5F * x)
                .m_6122_(rgb[0], rgb[1], rgb[2], 0).m_5752_();
    }

    private static void renderRight(
            VertexConsumer consumer, Matrix4f matrix, float y, float x, int[] rgb) {
        consumer.m_252986_(matrix, HALF_SQRT_3 * x, y, -0.5F * x)
                .m_6122_(rgb[0], rgb[1], rgb[2], 0).m_5752_();
    }

    private static void renderBack(
            VertexConsumer consumer, Matrix4f matrix, float y, float z, int[] rgb) {
        consumer.m_252986_(matrix, 0.0F, y, z)
                .m_6122_(rgb[0], rgb[1], rgb[2], 0).m_5752_();
    }
}
