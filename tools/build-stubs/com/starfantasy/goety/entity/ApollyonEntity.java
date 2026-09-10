package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.hostile.cultists.Cultist;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Compile-time declaration only; never shipped in place of the real implementation. */
public abstract class ApollyonEntity extends Cultist {
    public ApollyonEntity(EntityType<? extends ApollyonEntity> type, Level level) {
        super(type, level);
    }
}
