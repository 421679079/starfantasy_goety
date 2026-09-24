package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonCleaveEffectEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Replays the t_cleave mesh, additive material, and visible Birth keyframes. */
public final class ApollyonCleaveEffectRenderer
        extends EntityRenderer<ApollyonCleaveEffectEntity> {
    private static final float MAX_RADIUS = 20.0F;
    private static final float MODEL_TO_BLOCK = MAX_RADIUS / 223.07015F;
    private static final float SUBDIVISION_LENGTH = 0.25F;
    private static final float MASK_FEATHER = 0.2F;
    private static final float VERTICAL_SCALE = 0.2F;
    private static final float GROUND_Y = -0.03F;
    private static final float CRACK_WIDTH_SCALE = 10.0F;
    private static final float MIN_THICKENED_CRACK_HALF_WIDTH = 0.16666667F;
    private static final float BASE_COLOR = 0.7529412F;
    private static final float MIN_HEIGHT_SCALE = 0.00994873F;
    private static final int FULL_BRIGHT = 0xF000F0;

    /** Four MDX vertices per quad in top-left, bottom-left, bottom-right, top-right order. */
    private static final float[][] QUADS = {
            {30.625F, 25.375F, 175.0F, 30.625F, 25.125F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {32.75F, 72.0F, 175.0F, 32.75F, 72.0F, -0.126953F, 30.625F, 25.125F, -0.095703F, 30.625F, 25.375F, 175.0F},
            {100.0F, 132.0F, 175.0F, 100.0F, 132.0F, -0.143555F, 32.75F, 72.0F, -0.126953F, 32.75F, 72.0F, 175.0F},
            {-21.375F, 51.75F, 175.0F, -21.25F, 51.75F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {-79.0F, 48.75F, 175.0F, -79.0F, 48.5F, -0.126953F, -21.25F, 51.75F, -0.095703F, -21.375F, 51.75F, 175.0F},
            {-119.0F, 87.0F, 175.0F, -118.5F, 87.0F, -0.152344F, -79.0F, 48.5F, -0.126953F, -79.0F, 48.75F, 175.0F},
            {35.25F, -12.625F, 175.0F, 35.25F, -12.625F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {69.5F, 8.75F, 175.0F, 69.5F, 8.75F, -0.126953F, 35.25F, -12.625F, -0.095703F, 35.25F, -12.625F, 175.0F},
            {110.5F, 13.0625F, 175.0F, 110.0F, 13.0625F, -0.143555F, 69.5F, 8.75F, -0.126953F, 69.5F, 8.75F, 175.0F},
            {220.0F, -29.625F, 0.851563F, 220.0F, -29.625F, 0.851563F, 110.0F, 13.0625F, -0.143555F, 110.5F, 13.0625F, 175.0F},
            {83.0F, 171.0F, -0.166992F, 83.0F, 171.0F, -0.166992F, 100.0F, 132.0F, -0.143555F, 100.0F, 132.0F, 175.0F},
            {-177.0F, 46.75F, -0.166992F, -177.0F, 46.75F, -0.166992F, -118.5F, 87.0F, -0.152344F, -119.0F, 87.0F, 175.0F},
            {-20.625F, -47.75F, 175.0F, -20.375F, -47.75F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {-59.75F, -105.0F, 175.0F, -59.75F, -105.0F, -0.126953F, -20.375F, -47.75F, -0.095703F, -20.625F, -47.75F, 175.0F},
            {-125.0F, -100.5F, 175.0F, -125.0F, -100.5F, -0.152344F, -59.75F, -105.0F, -0.126953F, -59.75F, -105.0F, 175.0F},
            {-175.0F, -119.0F, -0.166992F, -175.0F, -119.0F, -0.166992F, -125.0F, -100.5F, -0.152344F, -125.0F, -100.5F, 175.0F},
            {13.8125F, -69.0F, 175.0F, 13.75F, -69.0F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {44.5F, -112.0F, 175.0F, 44.5F, -112.0F, -0.126953F, 13.75F, -69.0F, -0.095703F, 13.8125F, -69.0F, 175.0F},
            {106.5F, -88.5F, 175.0F, 106.5F, -88.5F, -0.152344F, 44.5F, -112.0F, -0.126953F, 44.5F, -112.0F, 175.0F},
            {105.5F, -153.0F, -0.166992F, 105.5F, -153.0F, -0.166992F, 106.5F, -88.5F, -0.152344F, 106.5F, -88.5F, 175.0F},
            {-57.25F, -7.03125F, 175.0F, -57.0F, -7.0F, -0.095703F, 1.03125F, 1.257813F, -0.001183F, 0.964844F, 1.28125F, 175.0F},
            {-108.5F, 2.4375F, 175.0F, -108.5F, 2.46875F, -0.126953F, -57.0F, -7.0F, -0.095703F, -57.25F, -7.03125F, 175.0F},
            {-136.0F, -18.625F, 175.0F, -136.0F, -18.625F, -0.152344F, -108.5F, 2.46875F, -0.126953F, -108.5F, 2.4375F, 175.0F},
            {-223.0F, -5.59375F, -0.166992F, -223.0F, -5.59375F, -0.166992F, -136.0F, -18.625F, -0.152344F, -136.0F, -18.625F, 175.0F},
            {-77.5F, -55.75F, 153.0F, -77.5F, -55.75F, 8.375F, -57.75F, -6.75F, -5.09375F, -58.0F, -6.9375F, 170.0F},
            {-130.0F, -52.25F, 153.0F, -130.0F, -52.25F, 8.375F, -77.5F, -55.75F, 8.375F, -77.5F, -55.75F, 153.0F},
            {-182.0F, -89.5F, 8.375F, -182.0F, -89.5F, 8.375F, -130.0F, -52.25F, 8.375F, -130.0F, -52.25F, 153.0F},
            {91.5F, -35.25F, 153.0F, 91.0F, -35.25F, 9.0625F, 35.75F, -13.0F, 9.125F, 36.0F, -13.0F, 153.0F},
            {98.5F, -70.0F, 153.0F, 98.5F, -70.0F, 9.0625F, 91.0F, -35.25F, 9.0625F, 91.5F, -35.25F, 153.0F},
            {144.0F, -86.5F, 9.0625F, 144.0F, -86.5F, 9.0625F, 98.5F, -70.0F, 9.0625F, 98.5F, -70.0F, 153.0F},
            {-1.804688F, -102.0F, 170.0F, -1.617188F, -102.0F, -5.125F, -19.875F, -49.0F, -5.09375F, -19.75F, -49.25F, 170.0F},
            {-4.8125F, -139.0F, 170.0F, -4.65625F, -139.0F, -5.125F, -1.617188F, -102.0F, -5.125F, -1.804688F, -102.0F, 170.0F},
            {-73.5F, -167.0F, -5.15625F, -73.5F, -167.0F, -5.15625F, -4.65625F, -139.0F, -5.125F, -4.8125F, -139.0F, 170.0F},
            {90.0F, 49.0F, 170.0F, 89.5F, 49.0F, -5.125F, 30.125F, 26.0F, -5.09375F, 30.375F, 25.875F, 170.0F},
            {129.0F, 99.5F, 170.0F, 129.0F, 100.0F, -5.125F, 89.5F, 49.0F, -5.125F, 90.0F, 49.0F, 170.0F},
            {177.0F, 62.25F, -5.15625F, 177.0F, 62.25F, -5.15625F, 129.0F, 100.0F, -5.125F, 129.0F, 99.5F, 170.0F},
            {-53.75F, 132.0F, 148.0F, -53.75F, 132.0F, 13.0F, -40.25F, 174.0F, 13.0625F, -40.25F, 174.0F, 148.0F},
            {-16.625F, 106.5F, 148.0F, -16.5F, 106.5F, 13.0F, -53.75F, 132.0F, 13.0F, -53.75F, 132.0F, 148.0F},
            {-21.75F, 51.25F, 13.0F, -21.75F, 51.25F, 13.0F, -16.5F, 106.5F, 13.0F, -16.625F, 106.5F, 148.0F}
    };

    public ApollyonCleaveEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0F;
    }

    @Override
    public void m_7392_(ApollyonCleaveEffectEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.visualAge(partialTick);
        if (age < entity.lifetimeTicks()) {
            float revealRadius = revealRadius(age);
            float heightScale = heightScale(age, entity.fadeStartTick());
            if (entity.isServantEffect()) {
                float peakScale = 30.0F / (175.0F * MODEL_TO_BLOCK * VERTICAL_SCALE);
                heightScale = MIN_HEIGHT_SCALE + (heightScale - MIN_HEIGHT_SCALE)
                        * (peakScale - MIN_HEIGHT_SCALE) / (5.0F - MIN_HEIGHT_SCALE);
            }
            float alpha = alpha(age, entity.fadeStartTick());
            float crackWidthBlend = crackWidthBlend(age);
            VertexConsumer consumer = buffer.m_6299_(ApollyonCleaveRenderType.get());
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            for (float[] quad : QUADS) {
                renderMaskedQuad(consumer, matrix, quad,
                        revealRadius, heightScale, alpha, crackWidthBlend);
            }
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonCleaveEffectEntity entity) {
        return ApollyonCleaveRenderType.texture();
    }

    private static float revealRadius(float age) {
        float progress = Mth.m_14036_(
                age / ApollyonCleaveEffectEntity.CRACK_EXPAND_TICKS, 0.0F, 1.0F);
        float eased = progress * progress * (3.0F - 2.0F * progress);
        return MAX_RADIUS * eased;
    }

    private static float heightScale(float age, int fadeStartTick) {
        if (age < ApollyonCleaveEffectEntity.BURST_START_TICK) {
            return MIN_HEIGHT_SCALE;
        }
        if (age < ApollyonCleaveEffectEntity.PULSE_START_TICK) {
            float burst = Mth.m_14036_((age - ApollyonCleaveEffectEntity.BURST_START_TICK)
                    / ApollyonCleaveEffectEntity.BURST_TICKS, 0.0F, 1.0F);
            float eased = 1.0F - (1.0F - burst) * (1.0F - burst);
            return Mth.m_14179_(eased, MIN_HEIGHT_SCALE, 5.0F);
        }
        if (age < fadeStartTick) {
            float pulseAge = age - ApollyonCleaveEffectEntity.PULSE_START_TICK;
            return 4.6F + 0.4F * Mth.m_14089_(pulseAge * ((float) Math.PI / 5.0F));
        }
        float fade = Mth.m_14036_((age - fadeStartTick)
                / ApollyonCleaveEffectEntity.FADE_TICKS, 0.0F, 1.0F);
        return Mth.m_14179_(fade, 5.0F, MIN_HEIGHT_SCALE);
    }

    private static float alpha(float age, int fadeStartTick) {
        if (age < ApollyonCleaveEffectEntity.PULSE_START_TICK) {
            return 1.0F;
        }
        if (age < fadeStartTick) {
            float pulseAge = age - ApollyonCleaveEffectEntity.PULSE_START_TICK;
            return 0.86F + 0.14F * Mth.m_14089_(pulseAge * ((float) Math.PI / 5.0F));
        }
        return 1.0F - Mth.m_14036_((age - fadeStartTick)
                / ApollyonCleaveEffectEntity.FADE_TICKS, 0.0F, 1.0F);
    }

    private static float crackWidthBlend(float age) {
        if (age <= ApollyonCleaveEffectEntity.BURST_START_TICK) {
            return 1.0F;
        }
        float burst = Mth.m_14036_((age - ApollyonCleaveEffectEntity.BURST_START_TICK)
                / ApollyonCleaveEffectEntity.BURST_TICKS, 0.0F, 1.0F);
        return 1.0F - burst;
    }

    private static void renderMaskedQuad(VertexConsumer consumer, Matrix4f matrix,
                                         float[] quad, float revealRadius,
                                         float heightScale, float alpha,
                                         float crackWidthBlend) {
        float startX = (quad[0] + quad[3]) * 0.5F;
        float startZ = (quad[1] + quad[4]) * 0.5F;
        float endX = (quad[6] + quad[9]) * 0.5F;
        float endZ = (quad[7] + quad[10]) * 0.5F;
        float length = Mth.m_14116_((startX - endX) * (startX - endX)
                + (startZ - endZ) * (startZ - endZ)) * MODEL_TO_BLOCK;
        int subdivisions = Math.max(1, Mth.m_14167_(length / SUBDIVISION_LENGTH));
        for (int slice = 0; slice < subdivisions; ++slice) {
            float t0 = slice / (float) subdivisions;
            float t1 = (slice + 1) / (float) subdivisions;
            float u0 = Mth.m_14179_(t0, 0.0005F, 0.9961F);
            float u1 = Mth.m_14179_(t1, 0.0005F, 0.9961F);
            vertex(consumer, matrix, quad, 0, 9, 3, 6, 1.0F,
                    t0, u0, 0.0005F, revealRadius, heightScale, alpha,
                    crackWidthBlend, false);
            vertex(consumer, matrix, quad, 3, 6, 0, 9, -1.0F,
                    t0, u0, 0.9961F, revealRadius, heightScale, alpha,
                    crackWidthBlend, true);
            vertex(consumer, matrix, quad, 3, 6, 0, 9, -1.0F,
                    t1, u1, 0.9961F, revealRadius, heightScale, alpha,
                    crackWidthBlend, true);
            vertex(consumer, matrix, quad, 0, 9, 3, 6, 1.0F,
                    t1, u1, 0.0005F, revealRadius, heightScale, alpha,
                    crackWidthBlend, false);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float[] quad, int startOffset, int endOffset,
                               int pairedStartOffset, int pairedEndOffset, float sideSign,
                               float progress, float u, float v, float revealRadius,
                               float heightScale, float alpha, float crackWidthBlend,
                               boolean groundEdge) {
        float modelX = Mth.m_14179_(progress,
                quad[startOffset], quad[endOffset]);
        float pairedModelX = Mth.m_14179_(progress,
                quad[pairedStartOffset], quad[pairedEndOffset]);
        float modelZ = Mth.m_14179_(progress,
                quad[startOffset + 1], quad[endOffset + 1]);
        float pairedModelZ = Mth.m_14179_(progress,
                quad[pairedStartOffset + 1], quad[pairedEndOffset + 1]);
        float x = modelX * MODEL_TO_BLOCK;
        float z = modelZ * MODEL_TO_BLOCK;
        if (crackWidthBlend > 0.0F) {
            float centerX = (modelX + pairedModelX) * 0.5F * MODEL_TO_BLOCK;
            float centerZ = (modelZ + pairedModelZ) * 0.5F * MODEL_TO_BLOCK;
            float startCenterX = (quad[0] + quad[3]) * 0.5F * MODEL_TO_BLOCK;
            float startCenterZ = (quad[1] + quad[4]) * 0.5F * MODEL_TO_BLOCK;
            float endCenterX = (quad[9] + quad[6]) * 0.5F * MODEL_TO_BLOCK;
            float endCenterZ = (quad[10] + quad[7]) * 0.5F * MODEL_TO_BLOCK;
            float directionX = endCenterX - startCenterX;
            float directionZ = endCenterZ - startCenterZ;
            float directionLength = Mth.m_14116_(
                    directionX * directionX + directionZ * directionZ);
            if (directionLength > 1.0E-5F) {
                float originalHalfWidth = Mth.m_14116_(
                        (modelX - pairedModelX) * (modelX - pairedModelX)
                                + (modelZ - pairedModelZ) * (modelZ - pairedModelZ))
                        * MODEL_TO_BLOCK * 0.5F;
                float thickenedHalfWidth = Math.max(
                        originalHalfWidth * CRACK_WIDTH_SCALE,
                        MIN_THICKENED_CRACK_HALF_WIDTH);
                float normalX = -directionZ / directionLength;
                float normalZ = directionX / directionLength;
                float thickenedX = centerX + normalX * sideSign * thickenedHalfWidth;
                float thickenedZ = centerZ + normalZ * sideSign * thickenedHalfWidth;
                x = Mth.m_14179_(crackWidthBlend, x, thickenedX);
                z = Mth.m_14179_(crackWidthBlend, z, thickenedZ);
            }
        }
        float modelY = Mth.m_14179_(progress,
                quad[startOffset + 2], quad[endOffset + 2]);
        float y = groundEdge ? GROUND_Y
                : GROUND_Y + Math.max(0.0F, modelY) * MODEL_TO_BLOCK
                * heightScale * VERTICAL_SCALE;
        float radialDistance = Mth.m_14116_(x * x + z * z);
        float mask = Mth.m_14036_((revealRadius + MASK_FEATHER - radialDistance)
                / MASK_FEATHER, 0.0F, 1.0F);
        mask = mask * mask * (3.0F - 2.0F * mask);
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(BASE_COLOR, BASE_COLOR, BASE_COLOR, alpha * mask)
                .m_7421_(u, v)
                .m_85969_(FULL_BRIGHT)
                .m_5752_();
    }
}
