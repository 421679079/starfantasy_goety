package com.starfantasy.goety.church;

import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Synchronizes the server-authoritative church boundary to each player's client. */
@Mod.EventBusSubscriber(modid = ChurchContent.MODID)
public final class ChurchFogSyncEvents {
    private static final ResourceLocation CHURCH =
            new ResourceLocation(ChurchContent.MODID, "church");
    private static final Map<UUID, Boolean> LAST_STATE = new HashMap<>();

    private ChurchFogSyncEvents() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        boolean inside = isInsideChurch(player);
        Boolean previous = LAST_STATE.put(player.getUUID(), inside);
        if (previous == null || previous != inside) {
            StarFantasyGoetyNetwork.setChurchFog(player, inside);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_STATE.remove(event.getEntity().getUUID());
    }

    private static boolean isInsideChurch(ServerPlayer player) {
        var level = player.serverLevel();
        var structure = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(CHURCH);
        return structure != null
                && level.structureManager()
                .getStructureAt(player.blockPosition(), structure)
                .isValid();
    }
}
