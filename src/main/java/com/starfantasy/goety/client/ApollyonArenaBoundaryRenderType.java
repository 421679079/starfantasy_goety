package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Shader-pack-safe, full-bright material for Apollyon's rising force-field rings. */
final class ApollyonArenaBoundaryRenderType extends RenderType {
    private static final Function<ResourceLocation, RenderType> WALL = Util.memoize(texture -> create(
            StarFantasyGoetyMod.MODID + ":arena_boundary/" + texture.getPath(),
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            32768,
            false,
            true,
            CompositeState.builder()
                    .setTextureState(new TextureStateShard(texture, true, false))
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false)));

    private ApollyonArenaBoundaryRenderType(
            String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload,
            Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload,
                setupState, clearState);
    }

    static RenderType get(ResourceLocation texture) {
        return WALL.apply(texture);
    }
}
