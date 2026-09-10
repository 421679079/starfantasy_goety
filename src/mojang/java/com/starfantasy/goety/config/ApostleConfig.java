package com.starfantasy.goety.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Options for Goety's original Apostle; Apollyon and pageant actors are excluded. */
public final class ApostleConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_APOSTLE_MOE;
    public static final ForgeConfigSpec.DoubleValue HARD_MAGIC_RESISTANCE;
    public static final ForgeConfigSpec.DoubleValue DAMAGE_TAKEN_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue IMPROVED_ARCHERY;
    public static final ForgeConfigSpec.BooleanValue ENHANCED_FIREBALL;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLE_APOSTLE_MOE = builder.comment("是否开启使徒萌化")
                .define("enableApostleMoe", true);
        HARD_MAGIC_RESISTANCE = builder.comment("需要开启Goety的apostleHardMagicResistance才生效，原版为85%魔抗。")
                .defineInRange("hardMagicResistance", 0.35D, 0.0D, 1.0D);
        DAMAGE_TAKEN_MULTIPLIER = builder.comment("使徒非施法期间的承伤倍率，原版为1.0。")
                .defineInRange("damageTakenMultiplier", 0.7D, 0.0D, 1000.0D);
        IMPROVED_ARCHERY = builder.comment("优化死亡之箭表现，使其更容易看清。")
                .define("improvedArchery", true);
        ENHANCED_FIREBALL = builder.comment("使徒会根据头衔施放不同的特色法术，修改Goety的apostleMagicDamage可配置伤害。")
                .define("enhancedFireball", true);
        SPEC = builder.build();
    }
    private ApostleConfig() {}
}
