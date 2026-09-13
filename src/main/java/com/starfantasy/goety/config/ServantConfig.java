package com.starfantasy.goety.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-side tuning for permanent reward servants. */
public final class ServantConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue HEALTH, ARMOR, ATTACK_DAMAGE,
            MAGIC_RESISTANCE, DAMAGE_REDUCTION, DAMAGE_CAP, REGENERATION;
    public static final ForgeConfigSpec.IntValue INVULNERABILITY_TICKS;

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
        b.pop();
        SPEC = b.build();
    }

    private ServantConfig() { }
}
