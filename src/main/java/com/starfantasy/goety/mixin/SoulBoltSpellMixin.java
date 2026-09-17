package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.spells.SoulBoltSpell;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.starfantasy.goety.magic.FinalStaffPriority;
import com.starfantasy.goety.magic.FinalStaffSchool;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SoulBoltSpell.class, remap = false)
public abstract class SoulBoltSpellMixin {
    @Redirect(method = "SpellResult(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/common/magic/SpellStat;)V", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;m_150930_(Lnet/minecraft/world/item/Item;)Z"), remap = false)
    private boolean starfantasy$namelessNecromancy(ItemStack stack, Item item,
            ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        if (stack.m_41720_() == item) return true;
        return item == ModItems.NAMELESS_STAFF.get() && FinalStaffSchool.isFinalStaff(stack)
                && FinalStaffPriority.soulBoltChoice(stack, CuriosFinder.hasNetherSet(caster)) == SpellType.NECROMANCY;
    }

    @Redirect(method = "SpellResult(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/common/magic/SpellStat;)V", require = 0, at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/common/magic/spells/SoulBoltSpell;typeStaff(Lnet/minecraft/world/item/ItemStack;Lcom/Polarice3/Goety/api/magic/SpellType;)Z"), remap = false)
    private boolean starfantasy$preferredBolt(SoulBoltSpell spell, ItemStack stack, SpellType requested,
            ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        if (FinalStaffSchool.isFinalStaff(stack) && (requested == SpellType.WILD || requested == SpellType.NETHER)) {
            return FinalStaffPriority.soulBoltChoice(stack, CuriosFinder.hasNetherSet(caster)) == requested;
        }
        // Other checks (including SoulBolt's necromancy bonus) retain full-school matching.
        return spell.typeStaff(stack, requested);
    }
}
