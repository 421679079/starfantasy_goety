/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.starfantasy.goety.event;

import com.starfantasy.goety.config.StaffConfig;
import com.starfantasy.goety.registry.SpellAttributeRegistry;
import com.starfantasy.goety.registry.StaffItemRegistry;
import com.Polarice3.Goety.api.magic.SpellType;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public final class StaffAttributeEvents {
    private static final ResourceLocation GOETY_NAMELESS_STAFF = new ResourceLocation("goety", "nameless_staff");
    private static final Map<ResourceLocation, SpellType> TIER2_SCHOOL_BY_ID = Map.ofEntries(
            Map.entry(StaffItemRegistry.OMINOUS_STAFF_2, SpellType.ILL),
            Map.entry(StaffItemRegistry.GEO_STAFF_2, SpellType.GEOMANCY),
            Map.entry(StaffItemRegistry.WIND_STAFF_2, SpellType.WIND),
            Map.entry(StaffItemRegistry.STORM_STAFF_2, SpellType.STORM),
            Map.entry(StaffItemRegistry.FROST_STAFF_2, SpellType.FROST),
            Map.entry(StaffItemRegistry.WILD_STAFF_2, SpellType.WILD),
            Map.entry(StaffItemRegistry.ABYSS_STAFF_2, SpellType.ABYSS),
            Map.entry(StaffItemRegistry.VOID_STAFF_2, SpellType.VOID),
            Map.entry(StaffItemRegistry.NETHER_STAFF_2, SpellType.NETHER),
            Map.entry(GOETY_NAMELESS_STAFF, SpellType.NECROMANCY));

    private StaffAttributeEvents() {
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        EquipmentSlot slot = event.getSlotType();
        if (slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.m_41720_());
        if (id == null) {
            return;
        }
        SpellType tier2School = TIER2_SCHOOL_BY_ID.get(id);
        if (tier2School != null) {
            StaffAttributeEvents.addModifier(event, SpellAttributeRegistry.spellPowerFor(tier2School).get(), (Double)StaffConfig.TIER2_SCHOOL_SPELL_POWER.get(), id, "school_spell_power", AttributeModifier.Operation.ADDITION);
            StaffAttributeEvents.addModifier(event, SpellAttributeRegistry.cooldownFor(tier2School).get(), (Double)StaffConfig.TIER2_COOLDOWN_REDUCTION.get(), id, "cooldown_reduction", AttributeModifier.Operation.MULTIPLY_TOTAL);
            return;
        }
        if (StaffItemRegistry.FINAL_STAFF.equals((Object)id)) {
            StaffAttributeEvents.addModifier(event, SpellAttributeRegistry.SPELL_POWER.get(), (Double)StaffConfig.FINAL_SPELL_POWER.get(), id, "spell_power", AttributeModifier.Operation.ADDITION);
            StaffAttributeEvents.addModifier(event, SpellAttributeRegistry.COOLDOWN_REDUCTION.get(), (Double)StaffConfig.FINAL_COOLDOWN_REDUCTION.get(), id, "cooldown_reduction", AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    }

    private static void addModifier(ItemAttributeModifierEvent event, Attribute attribute, double value, ResourceLocation itemId, String key, AttributeModifier.Operation operation) {
        if (value == 0.0) {
            return;
        }
        event.addModifier(attribute, new AttributeModifier(StaffAttributeEvents.namedUuid(String.valueOf(itemId) + "|" + key), String.valueOf(itemId) + "_" + key, value, operation));
    }

    private static UUID namedUuid(String text) {
        return UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8));
    }
}
