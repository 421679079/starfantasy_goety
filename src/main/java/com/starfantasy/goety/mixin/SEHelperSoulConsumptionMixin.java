package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.capabilities.soulenergy.ISoulEnergy;
import com.Polarice3.Goety.utils.SEHelper;
import com.starfantasy.goety.combat.HadesJudgmentSouls;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
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

    /*
     * Goety soul-consumption compatibility (version boundary: 2.5.59.0):
     * - Before 2.5.59.0 (verified with 2.5.58.4), decreaseSouls directly calls ITotem.decreaseSouls.
     * - Since 2.5.59.0 (verified with 2.5.59.2), that call lives in decreaseSoulsNoDiscount;
     *   decreaseSouls only applies the discount and delegates to it.
     * Keep BOTH redirects: require/expect=0 permits the inactive version-specific target to be absent,
     * while the shared group MUST inject exactly once. Do not simply remove the legacy hook or make
     * all failures optional: that would break old Goety or silently stop Hades soul-spend accounting.
     * Inject at the actual deduction, not both method entries, to avoid counting delegation twice.
     * The Arca hook above is separate and remains mandatory. Re-test both released JARs when editing.
     */
    @Group(name = "starfantasy$totemSoulSpendCompat", min = 1, max = 1)
    @Redirect(method = "decreaseSouls(Lnet/minecraft/world/entity/player/Player;I)V",
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/api/items/magic/ITotem;decreaseSouls(Lnet/minecraft/world/item/ItemStack;I)V"),
            require = 0, expect = 0, allow = 1, remap = false)
    private static void starfantasy$recordTotemSpendLegacy(ItemStack totem, int amount, Player player, int requested) {
        starfantasy$recordTotemSpend(totem, amount, player);
    }

    @Group(name = "starfantasy$totemSoulSpendCompat", min = 1, max = 1)
    @Redirect(method = "decreaseSoulsNoDiscount(Lnet/minecraft/world/entity/player/Player;I)V",
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/api/items/magic/ITotem;decreaseSouls(Lnet/minecraft/world/item/ItemStack;I)V"),
            require = 0, expect = 0, allow = 1, remap = false)
    private static void starfantasy$recordTotemSpendModern(ItemStack totem, int amount, Player player, int requested) {
        starfantasy$recordTotemSpend(totem, amount, player);
    }

    @Unique
    private static void starfantasy$recordTotemSpend(ItemStack totem, int amount, Player player) {
        int before = ITotem.currentSouls(totem);
        ITotem.decreaseSouls(totem, amount);
        HadesJudgmentSouls.spent(player, Math.max(0, before - ITotem.currentSouls(totem)));
    }
}
