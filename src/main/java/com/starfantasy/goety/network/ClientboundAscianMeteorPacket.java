package com.starfantasy.goety.network;

import com.starfantasy.goety.combat.ascian.AscianMeteorPattern.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Full owner snapshot; newly tracking clients receive the original start times. */
public record ClientboundAscianMeteorPacket(ResourceLocation dimension,UUID owner,int ownerId,long revision,
                                           List<Path> meteors,List<Trap> traps) {
    public ClientboundAscianMeteorPacket { meteors=List.copyOf(meteors);traps=List.copyOf(traps); }
    public static void encode(ClientboundAscianMeteorPacket p,FriendlyByteBuf b) {
        b.m_130085_(p.dimension);b.writeLong(p.owner.getMostSignificantBits());b.writeLong(p.owner.getLeastSignificantBits());b.writeInt(p.ownerId);b.writeLong(p.revision);
        b.writeInt(p.meteors.size());b.writeInt(p.traps.size());
        for(Path path:p.meteors){b.writeLong(path.id());b.writeLong(path.startTick());for(Vec3 v:path.points())point(b,v);}
        for(Trap trap:p.traps){b.writeLong(trap.id());b.writeLong(trap.startTick());point(b,trap.center());}
    }
    private static void point(FriendlyByteBuf b,Vec3 p){b.writeDouble(p.f_82479_);b.writeDouble(p.f_82480_);b.writeDouble(p.f_82481_);}
    private static Vec3 point(FriendlyByteBuf b){double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Non-finite meteor position");return new Vec3(x,y,z);}
    public static ClientboundAscianMeteorPacket decode(FriendlyByteBuf b) {
        var dimension=b.m_130281_();UUID owner=new UUID(b.readLong(),b.readLong());int id=b.readInt();long revision=b.readLong();int mc=b.readInt(),ec=b.readInt();
        if(mc<0||mc>64||ec<0||ec>512)throw new IllegalArgumentException("Invalid meteor state size");
        List<Path> meteors=new ArrayList<>(mc);List<Trap> traps=new ArrayList<>(ec);
        for(int i=0;i<mc;i++){long key=b.readLong(),start=b.readLong();List<Vec3> points=new ArrayList<>(5);for(int j=0;j<5;j++)points.add(point(b));meteors.add(new Path(key,start,points));}
        for(int i=0;i<ec;i++)traps.add(new Trap(b.readLong(),b.readLong(),point(b)));
        return new ClientboundAscianMeteorPacket(dimension,owner,id,revision,meteors,traps);
    }
    public static void handle(ClientboundAscianMeteorPacket p,Supplier<NetworkEvent.Context> context){
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.starfantasy.goety.client.AscianMeteorEffects.receive(p));context.get().setPacketHandled(true);
    }
}
