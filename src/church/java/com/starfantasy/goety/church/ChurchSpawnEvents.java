package com.starfantasy.goety.church;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Natural population is excluded; altar bosses, spells, commands and spawn eggs remain valid. */
@Mod.EventBusSubscriber(modid = ChurchContent.MODID)
public final class ChurchSpawnEvents {
    private static final ResourceLocation CHURCH = new ResourceLocation(ChurchContent.MODID, "church");
    private ChurchSpawnEvents() {}

    private static boolean blocked(ServerLevelAccessor access, BlockPos pos, MobSpawnType type) {
        if (type != MobSpawnType.NATURAL && type != MobSpawnType.CHUNK_GENERATION) return false;
        var level = access.getLevel();
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(CHURCH);
        if (structure == null) return false;
        if (access instanceof WorldGenRegion region) {
            // SPAWN regions contain only the current chunk. Following a structure reference
            // to its start can read outside that region and fail chunk generation. Suppress
            // initial population in intersecting chunks without loading their start chunks.
            var chunk = region.getChunk(pos.getX() >> 4, pos.getZ() >> 4,
                    ChunkStatus.STRUCTURE_REFERENCES, false);
            if (chunk == null) return false;
            var start = chunk.getStartForStructure(structure);
            return !chunk.getReferencesForStructure(structure).isEmpty()
                    || start != null && start.isValid();
        }
        return access instanceof ServerLevel && level.structureManager().getStructureAt(pos, structure).isValid();
    }
    @SubscribeEvent public static void position(MobSpawnEvent.PositionCheck event) {
        if (blocked(event.getLevel(), event.getEntity().blockPosition(), event.getSpawnType())) event.setResult(Event.Result.DENY);
    }
    @SubscribeEvent public static void finalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (blocked(event.getLevel(), BlockPos.containing(event.getX(), event.getY(), event.getZ()), event.getSpawnType()))
            event.setSpawnCancelled(true);
    }
}
