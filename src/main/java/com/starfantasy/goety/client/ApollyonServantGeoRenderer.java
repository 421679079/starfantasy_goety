package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ApollyonServantGeoRenderer extends GeoEntityRenderer<ApollyonServantEntity> {
    public ApollyonServantGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new ApollyonServantGeoModel());
        this.addRenderLayer(new ApollyonServantAuraLayer(this));
        this.addRenderLayer(new ApollyonHeldItemLayer<>(this, context.m_234598_()));
        this.withScale(0.65F);
        this.f_114477_=0.8F;
    }
    @Override public void m_7392_(ApollyonServantEntity entity, float yaw, float partialTick,
                                  PoseStack pose, MultiBufferSource buffers, int light) {
        this.withScale(entity.isPigVariant() ? 1.25F : 0.65F);
        super.m_7392_(entity, yaw, partialTick, pose, buffers, light);
        if (!entity.isPigVariant()) {
            ApollyonDeathLight.renderApollyon(entity.deathAge(), partialTick, pose, buffers);
        }
        if (entity.hasCooperativeShield() && entity.m_6084_()) {
            ApollyonCooperativeShieldRenderer.renderShield(entity, partialTick, pose, buffers);
        }
    }

    @Override protected float getDeathMaxRotation(ApollyonServantEntity entity) { return 0.0F; }
}
