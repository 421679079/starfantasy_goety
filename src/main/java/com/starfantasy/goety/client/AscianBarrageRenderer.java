/* Adapted from Star Fantasy Soulsweapons Tweaks 0.1.122 (MIT).
 * Copyright (c) 2026 Star Fantasy. See THIRD_PARTY/Soulsweapons_Tweaks/LICENSE.txt.
 */
package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import com.starfantasy.goety.entity.AscianProjectileEntity;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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

public class AscianBarrageRenderer
extends EntityRenderer<AscianProjectileEntity> {
    private static final ResourceLocation FIRE_TEXTURE = new ResourceLocation("minecraft", "textures/item/fire_charge.png");
    private static final ResourceLocation FROST_TEXTURE = new ResourceLocation("starfantasy_goety", "textures/entity/chaos_frost_meteor.png");
    private static final ResourceLocation SHOCKWAVE_TEXTURE = new ResourceLocation("starfantasy_goety", "textures/particle/night_lightning/thunder_shockwave_ring.png");
    private static final ResourceLocation ENERGY_CORE_TEXTURE = new ResourceLocation("starfantasy_goety", "textures/particle/night_lightning/lightning_ball_core.png");
    private static final ResourceLocation NIGHT_TEXTURE = new ResourceLocation("minecraft", "textures/entity/enderdragon/dragon_fireball.png");
    private static final ResourceLocation WITHER_SKULL_TEXTURE = new ResourceLocation("minecraft", "textures/item/wither_skeleton_skull.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Vec3 LIGHTNING_RIGHT_SEED = new Vec3(0.45, 0.82, 0.31);

    public AscianBarrageRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void m_7392_(AscianProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.style() == 3) {
            AscianBarrageRenderer.renderLightningProjectile(entity, partialTick, poseStack, buffer);
            super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        poseStack.m_85836_();
        poseStack.m_252781_(this.f_114476_.m_253208_());
        poseStack.m_252781_(Axis.f_252436_.m_252977_(180.0f));
        float scale = AscianBarrageRenderer.scaleFor(entity);
        poseStack.m_85841_(scale, scale, scale);
        VertexConsumer consumer = buffer.m_6299_(RenderType.m_110458_((ResourceLocation)this.m_5478_(entity)));
        PoseStack.Pose pose = poseStack.m_85850_();
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        AscianBarrageRenderer.vertex(consumer, matrix, normal, packedLight, -0.5f, -0.5f, 0.0f, 1.0f);
        AscianBarrageRenderer.vertex(consumer, matrix, normal, packedLight, 0.5f, -0.5f, 1.0f, 1.0f);
        AscianBarrageRenderer.vertex(consumer, matrix, normal, packedLight, 0.5f, 0.5f, 1.0f, 0.0f);
        AscianBarrageRenderer.vertex(consumer, matrix, normal, packedLight, -0.5f, 0.5f, 0.0f, 0.0f);
        poseStack.m_85849_();
        super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    public ResourceLocation m_5478_(AscianProjectileEntity entity) {
        return switch (entity.style()) {
            case 2 -> FROST_TEXTURE;
            case 3 -> ENERGY_CORE_TEXTURE;
            case 4 -> NIGHT_TEXTURE;
            case 5 -> WITHER_SKULL_TEXTURE;
            default -> FIRE_TEXTURE;
        };
    }

    private static float scaleFor(AscianProjectileEntity entity) {
        return switch (entity.style()) {
            case 3 -> 1.9f;
            case 5 -> 1.45f;
            case 2, 4 -> 1.5f;
            default -> 1.35f;
        };
    }

    private static void renderLightningProjectile(AscianProjectileEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        float age = (float)entity.f_19797_ + partialTick;
        Camera camera = Minecraft.m_91087_().f_91063_.m_109153_();
        poseStack.m_85836_();
        poseStack.m_252781_(camera.m_253121_());
        VertexConsumer shockwaveConsumer = buffer.m_6299_(RenderType.m_110473_((ResourceLocation)SHOCKWAVE_TEXTURE));
        poseStack.m_85836_();
        poseStack.m_252781_(Axis.f_252403_.m_252977_(age * 54.0f + (float)entity.m_19879_() * 13.0f));
        AscianBarrageRenderer.renderBillboardQuad(poseStack, shockwaveConsumer, 1.02f, 0.35f, 0.78f, 1.0f, 0.42f);
        poseStack.m_85849_();
        poseStack.m_85836_();
        poseStack.m_252781_(Axis.f_252403_.m_252977_(-age * 82.0f + (float)entity.m_19879_() * 7.0f));
        AscianBarrageRenderer.renderBillboardQuad(poseStack, shockwaveConsumer, 0.68f, 0.75f, 0.95f, 1.0f, 0.64f);
        poseStack.m_85849_();
        VertexConsumer coreConsumer = buffer.m_6299_(RenderType.m_110473_((ResourceLocation)ENERGY_CORE_TEXTURE));
        float pulse = 1.0f + Mth.m_14031_((float)(age * 0.9f + (float)entity.m_19879_() * 0.4f)) * 0.08f;
        poseStack.m_85836_();
        poseStack.m_252781_(Axis.f_252403_.m_252977_(age * 96.0f + (float)entity.m_19879_() * 11.0f));
        AscianBarrageRenderer.renderBillboardQuad(poseStack, coreConsumer, 0.66f * pulse, 0.92f, 1.0f, 1.0f, 0.96f);
        poseStack.m_85849_();
        poseStack.m_85836_();
        poseStack.m_252781_(Axis.f_252403_.m_252977_(-age * 128.0f + (float)entity.m_19879_() * 5.0f));
        AscianBarrageRenderer.renderBillboardQuad(poseStack, coreConsumer, 1.0f * pulse, 0.2f, 0.58f, 1.0f, 0.34f);
        poseStack.m_85849_();
        poseStack.m_85849_();
        AscianBarrageRenderer.renderProjectileLightning(entity, age, poseStack, buffer);
    }

    private static void renderProjectileLightning(AscianProjectileEntity entity, float age, PoseStack poseStack, MultiBufferSource buffer) {
        int refreshFrame = Math.max(0, (int)age / 2);
        VertexConsumer consumer = buffer.m_6299_(RenderType.m_110502_());
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        for (int i = 0; i < 7; ++i) {
            Random random = new Random(AscianBarrageRenderer.lightningSeed(entity.m_19879_(), refreshFrame, i));
            Vec3 start = AscianBarrageRenderer.randomLightningPoint(random, 0.12f, 0.42f);
            Vec3 end = AscianBarrageRenderer.randomLightningPoint(random, 0.72f, 1.28f);
            int segments = 5 + random.nextInt(4);
            Vec3[] path = AscianBarrageRenderer.buildLightningPath(start, end, segments, 0.2f, random);
            float alpha = Mth.m_14179_((float)random.nextFloat(), (float)0.62f, (float)0.95f);
            float width = Mth.m_14179_((float)random.nextFloat(), (float)0.035f, (float)0.065f);
            AscianBarrageRenderer.renderLightningPath(matrix, consumer, path, width, alpha);
        }
    }

    private static Vec3 randomLightningPoint(Random random, float minRadius, float maxRadius) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        double radius = Mth.m_14179_((float)random.nextFloat(), (float)minRadius, (float)maxRadius);
        double y = Mth.m_14179_((float)random.nextFloat(), (float)-0.55f, (float)0.55f);
        return new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static long lightningSeed(int entityId, int refreshFrame, int index) {
        long seed = -7046029254386353131L;
        seed ^= (long)entityId * -4658895280553007687L;
        seed ^= (long)refreshFrame * -7723592293110705685L;
        seed ^= (long)index * -2960836687051489901L;
        seed ^= seed >>> 30;
        seed *= -4658895280553007687L;
        seed ^= seed >>> 27;
        return seed;
    }

    private static Vec3[] buildLightningPath(Vec3 start, Vec3 end, int segments, float spreadFactor, Random random) {
        Vec3[] points = new Vec3[segments + 1];
        Vec3 diff = end.m_82546_(start);
        double length = Math.max(0.001, diff.m_82553_());
        Vec3 previousOffset = Vec3.f_82478_;
        for (int i = 0; i <= segments; ++i) {
            float progress = (float)i / (float)segments;
            if (i == 0) {
                points[i] = start;
                continue;
            }
            if (i == segments) {
                points[i] = end;
                continue;
            }
            Vec3 randomVector = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5);
            Vec3 perpendicular = diff.m_82537_(randomVector);
            if (perpendicular.m_82556_() < 1.0E-5) {
                perpendicular = diff.m_82537_(new Vec3(0.0, 1.0, 0.0));
            }
            if (perpendicular.m_82556_() < 1.0E-5) {
                perpendicular = new Vec3(1.0, 0.0, 0.0);
            }
            double spread = Math.sin(Math.PI * (double)progress) * length * (double)spreadFactor * (double)Mth.m_14179_((float)random.nextFloat(), (float)0.35f, (float)1.0f);
            previousOffset = previousOffset.m_82490_(0.48).m_82549_(perpendicular.m_82541_().m_82490_(spread));
            points[i] = start.m_82549_(diff.m_82490_((double)progress)).m_82549_(previousOffset);
        }
        return points;
    }

    private static void renderLightningPath(Matrix4f matrix, VertexConsumer consumer, Vec3[] points, float width, float alpha) {
        for (int i = 1; i < points.length; ++i) {
            float progress = (float)i / (float)(points.length - 1);
            float taper = 0.42f + (1.0f - progress) * 0.58f;
            AscianBarrageRenderer.renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper * 2.3f, 0.28f, 0.66f, 1.0f, alpha * 0.28f);
            AscianBarrageRenderer.renderBoltSegment(matrix, consumer, points[i - 1], points[i], width * taper, 0.88f, 0.98f, 1.0f, alpha);
        }
    }

    private static void renderBoltSegment(Matrix4f matrix, VertexConsumer consumer, Vec3 start, Vec3 end, float width, float red, float green, float blue, float alpha) {
        if (width <= 0.0f || alpha <= 0.0f) {
            return;
        }
        Vec3 diff = end.m_82546_(start);
        if (diff.m_82556_() < 1.0E-6) {
            return;
        }
        Vec3 right = diff.m_82537_(LIGHTNING_RIGHT_SEED);
        if (right.m_82556_() < 1.0E-6) {
            right = diff.m_82537_(new Vec3(0.0, 1.0, 0.0));
        }
        if (right.m_82556_() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.m_82541_().m_82490_((double)width);
        Vec3 back = diff.m_82537_(right).m_82541_().m_82490_((double)width);
        AscianBarrageRenderer.renderBoltQuad(matrix, consumer, start.m_82549_(right), start.m_82546_(right), end.m_82546_(right), end.m_82549_(right), red, green, blue, alpha);
        AscianBarrageRenderer.renderBoltQuad(matrix, consumer, start.m_82549_(back), start.m_82546_(back), end.m_82546_(back), end.m_82549_(back), red, green, blue, alpha * 0.72f);
    }

    private static void renderBoltQuad(Matrix4f matrix, VertexConsumer consumer, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float red, float green, float blue, float alpha) {
        AscianBarrageRenderer.boltVertex(matrix, consumer, a, red, green, blue, alpha);
        AscianBarrageRenderer.boltVertex(matrix, consumer, b, red, green, blue, alpha);
        AscianBarrageRenderer.boltVertex(matrix, consumer, c, red, green, blue, alpha);
        AscianBarrageRenderer.boltVertex(matrix, consumer, d, red, green, blue, alpha);
    }

    private static void boltVertex(Matrix4f matrix, VertexConsumer consumer, Vec3 point, float red, float green, float blue, float alpha) {
        consumer.m_252986_(matrix, (float)point.f_82479_, (float)point.f_82480_, (float)point.f_82481_).m_85950_(red, green, blue, alpha).m_5752_();
    }

    private static void renderBillboardQuad(PoseStack poseStack, VertexConsumer consumer, float halfSize, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.m_85850_();
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        AscianBarrageRenderer.texturedVertex(consumer, matrix, normal, -halfSize, -halfSize, 0.0f, 0.0f, 1.0f, red, green, blue, alpha);
        AscianBarrageRenderer.texturedVertex(consumer, matrix, normal, halfSize, -halfSize, 0.0f, 1.0f, 1.0f, red, green, blue, alpha);
        AscianBarrageRenderer.texturedVertex(consumer, matrix, normal, halfSize, halfSize, 0.0f, 1.0f, 0.0f, red, green, blue, alpha);
        AscianBarrageRenderer.texturedVertex(consumer, matrix, normal, -halfSize, halfSize, 0.0f, 0.0f, 0.0f, red, green, blue, alpha);
    }

    private static void texturedVertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, float red, float green, float blue, float alpha) {
        consumer.m_252986_(matrix, x, y, z).m_85950_(red, green, blue, alpha).m_7421_(u, v).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int packedLight, float x, float y, float u, float v) {
        consumer.m_252986_(matrix, x, y, 0.0f).m_6122_(255, 255, 255, 255).m_7421_(u, v).m_86008_(OverlayTexture.f_118083_).m_85969_(packedLight).m_252939_(normal, 0.0f, 1.0f, 0.0f).m_5752_();
    }
}
