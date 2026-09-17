package com.starfantasy.goety.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Arena-only flight suppression, called once per active boss server tick. */
public final class ApollyonArenaFlight {
    private static final double HEIGHT_ALLOWANCE = 3.0D;
    private static final double FALL_ACCELERATION = 0.35D;
    private static final double MAX_FORCED_FALL_SPEED = -2.5D;

    private ApollyonArenaFlight() {}

    public static void tick(ServerLevel level, Vec3 home, double radius, double combatDistanceSqr) {
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) continue;
            double dx = player.getX() - home.x, dz = player.getZ() - home.z;
            if (dx * dx + dz * dz > radius * radius
                    || player.position().distanceToSqr(home) > combatDistanceSqr) continue;

            // Also covers survival flight granted by another mod. Keep the ability
            // entitlement untouched so leaving the encounter needs no restoration.
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            if (isTooHigh(player, estimatedJumpHeight(player) + HEIGHT_ALLOWANCE)) {
                forceQuickFall(player);
            }
        }
    }

    private static boolean isTooHigh(ServerPlayer player, double allowedHeight) {
        ServerLevel level = player.serverLevel();
        int x = player.getBlockX(), z = player.getBlockZ();
        int startY = Math.min(level.getMaxBuildHeight() - 1, player.getBlockY() - 1);
        // Only ground within the allowance can prevent the downward force. This
        // is equivalent to the bosses' ground scan without scanning to bedrock.
        int minY = Math.max(level.getMinBuildHeight(), (int) Math.ceil(player.getY() - allowedHeight - 1.0D));
        for (int y = startY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP)) return false;
        }
        return true;
    }

    private static double estimatedJumpHeight(ServerPlayer player) {
        double velocity = 0.42D;
        MobEffectInstance jump = player.getEffect(MobEffects.JUMP);
        if (jump != null) velocity += 0.1D * (jump.getAmplifier() + 1);
        double height = 0.0D;
        for (int i = 0; i < 64 && velocity > 0.0D; i++) {
            height += velocity;
            velocity = (velocity - 0.08D) * 0.98D;
        }
        return height;
    }

    private static void forceQuickFall(ServerPlayer player) {
        Vec3 movement = player.getDeltaMovement();
        double nextY = Math.max(MAX_FORCED_FALL_SPEED, movement.y - FALL_ACCELERATION);
        player.setDeltaMovement(movement.x * 0.25D, nextY, movement.z * 0.25D);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
}
