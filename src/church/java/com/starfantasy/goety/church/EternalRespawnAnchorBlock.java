package com.starfantasy.goety.church;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Vanilla anchor interaction and dimension rules, with a permanently full charge. */
public final class EternalRespawnAnchorBlock extends RespawnAnchorBlock {
    public EternalRespawnAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CHARGE, MAX_CHARGES));
    }

    @Override public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide() && state.getValue(CHARGE) != MAX_CHARGES)
            level.setBlock(pos, state.setValue(CHARGE, MAX_CHARGES), UPDATE_ALL);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        return super.use(state.setValue(CHARGE, MAX_CHARGES), level, pos, player, hand, hit);
    }
}
