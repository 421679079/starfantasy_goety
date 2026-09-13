package com.starfantasy.goety.network;

import com.starfantasy.goety.entity.HadesServantEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundHadesAttackPacket(int entityId, float yaw) {
    public static void encode(ServerboundHadesAttackPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130130_(packet.entityId());
        buffer.writeFloat(packet.yaw());
    }
    public static ServerboundHadesAttackPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundHadesAttackPacket(buffer.m_130242_(), buffer.readFloat());
    }
    public static void handle(ServerboundHadesAttackPacket packet, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null) attack(player, packet.entityId(), packet.yaw());
        supplier.get().setPacketHandled(true);
    }
    public static boolean attack(ServerPlayer player, int entityId, float yaw) {
        return player.m_20202_() instanceof HadesServantEntity hades && hades.m_19879_() == entityId
                && hades.tryRiderAttack(player, yaw);
    }
}
