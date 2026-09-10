package com.starfantasy.goety.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class StaffConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue TIER2_SCHOOL_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue TIER2_COOLDOWN_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue TIER2_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue TIER2_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue TIER2_ENTITY_RANGE;
    public static final ForgeConfigSpec.DoubleValue FINAL_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue FINAL_COOLDOWN_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue FINAL_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue FINAL_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue FINAL_ENTITY_RANGE;

    private StaffConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("tier2_staff");
        TIER2_SCHOOL_SPELL_POWER = builder.comment("二级魔杖提供的对应学派强度加成。").defineInRange("spell_power", 2.0, 0.0, 2048.0);
        TIER2_COOLDOWN_REDUCTION = builder.comment("二级魔杖提供的对应学派冷却缩减。").defineInRange("cooldown_reduction", 0.20, -0.95, 0.95);
        TIER2_ATTACK_DAMAGE = builder.comment("二级魔杖的攻击伤害。").defineInRange("attack_damage", 6.0, -2048.0, 2048.0);
        TIER2_ATTACK_SPEED = builder.comment("二级魔杖的攻击速度。").defineInRange("attack_speed", 1.6, -20.0, 20.0);
        TIER2_ENTITY_RANGE = builder.comment("二级魔杖的实体范围。").defineInRange("entity_range", 3.0, 0.0, 64.0);
        builder.pop();
        builder.push("final_staff");
        FINAL_SPELL_POWER = builder.comment("终极魔杖提供的通用巫法强度加成。").defineInRange("spell_power", 3.0, 0.0, 2048.0);
        FINAL_COOLDOWN_REDUCTION = builder.comment("终极魔杖提供的通用巫法冷却缩减。").defineInRange("cooldown_reduction", 0.30, -0.95, 0.95);
        FINAL_ATTACK_DAMAGE = builder.comment("终极魔杖的攻击伤害。").defineInRange("attack_damage", 20.0, -2048.0, 2048.0);
        FINAL_ATTACK_SPEED = builder.comment("终极魔杖的攻击速度。").defineInRange("attack_speed", 1.6, -20.0, 20.0);
        FINAL_ENTITY_RANGE = builder.comment("终极魔杖的实体范围。").defineInRange("entity_range", 4.0, 0.0, 64.0);
        builder.pop();
        SPEC = builder.build();
    }
}
