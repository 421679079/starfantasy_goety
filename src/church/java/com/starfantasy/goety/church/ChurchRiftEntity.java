package com.starfantasy.goety.church;

import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** A temporary, server-authoritative entrance. No chunk tickets or retained player objects. */
public final class ChurchRiftEntity extends Entity {
    private static final int CHARGING = 0, OPEN = 1, CLOSING = 2;
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ChurchRiftEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> PHASE_START = SynchedEntityData.defineId(ChurchRiftEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> CLOSE_REVEAL = SynchedEntityData.defineId(ChurchRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CLOSE_OPEN = SynchedEntityData.defineId(ChurchRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CLOSE_BLOCK_TIME = SynchedEntityData.defineId(ChurchRiftEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerId;
    private InteractionHand chargingHand = InteractionHand.MAIN_HAND;
    private int blockedMessageCooldown;

    public ChurchRiftEntity(EntityType<? extends ChurchRiftEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static List<ChurchRiftEntity> forOwner(ServerLevel level, ServerPlayer player) {
        return level.getEntitiesOfClass(ChurchRiftEntity.class, player.getBoundingBox().inflate(64),
                rift -> player.getUUID().equals(rift.ownerId) && !rift.isRemoved());
    }

    public static boolean begin(ServerLevel level, ServerPlayer player, InteractionHand hand) {
        ChurchRiftEntity rift = ChurchContent.CHURCH_RIFT.get().create(level);
        if (rift == null) return false;
        double yaw = Math.toRadians(player.getYRot());
        Vec3 center = player.position().add(-Math.sin(yaw) * 6,
                ChurchRiftShape.CENTER_ABOVE_FEET, Math.cos(yaw) * 6);
        rift.moveTo(center.x, center.y, center.z, player.getYRot(), 0);
        rift.ownerId = player.getUUID();
        rift.chargingHand = hand;
        rift.entityData.set(PHASE_START, level.getGameTime());
        return level.addFreshEntity(rift);
    }

    public void finishCharging(ServerPlayer player) {
        if (entityData.get(PHASE) != CHARGING || !player.getUUID().equals(ownerId)
                || player.getTicksUsingItem() < ChurchRiftShape.CHARGE_TICKS - 1
                || player.distanceToSqr(this) > 32 * 32) return;
        ServerLevel nether=player.server.getLevel(Level.NETHER);
        if (nether==null || ChurchTravel.prepare(nether)==null) { cancelCharging(); return; }
        entityData.set(PHASE, OPEN);
        entityData.set(PHASE_START, level().getGameTime());
        level().playSound(null, blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.65F, 1.3F);
        player.getCooldowns().addCooldown(ChurchContent.UNDERWORLD_EYE.get(), ChurchRiftShape.IDLE_TICKS);

    }

    public void cancelCharging() {
        if (entityData.get(PHASE) == CHARGING) close();
    }

    private void close() {
        if (entityData.get(PHASE) == CLOSING) return;
        entityData.set(CLOSE_BLOCK_TIME, blockAnimationTime(0));
        entityData.set(CLOSE_REVEAL, reveal(0));
        entityData.set(CLOSE_OPEN, opening(0));
        entityData.set(PHASE, CLOSING);
        entityData.set(PHASE_START, level().getGameTime());
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (blockedMessageCooldown > 0) blockedMessageCooldown--;
        int phase = entityData.get(PHASE);
        float age = phaseAge(0);
        if (phase == CHARGING) {
            ServerPlayer owner = ownerId == null ? null : server.getServer().getPlayerList().getPlayer(ownerId);
            if (owner == null || owner.level() != server || !UnderworldEyeItem.canOpen(owner)
                    || !owner.isUsingItem() || owner.getUsedItemHand() != chargingHand
                    || !owner.getUseItem().is(ChurchContent.UNDERWORLD_EYE.get())
                    || owner.distanceToSqr(this) > 32 * 32 || age > ChurchRiftShape.CHARGE_TICKS + 5) close();
        } else if (phase == CLOSING) {
            if (age >= ChurchRiftShape.CLOSE_TICKS) discard();
        } else if (age >= ChurchRiftShape.IDLE_TICKS) {
            close();
        } else if (age >= ChurchRiftShape.OPEN_TICKS) {
            for (ServerPlayer player : List.copyOf(server.players())) {
                if (!player.isAlive() || player.isSpectator() || !holdsKey(player)) continue;
                Vec3 current = local(player.position().add(0, player.getBbHeight() * 0.5, 0));
                Vec3 previous = local(new Vec3(player.xo, player.yo + player.getBbHeight() * 0.5, player.zo));
                if (ChurchRiftShape.crosses(previous.x, previous.y, previous.z, current.x, current.y, current.z)) {
                    if (ChurchTravel.transfer(player)) {
                        close();
                        server.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);
                        break; // One successful transfer consumes this entrance, including in multiplayer.
                    }
                    if (blockedMessageCooldown == 0) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.starfantasy_goety.underworld_eye.unsafe"),true);
                        blockedMessageCooldown = 40;
                    }
                }
            }
        }
    }

    public static boolean holdsKey(ServerPlayer player) {
        return player.getMainHandItem().is(ChurchContent.UNDERWORLD_EYE.get())
                || player.getOffhandItem().is(ChurchContent.UNDERWORLD_EYE.get());
    }


    public Vec3 local(Vec3 worldPoint) {
        Vec3 delta = worldPoint.subtract(position());
        double yaw = Math.toRadians(getYRot()), cos = Math.cos(yaw), sin = Math.sin(yaw);
        return new Vec3(delta.x * cos + delta.z * sin, delta.y, -delta.x * sin + delta.z * cos);
    }

    public float phaseAge(float partialTick) { return Math.max(0, level().getGameTime() - entityData.get(PHASE_START) + partialTick); }
    public boolean hasOpened() {
        return entityData.get(PHASE) == OPEN || (entityData.get(PHASE) == CLOSING && entityData.get(CLOSE_OPEN) > 0);
    }

    /** Continuous at the opening transition; only the block lifecycle clock accelerates. */
    public float blockAnimationTime(float partialTick) {
        float age = phaseAge(partialTick);
        return switch (entityData.get(PHASE)) {
            case CHARGING -> age / 20F;
            case OPEN -> ChurchRiftShape.CHARGE_TICKS / 20F + age / 10F;
            default -> entityData.get(CLOSE_BLOCK_TIME) + age / (hasOpened() ? 10F : 20F);
        };
    }
    private static float smooth(float v) { v = Mth.clamp(v, 0, 1); return v * v * (3 - 2 * v); }
    public float reveal(float partialTick) {
        return switch (entityData.get(PHASE)) {
            case CHARGING -> smooth(phaseAge(partialTick) / ChurchRiftShape.CHARGE_TICKS);
            case CLOSING -> entityData.get(CLOSE_REVEAL) * (1 - smooth(phaseAge(partialTick) / ChurchRiftShape.CLOSE_TICKS));
            default -> 1;
        };
    }
    public float opening(float partialTick) {
        return switch (entityData.get(PHASE)) {
            case CHARGING -> 0;
            case CLOSING -> entityData.get(CLOSE_OPEN) * (1 - smooth(phaseAge(partialTick) / ChurchRiftShape.CLOSE_TICKS));
            default -> smooth(phaseAge(partialTick) / ChurchRiftShape.OPEN_TICKS);
        };
    }

    @Override protected void defineSynchedData() {
        entityData.define(PHASE, CHARGING);
        entityData.define(PHASE_START, 0L);
        entityData.define(CLOSE_REVEAL, 0F);
        entityData.define(CLOSE_OPEN, 0F);
        entityData.define(CLOSE_BLOCK_TIME, 0F);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public boolean isPickable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 128 * 128; }
    @Override public AABB getBoundingBoxForCulling() { return new AABB(position(), position()).inflate(9); }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
