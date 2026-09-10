package com.starfantasy.goety.network;

import com.starfantasy.goety.client.ApollyonPageantWhiteout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Starts the Infernal Judgment white-screen sequence on one client. */
public final class ClientboundApollyonWhiteoutPacket {
    public static void encode(
            ClientboundApollyonWhiteoutPacket message, FriendlyByteBuf buffer) {
    }

    public static ClientboundApollyonWhiteoutPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundApollyonWhiteoutPacket();
    }

    public static void handle(
            ClientboundApollyonWhiteoutPacket message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ApollyonPageantWhiteout.start());
        contextSupplier.get().setPacketHandled(true);
    }
}
