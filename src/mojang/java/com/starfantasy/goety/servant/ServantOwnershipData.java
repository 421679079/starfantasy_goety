package com.starfantasy.goety.servant;

import com.Polarice3.Goety.common.entities.neutral.Owned;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Extends Goety's loaded-entity summon check with persistent ownership of these two servants. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ServantOwnershipData extends SavedData {
    private record Owner(UUID player, String type) { }
    private final Map<UUID, Owner> servants = new HashMap<>();

    private static ServantOwnershipData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                ServantOwnershipData::load, ServantOwnershipData::new, "starfantasy_servant_owners");
    }

    private static String type(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static boolean limited(String type) {
        return type.equals("starfantasy_goety:hades_servant")
                || type.equals("starfantasy_goety:apollyon_servant");
    }

    // Goety checks count >= limit, so zero rejects a new summon even when the existing one is unloaded.
    public static int summonLimit(Entity sample, LivingEntity player) {
        if (player != null && player.level() instanceof ServerLevel level
                && get(level).servants.containsValue(new Owner(player.getUUID(), type(sample)))) return 0;
        return 1;
    }

    public static boolean contains(ServerLevel level, UUID servant) {
        return get(level).servants.containsKey(servant);
    }

    /** Called only after a successful world join or a live servant's ownership change. */
    public static void track(Owned servant) {
        if (!servant.isAddedToWorld() || !(servant.level() instanceof ServerLevel level)) return;
        String type = type(servant);
        if (!limited(type)) return;
        ServantOwnershipData data = get(level);
        UUID owner = servant.getOwnerId();
        if (owner == null) {
            if (data.servants.remove(servant.getUUID()) != null) data.setDirty();
        } else {
            Owner entry = new Owner(owner, type);
            if (!entry.equals(data.servants.put(servant.getUUID(), entry))) data.setDirty();
        }
    }

    @SubscribeEvent public static void removed(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level && limited(type(entity))
                && entity.getRemovalReason() != null && entity.getRemovalReason().shouldDestroy()) {
            ServantOwnershipData data = get(level);
            if (data.servants.remove(entity.getUUID()) != null) data.setDirty();
        }
    }

    private static ServantOwnershipData load(CompoundTag tag) {
        ServantOwnershipData data = new ServantOwnershipData();
        ListTag list = tag.getList("Servants", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String type = entry.getString("Type");
            if (limited(type) && entry.hasUUID("Servant") && entry.hasUUID("Owner"))
                data.servants.put(entry.getUUID("Servant"), new Owner(entry.getUUID("Owner"), type));
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        servants.forEach((id, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Servant", id);
            entry.putUUID("Owner", owner.player());
            entry.putString("Type", owner.type());
            list.add(entry);
        });
        tag.put("Servants", list);
        return tag;
    }
}
