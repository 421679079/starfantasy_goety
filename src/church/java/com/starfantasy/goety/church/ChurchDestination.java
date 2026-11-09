package com.starfantasy.goety.church;

import com.mojang.logging.LogUtils;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.saveddata.SavedData;

/** A verified travel destination, not a world-generation whitelist. Never scans region files. */
public final class ChurchDestination extends SavedData {
    public static final ResourceLocation ID = new ResourceLocation(ChurchContent.MODID, "church");
    private static final String FILE = "starfantasy_church_destination";
    private static final int MAX_RING = 32, MAX_PROBES = 512, MAX_STARTS = 64, MAX_BUILDINGS = 3;
    private BlockPos home;
    private ChunkPos start;
    private String failure = "";
    private int regionX, regionZ, cursor;
    private boolean hasSearchOrigin;
    private final Set<Long> rejected = new LinkedHashSet<>();
    private long retryAfter;

    public record Destination(ChunkPos start, BlockPos home) {
        public BlockPos arrival() { return home.offset(0, 1, 94); }
    }
    public static ChurchDestination get(ServerLevel nether) {
        return nether.getDataStorage().computeIfAbsent(ChurchDestination::load, ChurchDestination::new, FILE);
    }
    public BlockPos home() { return home; }
    public ChunkPos start() { return start; }
    public String failure() { return failure; }
    public Destination cached() { return home == null || start == null ? null : new Destination(start, home); }

    /** Synchronous and bounded. Later uses resume instead of repeating failed candidates. */
    public Destination locate(ServerLevel level, BlockPos origin, Entity traveler) {
        if (level.getGameTime() < retryAfter) return null;
        try {
            Destination old = cached();
            if (old != null) {
                var status = ChurchTravel.validate(level, old, traveler);
                if (status == ChurchTravel.Validation.VALID) { failure = ""; return old; }
                invalidate(old, status == ChurchTravel.Validation.MISSING);
            }
            var church = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ID);
            var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(ID);
            if (!(church instanceof ChurchStructure) || set == null
                    || !(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
                return failed(level, "structure_missing");
            }
            int rx = Math.floorDiv(origin.getX() >> 4, placement.spacing());
            int rz = Math.floorDiv(origin.getZ() >> 4, placement.spacing());
            if (!hasSearchOrigin || rx != regionX || rz != regionZ) {
                regionX = rx; regionZ = rz; cursor = 0; hasSearchOrigin = true;
            }
            List<ChunkPos> candidates = new ArrayList<>();
            for (int ring = 0; ring <= MAX_RING; ring++) {
                for (int x = -ring; x <= ring; x++) for (int z = -ring; z <= ring; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == ring) candidates.add(placement.getPotentialStructureChunk(
                            level.getSeed(), (rx + x) * placement.spacing(), (rz + z) * placement.spacing()));
                }
            }
            int probes = 0, starts = 0, buildings = 0;
            long deadline = System.nanoTime() + 20_000_000_000L;
            while (cursor < candidates.size() && probes < MAX_PROBES && starts < MAX_STARTS
                    && buildings < MAX_BUILDINGS && System.nanoTime() < deadline) {
                ChunkPos candidate = candidates.get(cursor);
                if (rejected.contains(candidate.toLong()) || old != null && old.start().equals(candidate)) {
                    cursor++; continue;
                }
                BlockPos center = new BlockPos(candidate.getMiddleBlockX(), 31, candidate.getMiddleBlockZ());
                if (!withinBorder(level, center) || !placement.isStructureChunk(
                        level.getChunkSource().getGeneratorState(), candidate.x, candidate.z)) { cursor++; continue; }
                probes++;
                // Vanilla locate's fast path consults saved metadata and probes new candidates.
                var presence = level.structureManager().checkStructurePresence(candidate, church, false);
                if (presence == StructureCheckResult.START_NOT_PRESENT) { cursor++; continue; }
                if (presence != StructureCheckResult.START_PRESENT
                        && !level.getServer().getWorldData().worldGenOptions().generateStructures()) {
                    cursor++; continue;
                }
                starts++;
                var chunk = level.getChunk(candidate.x, candidate.z, ChunkStatus.STRUCTURE_STARTS);
                var structureStart = chunk.getStartForStructure(church);
                if (structureStart == null || !structureStart.isValid() || structureStart.getPieces().isEmpty()
                        || !(structureStart.getPieces().get(0) instanceof PoolElementStructurePiece first)) {
                    cursor++; continue;
                }
                Destination found = new Destination(candidate, first.getPosition().offset(70, 8, 70));
                buildings++;
                var status = ChurchTravel.validate(level, found, traveler);
                cursor++; // Exceptions do not advance past a temporarily unreadable candidate.
                if (status == ChurchTravel.Validation.VALID) {
                    home = found.home(); start = found.start(); failure = ""; setDirty();
                    LogUtils.getLogger().info("Verified Underworld Church at {} after {} candidates", home, probes);
                    return found;
                }
                if (status == ChurchTravel.Validation.MISSING) reject(candidate);
                LogUtils.getLogger().warn("Skipping Underworld Church at {}: {}", found.home(), status);
            }
            if (cursor >= candidates.size()) cursor = 0;
            return failed(level, buildings >= MAX_BUILDINGS ? "invalid_buildings" : "not_found");
        } catch (RuntimeException exception) {
            // I/O and generation errors are not proof that a church is absent.
            LogUtils.getLogger().error("Underworld Church lookup failed; the eye was not consumed", exception);
            return failed(level, "storage_error");
        }
    }
    public static boolean withinBorder(ServerLevel level, BlockPos center) {
        return level.getWorldBorder().isWithinBounds(center.offset(-70, 0, -70))
                && level.getWorldBorder().isWithinBounds(center.offset(73, 0, 105));
    }
    public void invalidate(Destination destination, boolean missing) {
        if (missing) reject(destination.start());
        if (destination.equals(cached())) { home = null; start = null; }
        setDirty();
    }
    private void reject(ChunkPos chunk) {
        rejected.add(chunk.toLong());
        if (rejected.size() > 4096) rejected.remove(rejected.iterator().next());
        setDirty();
    }
    private Destination failed(ServerLevel level, String reason) {
        failure = reason; retryAfter = level.getGameTime() + 100; setDirty(); return null;
    }
    private static ChurchDestination load(CompoundTag tag) {
        ChurchDestination data = new ChurchDestination();
        // Legacy coordinates are revalidated; old Searched/Failure never disable worldgen or retries.
        if (tag.contains("Home") && tag.contains("Start")) {
            data.home = BlockPos.of(tag.getLong("Home")); data.start = new ChunkPos(tag.getLong("Start"));
        }
        data.hasSearchOrigin = tag.getBoolean("HasSearchOrigin");
        data.regionX = tag.getInt("RegionX"); data.regionZ = tag.getInt("RegionZ");
        data.cursor = Math.max(0, Math.min((MAX_RING * 2 + 1) * (MAX_RING * 2 + 1), tag.getInt("Cursor")));
        for (long chunk : tag.getLongArray("Rejected")) { if (data.rejected.size() < 4096) data.rejected.add(chunk); }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        tag.putInt("Version", 2); tag.putBoolean("HasSearchOrigin", hasSearchOrigin);
        tag.putInt("RegionX", regionX); tag.putInt("RegionZ", regionZ); tag.putInt("Cursor", cursor);
        tag.putLongArray("Rejected", rejected.stream().mapToLong(Long::longValue).toArray());
        if (home != null && start != null) { tag.putLong("Home", home.asLong()); tag.putLong("Start", start.toLong()); }
        return tag;
    }
}
