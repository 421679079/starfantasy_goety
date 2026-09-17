/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Polarice3.Goety.api.magic.SpellType
 */
package com.starfantasy.goety.item;

import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.item.ConfigurableDarkStaffItem;
import java.util.List;
import java.util.function.DoubleSupplier;

public final class FinalStaffDarkItem
extends ConfigurableDarkStaffItem {
    private static final List<SpellType> FINAL_SPELL_TYPES = List.of(SpellType.ILL, SpellType.NECROMANCY, SpellType.GEOMANCY, SpellType.WIND, SpellType.STORM, SpellType.FROST, SpellType.WILD, SpellType.ABYSS, SpellType.VOID, SpellType.NETHER);

    public FinalStaffDarkItem(DoubleSupplier attackDamageValue, DoubleSupplier attackSpeedValue, DoubleSupplier entityRangeValue) {
        super(12.0, SpellType.NONE, attackDamageValue, attackSpeedValue, entityRangeValue);
    }

    public List<SpellType> getSpellTypes() {
        return FINAL_SPELL_TYPES;
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        super.initializeClient(wandExtensions ->
                com.starfantasy.goety.client.FinalStaffRenderer.initialize(consumer, wandExtensions));
    }

}
