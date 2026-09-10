package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.ApollyonCastingLightningEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Hand-bound copy of the animated energy core at the center of the library Slam effect. */
public final class ApollyonCastingLightningRenderer
        extends EntityRenderer<ApollyonCastingLightningEntity> {
    private static final ResourceLocation ENERGY_CORE_TEXTURE =
            new ResourceLocation("star_fantasy_library", "textures/particle/slam/lightning_ball_core.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float CORE_SIZE = 1.05F;

    public ApollyonCastingLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(ApollyonCastingLightningEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.getVisualAge() + partialTick;
        float duration = Math.max(1.0F, entity.getDuration());
        float progress = Mth.m_14036_(age / duration, 0.0F, 1.0F);
        float alpha = Mth.m_14036_(age / 1.5F, 0.0F, 1.0F);
        if (alpha > 0.0F && progress < 1.0F) {
            float pop = 0.7F + 0.3F * (1.0F - (float) Math.pow(1.0F - progress, 2.0D));
            float pulse = 1.0F + (float) Math.sin(
                    age * 1.7F + entity.m_19879_() * 0.31F) * 0.08F;
            float size = CORE_SIZE * pop * pulse;

            VertexConsumer consumer = buffer.m_6299_(
                    StarFantasyVfxRenderTypes.particle(ENERGY_CORE_TEXTURE));
            poseStack.m_85836_();
            poseStack.m_252781_(Minecraft.m_91087_().f_91063_.m_109153_().m_253121_());

            poseStack.m_85836_();
            poseStack.m_252781_(Axis.f_252403_.m_252977_(
                    age * 28.0F + entity.m_19879_() * 17.0F));
            renderBillboardQuad(poseStack, consumer, size * 1.15F,
                    0.42F, 0.82F, 1.0F, alpha * 0.5F);
            poseStack.m_85849_();

            poseStack.m_85836_();
            poseStack.m_252781_(Axis.f_252403_.m_252977_(
                    -age * 43.0F + entity.m_19879_() * 11.0F));
            renderBillboardQuad(poseStack, consumer, size * 0.78F,
                    1.0F, 1.0F, 1.0F, alpha);
            poseStack.m_85849_();

            poseStack.m_85836_();
            poseStack.m_252781_(Axis.f_252403_.m_252977_(
                    age * 67.0F + entity.m_19879_() * 5.0F));
            renderBillboardQuad(poseStack, consumer, size * 1.52F,
                    0.18F, 0.58F, 1.0F, alpha * 0.3F);
            poseStack.m_85849_();

            poseStack.m_85849_();
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonCastingLightningEntity entity) {
        return ENERGY_CORE_TEXTURE;
    }

    private static void renderBillboardQuad(PoseStack poseStack, VertexConsumer consumer,
                                            float halfSize, float red, float green,
                                            float blue, float alpha) {
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        vertex(consumer, matrix, -halfSize, -halfSize, 0.0F, 1.0F,
                red, green, blue, alpha);
        vertex(consumer, matrix, halfSize, -halfSize, 1.0F, 1.0F,
                red, green, blue, alpha);
        vertex(consumer, matrix, halfSize, halfSize, 1.0F, 0.0F,
                red, green, blue, alpha);
        vertex(consumer, matrix, -halfSize, halfSize, 0.0F, 0.0F,
                red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float x, float y, float u, float v,
                               float red, float green, float blue, float alpha) {
        consumer.m_252986_(matrix, x, y, 0.0F)
                .m_85950_(red, green, blue, alpha)
                .m_7421_(u, v)
                .m_85969_(FULL_BRIGHT)
                .m_5752_();
    }
}
