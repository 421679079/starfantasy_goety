package com.starfantasy.goety.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Compile-time declaration only; replaced during final assembly. */
public final class ApollyonPageantSummonEntity extends Entity {
    public ApollyonPageantSummonEntity(EntityType<? extends ApollyonPageantSummonEntity> type, Level level) {
        super(type, level);
    }
    @Override protected void defineSynchedData() { throw new UnsupportedOperationException("Build stub"); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { throw new UnsupportedOperationException("Build stub"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { throw new UnsupportedOperationException("Build stub"); }
}
