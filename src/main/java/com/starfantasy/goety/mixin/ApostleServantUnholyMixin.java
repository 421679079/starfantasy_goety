package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.utils.CuriosFinder;
import com.starfantasy.goety.entity.ApostleServantEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Use the native Apostle branch of FireBlastSpell and FireBlastTrap for this servant. */
@Mixin(value=CuriosFinder.class,remap=false)
public abstract class ApostleServantUnholyMixin {
    @Inject(method="hasUnholySet",at=@At("HEAD"),cancellable=true,remap=false)
    private static void starfantasy$servant(LivingEntity entity,CallbackInfoReturnable<Boolean> result) {
        if(entity instanceof ApostleServantEntity) result.setReturnValue(true);
    }
}
