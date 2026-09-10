package com.starfantasy.goety.network;

import com.starfantasy.goety.client.ApollyonPageantDarkness;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Starts or clears the pageant's non-effect client darkness. */
public final class ClientboundApollyonDarknessPacket {
    private final int duration;

    public ClientboundApollyonDarknessPacket(int duration) {
        this.duration = Math.max(0, duration);
    }

    public static void encode(ClientboundApollyonDarknessPacket message, FriendlyByteBuf buffer) {
        buffer.m_130130_(message.duration);
    }

    public static ClientboundApollyonDarknessPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundApollyonDarknessPacket(buffer.m_130242_());
    }

    public static void handle(
            ClientboundApollyonDarknessPacket message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ApollyonPageantDarkness.setDuration(message.duration));
        contextSupplier.get().setPacketHandled(true);
    }
}
