package com.starfantasy.goety.combat.ascian;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

/** Resolve the attacker, not a projectile's impact position, before applying range falloff. */
public final class AscianRangedProtection {
    private AscianRangedProtection() {}

    public static double multiplier(double distance, double maximumDistance) {
        if (!Double.isFinite(distance)) return .1D;
        if (distance <= 5D) return 1D;
        if (maximumDistance <= 5D || distance >= maximumDistance) return .1D;
        return 1D - .9D * (distance - 5D) / (maximumDistance - 5D);
    }

    public static double distance(Entity boss, DamageSource source) {
        Entity attacker = source.m_7639_();
        if (attacker == null) attacker = source.m_7640_();
        if (attacker != null) {
            for (int depth = 0; depth < 8; depth++) {
                if (!(attacker instanceof Projectile projectile)) {
                    if (attacker.m_213877_() || attacker.m_9236_() != boss.m_9236_()) return Double.NaN;
                    return attacker.m_20182_().m_82554_(boss.m_20182_());
                }
                Entity owner = projectile.m_19749_();
                if (owner == null || owner == attacker) return Double.NaN;
                attacker = owner;
            }
            return Double.NaN;
        }
        Vec3 origin = source.m_7270_();
        return origin == null ? Double.NaN : origin.m_82554_(boss.m_20182_());
    }
}
