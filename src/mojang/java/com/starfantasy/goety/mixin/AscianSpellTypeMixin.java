package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.magic.AscianStaffSchool;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value=ISpell.class,remap=false)
public interface AscianSpellTypeMixin {
    /**
     * @author Star Fantasy
     * @reason Mixin 0.8.5 cannot inject into interface default methods. Retain Goety's exact fallback.
     */
    @Overwrite(remap=false)
    default boolean typeStaff(ItemStack staff, SpellType type){
        if(AscianStaffSchool.isAscian(staff))return AscianStaffSchool.get(staff).type==type;
        return staff.getItem() instanceof IWand wand&&wand.getSpellTypes().contains(type);
    }
}
