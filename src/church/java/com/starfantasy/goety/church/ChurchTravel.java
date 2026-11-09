package com.starfantasy.goety.church;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ChurchTravel {
    public enum Validation { VALID, MISSING, UNSAFE }
    private ChurchTravel() {}

    /** Structure metadata alone is insufficient: validate the altar, main floor and entrance floor. */
    public static Validation validate(ServerLevel level, ChurchDestination.Destination target, Entity traveler) {
        if (target == null || !ChurchDestination.withinBorder(level, target.home())) return Validation.UNSAFE;
        var church = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ChurchDestination.ID);
        if (church == null) return Validation.MISSING;
        var chunk = level.getChunk(target.start().x, target.start().z, ChunkStatus.STRUCTURE_STARTS);
        var start = chunk.getStartForStructure(church);
        if (start == null || !start.isValid()) return Validation.MISSING;
        loadNeighborhood(level, target.home());
        loadNeighborhood(level, target.arrival());
        if (!buildingPresent(level, target)) return Validation.MISSING;
        return landing(level, target.arrival(), traveler) == null ? Validation.UNSAFE : Validation.VALID;
    }
    private static void loadNeighborhood(ServerLevel level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            level.getChunk((center.getX() >> 4) + dx, (center.getZ() >> 4) + dz);
        }
    }
    static boolean buildingPresent(ServerLevel level, ChurchDestination.Destination target) {
        BlockPos altar = target.home(), entrance = target.arrival().below();
        if (!level.getBlockState(altar).is(ChurchContent.ALTAR.get())) return false;
        int mainFloor = 0, entranceFloor = 0;
        // Actual template samples; tolerate two changed blocks at each group of four.
        for (int[] offset : new int[][]{{-4, 0}, {4, 0}, {0, -4}, {0, 4}}) {
            if (level.getBlockState(altar.offset(offset[0], 0, offset[1])).is(Blocks.CALCITE)) mainFloor++;
            if (level.getBlockState(entrance.offset(offset[0], 0, offset[1])).is(Blocks.POLISHED_DEEPSLATE)) entranceFloor++;
        }
        return mainFloor >= 2 && entranceFloor >= 2;
    }
    public static boolean clear(ServerLevel level, BlockPos feet, Entity player) {
        AABB body = new AABB(feet.getX() + .5 - player.getBbWidth() / 2, feet.getY(), feet.getZ() + .5 - player.getBbWidth() / 2,
                feet.getX() + .5 + player.getBbWidth() / 2, feet.getY() + player.getBbHeight(), feet.getZ() + .5 + player.getBbWidth() / 2);
        BlockPos floor = feet.below();
        return feet.getY() > level.getMinBuildHeight() && body.maxY <= level.getMaxBuildHeight()
                && level.getWorldBorder().isWithinBounds(body)
                && level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getFluidState(floor).isEmpty()
                && !level.getBlockState(floor).is(Blocks.MAGMA_BLOCK)
                && !level.getBlockState(floor).is(Blocks.CAMPFIRE)
                && !level.getBlockState(floor).is(Blocks.SOUL_CAMPFIRE)
                && level.noCollision(player, body) && !level.containsAnyLiquid(body)
                && BlockPos.betweenClosedStream(body).noneMatch(p -> level.getBlockState(p).is(Blocks.FIRE)
                        || level.getBlockState(p).is(Blocks.SOUL_FIRE));
    }
    public static BlockPos landing(ServerLevel level, BlockPos original, Entity player) {
        for (int rise = 0; rise <= 5; rise++) {
            for (int radius = 0; radius <= 2; radius++) {
                for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                    BlockPos candidate = original.offset(x, rise, z);
                    if (clear(level, candidate, player)) return candidate;
                }
            }
        }
        return null;
    }
    /** Use the destination bound to this rift, never a mutable world-wide selection. */
    public static boolean transfer(ServerPlayer player, ChurchDestination.Destination target) {
        ItemStack eye = player.getMainHandItem().is(ChurchContent.UNDERWORLD_EYE.get()) ? player.getMainHandItem() : player.getOffhandItem();
        if (!eye.is(ChurchContent.UNDERWORLD_EYE.get()) || player.isSpectator() || player.isPassenger() || player.isSleeping()) return false;
        ServerLevel nether = player.server.getLevel(Level.NETHER);
        if (nether == null || target == null) return false;
        try {
            Validation status = validate(nether, target, player);
            if (status != Validation.VALID) {
                ChurchDestination.get(nether).invalidate(target, status == Validation.MISSING);
                return false;
            }
            BlockPos feet = landing(nether, target.arrival(), player);
            if (feet == null) return false;
            Vec3 destination = Vec3.atBottomCenterOf(feet);
            player.teleportTo(nether, destination.x, destination.y, destination.z, 180, 0);
            if (player.level() != nether || player.position().distanceToSqr(destination) > 4) return false;
            player.setDeltaMovement(Vec3.ZERO); player.fallDistance = 0;
            eye.shrink(1);
            return true;
        } catch (RuntimeException exception) {
            com.mojang.logging.LogUtils.getLogger().error("Could not prepare church arrival; the eye was not consumed", exception);
            return false;
        }
    }
}
