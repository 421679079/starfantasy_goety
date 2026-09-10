package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/** Additive position-and-color-only material used by the boss death rays. */
final class ApollyonDeathLightRenderType extends RenderType {
    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new TransparencyStateShard("starfantasy_goety_death_light_additive", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private static final RenderType TYPE = RenderType.m_173215_(
            "starfantasy_goety:death_light",
            DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS,
            32768, false, false,
            CompositeState.m_110628_()
                    .m_173292_(f_173104_)
                    .m_110685_(ADDITIVE_TRANSPARENCY)
                    .m_110661_(f_110110_)
                    .m_110687_(f_110115_)
                    .m_110663_(f_110113_)
                    .m_110675_(f_110126_)
                    .m_110691_(false));

    private ApollyonDeathLightRenderType(
            String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload,
            Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload,
                setupState, clearState);
    }

    static RenderType get() {
        return TYPE;
    }
}
