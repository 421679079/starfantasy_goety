package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Full-bright textured planes; the glow pass samples the exact same atlas coordinates. */
final class HaloCurioRenderType extends RenderType {
    private static final TransparencyStateShard GLOW = new TransparencyStateShard("halo_light", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final Map<ResourceLocation, RenderType> BASE_TYPES = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> GLOW_TYPES = new HashMap<>();

    static RenderType get(ResourceLocation texture, boolean glow) {
        return (glow ? GLOW_TYPES : BASE_TYPES).computeIfAbsent(texture, key -> RenderType.m_173215_(
                "starfantasy_goety:halo/" + (glow ? "glow/" : "base/") + key.m_135815_(),
                DefaultVertexFormat.f_85812_, VertexFormat.Mode.QUADS, 256, false, false,
                CompositeState.m_110628_()
                        .m_173290_(new TextureStateShard(key, false, false))
                        .m_173292_(f_234323_).m_110685_(glow ? GLOW : f_110139_)
                        .m_110661_(f_110110_).m_110671_(f_110152_).m_110677_(f_110154_)
                        .m_110687_(f_110115_).m_110663_(f_110113_).m_110691_(false)));
    }

    private HaloCurioRenderType(String name, VertexFormat format, VertexFormat.Mode mode,
            int size, boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }
}
