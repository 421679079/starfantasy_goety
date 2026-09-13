package com.starfantasy.goety.network;

import com.starfantasy.goety.entity.HadesServantEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Input only: ownership, current vehicle, finite values and speed are checked by the server. */
public record ServerboundHadesRidePacket(int entityId, float forward, float strafe, float vertical, float yaw) {
    public static void encode(ServerboundHadesRidePacket packet, FriendlyByteBuf buffer) {
        buffer.m_130130_(packet.entityId());
        buffer.writeFloat(packet.forward()); buffer.writeFloat(packet.strafe());
        buffer.writeFloat(packet.vertical()); buffer.writeFloat(packet.yaw());
    }
    public static ServerboundHadesRidePacket decode(FriendlyByteBuf buffer) {
        return new ServerboundHadesRidePacket(buffer.m_130242_(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat());
    }
    public static void handle(ServerboundHadesRidePacket packet, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null && player.m_20202_() instanceof HadesServantEntity hades
                && hades.m_19879_() == packet.entityId()) {
            hades.acceptRiderInput(player, packet.forward(), packet.strafe(), packet.vertical(), packet.yaw());
        }
        supplier.get().setPacketHandled(true);
    }
}
