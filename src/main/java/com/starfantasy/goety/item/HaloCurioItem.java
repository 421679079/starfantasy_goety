package com.starfantasy.goety.item;

import com.Polarice3.Goety.api.magic.SpellType;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.goety.registry.SpellAttributeRegistry;
import com.starfantasy.goety.combat.HaloPotionEffects;
import com.starfantasy.goety.combat.HaloProtection;
import com.starfantasy.library.combat.StarFantasyCombatAttributes;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Apostle and Hades halos worn in Curios' head slot. */
public final class HaloCurioItem extends Item implements ICurioItem {
    private static final double SPELL_POWER = 1.0D;
    private static final double COOLDOWN_REDUCTION = 0.10D;
    private static final double RESISTANCE = 0.10D;

    private final SpellType school;
    private final boolean hades;

    public HaloCurioItem(Properties properties, SpellType school, boolean hades) {
        super(properties);
        this.school = school;
        this.hades = hades;
    }

    public static HaloCurioItem school(SpellType school) {
        return new HaloCurioItem(new Item.Properties().m_41487_(1), school, false);
    }

    public static HaloCurioItem unschooled() {
        return new HaloCurioItem(new Item.Properties().m_41487_(1), null, false);
    }

    public static HaloCurioItem hades() {
        return new HaloCurioItem(new Item.Properties().m_41487_(1), null, true);
    }

    @Override
    public boolean m_41386_(net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "head".equals(slotContext.identifier());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.cosmetic()
                || !(slotContext.entity() instanceof Player player)
                || player.m_9236_().f_46443_ || !player.m_6084_()) {
            return;
        }
        if (this == HaloItemRegistry.HALO_OF_THE_PYRE_LORD.get()) {
            player.m_20095_();
        }
        HaloProtection.clearBlockedEffects(this, player);
        HaloPotionEffects.apply(this, player);
        if (this == HaloItemRegistry.HALO_OF_THE_PROFANE.get()
                && player.f_19797_ % 20 == 0) {
            FoodData foodData = player.m_36324_();
            int food = foodData.m_38702_();
            if (food < 20) {
                foodData.m_38705_(Math.min(20, food + (food < 4 ? 2 : 1)));
            } else {
                foodData.m_38717_(Math.min(20.0F, foodData.m_38722_() + 1.0F));
            }
        } else if (this == HaloItemRegistry.HALO_OF_THE_GLORIOUS.get()
                && player.f_19797_ % 200 == 0) {
            player.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 300, 0));
        }
    }

    @Override
    public void m_7373_(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.m_7373_(stack, level, tooltip, flag);
        if (this == HaloItemRegistry.HALO_OF_THE_PROFANE.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_profane.food")
                    .m_130940_(ChatFormatting.GREEN));
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_profane.immunity")
                    .m_130940_(ChatFormatting.GREEN));
        } else if (this == HaloItemRegistry.HALO_OF_THE_GLORIOUS.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_glorious")
                    .m_130940_(ChatFormatting.YELLOW));
        } else if (this == HaloItemRegistry.HALO_OF_THE_PYRE_LORD.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_pyre_lord")
                    .m_130940_(ChatFormatting.GOLD));
        } else if (this == HaloItemRegistry.HALO_OF_THE_CRUEL.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_cruel")
                    .m_130940_(ChatFormatting.AQUA));
        } else if (this == HaloItemRegistry.HALO_OF_THE_DEFILER.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_defiler")
                    .m_130940_(ChatFormatting.GREEN));
        } else if (this == HaloItemRegistry.HALO_OF_THE_TERRIBLE.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_terrible")
                    .m_130940_(ChatFormatting.YELLOW));
        } else if (this == HaloItemRegistry.HALO_OF_THE_DARK.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_dark")
                    .m_130940_(ChatFormatting.DARK_PURPLE));
        } else if (this == HaloItemRegistry.HALO_OF_THE_GREAT_SHADOW.get()) {
            tooltip.add(Component.m_237115_("tooltip.starfantasy_goety.halo_of_the_great_shadow")
                    .m_130940_(ChatFormatting.GRAY));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID slotUuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();

        if (this.school != null) {
            modifiers.put(
                    SpellAttributeRegistry.spellPowerFor(this.school).get(),
                    modifier(slotUuid, "school_spell_power", SPELL_POWER,
                            AttributeModifier.Operation.ADDITION));
            modifiers.put(
                    SpellAttributeRegistry.cooldownFor(this.school).get(),
                    modifier(slotUuid, "school_cooldown_reduction", COOLDOWN_REDUCTION,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this.hades) {
            modifiers.put(
                    SpellAttributeRegistry.SPELL_POWER.get(),
                    modifier(slotUuid, "spell_power", SPELL_POWER,
                            AttributeModifier.Operation.ADDITION));
            modifiers.put(
                    SpellAttributeRegistry.COOLDOWN_REDUCTION.get(),
                    modifier(slotUuid, "cooldown_reduction", COOLDOWN_REDUCTION,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
            modifiers.put(
                    StarFantasyCombatAttributes.RESISTANCE.get(),
                    modifier(slotUuid, "resistance", RESISTANCE,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
        }

        if (this == HaloItemRegistry.HALO_OF_THE_PYRE_LORD.get()) {
            modifiers.put(StarFantasyCombatAttributes.FIRE_RESISTANCE.get(),
                    modifier(slotUuid, "fire_resistance", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_GLORIOUS.get()) {
            modifiers.put(StarFantasyCombatAttributes.PHYSICAL_RESISTANCE.get(),
                    modifier(slotUuid, "physical_resistance", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_ATROCIOUS.get()) {
            modifiers.put(StarFantasyCombatAttributes.ARMOR_PENETRATION.get(),
                    modifier(slotUuid, "armor_penetration", 0.20D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (this == HaloItemRegistry.HALO_OF_THE_CRUEL.get()) {
            modifiers.put(StarFantasyCombatAttributes.FROST_RESISTANCE.get(),
                    modifier(slotUuid, "frost_resistance", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_DEFILER.get()) {
            modifiers.put(StarFantasyCombatAttributes.POISON_RESISTANCE.get(),
                    modifier(slotUuid, "poison_resistance", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_TERRIBLE.get()) {
            modifiers.put(StarFantasyCombatAttributes.LIGHTNING_RESISTANCE.get(),
                    modifier(slotUuid, "lightning_resistance", 0.25D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_DARK.get()) {
            modifiers.put(StarFantasyCombatAttributes.VOID_RESISTANCE.get(),
                    modifier(slotUuid, "void_resistance", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else if (this == HaloItemRegistry.HALO_OF_THE_GREAT_SHADOW.get()) {
            modifiers.put(StarFantasyCombatAttributes.PROJECTILE_RESISTANCE.get(),
                    modifier(slotUuid, "projectile_resistance", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return modifiers.build();
    }

    private static AttributeModifier modifier(
            UUID slotUuid, String name, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(slotUuid, "starfantasy_goety.halo." + name, amount, operation);
    }
}
