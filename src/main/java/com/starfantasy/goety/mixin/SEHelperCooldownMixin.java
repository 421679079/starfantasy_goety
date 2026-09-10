package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.capabilities.soulenergy.FocusCooldown;
import com.Polarice3.Goety.utils.SEHelper;
import com.starfantasy.goety.magic.SpellAttributeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies addon cooldown attributes at Goety's concrete, centralized cooldown boundary. */
@Mixin(value = SEHelper.class, remap = false)
public abstract class SEHelperCooldownMixin {
    @Redirect(
            method = "addCooldown(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/Item;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/Polarice3/Goety/common/capabilities/soulenergy/FocusCooldown;addCooldown(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/Item;I)V"),
            remap = false)
    private static void starfantasy$applyFocusCooldown(
            FocusCooldown cooldown,
            Player player,
            Level level,
            Item focusItem,
            int originalTicks) {
        cooldown.addCooldown(
                player,
                level,
                focusItem,
                SpellAttributeHelper.applyFocusCooldown(originalTicks, player, focusItem));
    }
}
