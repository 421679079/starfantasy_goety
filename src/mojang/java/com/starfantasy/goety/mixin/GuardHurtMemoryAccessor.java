package com.starfantasy.goety.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface GuardHurtMemoryAccessor {
    @Accessor(value = "f_20898_", remap = false) float starfantasy$getLastHurt();
    @Accessor(value = "f_20898_", remap = false) void starfantasy$setLastHurt(float amount);
}
