package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Doki Apostle's monolith-powered purple energy layer, applied to Apollyon. */
public final class ApollyonMonolithAuraLayer extends GeoRenderLayer<ApollyonEntity> {
    private static final ResourceLocation AURA =
            new ResourceLocation("goety", "textures/entity/cultist/apostle_aura.png");

    public ApollyonMonolithAuraLayer(ApollyonGeoRenderer renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            ApollyonEntity animatable,
            BakedGeoModel bakedModel,
            RenderType baseRenderType,
            MultiBufferSource bufferSource,
            VertexConsumer baseBuffer,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        if (!animatable.isMonolithPower() || animatable.m_21224_()) {
            return;
        }

        float age = animatable.f_19797_ + partialTick;
        float xOffset = (float) Math.cos(age * 0.02F) * 3.0F % 1.0F;
        float yOffset = age * 0.01F % 1.0F;
        RenderType auraRenderType = RenderType.m_110436_(AURA, xOffset, yOffset);
        VertexConsumer auraBuffer = bufferSource.m_6299_(auraRenderType);

        this.getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                animatable,
                auraRenderType,
                auraBuffer,
                partialTick,
                packedLight,
                packedOverlay,
                0.5F,
                0.5F,
                0.5F,
                1.0F);
    }
}
