package com.starfantasy.goety.network;

import com.starfantasy.goety.client.ApollyonClientSnap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Clears client lerp state when the scripted pageant teleports Apollyon. */
public final class ClientboundApollyonSnapPacket {
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public ClientboundApollyonSnapPacket(
            int entityId, double x, double y, double z, float yaw, float pitch) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(ClientboundApollyonSnapPacket message, FriendlyByteBuf buffer) {
        buffer.m_130130_(message.entityId);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.pitch);
    }

    public static ClientboundApollyonSnapPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundApollyonSnapPacket(
                buffer.m_130242_(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(
            ClientboundApollyonSnapPacket message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ApollyonClientSnap.apply(
                        message.entityId, message.x, message.y, message.z,
                        message.yaw, message.pitch));
        contextSupplier.get().setPacketHandled(true);
    }
}
