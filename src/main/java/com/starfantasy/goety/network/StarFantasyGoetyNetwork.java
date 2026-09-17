package com.starfantasy.goety.network;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Compact, one-shot visual messages owned by this addon. */
public final class StarFantasyGoetyNetwork {
    private static final String PROTOCOL = "10";
    private static final double LIGHTNING_TRACKING_RANGE = 256.0D;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StarFantasyGoetyMod.MODID, "visuals"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private StarFantasyGoetyNetwork() {
    }

    public static void init() {
        CHANNEL.messageBuilder(ClientboundApollyonLightningStrikePacket.class, 0,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundApollyonLightningStrikePacket::encode)
                .decoder(ClientboundApollyonLightningStrikePacket::decode)
                .consumerMainThread(ClientboundApollyonLightningStrikePacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundApollyonSnapPacket.class, 1,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundApollyonSnapPacket::encode)
                .decoder(ClientboundApollyonSnapPacket::decode)
                .consumerMainThread(ClientboundApollyonSnapPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundApollyonDarknessPacket.class, 2,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundApollyonDarknessPacket::encode)
                .decoder(ClientboundApollyonDarknessPacket::decode)
                .consumerMainThread(ClientboundApollyonDarknessPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundApollyonWhiteoutPacket.class, 3,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundApollyonWhiteoutPacket::encode)
                .decoder(ClientboundApollyonWhiteoutPacket::decode)
                .consumerMainThread(ClientboundApollyonWhiteoutPacket::handle)
                .add();
        CHANNEL.messageBuilder(ClientboundChurchFogPacket.class, 4,
                        NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundChurchFogPacket::encode)
                .decoder(ClientboundChurchFogPacket::decode)
                .consumerMainThread(ClientboundChurchFogPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundHadesRidePacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundHadesRidePacket::encode)
                .decoder(ServerboundHadesRidePacket::decode)
                .consumerMainThread(ServerboundHadesRidePacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundHadesDismountPacket.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundHadesDismountPacket::encode)
                .decoder(ServerboundHadesDismountPacket::decode)
                .consumerMainThread(ServerboundHadesDismountPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundHadesAttackPacket.class, 7, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundHadesAttackPacket::encode)
                .decoder(ServerboundHadesAttackPacket::decode)
                .consumerMainThread(ServerboundHadesAttackPacket::handle)
                .add();
        CHANNEL.messageBuilder(ServerboundStaffSchoolPacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundStaffSchoolPacket::encode)
                .decoder(ServerboundStaffSchoolPacket::decode)
                .consumerMainThread(ServerboundStaffSchoolPacket::handle)
                .add();
    }

    public static void sendHadesRideInput(ServerboundHadesRidePacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void selectStaffSchool(ServerboundStaffSchoolPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void attackWithHades(int entityId, float yaw) {
        CHANNEL.sendToServer(new ServerboundHadesAttackPacket(entityId, yaw));
    }

    public static void dismountHades(int entityId) { CHANNEL.sendToServer(new ServerboundHadesDismountPacket(entityId)); }

    public static void sendApollyonLightningStrike(ServerLevel level, Vec3 center) {
        sendApollyonLightningStrike(level, center, 3.0F);
    }

    public static void sendApollyonLightningStrike(
            ServerLevel level, Vec3 center, float effectSize) {
        long seed = level.m_213780_().m_188505_();
        PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(
                center.f_82479_, center.f_82480_, center.f_82481_,
                LIGHTNING_TRACKING_RANGE, level.m_46472_());
        CHANNEL.send(PacketDistributor.NEAR.with(() -> target),
                new ClientboundApollyonLightningStrikePacket(center, seed, effectSize));
    }

    public static void snapEntity(Entity entity, Vec3 position, float yaw, float pitch) {
        if (entity == null || position == null || entity.m_9236_().f_46443_) {
            return;
        }
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new ClientboundApollyonSnapPacket(
                        entity.m_19879_(), position.f_82479_, position.f_82480_,
                        position.f_82481_, yaw, pitch));
    }

    public static void setPageantDarkness(ServerPlayer player, int duration) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientboundApollyonDarknessPacket(duration));
    }

    public static void startPageantWhiteout(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientboundApollyonWhiteoutPacket());
    }

    public static void setChurchFog(ServerPlayer player, boolean inside) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ClientboundChurchFogPacket(inside));
    }
}
