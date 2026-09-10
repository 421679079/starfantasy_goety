package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.model.CultistModel;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/** Applies Goety's Cultist spell pose to the twelve Doki Apostle geometries. */
public final class ApollyonPageantApostleModel extends GeoModel<ApollyonPageantApostleEntity> {
    private static final String[] RESOURCE_NAMES = {
            "apostle_the_risen",
            "apostle_the_abhorrent",
            "apostle_the_defiler",
            "apostle_the_dark",
            "apostle_the_great_shadow",
            "apostle_the_witch_king",
            "apostle_the_pyre_lord",
            "apostle_the_profane",
            "apostle_the_cruel",
            "apostle_the_terrible",
            "apostle_the_glorious",
            "apostle_the_atrocious"
    };
    private static final ResourceLocation ANIMATION = id(
            "animations/entity/apostle/apostle.animation.json");

    private final CultistModel<ApollyonPageantApostleEntity> poseModel;

    public ApollyonPageantApostleModel(CultistModel<ApollyonPageantApostleEntity> poseModel) {
        this.poseModel = poseModel;
    }

    @Override
    public ResourceLocation getModelResource(ApollyonPageantApostleEntity entity) {
        return id("geo/entity/apostle/" + resourceName(entity) + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ApollyonPageantApostleEntity entity) {
        return id("textures/entity/apostle/" + resourceName(entity) + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ApollyonPageantApostleEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(
            ApollyonPageantApostleEntity entity, long instanceId,
            AnimationState<ApollyonPageantApostleEntity> state) {
        EntityModelData modelData = state.getData(DataTickets.ENTITY_MODEL_DATA);
        Double tickData = state.getData(DataTickets.TICK);
        float tick = tickData == null ? (float) state.getAnimationTick() : tickData.floatValue();
        float headYaw = modelData == null ? 0.0F : -modelData.netHeadYaw();
        float headPitch = modelData == null ? 0.0F : -modelData.headPitch();

        float limbSwing = entity.isPageantMoving() ? tick * 0.8F : state.getLimbSwing();
        float limbSwingAmount = entity.isPageantMoving() ? 0.85F : state.getLimbSwingAmount();
        this.poseModel.prepareMobModel(
                entity, limbSwing, limbSwingAmount, state.getPartialTick());
        this.poseModel.setupAnim(
                entity, limbSwing, limbSwingAmount, tick, headYaw, headPitch);

        ModelPart head = this.poseModel.f_102808_;
        ModelPart rightArm = this.poseModel.f_102811_;
        ModelPart leftArm = this.poseModel.f_102812_;
        ModelPart rightLeg = this.poseModel.f_102813_;
        ModelPart leftLeg = this.poseModel.f_102814_;
        setCemRotation("a_head", head.f_104203_, head.f_104204_, head.f_104205_);
        setCemRotation("a_body", 0.0F, head.f_104204_ * 0.3F, 0.0F);
        setCemRotation("longlong", 0.5F * limbSwingAmount - head.f_104203_, 0.0F, 0.0F);
        setCemRotation("a_armL", leftArm.f_104203_, leftArm.f_104204_, -0.3F + leftArm.f_104205_);
        setCemRotation("a_armR", rightArm.f_104203_, rightArm.f_104204_, 0.3F + rightArm.f_104205_);
        setCemRotation("a_legL", Mth.m_14036_(leftLeg.f_104203_ * 0.35F, -0.24F, 0.24F), 0.0F, 0.0F);
        setCemRotation("a_legR", Mth.m_14036_(rightLeg.f_104203_ * 0.35F, -0.24F, 0.24F), 0.0F, 0.0F);
        setCemRotation("a_skirt1", 0.0F, 0.0F, 0.0F);
        float haloTurn = 0.4F * Mth.m_14031_(tick * 0.2F);
        setCemRotation("c1", 0.0F, haloTurn, 0.0F);
        setCemRotation("c2", 0.0F, -haloTurn, 0.0F);
    }

    private static String resourceName(ApollyonPageantApostleEntity entity) {
        int variant = Mth.m_14045_(entity.variant(), 0, RESOURCE_NAMES.length - 1);
        return RESOURCE_NAMES[variant];
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("starfantasy_goety", path);
    }

    private void setCemRotation(String boneName, float x, float y, float z) {
        this.getBone(boneName).ifPresent(bone -> setRotation(bone, -x, -y, z));
    }

    private static void setRotation(GeoBone bone, float x, float y, float z) {
        bone.setRotX(x);
        bone.setRotY(y);
        bone.setRotZ(z);
    }
}
