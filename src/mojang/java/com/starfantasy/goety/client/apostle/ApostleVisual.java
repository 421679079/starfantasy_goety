package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.api.ApostleAppearanceAccess;
import net.minecraft.resources.ResourceLocation;

/** Title numbers are gameplay data; custom names and translations are never consulted. */
public enum ApostleVisual {
    RISEN("apostle_the_risen"),
    ABHORRENT("apostle_the_abhorrent"),
    DEFILER("apostle_the_defiler"),
    DARK("apostle_the_dark"),
    GREAT_SHADOW("apostle_the_great_shadow"),
    WITCH_KING("apostle_the_witch_king"),
    PYRE_LORD("apostle_the_pyre_lord"),
    PROFANE("apostle_the_profane"),
    CRUEL("apostle_the_cruel"),
    TERRIBLE("apostle_the_terrible"),
    GLORIOUS("apostle_the_glorious"),
    ATROCIOUS("apostle_the_atrocious"),
    DEFAULT("apostle");

    private static final ApostleVisual[] TITLES = values();
    private final ResourceLocation model;
    private final ResourceLocation texture;

    ApostleVisual(String name) {
        model = ApostleResources.id("geo/entity/apostle/" + name + ".geo.json");
        texture = ApostleResources.id("textures/entity/apostle/" + name + ".png");
    }

    public ResourceLocation model() { return model; }
    public ResourceLocation texture() { return texture; }

    public static ApostleVisual fromTitle(int title) {
        return title >= 0 && title < 12 ? TITLES[title] : DEFAULT;
    }

    public static ApostleVisual resolve(Apostle apostle) {
        return apostle instanceof ApostleAppearanceAccess data
                ? fromTitle(data.starfantasy$visualTitle()) : DEFAULT;
    }
}
