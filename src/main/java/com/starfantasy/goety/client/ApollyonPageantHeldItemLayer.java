package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/** Renders phase-three bows on the locator bones supplied by Doki Apostle. */
public final class ApollyonPageantHeldItemLayer
        extends BlockAndItemGeoLayer<ApollyonPageantApostleEntity> {
    private static final String RIGHT_HAND_BONE = "right_hand_item";
    private static final String LEFT_HAND_BONE = "left_hand_item";

    private final ItemInHandRenderer itemRenderer;

    public ApollyonPageantHeldItemLayer(
            GeoRenderer<ApollyonPageantApostleEntity> renderer,
            ItemInHandRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    protected ItemStack getStackForBone(
            GeoBone bone, ApollyonPageantApostleEntity entity) {
        boolean mainArmIsRight = entity.m_5737_() == HumanoidArm.RIGHT;
        ItemStack stack;
        if (RIGHT_HAND_BONE.equals(bone.getName())) {
            stack = mainArmIsRight ? entity.m_21205_() : entity.m_21206_();
        } else if (LEFT_HAND_BONE.equals(bone.getName())) {
            stack = mainArmIsRight ? entity.m_21206_() : entity.m_21205_();
        } else {
            return null;
        }
        return stack.m_41619_() ? null : stack;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(
            GeoBone bone, ItemStack stack, ApollyonPageantApostleEntity entity) {
        return LEFT_HAND_BONE.equals(bone.getName())
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(
            PoseStack poseStack,
            GeoBone bone,
            ItemStack stack,
            ApollyonPageantApostleEntity entity,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay) {
        boolean leftHand = LEFT_HAND_BONE.equals(bone.getName());
        ItemDisplayContext transform = this.getTransformTypeForStack(bone, stack, entity);
        poseStack.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
        poseStack.m_85837_((leftHand ? -1.0F : 1.0F) / 16.0F, 0.125F, 0.0F);
        this.itemRenderer.m_269530_(
                entity, stack, transform, leftHand, poseStack, bufferSource, packedLight);
    }
}
