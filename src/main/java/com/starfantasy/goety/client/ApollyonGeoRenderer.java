package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.model.CultistModel;
import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ApollyonGeoRenderer extends GeoEntityRenderer<ApollyonEntity> {
    private final ApollyonArenaBoundaryRenderer arenaBoundaryRenderer;

    public ApollyonGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new ApollyonGeoModel(
                new CultistModel<>(context.m_174023_(ModModelLayer.APOSTLE))));
        this.arenaBoundaryRenderer = new ApollyonArenaBoundaryRenderer();
        this.addRenderLayer(new ApollyonMonolithAuraLayer(this));
        this.addRenderLayer(new ApollyonHeldItemLayer<>(this, context.m_234598_()));
        this.withScale(0.65F);
        this.f_114477_ = 0.8F;
    }

    @Override
    public void m_7392_(ApollyonEntity entity, float entityYaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.arenaBoundaryRenderer.render(entity, partialTick, poseStack, buffer);
        if (entity.getPageantOpacity() <= 0.001F) {
            return;
        }
        super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        ApollyonCooperativeShieldRenderer.render(entity, partialTick, poseStack, buffer);
        ApollyonDeathLight.renderApollyon(entity, partialTick, poseStack, buffer);
    }

    @Override
    public boolean shouldRender(
            ApollyonEntity entity, Frustum frustum,
            double cameraX, double cameraY, double cameraZ) {
        if (super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ)) {
            return true;
        }
        if (!entity.isArenaActive()) {
            return false;
        }
        Vec3 home = entity.arenaHomePosition();
        double halfSize = ApollyonEntity.ARENA_RADIUS + 3.0D;
        AABB forceFieldBounds = new AABB(
                home.f_82479_ - halfSize, home.f_82480_ - 0.25D,
                home.f_82481_ - halfSize,
                home.f_82479_ + halfSize, home.f_82480_ + 2.25D,
                home.f_82481_ + halfSize);
        return frustum.isVisible(forceFieldBounds);
    }

    @Override
    protected float getDeathMaxRotation(ApollyonEntity entity) {
        return 0.0F;
    }

    @Override
    public Color getRenderColor(
            ApollyonEntity entity, float partialTick, int packedLight) {
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, entity.getPageantOpacity());
    }

    @Override
    public RenderType getRenderType(
            ApollyonEntity entity, ResourceLocation texture,
            MultiBufferSource buffer, float partialTick) {
        return RenderType.m_110473_(texture);
    }
}
