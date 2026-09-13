package com.starfantasy.goety.ritual;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.items.revive.ReviveServantItem;
import com.Polarice3.Goety.common.ritual.Ritual;
import com.Polarice3.Goety.common.ritual.RitualRequirements;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.item.FadedHaloItem;
import java.util.List;
import java.util.UUID;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public final class HadesRevivalRitual extends Ritual {
    public HadesRevivalRitual(RitualRecipe recipe) { super(recipe); }

    private boolean canRevive(Level level, Player player, ItemStack stack) {
        if (!FadedHaloItem.belongsTo(stack, player)) return false;
        CompoundTag saved = stack.m_41783_().m_128469_("entity");
        // Do not revive an identity already loaded in any dimension.
        if (level instanceof ServerLevel server && saved.m_128403_("UUID")) {
            UUID id = saved.m_128342_("UUID");
            for (ServerLevel dimension : server.m_7654_().m_129785_()) {
                if (dimension.m_8791_(id) != null) return false;
            }
        }
        return RitualRequirements.canSummon(level, player, this.recipe.getEntityToSummon());
    }

    @Override public boolean identify(Level level, BlockPos pos, Player player, ItemStack stack) {
        return canRevive(level, player, stack) && super.identify(level, pos, player, stack);
    }

    @Override public boolean isValid(Level level, BlockPos pos, DarkAltarBlockEntity altar,
                                     Player player, ItemStack stack, List<Ingredient> ingredients) {
        return canRevive(level, player, stack) && super.isValid(level, pos, altar, player, stack, ingredients);
    }

    @Override public void finish(Level level, BlockPos pos, DarkAltarBlockEntity altar,
                                 Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel) || !canRevive(level, player, stack)) return;
        Entity saved = ReviveServantItem.getSummon(stack, level);
        if (!(saved instanceof HadesServantEntity servant)
                || !player.m_20148_().equals(servant.getOwnerId())) return;
        servant.prepareRevival();
        // Keep the saved owner and equipment. Native preparation supplies position and summon effects.
        this.prepareLivingEntityForSpawn(servant, level, pos, altar, player, false);
        if (level.m_7967_(servant)) {
            stack.m_41774_(1);
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.f_10580_.m_68256_(serverPlayer, servant);
            super.finish(level, pos, altar, player, stack);
        }
    }
}
