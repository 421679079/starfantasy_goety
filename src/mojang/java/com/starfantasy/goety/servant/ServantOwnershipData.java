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

/** Extends Goety's loaded-entity summon check with persistent ownership across chunk unloads. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ServantOwnershipData extends SavedData {
    private record Owner(UUID player, String type) { }
    private final Map<UUID, Owner> servants = new HashMap<>();
    private final Map<UUID, Integer> pendingPillars = new HashMap<>();
    private final java.util.Set<UUID> retiredPillars = new java.util.HashSet<>();

    public static void retirePillar(ServerLevel level, UUID pillar) {
        ServantOwnershipData data = get(level);
        if (data.retiredPillars.add(pillar)) data.setDirty();
    }

    public static boolean consumeRetiredPillar(ServerLevel level, UUID pillar) {
        ServantOwnershipData data = get(level);
        if (!data.retiredPillars.remove(pillar)) return false;
        data.setDirty();
        return true;
    }

    public static void creditPillar(ServerLevel level, UUID servant) {
        if (servant == null) return;
        ServantOwnershipData data = get(level);
        data.pendingPillars.merge(servant, 1, (a,b) -> Math.min(4,a+b));
        data.setDirty();
    }

    public static int claimPillars(ServerLevel level, UUID servant) {
        ServantOwnershipData data = get(level);
        Integer count = data.pendingPillars.remove(servant);
        if (count == null) return 0;
        data.setDirty();
        return count;
    }

    private static ServantOwnershipData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                ServantOwnershipData::load, ServantOwnershipData::new, "starfantasy_servant_owners");
    }

    private static String type(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static boolean limited(String type) {
        return type.equals("starfantasy_goety:hades_servant")
                || type.equals("starfantasy_goety:apollyon_servant")
                || type.equals("starfantasy_goety:apostle_servant");
    }

    // Goety checks count >= limit, so zero rejects a new summon even when the existing one is unloaded.
    public static int summonLimit(Entity sample, LivingEntity player) {
        int limit = type(sample).equals("starfantasy_goety:apostle_servant") ? 12 : 1;
        if (!canOwnAnother(type(sample), player)) return 0;
        return limit;
    }

    public static boolean canOwnAnother(String type, LivingEntity player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) return true;
        int limit = type.equals("starfantasy_goety:apostle_servant") ? 12 : 1;
        Owner owner = new Owner(player.getUUID(), type);
        return get(level).servants.values().stream().filter(owner::equals).count() < limit;
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
            if (data.pendingPillars.remove(entity.getUUID()) != null) data.setDirty();
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
        CompoundTag credits = tag.getCompound("PendingPillars");
        for (String id : credits.getAllKeys()) {
            try { data.pendingPillars.put(UUID.fromString(id), Math.min(4,Math.max(0,credits.getInt(id)))); }
            catch (IllegalArgumentException ignored) { }
        }
        for (String id : tag.getCompound("RetiredPillars").getAllKeys()) {
            try { data.retiredPillars.add(UUID.fromString(id)); }
            catch (IllegalArgumentException ignored) { }
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
        CompoundTag credits = new CompoundTag();
        pendingPillars.forEach((id,count) -> credits.putInt(id.toString(),count));
        tag.put("PendingPillars",credits);
        CompoundTag retired = new CompoundTag();
        retiredPillars.forEach(id -> retired.putInt(id.toString(),1));
        tag.put("RetiredPillars",retired);
        return tag;
    }
}
