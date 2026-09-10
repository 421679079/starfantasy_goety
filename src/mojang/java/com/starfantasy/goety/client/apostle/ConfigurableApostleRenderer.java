package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.client.render.ApostleRenderer;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.config.ApostleConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Only registered for goety:apostle. Pageant actors own separate renderers. */
public final class ConfigurableApostleRenderer extends ApostleRenderer {
    private final ApostleGeoRenderer moe;

    public ConfigurableApostleRenderer(EntityRendererProvider.Context context) {
        super(context);
        moe = new ApostleGeoRenderer(context);
    }

    @Override
    public void render(Apostle entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        if (entity.getClass() == Apostle.class && ApostleConfig.ENABLE_APOSTLE_MOE.get()) {
            moe.render(entity, yaw, partialTick, pose, buffer, light);
        } else {
            super.render(entity, yaw, partialTick, pose, buffer, light);
        }
    }
}
