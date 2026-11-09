package com.starfantasy.goety.church;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

public final class UnderworldAltarBlock extends BaseEntityBlock {
    private static final ResourceLocation SUMMON_ITEM = new ResourceLocation(ChurchContent.MODID, "ascian_mask");
    public UnderworldAltarBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new UnderworldAltarEntity(pos, state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!SUMMON_ITEM.equals(ForgeRegistries.ITEMS.getKey(player.getItemInHand(hand).getItem()))) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof UnderworldAltarEntity altar) altar.use(player);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ChurchContent.ALTAR_ENTITY.get(), UnderworldAltarEntity::tick);
    }
}
