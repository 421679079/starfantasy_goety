package com.starfantasy.goety.ritual;

import com.Polarice3.Goety.common.blocks.entities.DarkAltarBlockEntity;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.Polarice3.Goety.common.items.revive.ReviveServantItem;
import com.Polarice3.Goety.common.ritual.Ritual;
import com.Polarice3.Goety.common.ritual.RitualRequirements;
import com.starfantasy.goety.entity.HadesServantEntity;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.goety.servant.ServantOwnershipData;
import com.Polarice3.Goety.common.entities.ally.Summoned;
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

/** Halos follow their existing recipes; Apollyon's crown restores her through a Sabbath ritual. */
public final class ServantHaloRitual extends Ritual {
    private final boolean crownRevival;
    public ServantHaloRitual(RitualRecipe recipe) { this(recipe, false); }
    public ServantHaloRitual(RitualRecipe recipe, boolean crownRevival) {
        super(recipe);
        this.crownRevival = crownRevival;
    }

    private boolean summonsApollyon() {
        return this.recipe.getEntityToSummon() == ApollyonEntityRegistry.APOLLYON_SERVANT.get();
    }

    private boolean canRevive(Level level, Player player, ItemStack stack) {
        if (!FadedHaloItem.belongsTo(stack, player)) return false;
        if (this.crownRevival) {
            if (!this.summonsApollyon() || !FadedHaloItem.isApollyon(stack)
                    || !stack.m_150930_(HaloItemRegistry.FADED_CROWN.get())) return false;
        } else if (!stack.m_150930_(HaloItemRegistry.FADED_HALO.get())) return false;
        CompoundTag saved = stack.m_41783_().m_128469_("entity");
        // Also reject a copied halo whose original servant is still alive in an unloaded chunk.
        if (level instanceof ServerLevel server && saved.m_128403_("UUID")) {
            UUID id = saved.m_128342_("UUID");
            if (ServantOwnershipData.contains(server, id)) return false;
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
        boolean convert = this.summonsApollyon() != FadedHaloItem.isApollyon(stack);
        Entity saved = convert ? this.recipe.getEntityToSummon().m_20615_(level)
                : ReviveServantItem.getSummon(stack, level);
        if (!(saved instanceof Summoned servant)
                || !convert && !player.m_20148_().equals(servant.getOwnerId())) return;
        if (!this.summonsApollyon() && servant instanceof HadesServantEntity hades) hades.prepareRevival();
        else if (this.summonsApollyon() && servant instanceof ApollyonServantEntity apollyon) apollyon.prepareRevival();
        else return;
        // Conversion creates a fresh owned servant; revival preserves the stored identity/equipment.
        this.prepareLivingEntityForSpawn(servant, level, pos, altar, player, convert);
        if (level.m_7967_(servant)) {
            stack.m_41774_(1);
            if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.f_10580_.m_68256_(serverPlayer, servant);
            super.finish(level, pos, altar, player, stack);
        }
    }
}
