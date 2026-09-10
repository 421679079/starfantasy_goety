package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesClawSlashEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
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

/** Additive three-level claw trails for Hades's roundhouse and crossing swipes. */
public final class HadesClawSlashRenderer extends EntityRenderer<HadesClawSlashEntity> {
    private static final ResourceLocation WHITE =
            new ResourceLocation("minecraft", "textures/block/white_concrete.png");
    private static final ResourceLocation DIVE_RAY_RING = new ResourceLocation(
            StarFantasyGoetyMod.CONTENT_NAMESPACE,
            "textures/effect/hades_dive_ray_ring.png");
    private static final int SEGMENTS = 64;
    private static final int CLAW_COUNT = 3;
    private static final float SWEEP_TICKS = 5.0F;
    private static final float TRAIL_RADIANS = (float) Math.toRadians(105.0D);
    private static final float TWO_PI = (float) (Math.PI * 2.0D);
    private static final float WIDTH_SCALE = 20.0F;
    private static final float RECTANGLE_WIDTH_SCALE = 2.5F;
    private static final int RECTANGLE_SEGMENTS = 48;
    private static final float RECTANGLE_TRAIL_FRACTION = 0.70F;
    private static final int OUTER_COLOR = 0x4D00FF;
    private static final int BODY_COLOR = 0xB040FF;
    private static final int CORE_COLOR = 0xF8E9FF;
    private static final int LASER_OUTLINE_COLOR = 0xA840FF;
    private static final int LASER_RING_COLOR = 0xD58AFF;
    private static final int LASER_CORE_COLOR = 0x26005F;
    private static final int LASER_VISIBLE_TICKS = 20;
    private static final int LASER_EXTEND_TICKS = 10;
    private static final int LASER_RING_LIFETIME_TICKS = 20;
    private static final float LASER_ALPHA = 0.48F;

