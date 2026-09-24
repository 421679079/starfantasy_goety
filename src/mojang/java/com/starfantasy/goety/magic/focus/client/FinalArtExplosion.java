package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** SlashBlade's procedural Final Art burst, local to Goety and texture-free.
 * Copyright (c) 2026 Funits and StarFantasy SlashBlade Addon contributors.
 * MIT license: THIRD_PARTY/StarFantasy_SlashBlade/LICENSE.txt.
 */
public final class FinalArtExplosion {
    private static final float TAU = (float) (Math.PI * 2.0D);
    private static final int SPHERE_RINGS = 14, SPHERE_SEGMENTS = 36, BAND_SEGMENTS = 96;

    public static void render(float age, float seed, float progress, PoseStack poses, MultiBufferSource buffer) {
        if (StarFantasyShaderCompat.isRenderingShaderShadowPass()) return;
        progress = Mth.clamp(progress, 0F, 1F);
        float scale = 15F * (1F - (float) Math.pow(1F - progress, 3D));
        renderBurstVisual(age, seed, scale, 1F - progress, poses, buffer);
    }

    public static void renderBurstVisual(float age, float seed, float scale, float alpha,
                                         PoseStack poseStack, MultiBufferSource buffer) {
        if (scale <= 0.002F || alpha <= 0.002F) {
            return;
        }
        poseStack.pushPose();
        FinalArtExplosion.renderProceduralCore(age, seed, scale, alpha, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderProceduralCore(float age, float seed, float scale, float alpha,
                                             PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.scale(scale, scale, scale);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer core = buffer.getBuffer(FinalArtExplosionRenderTypes.blackHoleSurface());
        VertexConsumer overlay = buffer.getBuffer(FinalArtExplosionRenderTypes.blackHoleOverlay());

        FinalArtExplosion.renderCore(core, matrix, alpha);
        FinalArtExplosion.renderGlowSphere(overlay, matrix, 0.58F, 0.12F, 0.42F, 1.00F, alpha * 0.42F);
        FinalArtExplosion.renderGlowSphere(overlay, matrix, 0.74F, 0.05F, 0.24F, 0.82F, alpha * 0.24F);
        FinalArtExplosion.renderAccretionBands(overlay, matrix, age, seed, alpha);
    }

    private static void renderCore(VertexConsumer consumer, Matrix4f matrix, float alpha) {
        FinalArtExplosion.renderSphere(consumer, matrix, 0.46F, 0.0F, 0.0F, 0.0F, alpha, true);
    }

    private static void renderGlowSphere(VertexConsumer consumer, Matrix4f matrix, float radius,
                                         float red, float green, float blue, float alpha) {
        FinalArtExplosion.renderSphere(consumer, matrix, radius, red, green, blue, alpha, false);
    }

    private static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float radius,
                                     float red, float green, float blue, float alpha, boolean core) {
        for (int ring = 0; ring < SPHERE_RINGS; ring++) {
            float theta0 = -Mth.HALF_PI + Mth.PI * ring / SPHERE_RINGS;
            float theta1 = -Mth.HALF_PI + Mth.PI * (ring + 1) / SPHERE_RINGS;
            for (int segment = 0; segment < SPHERE_SEGMENTS; segment++) {
                float phi0 = TAU * segment / SPHERE_SEGMENTS;
                float phi1 = TAU * (segment + 1) / SPHERE_SEGMENTS;
                Point3 p00 = FinalArtExplosion.spherePoint(theta0, phi0, radius);
                Point3 p01 = FinalArtExplosion.spherePoint(theta0, phi1, radius);
                Point3 p11 = FinalArtExplosion.spherePoint(theta1, phi1, radius);
                Point3 p10 = FinalArtExplosion.spherePoint(theta1, phi0, radius);

                FinalArtExplosion.sphereVertex(consumer, matrix, p00, radius, red, green, blue, alpha, core);
                FinalArtExplosion.sphereVertex(consumer, matrix, p01, radius, red, green, blue, alpha, core);
                FinalArtExplosion.sphereVertex(consumer, matrix, p11, radius, red, green, blue, alpha, core);
                FinalArtExplosion.sphereVertex(consumer, matrix, p10, radius, red, green, blue, alpha, core);
            }
        }
    }

    private static void renderAccretionBands(VertexConsumer consumer, Matrix4f matrix, float age, float seed, float alpha) {
        float phase = seed * 0.01F + Mth.sin(age * 0.06F + seed) * 0.06F;
        float bandPulse = 0.92F + 0.08F * Mth.sin(age * 0.14F + seed * 0.31F);
        float tilt = 0.0F;
        FinalArtExplosion.renderBand(consumer, matrix, 0.48F, 1.24F, 0.0F, tilt, phase,
                0.22F, 0.58F, 1.00F, alpha * 1.00F * bandPulse);
        FinalArtExplosion.renderBand(consumer, matrix, 0.50F, 0.94F, 0.0F, tilt, phase + 0.8F,
                0.90F, 1.00F, 1.00F, alpha * bandPulse);
        FinalArtExplosion.renderBand(consumer, matrix, 0.62F, 1.55F, 0.0F, tilt, -phase * 0.5F,
                0.12F, 0.32F, 0.82F, alpha * 0.88F * bandPulse);
    }

    private static void renderBand(VertexConsumer consumer, Matrix4f matrix,
                                   float innerRadius, float outerRadius, float halfHeight,
                                   float tilt, float phase,
                                   float red, float green, float blue, float alpha) {
        for (int i = 0; i < BAND_SEGMENTS; i++) {
            float a0 = TAU * i / BAND_SEGMENTS + phase;
            float a1 = TAU * (i + 1) / BAND_SEGMENTS + phase;
            float wave = 0.82F + 0.18F * Mth.sin(a0 * 3.0F - phase * 4.0F);
            float localAlpha = alpha * wave;

            float planeY0 = Mth.sin(a0 * 2.0F + phase) * halfHeight;
            float planeY1 = Mth.sin(a1 * 2.0F + phase) * halfHeight;
            Point3 p00 = FinalArtExplosion.bandPoint(a0, innerRadius, planeY0, tilt);
            Point3 p01 = FinalArtExplosion.bandPoint(a1, innerRadius, planeY1, tilt);
            Point3 p11 = FinalArtExplosion.bandPoint(a1, outerRadius, planeY1, tilt);
            Point3 p10 = FinalArtExplosion.bandPoint(a0, outerRadius, planeY0, tilt);
            FinalArtExplosion.vertex(consumer, matrix, p00, red, green, blue, localAlpha * 1.18F);
            FinalArtExplosion.vertex(consumer, matrix, p01, red, green, blue, localAlpha * 1.18F);
            FinalArtExplosion.vertex(consumer, matrix, p11, red * 0.62F, green * 0.78F, blue, localAlpha * 0.88F);
            FinalArtExplosion.vertex(consumer, matrix, p10, red * 0.62F, green * 0.78F, blue, localAlpha * 0.88F);
        }
    }

    private static Point3 spherePoint(float theta, float phi, float radius) {
        float cosTheta = Mth.cos(theta);
        return new Point3(
                Mth.cos(phi) * cosTheta * radius,
                Mth.sin(theta) * radius,
                Mth.sin(phi) * cosTheta * radius);
    }

    private static Point3 bandPoint(float angle, float radius, float y, float tilt) {
        float x = Mth.cos(angle) * radius;
        float z = Mth.sin(angle) * radius;
        float sinTilt = Mth.sin(tilt);
        float cosTilt = Mth.cos(tilt);
        return new Point3(x, y * cosTilt - z * sinTilt, z * cosTilt + y * sinTilt);
    }

    private static void sphereVertex(VertexConsumer consumer, Matrix4f matrix, Point3 point, float radius,
                                     float red, float green, float blue, float alpha, boolean core) {
        float rim = Mth.clamp((Mth.sqrt(point.x * point.x + point.z * point.z) / radius - 0.45F) / 0.55F, 0.0F, 1.0F);
        float top = Mth.clamp(point.y / radius * 0.5F + 0.5F, 0.0F, 1.0F);
        if (core) {
            float shade = 0.004F + rim * rim * 0.012F + top * 0.004F;
            FinalArtExplosion.vertex(consumer, matrix, point, shade * 0.14F, shade * 0.22F, shade, alpha);
            return;
        }

        float glowAlpha = alpha * (0.18F + rim * rim * 0.92F);
        FinalArtExplosion.vertex(consumer, matrix, point, red, green, blue, glowAlpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Point3 point,
                               float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, point.x, point.y, point.z)
                .color(Mth.clamp(red, 0.0F, 1.0F), Mth.clamp(green, 0.0F, 1.0F),
                        Mth.clamp(blue, 0.0F, 1.0F), Mth.clamp(alpha, 0.0F, 1.0F))
                .endVertex();
    }

    private record Point3(float x, float y, float z) {
    }

    private FinalArtExplosion() {}
}
