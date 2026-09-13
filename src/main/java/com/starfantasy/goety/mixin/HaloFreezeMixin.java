package com.starfantasy.goety.mixin;

import com.starfantasy.goety.combat.HaloProtection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, remap = false)
public abstract class HaloFreezeMixin {
    @ModifyVariable(method = "m_146917_(I)V", at = @At("HEAD"), argsOnly = true, remap = false)
    private int starfantasy$preventHaloFreezing(int ticks) {
        return ticks > 0 && HaloProtection.preventsFreezing((Entity)(Object)this) ? 0 : ticks;
    }

    @Inject(method = "m_146888_()I", at = @At("RETURN"), cancellable = true, remap = false)
    private void starfantasy$hideStaleFrozenTicks(CallbackInfoReturnable<Integer> callback) {
        if (callback.getReturnValueI() > 0 && HaloProtection.preventsFreezing((Entity)(Object)this)) {
            callback.setReturnValue(0);
        }
    }
}
