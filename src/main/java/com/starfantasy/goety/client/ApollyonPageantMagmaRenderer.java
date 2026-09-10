package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApollyonPageantMagmaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public final class ApollyonPageantMagmaRenderer
        extends EntityRenderer<ApollyonPageantMagmaEntity> {
    private static final ResourceLocation BLOCK_ATLAS =
            new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    private final BlockRenderDispatcher blockRenderer;

    public ApollyonPageantMagmaRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.m_234597_();
        this.f_114477_ = 1.5F;
    }

    @Override
    public void m_7392_(ApollyonPageantMagmaEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.m_85836_();
        poseStack.m_85841_(2.0F, 2.0F, 2.0F);
        poseStack.m_252880_(-0.5F, 0.0F, -0.5F);
        this.blockRenderer.m_110912_(Blocks.f_50450_.m_49966_(), poseStack, buffer,
                packedLight, OverlayTexture.f_118083_);
        poseStack.m_85849_();
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonPageantMagmaEntity entity) {
        return BLOCK_ATLAS;
    }
}
