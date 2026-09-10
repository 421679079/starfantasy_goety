package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesDiveRayLaserEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Bosses' Denia laser-column renderer, recolored purple for Hades. */
public final class HadesDiveRayLaserRenderer
        extends EntityRenderer<HadesDiveRayLaserEntity> {
    private static final ResourceLocation WHITE =
            new ResourceLocation("minecraft", "textures/block/white_concrete.png");
    private static final ResourceLocation RING = new ResourceLocation(
            StarFantasyGoetyMod.CONTENT_NAMESPACE,
            "textures/effect/hades_dive_ray_ring.png");
    private static final int OUTLINE_COLOR = 0xA840E6;
    private static final int RING_COLOR = 0xD58AFF;
    private static final int CORE_COLOR = 0x26045F;
    private static final float LASER_WIDTH_RATIO = 0.7F;
    private static final float RING_RADIUS_RATIO = 0.7F;

    public HadesDiveRayLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0F;
    }

    @Override
    public void m_7392_(
            HadesDiveRayLaserEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.visualAge(partialTick);
        float beamPulse = age <= HadesDiveRayLaserEntity.BEAM_VISIBLE_TICKS
                ? Mth.m_14031_(Mth.m_14036_(
                age / HadesDiveRayLaserEntity.BEAM_VISIBLE_TICKS,
                0.0F, 1.0F) * (float) Math.PI)
                : 0.0F;
        float width = entity.visualWidth() * LASER_WIDTH_RATIO * beamPulse;
        float length = entity.visualLength() * Mth.m_14036_(
                age / HadesDiveRayLaserEntity.EXTEND_TICKS, 0.0F, 1.0F);

        poseStack.m_85836_();
        poseStack.m_252781_(rotationFromPositiveZ(entity.beamDirection()));
        if (beamPulse > 0.01F && width > 0.04F && length > 0.04F) {
            VertexConsumer consumer = buffer.m_6299_(
                    StarFantasyVfxRenderTypes.translucentPositionColor());
            renderBeam(entity, age, width, length, poseStack, consumer);
        }
        // Different render types may reuse the same immediate BufferBuilder.
        // Finish all position/color vertices before switching to the entity format.
        VertexConsumer ringConsumer = buffer.m_6299_(RenderType.m_234338_(RING));
        renderRings(entity, age, poseStack, ringConsumer);
        poseStack.m_85849_();
        super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(HadesDiveRayLaserEntity entity) {
        return WHITE;
    }

    private static Quaternionf rotationFromPositiveZ(Vec3 direction) {
        Vec3 normalized = direction.m_82556_() > 1.0E-6D
                ? direction.m_82541_() : new Vec3(0.0D, 0.0D, 1.0D);
        return new Vector3f(0.0F, 0.0F, 1.0F).rotationTo(
                new Vector3f((float) normalized.f_82479_,
                        (float) normalized.f_82480_,
                        (float) normalized.f_82481_),
                new Quaternionf());
    }

    private static void renderBeam(
            HadesDiveRayLaserEntity entity, float age, float width, float length,
            PoseStack poseStack, VertexConsumer consumer) {
        float spin = age * 34.0F + (entity.visualSeed() & 0xFF);
        poseStack.m_85836_();
        poseStack.m_85837_(0.0D, 0.0D, length * 0.5D);
        poseStack.m_252781_(new Quaternionf().rotationZ(
                spin * ((float) Math.PI / 180.0F)));
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        float alpha = entity.visualAlpha();
        float outlineAlpha = alpha * 0.5F;
        renderCenteredCuboid(consumer, matrix, width, width, length,
                OUTLINE_COLOR, outlineAlpha);
        renderCenteredCuboid(consumer, matrix,
                width * 0.82F, width * 0.82F, length * 1.01F,
                CORE_COLOR, alpha);
        renderCenteredCuboid(consumer, matrix,
                width * 1.03F, width * 0.12F, length * 1.02F,
                OUTLINE_COLOR, outlineAlpha);
        renderCenteredCuboid(consumer, matrix,
                width * 0.12F, width * 1.03F, length * 1.02F,
                OUTLINE_COLOR, outlineAlpha);
        poseStack.m_85849_();
    }

    private static void renderRings(
            HadesDiveRayLaserEntity entity, float age,
            PoseStack poseStack, VertexConsumer consumer) {
        int lastSpawnTick = Math.min(HadesDiveRayLaserEntity.EXTEND_TICKS,
                (int) Math.floor(age));
        for (int spawnTick = 0; spawnTick <= lastSpawnTick; ++spawnTick) {
            float ringAge = age - spawnTick;
            if (ringAge < 0.0F
                    || ringAge > HadesDiveRayLaserEntity.RING_LIFETIME_TICKS) {
                continue;
            }
            float progress = Mth.m_14036_(
                    ringAge / HadesDiveRayLaserEntity.RING_LIFETIME_TICKS,
                    0.0F, 1.0F);
            float ringPulse = Mth.m_14031_(progress * (float) Math.PI);
            float radius = entity.visualWidth() * RING_RADIUS_RATIO * ringPulse;
            float alphaScale = entity.visualAlpha()
                    / HadesDiveRayLaserEntity.DEFAULT_ALPHA;
            float alpha = Mth.m_14036_(
                    0.72F * ringPulse * alphaScale, 0.0F, 1.0F);
            if (radius <= 0.04F || alpha <= 0.01F) {
                continue;
            }
            float z = entity.visualLength() * Mth.m_14036_(
                    spawnTick / (float) HadesDiveRayLaserEntity.EXTEND_TICKS,
                    0.0F, 1.0F);
            float rotation = age * 22.0F + spawnTick * 31.0F
                    + (entity.visualSeed() & 0xFF);
            poseStack.m_85836_();
            poseStack.m_85837_(0.0D, 0.0D, z);
            poseStack.m_252781_(new Quaternionf().rotationZ(
                    rotation * ((float) Math.PI / 180.0F)));
            renderTexturedRing(consumer, poseStack.m_85850_(), radius,
                    RING_COLOR, alpha);
            poseStack.m_85849_();
        }
    }

    private static void renderCenteredCuboid(
            VertexConsumer consumer, Matrix4f matrix,
            float width, float height, float length, int color, float alpha) {
        if (width <= 0.0F || height <= 0.0F || length <= 0.0F || alpha <= 0.0F) {
            return;
        }
        float x = width * 0.5F;
        float y = height * 0.5F;
        float z = length * 0.5F;
        renderFace(consumer, matrix, -x, -y, z, x, -y, z,
                x, y, z, -x, y, z, color, alpha);
        renderFace(consumer, matrix, x, -y, -z, -x, -y, -z,
                -x, y, -z, x, y, -z, color, alpha);
        renderFace(consumer, matrix, x, -y, z, x, -y, -z,
                x, y, -z, x, y, z, color, alpha);
        renderFace(consumer, matrix, -x, -y, -z, -x, -y, z,
                -x, y, z, -x, y, -z, color, alpha);
        renderFace(consumer, matrix, -x, y, z, x, y, z,
                x, y, -z, -x, y, -z, color, alpha);
        renderFace(consumer, matrix, -x, -y, -z, x, -y, -z,
                x, -y, z, -x, -y, z, color, alpha);
    }

    private static void renderTexturedRing(
            VertexConsumer consumer, PoseStack.Pose pose,
            float radius, int color, float alpha) {
        float half = radius;
        texturedVertex(consumer, pose, -half, -half, 0.0F,
                0.0F, 1.0F, color, alpha, 1.0F);
        texturedVertex(consumer, pose, half, -half, 0.0F,
                1.0F, 1.0F, color, alpha, 1.0F);
        texturedVertex(consumer, pose, half, half, 0.0F,
                1.0F, 0.0F, color, alpha, 1.0F);
        texturedVertex(consumer, pose, -half, half, 0.0F,
                0.0F, 0.0F, color, alpha, 1.0F);
        texturedVertex(consumer, pose, -half, half, 0.0F,
                0.0F, 0.0F, color, alpha, -1.0F);
        texturedVertex(consumer, pose, half, half, 0.0F,
                1.0F, 0.0F, color, alpha, -1.0F);
        texturedVertex(consumer, pose, half, -half, 0.0F,
                1.0F, 1.0F, color, alpha, -1.0F);
        texturedVertex(consumer, pose, -half, -half, 0.0F,
                0.0F, 1.0F, color, alpha, -1.0F);
    }

    private static void renderFace(
            VertexConsumer consumer, Matrix4f matrix,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            int color, float alpha) {
        vertex(consumer, matrix, x1, y1, z1, color, alpha);
        vertex(consumer, matrix, x2, y2, z2, color, alpha);
        vertex(consumer, matrix, x3, y3, z3, color, alpha);
        vertex(consumer, matrix, x4, y4, z4, color, alpha);
    }

    private static void vertex(
            VertexConsumer consumer, Matrix4f matrix,
            float x, float y, float z, int color, float alpha) {
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(red(color), green(color), blue(color), alpha)
                .m_5752_();
    }

    private static void texturedVertex(
            VertexConsumer consumer, PoseStack.Pose pose,
            float x, float y, float z, float u, float v,
            int color, float alpha, float normalZ) {
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(red(color), green(color), blue(color), alpha)
                .m_7421_(u, v)
                .m_86008_(OverlayTexture.f_118083_)
                .m_85969_(0xF000F0)
                .m_252939_(normal, 0.0F, 0.0F, normalZ)
                .m_5752_();
    }

    private static float red(int color) {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    private static float green(int color) {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }
}
