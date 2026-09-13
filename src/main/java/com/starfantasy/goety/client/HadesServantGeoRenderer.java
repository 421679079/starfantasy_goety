package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.entity.riding.HadesRiderSeat;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class HadesServantGeoRenderer extends GeoEntityRenderer<HadesServantEntity> {
    public HadesServantGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        this.withScale(1);
        this.f_114477_ = 1;
    }

    @Override protected float getDeathMaxRotation(HadesServantEntity entity) { return 0; }

    @Override public void m_7392_(HadesServantEntity entity, float yaw, float partialTick,
                                  PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, light);
        int afterglow = entity.shootAfterglowAge();
        if (entity.m_6084_() && (entity.isShootCharging() || afterglow >= 0 && afterglow < 24)) {
            double radians = Math.toRadians(entity.m_146908_());
            Vec3 focus = new Vec3(-Math.sin(radians) * 2.5, 2.5, Math.cos(radians) * 2.5);
            HadesGeoRenderer.renderCharge(entity, partialTick, poseStack, buffer, focus,
                    afterglow, 0.58F, 0.18F, 1, 1.0F / 3.0F);
        }
        ApollyonDeathLight.renderHadesServant(entity, partialTick, poseStack, buffer);
    }

    private static final class Model extends GeoModel<HadesServantEntity> {
        private static final ResourceLocation MODEL = id("geo/entity/hades.geo.json");
        private static final ResourceLocation TEXTURE = id("textures/entity/hades/hades.png");
        private static final ResourceLocation ANIMATION = id("animations/entity/hades.animation.json");
        private static ResourceLocation id(String path) { return new ResourceLocation(StarFantasyGoetyMod.MODID, path); }
        @Override public ResourceLocation getModelResource(HadesServantEntity entity) { return MODEL; }
        @Override public ResourceLocation getTextureResource(HadesServantEntity entity) { return TEXTURE; }
        @Override public ResourceLocation getAnimationResource(HadesServantEntity entity) { return ANIMATION; }
        @Override public void setCustomAnimations(HadesServantEntity entity, long instanceId,
                                                   AnimationState<HadesServantEntity> state) {
            super.setCustomAnimations(entity, instanceId, state);
            if (entity.m_6688_() == null || !entity.m_6084_()) return;
            // The visible head and physical passenger share the same animation clock and curves.
            HadesRiderSeat.Pose pose = HadesSeatInterpolation.pose(entity, state.getPartialTick());
            getBone("body").ifPresent(bone -> apply(bone, pose.body()));
            getBone("h_head").ifPresent(bone -> apply(bone, pose.head()));
        }
        private static void apply(GeoBone bone, HadesRiderSeat.BonePose pose) {
            bone.setPosX(pose.position().x); bone.setPosY(pose.position().y); bone.setPosZ(pose.position().z);
            bone.setRotX(pose.rotation().x); bone.setRotY(pose.rotation().y); bone.setRotZ(pose.rotation().z);
            bone.setScaleX(pose.scale().x); bone.setScaleY(pose.scale().y); bone.setScaleZ(pose.scale().z);
        }
    }
}
