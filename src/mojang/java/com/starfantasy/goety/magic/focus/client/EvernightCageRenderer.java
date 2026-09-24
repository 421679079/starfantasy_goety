package com.starfantasy.goety.magic.focus.client;

import com.starfantasy.library.vfx.StarFantasyCageGeometry;

import com.mojang.blaze3d.vertex.*;
import com.starfantasy.library.vfx.client.*;
import com.starfantasy.goety.magic.focus.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class EvernightCageRenderer extends EntityRenderer<EvernightCageEntity> {
    private static final ResourceLocation FLOWER = new ResourceLocation("star_fantasy_library", "textures/effect/cage_bloom.png");
    private static final ResourceLocation STAR = StarFantasyStarArrowVisualRenderer.starTexture();
    private static final ResourceLocation[] BLACK_FIRE = java.util.stream.IntStream.range(0, StarFantasyCageGeometry.BLACK_FIRE_FRAMES)
            .mapToObj(i -> new ResourceLocation("star_fantasy_library", "textures/effect/black_fire/black_fire_0" + i + ".png"))
            .toArray(ResourceLocation[]::new);
    public EvernightCageRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(EvernightCageEntity entity) { return FLOWER; }
    @Override public boolean shouldRender(EvernightCageEntity entity, Frustum frustum, double x, double y, double z) {
        return frustum.isVisible(entity.getBoundingBox().inflate(
                StarFantasyCageGeometry.FINAL_BLACK_FIRE_SCALE * 1.5 + StarFantasyCageGeometry.HEIGHT * .5));
    }
    @Override public void render(EvernightCageEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
        if (StarFantasyShaderCompat.isRenderingShaderShadowPass()) return;
        if (StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(poses, (restored, deferred) -> render(entity, yaw, partial, restored, deferred, light));
            return;
        }
        float age = entity.visualAge(partial);
        float fade = Mth.clamp((StarFantasyCageGeometry.LIFETIME - age) / 12F, 0, 1);
        if (fade <= 0) return;
        Vec3 camera = entityRenderDispatcher.camera.getPosition().subtract(entity.position());
        Matrix4f pose = poses.last().pose();
        VertexConsumer mesh = buffers.getBuffer(StarFantasyVfxRenderTypes.translucentPositionColor());
        int color = entity.color();
        double growth = Mth.clamp(age / StarFantasyCageGeometry.GROW_TICKS, 0, 1);
        // Alternating helices meet at the crown, revealed from the ground upward.
        for (int root = 0; root < StarFantasyCageGeometry.ROOTS; root++) {
            Vec3[] points = new Vec3[49];
            for (int i = 0; i < points.length; i++) points[i] = local(entity, StarFantasyCageGeometry.root(root, i / 48.0 * growth));
            strip(mesh, pose, points, camera, .20, color, fade * .17F, 0);
            strip(mesh, pose, points, camera, .065, color, fade * .74F, .16F);
            strip(mesh, pose, points, camera, .018, color, fade * .92F, .62F);
        }
        // Interwoven belts make the volume readable from low viewing angles.
        for (int belt = 0; belt < 2; belt++) {
            float beltFade = Mth.clamp((age - 12 - belt * 6) / 12, 0, 1) * fade;
            if (beltFade <= 0) continue;
            Vec3[] points = new Vec3[73];
            for (int i = 0; i < points.length; i++) {
                double angle = i / 72.0 * Math.PI * 2;
                points[i] = local(entity, StarFantasyCageGeometry.belt(belt, angle));
            }
            strip(mesh, pose, points, camera, .085, color, beltFade * .55F, .1F);
            strip(mesh, pose, points, camera, .018, color, beltFade * .88F, .6F);
        }
        for (int i = 0; i < StarFantasyCageGeometry.BLOOMS; i++) {
            if (age >= StarFantasyCageGeometry.burstTick(i)) continue;
            Vec3 head = local(entity, StarFantasyCageGeometry.bloom(i, age));
            double flightAge = Math.min(age, StarFantasyCageGeometry.arrivalTick(i));
            Vec3 direction = local(entity, StarFantasyCageGeometry.bloom(i, flightAge + .1))
                    .subtract(local(entity, StarFantasyCageGeometry.bloom(i, Math.max(0, flightAge - .1)))).normalize();
            if (direction.lengthSqr() < .001) direction = new Vec3(0, 1, 0);
            Vec3 body = head.subtract(direction.scale(.55));
            sphere(mesh, pose, body, .44, color, .55F);
            Vec3 side = direction.cross(new Vec3(0, 1, 0)).normalize();
            if (side.lengthSqr() < .01) side = new Vec3(1, 0, 0);
            Vec3 up = side.cross(direction).normalize();
            // Four tendrils extend opposite the tangent, never in front of the flower.
            for (int arm = 0; arm < 4; arm++) {
                double angle = arm * Math.PI / 2;
                Vec3 radial = side.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
                Vec3[] points = new Vec3[25];
                for (int p = 0; p < points.length; p++) {
                    double t = p / 24.0;
                    double wave = Math.sin(t * 7 - age * .27 + i + arm) * .24 * t;
                    points[p] = body.subtract(direction.scale(t * 3.1))
                            .add(radial.scale(.25 + t * .65 + wave)).add(up.scale(Math.sin(t * 5 - age * .18) * .18 * t));
                    // Bend the hanging tips above the launch floor while gathering close to the ground.
                    points[p] = new Vec3(points[p].x, Math.max(.15, points[p].y), points[p].z);
                }
                strip(mesh, pose, points, camera, .09, color, .35F, 0);
                strip(mesh, pose, points, camera, .025, color, .85F, .5F);
            }
            // Sample history at the head's fractional age so the trail cannot jump ahead.
            Vec3[] trail = new Vec3[25];
            for (int p = 0; p < trail.length; p++) trail[p] = local(entity, StarFantasyCageGeometry.bloom(i, Math.max(0, age - p * .3)));
            strip(mesh, pose, trail, camera, .14, color, .26F, .12F);
        }
        for (int i = 0; i < StarFantasyCageGeometry.BLOOMS; i++) {
            if (age >= StarFantasyCageGeometry.burstTick(i)) continue;
            Vec3 head = local(entity, StarFantasyCageGeometry.bloom(i, age));
            poses.pushPose(); poses.translate(head.x, head.y, head.z);
            poses.mulPose(entityRenderDispatcher.cameraOrientation());
            poses.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i * 72 + age * 1.8F));
            var petals = buffers.getBuffer(StarFantasyVfxRenderTypes.depthParticle(FLOWER));
            flower(petals, poses.last().pose(), 1.3F, color, .22F);
            flower(petals, poses.last().pose(), 1F, color, 1F);
            poses.popPose();
        }
        // Each bloom flashes for ten ticks starting at its own staggered arrival.
        // Use the actual star-arrow texture with scene depth, keeping the shared renderer unchanged.
        for (int i = 0; i < StarFantasyCageGeometry.BLOOMS; i++) {
            float chargeScale = StarFantasyCageGeometry.chargeScale(i, age);
            if (chargeScale <= 0) continue;
            Vec3 head = local(entity, StarFantasyCageGeometry.impact(i));
            poses.pushPose(); poses.translate(head.x, head.y, head.z);
            poses.mulPose(entityRenderDispatcher.cameraOrientation());
            poses.translate(0, 0, .025);
            poses.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(i * 72
                    + (age - StarFantasyCageGeometry.arrivalTick(i)) * 90F / StarFantasyCageGeometry.CHARGE_TICKS));
            var star = buffers.getBuffer(StarFantasyVfxRenderTypes.depthParticle(STAR));
            flower(star, poses.last().pose(), chargeScale, 0xFFFFFF, 1F);
            poses.popPose();
        }
        for (int i = 0; i < StarFantasyCageGeometry.BLOOMS; i++) {
            int frame = StarFantasyCageGeometry.blackFireFrame(i, age);
            if (frame < 0) continue;
            Vec3 center = local(entity, StarFantasyCageGeometry.impact(i));
            blackFire(poses, buffers, center, frame, StarFantasyCageGeometry.BLACK_FIRE_SCALE, color);
        }
        int finalFrame = StarFantasyCageGeometry.finalBlackFireFrame(age);
        if (finalFrame >= 0) blackFire(poses, buffers, local(entity, StarFantasyCageGeometry.finalBurstCenter()),
                finalFrame, StarFantasyCageGeometry.FINAL_BLACK_FIRE_SCALE, color);
    }
    private void blackFire(PoseStack poses, MultiBufferSource buffers, Vec3 center, int frame, float scale, int color) {
        poses.pushPose(); poses.translate(center.x, center.y, center.z);
        poses.mulPose(entityRenderDispatcher.cameraOrientation());
        var burst = buffers.getBuffer(StarFantasyVfxRenderTypes.depthParticle(BLACK_FIRE[frame]));
        flower(burst, poses.last().pose(), scale, color, 1F);
        poses.popPose();
    }
    private static Vec3 local(EvernightCageEntity entity, Vec3 point) {
        return StarFantasyCageGeometry.toWorld(point, Vec3.ZERO, entity.facing());
    }
    private static void strip(VertexConsumer mesh, Matrix4f pose, Vec3[] points, Vec3 camera,
                              double width, int color, float alpha, float white) {
        Vec3 previousNormal = null;
        for (int i = 0; i < points.length - 1; i++) {
            Vec3 a = points[i], b = points[i + 1], tangent = b.subtract(a);
            if (tangent.lengthSqr() < 1e-10) continue;
            Vec3 normal = tangent.cross(camera.subtract(a)).normalize();
            if (normal.lengthSqr() < .01) normal = new Vec3(1, 0, 0);
            if (previousNormal != null && normal.dot(previousNormal) < 0) normal = normal.scale(-1);
            previousNormal = normal;
            Vec3 offset = normal.scale(width);
            vertex(mesh, pose, a.subtract(offset), color, alpha, white);
            vertex(mesh, pose, b.subtract(offset), color, alpha, white);
            vertex(mesh, pose, b.add(offset), color, alpha, white);
            vertex(mesh, pose, a.add(offset), color, alpha, white);
        }
    }
    private static void sphere(VertexConsumer mesh, Matrix4f pose, Vec3 center, double radius, int color, float alpha) {
        for (int row = 0; row < 8; row++) for (int col = 0; col < 12; col++) {
            for (int corner = 0; corner < 4; corner++) {
                double lat = -Math.PI / 2 + Math.PI * (row + (corner > 1 ? 1 : 0)) / 8;
                double lon = Math.PI * 2 * (col + (corner == 1 || corner == 2 ? 1 : 0)) / 12;
                Vec3 p = center.add(Math.cos(lat) * Math.cos(lon) * radius, Math.sin(lat) * radius, Math.cos(lat) * Math.sin(lon) * radius);
                vertex(mesh, pose, p, color, alpha, .22F + (float)(Math.sin(lat) + 1) * .18F);
            }
        }
    }
    private static void vertex(VertexConsumer mesh, Matrix4f pose, Vec3 p, int color, float alpha, float white) {
        float r = ((color >> 16) & 255) / 255F, g = ((color >> 8) & 255) / 255F, b = (color & 255) / 255F;
        mesh.vertex(pose, (float)p.x, (float)p.y, (float)p.z)
                .color(r + (1 - r) * white, g + (1 - g) * white, b + (1 - b) * white, alpha).endVertex();
    }
    private static void flower(VertexConsumer mesh, Matrix4f pose, float radius, int color, float alpha) {
        float r = ((color >> 16) & 255) / 255F, g = ((color >> 8) & 255) / 255F, b = (color & 255) / 255F;
        for (int i = 0; i < 4; i++) {
            float x = i == 0 || i == 3 ? -1 : 1, y = i < 2 ? -1 : 1;
            mesh.vertex(pose, x * radius, y * radius, .01F).color(r, g, b, alpha)
                    .uv((x + 1) * .5F, (1 - y) * .5F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        }
    }
}
