package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.combat.apostle.ApostleSpellSupport;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"com.Polarice3.Goety.common.entities.boss.Apostle$FireballSpellGoal",
        "com.Polarice3.Goety.common.entities.boss.Apostle$DamnedSpellGoal"}, remap = false)
public abstract class ApostleNativeSpellMixin {
    @Shadow(remap = false) @Final private Apostle this$0;
    @Inject(method = "m_8036_", at = @At("HEAD"), cancellable = true)
    private void starfantasy$titleSlot(CallbackInfoReturnable<Boolean> ci) {
        if (ApostleSpellSupport.enhanced(this$0)) ci.setReturnValue(false);
    }
}
