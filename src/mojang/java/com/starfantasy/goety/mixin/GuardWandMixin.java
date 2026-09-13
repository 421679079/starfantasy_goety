package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.starfantasy.goety.magic.guard.GuardChannel;
import com.starfantasy.goety.magic.guard.GuardSpell;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Only the guard focus replaces the normal repeated-shot channel lifecycle. */
@Mixin(value = DarkWand.class, remap = false)
public abstract class GuardWandMixin {
    @Inject(method = "m_7203_", at = @At("RETURN"), remap = false)
    private void starfantasy$beginGuard(Level level, Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        ItemStack staff = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server && player.isUsingItem() && player.getUseItem() == staff
                && GuardChannel.isGuardStaff(staff)) GuardChannel.begin(server, staff);
    }
    @Inject(method = "m_8105_", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$guardDuration(ItemStack stack, CallbackInfoReturnable<Integer> callback) {
        if (GuardChannel.isGuardStaff(stack)) callback.setReturnValue(GuardSpell.duration(stack));
    }
    @Inject(method = "m_5929_", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$guardTick(Level level, LivingEntity caster, ItemStack staff, int remaining, CallbackInfo callback) {
        if (GuardChannel.isGuardStaff(staff)) callback.cancel();
    }
    @Inject(method = "m_5551_", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$releaseGuard(ItemStack staff, Level level, LivingEntity caster, int remaining, CallbackInfo callback) {
        if (!GuardChannel.isGuardStaff(staff)) return;
        if (caster instanceof ServerPlayer player) GuardChannel.finish(player);
        callback.cancel();
    }
    @Inject(method = "m_5922_", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$finishGuard(ItemStack staff, Level level, LivingEntity caster, CallbackInfoReturnable<ItemStack> callback) {
        if (!GuardChannel.isGuardStaff(staff)) return;
        if (caster instanceof ServerPlayer player) GuardChannel.finish(player);
        callback.setReturnValue(staff);
    }
}
