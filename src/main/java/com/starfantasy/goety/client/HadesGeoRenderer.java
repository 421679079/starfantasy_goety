package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.HadesEntity;
import com.starfantasy.library.vfx.client.StarFantasyStarArrowVisualRenderer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.core.object.Color;

public final class HadesGeoRenderer extends GeoEntityRenderer<HadesEntity> {
    /*
     * Matches the two-times-scaled model's visible bounds, with a small margin
     * for animated bones at the edges.
     */
    private static final double CULLING_HALF_WIDTH = 48.0D;
    private static final double CULLING_MIN_Y_OFFSET = -7.0D;
    private static final double CULLING_MAX_Y_OFFSET = 43.0D;
    private static final Vec3 CHARGE_FOCUS = new Vec3(0.0D, 14.0D, 4.0D);
    private static final Vec3 DIVE_RAY_CHARGE_FOCUS = new Vec3(0.0D, 5.0D, 5.0D);
    private static final int CHARGE_TRAIL_COUNT = 56;
    private static final int CHARGE_TRAIL_LIFETIME = 24;
    private static final int CHARGE_STAR_AFTERGLOW_TICKS = 20;

    public HadesGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new HadesGeoModel());
        this.f_114477_ = 0.0F;
        this.withScale(2.0F);
    }

    @Override
    public Color getRenderColor(HadesEntity entity, float partialTick, int packedLight) {
        float opacity = entity.isPlayingDeathAnimation()
                ? deathOpacity(entity.getDeathAnimationTicks() + partialTick) : 1.0F;
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, opacity);
    }

    static float deathOpacity(float deathAge) {
        float progress = Mth.m_14036_(
                (deathAge - (HadesEntity.DEATH_ANIMATION_TICKS - 40)) / 40.0F, 0.0F, 1.0F);
        return 1.0F - 0.8F * progress;
    }

    @Override
    public RenderType getRenderType(HadesEntity entity, ResourceLocation texture,
                                    MultiBufferSource buffer, float partialTick) {
        return entity.isPlayingDeathAnimation()
                ? RenderType.m_110473_(texture)
                : super.getRenderType(entity, texture, buffer, partialTick);
    }

    @Override
    public void m_7392_(HadesEntity entity, float entityYaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        int afterglowAge = entity.chargeAfterglowAge();
        if (entity.isCharging()
                || (afterglowAge >= 0 && afterglowAge < CHARGE_TRAIL_LIFETIME)) {
            this.renderJudgmentCharge(
                    entity, partialTick, poseStack, buffer, afterglowAge);
        }
        int diveRayAfterglowAge = entity.diveRayShootAfterglowAge();
        if (entity.isDiveRayShootCharging()
                || (diveRayAfterglowAge >= 0
                && diveRayAfterglowAge < CHARGE_TRAIL_LIFETIME)) {
            this.renderCharge(
                    entity, partialTick, poseStack, buffer,
                    DIVE_RAY_CHARGE_FOCUS, diveRayAfterglowAge,
                    0.58F, 0.18F, 1.0F, 2.0F / 3.0F);
        }
        ApollyonDeathLight.renderHades(entity, partialTick, poseStack, buffer);
    }

    @Override
    public boolean m_5523_(HadesEntity entity, Frustum frustum,
                           double cameraX, double cameraY, double cameraZ) {
        if (super.m_5523_(entity, frustum, cameraX, cameraY, cameraZ)) {
            return true;
        }
        if (!entity.m_6000_(cameraX, cameraY, cameraZ)) {
            return false;
        }

        AABB visualBounds = new AABB(
                entity.m_20185_() - CULLING_HALF_WIDTH,
                entity.m_20186_() + CULLING_MIN_Y_OFFSET,
                entity.m_20189_() - CULLING_HALF_WIDTH,
                entity.m_20185_() + CULLING_HALF_WIDTH,
                entity.m_20186_() + CULLING_MAX_Y_OFFSET,
                entity.m_20189_() + CULLING_HALF_WIDTH);
        return frustum.m_113029_(visualBounds);
    }

    private void renderJudgmentCharge(
            HadesEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int afterglowAge) {
        this.renderCharge(
                entity, partialTick, poseStack, buffer,
                CHARGE_FOCUS, afterglowAge,
                0.58F, 0.18F, 1.0F, 1.0F);
    }

    static void renderCharge(
            net.minecraft.world.entity.Entity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, Vec3 focus, int afterglowAge,
            float red, float green, float blue, float visualScale) {
        float age = entity.f_19797_ + partialTick;
        float afterglow = afterglowAge < 0 ? 0.0F : afterglowAge + partialTick;
        if (afterglowAge < 0 || afterglow < CHARGE_STAR_AFTERGLOW_TICKS) {
            float fade = afterglowAge < 0
                    ? 1.0F
                    : 1.0F - afterglow / CHARGE_STAR_AFTERGLOW_TICKS;
            float pulse = (1.25F + 0.18F * Mth.m_14089_(age * 0.32F))
                    * 25.0F * visualScale * fade;
            StarFantasyStarArrowVisualRenderer.renderCenterStar(
                    entity.m_19879_(), entity.f_19797_, focus, partialTick,
                    Minecraft.m_91087_().f_91063_.m_109153_(), poseStack, buffer,
                    red, green, blue, pulse, 7.0F);
        }

        for (int i = 0; i < CHARGE_TRAIL_COUNT; ++i) {
            float phaseOffset = i * 5.25F;
            float chargeAge = afterglowAge < 0 ? age : age - afterglow;
            float trailAge = positiveModulo(
                    chargeAge + phaseOffset, CHARGE_TRAIL_LIFETIME) + afterglow;
            if (trailAge >= CHARGE_TRAIL_LIFETIME) {
                continue;
            }
            float progress = trailAge / CHARGE_TRAIL_LIFETIME;
            float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
            float previousProgress = Math.max(0.0F, progress - 0.20F);
            float previousEased = 1.0F
                    - (1.0F - previousProgress) * (1.0F - previousProgress);
            long cycle = Mth.m_14107_((chargeAge + phaseOffset) / CHARGE_TRAIL_LIFETIME);
            long seed = mix(entity.m_19879_() * 73428767L + i * 912931L + cycle * 19349663L);
            double radius = (6.0D + random01(seed + 11L) * 8.0D)
                    * 25.0D * visualScale;
            Vec3 start = focus.m_82549_(randomUnit(seed).m_82490_(radius));
            Vec3 head = start.m_165921_(focus, eased);
            Vec3 tail = start.m_165921_(focus, previousEased);
            float alpha = 0.28F + (1.0F - progress) * 0.62F;
            StarFantasyStarArrowVisualRenderer.renderWorldTrailDepthTested(
                    List.of(tail, head), poseStack, buffer,
                    red, green, blue, alpha, 3.0F * visualScale);
        }
    }

    private static float positiveModulo(float value, float modulus) {
        float result = value % modulus;
        return result < 0.0F ? result + modulus : result;
    }

    private static Vec3 randomUnit(long seed) {
        double theta = random01(seed) * Math.PI * 2.0D;
        double y = random01(seed + 0x9E3779B97F4A7C15L) * 2.0D - 1.0D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        return new Vec3(Math.cos(theta) * horizontal, y, Math.sin(theta) * horizontal);
    }

    private static double random01(long seed) {
        long mixed = mix(seed);
        return ((mixed >>> 11) & ((1L << 53) - 1)) * 0x1.0p-53;
    }

    private static long mix(long value) {
        long mixed = value + 0x9E3779B97F4A7C15L;
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        return mixed ^ (mixed >>> 31);
    }
}
