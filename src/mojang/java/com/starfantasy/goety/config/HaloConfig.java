package com.starfantasy.goety.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public final class HaloConfig {
    public static final ForgeConfigSpec SPEC;
    public static final Map<String, ForgeConfigSpec.ConfigValue<List<? extends String>>> EFFECTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        Map<String, ForgeConfigSpec.ConfigValue<List<? extends String>>> effects = new LinkedHashMap<>();
        builder.comment("药水效果格式：modid:effect:等级，等级从1开始。空列表表示不提供药水效果。")
                .push("halo");
        define(builder, effects, "halo_of_the_risen", "不灭重生", "minecraft:regeneration:2");
        define(builder, effects, "halo_of_the_abhorrent", "憎恶本质", "goety:electrified:2");
        define(builder, effects, "halo_of_the_defiler", "毒蝎之尾", "goety:venomous_hands:2");
        define(builder, effects, "halo_of_the_dark", "漆黑暗影", "minecraft:invisibility:1", "goety:frog_leg:1");
        define(builder, effects, "halo_of_the_great_shadow", "黑天使之影", "minecraft:night_vision:1", "goety:deflective:2");
        define(builder, effects, "halo_of_the_witch_king", "女巫之王", "goety:bottling:2", "goety:insight:2");
        define(builder, effects, "halo_of_the_pyre_lord", "爆燃领主", "minecraft:fire_resistance:1", "goety:fiery_aura:1");
        define(builder, effects, "halo_of_the_cruel", "冷酷寒冬", "goety:frosty_aura:1", "goety:chill_hide:1");
        define(builder, effects, "halo_of_the_terrible", "可怖之物", "minecraft:speed:2");
        define(builder, effects, "halo_of_the_glorious", "荣耀之名", "goety:shielding:2", "minecraft:resistance:1");
        define(builder, effects, "halo_of_the_atrocious", "十恶不赦", "goety:rallied:2");
        define(builder, effects, "halo_of_the_profane", "天启饥荒", "goety:corpse_eater:2");
        define(builder, effects, "halo_of_hades", "哈迪斯");
        builder.pop();
        EFFECTS = Collections.unmodifiableMap(effects);
        SPEC = builder.build();
    }

    private static void define(ForgeConfigSpec.Builder builder,
            Map<String, ForgeConfigSpec.ConfigValue<List<? extends String>>> values,
            String halo, String label, String... defaults) {
        values.put(halo, builder.comment(label).defineListAllowEmpty(
                halo, List.of(defaults), value -> value instanceof String text && parse(text) != null));
    }

    public static EffectEntry parse(String text) {
        int separator = text.lastIndexOf(':');
        if (separator <= text.indexOf(':') || separator == text.length() - 1) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(text.substring(0, separator));
        if (id == null) {
            return null;
        }
        try {
            int level = Integer.parseInt(text.substring(separator + 1));
            return level >= 1 && level <= 256 ? new EffectEntry(id, level) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public record EffectEntry(ResourceLocation id, int level) {}

    private HaloConfig() {}
}
