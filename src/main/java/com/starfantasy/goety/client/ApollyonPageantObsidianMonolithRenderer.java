package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.AbstractMonolithRenderer;
import com.Polarice3.Goety.common.entities.neutral.AbstractMonolith;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonPageantObsidianMonolithEntity;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Doubles the monolith model while keeping Goety's link endpoint at world scale. */
public final class ApollyonPageantObsidianMonolithRenderer
        extends AbstractMonolithRenderer<ApollyonPageantObsidianMonolithEntity> {
    private static final ResourceLocation TEXTURE =
            id("textures/entity/monolith/obsidian_monolith.png");
    private static final RenderType GLOW = RenderType.m_110488_(
            id("textures/entity/monolith/obsidian_monolith_glow.png"));
    private static final RenderType CHAIN = RenderType.m_110443_(
            id("textures/entity/monolith/obsidian_monolith_chain.png"), false);
    private static final Map<AbstractMonolith.Crackiness, ResourceLocation> CRACKS = Map.of(
            AbstractMonolith.Crackiness.LOW,
            id("textures/entity/monolith/obsidian_monolith_crack_1.png"),
            AbstractMonolith.Crackiness.MEDIUM,
            id("textures/entity/monolith/obsidian_monolith_crack_2.png"),
            AbstractMonolith.Crackiness.HIGH,
            id("textures/entity/monolith/obsidian_monolith_crack_3.png"));

    public ApollyonPageantObsidianMonolithRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(
            ApollyonPageantObsidianMonolithEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight) {
        poseStack.m_85836_();
        poseStack.m_85841_(2.0F, 2.0F, 2.0F);
        super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.m_85849_();
        LivingEntity linked = entity.getTrueOwner();
        if (linked == null || entity.isEmerging()) {
            return;
        }
        poseStack.m_85836_();
        Vec3 camera = Minecraft.m_91087_().f_91063_.m_109153_().m_90583_();
        Vec3 start = new Vec3(
                Mth.m_14139_(partialTick, entity.f_19854_, entity.m_20185_()),
                Mth.m_14139_(partialTick, entity.f_19855_, entity.m_20186_())
                        + entity.m_20206_(),
                Mth.m_14139_(partialTick, entity.f_19856_, entity.m_20189_()));
        poseStack.m_85837_(-start.f_82479_,
                -(start.f_82480_ - entity.m_20206_()), -start.f_82481_);
        Vec3 end = new Vec3(
                Mth.m_14139_(partialTick, linked.f_19854_, linked.m_20185_()),
                Mth.m_14139_(partialTick, linked.f_19855_, linked.m_20186_())
                        + linked.m_20206_() * 0.5D,
                Mth.m_14139_(partialTick, linked.f_19856_, linked.m_20189_()));
        VertexConsumer consumer = bufferSource.m_6299_(CHAIN);
        Vec3 offset = end.m_82546_(start);
        Vec3 sight = camera.m_82546_(start).m_82490_(-1.0D);
        Vec3 side = offset.m_82537_(sight).m_82541_().m_82490_(0.25D);
        float textureOffset = -(entity.f_19797_ + partialTick) * 0.06F;
        PoseStack.Pose pose = poseStack.m_85850_();
        this.vertex(consumer, pose, start.m_82549_(side), textureOffset, 0.0F);
        this.vertex(consumer, pose, start.m_82549_(side.m_82490_(-1.0D)),
                textureOffset, 1.0F);
        this.vertex(consumer, pose, end.m_82549_(side.m_82490_(-1.0D)),
                (float) (offset.m_82553_() * 2.0D) + textureOffset, 1.0F);
        this.vertex(consumer, pose, end.m_82549_(side),
                (float) (offset.m_82553_() * 2.0D) + textureOffset, 0.0F);
        poseStack.m_85849_();
    }

    @Override
    public RenderType getActivatedTextureLocation(
            ApollyonPageantObsidianMonolithEntity entity) {
        return GLOW;
    }

    @Override
    public Map<AbstractMonolith.Crackiness, ResourceLocation> cracknessLocation() {
        return CRACKS;
    }

    @Override
    public ResourceLocation m_5478_(ApollyonPageantObsidianMonolithEntity entity) {
        return TEXTURE;
    }

    private void vertex(
            VertexConsumer consumer, PoseStack.Pose pose, Vec3 position, float u, float v) {
        consumer.m_252986_(pose.m_252922_(),
                        (float) position.f_82479_,
                        (float) position.f_82480_,
                        (float) position.f_82481_)
                .m_193479_(-1)
                .m_7421_(u, v)
                .m_86008_(OverlayTexture.f_118083_)
                .m_85969_(0xF000F0)
                .m_252939_(pose.m_252943_(), 0.0F, 1.0F, 0.0F)
                .m_5752_();
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("goety", path);
    }
}
