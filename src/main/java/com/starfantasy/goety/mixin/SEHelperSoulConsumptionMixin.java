package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.capabilities.soulenergy.ISoulEnergy;
import com.Polarice3.Goety.utils.SEHelper;
import com.starfantasy.goety.combat.HadesJudgmentSouls;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Observe committed deductions after Goety's discounts and cancellable soul-loss event. */
@Mixin(value = SEHelper.class, remap = false)
public abstract class SEHelperSoulConsumptionMixin {
    @Redirect(method = "decreaseSESouls(Lnet/minecraft/world/entity/player/Player;I)Z",
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/common/capabilities/soulenergy/ISoulEnergy;decreaseSE(I)Z"),
            remap = false)
    private static boolean starfantasy$recordArcaSpend(ISoulEnergy energy, int amount, Player player, int requested) {
        int before = energy.getSoulEnergy();
        boolean result = energy.decreaseSE(amount);
        HadesJudgmentSouls.spent(player, Math.max(0, before - energy.getSoulEnergy()));
        return result;
    }

    @Redirect(method = "decreaseSouls(Lnet/minecraft/world/entity/player/Player;I)V",
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/api/items/magic/ITotem;decreaseSouls(Lnet/minecraft/world/item/ItemStack;I)V"),
            remap = false)
    private static void starfantasy$recordTotemSpend(ItemStack totem, int amount, Player player, int requested) {
        int before = ITotem.currentSouls(totem);
        ITotem.decreaseSouls(totem, amount);
        HadesJudgmentSouls.spent(player, Math.max(0, before - ITotem.currentSouls(totem)));
    }
}
