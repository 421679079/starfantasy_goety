package com.starfantasy.goety.magic.focus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class ChaosMeteorPath {
    private static final double FLOOR_SEARCH = 20;
    private static final double MAX_CLIMB = 5;
    private static final double EPSILON = 1.0E-4;
    private static final int ROLL_STEPS = 4;
    private static final double ROLL_SPEED = .4;

    static Vec3 aim(ServerLevel level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(range));
        var hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        // Keep a side hit just outside the wall so the downward probe samples the near side.
        return hit.getType() == HitResult.Type.BLOCK
                ? hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(EPSILON))
                : end;
    }

    static Vec3 anchor(ServerLevel level, LivingEntity caster, Vec3 probe) {
        if (!level.hasChunkAt(BlockPos.containing(probe))) return null;
        Vec3 bottom = probe.add(0, -FLOOR_SEARCH, 0);
        return floor(level, caster, probe, bottom, bottom);
    }

    private static Vec3 floor(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end, Vec3 fallback) {
        var context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
        var hit = level.clip(context);
        if (hit.getType() != HitResult.Type.BLOCK) return fallback;
        if (hit.isInside()) {
            // VoxelShape.clip tests a point 0.1% down the ray, which can be inside a floor
            // even when the origin is just above it. Intersect the exact collision faces.
            BlockPos block = hit.getBlockPos();
            var shape = context.getBlockShape(level.getBlockState(block), level, block);
            hit = AABB.clip(shape.toAabbs(), start.add(0, EPSILON, 0), end, block);
            if (hit == null) return fallback;
        }
        return hit.getLocation().add(0, EPSILON, 0);
    }

    static Vec3 roll(ServerLevel level, LivingEntity caster, Vec3 position, Vec3 direction) {
        Vec3 step = direction.scale(ROLL_SPEED / ROLL_STEPS);
        Vec3 current = position;
        for (int index = 0; index < ROLL_STEPS; index++) {
            Vec3 next = current.add(step);
            if (!level.hasChunkAt(BlockPos.containing(next))) return null;
            double top = obstacleTop(level, caster, next);
            if (top > position.y + MAX_CLIMB + EPSILON) return null;
            current = top > next.y
                    ? new Vec3(next.x, top + EPSILON, next.z)
                    : floor(level, caster, next, next.add(0, -FLOOR_SEARCH, 0), next);
        }
        return current;
    }

    private static double obstacleTop(ServerLevel level, LivingEntity caster, Vec3 step) {
        AABB column = new AABB(step.x - EPSILON, step.y, step.z - EPSILON,
                step.x + EPSILON, step.y + MAX_CLIMB + EPSILON, step.z + EPSILON);
        List<AABB> boxes = new ArrayList<>();
        for (var shape : level.getBlockCollisions(caster, column)) boxes.addAll(shape.toAabbs());
        boxes.sort(Comparator.comparingDouble(box -> box.minY));
        double top = step.y;
        // Follow contiguous obstacles from the ground up. A detached cave ceiling is not a wall.
        for (AABB box : boxes) {
            if (box.minY > top + EPSILON) break;
            top = Math.max(top, box.maxY);
        }
        return top;
    }

    private ChaosMeteorPath() {}
}
