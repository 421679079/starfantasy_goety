package com.starfantasy.goety.config;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/** Common combat tuning for Apollyon. */
public final class ApollyonConfig {
    private static final List<String> DEFAULT_DEATH_DROPS = List.of(
            "starfantasy_goety:halo_of_hades");

    public static final double DEFAULT_BOSS_HEALTH = 666.0D;
    public static final double DEFAULT_ARMOR = 8.0D;
    public static final double DEFAULT_APOSTLE_GLORIOUS_HEALTH = 100.0D;
    public static final double DEFAULT_APOSTLE_RISEN_HEALTH = 120.0D;
    public static final double DEFAULT_APOSTLE_WITCH_KING_HEALTH = 120.0D;
    public static final double DEFAULT_APOSTLE_BOW_DAMAGE = 12.0D;
    public static final double DEFAULT_APOSTLE_DAMAGE_CAP = 20.0D;
    public static final double DEFAULT_APOSTLE_DAMAGE_REDUCTION = 0.25D;
    public static final double DEFAULT_APOSTLE_ARMOR = 6.0D;
    public static final double DEFAULT_APOSTLE_MAGIC_DAMAGE_REDUCTION = 0.35D;

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue HARD_MODE;

    private static final ForgeConfigSpec.DoubleValue BOSS_HEALTH;
    private static final ForgeConfigSpec.DoubleValue HEALTH_REGENERATION;
    private static final ForgeConfigSpec.DoubleValue ARMOR;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_TAKEN_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue MAGIC_DAMAGE_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue FIXED_DAMAGE_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue DAMAGE_CAP;
    private static final ForgeConfigSpec.IntValue BOSS_INVULNERABILITY_TIME;
    private static final ForgeConfigSpec.IntValue DEATH_EXPERIENCE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEATH_DROPS;
    private static final ForgeConfigSpec.IntValue RANDOM_APOSTLE_HALO_DROPS;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_GLORIOUS_HEALTH;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_RISEN_HEALTH;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_WITCH_KING_HEALTH;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_BOW_DAMAGE;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_DAMAGE_CAP;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_DAMAGE_REDUCTION;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_ARMOR;
    private static final ForgeConfigSpec.DoubleValue APOSTLE_MAGIC_DAMAGE_REDUCTION;

    private ApollyonConfig() {
    }

    public static boolean hardMode() {
        return HARD_MODE.get();
    }

    public static double bossHealth() {
        return BOSS_HEALTH.get();
    }

    public static double healthRegeneration() {
        return HEALTH_REGENERATION.get();
    }

    public static double armor() {
        return ARMOR.get();
    }

    public static double damageMultiplier() {
        return DAMAGE_MULTIPLIER.get();
    }

    public static double damageTakenMultiplier() {
        return DAMAGE_TAKEN_MULTIPLIER.get();
    }

    public static double magicDamageReduction() {
        return MAGIC_DAMAGE_REDUCTION.get();
    }

    public static double fixedDamageReduction() {
        return FIXED_DAMAGE_REDUCTION.get();
    }

    public static double damageCap() {
        return DAMAGE_CAP.get();
    }

    public static int bossInvulnerabilityTime() {
        return BOSS_INVULNERABILITY_TIME.get();
    }

    public static int deathExperience() {
        return DEATH_EXPERIENCE.get();
    }

    public static List<String> deathDrops() {
        return List.copyOf(DEATH_DROPS.get());
    }

    public static List<String> defaultDeathDrops() {
        return DEFAULT_DEATH_DROPS;
    }

    public static int randomApostleHaloDrops() {
        return RANDOM_APOSTLE_HALO_DROPS.get();
    }

    public static double apostleGloriousHealth() {
        return APOSTLE_GLORIOUS_HEALTH.get();
    }

    public static double apostleRisenHealth() {
        return APOSTLE_RISEN_HEALTH.get();
    }

    public static double apostleWitchKingHealth() {
        return APOSTLE_WITCH_KING_HEALTH.get();
    }

    public static double apostleBowDamage() {
        return APOSTLE_BOW_DAMAGE.get();
    }

    public static double apostleDamageCap() {
        return APOSTLE_DAMAGE_CAP.get();
    }

    public static double apostleDamageReduction() {
        return APOSTLE_DAMAGE_REDUCTION.get();
    }

    public static double apostleArmor() {
        return APOSTLE_ARMOR.get();
    }

