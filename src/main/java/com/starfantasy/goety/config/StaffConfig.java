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
    public static final ForgeConfigSpec.DoubleValue ASCIAN_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue ASCIAN_COOLDOWN_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue ASCIAN_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ASCIAN_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ASCIAN_ENTITY_RANGE;
    public static final java.util.Map<com.Polarice3.Goety.api.magic.SpellType,ForgeConfigSpec.IntValue> ASCIAN_CUBE_COLORS;

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
        builder.push("ascian_staff");
        ASCIAN_SPELL_POWER = builder.comment("无影魔杖提供的通用巫法强度加成。").defineInRange("spell_power", 2.0, 0.0, 2048.0);
        ASCIAN_COOLDOWN_REDUCTION = builder.comment("无影魔杖提供的通用巫法冷却缩减。").defineInRange("cooldown_reduction", 0.20, -0.95, 0.95);
        ASCIAN_ATTACK_DAMAGE = builder.comment("无影魔杖的攻击伤害。").defineInRange("attack_damage", 12.0, -2048.0, 2048.0);
        ASCIAN_ATTACK_SPEED = builder.comment("无影魔杖的攻击速度。").defineInRange("attack_speed", 1.6, -20.0, 20.0);
        ASCIAN_ENTITY_RANGE = builder.comment("无影魔杖的实体范围。").defineInRange("entity_range", 3.5, 0.0, 64.0);
        builder.push("cube_colors");
        var colors = new java.util.EnumMap<com.Polarice3.Goety.api.magic.SpellType,ForgeConfigSpec.IntValue>(com.Polarice3.Goety.api.magic.SpellType.class);
        for(var school:com.starfantasy.goety.magic.FinalStaffSchool.values()){
            int color=switch(school){case ILL->0xFFAA00;case NECROMANCY,WIND->0x55FFFF;case GEOMANCY->0xAAAAAA;
                case STORM->0xFFFF55;case FROST->0x5555FF;case WILD->0x55FF55;case ABYSS->0x0000AA;case VOID->0xAA00AA;case NETHER->0xAA0000;};
            colors.put(school.type,builder.comment("该学派立方体的 RGB 颜色，范围 0x000000 至 0xFFFFFF。").defineInRange(school.id,color,0,0xFFFFFF));
        }
        ASCIAN_CUBE_COLORS=java.util.Collections.unmodifiableMap(colors);
        builder.pop();builder.pop();
        SPEC = builder.build();
    }
}
