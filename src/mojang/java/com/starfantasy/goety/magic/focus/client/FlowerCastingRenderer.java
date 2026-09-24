package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.*;
import com.starfantasy.goety.magic.focus.entity.FlowerCastingEntity;
import com.starfantasy.library.vfx.StarFantasyRibbonGeometry;
import com.starfantasy.library.vfx.client.StarFantasyDeferredWorldRenderer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class FlowerCastingRenderer extends EntityRenderer<FlowerCastingEntity> {
    public FlowerCastingRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(FlowerCastingEntity entity) { return null; }
    @Override public boolean shouldRender(FlowerCastingEntity entity, Frustum frustum, double x, double y, double z) {
        return frustum.isVisible(entity.getBoundingBox().inflate(FlowerCastingGeometry.RADIUS * 2.5F + 2));
    }
    @Override public void render(FlowerCastingEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffers, int light) {
        if (StarFantasyShaderCompat.isRenderingShaderShadowPass()) return;
        if (StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(poses, (restored, deferred) -> render(entity, yaw, partial, restored, deferred, light));
            return;
        }
        float fade = entity.opacity(partial);
        if (fade <= 0) return;
        float age = entity.age(partial), progress = entity.progress(partial);
        float radius = FlowerCastingGeometry.RADIUS;
        var camera = entityRenderDispatcher.cameraOrientation();
        poses.pushPose();
        var caster = entity.caster();
        if (caster != null) {
            Vec3 origin = new Vec3(Mth.lerp(partial, entity.xOld, entity.getX()),
                    Mth.lerp(partial, entity.yOld, entity.getY()), Mth.lerp(partial, entity.zOld, entity.getZ()));
            Vec3 offset = FlowerCastingEntity.anchor(caster, partial).subtract(origin);
            poses.translate(offset.x, offset.y, offset.z);
        }
        poses.mulPose(camera);
        poses.scale(radius, radius, radius);
        Matrix4f pose = poses.last().pose();
        VertexConsumer mesh = buffers.getBuffer(StarFantasyVfxRenderTypes.translucentPositionColor());
        if (progress > 0) for (int arm = 0; arm < FlowerCastingGeometry.ARMS; arm++) {
            final int branch = arm;
            FlowerRibbonMesh.castingStrip(mesh, pose, fade, FlowerCastingGeometry.reach(progress),
                    FlowerCastingGeometry.SEGMENTS,
                    (ribbon, u, width, side, out) -> FlowerCastingGeometry.point(branch, ribbon, u, progress, age, width, side, out));
        }
        float star = Math.min(1, age / 2) * fade;
        float pulse = FlowerCastingGeometry.pulse(age);
        float coreScale = .90F + .20F * pulse, glowScale = .90F + .30F * pulse;
        FlowerRibbonMesh.star(mesh, pose, 0, 0, StarFantasyRibbonGeometry.CENTER_GLOW_LENGTH * glowScale,
                StarFantasyRibbonGeometry.CENTER_GLOW_WIDTH * glowScale, .78F, .85F, 1, star * (.18F + .14F * pulse));
        FlowerRibbonMesh.star(mesh, pose, 0, 0, StarFantasyRibbonGeometry.CENTER_STAR_LENGTH * coreScale,
                StarFantasyRibbonGeometry.CENTER_STAR_WIDTH * coreScale, 1, .94F, 1, star * (.78F + .22F * pulse));
        poses.popPose();
    }
}
