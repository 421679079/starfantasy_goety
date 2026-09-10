/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Polarice3.Goety.api.magic.SpellType
 *  com.Polarice3.Goety.common.items.magic.DarkStaff
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraftforge.common.ForgeMod
 */
package com.starfantasy.goety.item;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.magic.DarkStaff;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;

public class ConfigurableDarkStaffItem
extends DarkStaff {
    private final DoubleSupplier attackDamageValue;
    private final DoubleSupplier attackSpeedValue;
    private final DoubleSupplier entityRangeValue;
    private final UUID entityRangeUuid;

    public ConfigurableDarkStaffItem(double baseDamage, SpellType spellType, DoubleSupplier attackDamageValue, DoubleSupplier attackSpeedValue, DoubleSupplier entityRangeValue) {
        super(baseDamage, spellType);
        this.attackDamageValue = attackDamageValue;
        this.attackSpeedValue = attackSpeedValue;
        this.entityRangeValue = entityRangeValue;
        this.entityRangeUuid = UUID.nameUUIDFromBytes(("range|" + spellType.name()).getBytes());
    }

    public ConfigurableDarkStaffItem(Item.Properties properties, double baseDamage, SpellType spellType, DoubleSupplier attackDamageValue, DoubleSupplier attackSpeedValue, DoubleSupplier entityRangeValue) {
        super(properties, baseDamage, spellType);
        this.attackDamageValue = attackDamageValue;
        this.attackSpeedValue = attackSpeedValue;
        this.entityRangeValue = entityRangeValue;
        this.entityRangeUuid = UUID.nameUUIDFromBytes(("range|" + spellType.name()).getBytes());
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }
        double panelDamage = this.attackDamageValue.getAsDouble();
        double panelSpeed = this.attackSpeedValue.getAsDouble();
        double panelRange = this.entityRangeValue.getAsDouble();
        double damageModifier = panelDamage - 1.0;
        double speedModifier = panelSpeed - 4.0;
        double rangeModifier = panelRange - 3.0;
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put((Object)Attributes.f_22281_, (Object)new AttributeModifier(f_41374_, "Weapon modifier", damageModifier, AttributeModifier.Operation.ADDITION)).put((Object)Attributes.f_22283_, (Object)new AttributeModifier(f_41375_, "Weapon modifier", speedModifier, AttributeModifier.Operation.ADDITION));
        if (rangeModifier != 0.0) {
            builder.put((Object)((Attribute)ForgeMod.ENTITY_REACH.get()), (Object)new AttributeModifier(this.entityRangeUuid, "Weapon modifier", rangeModifier, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

}
