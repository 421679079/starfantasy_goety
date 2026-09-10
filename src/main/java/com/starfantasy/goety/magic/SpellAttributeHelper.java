package com.starfantasy.goety.magic;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.registry.SpellAttributeRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.RegistryObject;

/** Applies the addon's attributes at Goety's public spell and focus-enchantment boundaries. */
public final class SpellAttributeHelper {
    private static final double MIN_COOLDOWN_FACTOR = 0.05D;
    private static final double MAX_COOLDOWN_FACTOR = 4.0D;

    private SpellAttributeHelper() {
    }

    /**
     * Returns the effective virtual focus-enchantment levels supplied by held staffs.
     * General power stacks with the highest matching school, while multi-school spells
     * never add multiple school values together.
     */
    public static int getSpellPowerLevel(LivingEntity caster, ISpell spell) {
        double generalPower = value(caster, SpellAttributeRegistry.SPELL_POWER, 0.0D);
        double schoolPower = 0.0D;
        for (SpellType type : uniqueTypes(spell)) {
            RegistryObject<Attribute> attribute = SpellAttributeRegistry.spellPowerFor(type);
            if (attribute != null) {
                schoolPower = Math.max(schoolPower, value(caster, attribute, 0.0D));
            }
        }
        return Math.max(0, (int)Math.floor(generalPower + schoolPower));
    }

    /** Adds spell power only to the six numeric focus enchantments accepted by the active spell. */
    public static int getVirtualEnchantmentLevel(LivingEntity caster, Enchantment enchantment) {
        if (!isNumericFocusEnchantment(enchantment)) {
            return 0;
        }
        ISpell spell = WandUtil.getSpell(caster);
        if (spell == null || !accepts(spell, enchantment)) {
            return 0;
        }
        return getSpellPowerLevel(caster, spell);
    }

    /** Allows an unenchanted focus to enter Goety's enchantment branches when spell power applies. */
    public static boolean hasVirtualFocusEnchantments(LivingEntity caster) {
        ISpell spell = WandUtil.getSpell(caster);
        if (spell == null || getSpellPowerLevel(caster, spell) <= 0) {
            return false;
        }
        List<Enchantment> accepted = spell.acceptedEnchantments();
        return accepted != null && accepted.stream().anyMatch(SpellAttributeHelper::isNumericFocusEnchantment);
    }

    public static int applyCooldown(int originalTicks, LivingEntity caster, ISpell spell) {
        if (originalTicks <= 0) {
            return originalTicks;
        }

        double reduction = value(caster, SpellAttributeRegistry.COOLDOWN_REDUCTION, 1.0D) - 1.0D;
        for (SpellType type : uniqueTypes(spell)) {
            RegistryObject<Attribute> attribute = SpellAttributeRegistry.cooldownFor(type);
            if (attribute != null) {
                reduction += value(caster, attribute, 1.0D) - 1.0D;
            }
        }

        double factor = Math.max(MIN_COOLDOWN_FACTOR, Math.min(MAX_COOLDOWN_FACTOR, 1.0D - reduction));
        return Math.max(1, (int)Math.round(originalTicks * factor));
    }

    /**
     * Resolves the spell from the focus item passed to Goety's centralized cooldown
     * helper. Non-focus cooldowns are left untouched.
     */
    public static int applyFocusCooldown(int originalTicks, LivingEntity caster, Item focusItem) {
        if (!(focusItem instanceof IFocus focus) || focus.getSpell() == null) {
            return originalTicks;
        }
        return applyCooldown(originalTicks, caster, focus.getSpell());
    }

    private static Set<SpellType> uniqueTypes(ISpell spell) {
        List<SpellType> types = spell.getSpellTypes();
        return types == null ? Set.of() : new HashSet<>(types);
    }

    private static boolean accepts(ISpell spell, Enchantment enchantment) {
        List<Enchantment> accepted = spell.acceptedEnchantments();
        return accepted != null && accepted.contains(enchantment);
    }

    private static boolean isNumericFocusEnchantment(Enchantment enchantment) {
        return enchantment == ModEnchantments.POTENCY.get()
                || enchantment == ModEnchantments.RANGE.get()
                || enchantment == ModEnchantments.RADIUS.get()
                || enchantment == ModEnchantments.DURATION.get()
                || enchantment == ModEnchantments.BURNING.get()
                || enchantment == ModEnchantments.VELOCITY.get();
    }

    private static double value(
            LivingEntity caster, RegistryObject<Attribute> registeredAttribute, double fallback) {
        Attribute attribute = registeredAttribute.get();
        return caster.m_21051_(attribute) == null ? fallback : caster.m_21133_(attribute);
    }
}
