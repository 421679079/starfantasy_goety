package com.starfantasy.goety.network;

import com.starfantasy.goety.entity.HadesServantEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public record ServerboundHadesDismountPacket(int entityId) {
    public static void encode(ServerboundHadesDismountPacket packet, FriendlyByteBuf buffer) { buffer.m_130130_(packet.entityId()); }
    public static ServerboundHadesDismountPacket decode(FriendlyByteBuf buffer) { return new ServerboundHadesDismountPacket(buffer.m_130242_()); }
    public static void handle(ServerboundHadesDismountPacket packet, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null) dismount(player, packet.entityId());
        supplier.get().setPacketHandled(true);
    }
    public static void dismount(ServerPlayer player, int entityId) {
        if (player.m_20202_() instanceof HadesServantEntity hades && hades.m_19879_() == entityId
                && hades.m_6688_() == player) {
            hades.acceptRiderInput(player, 0, 0, 0, player.m_146908_());
            player.m_8127_();
        }
    }
}
