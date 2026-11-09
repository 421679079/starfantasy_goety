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
    private static final String PROTOCOL = "24";
    private static final double LIGHTNING_TRACKING_RANGE = 256.0D;

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(StarFantasyGoetyMod.MODID, "visuals"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private StarFantasyGoetyNetwork() {
    }

    public static void init() {
        CHANNEL.messageBuilder(ClientboundAscianIceRingPacket.class, 13, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundAscianIceRingPacket::encode).decoder(ClientboundAscianIceRingPacket::decode)
                .consumerMainThread(ClientboundAscianIceRingPacket::handle).add();
        CHANNEL.messageBuilder(ClientboundAscianMeteorPacket.class, 12, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundAscianMeteorPacket::encode).decoder(ClientboundAscianMeteorPacket::decode)
                .consumerMainThread(ClientboundAscianMeteorPacket::handle).add();
        CHANNEL.messageBuilder(ClientboundAscianEffectPacket.class, 11, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundAscianEffectPacket::encode).decoder(ClientboundAscianEffectPacket::decode)
                .consumerMainThread(ClientboundAscianEffectPacket::handle).add();
        CHANNEL.messageBuilder(ClientboundIceImpactPacket.class, 10, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundIceImpactPacket::encode).decoder(ClientboundIceImpactPacket::decode)
                .consumerMainThread(ClientboundIceImpactPacket::handle).add();
        CHANNEL.messageBuilder(ClientboundFireTrapWavePacket.class, 9, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundFireTrapWavePacket::encode)
                .decoder(ClientboundFireTrapWavePacket::decode)
                .consumerMainThread(ClientboundFireTrapWavePacket::handle)
                .add();
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

    public static void sendFireTraps(Entity owner, java.util.List<Vec3> centers) {
        if (centers.isEmpty() || !(owner.m_9236_() instanceof ServerLevel level)) return;
        Vec3 center = centers.get(0);
        PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(
                center.f_82479_, center.f_82480_, center.f_82481_, 256.0D, level.m_46472_());
        CHANNEL.send(PacketDistributor.NEAR.with(() -> target), new ClientboundFireTrapWavePacket(
                level.m_46472_().m_135782_(), owner.m_20148_(), owner.m_19879_(),
                level.m_46467_(), level.m_213780_().m_188505_(), centers));
    }
    public static void sendAscianMeteorState(Entity owner,ClientboundAscianMeteorPacket packet){
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->owner),packet);
    }
    public static void sendAscianIceRings(Entity owner,ClientboundAscianIceRingPacket packet){
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->owner),packet);
    }
    public static void sendAscianIceRings(ServerPlayer player,ClientboundAscianIceRingPacket packet){
        CHANNEL.send(PacketDistributor.PLAYER.with(()->player),packet);
    }
    public static void sendAscianMeteorState(ServerPlayer player,ClientboundAscianMeteorPacket packet){
        CHANNEL.send(PacketDistributor.PLAYER.with(()->player),packet);
    }
    public static void sendAscianEffects(Entity owner,int kind,java.util.List<Vec3> centers){
        sendAscianEffects(owner,kind,centers,0);
    }
    public static void sendAscianEffects(Entity owner,int kind,java.util.List<Vec3> centers,int initialAge){
        if(!(owner.m_9236_() instanceof ServerLevel level))return;
        var packet=new ClientboundAscianEffectPacket(level.m_46472_().m_135782_(),owner.m_20148_(),owner.m_19879_(),level.m_46467_()-Math.max(0,initialAge),kind,centers);
        sendAscianWave(owner,level,packet);
    }
    public static void sendAscianMeleeEffects(Entity owner,int phase,int styleOffset,java.util.List<Vec3> centers){
        if(!(owner.m_9236_() instanceof ServerLevel level))return;
        sendAscianWave(owner,level,new ClientboundAscianEffectPacket(level.m_46472_().m_135782_(),owner.m_20148_(),owner.m_19879_(),level.m_46467_(),8,centers,phase,styleOffset));
    }
    private static void sendAscianWave(Entity owner,ServerLevel level,ClientboundAscianEffectPacket packet){
        if(packet.kind()==0){CHANNEL.send(PacketDistributor.DIMENSION.with(level::m_46472_),packet);return;}
        var centers=packet.centers();
        Vec3 p=centers.isEmpty()?owner.m_20182_():centers.get(0);
        var target=new PacketDistributor.TargetPoint(p.f_82479_,p.f_82480_,p.f_82481_,256,level.m_46472_());
        CHANNEL.send(PacketDistributor.NEAR.with(()->target),packet);
    }

    public static void sendIceImpact(Entity entity,float scale) {
        if(!(entity.m_9236_() instanceof ServerLevel level))return;
        Vec3 p=entity.m_20182_();
        PacketDistributor.TargetPoint target=new PacketDistributor.TargetPoint(p.f_82479_,p.f_82480_,p.f_82481_,128,level.m_46472_());
        CHANNEL.send(PacketDistributor.NEAR.with(()->target),new ClientboundIceImpactPacket(p,scale,level.m_213780_().m_188505_()));
    }

    public static void clearFireTraps(Entity owner) {
        if (!(owner.m_9236_() instanceof ServerLevel level)) return;
        // All viewers in this dimension must clear, including those who moved away.
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::m_46472_), new ClientboundFireTrapWavePacket(
                level.m_46472_().m_135782_(), owner.m_20148_(), owner.m_19879_(),
                level.m_46467_(), 0L, java.util.List.of()));
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
