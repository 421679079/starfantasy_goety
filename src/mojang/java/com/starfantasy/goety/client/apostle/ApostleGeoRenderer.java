package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.Polarice3.Goety.client.render.model.ApostleModel;
import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

public final class ApostleGeoRenderer
        extends GeoReplacedEntityRenderer<Apostle, ApostleGeoAnimatable> {
    public ApostleGeoRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new ApostleGeoModel(new ApostleModel<>(context.bakeLayer(ModModelLayer.APOSTLE))),
                ApostleGeoAnimatable.INSTANCE);
        this.addRenderLayer(new ApostleMonolithAuraLayer(this));
        this.addRenderLayer(new ApostleHeldItemLayer(this, context.getItemInHandRenderer()));
    }
}
