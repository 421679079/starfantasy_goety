package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Additive, full-bright, double-sided material matching the MDX FilterMode Additive layer. */
final class ApollyonCleaveRenderType extends RenderType {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "starfantasy_goety", "textures/effect/apollyon_cleave_white_flame.png");
    private static final TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new TransparencyStateShard("starfantasy_goety_cleave_additive", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
    private static final RenderType TYPE = RenderType.m_173215_(
            "starfantasy_goety:apollyon_cleave",
            DefaultVertexFormat.f_85820_, VertexFormat.Mode.QUADS,
            8192, false, true,
            CompositeState.m_110628_()
                    .m_173290_(new RenderStateShard.TextureStateShard(TEXTURE, false, false))
                    .m_173292_(f_173068_)
                    .m_110685_(ADDITIVE_TRANSPARENCY)
                    .m_110661_(f_110110_)
                    .m_110687_(f_110115_)
                    .m_110663_(f_110113_)
                    .m_110675_(f_110126_)
                    .m_110691_(false));

    private ApollyonCleaveRenderType(
            String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload,
            Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload,
                setupState, clearState);
    }

    static RenderType get() {
        return TYPE;
    }

    static ResourceLocation texture() {
        return TEXTURE;
    }
}
