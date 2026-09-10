package com.starfantasy.goety.church;

import java.util.*;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonPageantSummonEntity;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

public final class UnderworldAltarEntity extends BlockEntity {
    public static final TagKey<Item> HALOS = TagKey.create(Registries.ITEM, new ResourceLocation(ChurchContent.MODID, "halo_of_apostle"));
    private final Map<UUID, Long> confirmations = new HashMap<>();
    private int remaining;
    private long startedAt = Long.MIN_VALUE;
    private UUID summoner;
    private UUID effectId;
    public UnderworldAltarEntity(BlockPos pos, BlockState state) { super(ChurchContent.ALTAR_ENTITY.get(), pos, state); }
    public void use(Player player) {
        if (!(level instanceof ServerLevel server) || player.isSpectator()) return;
        if (server.getDifficulty() == Difficulty.PEACEFUL) { tell(player, "peaceful"); return; }
        if (nearbyBoss(server) || ActiveChurchBosses.get(server).contains(worldPosition)) { rejectDuplicate(player); return; }
        if (remaining > 0) { tell(player, "busy"); return; }
        long now = server.getGameTime();
        confirmations.entrySet().removeIf(e -> now < e.getValue() || now - e.getValue() >= 100);
        Long first = confirmations.get(player.getUUID());
        if (first == null) {
            if (confirmations.size() >= 64) confirmations.clear();
            confirmations.put(player.getUUID(), now); tell(player, "confirm"); return;
        }
        // Off-hand processing and duplicate packets in the first tick cannot confirm.
        if (now == first) return;
        Vec3 home = Vec3.atBottomCenterOf(worldPosition.above());
        if (!server.noCollision(new AABB(home.x - .5, home.y, home.z - .5, home.x + .5, home.y + 3, home.z + .5))) {
            tell(player, "blocked"); return;
        }
        confirmations.clear(); remaining = 100; startedAt = now; summoner = player.getUUID();
        ApollyonPageantSummonEntity effect = new ApollyonPageantSummonEntity(ApollyonEntityRegistry.APOLLYON_PAGEANT_SUMMON.get(), server);
        CompoundTag tag = new CompoundTag(); effect.saveWithoutId(tag); tag.putInt("Duration", 100); effect.load(tag);
        effect.setPos(home); if (server.addFreshEntity(effect)) effectId = effect.getUUID();
        setChanged();
    }
    private boolean nearbyBoss(ServerLevel server) {
        return !server.getEntitiesOfClass(ApollyonEntity.class, new AABB(worldPosition).inflate(96), e -> e.isAlive()).isEmpty();
    }
    public static void tick(Level world, BlockPos pos, BlockState state, UnderworldAltarEntity altar) {
        if (!(world instanceof ServerLevel level) || altar.remaining <= 0) return;
        if (level.getGameTime() == altar.startedAt) return;
        altar.remaining--; altar.setChanged();
        if (altar.remaining != 0) return;
        if (altar.effectId != null) { var effect = level.getEntity(altar.effectId); if (effect != null) effect.discard(); altar.effectId = null; }
        if (altar.nearbyBoss(level) || ActiveChurchBosses.get(level).contains(pos)) {
            Player player = altar.summoner == null ? null : level.getPlayerByUUID(altar.summoner);
            if (player != null) rejectDuplicate(player);
            altar.summoner = null;
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) { altar.summoner = null; return; }
        Vec3 home = Vec3.atBottomCenterOf(pos.above());
        ApollyonEntity boss = ApollyonEntityRegistry.APOLLYON.get().create(level);
        if (boss == null) return;
        boss.moveTo(home.x, home.y, home.z, 0, 0);
        // finalizeSpawn initializes Apollyon's existing home system at this exact position.
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos.above()), MobSpawnType.TRIGGERED, null, null);
        boss.setPersistenceRequired();
        if (altar.summoner != null) {
            Player player = level.getPlayerByUUID(altar.summoner);
            if (player != null && !player.isCreative() && !player.isSpectator()) boss.setTarget(player);
        }
        if (level.addFreshEntity(boss)) {
            ActiveChurchBosses.get(level).add(boss.getUUID(), pos);
            playArrival(level, home);
        }
        altar.summoner = null; altar.setChanged();
    }
    private static void playArrival(ServerLevel level, Vec3 position) {
        level.playSound(null, BlockPos.containing(position), ApollyonSoundRegistry.SUMMON_APOSTLE.get(), SoundSource.HOSTILE, 2.0F, 1.0F);
        // Match the pageant apostles' spherical arrival smoke (1,000 large smoke particles).
        Vec3 center = position.add(0, 1, 0);
        double phi = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int i = 0; i < 1000; i++) {
            double y = 1.0D - i / 999.0D * 2.0D;
            double radius = Math.sqrt(1.0D - y * y);
            double theta = phi * i;
            level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 0,
                    Math.cos(theta) * radius, y, Math.sin(theta) * radius, 1.0D);
        }
    }
    private static void rejectDuplicate(Player player) {
        tell(player, "duplicate", ApollyonEntityRegistry.APOLLYON.get().getDescription());
    }
    private static void tell(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.starfantasy_goety.altar." + key, args).withStyle(ChatFormatting.RED), true);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); remaining = Math.max(0, Math.min(100, tag.getInt("RitualTicks")));
        startedAt = tag.contains("RitualStartedAt") ? tag.getLong("RitualStartedAt") : Long.MIN_VALUE;
        summoner = tag.hasUUID("Summoner") ? tag.getUUID("Summoner") : null;
        effectId = tag.hasUUID("SummonEffect") ? tag.getUUID("SummonEffect") : null;
        confirmations.clear();
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.putInt("RitualTicks", remaining);
        tag.putLong("RitualStartedAt", startedAt);
        if (summoner != null) tag.putUUID("Summoner", summoner);
        if (effectId != null) tag.putUUID("SummonEffect", effectId);
    }
}
