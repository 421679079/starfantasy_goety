package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.starfantasy.goety.registry.HaloItemRegistry;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

/** Item-local geometry: no entities, particles, tick listeners, or retained world references. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FinalStaffRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation BODY = new ResourceLocation("starfantasy_goety", "item/final_staff_body");
    private static final int[] CORE = {0xFFB72D, 0xFFE18A, 0xFFF8D9};
    private static final int[] INNER_FLAME = {0xE94C0B, 0xFF9D1C, 0xFFE779};
    private static final int[] OUTER_FLAME = {0xAB1709, 0xF3470C, 0xFF9B25};
    private static final float ORBIT_RADIUS = 6.9F / 16.0F;
    private static final float ORBIT_DEGREES_PER_TICK = 3.0F;
    private static final float ORBIT_CENTER_Y = 32.25F / 16.0F;
    private static final float[] ORBIT_PHASES = {0.0F, 105.0F, 0.0F, 105.0F};
    private static final Quaternionf[] ORBIT_ROTATIONS = createOrbitRotations();
    private static final float HALO_SIZE = 1.7F / 16.0F;
    private static final float HALO_CORE_SIZE = 2.55F / 16.0F;
    private static final int TRAIL_SEGMENTS = 6;
    private static final float TRAIL_STEP_DEGREES = 15.0F;
    private static final ResourceLocation STAR_TEXTURE = new ResourceLocation(
            "star_fantasy_library", "textures/cil_particle/star.png");
    // Bright accents from the twelve halo palettes, in the same registry order.
    private static final int[] HALO_LIGHT = {0x71D4C5, 0xD41023, 0xA4669C, 0x909090,
            0xF1F1F3, 0xF759B8, 0xFF9B25, 0x4CD962, 0x95D6F1, 0xFCA5B8, 0xE7C870, 0xA6C3EF};
    private final ResourceLocation[] haloTextures;

    private FinalStaffRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        haloTextures = HaloItemRegistry.APOSTLE_HALOS.stream()
                .map(halo -> new ResourceLocation(halo.getId().getNamespace(), "item/" + halo.getId().getPath()))
                .toArray(ResourceLocation[]::new);
    }

    public static void initialize(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private FinalStaffRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new FinalStaffRenderer();
                return renderer;
            }
        });
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(BODY);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        pose.pushPose();
        // The outer ItemRenderer has already applied the display transform and -0.5 origin.
        // Cancel only the inner renderer's origin shift; NONE avoids applying the display twice.
        pose.translate(0.5, 0.5, 0.5);
        minecraft.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, pose, buffers,
                light, overlay, minecraft.getModelManager().getModel(BODY));
        pose.popPose();

        float time = minecraft.level == null ? 0.0F
                : (minecraft.level.getGameTime() % 12000L) + (minecraft.isPaused() ? 0 : minecraft.getFrameTime());
        pose.pushPose();
        // Lift the spherical assembly enough to keep its lower hemisphere clear of the staff's horns.
        pose.translate(7.5 / 16.0, ORBIT_CENTER_Y, 8.5 / 16.0);

        // Opaque emissive core anchors the flame even against bright backgrounds.
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(time * 1.8F));
        pose.mulPose(Axis.ZP.rotationDegrees(35.0F));
        cube(pose, buffers.getBuffer(CubeRenderType.SOLID), 1.9F / 16.0F, CORE, 4, 0, 255);
        pose.popPose();

        // Additive shells retain the visibility of the inner layers; no depth writes.
        VertexConsumer glow = buffers.getBuffer(CubeRenderType.GLOW);
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(-time * 1.8F + 25.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(time * 0.9F));
        cube(pose, glow, 3.4F / 16.0F, INNER_FLAME, 6, time * 0.08F, 145);
        pose.popPose();
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotationDegrees(time * 1.8F - 35.0F));
        pose.mulPose(Axis.YP.rotationDegrees(-time * 0.9F));
        cube(pose, glow, 6.0F / 16.0F, OUTER_FLAME, 8, time * 0.08F, 95);
        pose.popPose();

        // One atlas buffer for all twelve flat halos. Resolve sprites each frame for resource reloads.
        VertexConsumer halos = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        var atlas = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        for (int i = 0; i < haloTextures.length; i++) {
            pose.pushPose();
            orientHalo(pose, i, time);
            halo(pose, halos, atlas.apply(haloTextures[i]));
            pose.popPose();
        }

        // Batch the effects by render type. The orbit is analytic, with no per-item history or ticks.
        VertexConsumer trails = buffers.getBuffer(CubeRenderType.TRAIL);
        for (int i = 0; i < haloTextures.length; i++) {
            haloTrail(pose.last().pose(), trails, i, time, HALO_LIGHT[i]);
        }
        VertexConsumer stars = buffers.getBuffer(CubeRenderType.STAR);
        for (int i = 0; i < haloTextures.length; i++) {
            haloCore(pose.last().pose(), stars, i, time, HALO_LIGHT[i]);
        }
        pose.popPose();
    }

    private static Quaternionf[] createOrbitRotations() {
        Quaternionf[] rotations = new Quaternionf[4];
        for (int plane = 0; plane < rotations.length; plane++) {
            // Four equally spaced normals on a cone form four distinct great-circle planes.
            rotations[plane] = new Quaternionf().rotationY((float)Math.toRadians(45.0F + plane * 90.0F))
                    .rotateX((float)Math.acos(1.0 / Math.sqrt(3.0)));
        }
        return rotations;
    }

    private static float orbitDegrees(int index, float time) {
        // Three halos per plane; phase offsets keep halos apart where the planes intersect.
        return (index / 4) * 120.0F + ORBIT_PHASES[index % 4] + time * ORBIT_DEGREES_PER_TICK;
    }

    private static Vector3f orbitPoint(Matrix4f matrix, int index, float degrees, Vector3f target) {
        double angle = Math.toRadians(degrees);
        return matrix.transformPosition(target.set((float)Math.cos(angle) * ORBIT_RADIUS,
                0, (float)Math.sin(angle) * ORBIT_RADIUS).rotate(ORBIT_ROTATIONS[index % 4]));
    }

    private static float itemScale(Matrix4f matrix) {
        return (float)Math.sqrt(matrix.m00()*matrix.m00() + matrix.m01()*matrix.m01() + matrix.m02()*matrix.m02());
    }

    private static void haloTrail(Matrix4f matrix, VertexConsumer out, int index, float time, int color) {
        float degrees = orbitDegrees(index, time);
        float halfWidth = HALO_SIZE * 0.11F * itemScale(matrix);
        Vector3f head = orbitPoint(matrix, index, degrees, new Vector3f());
        Vector3f tail = new Vector3f();
        for (int segment = 0; segment < TRAIL_SEGMENTS; segment++) {
            orbitPoint(matrix, index, degrees - (segment + 1) * TRAIL_STEP_DEGREES, tail);
            float dx = tail.x - head.x, dy = tail.y - head.y;
            float length = (float)Math.sqrt(dx*dx + dy*dy);
            // Work in rendered coordinates: the ribbon faces the screen in hands, GUI and item frames.
            float px = length > 1.0E-7F ? -dy / length : 1.0F;
            float py = length > 1.0E-7F ? dx / length : 0.0F;
            float start = 1.0F - (float)segment / TRAIL_SEGMENTS;
            float end = 1.0F - (float)(segment + 1) / TRAIL_SEGMENTS;
            trailVertex(out, head, px, py, halfWidth * start, color, Math.round(145 * start * start));
            trailVertex(out, tail, px, py, halfWidth * end, color, Math.round(145 * end * end));
            trailVertex(out, tail, px, py, -halfWidth * end, color, Math.round(145 * end * end));
            trailVertex(out, head, px, py, -halfWidth * start, color, Math.round(145 * start * start));
            head.set(tail);
        }
    }

    private static void trailVertex(VertexConsumer out, Vector3f point, float px, float py,
                                    float width, int color, int alpha) {
        out.vertex(point.x + px * width, point.y + py * width, point.z)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha).endVertex();
    }

    private static void haloCore(Matrix4f matrix, VertexConsumer out, int index, float time, int color) {
        Vector3f center = orbitPoint(matrix, index, orbitDegrees(index, time), new Vector3f());
        float size = HALO_CORE_SIZE * itemScale(matrix);
        float rotation = (float)Math.toRadians(time * 3.6F + index * 30.0F);
        // The same star sprite as Star Arrow, with a colored flare and a smaller white-hot center.
        starQuad(out, center, size * 0.34F, rotation, color, 190);
        starQuad(out, center, size * 0.14F, -rotation, 0xFFF5DF, 235);
    }

    private static void starQuad(VertexConsumer out, Vector3f center, float halfSize,
                                 float rotation, int color, int alpha) {
        float c = (float)Math.cos(rotation), s = (float)Math.sin(rotation);
        starVertex(out, center, -halfSize,-halfSize, c,s, 0,1, color,alpha);
        starVertex(out, center, halfSize,-halfSize, c,s, 1,1, color,alpha);
        starVertex(out, center, halfSize,halfSize, c,s, 1,0, color,alpha);
        starVertex(out, center, -halfSize,halfSize, c,s, 0,0, color,alpha);
    }

    private static void starVertex(VertexConsumer out, Vector3f center, float x, float y, float c, float s,
                                   float u, float v, int color, int alpha) {
        out.vertex(center.x + x*c - y*s, center.y + x*s + y*c, center.z)
                .uv(u,v).color((color >> 16) & 255, (color >> 8) & 255, color & 255, alpha).endVertex();
    }

    private static void orientHalo(PoseStack pose, int index, float time) {
        float degrees = orbitDegrees(index, time);
        double angle = Math.toRadians(degrees);
        pose.mulPose(ORBIT_ROTATIONS[index % 4]);
        pose.translate(Math.cos(angle) * ORBIT_RADIUS, 0, Math.sin(angle) * ORBIT_RADIUS);
        // The texture's +Z face points along the inward radius at every point of its orbit.
        pose.mulPose(Axis.YP.rotationDegrees(-degrees - 90.0F));
    }

    private static void halo(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite) {
        float h = HALO_SIZE * 0.5F;
        haloVertex(pose, out, -h, -h, sprite.getU0(), sprite.getV1());
        haloVertex(pose, out, h, -h, sprite.getU1(), sprite.getV1());
        haloVertex(pose, out, h, h, sprite.getU1(), sprite.getV0());
        haloVertex(pose, out, -h, h, sprite.getU0(), sprite.getV0());
    }

    private static void haloVertex(PoseStack pose, VertexConsumer out, float x, float y, float u, float v) {
        out.vertex(pose.last().pose(), x, y, 0).color(255,255,255,255).uv(u,v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.last().normal(), 0,0,1).endVertex();
    }

    private static void cube(PoseStack pose, VertexConsumer out, float size, int[] palette, int bands, float flicker, int alpha) {
        Matrix4f matrix = pose.last().pose();
        float h = size * 0.5F;
        // Four vertically graded sides. Small face-to-face brightness differences keep the cube readable.
        for (int side = 0; side < 4; side++) {
            for (int band = 0; band < bands; band++) {
                float low = -h + size * band / bands;
                float high = -h + size * (band + 1) / bands;
                float shift = flicker == 0 ? 0 : 0.06F * (float) Math.sin(flicker + band * 1.1F + side);
                int color = gradient(palette, (band + 0.5F) / bands + shift);
                float shade = switch (side) { case 0 -> 0.88F; case 1 -> 0.96F; case 2 -> 0.80F; default -> 1.0F; };
                switch (side) {
                    case 0 -> quad(out, matrix, color, shade, alpha, -h,low,-h, -h,high,-h, h,high,-h, h,low,-h);
                    case 1 -> quad(out, matrix, color, shade, alpha, h,low,h, h,high,h, -h,high,h, -h,low,h);
                    case 2 -> quad(out, matrix, color, shade, alpha, -h,low,h, -h,high,h, -h,high,-h, -h,low,-h);
                    default -> quad(out, matrix, color, shade, alpha, h,low,-h, h,high,-h, h,high,h, h,low,h);
                }
            }
        }
        quad(out, matrix, palette[0], 0.8F, alpha, -h,-h,h, -h,-h,-h, h,-h,-h, h,-h,h);
        quad(out, matrix, palette[palette.length - 1], 1, alpha, -h,h,-h, -h,h,h, h,h,h, h,h,-h);
    }

    private static int gradient(int[] stops, float position) {
        float scaled = Math.max(0, Math.min(1, position)) * (stops.length - 1);
        int index = Math.min((int) scaled, stops.length - 2);
        float blend = scaled - index;
        int result = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            int a = (stops[index] >> shift) & 255, b = (stops[index + 1] >> shift) & 255;
            result |= Math.round(a + (b - a) * blend) << shift;
        }
        return result;
    }

    private static void quad(VertexConsumer out, Matrix4f matrix, int color, float shade, int alpha,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        int r = (int) (((color >> 16) & 255) * shade);
        int g = (int) (((color >> 8) & 255) * shade);
        int b = (int) ((color & 255) * shade);
        out.vertex(matrix,x0,y0,z0).color(r,g,b,alpha).endVertex();
        out.vertex(matrix,x1,y1,z1).color(r,g,b,alpha).endVertex();
        out.vertex(matrix,x2,y2,z2).color(r,g,b,alpha).endVertex();
        out.vertex(matrix,x3,y3,z3).color(r,g,b,alpha).endVertex();
    }

    private static final class CubeRenderType extends RenderType {
        private static final RenderType SOLID = create("starfantasy_goety:staff_cubes",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 32768, false, false,
                CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(NO_TRANSPARENCY).setCullState(CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false));

        private static final RenderType GLOW = create("starfantasy_goety:staff_flame",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, false,
                CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        private static final RenderType TRAIL = create("starfantasy_goety:staff_halo_trails",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, false,
                CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        private static final RenderType STAR = create("starfantasy_goety:staff_halo_cores",
                // Oculus maps both color/UV shader variants to POSITION_TEX_COLOR; match its attribute order.
                DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                CompositeState.builder().setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                        .setTextureState(new TextureStateShard(STAR_TEXTURE, false, false))
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));

        private CubeRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                               boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }
    }
}
