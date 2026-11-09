package com.starfantasy.goety.network;

import com.starfantasy.goety.client.FireTrapEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** One wave of visuals; an empty wave cancels this caster's active effects. */
public record ClientboundFireTrapWavePacket(ResourceLocation dimension, UUID owner, int ownerId,
                                          long startTick, long seed, List<Vec3> centers) {
    public ClientboundFireTrapWavePacket {
        centers = List.copyOf(centers);
        if (centers.size() > 64) throw new IllegalArgumentException("Too many fire traps");
        for (Vec3 p : centers) if (!Double.isFinite(p.f_82479_) || !Double.isFinite(p.f_82480_)
                || !Double.isFinite(p.f_82481_)) throw new IllegalArgumentException("Invalid fire trap position");
    }

    public static void encode(ClientboundFireTrapWavePacket p, FriendlyByteBuf b) {
        b.m_130085_(p.dimension);
        b.writeLong(p.owner.getMostSignificantBits()); b.writeLong(p.owner.getLeastSignificantBits());
        b.writeInt(p.ownerId); b.writeLong(p.startTick); b.writeLong(p.seed);
        b.writeByte(p.centers.size());
        for (Vec3 v : p.centers) { b.writeDouble(v.f_82479_); b.writeDouble(v.f_82480_); b.writeDouble(v.f_82481_); }
    }

    public static ClientboundFireTrapWavePacket decode(FriendlyByteBuf b) {
        ResourceLocation dimension = b.m_130281_();
        UUID owner = new UUID(b.readLong(), b.readLong());
        int id = b.readInt(); long tick = b.readLong(), seed = b.readLong();
        int count = b.readUnsignedByte();
        if (count > 64) throw new IllegalArgumentException("Too many fire traps");
        List<Vec3> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) points.add(new Vec3(b.readDouble(), b.readDouble(), b.readDouble()));
        return new ClientboundFireTrapWavePacket(dimension, owner, id, tick, seed, points);
    }

    public static void handle(ClientboundFireTrapWavePacket p, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FireTrapEffect.receive(p));
        context.get().setPacketHandled(true);
    }
}
