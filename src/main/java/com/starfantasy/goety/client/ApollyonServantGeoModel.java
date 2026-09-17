package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.ApollyonServantEntity;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import java.util.List;
import java.util.Map;

/** Existing Apollyon mesh, with the bow/casting poses applied to a Goety servant. */
public final class ApollyonServantGeoModel extends GeoModel<ApollyonServantEntity> {
    private static final ResourceLocation PIG_MODEL = new ResourceLocation("starfantasy_goety", "geo/entity/apollyon/apollyon_pig.geo.json");
    private static final ResourceLocation PIG_TEXTURE = new ResourceLocation("starfantasy_goety", "textures/entity/apollyon/apollyon_pig.png");
    private static final ResourceLocation PIG_ANIMATION = new ResourceLocation("starfantasy_goety", "animations/entity/apollyon/apollyon_pig.animation.json");
    private final ModelPart rightArm = new ModelPart(List.of(), Map.of());
    private final ModelPart leftArm = new ModelPart(List.of(), Map.of());

    @Override public ResourceLocation getModelResource(ApollyonServantEntity entity) {
        if (entity.isPigVariant()) return PIG_MODEL;
        return new ResourceLocation("starfantasy_goety", "geo/entity/apollyon/apollyon.geo.json");
    }
    @Override public ResourceLocation getTextureResource(ApollyonServantEntity entity) {
        if (entity.isPigVariant()) return PIG_TEXTURE;
        return new ResourceLocation("starfantasy_goety", "textures/entity/apollyon/apollyon.png");
    }
    @Override public ResourceLocation getAnimationResource(ApollyonServantEntity entity) {
        if (entity.isPigVariant()) return PIG_ANIMATION;
        return new ResourceLocation("starfantasy_goety", "animations/entity/apollyon/apollyon.animation.json");
    }
    @Override public void setCustomAnimations(ApollyonServantEntity entity, long id,
                                              AnimationState<ApollyonServantEntity> state) {
        if (entity.isPigVariant()) return;
        boolean seated = entity.isStaying() && entity.m_6084_();
        this.getBone("Throne").ifPresent(bone -> bone.setHidden(!seated));
        if (seated) return;
        // Sitting animates the root, skirt, hair and lower legs as well as arms.
        // Reset all of them before applying the native combat pose on this shared mesh.
        for (CoreGeoBone bone : this.getAnimationProcessor().getRegisteredBones()) {
            var initial = bone.getInitialSnapshot();
            bone.setRotX(initial.getRotX());
            bone.setRotY(initial.getRotY());
            bone.setRotZ(initial.getRotZ());
            bone.setPosX(initial.getOffsetX());
            bone.setPosY(initial.getOffsetY());
            bone.setPosZ(initial.getOffsetZ());
            bone.setScaleX(initial.getScaleX());
            bone.setScaleY(initial.getScaleY());
            bone.setScaleZ(initial.getScaleZ());
        }
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        float yaw = data == null ? 0 : -data.netHeadYaw() * ((float) Math.PI / 180);
        float pitch = data == null ? 0 : -data.headPitch() * ((float) Math.PI / 180);
        pose("AllHead", pitch, yaw, 0);
        pose("UpBody", 0, yaw * 0.3F, 0);
        if (entity.isCastingAction()) {
            float tick = entity.f_19797_ + state.getPartialTick();
            // Same SPELL_AND_WEAPON pose as Goety's CultistModel: hold the bow
            // in one hand and raise the other hand to cast.
            AnimationUtils.m_102091_(rightArm, leftArm, entity, 0, tick);
            pose("RightArm", rightArm.f_104203_, rightArm.f_104204_, rightArm.f_104205_);
            pose("LeftArm", leftArm.f_104203_, leftArm.f_104204_, leftArm.f_104205_);
            float sway = Mth.m_14089_(tick * 0.6662F) * 0.25F;
            if (entity.m_5737_() == HumanoidArm.RIGHT) pose("LeftArm", sway, 0, -2.3561945F);
            else pose("RightArm", sway, 0, 2.3561945F);
        } else if (entity.m_6117_() || entity.m_5912_()) {
            pose("RightArm", -(float) Math.PI / 2 + pitch, yaw - 0.1F, 0);
            pose("LeftArm", -(float) Math.PI / 2 + pitch, yaw + 0.5F, 0);
        } else {
            // Keep ordinary alternating walking motion, at half strength for the long sleeves.
            float swing = state.getLimbSwing() * 0.6662F;
            float amplitude = state.getLimbSwingAmount() * 0.5F;
            pose("RightArm", Mth.m_14089_(swing + (float) Math.PI) * amplitude, -0.314F, 0.3F);
            pose("LeftArm", Mth.m_14089_(swing) * amplitude, 0.314F, -0.3F);
        }
        float legScale = entity.isCastingAction() || entity.m_5912_() ? 0.35F : 0.2F;
        float stride = Mth.m_14036_(Mth.m_14089_(state.getLimbSwing() * 0.6662F)
                * state.getLimbSwingAmount() * 0.7F * legScale, -0.24F, 0.24F);
        pose("RightLeg", stride, 0, 0);
        pose("LeftLeg", -stride, 0, 0);
        pose("RightForeArm", 0, 0, 0);
        pose("LeftForeArm", 0, 0, 0);
    }
    private void pose(String name, float x, float y, float z) {
        getBone(name).ifPresent(bone -> { bone.setRotX(-x); bone.setRotY(-y); bone.setRotZ(z); });
    }
}
