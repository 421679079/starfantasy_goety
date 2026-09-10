package com.starfantasy.goety.network;

import com.starfantasy.goety.client.ChurchFogEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Enables or clears the client-only vanilla fog override for the church. */
public final class ClientboundChurchFogPacket {
    private final boolean inside;

    public ClientboundChurchFogPacket(boolean inside) {
        this.inside = inside;
    }

    public static void encode(ClientboundChurchFogPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.inside);
    }

    public static ClientboundChurchFogPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundChurchFogPacket(buffer.readBoolean());
    }

    public static void handle(
            ClientboundChurchFogPacket message,
            Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ChurchFogEvents.setInsideChurch(message.inside));
        contextSupplier.get().setPacketHandled(true);
    }
}
