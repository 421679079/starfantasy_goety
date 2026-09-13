package com.starfantasy.goety.mixin;

import com.starfantasy.goety.church.EternalRespawnAnchorBlock;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, remap = false)
public abstract class EternalAnchorRespawnMixin {
    @Inject(method = "m_36130_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;FZZ)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void starfantasy$respawnWithoutDepleting(ServerLevel level, BlockPos pos, float angle,
            boolean forced, boolean alive, CallbackInfoReturnable<Optional<Vec3>> callback) {
        if (level.getBlockState(pos).getBlock() instanceof EternalRespawnAnchorBlock
                && RespawnAnchorBlock.canSetSpawn(level)) {
            // Reuse vanilla obstruction/hazard checks; only omit the charge-decrement write.
            callback.setReturnValue(RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos));
        }
    }
}
