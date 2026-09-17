package com.starfantasy.goety.ritual;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.ritual.RitualRequirements;
import com.Polarice3.Goety.common.ritual.SummonRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Native tamed/no-variant ritual with a final limit check before consuming the activation item. */
public final class HadesSummonRitual extends SummonRitual {
    public HadesSummonRitual(RitualRecipe recipe) { super(recipe, true, true); }

    @Override public void finish(Level level, BlockPos pos, DarkAltarBlockEntity altar,
                                 Player player, ItemStack stack) {
        if (RitualRequirements.canSummon(level, player, this.recipe.getEntityToSummon()))
            super.finish(level, pos, altar, player, stack);
    }
}
