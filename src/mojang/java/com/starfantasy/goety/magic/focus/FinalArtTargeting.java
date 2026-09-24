package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.utils.MobUtil;
import com.starfantasy.goety.config.SpellConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

final class FinalArtTargeting {
    // Additional qualification after BattleFocusCombat's self, ally and player protections.
    static boolean allows(LivingEntity caster, LivingEntity target) {
        if (SpellConfig.FINAL_ART_DAMAGE_ALL_NON_ALLIES.get()) return true;
        if (target instanceof Player) return false;
        if (isCasterSide(caster, target.getLastHurtByMob())) return true;
        if (target instanceof Mob mob && isCasterSide(caster, mob.getTarget())) return true;
        return target instanceof Enemy && !target.hasPassenger(passenger -> passenger instanceof Player);
    }

    private static boolean isCasterSide(LivingEntity caster, LivingEntity entity) {
        return entity != null && (entity == caster || MobUtil.areAllies(caster, entity));
    }

    private FinalArtTargeting() {}
}
