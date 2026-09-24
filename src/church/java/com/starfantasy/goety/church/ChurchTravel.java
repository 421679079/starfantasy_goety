package com.starfantasy.goety.church;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ChurchTravel {
    private ChurchTravel() { }
    /** Loads only the entrance neighborhood; the rest streams in normally after arrival. */
    public static BlockPos prepare(ServerLevel nether) {
        var data=ChurchDestination.get(nether);
        BlockPos feet=data.arrival();
        if(feet==null || data.start()==null || !nether.getWorldBorder().isWithinBounds(feet)) return null;
        // Build the registered start before its outlying template pieces are requested.
        nether.getChunk(data.start().x,data.start().z,net.minecraft.world.level.chunk.ChunkStatus.STRUCTURE_STARTS);
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) nether.getChunk((feet.getX()>>4)+dx,(feet.getZ()>>4)+dz);
        return feet;
    }
    public static boolean clear(ServerLevel level, BlockPos feet, Entity player) {
        AABB body=new AABB(feet.getX()+0.5-player.getBbWidth()/2,feet.getY(),feet.getZ()+0.5-player.getBbWidth()/2,
                feet.getX()+0.5+player.getBbWidth()/2,feet.getY()+player.getBbHeight(),feet.getZ()+0.5+player.getBbWidth()/2);
        return feet.getY()>=level.getMinBuildHeight() && body.maxY<=level.getMaxBuildHeight()
                && level.noCollision(player,body) && !level.containsAnyLiquid(body);
    }
    /** Try the original feet position and five blocks above it; obstructions never disable entry. */
    public static BlockPos landing(ServerLevel level, BlockPos original, Entity player) {
        for(int rise=0;rise<=5;rise++) {
            BlockPos candidate=original.above(rise);
            if(clear(level,candidate,player)) return candidate;
        }
        return original;
    }
    public static boolean transfer(ServerPlayer player) {
        ItemStack eye=player.getMainHandItem().is(ChurchContent.UNDERWORLD_EYE.get())?player.getMainHandItem():player.getOffhandItem();
        if(!eye.is(ChurchContent.UNDERWORLD_EYE.get()) || player.isSpectator() || player.isPassenger() || player.isSleeping()) return false;
        ServerLevel nether=player.server.getLevel(Level.NETHER);
        if(nether==null) return false;
        BlockPos feet=prepare(nether);
        if(feet==null) return false;
        feet=landing(nether,feet,player);
        Vec3 destination=Vec3.atBottomCenterOf(feet);
        player.teleportTo(nether,destination.x,destination.y,destination.z,180,0);
        if(player.level()!=nether || player.position().distanceToSqr(destination)>4) return false;
        player.setDeltaMovement(Vec3.ZERO); player.fallDistance=0;
        eye.shrink(1); // Charged/open/cancelled portals never consume the item. Only actual arrival does.
        return true;
    }
}
