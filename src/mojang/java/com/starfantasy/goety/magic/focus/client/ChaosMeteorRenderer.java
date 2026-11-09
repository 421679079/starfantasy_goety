package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starfantasy.goety.magic.focus.ChaosMeteorEntity;
import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class ChaosMeteorRenderer extends EntityRenderer<ChaosMeteorEntity> {
    private static final ResourceLocation ROCK = new ResourceLocation("goety", "textures/entity/projectiles/pyroclast.png");
    private static final ResourceLocation FLAME = new ResourceLocation("minecraft", "textures/particle/flame.png");
    private static final float[][][] FACES = {
            {{1,-1,1},{1,-1,-1},{1,1,-1},{1,1,1}}, {{-1,-1,-1},{-1,-1,1},{-1,1,1},{-1,1,-1}},
            {{-1,1,1},{1,1,1},{1,1,-1},{-1,1,-1}}, {{-1,-1,-1},{1,-1,-1},{1,-1,1},{-1,-1,1}},
            {{-1,-1,1},{1,-1,1},{1,1,1},{-1,1,1}}, {{1,-1,-1},{-1,-1,-1},{-1,1,-1},{1,1,-1}}
    };
    private static final int[][] UV = {{16,24,24,32},{0,24,8,32},{8,16,16,24},{16,16,24,24},{24,24,32,32},{8,24,16,32}};
    private static final float[] SHADES = {.85F,.68F,1,.58F,.9F,.72F};

    public ChaosMeteorRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override public ResourceLocation getTextureLocation(ChaosMeteorEntity meteor) { return ROCK; }

    @Override public void render(ChaosMeteorEntity meteor, float yaw, float partial, PoseStack pose,
                                 MultiBufferSource buffers, int light) {
        long birth = meteor.visualBirth();
        if (birth < 0) return;
        double now = meteor.level().getGameTime() + (double) partial;
        if (meteor.stoppedAt() < 0) {
            float age = (float) Math.max(0, now - birth);
            pose.pushPose();
            pose.translate(0, 1.3, 0);
            pose.mulPose(new Quaternionf().rotationY(-yaw * Mth.DEG_TO_RAD).rotateX(age * .65F).rotateZ(.18F));
            VertexConsumer rock = buffers.getBuffer(Materials.ROCK_TYPE);
            cube(pose, rock, 2.55F);
            pose.mulPose(new Quaternionf().rotationXYZ(.52F, .78F, .38F));
            cube(pose, rock, 2.2F);
            pose.popPose();
        }

        Vec3 center = new Vec3(Mth.lerp(partial, meteor.xOld, meteor.getX()),
                Mth.lerp(partial, meteor.yOld, meteor.getY()), Mth.lerp(partial, meteor.zOld, meteor.getZ()));
        Vec3 back = Vec3.directionFromRotation(0, yaw).scale(-.4);
        VertexConsumer flame = buffers.getBuffer(Materials.FLAME_TYPE);
        Random random = new Random();
        for (ChaosMeteorEntity.TrailPoint point : meteor.trail()) {
            if (meteor.stoppedAt() >= 0 && point.tick() >= meteor.stoppedAt()) continue;
            double elapsed = now - point.tick();
            if (elapsed < 0 || elapsed >= ChaosMeteorEntity.TRAIL_TICKS) continue;
            Vec3 relative = point.position().subtract(center).add(back);
            random.setSeed(meteor.getId() * 100003L + point.tick() * 97L);
            for (int i = 0; i < 6; i++) {
                double life = 14 + 4 * random.nextDouble();
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = .25 + .95 * random.nextDouble();
                double height = .55 + 1.7 * random.nextDouble();
                if (elapsed >= life) continue;
                pose.pushPose();
                pose.translate(relative.x + Math.cos(angle) * radius, relative.y + height, relative.z + Math.sin(angle) * radius);
                pose.mulPose(entityRenderDispatcher.cameraOrientation());
                sheet(pose, flame, (float) (.95 * Math.pow(1 - elapsed / life, 1.15)));
                pose.popPose();
            }
        }
    }

    private static void cube(PoseStack pose, VertexConsumer out, float size) {
        float half = size / 2;
        for (int face = 0; face < FACES.length; face++) {
            int[] uv = UV[face];
            for (int corner = 0; corner < 4; corner++) {
                float[] xyz = FACES[face][corner];
                float u = uv[corner == 1 || corner == 2 ? 2 : 0] / 32F;
                float v = uv[corner < 2 ? 3 : 1] / 32F;
                vertex(pose, out, xyz[0] * half, xyz[1] * half, xyz[2] * half, u, v, SHADES[face], 1);
            }
        }
    }

    private static void sheet(PoseStack pose, VertexConsumer out, float alpha) {
        flameVertex(pose, out, -.85F, -.85F, 0, 0, 1, 1, alpha);
        flameVertex(pose, out, .85F, -.85F, 0, 1, 1, 1, alpha);
        flameVertex(pose, out, .85F, .85F, 0, 1, 0, 1, alpha);
        flameVertex(pose, out, -.85F, .85F, 0, 0, 0, 1, alpha);
    }

    private static void vertex(PoseStack pose, VertexConsumer out, float x, float y, float z,
                               float u, float v, float shade, float alpha) {
        out.vertex(pose.last().pose(), x, y, z).color(shade, shade, shade, alpha).uv(u, v).endVertex();
    }

    private static void flameVertex(PoseStack pose, VertexConsumer out, float x, float y, float z,
                                    float u, float v, float shade, float alpha) {
        out.vertex(pose.last().pose(), x, y, z).color(shade, shade, shade, alpha).uv(u, v)
                .overlayCoords(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .uv2(net.minecraft.client.renderer.LightTexture.FULL_BRIGHT)
                .normal(pose.last().normal(), 0, 0, 1).endVertex();
    }

    private static final class Materials extends RenderType {
        private static final RenderType ROCK_TYPE = create("chaos_meteor_focus_rock", DefaultVertexFormat.POSITION_COLOR_TEX,
                VertexFormat.Mode.QUADS, 4096, false, false, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_TEX_SHADER).setTextureState(new TextureStateShard(ROCK, false, false))
                        .setTransparencyState(NO_TRANSPARENCY).setCullState(CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));
        private static final RenderType FLAME_TYPE = RenderType.entityTranslucentEmissive(FLAME);

        private Materials(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling,
                          boolean sorted, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sorted, setup, clear);
        }
    }
}
