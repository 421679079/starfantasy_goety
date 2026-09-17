package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.magic.spells.wild.SlimySpell;
import com.Polarice3.Goety.common.entities.ally.SlimeServant;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.starfantasy.goety.magic.FinalStaffPriority;
import com.starfantasy.goety.magic.FinalStaffSummonPriority;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Leave count, potency and subsequent spawn setup to Goety; replace only the selected variant. */
@Mixin(value = SlimySpell.class, remap = false)
public abstract class FinalStaffSlimePriorityMixin {
    @ModifyVariable(method = "SpellResult(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/common/magic/SpellStat;)V", name = "slimeServant", require = 0, at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/common/entities/ally/SlimeServant;setTrueOwner(Lnet/minecraft/world/entity/LivingEntity;)V",
            shift = At.Shift.AFTER), remap = false)
    private SlimeServant starfantasy$preferredVariant(SlimeServant original, ServerLevel level,
            LivingEntity caster, ItemStack staff, SpellStat stats) {
        return FinalStaffSummonPriority.apply(FinalStaffPriority.SummonFamily.SLIME, original,
                level, caster, staff, SlimeServant.class);
    }
}
