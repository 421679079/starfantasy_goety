package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class ApostleMonolithAuraLayer extends GeoRenderLayer<ApostleGeoAnimatable> {
    private static final ResourceLocation AURA =
            ResourceLocation.fromNamespaceAndPath(
                    "goety", "textures/entity/cultist/apostle_aura.png");

    private final ApostleGeoRenderer apostleRenderer;

    public ApostleMonolithAuraLayer(ApostleGeoRenderer renderer) {
        super(renderer);
        this.apostleRenderer = renderer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            ApostleGeoAnimatable animatable,
            BakedGeoModel bakedModel,
            RenderType baseRenderType,
            MultiBufferSource bufferSource,
            VertexConsumer baseBuffer,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        Apostle apostle = this.apostleRenderer.getCurrentEntity();
        if (apostle == null || !apostle.isMonolithPower() || apostle.isDeadOrDying()) {
            return;
        }

        float age = apostle.tickCount + partialTick;
        float xOffset = (float) Math.cos(age * 0.02F) * 3.0F % 1.0F;
        float yOffset = age * 0.01F % 1.0F;
        RenderType auraRenderType = RenderType.energySwirl(AURA, xOffset, yOffset);
        VertexConsumer auraBuffer = bufferSource.getBuffer(auraRenderType);

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
