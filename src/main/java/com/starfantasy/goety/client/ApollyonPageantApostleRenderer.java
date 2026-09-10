package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.model.CultistModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ApollyonPageantApostleRenderer
        extends GeoEntityRenderer<ApollyonPageantApostleEntity> {
    public ApollyonPageantApostleRenderer(EntityRendererProvider.Context context) {
        super(context, new ApollyonPageantApostleModel(
                new CultistModel<>(context.m_174023_(ModModelLayer.APOSTLE))));
        this.addRenderLayer(new ApollyonPageantMonolithAuraLayer(this));
        this.addRenderLayer(new ApollyonPageantHeldItemLayer(this, context.m_234598_()));
        this.f_114477_ = 0.8F;
    }

    @Override
    protected void applyRotations(
            ApollyonPageantApostleEntity entity, PoseStack poseStack,
            float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(
                entity, poseStack, ageInTicks,
                entity.interpolatedPageantBodyYaw(partialTick), partialTick);
    }
}
