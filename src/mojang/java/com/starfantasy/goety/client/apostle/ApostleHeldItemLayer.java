package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class ApostleHeldItemLayer extends BlockAndItemGeoLayer<ApostleGeoAnimatable> {
    private static final String RIGHT_HAND_BONE = "right_hand_item";
    private static final String LEFT_HAND_BONE = "left_hand_item";

    private final ApostleGeoRenderer apostleRenderer;
    private final ItemInHandRenderer itemRenderer;

    public ApostleHeldItemLayer(ApostleGeoRenderer renderer, ItemInHandRenderer itemRenderer) {
        super(renderer);
        this.apostleRenderer = renderer;
        this.itemRenderer = itemRenderer;
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, ApostleGeoAnimatable animatable) {
        Apostle apostle = this.apostleRenderer.getCurrentEntity();
        if (apostle == null || apostle.getArmPose() == Cultist.CultistArmPose.CROSSED) {
            return null;
        }

        boolean mainArmIsRight = apostle.getMainArm() == HumanoidArm.RIGHT;
        ItemStack stack;
        if (RIGHT_HAND_BONE.equals(bone.getName())) {
            stack = mainArmIsRight ? apostle.getMainHandItem() : apostle.getOffhandItem();
        } else if (LEFT_HAND_BONE.equals(bone.getName())) {
            stack = mainArmIsRight ? apostle.getOffhandItem() : apostle.getMainHandItem();
        } else {
            return null;
        }

        return stack.isEmpty() ? null : stack;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(
            GeoBone bone, ItemStack stack, ApostleGeoAnimatable animatable) {
        return LEFT_HAND_BONE.equals(bone.getName())
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(
            PoseStack poseStack,
            GeoBone bone,
            ItemStack stack,
            ApostleGeoAnimatable animatable,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        Apostle apostle = this.apostleRenderer.getCurrentEntity();
        if (apostle == null) {
            return;
        }

        boolean leftHand = LEFT_HAND_BONE.equals(bone.getName());
        ItemDisplayContext transform = getTransformTypeForStack(bone, stack, animatable);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate((leftHand ? -1.0F : 1.0F) / 16.0F, 0.125F, 0.0F);
        this.itemRenderer.renderItem(
                apostle, stack, transform, leftHand, poseStack, bufferSource, packedLight);
    }
}
