package com.starfantasy.goety.magic.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class BattleFocusPlacement {
    /** Trace down locally, so underground casts use their floor rather than the world surface. */
    static Vec3 ground(ServerLevel level, LivingEntity caster, Vec3 desired) {
        if (!level.hasChunkAt(BlockPos.containing(desired))) return null;
        double top = Math.min(desired.y + 2, level.getMaxBuildHeight());
        if (top <= level.getMinBuildHeight()) return null;
        double ceiling = level.getMaxBuildHeight();
        while (top <= ceiling) {
            var hit = level.clip(new ClipContext(new Vec3(desired.x, top, desired.z),
                    new Vec3(desired.x, level.getMinBuildHeight(), desired.z),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
            if (hit.getType() != HitResult.Type.BLOCK) return null;
            // A forward point embedded in a hillside must rise out of the solid column first.
            if (!hit.isInside()) return hit.getLocation();
            if (top >= ceiling) return null;
            top = Math.min(ceiling, top + 1);
        }
        return null;
    }

    /** One collision-checked horizontal displacement, before Tangled can suppress movement. */
    static void gather(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        for (LivingEntity target : BattleFocusCombat.targets(level, caster, center, radius)) {
            if (target.noPhysics) continue;
            Vec3 inward = new Vec3(center.x - target.getX(), 0, center.z - target.getZ());
            double distance = inward.length();
            if (distance <= 2) continue;
            target.move(MoverType.SELF, inward.scale(Math.min(4, distance - 2) / distance));
            if (target instanceof ServerPlayer player) {
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
            }
        }
    }

    private BattleFocusPlacement() {}
}
