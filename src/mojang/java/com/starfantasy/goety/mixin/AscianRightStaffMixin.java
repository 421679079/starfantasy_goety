package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.magic.Spell;
import com.starfantasy.goety.magic.AscianStaffSchool;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Spell.class,remap=false)
public abstract class AscianRightStaffMixin {
    @Inject(method="rightStaff(Lnet/minecraft/world/item/ItemStack;)Z",at=@At("HEAD"),cancellable=true,remap=false)
    private void starfantasy$selectedSchool(ItemStack staff,CallbackInfoReturnable<Boolean> callback){
        if(AscianStaffSchool.isAscian(staff))callback.setReturnValue(AscianStaffSchool.get(staff).type==((Spell)(Object)this).getSpellType());
    }
}
