package com.starfantasy.goety.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

final class AscianItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    private final ResourceLocation model;
    AscianItemModel(String id){model=new ResourceLocation("starfantasy_goety","geo/item/"+id+".geo.json");}
    @Override public ResourceLocation getModelResource(T item){return model;}
    @Override public ResourceLocation getTextureResource(T item){return new ResourceLocation("starfantasy_goety","textures/entity/ascian_king/ascian_king.png");}
    @Override public ResourceLocation getAnimationResource(T item){return new ResourceLocation("starfantasy_goety","animations/item/ascian_items.animation.json");}
}
