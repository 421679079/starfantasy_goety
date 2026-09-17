package com.starfantasy.goety.config;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public final class SpellConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Integer> GUARD_SOUL_COST;
    public static final ForgeConfigSpec.ConfigValue<Integer> GUARD_SOUL_REWARD;
    public static final ForgeConfigSpec.IntValue GUARD_DURATION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GUARD_BLACKLIST;
    public static final ForgeConfigSpec.IntValue GUARD_INVULNERABILITY;
    public static final ForgeConfigSpec.DoubleValue GUARD_DAMAGE_REDUCTION;
    public static final ForgeConfigSpec.IntValue GUARD_COOLDOWN;
    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("guard_focus");
        GUARD_SOUL_COST = b.comment("消耗灵魂能量")
                .define("soul_cost", 20, value -> value instanceof Integer amount && amount >= 0);
        GUARD_SOUL_REWARD = b.comment("格挡成功恢复灵魂能量")
                .define("soul_reward", 40, value -> value instanceof Integer amount && amount >= 0);
        GUARD_DURATION = b.comment("施法持续时间，单位tick。")
                .defineInRange("cast_duration_ticks", 10, 1, 1200);
        GUARD_BLACKLIST = b.comment("以下实体造成的伤害不可格挡。")
                .defineListAllowEmpty("entity_blacklist", List.of("minecraft:area_effect_cloud", "goety:hellfire"),
                        v -> v instanceof String s && ResourceLocation.tryParse(s) != null);
        GUARD_INVULNERABILITY = b.comment("格挡成功获得的受击无敌时间，单位tick。")
                .defineInRange("hurt_invulnerability_ticks", 20, 0, 1200);
        GUARD_DAMAGE_REDUCTION = b.comment("格挡减伤比例 1.0为取消伤害")
                .defineInRange("damage_reduction", 1.0D, 0.0D, 1.0D);
        GUARD_COOLDOWN = b.comment("聚晶冷却时间，单位tick。结束施法时结算；成功格挡则免除冷却。")
                .defineInRange("cooldown_ticks", 100, 0, 12000);
        b.pop();
        SPEC = b.build();
    }
    private SpellConfig() {}
}
