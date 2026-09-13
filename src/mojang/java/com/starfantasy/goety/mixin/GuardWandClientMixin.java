package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.starfantasy.goety.client.GuardUseInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Kept in the client mixin list so a dedicated server never loads client input classes. */
@Mixin(value = DarkWand.class, remap = false)
public abstract class GuardWandClientMixin {
    @Inject(method = "m_7203_", at = @At("HEAD"), remap = false)
    private void starfantasy$consumeUsePress(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        // Goety starts charging only on the server and returns PASS on the client.
        // Consume the local attempt, never wait for isUsingItem or a SUCCESS result.
        if (level.isClientSide) GuardUseInput.attempted(player, player.getItemInHand(hand));
    }
}
