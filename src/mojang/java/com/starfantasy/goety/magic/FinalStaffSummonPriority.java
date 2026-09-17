package com.starfantasy.goety.magic;

import com.Polarice3.Goety.common.entities.ally.Summoned;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class FinalStaffSummonPriority {
    private FinalStaffSummonPriority() {}

    /** Runs after variant selection/owner assignment but before any spawn setup or world insertion. */
    public static <T extends Summoned> T apply(FinalStaffPriority.SummonFamily family, T original,
                                              ServerLevel level, LivingEntity caster, ItemStack staff,
                                              Class<T> familyClass) {
        String id = family.preferredEntity(staff);
        if (id == null) return original;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("goety", id));
        if (type == null || type == original.getType()) return original;
        Entity replacement = type.create(level);
        if (!familyClass.isInstance(replacement)) return original;
        T summon = familyClass.cast(replacement);
        summon.setTrueOwner(caster);
        return summon;
    }
}
