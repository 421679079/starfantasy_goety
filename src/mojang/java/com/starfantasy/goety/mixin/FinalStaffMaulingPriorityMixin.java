package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.magic.spells.wild.MaulingSpell;
import com.Polarice3.Goety.common.entities.ally.Summoned;
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
@Mixin(value = MaulingSpell.class, remap = false)
public abstract class FinalStaffMaulingPriorityMixin {
    @ModifyVariable(method = "SpellResult(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/common/magic/SpellStat;)V", name = "summonedentity", require = 0, at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/common/entities/ally/Summoned;setTrueOwner(Lnet/minecraft/world/entity/LivingEntity;)V",
            shift = At.Shift.AFTER), remap = false)
    private Summoned starfantasy$preferredVariant(Summoned original, ServerLevel level,
            LivingEntity caster, ItemStack staff, SpellStat stats) {
        return FinalStaffSummonPriority.apply(FinalStaffPriority.SummonFamily.MAULING, original,
                level, caster, staff, Summoned.class);
    }
}
