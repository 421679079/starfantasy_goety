package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Prepare the silhouette at resource-load time, never in a spell's render loop. */
public final class GuardShieldTextures extends SimplePreparableReloadListener<float[]> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("starfantasy_goety", "dynamic/guard_light_curtain");
    private static final ResourceLocation SOURCE = new ResourceLocation("starfantasy_goety", "textures/effect/guard_light_lobe_mask.png");

    @Override protected float[] prepare(ResourceManager resources, ProfilerFiller profiler) {
        try (var stream = resources.getResourceOrThrow(SOURCE).open(); NativeImage source = NativeImage.read(stream)) {
            int width = source.getWidth(), height = source.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
                pixels[y * width + x] = source.getPixelRGBA(x, y);
            return GuardShieldMask.build(pixels, width, height);
        } catch (IOException e) { throw new IllegalStateException("Unable to load guard shield silhouette", e); }
    }
    @Override protected void apply(float[] mask, ResourceManager resources, ProfilerFiller profiler) {
        NativeImage image = new NativeImage(GuardShieldMask.SIZE, GuardShieldMask.SIZE, false);
        for (int y = 0; y < GuardShieldMask.SIZE; y++) for (int x = 0; x < GuardShieldMask.SIZE; x++) {
            int alpha = Math.round(255 * mask[y * GuardShieldMask.SIZE + x]);
            image.setPixelRGBA(x, y, (alpha << 24) | 0xFFFFFF);
        }
        Minecraft.getInstance().getTextureManager().register(TEXTURE, new DynamicTexture(image));
    }
}
