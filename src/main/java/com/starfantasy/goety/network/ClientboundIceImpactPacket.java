package com.starfantasy.goety.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record ClientboundIceImpactPacket(Vec3 position,float scale,long seed) {
    public static void encode(ClientboundIceImpactPacket p,FriendlyByteBuf b){b.writeDouble(p.position.f_82479_);b.writeDouble(p.position.f_82480_);b.writeDouble(p.position.f_82481_);b.writeFloat(p.scale);b.writeLong(p.seed);}
    public static ClientboundIceImpactPacket decode(FriendlyByteBuf b){return new ClientboundIceImpactPacket(new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat(),b.readLong());}
    public static void handle(ClientboundIceImpactPacket p,Supplier<NetworkEvent.Context> context){DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.starfantasy.goety.client.IceChunkEffect.impact(p.position,p.scale,p.seed));context.get().setPacketHandled(true);}
}
