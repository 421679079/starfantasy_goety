package com.starfantasy.goety.registry;

import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.StarFantasyGoetyMod;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Spell attributes owned entirely by this add-on. */
public final class SpellAttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.Keys.ATTRIBUTES, StarFantasyGoetyMod.CONTENT_NAMESPACE);

    public static final RegistryObject<Attribute> SPELL_POWER = spellPower("spell_power");
    public static final RegistryObject<Attribute> COOLDOWN_REDUCTION = cooldown("cooldown_reduction");

    public static final RegistryObject<Attribute> ILL_SPELL_POWER = spellPower("ill_spell_power");
    public static final RegistryObject<Attribute> ILL_COOLDOWN_REDUCTION = cooldown("ill_cooldown_reduction");
    public static final RegistryObject<Attribute> NECROMANCY_SPELL_POWER = spellPower("necromancy_spell_power");
    public static final RegistryObject<Attribute> NECROMANCY_COOLDOWN_REDUCTION = cooldown("necromancy_cooldown_reduction");
    public static final RegistryObject<Attribute> GEOMANCY_SPELL_POWER = spellPower("geomancy_spell_power");
    public static final RegistryObject<Attribute> GEOMANCY_COOLDOWN_REDUCTION = cooldown("geomancy_cooldown_reduction");
    public static final RegistryObject<Attribute> WIND_SPELL_POWER = spellPower("wind_spell_power");
    public static final RegistryObject<Attribute> WIND_COOLDOWN_REDUCTION = cooldown("wind_cooldown_reduction");
    public static final RegistryObject<Attribute> STORM_SPELL_POWER = spellPower("storm_spell_power");
    public static final RegistryObject<Attribute> STORM_COOLDOWN_REDUCTION = cooldown("storm_cooldown_reduction");
    public static final RegistryObject<Attribute> FROST_SPELL_POWER = spellPower("frost_spell_power");
    public static final RegistryObject<Attribute> FROST_COOLDOWN_REDUCTION = cooldown("frost_cooldown_reduction");
    public static final RegistryObject<Attribute> WILD_SPELL_POWER = spellPower("wild_spell_power");
    public static final RegistryObject<Attribute> WILD_COOLDOWN_REDUCTION = cooldown("wild_cooldown_reduction");
    public static final RegistryObject<Attribute> ABYSS_SPELL_POWER = spellPower("abyss_spell_power");
    public static final RegistryObject<Attribute> ABYSS_COOLDOWN_REDUCTION = cooldown("abyss_cooldown_reduction");
    public static final RegistryObject<Attribute> VOID_SPELL_POWER = spellPower("void_spell_power");
    public static final RegistryObject<Attribute> VOID_COOLDOWN_REDUCTION = cooldown("void_cooldown_reduction");
    public static final RegistryObject<Attribute> NETHER_SPELL_POWER = spellPower("nether_spell_power");
    public static final RegistryObject<Attribute> NETHER_COOLDOWN_REDUCTION = cooldown("nether_cooldown_reduction");

    private static final List<RegistryObject<Attribute>> ALL = List.of(
            SPELL_POWER,
            COOLDOWN_REDUCTION,
            ILL_SPELL_POWER,
            ILL_COOLDOWN_REDUCTION,
            NECROMANCY_SPELL_POWER,
            NECROMANCY_COOLDOWN_REDUCTION,
            GEOMANCY_SPELL_POWER,
            GEOMANCY_COOLDOWN_REDUCTION,
            WIND_SPELL_POWER,
            WIND_COOLDOWN_REDUCTION,
            STORM_SPELL_POWER,
            STORM_COOLDOWN_REDUCTION,
            FROST_SPELL_POWER,
            FROST_COOLDOWN_REDUCTION,
            WILD_SPELL_POWER,
            WILD_COOLDOWN_REDUCTION,
            ABYSS_SPELL_POWER,
            ABYSS_COOLDOWN_REDUCTION,
            VOID_SPELL_POWER,
            VOID_COOLDOWN_REDUCTION,
            NETHER_SPELL_POWER,
            NETHER_COOLDOWN_REDUCTION);

    private SpellAttributeRegistry() {
    }

    public static void init(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(SpellAttributeRegistry::addPlayerAttributes);
    }

    public static RegistryObject<Attribute> spellPowerFor(SpellType type) {
        return switch (type) {
            case ILL -> ILL_SPELL_POWER;
            case NECROMANCY -> NECROMANCY_SPELL_POWER;
            case GEOMANCY -> GEOMANCY_SPELL_POWER;
            case WIND -> WIND_SPELL_POWER;
            case STORM -> STORM_SPELL_POWER;
            case FROST -> FROST_SPELL_POWER;
            case WILD -> WILD_SPELL_POWER;
            case ABYSS -> ABYSS_SPELL_POWER;
            case VOID -> VOID_SPELL_POWER;
            case NETHER -> NETHER_SPELL_POWER;
            default -> null;
        };
    }

    public static RegistryObject<Attribute> cooldownFor(SpellType type) {
        return switch (type) {
            case ILL -> ILL_COOLDOWN_REDUCTION;
            case NECROMANCY -> NECROMANCY_COOLDOWN_REDUCTION;
            case GEOMANCY -> GEOMANCY_COOLDOWN_REDUCTION;
            case WIND -> WIND_COOLDOWN_REDUCTION;
            case STORM -> STORM_COOLDOWN_REDUCTION;
            case FROST -> FROST_COOLDOWN_REDUCTION;
            case WILD -> WILD_COOLDOWN_REDUCTION;
            case ABYSS -> ABYSS_COOLDOWN_REDUCTION;
            case VOID -> VOID_COOLDOWN_REDUCTION;
            case NETHER -> NETHER_COOLDOWN_REDUCTION;
            default -> null;
        };
    }

    private static RegistryObject<Attribute> spellPower(String path) {
        return ATTRIBUTES.register(
                path,
                () -> new RangedAttribute(description(path), 0.0D, 0.0D, 2048.0D).m_22084_(true));
    }

    private static RegistryObject<Attribute> cooldown(String path) {
        // Base 1 lets MULTIPLY_TOTAL modifiers display naturally as percentages.
        return ATTRIBUTES.register(
                path,
                () -> new RangedAttribute(description(path), 1.0D, 0.05D, 4.0D).m_22084_(true));
    }

    private static String description(String path) {
        return "attribute.name." + StarFantasyGoetyMod.CONTENT_NAMESPACE + "." + path;
    }

    private static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        for (RegistryObject<Attribute> attribute : ALL) {
            event.add(EntityType.f_20532_, attribute.get());
        }
    }
}
