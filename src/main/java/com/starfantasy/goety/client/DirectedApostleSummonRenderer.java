package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.DirectedApostleSummonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Visual-equivalent renderer for Goety's original Apostle summoning circle. */
public final class DirectedApostleSummonRenderer
        extends EntityRenderer<DirectedApostleSummonEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("goety", "textures/entity/cultist/summon_apostle.png");
    private static final ResourceLocation RING =
            new ResourceLocation("goety", "textures/entity/cultist/summon_apostle_ring.png");

    public DirectedApostleSummonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(DirectedApostleSummonEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.f_19797_ + partialTick;
        float size = age >= 440.0F
                ? Math.max(0.0F, (450.0F - age) / 10.0F * 3.0F)
                : Mth.m_14036_(age * 0.1F, 0.0F, 3.0F);

        poseStack.m_85836_();
        VertexConsumer baseConsumer = buffer.m_6299_(RenderType.m_110458_(TEXTURE));
        poseStack.m_252880_(0.0F, 0.001F, 0.0F);
        poseStack.m_85841_(size, size, size);
        poseStack.m_252781_(Axis.f_252436_.m_252977_(90.0F + age));
        drawRing(poseStack.m_85850_(), baseConsumer);
        poseStack.m_85849_();

        VertexConsumer ringConsumer = buffer.m_6299_(RenderType.m_110458_(RING));
        poseStack.m_85836_();
        poseStack.m_252880_(0.0F, Mth.m_14036_(age * 0.3F, 0.001F, 2.001F), 0.0F);
        poseStack.m_85841_(size, size, size);
        poseStack.m_252781_(Axis.f_252436_.m_252977_(90.0F - age / 2.0F));
        drawRing(poseStack.m_85850_(), ringConsumer);
        poseStack.m_85849_();

        poseStack.m_85836_();
        poseStack.m_252880_(0.0F, Mth.m_14036_(age * 0.6F, 0.001F, 4.001F), 0.0F);
        poseStack.m_85841_(size, size, size);
        poseStack.m_252781_(Axis.f_252436_.m_252977_(90.0F + age / 2.0F));
        drawRing(poseStack.m_85850_(), ringConsumer);
        poseStack.m_85849_();

        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void drawRing(PoseStack.Pose pose, VertexConsumer consumer) {
        Matrix4f matrix4f = pose.m_252922_();
        Matrix3f matrix3f = pose.m_252943_();
        drawVertex(matrix4f, matrix3f, consumer, -1.0F, -1.0F, 0.0F, 0.0F);
        drawVertex(matrix4f, matrix3f, consumer, -1.0F, 1.0F, 0.0F, 1.0F);
        drawVertex(matrix4f, matrix3f, consumer, 1.0F, 1.0F, 1.0F, 1.0F);
        drawVertex(matrix4f, matrix3f, consumer, 1.0F, -1.0F, 1.0F, 0.0F);
    }

    private static void drawVertex(Matrix4f matrix4f, Matrix3f matrix3f,
                                   VertexConsumer consumer, float x, float z, float u, float v) {
        consumer.m_252986_(matrix4f, x, 0.0F, z)
                .m_6122_(255, 255, 255, 255)
                .m_7421_(u, v)
                .m_86008_(OverlayTexture.f_118083_)
                .m_85969_(0xF000F0)
                .m_252939_(matrix3f, 1.0F, 1.0F, 0.0F)
                .m_5752_();
    }

    @Override
    public ResourceLocation m_5478_(DirectedApostleSummonEntity entity) {
        return TEXTURE;
    }
}
