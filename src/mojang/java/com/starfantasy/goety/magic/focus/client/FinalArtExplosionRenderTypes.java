package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Copyright (c) 2026 Funits and StarFantasy SlashBlade Addon contributors.
 * MIT license: THIRD_PARTY/StarFantasy_SlashBlade/LICENSE.txt.
 */
public final class FinalArtExplosionRenderTypes extends RenderType {
    private static final RenderType BLACK_HOLE_SURFACE = create(
            "starfantasy_goety:final_art_black_hole_surface",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private static final RenderType BLACK_HOLE_OVERLAY = create(
            "starfantasy_goety:final_art_black_hole_overlay",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            4096,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private FinalArtExplosionRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                     boolean affectsCrumbling, boolean sortOnUpload,
                                     Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType blackHoleSurface() {
        return BLACK_HOLE_SURFACE;
    }

    public static RenderType blackHoleOverlay() {
        return BLACK_HOLE_OVERLAY;
    }
}
