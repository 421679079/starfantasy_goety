package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class HadesGeoModel extends GeoModel<HadesEntity> {
    private static final ResourceLocation MODEL = id("geo/entity/hades.geo.json");
    private static final ResourceLocation TEXTURE = id("textures/entity/hades/hades.png");
    private static final ResourceLocation ANIMATION = id("animations/entity/hades.animation.json");

    private static ResourceLocation id(String path) {
        return new ResourceLocation(StarFantasyGoetyMod.MODID, path);
    }

    @Override
    public ResourceLocation getModelResource(HadesEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HadesEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HadesEntity entity) {
        return ANIMATION;
    }
}
