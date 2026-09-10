package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.client.render.model.ApostleModel;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist.CultistArmPose;
import com.starfantasy.goety.client.apostle.ApostleResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Map;
import java.util.WeakHashMap;

public final class ApostleGeoModel extends GeoModel<ApostleGeoAnimatable> {
    private static final float NORMAL_LEG_SCALE = 0.2F;
    private static final float ACTION_LEG_SCALE = 0.35F;
    private static final float MAX_LEG_ROTATION = 0.24F;
    private static final float LEG_SCALE_CHANGE_PER_TICK = 0.04F;

    private static final ResourceLocation ANIMATION =
            ApostleResources.id("animations/entity/apostle/apostle.animation.json");

    private final ApostleModel<Apostle> poseModel;
    private final Map<Apostle, LegMotionState> legMotionStates = new WeakHashMap<>();

    public ApostleGeoModel(ApostleModel<Apostle> poseModel) {
        this.poseModel = poseModel;
    }

    @Override
    public ResourceLocation getModelResource(ApostleGeoAnimatable animatable) {
        return ApostleVisual.DEFAULT.model();
    }

    @Override
    public ResourceLocation getModelResource(
            ApostleGeoAnimatable animatable, GeoRenderer<ApostleGeoAnimatable> renderer) {
        return visual(renderer).model();
    }

    @Override
    public ResourceLocation getTextureResource(ApostleGeoAnimatable animatable) {
        return ApostleVisual.DEFAULT.texture();
    }

    @Override
    public ResourceLocation getTextureResource(
            ApostleGeoAnimatable animatable, GeoRenderer<ApostleGeoAnimatable> renderer) {
        return visual(renderer).texture();
    }

    @Override
    public ResourceLocation getAnimationResource(ApostleGeoAnimatable animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(
            ApostleGeoAnimatable animatable,
            long instanceId,
            AnimationState<ApostleGeoAnimatable> state) {
        Entity renderedEntity = state.getData(DataTickets.ENTITY);
        if (!(renderedEntity instanceof Apostle apostle)) {
            return;
        }

        EntityModelData modelData = state.getData(DataTickets.ENTITY_MODEL_DATA);
        Double tickData = state.getData(DataTickets.TICK);
        double tick = tickData == null ? state.getAnimationTick() : tickData;
        float limbSwing = state.getLimbSwing();
        float limbSwingAmount = state.getLimbSwingAmount();
        float partialTick = state.getPartialTick();
        float vanillaHeadYaw = modelData == null ? 0.0F : -modelData.netHeadYaw();
        float vanillaHeadPitch = modelData == null ? 0.0F : -modelData.headPitch();

        this.poseModel.prepareMobModel(apostle, limbSwing, limbSwingAmount, partialTick);
        this.poseModel.setupAnim(
                apostle,
                limbSwing,
                limbSwingAmount,
                (float) tick,
                vanillaHeadYaw,
                vanillaHeadPitch);

        float headX = this.poseModel.head.xRot;
        float headY = this.poseModel.head.yRot;
        float headZ = this.poseModel.head.zRot;
        float leftLegX = this.poseModel.leftLeg.xRot;
        float rightLegX = this.poseModel.rightLeg.xRot;
        boolean individualArmsVisible = this.poseModel.rightArm.visible;
        float legScale = legScale(apostle, tick);
        float leftLegPose = Mth.clamp(
                leftLegX * legScale, -MAX_LEG_ROTATION, MAX_LEG_ROTATION);
        float rightLegPose = Mth.clamp(
                rightLegX * legScale, -MAX_LEG_ROTATION, MAX_LEG_ROTATION);

        float leftArmX;
        float rightArmX;
        float leftArmY;
        float rightArmY;
        float leftArmZ;
        float rightArmZ;

        if (individualArmsVisible) {
            // Preserve Goety's complete per-arm pose. Bow aiming relies on Y rotation,
            // while spellcasting relies on the age-driven X rotation.
            leftArmX = this.poseModel.leftArm.xRot;
            rightArmX = this.poseModel.rightArm.xRot;
            leftArmY = this.poseModel.leftArm.yRot;
            rightArmY = this.poseModel.rightArm.yRot;
            leftArmZ = -0.3F + this.poseModel.leftArm.zRot;
            rightArmZ = 0.3F + this.poseModel.rightArm.zRot;
        } else {
            // The source model hides the individual arms for its crossed-arm part.
            // Keep the CEM fallback pose used by the separated Gecko bones.
            leftArmX = this.poseModel.leftArm.xRot + leftLegX;
            rightArmX = this.poseModel.rightArm.xRot + rightLegX;
            leftArmY = 0.314F;
            rightArmY = -0.314F;
            leftArmZ = -0.3F;
            rightArmZ = 0.3F;
        }

        setCemRotation("a_head", headX, headY, headZ);
        setCemRotation("a_body", 0.0F, headY * 0.3F, 0.0F);
        setCemRotation("longlong", 0.5F * limbSwingAmount - headX, 0.0F, 0.0F);
        setCemRotation("a_armL", leftArmX, leftArmY, leftArmZ);
        setCemRotation("a_armR", rightArmX, rightArmY, rightArmZ);
        setCemRotation("a_legL", leftLegPose, 0.0F, 0.0F);
        setCemRotation("a_legR", rightLegPose, 0.0F, 0.0F);
        setCemRotation("a_skirt1", 0.0F, 0.0F, 0.0F);

        float haloTurn = 0.4F * (float) Math.sin(tick * 0.2D);
        setCemRotation("c1", 0.0F, haloTurn, 0.0F);
        setCemRotation("c2", 0.0F, -haloTurn, 0.0F);
    }

    private float legScale(Apostle apostle, double tick) {
        float targetScale = usesEmphasizedLegMotion(apostle.getArmPose())
                ? ACTION_LEG_SCALE
                : NORMAL_LEG_SCALE;
        LegMotionState motion = this.legMotionStates.computeIfAbsent(
                apostle, ignored -> new LegMotionState(NORMAL_LEG_SCALE, tick));

        double elapsedTicks = tick - motion.lastTick;
        if (elapsedTicks < 0.0D || elapsedTicks > 20.0D) {
            motion.scale = targetScale;
        } else {
            float maxChange = (float) elapsedTicks * LEG_SCALE_CHANGE_PER_TICK;
            motion.scale = Mth.approach(motion.scale, targetScale, maxChange);
        }

        motion.lastTick = tick;
        return motion.scale;
    }

    private static boolean usesEmphasizedLegMotion(CultistArmPose armPose) {
        return armPose == CultistArmPose.BOW_AND_ARROW
                || armPose == CultistArmPose.SPELLCASTING
                || armPose == CultistArmPose.SPELL_AND_WEAPON;
    }

    private ApostleVisual visual(GeoRenderer<ApostleGeoAnimatable> renderer) {
        if (renderer instanceof ApostleGeoRenderer apostleRenderer) {
            return ApostleVisual.resolve(apostleRenderer.getCurrentEntity());
        }
        return ApostleVisual.DEFAULT;
    }

    private void setCemRotation(String boneName, float x, float y, float z) {
        setRotation(boneName, -x, -y, z);
    }

    private void setRotation(String boneName, float x, float y, float z) {
        this.getBone(boneName).ifPresent(bone -> setRotation(bone, x, y, z));
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