    public static double apostleMagicDamageReduction() {
        return APOSTLE_MAGIC_DAMAGE_REDUCTION.get();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("apollyon");
        HARD_MODE = builder
                .comment("是否开启困难模式")
                .define("hard_mode", false);
        BOSS_HEALTH = builder
                .comment("最大生命值。")
                .defineInRange("bossHealth", DEFAULT_BOSS_HEALTH, 1.0D, 100000.0D);
        HEALTH_REGENERATION = builder
                .comment("每秒生命恢复")
                .defineInRange("healthRegeneration", 2.0D, 0.0D, 100000.0D);
        ARMOR = builder
                .comment("护甲&盔甲韧性。")
                .defineInRange("armor", DEFAULT_ARMOR, 0.0D, 1000.0D);
        DAMAGE_MULTIPLIER = builder
                .comment("伤害倍率。")
                .defineInRange("damageMultiplier", 1.0D, 0.0D, 1000.0D);
        DAMAGE_TAKEN_MULTIPLIER = builder
                .comment("非施法期间的承伤倍率。")
                .defineInRange("damageTakenMultiplier", 0.5D, 0.0D, 1000.0D);
        MAGIC_DAMAGE_REDUCTION = builder
                .comment("魔法抗性。")
                .defineInRange("magicDamageReduction", 0.35D, 0.0D, 1.0D);
        FIXED_DAMAGE_REDUCTION = builder
                .comment("常驻减伤。")
                .defineInRange("fixedDamageReduction", 0.25D, 0.0D, 1.0D);
        DAMAGE_CAP = builder
                .comment("单次受伤的伤害限制。")
                .defineInRange("damageCap", 20.0D, 0.0D, 100000.0D);
        BOSS_INVULNERABILITY_TIME = builder
                .comment("受击时产生的无敌帧 （单位是tick）")
                .defineInRange("bossInvulnerabilityTime", 10, 0, Integer.MAX_VALUE);
        DEATH_EXPERIENCE = builder
                .comment("掉落的经验值。")
                .defineInRange("deathExperience", 10000, 0, Integer.MAX_VALUE);
        DEATH_DROPS = builder
                .comment("掉落的物品: modid:item[:count|min-max].")
                .defineListAllowEmpty(List.of("deathDrops"), DEFAULT_DEATH_DROPS,
                        ApollyonConfig::isValidDeathDrop);
        RANDOM_APOSTLE_HALO_DROPS = builder
                .comment("掉落的神环数量。")
                .defineInRange("randomApostleHaloDrops", 6, 0, 12);
        builder.push("apostle");
        APOSTLE_GLORIOUS_HEALTH = builder
                .comment("运动会阶段，荣耀之名使徒的血量。")
                .defineInRange("gloriousHealth", DEFAULT_APOSTLE_GLORIOUS_HEALTH,
                        1.0D, 100000.0D);
        APOSTLE_RISEN_HEALTH = builder
                .comment("运动会阶段，不灭重生的血量。")
                .defineInRange("risenHealth", DEFAULT_APOSTLE_RISEN_HEALTH,
                        1.0D, 100000.0D);
        APOSTLE_WITCH_KING_HEALTH = builder
                .comment("运动会阶段，女巫之王的血量。")
                .defineInRange("witchKingHealth", DEFAULT_APOSTLE_WITCH_KING_HEALTH,
                        1.0D, 100000.0D);
        APOSTLE_BOW_DAMAGE = builder
                .comment("运动会阶段，使徒的弓箭基础伤害。")
                .defineInRange("bowDamage", DEFAULT_APOSTLE_BOW_DAMAGE,
                        0.0D, 100000.0D);
        APOSTLE_DAMAGE_CAP = builder
                .comment("运动会阶段，使徒单次受伤的伤害限制。")
                .defineInRange("damageCap", DEFAULT_APOSTLE_DAMAGE_CAP,
                        0.0D, 100000.0D);
        APOSTLE_DAMAGE_REDUCTION = builder
                .comment("运动会阶段，使徒的常驻减伤。")
                .defineInRange("damageReduction", DEFAULT_APOSTLE_DAMAGE_REDUCTION,
                        0.0D, 1.0D);
        APOSTLE_ARMOR = builder
                .comment("运动会阶段，使徒的护甲&盔甲韧性。")
                .defineInRange("armor", DEFAULT_APOSTLE_ARMOR, 0.0D, 1000.0D);
        APOSTLE_MAGIC_DAMAGE_REDUCTION = builder
                .comment("运动会阶段，使徒的魔法抗性。")
                .defineInRange("magicDamageReduction", DEFAULT_APOSTLE_MAGIC_DAMAGE_REDUCTION,
                        0.0D, 1.0D);
        builder.pop();
        builder.pop();
        SPEC = builder.build();
    }

    private static boolean isValidString(Object value) {
        return value instanceof String string && !string.isBlank();
    }

    private static boolean isValidDeathDrop(Object value) {
        return isValidString(value) && !((String) value).trim().startsWith("#");
    }
}
