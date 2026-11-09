package com.starfantasy.goety.church;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class AscianAltarBlock extends BaseEntityBlock {
    public AscianAltarBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AscianAltarEntity(pos,state); }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!player.getItemInHand(hand).is(UnderworldAltarEntity.HALOS))return InteractionResult.PASS;
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof AscianAltarEntity altar)altar.use(player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack,BlockGetter level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.starfantasy_goety.ascian_altar").withStyle(ChatFormatting.GRAY));
    }
}
