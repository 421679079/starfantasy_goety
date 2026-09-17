package com.starfantasy.goety.network;

import com.starfantasy.goety.magic.FinalStaffSchool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Only changes a held staff's school; inventory and casting stay server-authoritative. */
public record ServerboundStaffSchoolPacket(int slot, String school, String previousSchool) {
    public static void encode(ServerboundStaffSchoolPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot());
        buffer.writeUtf(packet.school(), 24);
        buffer.writeUtf(packet.previousSchool(), 24);
    }

    public static ServerboundStaffSchoolPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundStaffSchoolPacket(buffer.readVarInt(), buffer.readUtf(24), buffer.readUtf(24));
    }

    public static void handle(ServerboundStaffSchoolPacket packet, Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player != null) apply(player, packet);
        supplier.get().setPacketHandled(true);
    }

    public static boolean apply(ServerPlayer player, ServerboundStaffSchoolPacket packet) {
        FinalStaffSchool mode = FinalStaffSchool.byId(packet.school());
        if (mode == null || !player.isAlive() || player.isSpectator()) return false;
        if (packet.slot() != 40 && (packet.slot() < 0 || packet.slot() > 8
                || packet.slot() != player.getInventory().selected)) return false;
        ItemStack stack = player.getInventory().getItem(packet.slot());
        if (!FinalStaffSchool.isFinalStaff(stack)
                || !FinalStaffSchool.get(stack).id.equals(packet.previousSchool())) return false;
        if (player.isUsingItem()) {
            player.displayClientMessage(Component.translatable("message.starfantasy_goety.school_casting"), true);
            return false;
        }
        FinalStaffSchool.set(stack, mode);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable("message.starfantasy_goety.school_selected", mode.label()), true);
        return true;
    }
}
