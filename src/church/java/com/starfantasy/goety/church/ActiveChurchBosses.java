package com.starfantasy.goety.church;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Only active encounters are persisted; chunk unloads do not unlock an altar. */
@Mod.EventBusSubscriber(modid = ChurchContent.MODID)
public final class ActiveChurchBosses extends SavedData {
    private final Map<UUID, Long> active = new HashMap<>();
    public static ActiveChurchBosses get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ActiveChurchBosses::load, ActiveChurchBosses::new, "starfantasy_church_bosses");
    }
    public boolean contains(BlockPos pos) { return active.containsValue(pos.asLong()); }
    public void add(UUID boss, BlockPos pos) { active.put(boss, pos.asLong()); setDirty(); }
    public void remove(UUID boss) { if (active.remove(boss) != null) setDirty(); }
    private static ActiveChurchBosses load(CompoundTag tag) {
        ActiveChurchBosses data = new ActiveChurchBosses();
        ListTag list = tag.getList("Active", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Boss")) data.active.put(entry.getUUID("Boss"), entry.getLong("Altar"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        active.forEach((uuid, pos) -> { CompoundTag entry = new CompoundTag(); entry.putUUID("Boss", uuid); entry.putLong("Altar", pos); list.add(entry); });
        tag.put("Active", list); return tag;
    }
    @SubscribeEvent public static void removed(EntityLeaveLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity().getRemovalReason() != null
                && event.getEntity().getRemovalReason().shouldDestroy()
                && event.getEntity() instanceof com.starfantasy.goety.entity.ApollyonEntity)
            get(level).remove(event.getEntity().getUUID());
    }
}
