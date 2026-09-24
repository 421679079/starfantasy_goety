package com.starfantasy.goety.church;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.*;

public final class ChurchStructure extends Structure {
    public static final Codec<ChurchStructure> CODEC = simpleCodec(ChurchStructure::new);
    public ChurchStructure(StructureSettings settings) { super(settings); }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!ChurchDestination.allows(context.randomState(), context.chunkPos())) return Optional.empty();
        return candidate(context);
    }
    /** Read-only terrain probe. Selecting a destination never creates structure starts. */
    public Optional<GenerationStub> candidate(GenerationContext context) {
        int homeX = context.chunkPos().getMiddleBlockX(), homeZ = context.chunkPos().getMiddleBlockZ();
        // The bridge top meets the lava sea surface; exterior template air below it is lava.
        int seaY = context.chunkGenerator().getSeaLevel();
        int bridgeY = seaY - 1;
        var biome = context.biomeSource().getNoiseBiome(homeX >> 2, bridgeY >> 2, homeZ >> 2, context.randomState().sampler());
        if (!context.validBiome().test(biome)) return Optional.empty();
        int lava = 0, air = 0;
        for (int dx : new int[]{-48, 0, 48}) for (int dz : new int[]{-32, 16, 64}) {
            var column = context.chunkGenerator().getBaseColumn(homeX + dx, homeZ + dz, context.heightAccessor(), context.randomState());
            if (column.getBlock(seaY - 1).is(Blocks.LAVA)) lava++;
            for (int offset : new int[]{0, 20, 40, 60}) if (column.getBlock(seaY + offset).isAir()) air++;
        }
        if (lava < 3 || air < 18) return Optional.empty();
        // Altar offset in the exported template is (70,8,70); no random rotation.
        BlockPos origin = new BlockPos(homeX - 70, bridgeY - 8, homeZ - 70);
        return Optional.of(new GenerationStub(new BlockPos(homeX, bridgeY, homeZ), builder -> {
            // Chunk-sized templates avoid scanning the entire cathedral for every chunk.
            for (int x = 0; x < 9; x++) for (int z = 0; z < 11; z++) {
                String template = ChurchContent.MODID + ":church/part_" + x + "_" + z;
                StructurePoolElement element = StructurePoolElement.single(template).apply(StructureTemplatePool.Projection.RIGID);
                BlockPos at = origin.offset(x * 16, 0, z * 16);
                BoundingBox box = element.getBoundingBox(context.structureTemplateManager(), at, Rotation.NONE);
                builder.addPiece(new PoolElementStructurePiece(context.structureTemplateManager(), element, at,
                        element.getGroundLevelDelta(), Rotation.NONE, box));
            }
        }));
    }
    @Override public StructureType<?> type() { return ChurchContent.CHURCH.get(); }
}
