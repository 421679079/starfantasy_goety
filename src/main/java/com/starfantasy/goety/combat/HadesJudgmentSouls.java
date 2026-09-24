package com.starfantasy.goety.combat;

import com.starfantasy.goety.entity.HadesServantEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Routes successful soul deductions directly to the owner's loaded Hades servants. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class HadesJudgmentSouls {
    private static final Map<UUID, Set<HadesServantEntity>> LOADED = new HashMap<>();

    public static void track(HadesServantEntity servant) {
        if (!servant.isAddedToWorld() || servant.m_9236_().f_46443_ || servant.getOwnerId() == null) return;
        LOADED.computeIfAbsent(servant.getOwnerId(), key -> new HashSet<>()).add(servant);
    }

    public static void untrack(HadesServantEntity servant, UUID owner) {
        if (servant.m_9236_().f_46443_ || owner == null) return;
        Set<HadesServantEntity> servants = LOADED.get(owner);
        if (servants != null) {
            servants.remove(servant);
            if (servants.isEmpty()) LOADED.remove(owner);
        }
    }

    public static void spent(Player player, int amount) {
        if (player.m_9236_().f_46443_ || amount <= 0) return;
        Set<HadesServantEntity> servants = LOADED.get(player.m_20148_());
        if (servants == null) return;
        for (HadesServantEntity servant : servants) {
            if (!servant.m_213877_()) servant.recordJudgmentSouls(amount);
        }
    }

    @SubscribeEvent public static void leave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof HadesServantEntity servant) untrack(servant, servant.getOwnerId());
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { LOADED.clear(); }

    private HadesJudgmentSouls() { }
}
