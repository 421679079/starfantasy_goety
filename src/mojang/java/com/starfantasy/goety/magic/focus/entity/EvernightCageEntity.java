package com.starfantasy.goety.magic.focus.entity;

import com.starfantasy.library.vfx.StarFantasyCageGeometry;

import com.starfantasy.goety.magic.focus.BattleFocusContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/** One synchronized visual group: five analytical projectiles and a growing cage. No collision or damage. */
public final class EvernightCageEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(EvernightCageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> BIRTH = SynchedEntityData.defineId(EvernightCageEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> FACING = SynchedEntityData.defineId(EvernightCageEntity.class, EntityDataSerializers.FLOAT);
    private boolean restored;

    public EvernightCageEntity(EntityType<? extends EvernightCageEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    public EvernightCageEntity(net.minecraftforge.network.PlayMessages.SpawnEntity packet, Level level) {
        this(BattleFocusContent.EVERNIGHT_CAGE.get(), level);
    }
    public void configure(Vec3 anchor, float facing, int color) {
        setPos(anchor); entityData.set(FACING, facing); entityData.set(COLOR, color & 0xFFFFFF);
        entityData.set(BIRTH, level().getGameTime());
    }
    @Override protected void defineSynchedData() {
        entityData.define(COLOR, 0xFF7BAF); entityData.define(BIRTH, -1L); entityData.define(FACING, 0F);
    }
    public int color() { return entityData.get(COLOR); }
    public float facing() { return entityData.get(FACING); }
    public float visualAge(float partial) {
        return entityData.get(BIRTH) < 0 ? 0 : Math.max(0, level().getGameTime() - entityData.get(BIRTH) + partial);
    }
    public Vec3 worldPoint(Vec3 local) { return StarFantasyCageGeometry.toWorld(local, position(), facing()); }
    @Override public void tick() {
        super.tick();
        if (restored || visualAge(0) >= StarFantasyCageGeometry.LIFETIME) discard();
    }
    @Override public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(color()); buffer.writeLong(entityData.get(BIRTH)); buffer.writeFloat(facing());
    }
    @Override public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(COLOR, buffer.readInt()); entityData.set(BIRTH, buffer.readLong()); entityData.set(FACING, buffer.readFloat());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { restored = true; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
