package com.starfantasy.goety.network;

import com.starfantasy.goety.client.ApollyonLightningStrikeEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** One packet describes one complete Goety-style lightning strike. */
public final class ClientboundApollyonLightningStrikePacket {
    private final double x;
    private final double y;
    private final double z;
    private final long seed;
    private final float effectSize;

    public ClientboundApollyonLightningStrikePacket(Vec3 center, long seed, float effectSize) {
        this(center.f_82479_, center.f_82480_, center.f_82481_, seed, effectSize);
    }

    private ClientboundApollyonLightningStrikePacket(
            double x, double y, double z, long seed, float effectSize) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.seed = seed;
        this.effectSize = effectSize;
    }

    public static void encode(ClientboundApollyonLightningStrikePacket message,
                              FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeLong(message.seed);
        buffer.writeFloat(message.effectSize);
    }

    public static ClientboundApollyonLightningStrikePacket decode(FriendlyByteBuf buffer) {
        return new ClientboundApollyonLightningStrikePacket(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readLong(),
                buffer.readFloat());
    }

    public static void handle(ClientboundApollyonLightningStrikePacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ApollyonLightningStrikeEffect.spawn(
                        new Vec3(message.x, message.y, message.z), message.seed,
                        message.effectSize));
        contextSupplier.get().setPacketHandled(true);
    }
}
