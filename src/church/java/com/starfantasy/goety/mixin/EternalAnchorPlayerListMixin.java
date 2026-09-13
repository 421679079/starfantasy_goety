package com.starfantasy.goety.mixin;

import com.starfantasy.goety.church.EternalRespawnAnchorBlock;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerList.class, remap = false)
public abstract class EternalAnchorPlayerListMixin {
    @Redirect(method = "m_11236_(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;m_60713_(Lnet/minecraft/world/level/block/Block;)Z"),
            remap = false)
    private boolean starfantasy$recognizeEternalAnchor(BlockState state, Block expected) {
        // Preserve vanilla respawn facing and sound for this anchor too.
        return state.is(expected) || expected == Blocks.RESPAWN_ANCHOR
                && state.getBlock() instanceof EternalRespawnAnchorBlock;
    }
}
