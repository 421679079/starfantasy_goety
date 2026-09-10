package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.ApollyonPageantHaloEntity;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.library.vfx.client.StarFantasyStarArrowVisualRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Renders the matching registered halo item at the orbit anchor. */
public final class ApollyonPageantHaloRenderer
        extends EntityRenderer<ApollyonPageantHaloEntity> {
    private static final ResourceLocation ITEM_ATLAS =
            new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    private static final float HALO_ITEM_SCALE = 2.0F;

    private final ItemRenderer itemRenderer;

    public ApollyonPageantHaloRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.m_174025_();
        this.f_114477_ = 0.0F;
    }

    @Override
    public void m_7392_(
            ApollyonPageantHaloEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        Vec3 origin = new Vec3(
                Mth.m_14139_(partialTick, entity.f_19854_, entity.m_20185_()),
                Mth.m_14139_(partialTick, entity.f_19855_, entity.m_20186_()),
                Mth.m_14139_(partialTick, entity.f_19856_, entity.m_20189_()));
        List<Vec3> trail = new ArrayList<>(entity.trailPointCount());
        for (int i = 0; i < entity.trailPointCount(); ++i) {
            Vec3 point = entity.trailPoint(i);
            trail.add(new Vec3(
                    point.f_82479_ - origin.f_82479_,
                    point.f_82480_ - origin.f_82480_,
                    point.f_82481_ - origin.f_82481_));
        }
        StarFantasyStarArrowVisualRenderer.renderWorldTrailDepthTested(
                trail, poseStack, buffer,
                1.0F, 0.78F, 0.08F, 0.72F, 0.14F);

        poseStack.m_85836_();
        poseStack.m_252781_(Axis.f_252436_.m_252977_(
                180.0F - entity.interpolatedOutwardYaw(partialTick)));
        poseStack.m_85841_(HALO_ITEM_SCALE, HALO_ITEM_SCALE, HALO_ITEM_SCALE);
        this.itemRenderer.m_269128_(
                haloStack(entity), ItemDisplayContext.FIXED,
                0xF000F0, OverlayTexture.f_118083_, poseStack, buffer,
                entity.m_9236_(), entity.m_19879_());
        poseStack.m_85849_();
        super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static ItemStack haloStack(ApollyonPageantHaloEntity entity) {
        int variant = Mth.m_14045_(
                entity.variant(), 0, HaloItemRegistry.APOSTLE_HALOS.size() - 1);
        return HaloItemRegistry.APOSTLE_HALOS.get(variant).get().m_7968_();
    }

    @Override
    public ResourceLocation m_5478_(ApollyonPageantHaloEntity entity) {
        return ITEM_ATLAS;
    }
}
