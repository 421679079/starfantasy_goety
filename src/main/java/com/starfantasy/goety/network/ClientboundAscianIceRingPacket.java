package com.starfantasy.goety.network;

import com.starfantasy.goety.combat.ascian.AscianIceRingPattern.Ring;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Ordered full snapshots: start times survive late tracking, clear invalidates stale packets. */
public record ClientboundAscianIceRingPacket(ResourceLocation dimension,UUID owner,int ownerId,long revision,List<Ring> rings,List<Ring> shattered){
    public ClientboundAscianIceRingPacket {rings=List.copyOf(rings);shattered=List.copyOf(shattered);}
    public ClientboundAscianIceRingPacket(ResourceLocation d,UUID owner,int ownerId,long revision,List<Ring> rings){this(d,owner,ownerId,revision,rings,List.of());}
    public static void encode(ClientboundAscianIceRingPacket p,FriendlyByteBuf b){
        b.m_130085_(p.dimension);uuid(b,p.owner);b.writeInt(p.ownerId);b.writeLong(p.revision);
        for(List<Ring> list:List.of(p.rings,p.shattered)){b.writeInt(list.size());for(Ring r:list){b.writeLong(r.id());uuid(b,r.target());b.writeInt(r.targetId());b.writeLong(r.startTick());b.writeInt(r.index());b.writeBoolean(r.hard());b.writeDouble(r.center().f_82479_);b.writeDouble(r.center().f_82480_);b.writeDouble(r.center().f_82481_);b.writeBoolean(r.frozen());}}
    }
    private static void uuid(FriendlyByteBuf b,UUID id){b.writeLong(id.getMostSignificantBits());b.writeLong(id.getLeastSignificantBits());}
    private static UUID uuid(FriendlyByteBuf b){return new UUID(b.readLong(),b.readLong());}
    public static ClientboundAscianIceRingPacket decode(FriendlyByteBuf b){
        var dimension=b.m_130281_();UUID owner=uuid(b);int ownerId=b.readInt();long revision=b.readLong();
        return new ClientboundAscianIceRingPacket(dimension,owner,ownerId,revision,rings(b),rings(b));
    }
    private static List<Ring> rings(FriendlyByteBuf b){
        int count=b.readInt();
        if(count<0||count>32)throw new IllegalArgumentException("Invalid ice ring count");
        List<Ring> rings=new ArrayList<>(count);
        for(int i=0;i<count;i++){
            long id=b.readLong();UUID target=uuid(b);int targetId=b.readInt();long start=b.readLong();int index=b.readInt();boolean hard=b.readBoolean();
            double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Non-finite ice ring center");
            rings.add(new Ring(id,target,targetId,start,index,hard,new Vec3(x,y,z),b.readBoolean()));
        }
        return rings;
    }
    public static void handle(ClientboundAscianIceRingPacket p,Supplier<NetworkEvent.Context> context){
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.starfantasy.goety.client.AscianIceRingEffects.receive(p));context.get().setPacketHandled(true);
    }
}
