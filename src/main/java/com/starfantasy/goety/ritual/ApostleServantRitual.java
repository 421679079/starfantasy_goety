package com.starfantasy.goety.ritual;

import com.Polarice3.Goety.common.ritual.SummonRitual;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.starfantasy.goety.entity.ApostleServantEntity;
import com.starfantasy.goety.registry.HaloItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** All twelve recipes use this single factory and entity type. */
public final class ApostleServantRitual extends SummonRitual {
    public ApostleServantRitual(RitualRecipe recipe) {
        super(recipe, true, true);
    }
    private int title() {
        int found = -1;
        for (int i = 0; i < HaloItemRegistry.APOSTLE_HALOS.size(); i++) {
            if (recipe.getActivationItem().test(new ItemStack(HaloItemRegistry.APOSTLE_HALOS.get(i).get()))) {
                found = i; break;
            }
        }
        if (found < 0) throw new IllegalArgumentException("Apostle servant ritual requires an Apostle halo");
        return found;
    }
    @Override public void initSummoned(LivingEntity entity, Level level, BlockPos pos,
            DarkAltarBlockEntity altar, Player player) {
        if (entity instanceof ApostleServantEntity servant) {
            servant.setTrueOwner(player);
            servant.initializeTitle(title());
        }
    }
}
