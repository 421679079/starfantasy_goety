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
    public static final ForgeConfigSpec.IntValue FLOWER_CAST, FLOWER_COOLDOWN, FLOWER_SOULS, FLOWER_FINAL_HITS;
    public static final ForgeConfigSpec.DoubleValue FLOWER_DAMAGE;
    public static final ForgeConfigSpec.IntValue EVERNIGHT_CAST, EVERNIGHT_COOLDOWN, EVERNIGHT_SOULS;
    public static final ForgeConfigSpec.DoubleValue EVERNIGHT_DAMAGE;
    public static final ForgeConfigSpec.IntValue EVERNIGHT_TOTAL_HITS;
    public static final ForgeConfigSpec.IntValue FINAL_ART_CAST, FINAL_ART_COOLDOWN, FINAL_ART_SOULS, FINAL_ART_FINAL_HITS;
    public static final ForgeConfigSpec.DoubleValue FINAL_ART_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue FINAL_ART_DAMAGE_ALL_NON_ALLIES;
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
        b.push("blooms_and_plumes_focus");
        FLOWER_CAST = b.comment("基础施法时间，单位tick。").defineInRange("cast_duration_ticks", 80, 1, 12000);
        FLOWER_COOLDOWN = b.comment("基础冷却，单位tick。").defineInRange("cooldown_ticks", 600, 0, 1728000);
        FLOWER_SOULS = b.comment("基础灵魂消耗。").defineInRange("soul_cost", 2500, 0, 100000000);
        FLOWER_DAMAGE = b.comment("每段爆炸的音爆伤害。")
                .defineInRange("damage_per_hit", 10D, 0D, 1000000D);
        FLOWER_FINAL_HITS = b.comment("终结爆炸的伤害段数。").defineInRange("final_hit_count", 12, 1, 200);
        b.pop();
        b.push("evernight_focus");
        EVERNIGHT_CAST = b.comment("基础施法时间，单位tick。").defineInRange("cast_duration_ticks", 20, 1, 12000);
        EVERNIGHT_COOLDOWN = b.comment("基础冷却，单位tick。").defineInRange("cooldown_ticks", 200, 0, 1728000);
        EVERNIGHT_SOULS = b.comment("基础灵魂消耗。").defineInRange("soul_cost", 100, 0, 100000000);
        EVERNIGHT_DAMAGE = b.comment("每段爆炸的凋零伤害").defineInRange("damage_per_hit", 5D, 0D, 1000000D);
        EVERNIGHT_TOTAL_HITS = b.comment("总计爆炸段数").defineInRange("total_hit_count", 8, 6, 200);
        b.pop();
        b.push("final_art_focus");
        FINAL_ART_CAST = b.comment("基础施法时间，单位tick。").defineInRange("cast_duration_ticks", 100, 1, 12000);
        FINAL_ART_COOLDOWN = b.comment("基础冷却，单位tick。").defineInRange("cooldown_ticks", 2400, 0, 1728000);
        FINAL_ART_SOULS = b.comment("基础灵魂消耗。").defineInRange("soul_cost", 500, 0, 100000000);
        FINAL_ART_DAMAGE = b.comment("每段额外魔法伤害。").defineInRange("damage_per_hit", 10D, 0D, 1000000D);
        FINAL_ART_FINAL_HITS = b.comment("终结爆炸的伤害段数。").defineInRange("final_hit_count", 5, 1, 200);
        FINAL_ART_DAMAGE_ALL_NON_ALLIES = b.comment("黑洞会伤害所有非友方生物").define("damage_all_non_allies", false);
        b.pop();
        SPEC = b.build();
    }
    private SpellConfig() {}
}
