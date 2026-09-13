package com.starfantasy.goety.magic.guard;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** A visual only entity; damage interception is exclusively server-side in GuardChannel. */
public final class GuardShieldEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(GuardShieldEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(GuardShieldEntity.class, EntityDataSerializers.LONG);
    public GuardShieldEntity(EntityType<? extends GuardShieldEntity> type, Level level) {
        super(type, level); noPhysics = true;
    }
    @Override protected void defineSynchedData() { entityData.define(OWNER, -1); entityData.define(START, 0L); }
    public void initialize(LivingEntity owner) {
        entityData.set(OWNER, owner.getId()); entityData.set(START, level().getGameTime());
        setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5D)));
    }
    public LivingEntity owner() {
        return level().getEntity(entityData.get(OWNER)) instanceof LivingEntity living ? living : null;
    }
    public float age(float partial) { return (float)(level().getGameTime() - entityData.get(START)) + partial; }
    @Override public void tick() {
        super.tick();
        LivingEntity owner = owner();
        if (owner != null) setPos(owner.getEyePosition().add(owner.getLookAngle().scale(1.5D)));
        if (!level().isClientSide && (owner == null || !owner.isAlive() || !owner.isUsingItem()
                || !GuardChannel.isGuardStaff(owner.getUseItem()))) discard();
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
    @Override public boolean isPickable() { return false; }
}
