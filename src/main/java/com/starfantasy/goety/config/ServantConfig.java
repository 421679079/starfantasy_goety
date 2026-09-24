package com.starfantasy.goety.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-side tuning for permanent reward servants. */
public final class ServantConfig {
    public static final ForgeConfigSpec.BooleanValue APOSTLE_ALLOW_WEATHER_CHANGE, APOLLYON_ALLOW_WEATHER_CHANGE;
    public static final ForgeConfigSpec.BooleanValue APOSTLE_AUTO_RESET_PHASE, APOLLYON_AUTO_RESET_PHASE;
    public static final ForgeConfigSpec.DoubleValue APOSTLE_HEALTH, APOSTLE_ARMOR,
            APOSTLE_DAMAGE_MULTIPLIER, APOSTLE_MAGIC_RESISTANCE, APOSTLE_NETHER_REDUCTION, APOSTLE_REGENERATION;
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue HEALTH, ARMOR, ATTACK_DAMAGE,
            MAGIC_RESISTANCE, DAMAGE_REDUCTION, DAMAGE_CAP, REGENERATION;
    public static final ForgeConfigSpec.IntValue INVULNERABILITY_TICKS;
    public static final ForgeConfigSpec.IntValue JUDGMENT_SOUL_COST;
    public static final ForgeConfigSpec.DoubleValue APOLLYON_HEALTH, APOLLYON_ARMOR,
            APOLLYON_ATTACK_DAMAGE, APOLLYON_ATTACK_SPEED, APOLLYON_MAGIC_RESISTANCE,
            APOLLYON_DAMAGE_REDUCTION, APOLLYON_DAMAGE_CAP, APOLLYON_REGENERATION;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("hades_servant");
        HEALTH = b.comment("最大生命值").defineInRange("health", 600.0, 1.0, 100000.0);
        ARMOR = b.comment("护甲").defineInRange("armor", 12.0, 0.0, 1000.0);
        ATTACK_DAMAGE = b.comment("基础攻击力，每点提升10%伤害")
                .defineInRange("attackDamage", 10.0, 0.0, 100000.0);
        MAGIC_RESISTANCE = b.comment("魔法抗性")
                .defineInRange("magicResistance", 0.35, 0.0, 1.0);
        DAMAGE_REDUCTION = b.comment("常态减伤")
                .defineInRange("damageReduction", 0.25, 0.0, 1.0);
        DAMAGE_CAP = b.comment("单次受击的伤害限制")
                .defineInRange("damageCap", 40.0, 0.0, 100000.0);
        INVULNERABILITY_TICKS = b.comment("受击后的特殊无敌帧，单位 tick")
                .defineInRange("invulnerabilityTicks", 10, 0, 12000);
        REGENERATION = b.comment("每秒恢复的生命值")
                .defineInRange("healthRegeneration", 2.0, 0.0, 100000.0);
        JUDGMENT_SOUL_COST = b.comment("消耗多少灵魂能量可触发炼狱审判")
                .defineInRange("judgmentSoulCost", 20000, 1, Integer.MAX_VALUE);
        b.pop();
        b.push("apollyon_servant");
        APOLLYON_AUTO_RESET_PHASE = b.comment("脱离仇恨一段时间后是否恢复到一阶段，false时需要使用不洁之血才能恢复")
                .define("autoResetPhase", true);
        APOLLYON_ALLOW_WEATHER_CHANGE = b.comment("是否允许改变天气").define("allowWeatherChange", true);
        APOLLYON_HEALTH = b.comment("最大生命值")
                .defineInRange("health", ApollyonConfig.DEFAULT_BOSS_HEALTH, 1.0, 100000.0);
        APOLLYON_ARMOR = b.comment("护甲")
                .defineInRange("armor", ApollyonConfig.DEFAULT_ARMOR, 0.0, 1000.0);
        APOLLYON_ATTACK_DAMAGE = b.comment("基础攻击力").defineInRange("attackDamage", 10.0, 0.0, 100000.0);
        APOLLYON_ATTACK_SPEED = b.comment("基础攻击速度").defineInRange("attackSpeed", 1.0, 0.01, 1000.0);
        APOLLYON_MAGIC_RESISTANCE = b.comment("魔法抗性")
                .defineInRange("magicResistance", ApollyonConfig.DEFAULT_MAGIC_DAMAGE_REDUCTION, 0.0, 1.0);
        APOLLYON_DAMAGE_REDUCTION = b.comment("常态减伤")
                .defineInRange("damageReduction", ApollyonConfig.DEFAULT_FIXED_DAMAGE_REDUCTION, 0.0, 1.0);
        APOLLYON_DAMAGE_CAP = b.comment("单次受击的伤害限制")
                .defineInRange("damageCap", ApollyonConfig.DEFAULT_DAMAGE_CAP, 0.0, 100000.0);
        APOLLYON_REGENERATION = b.comment("每秒恢复的生命值")
                .defineInRange("healthRegeneration", ApollyonConfig.DEFAULT_HEALTH_REGENERATION, 0.0, 100000.0);
        b.pop();
        b.push("apostle_servant");
        APOSTLE_AUTO_RESET_PHASE = b.comment("脱离仇恨一段时间后是否恢复到一阶段，false时需要使用不洁之血才能恢复")
                .define("autoResetPhase", true);
        APOSTLE_ALLOW_WEATHER_CHANGE = b.comment("是否允许改变天气").define("allowWeatherChange", true);
        APOSTLE_HEALTH = b.comment("最大生命值").defineInRange("health", 320.0, 1.0, 100000.0);
        APOSTLE_ARMOR = b.comment("护甲").defineInRange("armor", 12.0, 0.0, 1000.0);
        APOSTLE_DAMAGE_MULTIPLIER = b.comment("伤害倍率").defineInRange("damageMultiplier", 1.0, 0.0, 1000.0);
        APOSTLE_MAGIC_RESISTANCE = b.comment("魔法抗性").defineInRange("magicResistance", ApollyonConfig.DEFAULT_MAGIC_DAMAGE_REDUCTION, 0.0, 1.0);
        APOSTLE_NETHER_REDUCTION = b.comment("下界减伤").defineInRange("netherDamageReduction", 0.5, 0.0, 1.0);
        APOSTLE_REGENERATION = b.comment("每秒恢复的生命值").defineInRange("healthRegeneration", 0.5, 0.0, 100000.0);
        b.pop();
        SPEC = b.build();
    }

    private ServantConfig() { }
}
