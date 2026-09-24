package com.starfantasy.goety.church;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.nbt.visitors.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One persisted destination per save. Worldgen workers only read an immutable selection. */
@Mod.EventBusSubscriber(modid = ChurchContent.MODID)
public final class ChurchDestination extends SavedData {
    public static final ResourceLocation ID = new ResourceLocation(ChurchContent.MODID, "church");
    private static final String FILE = "starfantasy_church_destination";
    private static final Map<RandomState, Optional<ChunkPos>> SELECTIONS = Collections.synchronizedMap(new WeakHashMap<>());
    private boolean searched;
    private BlockPos home;
    private ChunkPos start;
    private String failure = "";

    public static ChurchDestination get(ServerLevel nether) {
        return nether.getDataStorage().computeIfAbsent(ChurchDestination::load, ChurchDestination::new, FILE);
    }
    public BlockPos home() { return home; }
    public ChunkPos start() { return start; }
    public boolean searched() { return searched; }
    public String failure() { return failure; }
    // The outermost entrance inset is template (70,8,164), altar is (70,8,70).
    public BlockPos arrival() { return home == null ? null : home.offset(0, 1, 94); }
    public static boolean allows(RandomState state, ChunkPos chunk) {
        return SELECTIONS.getOrDefault(state, Optional.empty()).filter(chunk::equals).isPresent();
    }
    public void publish(ServerLevel level) {
        SELECTIONS.put(level.getChunkSource().randomState(), Optional.ofNullable(start));
    }
    @SubscribeEvent public static void loadWorld(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.NETHER)) get(level).initialize(level);
    }
    @SubscribeEvent public static void unloadWorld(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) SELECTIONS.remove(level.getChunkSource().randomState());
    }
    public void initialize(ServerLevel level) {
        if (searched) { publish(level); return; }
        long began = System.nanoTime();
        LogUtils.getLogger().info("Locating the unique Underworld Church for this save...");
        try {
            if (!adoptExisting(level)) selectNew(level);
            searched = true;
            setDirty();
            publish(level);
            // Persist before any destination chunks can generate, including on first startup.
            level.getDataStorage().save();
            LogUtils.getLogger().info("Underworld Church selection: {} ({} ms)", home == null ? failure : home,
                    (System.nanoTime() - began) / 1_000_000);
        } catch (IOException e) {
            // An unreadable old region must never cause an extra church to be selected.
            failure = "storage_error";
            publish(level);
            LogUtils.getLogger().error("Could not inspect existing churches; no new church will generate", e);
        }
    }
    private void selectNew(ServerLevel level) {
        if (!level.getServer().getWorldData().worldGenOptions().generateStructures()) { failure="structures_disabled"; return; }
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        if (!(registry.get(ID) instanceof ChurchStructure church)) { failure="structure_missing"; return; }
        var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(ID);
        if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) { failure="placement_missing"; return; }
        var generator = level.getChunkSource().getGenerator();
        var state = level.getChunkSource().randomState();
        for (int ring=0; ring<=32; ring++) for (int rx=-ring; rx<=ring; rx++) for (int rz=-ring; rz<=ring; rz++) {
            if (Math.max(Math.abs(rx),Math.abs(rz)) != ring) continue;
            ChunkPos candidate = placement.getPotentialStructureChunk(level.getSeed(), rx*placement.spacing(), rz*placement.spacing());
            if (!placement.isStructureChunk(level.getChunkSource().getGeneratorState(),candidate.x,candidate.z)) continue;
            BlockPos center = new BlockPos(candidate.getMiddleBlockX(), generator.getSeaLevel()-1, candidate.getMiddleBlockZ());
            if (!level.getWorldBorder().isWithinBounds(center.offset(-70,0,-70))
                    || !level.getWorldBorder().isWithinBounds(center.offset(74,0,106))) continue;
            var context = new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),state,
                    level.getStructureManager(),level.getSeed(),candidate,level,church.biomes()::contains);
            if (church.candidate(context).isEmpty()) continue;
            // Never retrofit a start into a chunk whose structure pass has already run.
            if (!unexploredFootprint(level,center)) continue;
            home=center; start=candidate; return;
        }
        failure="not_found";
    }
    private static boolean unexploredFootprint(ServerLevel level, BlockPos center) {
        for(int x=(center.getX()-70)>>4;x<=(center.getX()+73)>>4;x++)
            for(int z=(center.getZ()-70)>>4;z<=(center.getZ()+105)>>4;z++)
                if(level.getChunkSource().chunkMap.read(new ChunkPos(x,z)).join().isPresent()) return false;
        return true;
    }
    private boolean adoptExisting(ServerLevel level) throws IOException {
        Path directory = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("DIM-1/region");
        if (!Files.isDirectory(directory)) return false;
        try (var files = Files.list(directory)) {
            for (Path file : files.filter(p -> p.getFileName().toString().matches("r\\.-?\\d+\\.-?\\d+\\.mca")).sorted().toList()) {
                String[] parts = file.getFileName().toString().split("\\.");
                int regionX = Integer.parseInt(parts[1]), regionZ = Integer.parseInt(parts[2]);
                try (RegionFile region = new RegionFile(file,directory,false)) {
                    for (int x=0;x<32;x++) for (int z=0;z<32;z++) {
                        ChunkPos chunk = new ChunkPos(regionX*32+x,regionZ*32+z);
                        if (!region.hasChunk(chunk)) continue;
                        try (var input = region.getChunkDataInputStream(chunk)) {
                            if (input == null) continue;
                            CollectFields fields = new CollectFields(new FieldSelector("structures", CompoundTag.TYPE, "starts"));
                            NbtIo.parse(input,fields);
                            if (!(fields.getResult() instanceof CompoundTag root)) continue;
                            CompoundTag saved = root.getCompound("structures").getCompound("starts").getCompound(ID.toString());
                            if (!saved.getString("id").equals(ID.toString())) continue;
                            // Template piece 0 origin reconstructs the actual altar even in migrated saves.
                            ListTag pieces = saved.getList("Children",Tag.TAG_COMPOUND);
                            if (pieces.isEmpty()) continue;
                            CompoundTag piece = pieces.getCompound(0);
                            BlockPos found = new BlockPos(piece.getInt("PosX")+70,piece.getInt("PosY")+8,piece.getInt("PosZ")+70);
                            if (home == null || found.distSqr(BlockPos.ZERO) < home.distSqr(BlockPos.ZERO)) {
                                home=found; start=new ChunkPos(saved.getInt("ChunkX"),saved.getInt("ChunkZ"));
                            }
                        }
                    }
                }
            }
        }
        return home != null;
    }
    private static ChurchDestination load(CompoundTag tag) {
        ChurchDestination data = new ChurchDestination();
        data.searched=tag.getBoolean("Searched"); data.failure=tag.getString("Failure");
        if(tag.contains("Home") && tag.contains("Start")) { data.home=BlockPos.of(tag.getLong("Home")); data.start=new ChunkPos(tag.getLong("Start")); }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Searched",searched); tag.putString("Failure",failure);
        if(home!=null && start!=null) { tag.putLong("Home",home.asLong()); tag.putLong("Start",start.toLong()); }
        return tag;
    }
}
