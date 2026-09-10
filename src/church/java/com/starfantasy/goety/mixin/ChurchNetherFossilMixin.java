package com.starfantasy.goety.mixin;

import com.starfantasy.goety.church.ChurchFossilProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetherFossilPieces.NetherFossilPiece.class, remap = false)
public abstract class ChurchNetherFossilMixin {
    // Explicit production SRG selector, matching the church module's mixin convention.
    @Inject(method = "m_213694_", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$excludeChurchFossils(WorldGenLevel level, StructureManager manager,
            ChunkGenerator generator, RandomSource random, BoundingBox clip, ChunkPos chunk,
            BlockPos pivot, CallbackInfo callback) {
        BoundingBox fossil = ((StructurePiece)(Object)this).getBoundingBox();
        if (ChurchFossilProtection.intersectsChurch(level, manager, fossil)) callback.cancel();
    }
}
