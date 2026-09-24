package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfantasy.library.vfx.StarFantasyStarArrowVisual;
import com.starfantasy.library.vfx.client.StarFantasyStarArrowVisualRenderer;
import com.starfantasy.library.vfx.client.StarFantasyDeferredWorldRenderer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import com.starfantasy.goety.magic.focus.entity.FlowerArrowEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FlowerArrowRenderer extends EntityRenderer<FlowerArrowEntity> {
    private static final ResourceLocation[] BLACK_FIRE = java.util.stream.IntStream.range(0, 5)
            .mapToObj(i -> new ResourceLocation("star_fantasy_library", "textures/effect/black_fire/black_fire_0" + i + ".png"))
            .toArray(ResourceLocation[]::new);
    public FlowerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(@NotNull FlowerArrowEntity entity) {
        return StarFantasyStarArrowVisualRenderer.starTexture();
    }

    public void render(FlowerArrowEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (entity.isExploded()) com.starfantasy.library.vfx.client.StarFantasyStarArrowTrailRenderer.render(entity, entity,
                entity.getExplosionTrailAge(), FlowerArrowEntity.EXPLOSION_TRAIL_TICKS, partialTicks, poseStack, buffer);
        else StarFantasyStarArrowVisualRenderer.renderAtEntityOrigin(entity, (StarFantasyStarArrowVisual)entity, partialTicks, poseStack, buffer, MinecraftInstanceProvider.camera());
        if (entity.isExploded()) renderBurst(entity, partialTicks, poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override public boolean shouldRender(FlowerArrowEntity entity, Frustum frustum, double x, double y, double z) {
        return super.shouldRender(entity, frustum, x, y, z)
                || entity.isExploded() && frustum.isVisible(entity.getBoundingBox().inflate(7.2));
    }

    private void renderBurst(FlowerArrowEntity entity, float partial, PoseStack poses, MultiBufferSource buffers) {
        if (StarFantasyShaderCompat.isRenderingShaderShadowPass()) return;
        int frame = (int) ((entity.getExplosionTrailAge() + partial) / 2);
        if (frame < 0 || frame >= BLACK_FIRE.length) return;
        if (StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(poses, (restored, deferred) -> renderBurst(entity, partial, restored, deferred));
            return;
        }
        // Reuse the arrow's synced explosion clock and frozen position; no extra entity or damage.
        Vec3 origin = new Vec3(Mth.lerp(partial, entity.xOld, entity.getX()),
                Mth.lerp(partial, entity.yOld, entity.getY()), Mth.lerp(partial, entity.zOld, entity.getZ()));
        Vec3 offset = entity.position().subtract(origin);
        poses.pushPose();
        poses.translate(offset.x, offset.y, offset.z);
        poses.mulPose(entityRenderDispatcher.cameraOrientation());
        // The synced UUID gives each explosion a stable random roll, identical on every client
        // and throughout all five frames. Do not consume render-time RNG.
        float roll = (entity.getUUID().hashCode() & 0xFFFF) * (360F / 65536F);
        poses.mulPose(Axis.ZP.rotationDegrees(roll));
        var mesh = buffers.getBuffer(StarFantasyVfxRenderTypes.depthParticle(BLACK_FIRE[frame]));
        for (int i = 0; i < 4; i++) {
            float x = i == 0 || i == 3 ? -1 : 1, y = i < 2 ? -1 : 1;
            mesh.vertex(poses.last().pose(), x * 5, y * 5, .01F).color(1F, 1F, 1F, 1F)
                    .uv((x + 1) * .5F, (1 - y) * .5F).uv2(LightTexture.FULL_BRIGHT).endVertex();
        }
        poses.popPose();
    }

    private static final class MinecraftInstanceProvider {
        static Camera camera() {
            return net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
        }
    }
}
