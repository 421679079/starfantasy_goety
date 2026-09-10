package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Draws the persistent phase-two damage shield directly around Apollyon. */
final class ApollyonCooperativeShieldRenderer {
    private static final float SHIELD_RADIUS = 1.55F;

    private ApollyonCooperativeShieldRenderer() {
    }

    static void render(ApollyonEntity entity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer) {
        if (!entity.hasCooperativeShield() || entity.isPlayingDeathAnimation()) {
            return;
        }
        float pulse = 0.025F * Mth.m_14031_((entity.f_19797_ + partialTick) * 0.22F);
        float radius = SHIELD_RADIUS + pulse;
        float alpha = 0.22F + 0.035F
                * Mth.m_14031_((entity.f_19797_ + partialTick) * 0.15F);

        poseStack.m_85836_();
        poseStack.m_85837_(0.0D, 1.15D, 0.0D);
        VertexConsumer consumer = buffer.m_6299_(
                StarFantasyVfxRenderTypes.translucentPositionColor());
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        ApollyonGloriousSphereRenderer.renderSphere(
                consumer, matrix, radius, 1.0F, 0.82F, 0.12F, alpha);
        poseStack.m_85849_();
    }
}
