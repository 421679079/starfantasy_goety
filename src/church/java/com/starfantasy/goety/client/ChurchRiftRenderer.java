package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.mojang.logging.LogUtils;
import com.starfantasy.goety.church.ChurchContent;
import com.starfantasy.goety.church.ChurchRiftEntity;
import com.starfantasy.goety.church.ChurchRiftShape;
import com.starfantasy.library.vfx.client.StarFantasyDeferredWorldRenderer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = ChurchContent.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ChurchRiftRenderer extends EntityRenderer<ChurchRiftEntity> {
    private static ResourceLocation id(String path) { return new ResourceLocation(ChurchContent.MODID,path); }
    @SubscribeEvent public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ChurchContent.CHURCH_RIFT.get(), ChurchRiftRenderer::new);
    }
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ShaderInstance shader;
    private final MultiBufferSource.BufferSource isolated = MultiBufferSource.immediate(new BufferBuilder(131072));

    public ChurchRiftRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0; }

    @SubscribeEvent public static void registerShaders(RegisterShadersEvent event) {
        // GameRenderer owns shader lifetimes. Never retain a closed instance across reloads.
        shader = null;
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), id("church_rift"),
                    DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> shader = loaded);
        } catch (IOException | RuntimeException failure) {
            // Oculus can turn a compiler failure into a runtime cancellation exception.
            LOGGER.warn("Church rift shader unavailable; using vanilla End portal appearance until the next resource reload.", failure);
        }
    }

    @Override public void render(ChurchRiftEntity entity, float yaw, float partialTick, PoseStack pose,
                                 MultiBufferSource ignored, int light) {
        if (StarFantasyShaderCompat.isRenderingShaderShadowPass() || entity.reveal(partialTick) < 0.001F) return;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        if (StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(pose, (restored, unused) -> draw(entity, partialTick, restored));
        } else draw(entity, partialTick, pose);
        pose.popPose();
    }

    private void draw(ChurchRiftEntity entity, float partialTick, PoseStack pose) {
        float reveal = entity.reveal(partialTick), opening = entity.opening(partialTick);
        if (reveal < 0.001F) return;
        // Check at draw time too: deferred work must not use a shader invalidated by reload.
        if (shader == null) {
            drawEndPortal(pose, reveal, opening);
            return;
        }
        float time = (entity.level().getGameTime() % 24000L + partialTick) / 20F;
        Vec3 camera = entity.local(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        shader.safeGetUniform("CameraLocal").set((float) camera.x, (float) camera.y, (float) camera.z);
        shader.safeGetUniform("RiftTime").set(time);
        shader.safeGetUniform("Reveal").set(reveal);
        shader.safeGetUniform("Opening").set(opening);
        shader.safeGetUniform("HalfSize").set((float) ChurchRiftShape.HALF_WIDTH, (float) ChurchRiftShape.HALF_HEIGHT);
        float quadWidth = (float) ChurchRiftShape.HALF_WIDTH + ChurchRiftShape.VISUAL_PADDING;
        float quadHeight = (float) ChurchRiftShape.HALF_HEIGHT + ChurchRiftShape.VISUAL_PADDING;
        shader.safeGetUniform("QuadHalfSize").set(quadWidth, quadHeight);
        shader.safeGetUniform("RiftSeed").set((float) (entity.getUUID().getLeastSignificantBits() & 65535));
        Matrix4f matrix = pose.last().pose();
        VertexConsumer out = isolated.getBuffer(Types.APERTURE);
        // A fixed UV rectangle keeps the interior stable as the aperture opens and closes.
        vertex(out, matrix, -quadWidth, -quadHeight, 0, 0, 0);
        vertex(out, matrix, quadWidth, -quadHeight, 0, 1, 0);
        vertex(out, matrix, quadWidth, quadHeight, 0, 1, 1);
        vertex(out, matrix, -quadWidth, quadHeight, 0, 0, 1);
        isolated.endBatch(Types.APERTURE); // Per-entity uniforms must not share a deferred batch.

        VertexConsumer blocks = isolated.getBuffer(Types.DATA_BLOCKS);
        long seed = entity.getUUID().getLeastSignificantBits();
        int blockCount = entity.hasOpened() ? 112 : 8;
        float blockTime = entity.blockAnimationTime(partialTick);
        for (int i = 0; i < blockCount; i++) {
            double period = 3.0 + random(seed + i * 11L) * 2.0;
            double clock = blockTime / period + random(seed + i * 37L);
            int cycle = (int) Math.floor(clock);
            float progress = (float) (clock - cycle);
            long key = seed + i * 1009L + cycle * 65537L;
            float y = (float) ((random(key) * 2 - 1) * ChurchRiftShape.HALF_HEIGHT * reveal);
            float side = i % 2 == 0 ? -1 : 1;
            float edge = (float) ChurchRiftShape.halfWidthAt(y / Math.max(0.001F, reveal)) * opening;
            float x = side * (edge + 0.08F + (float) random(key + 1) * (0.12F + 0.45F * opening));
            float z = (float) (random(key + 2) - 0.5) * 0.8F;
            float size = (0.08F + (float) random(key + 3) * 0.24F) * (0.5F + progress * 1.8F) * reveal;
            float alpha = Math.min(1, progress * 7) * (1 - progress) * reveal;
            cube(blocks, matrix, x, y, z, size, alpha);
        }
        isolated.endBatch(Types.DATA_BLOCKS);
    }

    private void drawEndPortal(PoseStack pose, float reveal, float opening) {
        // Vanilla may also be temporarily unavailable while resources are being reloaded.
        if (GameRenderer.getRendertypeEndPortalShader() == null) return;
        RenderType type = RenderType.endPortal();
        VertexConsumer out = isolated.getBuffer(type);
        Matrix4f matrix = pose.last().pose();
        // A small two-sided mesh preserves the aperture and its opening animation.
        // POSITION only: use vanilla's shader/textures, with no custom shader or diorama.
        int strips = 36;
        for (int i = 0; i < strips; i++) {
            float y0 = (float) ChurchRiftShape.HALF_HEIGHT * (2F * i / strips - 1F);
            float y1 = (float) ChurchRiftShape.HALF_HEIGHT * (2F * (i + 1) / strips - 1F);
            float w0 = 0.022F * (1F - opening) + (float) ChurchRiftShape.halfWidthAt(y0) * opening;
            float w1 = 0.022F * (1F - opening) + (float) ChurchRiftShape.halfWidthAt(y1) * opening;
            y0 *= reveal;
            y1 *= reveal;
            out.vertex(matrix, -w0, y0, 0).endVertex();
            out.vertex(matrix, w0, y0, 0).endVertex();
            out.vertex(matrix, w1, y1, 0).endVertex();
            out.vertex(matrix, -w1, y1, 0).endVertex();
            out.vertex(matrix, -w1, y1, 0).endVertex();
            out.vertex(matrix, w1, y1, 0).endVertex();
            out.vertex(matrix, w0, y0, 0).endVertex();
            out.vertex(matrix, -w0, y0, 0).endVertex();
        }
        isolated.endBatch(type);
    }

    private static double random(long seed) {
        seed = (seed ^ (seed >>> 30)) * 0xbf58476d1ce4e5b9L;
        seed = (seed ^ (seed >>> 27)) * 0x94d049bb133111ebL;
        return ((seed ^ (seed >>> 31)) >>> 11) * 0x1.0p-53;
    }

    private static void cube(VertexConsumer out, Matrix4f m, float x, float y, float z, float s, float alpha) {
        // Six inset panels expose a luminous rim on a dark red block, without textures/particles.
        for (int axis = 0; axis < 3; axis++) for (int sign = -1; sign <= 1; sign += 2) {
            face(out, m, x, y, z, axis, sign * s, s, 0.25F, 0.95F, 0.76F, alpha);
            face(out, m, x, y, z, axis, sign * (s + 0.001F), s * 0.86F, 0.018F, 0.16F, 0.14F, alpha);
        }
    }

    private static void face(VertexConsumer out, Matrix4f m, float x, float y, float z, int axis,
                             float d, float s, float r, float g, float b, float alpha) {
        for (int i = 0; i < 4; i++) {
            float a = i == 0 || i == 3 ? -s : s;
            float c = i < 2 ? -s : s;
            out.vertex(m, x + (axis == 0 ? d : a), y + (axis == 1 ? d : axis == 0 ? a : c),
                    z + (axis == 2 ? d : c)).color(r, g, b, alpha).endVertex();
        }
    }

    private static void vertex(VertexConsumer out, Matrix4f m, float x, float y, float z, float u, float v) {
        out.vertex(m, x, y, z).uv(u, v).color(255, 255, 255, 255).endVertex();
    }

    @Override public ResourceLocation getTextureLocation(ChurchRiftEntity entity) { return id("textures/item/underworld_eye.png"); }

    private static final class Types extends RenderType {
        private Types(String n, VertexFormat f, VertexFormat.Mode m, int s, boolean c, boolean o, Runnable a, Runnable b) {
            super(n, f, m, s, c, o, a, b);
        }

        private static final RenderType APERTURE = create("starfantasy_goety:church_rift", DefaultVertexFormat.POSITION_TEX_COLOR,
                VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
                        .setShaderState(new ShaderStateShard(() -> shader))
                        .setTextureState(MultiTextureStateShard.builder()
                                .add(new ResourceLocation("minecraft", "textures/block/reinforced_deepslate_side.png"), false, false)
                                .add(new ResourceLocation("minecraft", "textures/block/stone_bricks.png"), false, false)
                                .add(new ResourceLocation("minecraft", "textures/block/polished_deepslate.png"), false, false)
                                .add(new ResourceLocation("minecraft", "textures/block/red_nether_bricks.png"), false, false)
                                .add(new ResourceLocation("minecraft", "textures/block/dead_fire_coral_block.png"), false, false).build())
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
        private static final RenderType DATA_BLOCKS = create("starfantasy_goety:church_rift_blocks", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.QUADS, 131072, false, true, CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }
}
