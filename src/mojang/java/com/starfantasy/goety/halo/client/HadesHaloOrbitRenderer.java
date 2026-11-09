package com.starfantasy.goety.halo.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starfantasy.goety.halo.HadesHaloContents;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Worn Hades eye-local geometry. No particles, retained history, tick hooks or world references. */
public final class HadesHaloOrbitRenderer {
    private static final ResourceLocation[] TEXTURES = HadesHaloContents.IDS.stream()
            .map(id -> new ResourceLocation(HadesHaloContents.MODID, "textures/entity/apostle/"
                    + id.replace("halo_of_", "apostle_") + ".png")).toArray(ResourceLocation[]::new);

    public static void render(ItemStack stack, float time, PoseStack pose, MultiBufferSource buffers,
                              float width, float height) {
        int mask = HadesHaloContents.collectedMask(stack);
        if (mask == 0) return;
        Matrix4f matrix = pose.last().pose();
        float size = width * .075F;
        float scale = (float)Math.sqrt(matrix.m00()*matrix.m00() + matrix.m01()*matrix.m01() + matrix.m02()*matrix.m02());
        Vector3f head = new Vector3f(), tail = new Vector3f();
        Quaternionf rotation = new Quaternionf();
        // As on Final Staff, draw finite additive ribbons from previous analytic orbit positions.
        VertexConsumer trail = buffers.getBuffer(Types.TRAIL);
        for (int i = 0; i < 12; i++) if ((mask & 1 << i) != 0) {
            ribbon(matrix, trail, i, time, width, height, size * HadesHaloOrbit.TRAIL_WIDTH_TO_SIZE * scale, head, tail);
        }
        for (int i = 0; i < 12; i++) if ((mask & 1 << i) != 0) {
            float angle = HadesHaloOrbit.degrees(i, time);
            HadesHaloOrbit.point(i, angle, width, height, head);
            float atlasWidth = i == 4 ? 512F : 256F;
            VertexConsumer halo = buffers.getBuffer(Types.halo(TEXTURES[i]));
            float half = size * .5F;
            // Turn with the orbit as on Final Staff, keeping the icon's original square size.
            pose.pushPose();
            pose.translate(head.x, head.y, head.z);
            pose.mulPose(HadesHaloOrbit.orientation(i, angle, rotation));
            Matrix4f haloMatrix = pose.last().pose();
            Matrix3f haloNormal = pose.last().normal();
            vertex(halo, haloMatrix, haloNormal, -half, -half, 0, 192F/atlasWidth, 0);
            vertex(halo, haloMatrix, haloNormal, -half, half, 0, 192F/atlasWidth, 64F/512F);
            vertex(halo, haloMatrix, haloNormal, half, half, 0, 256F/atlasWidth, 64F/512F);
            vertex(halo, haloMatrix, haloNormal, half, -half, 0, 256F/atlasWidth, 0);
            pose.popPose();
        }
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v) {
        out.vertex(matrix, x, y, z).color(1F, 1F, 1F, 1F).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0, 0, -1).endVertex();
    }

    private static void ribbon(Matrix4f matrix, VertexConsumer out, int slot, float time,
                               float width, float height, float halfWidth, Vector3f head, Vector3f tail) {
        float angle = HadesHaloOrbit.degrees(slot, time);
        matrix.transformPosition(HadesHaloOrbit.point(slot, angle, width, height, head));
        for (int segment = 0; segment < HadesHaloOrbit.TRAIL_SEGMENTS; segment++) {
            float tailAngle = angle - (segment + 1) * HadesHaloOrbit.TRAIL_STEP;
            matrix.transformPosition(HadesHaloOrbit.point(slot, tailAngle, width, height, tail));
            float dx = tail.x-head.x, dy = tail.y-head.y, length = (float)Math.sqrt(dx*dx+dy*dy);
            float px = length > 1E-7F ? -dy/length : 1, py = length > 1E-7F ? dx/length : 0;
            float start = HadesHaloOrbit.fade(segment), end = HadesHaloOrbit.fade(segment+1);
            int color = HadesHaloOrbit.LIGHT[slot];
            trailVertex(out,head,px,py,halfWidth*start,color,Math.round(145*start*start));
            trailVertex(out,tail,px,py,halfWidth*end,color,Math.round(145*end*end));
            trailVertex(out,tail,px,py,-halfWidth*end,color,Math.round(145*end*end));
            trailVertex(out,head,px,py,-halfWidth*start,color,Math.round(145*start*start));
            head.set(tail);
        }
    }

    private static void trailVertex(VertexConsumer out, Vector3f point,
                                    float px, float py, float halfWidth, int color, int alpha) {
        out.vertex(point.x+px*halfWidth,point.y+py*halfWidth,point.z)
                .color(color>>16&255,color>>8&255,color&255,alpha).endVertex();
    }

    private static final class Types extends RenderType {
        private static final Map<ResourceLocation, RenderType> HALOS = new HashMap<>();
        private static final RenderType TRAIL = create("starfantasy_goety:hades_orbit_trails",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, false,
                CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));

        static RenderType halo(ResourceLocation texture) {
            return HALOS.computeIfAbsent(texture, key -> create("starfantasy_goety:hades_orbit/"+key.getPath(),
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false,
                    CompositeState.builder().setTextureState(new TextureStateShard(key,false,false))
                            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                            .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false)));
        }

        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                      boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
            super(name,format,mode,size,crumbling,sorted,setup,clear);
        }
    }

    private HadesHaloOrbitRenderer() {}
}
