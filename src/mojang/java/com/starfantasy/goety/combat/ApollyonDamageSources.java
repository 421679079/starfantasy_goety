package com.starfantasy.goety.combat;

import com.Polarice3.Goety.utils.NoKnockBackDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Per-victim source coordinates for telegraphs; no damage type or attribution is discarded. */
public final class ApollyonDamageSources {
    private ApollyonDamageSources() {}
    public static DamageSource front(LivingEntity victim, DamageSource base) {
        return facing(victim, base, true);
    }
    public static DamageSource rear(LivingEntity victim, DamageSource base) {
        return facing(victim, base, false);
    }
    private static DamageSource facing(LivingEntity victim, DamageSource base, boolean front) {
        if (!(victim instanceof Player)) return base;
        Vec3 position = position(victim.position(), victim.getYRot(), victim.getBbHeight(), front);
        return at(base, position);
    }
    public static Vec3 position(Vec3 feet, float yaw, float height, boolean front) {
        double radians = Math.toRadians(yaw), distance = front ? 2.0D : -2.0D;
        return feet.add(-Math.sin(radians) * distance, height * .5D, Math.cos(radians) * distance);
    }
    public static DamageSource at(DamageSource base, Vec3 position) {
        Entity direct = base.getDirectEntity(), owner = base.getEntity();
        // Anonymous damage intentionally cannot be parried. Never invent an owner or position for it.
        if (direct == null && owner == null) return base;
        return base instanceof NoKnockBackDamageSource
                ? new NoKnockback(base, direct, owner, position)
                : new DamageSource(base.typeHolder(), direct, owner, position);
    }
    // Goety checks this subtype to suppress knockback; retain that behavior for acid/hellfire.
    private static final class NoKnockback extends NoKnockBackDamageSource {
        private final Vec3 position;
        NoKnockback(DamageSource base, Entity direct, Entity owner, Vec3 position) {
            super(base.typeHolder(), direct, owner);
            this.position = position;
        }
        @Override public Vec3 getSourcePosition() { return position; }
        @Override public Vec3 sourcePositionRaw() { return position; }
    }
}
