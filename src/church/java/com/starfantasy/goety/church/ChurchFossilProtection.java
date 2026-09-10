package com.starfantasy.goety.church;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Fossils are structures, so the biome-feature exclusion does not cover them. */
@Mod.EventBusSubscriber(modid = ChurchContent.MODID)
public final class ChurchFossilProtection {
    private static final ResourceLocation CHURCH = new ResourceLocation(ChurchContent.MODID, "church");

    private ChurchFossilProtection() {}

    public static boolean intersectsChurch(WorldGenLevel level, StructureManager manager, BoundingBox fossil) {
        Structure church = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(CHURCH);
        if (church == null) return false;
        // Use every chunk touched by the fossil, not just the chunk currently decorating.
        // NetherFossilPiece expands its placement box and can write across that border.
        for (int x = fossil.minX() >> 4; x <= fossil.maxX() >> 4; x++) {
            for (int z = fossil.minZ() >> 4; z <= fossil.maxZ() >> 4; z++) {
                if (!level.hasChunk(x, z)) continue;
                for (var start : manager.startsForStructure(new ChunkPos(x, z), s -> s == church)) {
                    if (start.isValid() && start.getBoundingBox().intersects(fossil)) return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void chunkLoaded(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) return;
        // Chunk load can be dispatched off-thread. Defer edits to the server thread,
        // and never reload a chunk that has since been unloaded or replaced.
        level.getServer().tell(new TickTask(level.getServer().getTickCount(), () -> {
            ChunkPos pos = chunk.getPos();
            if (level.getChunkSource().getChunkNow(pos.x, pos.z) == chunk) removeResidualBones(level, chunk);
        }));
    }

    public static void removeResidualBones(ServerLevel level, LevelChunk chunk) {
        // The 99 church templates contain no bone blocks. Only this block type is
        // repaired; floors, altar, decorations and temporary boss blocks stay intact.
        boolean hasBones = false;
        for (var section : chunk.getSections()) {
            if (section.maybeHas(state -> state.is(Blocks.BONE_BLOCK))) { hasBones = true; break; }
        }
        if (!hasBones) return;
        Structure church = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(CHURCH);
        if (church == null) return;
        List<BoundingBox> boxes = level.structureManager().startsForStructure(chunk.getPos(), s -> s == church)
                .stream().filter(start -> start.isValid()).map(start -> start.getBoundingBox()).toList();
        if (boxes.isEmpty()) return;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int baseX = chunk.getPos().getMinBlockX(), baseZ = chunk.getPos().getMinBlockZ();
        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            var section = chunk.getSection(sectionIndex);
            if (!section.maybeHas(state -> state.is(Blocks.BONE_BLOCK))) continue;
            int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                if (!section.getBlockState(x, y, z).is(Blocks.BONE_BLOCK)) continue;
                pos.set(baseX + x, baseY + y, baseZ + z);
                for (BoundingBox box : boxes) {
                    if (box.isInside(pos)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        break;
                    }
                }
            }
        }
    }
}
