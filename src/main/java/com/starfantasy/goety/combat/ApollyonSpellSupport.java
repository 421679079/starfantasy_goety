package com.starfantasy.goety.combat;

import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Shared spell policy; the boss and servant retain separate combat controllers. */
public final class ApollyonSpellSupport {
    private ApollyonSpellSupport() {}

    public static boolean isCaster(Entity entity) {
        return entity instanceof ApollyonEntity || entity instanceof ApollyonServantEntity;
    }

    public static boolean warnings(Entity entity) {
        return !(entity instanceof ApollyonServantEntity);
    }

    public static float damage(Mob caster, float base) {
        return caster instanceof ApollyonServantEntity servant ? servant.scaleOutgoingDamage(base)
                : caster instanceof ApollyonEntity boss ? boss.scaleOutgoingDamage(base) : base;
    }

    /** Boss-only max-health bonus; shared servant spells retain their attack-based scaling. */
    public static float damage(Mob caster, LivingEntity target, float base, float maxHealthFraction) {
        return caster instanceof ApollyonEntity boss
                ? boss.outgoingDamage(target, base, maxHealthFraction) : damage(caster, base);
    }

    public static boolean friendly(Entity caster, Entity target) {
        return caster instanceof ApollyonServantEntity servant ? servant.isFriendlyEntity(target)
                : caster instanceof ApollyonEntity boss && boss.isFriendlyEntity(target);
    }

    public static Vec3 home(Mob caster) {
        return caster instanceof ApollyonEntity boss ? boss.arenaHomePosition() : caster.m_20182_();
    }
}
