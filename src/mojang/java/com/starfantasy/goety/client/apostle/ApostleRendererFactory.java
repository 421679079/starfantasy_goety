package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.client.render.ApostleRenderer;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.fml.ModList;

/** Keep the replaced GeckoLib renderer unloaded when Particle Storm is installed. */
public final class ApostleRendererFactory {
    private ApostleRendererFactory() {}

    public static EntityRenderer<? super Apostle> create(EntityRendererProvider.Context context) {
        if (ModList.get().isLoaded("particlestorm")) return new ApostleRenderer(context);
        return MoeRenderer.create(context);
    }

    // Separate class: verifying the fallback factory must not resolve the Geo renderer.
    private static final class MoeRenderer {
        private static EntityRenderer<? super Apostle> create(EntityRendererProvider.Context context) {
            return new ConfigurableApostleRenderer(context);
        }
    }
}
