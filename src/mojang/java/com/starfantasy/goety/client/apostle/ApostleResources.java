package com.starfantasy.goety.client.apostle;

import net.minecraft.resources.ResourceLocation;

public final class ApostleResources {
    private ApostleResources() {}
    public static ResourceLocation id(String path) {
        return new ResourceLocation("starfantasy_goety", path);
    }
}
