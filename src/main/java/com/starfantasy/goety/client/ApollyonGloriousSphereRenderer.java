package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonGloriousSphereEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Renders Glorious's ten-tick, translucent yellow expanding sphere. */
public final class ApollyonGloriousSphereRenderer
        extends EntityRenderer<ApollyonGloriousSphereEntity> {
    private static final ResourceLocation WHITE =
            new ResourceLocation("minecraft", "textures/block/white_concrete.png");
    private static final int RINGS = 12;
    private static final int SEGMENTS = 32;

    public ApollyonGloriousSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0F; // Visual-only effect: no vanilla ground shadow.
    }

    @Override
    public void m_7392_(ApollyonGloriousSphereEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float progress = Mth.m_14036_(
                entity.visualAge(partialTick) / ApollyonGloriousSphereEntity.LIFETIME_TICKS,
                0.0F, 1.0F);
        if (progress < 1.0F) {
            float radius = entity.maxRadius() * progress;
            float alpha = 0.3F * (1.0F - Mth.m_14036_((progress - 0.7F) / 0.3F,
                    0.0F, 1.0F));
            VertexConsumer consumer = buffer.m_6299_(
                    StarFantasyVfxRenderTypes.translucentPositionColor());
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            if (entity.isRed()) {
                renderSphere(consumer, matrix, radius, 1.0F, 0.06F, 0.06F, alpha);
            } else {
                renderSphere(consumer, matrix, radius, 1.0F, 0.82F, 0.12F, alpha);
            }
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonGloriousSphereEntity entity) {
        return WHITE;
    }

    static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float radius,
                             float red, float green, float blue, float alpha) {
        if (radius <= 0.01F || alpha <= 0.01F) {
            return;
        }
        for (int ring = 0; ring < RINGS; ++ring) {
            float theta0 = -(float) Math.PI * 0.5F + (float) Math.PI * ring / RINGS;
            float theta1 = -(float) Math.PI * 0.5F
                    + (float) Math.PI * (ring + 1) / RINGS;
            for (int segment = 0; segment < SEGMENTS; ++segment) {
                float phi0 = (float) Math.PI * 2.0F * segment / SEGMENTS;
                float phi1 = (float) Math.PI * 2.0F * (segment + 1) / SEGMENTS;
                vertex(consumer, matrix, theta0, phi0, radius, red, green, blue, alpha);
                vertex(consumer, matrix, theta0, phi1, radius, red, green, blue, alpha);
                vertex(consumer, matrix, theta1, phi1, radius, red, green, blue, alpha);
                vertex(consumer, matrix, theta1, phi0, radius, red, green, blue, alpha);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float theta, float phi, float radius,
                               float red, float green, float blue, float alpha) {
        float cosTheta = Mth.m_14089_(theta);
        float x = cosTheta * Mth.m_14089_(phi) * radius;
        float y = Mth.m_14031_(theta) * radius;
        float z = cosTheta * Mth.m_14031_(phi) * radius;
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(red, green, blue, alpha)
                .m_5752_();
    }
}
