package com.starfantasy.goety.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.*;
import java.util.function.Supplier;

/** One message per visual wave, with server time, caster identity and no per-particle traffic. */
public record ClientboundAscianEffectPacket(ResourceLocation dimension,UUID owner,int ownerId,long tick,int kind,List<Vec3> centers,int meleePhase,int styleOffset) {
    public static final int TELEPORT_WINDUP=9, TELEPORT_ARRIVAL=10;
    public static final int TRANSITION_HANDS=11;
    public ClientboundAscianEffectPacket(ResourceLocation dimension,UUID owner,int ownerId,long tick,int kind,List<Vec3> centers) {
        this(dimension,owner,ownerId,tick,kind,centers,0,0);
    }
    public ClientboundAscianEffectPacket {
        centers=List.copyOf(centers);
        if(kind<0||kind>TRANSITION_HANDS||centers.size()>64)throw new IllegalArgumentException("Invalid Ascian wave");
        if(kind==8?(meleePhase<1||meleePhase>6||styleOffset<0||styleOffset>4||centers.isEmpty()):(meleePhase!=0||styleOffset!=0))
            throw new IllegalArgumentException("Invalid Ascian melee wave");
        for(Vec3 p:centers)if(!Double.isFinite(p.f_82479_)||!Double.isFinite(p.f_82480_)||!Double.isFinite(p.f_82481_))throw new IllegalArgumentException("Invalid position");
    }
    public static void encode(ClientboundAscianEffectPacket p,FriendlyByteBuf b){
        b.m_130085_(p.dimension);b.writeLong(p.owner.getMostSignificantBits());b.writeLong(p.owner.getLeastSignificantBits());
        b.writeInt(p.ownerId);b.writeLong(p.tick);b.writeByte(p.kind);b.writeByte(p.centers.size());
        b.writeByte(p.meleePhase);b.writeByte(p.styleOffset);
        for(Vec3 v:p.centers){b.writeDouble(v.f_82479_);b.writeDouble(v.f_82480_);b.writeDouble(v.f_82481_);}
    }
    public static ClientboundAscianEffectPacket decode(FriendlyByteBuf b){
        var dim=b.m_130281_();var owner=new UUID(b.readLong(),b.readLong());int id=b.readInt();long tick=b.readLong();int kind=b.readUnsignedByte(),count=b.readUnsignedByte();
        if(count>64)throw new IllegalArgumentException("Too many points");List<Vec3> points=new ArrayList<>(count);
        int phase=b.readUnsignedByte(),offset=b.readUnsignedByte();
        for(int i=0;i<count;i++)points.add(new Vec3(b.readDouble(),b.readDouble(),b.readDouble()));
        return new ClientboundAscianEffectPacket(dim,owner,id,tick,kind,points,phase,offset);
    }
    public static void handle(ClientboundAscianEffectPacket p,Supplier<NetworkEvent.Context> c){
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.starfantasy.goety.client.AscianSpellEffects.receive(p));c.get().setPacketHandled(true);
    }
}
