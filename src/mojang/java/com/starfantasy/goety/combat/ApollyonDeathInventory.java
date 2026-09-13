package com.starfantasy.goety.combat;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

/** One death-scoped rule override; inventory and respawn copying remain vanilla. */
public final class ApollyonDeathInventory {
    private static final String KEEP_ON_DEATH = "StarFantasyApollyonKeepInventoryOnDeath";

    public interface Encounter {
        boolean isInventoryProtectedParticipant(UUID player);
    }

    private ApollyonDeathInventory() {}

    public static void captureDeath(ServerPlayer player) {
        clear(player);
        if (player.isCreative() || player.isSpectator()
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        // Consult the existing encounter membership once, after death cancellation has been resolved.
        for (var entity : player.serverLevel().getAllEntities()) {
            if (entity.isAlive() && entity instanceof Encounter encounter
                    && encounter.isInventoryProtectedParticipant(player.getUUID())) {
                player.getPersistentData().putBoolean(KEEP_ON_DEATH, true);
                return;
            }
        }
    }

    public static boolean isProtected(Player player) {
        return player.getPersistentData().getBoolean(KEEP_ON_DEATH);
    }

    public static boolean rule(GameRules rules, GameRules.Key<GameRules.BooleanValue> key, Player player) {
        return rules.getBoolean(key) || key == GameRules.RULE_KEEPINVENTORY && isProtected(player);
    }

    public static void clear(Player player) {
        player.getPersistentData().remove(KEEP_ON_DEATH);
    }
}
