package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.model.CultistModel;
import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist.CultistArmPose;
import com.starfantasy.goety.entity.ApollyonEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Keeps the custom Apollyon mesh, while copying Goety Cultist/Apostle combat poses
 * into its bones every frame. No Apollyon Gecko animation controller is used.
 */
public final class ApollyonGeoModel extends GeoModel<ApollyonEntity> {
    private static final float NORMAL_LEG_SCALE = 0.2F;
    private static final float ACTION_LEG_SCALE = 0.35F;
    private static final float MAX_LEG_ROTATION = 0.24F;
    private static final float LEG_SCALE_CHANGE_PER_TICK = 0.04F;

    private static final String RESOURCE_NAMESPACE = "starfantasy_goety";
    private static final ResourceLocation MODEL = id("geo/entity/apollyon/apollyon.geo.json");
    private static final ResourceLocation TEXTURE = id("textures/entity/apollyon/apollyon.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation("starfantasy_goety", "animations/entity/apollyon/apollyon.animation.json");

    private final CultistModel<ApollyonEntity> poseModel;
    private final Map<ApollyonEntity, LegMotionState> legMotionStates = new WeakHashMap<>();

    public ApollyonGeoModel(CultistModel<ApollyonEntity> poseModel) {
        this.poseModel = poseModel;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(RESOURCE_NAMESPACE, path);
    }

    @Override
    public ResourceLocation getModelResource(ApollyonEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ApollyonEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ApollyonEntity entity) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(
            ApollyonEntity entity, long instanceId, AnimationState<ApollyonEntity> state) {
        EntityModelData modelData = state.getData(DataTickets.ENTITY_MODEL_DATA);
        Double tickData = state.getData(DataTickets.TICK);
        float tick = tickData == null ? (float) state.getAnimationTick() : tickData.floatValue();
        float headYaw = modelData == null ? 0.0F : -modelData.netHeadYaw();
        float headPitch = modelData == null ? 0.0F : -modelData.headPitch();

        this.poseModel.prepareMobModel(
                entity, state.getLimbSwing(), state.getLimbSwingAmount(), state.getPartialTick());
        this.poseModel.setupAnim(
                entity, state.getLimbSwing(), state.getLimbSwingAmount(), tick, headYaw, headPitch);

        ModelPart head = this.poseModel.f_102808_;
        copyCemRotation("AllHead", head);
        // Doki Apostle only lets the upper body follow 30% of head yaw. Copying
        // the pose model body's pitch here made Apollyon's whole torso look up.
        setCemRotation("UpBody", 0.0F, head.f_104204_ * 0.3F, 0.0F);
        if (this.poseModel.f_102811_.f_104207_) {
            copyCemRotation("RightArm", this.poseModel.f_102811_);
            copyCemRotation("LeftArm", this.poseModel.f_102812_);
        } else {
            setCemRotation("RightArm", this.poseModel.f_102811_.f_104203_, -0.314F, 0.3F);
            setCemRotation("LeftArm", this.poseModel.f_102812_.f_104203_, 0.314F, -0.3F);
        }
        float legScale = legScale(entity, tick);
        float rightLegX = Mth.m_14036_(
                this.poseModel.f_102813_.f_104203_ * legScale,
                -MAX_LEG_ROTATION,
                MAX_LEG_ROTATION);
        float leftLegX = Mth.m_14036_(
                this.poseModel.f_102814_.f_104203_ * legScale,
                -MAX_LEG_ROTATION,
                MAX_LEG_ROTATION);
        setCemRotation("RightLeg", rightLegX, 0.0F, 0.0F);
        setCemRotation("LeftLeg", leftLegX, 0.0F, 0.0F);
        setCemRotation("RightForeArm", 0.0F, 0.0F, 0.0F);
        setCemRotation("LeftForeArm", 0.0F, 0.0F, 0.0F);
        this.getBone("Throne").ifPresent(bone -> bone.setHidden(true));
    }

    private float legScale(ApollyonEntity entity, double tick) {
        float targetScale = usesEmphasizedLegMotion(entity.getArmPose())
                ? ACTION_LEG_SCALE
                : NORMAL_LEG_SCALE;
        LegMotionState motion = this.legMotionStates.computeIfAbsent(
                entity, ignored -> new LegMotionState(NORMAL_LEG_SCALE, tick));

        double elapsedTicks = tick - motion.lastTick;
        if (elapsedTicks < 0.0D || elapsedTicks > 20.0D) {
            motion.scale = targetScale;
        } else {
            float maxChange = (float) elapsedTicks * LEG_SCALE_CHANGE_PER_TICK;
            motion.scale = approach(motion.scale, targetScale, maxChange);
        }

        motion.lastTick = tick;
        return motion.scale;
    }

    private static float approach(float current, float target, float maxChange) {
        if (current < target) {
            return Math.min(current + maxChange, target);
        }
        return Math.max(current - maxChange, target);
    }

    private static boolean usesEmphasizedLegMotion(CultistArmPose armPose) {
        return armPose == CultistArmPose.BOW_AND_ARROW
                || armPose == CultistArmPose.SPELLCASTING
                || armPose == CultistArmPose.SPELL_AND_WEAPON;
    }

    private void copyCemRotation(String boneName, ModelPart part) {
        setCemRotation(boneName, part.f_104203_, part.f_104204_, part.f_104205_);
    }

    private void setCemRotation(String boneName, float x, float y, float z) {
        this.getBone(boneName).ifPresent(bone -> setRotation(bone, -x, -y, z));
    }

    private static void setRotation(GeoBone bone, float x, float y, float z) {
        bone.setRotX(x);
        bone.setRotY(y);
        bone.setRotZ(z);
    }

    private static final class LegMotionState {
        private float scale;
        private double lastTick;

        private LegMotionState(float scale, double lastTick) {
            this.scale = scale;
            this.lastTick = lastTick;
        }
    }
}
