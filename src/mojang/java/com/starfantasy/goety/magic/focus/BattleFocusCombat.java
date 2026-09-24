package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.utils.MobUtil;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;

public final class BattleFocusCombat {
    public static void sonicDamage(ServerLevel level, LivingEntity caster, Vec3 center, double radius,
                                   float amount, java.util.function.Predicate<LivingEntity> filter) {
        damageFiltered(level, caster, center, radius, caster.damageSources().sonicBoom(caster), amount, 1, filter);
    }
    static List<LivingEntity> targets(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        List<LivingEntity> targets = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        for (Entity candidate : level.getEntities(caster, new AABB(center, center).inflate(radius), Entity::isAlive)) {
            Entity resolved = candidate instanceof PartEntity<?> part ? part.getParent() : candidate;
            if (!(resolved instanceof LivingEntity target) || target == caster || !target.isAlive()
                    || target.isSpectator() || !target.isAttackable() || MobUtil.areAllies(caster, target)
                    || target.position().distanceToSqr(center) > radius * radius || !ids.add(target.getId())) continue;
            if (target instanceof Player player && (player.isCreative()
                    || caster instanceof Player owner && !owner.canHarmPlayer(player))) continue;
            targets.add(target);
        }
        return targets;
    }
    static void damage(ServerLevel level, LivingEntity caster, Vec3 center, double radius, DamageSource source, float amount, int segments) {
        damageFiltered(level, caster, center, radius, source, amount, segments, target -> true);
    }
    static void damageFiltered(ServerLevel level, LivingEntity caster, Vec3 center, double radius, DamageSource source,
                               float amount, int segments, java.util.function.Predicate<LivingEntity> filter) {
        if (amount <= 0) return;
        for (LivingEntity target : targets(level, caster, center, radius)) {
            if (!filter.test(target)) continue;
            for (int i = 0; i < segments && target.isAlive(); i++) {
                // Normal hurt pipeline: retain armor, event cancellation, caps and phase/encounter locks.
                Vec3 motion = target.getDeltaMovement();
                BattleFocusHitImmunity.clear(target);
                try { target.hurt(source, amount); }
                finally { BattleFocusHitImmunity.clear(target); target.setDeltaMovement(motion); }
            }
        }
    }
    private BattleFocusCombat() {}
}
