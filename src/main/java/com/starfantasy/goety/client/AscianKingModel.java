package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.AscianKingEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class AscianKingModel extends GeoModel<AscianKingEntity> {
    @Override public ResourceLocation getModelResource(AscianKingEntity entity) { return id("geo/entity/ascian_king.geo.json"); }
    @Override public ResourceLocation getTextureResource(AscianKingEntity entity) { return id("textures/entity/ascian_king/ascian_king.png"); }
    @Override public ResourceLocation getAnimationResource(AscianKingEntity entity) { return id("animations/entity/ascian_king.animation.json"); }
    static ResourceLocation id(String path) { return new ResourceLocation("starfantasy_goety", path); }
}