    public HadesClawSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 32.0F;
    }

    @Override
    public void m_7392_(HadesClawSlashEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.visualAge(partialTick);
        if (entity.isDiveRayLaser()) {
            renderDiveRayLaser(entity, age, poseStack, buffer);
        } else if (age >= 0.0F && age < HadesClawSlashEntity.LIFETIME_TICKS) {
            float sweep = Mth.m_14036_(age / SWEEP_TICKS, 0.0F, 1.0F) * TWO_PI;
            float fade = age <= SWEEP_TICKS ? 1.0F : Mth.m_14036_(
                    1.0F - (age - SWEEP_TICKS)
                            / (HadesClawSlashEntity.LIFETIME_TICKS - SWEEP_TICKS),
                    0.0F, 1.0F);
            fade *= fade;
            VertexConsumer consumer = buffer.m_6299_(ApollyonDeathLightRenderType.get());
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            if (entity.isRectangleSlash()) {
                renderRectangleClaws(consumer, matrix, entity, age, fade);
            } else {
                for (int claw = 0; claw < CLAW_COUNT; ++claw) {
                    float angularOffset = (claw - 1) * 0.035F;
                    renderLayeredArc(consumer, matrix, entity.radius(),
                            sweep + angularOffset, 1.0F + claw * 2.0F, fade);
                }
            }
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(HadesClawSlashEntity entity) {
        return WHITE;
    }

    /** Denia's layered laser-column visual, rotated onto Hades's horizontal ray. */
    private static void renderDiveRayLaser(
            HadesClawSlashEntity entity, float age, PoseStack poseStack,
            MultiBufferSource buffer) {
        float beamPulse = age <= LASER_VISIBLE_TICKS
                ? Mth.m_14031_(Mth.m_14036_(age / LASER_VISIBLE_TICKS,
                0.0F, 1.0F) * (float) Math.PI)
                : 0.0F;
        float width = entity.warningWidth() * beamPulse;
        float length = entity.warningLength() * Mth.m_14036_(
                age / LASER_EXTEND_TICKS, 0.0F, 1.0F);

        VertexConsumer consumer = buffer.m_6299_(
                StarFantasyVfxRenderTypes.translucentPositionColor());
        VertexConsumer ringConsumer = buffer.m_6299_(
                RenderType.m_234338_(DIVE_RAY_RING));
        poseStack.m_85836_();
        poseStack.m_252781_(rotationFromPositiveZ(entity.directionYaw()));
        if (beamPulse > 0.01F && width > 0.04F && length > 0.04F) {
            renderDiveRayBeam(entity, age, width, length, poseStack, consumer);
        }
        renderDiveRayRings(entity, age, poseStack, ringConsumer);
        poseStack.m_85849_();
    }

    private static Quaternionf rotationFromPositiveZ(float yawDegrees) {
        float yaw = yawDegrees * ((float) Math.PI / 180.0F);
        Vec3 direction = new Vec3(-Mth.m_14031_(yaw), 0.0D, Mth.m_14089_(yaw));
        return new Vector3f(0.0F, 0.0F, 1.0F).rotationTo(
                new Vector3f((float) direction.f_82479_, 0.0F,
                        (float) direction.f_82481_),
                new Quaternionf());
    }

    private static void renderDiveRayBeam(
            HadesClawSlashEntity entity, float age, float width, float length,
            PoseStack poseStack, VertexConsumer consumer) {
        float spin = age * 34.0F + (entity.m_19879_() & 0xFF);
        poseStack.m_85836_();
        poseStack.m_85837_(0.0D, 0.0D, length * 0.5D);
        poseStack.m_252781_(new Quaternionf().rotationZ(
                spin * ((float) Math.PI / 180.0F)));
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        float outlineAlpha = LASER_ALPHA * 0.5F;
        renderCenteredCuboid(consumer, matrix, width, width, length,
                LASER_OUTLINE_COLOR, outlineAlpha);
        renderCenteredCuboid(consumer, matrix, width * 0.82F, width * 0.82F,
                length * 1.01F, LASER_CORE_COLOR, LASER_ALPHA);
        renderCenteredCuboid(consumer, matrix, width * 1.03F, width * 0.12F,
                length * 1.02F, LASER_OUTLINE_COLOR, outlineAlpha);
        renderCenteredCuboid(consumer, matrix, width * 0.12F, width * 1.03F,
                length * 1.02F, LASER_OUTLINE_COLOR, outlineAlpha);
        poseStack.m_85849_();
    }

    private static void renderDiveRayRings(
            HadesClawSlashEntity entity, float age, PoseStack poseStack,
            VertexConsumer consumer) {
        int lastSpawnTick = Math.min(LASER_EXTEND_TICKS, (int) Math.floor(age));
        for (int spawnTick = 0; spawnTick <= lastSpawnTick; ++spawnTick) {
            float ringAge = age - spawnTick;
            if (ringAge < 0.0F || ringAge > LASER_RING_LIFETIME_TICKS) {
                continue;
            }
            float progress = Mth.m_14036_(
                    ringAge / LASER_RING_LIFETIME_TICKS, 0.0F, 1.0F);
            float ringPulse = Mth.m_14031_(progress * (float) Math.PI);
            float radius = entity.warningWidth() * 0.7F * ringPulse;
            float alpha = Mth.m_14036_(0.72F * ringPulse, 0.0F, 1.0F);
            if (radius <= 0.04F || alpha <= 0.01F) {
                continue;
            }
            float z = entity.warningLength() * Mth.m_14036_(
                    spawnTick / (float) LASER_EXTEND_TICKS, 0.0F, 1.0F);
            float rotation = age * 22.0F + spawnTick * 31.0F
                    + (entity.m_19879_() & 0xFF);
            poseStack.m_85836_();
            poseStack.m_85837_(0.0D, 0.0D, z);
            poseStack.m_252781_(new Quaternionf().rotationZ(
                    rotation * ((float) Math.PI / 180.0F)));
            renderTexturedRing(consumer, poseStack.m_85850_(), radius,
                    LASER_RING_COLOR, alpha);
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
        laserVertex(consumer, matrix, x1, y1, z1, color, alpha);
        laserVertex(consumer, matrix, x2, y2, z2, color, alpha);
        laserVertex(consumer, matrix, x3, y3, z3, color, alpha);
        laserVertex(consumer, matrix, x4, y4, z4, color, alpha);
    }

    private static void laserVertex(
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

    private static void renderLayeredArc(
            VertexConsumer consumer, Matrix4f matrix, float radius,
            float headAngle, float height, float fade) {
        renderArc(consumer, matrix, radius, headAngle, height,
                0.72F * WIDTH_SCALE, OUTER_COLOR, fade * 0.20F);
        renderArc(consumer, matrix, radius, headAngle, height + 0.01F,
                0.34F * WIDTH_SCALE, BODY_COLOR, fade * 0.60F);
        renderArc(consumer, matrix, radius, headAngle, height + 0.02F,
                0.09F * WIDTH_SCALE, CORE_COLOR, fade);
    }

    private static void renderRectangleClaws(
            VertexConsumer consumer, Matrix4f matrix,
            HadesClawSlashEntity entity, float age, float fade) {
        float head = Mth.m_14036_(age / SWEEP_TICKS, 0.0F, 1.0F);
        float tail = Math.max(0.0F, head - RECTANGLE_TRAIL_FRACTION);
        for (int claw = 0; claw < CLAW_COUNT; ++claw) {
            float laneOffset = (claw - 1) * 5.0F;
            renderLayeredRectangleArc(
                    consumer, matrix, entity.directionYaw(),
                    entity.warningWidth(), entity.warningLength(),
                    entity.curveSign(), laneOffset, tail, head, 1.0F, fade);
        }
    }

    private static void renderLayeredRectangleArc(
            VertexConsumer consumer, Matrix4f matrix,
            float yaw, float warningWidth, float warningLength, float curveSign,
            float laneOffset, float tail, float head, float height, float fade) {
        renderRectangleArc(consumer, matrix, yaw, warningWidth, warningLength,
                curveSign, laneOffset, tail, head, height,
                0.72F * RECTANGLE_WIDTH_SCALE, OUTER_COLOR, fade * 0.20F);
        renderRectangleArc(consumer, matrix, yaw, warningWidth, warningLength,
                curveSign, laneOffset, tail, head, height + 0.01F,
                0.34F * RECTANGLE_WIDTH_SCALE, BODY_COLOR, fade * 0.60F);
        renderRectangleArc(consumer, matrix, yaw, warningWidth, warningLength,
                curveSign, laneOffset, tail, head, height + 0.02F,
                0.09F * RECTANGLE_WIDTH_SCALE, CORE_COLOR, fade);
    }

    private static void renderRectangleArc(
            VertexConsumer consumer, Matrix4f matrix,
            float yaw, float warningWidth, float warningLength, float curveSign,
            float laneOffset, float tail, float head, float height,
            float width, int color, float alpha) {
        if (head <= tail || alpha <= 0.001F) {
            return;
        }
        int segmentCount = Math.max(1, Mth.m_14167_(
                RECTANGLE_SEGMENTS * (head - tail)));
        for (int segment = 0; segment < segmentCount; ++segment) {
            float u0 = segment / (float) segmentCount;
            float u1 = (segment + 1) / (float) segmentCount;
            float t0 = Mth.m_14179_(u0, tail, head);
            float t1 = Mth.m_14179_(u1, tail, head);
            CurveSample p0 = sampleRectangleArc(
                    t0, yaw, warningWidth, warningLength, curveSign, laneOffset);
            CurveSample p1 = sampleRectangleArc(
                    t1, yaw, warningWidth, warningLength, curveSign, laneOffset);
            float width0 = width * endpointTaper(u0);
            float width1 = width * endpointTaper(u1);
            float alpha0 = alpha * (0.20F + 0.80F * u0);
            float alpha1 = alpha * (0.20F + 0.80F * u1);
            curveVertex(consumer, matrix, p0, -width0, height, color, alpha0);
            curveVertex(consumer, matrix, p1, -width1, height, color, alpha1);
            curveVertex(consumer, matrix, p1, width1, height, color, alpha1);
            curveVertex(consumer, matrix, p0, width0, height, color, alpha0);
        }
    }

    private static CurveSample sampleRectangleArc(
            float t, float yawDegrees, float width, float length,
            float curveSign, float laneOffset) {
        float bend = width * 0.20F;
        float along = -length * 0.5F + length * t;
        float across = laneOffset + curveSign * bend * 4.0F * t * (1.0F - t);
        float alongDerivative = length;
        float acrossDerivative = curveSign * bend * 4.0F * (1.0F - 2.0F * t);

        float yaw = (float) Math.toRadians(yawDegrees);
        float forwardX = -Mth.m_14031_(yaw);
        float forwardZ = Mth.m_14089_(yaw);
        float rightX = -forwardZ;
        float rightZ = forwardX;
        float x = forwardX * along + rightX * across;
        float z = forwardZ * along + rightZ * across;
        float tangentX = forwardX * alongDerivative + rightX * acrossDerivative;
        float tangentZ = forwardZ * alongDerivative + rightZ * acrossDerivative;
        float tangentLength = Mth.m_14116_(
                tangentX * tangentX + tangentZ * tangentZ);
        if (tangentLength <= 1.0E-4F) {
            tangentLength = 1.0F;
        }
        return new CurveSample(x, z,
                -tangentZ / tangentLength, tangentX / tangentLength);
    }

    private static void curveVertex(
            VertexConsumer consumer, Matrix4f matrix, CurveSample point,
            float widthOffset, float height, int color, float alpha) {
        consumer.m_252986_(matrix,
                        point.x + point.normalX * widthOffset,
                        height,
                        point.z + point.normalZ * widthOffset)
                .m_85950_(red(color), green(color), blue(color),
                        Mth.m_14036_(alpha, 0.0F, 1.0F))
                .m_5752_();
    }

    private static void renderArc(
            VertexConsumer consumer, Matrix4f matrix, float radius,
            float headAngle, float height, float width, int color, float alpha) {
        if (headAngle <= 0.0F || alpha <= 0.001F) {
            return;
        }
        float tailAngle = Math.max(0.0F, headAngle - TRAIL_RADIANS);
        float visibleAngle = headAngle - tailAngle;
        int segmentCount = Math.max(1, Mth.m_14167_(
                SEGMENTS * visibleAngle / TWO_PI));
        for (int segment = 0; segment < segmentCount; ++segment) {
            float t0 = segment / (float) segmentCount;
            float t1 = (segment + 1) / (float) segmentCount;
            float angle0 = Mth.m_14179_(t0, tailAngle, headAngle);
            float angle1 = Mth.m_14179_(t1, tailAngle, headAngle);
            float width0 = width * endpointTaper(t0);
            float width1 = width * endpointTaper(t1);
            float alpha0 = alpha * (0.20F + 0.80F * t0);
            float alpha1 = alpha * (0.20F + 0.80F * t1);
            arcVertex(consumer, matrix, radius - width0, angle0, height, color, alpha0);
            arcVertex(consumer, matrix, radius - width1, angle1, height, color, alpha1);
            arcVertex(consumer, matrix, radius + width1, angle1, height, color, alpha1);
            arcVertex(consumer, matrix, radius + width0, angle0, height, color, alpha0);
        }
    }

    private static float endpointTaper(float t) {
        return (float) Math.pow(Math.max(0.0F,
                Math.sin(Math.PI * t)), 0.35D);
    }

    /** Angle zero is world north; increasing angle travels counter-clockwise. */
    private static void arcVertex(
            VertexConsumer consumer, Matrix4f matrix,
            float radius, float angle, float height, int color, float alpha) {
        float x = -Mth.m_14031_(angle) * radius;
        float z = -Mth.m_14089_(angle) * radius;
        consumer.m_252986_(matrix, x, height, z)
                .m_85950_(red(color), green(color), blue(color),
                        Mth.m_14036_(alpha, 0.0F, 1.0F))
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

    private record CurveSample(float x, float z, float normalX, float normalZ) {
    }
}
