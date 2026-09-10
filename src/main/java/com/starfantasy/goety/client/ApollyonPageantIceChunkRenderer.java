package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.IceChunkRenderer;
import com.Polarice3.Goety.common.entities.projectiles.IceChunk;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Two-times Goety IceChunk model while preserving its rotating formation render. */
public final class ApollyonPageantIceChunkRenderer extends IceChunkRenderer {
    public ApollyonPageantIceChunkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 8.0F;
    }

    @Override
    public void m_7392_(IceChunk entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.m_85836_();
        poseStack.m_85841_(2.0F, 2.0F, 2.0F);
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
        poseStack.m_85849_();
    }
}
