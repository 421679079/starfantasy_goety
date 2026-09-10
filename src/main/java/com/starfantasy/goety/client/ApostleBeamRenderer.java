package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApostleBeamEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Three-layer corruption-focus-style beam attached to a pageant Apostle. */
public final class ApostleBeamRenderer
        extends EntityRenderer<ApostleBeamEntity> {
    private static final ResourceLocation BEAM_GLOW =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_glow.png");
    private static final ResourceLocation BEAM_MAIN =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_main.png");
    private static final ResourceLocation BEAM_CORE =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_core.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public ApostleBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = ApostleBeamEntity.LENGTH;
    }

    @Override
    public void m_7392_(ApostleBeamEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!entity.active()) return;
        yaw = entity.visualYaw(partialTick);
        poseStack.m_85836_();
        poseStack.m_85837_(0, 1.35, 0);
        float age = entity.visualAge(partialTick);
        float fadeIn = Mth.m_14036_(age / 3.0F, 0.0F, 1.0F);
        float fadeOut = Mth.m_14036_((entity.duration() - age) / 10.0F, 0.0F, 1.0F);
        float alpha = Math.min(fadeIn, fadeOut);
        if (alpha > 0.01F) {
            float scroll = -(entity.m_9236_().m_46467_() + age) * 0.08F;
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            drawLayer(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_GLOW)),
                    matrix, yaw, 0.72F, scroll, alpha * 0.55F);
            drawLayer(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_MAIN)),
                    matrix, yaw, 0.46F, scroll * 1.35F, alpha * 0.9F);
            drawLayer(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_CORE)),
                    matrix, yaw, 0.2F, scroll * 1.8F, alpha);
        }
        poseStack.m_85849_();
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApostleBeamEntity entity) {
        return BEAM_MAIN;
    }

    private static void drawLayer(VertexConsumer consumer, Matrix4f matrix, float yaw,
                                  float width, float scroll, float alpha) {
        float radians = yaw * ((float) Math.PI / 180.0F);
        float forwardX = -Mth.m_14031_(radians);
        float forwardZ = Mth.m_14089_(radians);
        float sideX = Mth.m_14089_(radians) * width;
        float sideZ = Mth.m_14031_(radians) * width;
        float endX = forwardX * ApostleBeamEntity.LENGTH;
        float endZ = forwardZ * ApostleBeamEntity.LENGTH;
        float endV = scroll + ApostleBeamEntity.LENGTH * 0.28F;

        quad(consumer, matrix,
                -sideX, 0.0F, -sideZ,
                sideX, 0.0F, sideZ,
                endX + sideX, 0.0F, endZ + sideZ,
                endX - sideX, 0.0F, endZ - sideZ,
                scroll, endV, alpha);
        quad(consumer, matrix,
                0.0F, -width, 0.0F,
                0.0F, width, 0.0F,
                endX, width, endZ,
                endX, -width, endZ,
                scroll, endV, alpha);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float startV, float endV, float alpha) {
        vertex(consumer, matrix, x0, y0, z0, 0.0F, startV, alpha);
        vertex(consumer, matrix, x1, y1, z1, 1.0F, startV, alpha);
        vertex(consumer, matrix, x2, y2, z2, 1.0F, endV, alpha);
        vertex(consumer, matrix, x3, y3, z3, 0.0F, endV, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float x, float y, float z, float u, float v, float alpha) {
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(1.0F, 1.0F, 1.0F, alpha)
                .m_7421_(u, v)
                .m_85969_(FULL_BRIGHT)
                .m_5752_();
    }
}
