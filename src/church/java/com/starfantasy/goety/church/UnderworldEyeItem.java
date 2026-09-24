package com.starfantasy.goety.church;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class UnderworldEyeItem extends Item {
    public UnderworldEyeItem(Properties properties) { super(properties); }
    public static boolean canOpen(Player player) { return player.isAlive() && !player.isSpectator(); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if (!canOpen(player)) return InteractionResultHolder.fail(stack);
        if(level instanceof ServerLevel server && player instanceof ServerPlayer owner) {
            ServerLevel nether=server.getServer().getLevel(Level.NETHER);
            if(nether==null || ChurchDestination.get(nether).home()==null) {
                owner.displayClientMessage(Component.translatable("message.starfantasy_goety.underworld_eye.not_found"),true);
                return InteractionResultHolder.fail(stack);
            }
            if(!ChurchRiftEntity.forOwner(server,owner).isEmpty() || !ChurchRiftEntity.begin(server,owner,hand))
                return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if(level instanceof ServerLevel server && user instanceof ServerPlayer player && canOpen(player))
            ChurchRiftEntity.forOwner(server,player).forEach(rift -> rift.finishCharging(player));
        return stack;
    }
    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
        if(level instanceof ServerLevel server && user instanceof ServerPlayer player)
            ChurchRiftEntity.forOwner(server,player).forEach(ChurchRiftEntity::cancelCharging);
    }
    @Override public int getUseDuration(ItemStack stack) { return ChurchRiftShape.CHARGE_TICKS; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.starfantasy_goety.underworld_eye.use").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.starfantasy_goety.underworld_eye.enter",Component.translatable(getDescriptionId()),Component.translatable(getDescriptionId())).withStyle(ChatFormatting.GRAY));
    }
}
