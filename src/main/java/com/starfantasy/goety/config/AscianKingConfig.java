package com.starfantasy.goety.config;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/** Public boss attributes and phase effects; combat rules and spell damage are internal. */
public final class AscianKingConfig {
    public static final List<String> DEFAULT_DEATH_DROPS = List.of(
            "starfantasy_goety:ascian_mask:1",
            "starfantasy_goety:ascian_altar:1", "minecraft:nether_star:1", "minecraft:netherite_ingot:2-9",
            "goety:shadow_essence:1", "goety:forbidden_fragment:1-2");
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue HEALTH, ARMOR, DAMAGE_MULTIPLIER,
            MAGIC_REDUCTION, FIXED_REDUCTION, DAMAGE_CAP, FINAL_HEAL_PER_SECOND, RANGED_PROTECTION_DISTANCE;
    public static final ForgeConfigSpec.IntValue EXPERIENCE, INVULNERABILITY, SERVANT_BUFF_LEVEL;
    public static final ForgeConfigSpec.BooleanValue HARD_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DROPS;
    public static final java.util.ArrayList<ForgeConfigSpec.ConfigValue<List<? extends String>>> BUFFS = new java.util.ArrayList<>(),
            DEBUFFS = new java.util.ArrayList<>(), HIT_EFFECTS = new java.util.ArrayList<>();
    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        HARD_MODE = b.comment("是否开启困难模式").define("hard_mode", false);
        HEALTH = b.comment("最大生命值。").defineInRange("bossHealth", 600D, 1D, 100000D);
        ARMOR = b.comment("护甲&盔甲韧性。").defineInRange("armor", 10D, 0D, 1000D);
        DAMAGE_MULTIPLIER = b.comment("伤害倍率。").defineInRange("damageMultiplier", 1D, 0D, 1000D);
        FINAL_HEAL_PER_SECOND = b.comment("最终阶段每秒回复的生命值。").defineInRange("finalHealPerSecond", .5D, 0D, 100000D);
        MAGIC_REDUCTION = b.comment("魔法抗性。").defineInRange("magicDamageReduction", .4D, -1D, 1D);
        FIXED_REDUCTION = b.comment("非破韧期间的减伤。").defineInRange("fixedDamageReduction", .5D, 0D, 1D);
        DAMAGE_CAP = b.comment("单次受伤的伤害限制。").defineInRange("damageCap", 20D, 0D, 100000D);
        RANGED_PROTECTION_DISTANCE = b.comment("远程保护距离").defineInRange("rangedProtectionDistance", 20D, 5D, Double.MAX_VALUE);
        SERVANT_BUFF_LEVEL = b.comment("仆从附加的强效等级").defineInRange("servantBuffLevel", 5, 0, 256);
        EXPERIENCE = b.comment("掉落经验。").defineInRange("deathExperience", 5000, 0, Integer.MAX_VALUE);
        DROPS = b.comment("掉落的物品: modid:item[:count|min-max]。").defineListAllowEmpty(List.of("deathDrops"), DEFAULT_DEATH_DROPS, v -> v instanceof String);
        INVULNERABILITY = b.comment("受击时产生的无敌帧 （单位是tick）").defineInRange("bossInvulnerabilityTime", 10, 0, Integer.MAX_VALUE);
        List<List<String>> buffs=List.of(List.of("goety:fiery_aura:10"),List.of("goety:chill_hide:1"),
                List.of("goety:electrified:1"),List.of("goety:deflective:10"),List.of(),
                List.of("minecraft:resistance:2","goety:fiery_aura:10","goety:chill_hide:1","goety:electrified:1","goety:deflective:10"));
        List<String> combined=List.of("goety:burn_hex:1:200","goety:flammable:5:200","goety:freezing:2:200",
                "star_fantasy:unsteady:5:200","minecraft:hunger:1:200","goety:plunge:1:200");
        List<List<String>> debuffs=List.of(List.of("goety:burn_hex:1:200","goety:flammable:5:200"),List.of("goety:freezing:2:200"),
                List.of("star_fantasy:unsteady:5:200"),List.of("goety:plunge:1:200"),List.of("minecraft:hunger:1:200","minecraft:poison:4:200"),combined);
        List<List<String>> hits=List.of(List.of("goety:busted:1:200"),List.of("minecraft:slowness:2:200"),
                List.of("goety:spasms:2:200"),List.of("goety:void_touched:1:200"),List.of("minecraft:wither:4:200"),List.of("goety:doom:5:20"));
        for(int i=0;i<6;i++) {
            b.push("phase"+(i+1));
            BUFFS.add(b.comment("自身增益：modid:effect:等级").defineListAllowEmpty(List.of("buffs"),buffs.get(i),v->v instanceof String));
            DEBUFFS.add(b.comment("玩家减益：modid:effect:等级:持续tick").defineListAllowEmpty(List.of("debuffs"),debuffs.get(i),v->v instanceof String));
            HIT_EFFECTS.add(b.comment(i==5?"攻击命中附加的额外负面效果：modid:effect:等级:持续tick"
                    :"攻击命中附加的负面效果：modid:effect:等级:持续tick").defineListAllowEmpty(List.of("hitEffects"),hits.get(i),v->v instanceof String));
            b.pop();
        }
        SPEC=b.build();
    }
    public static float scale(double amount) { return (float)(amount*DAMAGE_MULTIPLIER.get()); }
    private AscianKingConfig() {}
}
