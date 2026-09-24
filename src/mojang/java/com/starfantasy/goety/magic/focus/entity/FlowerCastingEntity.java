package com.starfantasy.goety.magic.focus.entity;

import com.starfantasy.goety.magic.focus.BattleFocusContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/** One cosmetic entity per live cast. No world scans, damage, or saved visual entities. */
public final class FlowerCastingEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final String CAST_TAG = "starfantasy_goety:flower_cast_visual";
    private static final EntityDataAccessor<Integer> CASTER = SynchedEntityData.defineId(FlowerCastingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(FlowerCastingEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(FlowerCastingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> END = SynchedEntityData.defineId(FlowerCastingEntity.class, EntityDataSerializers.LONG);
    public static final int FADE_TICKS = 6;
    private LivingEntity caster;
    private long lastRefresh;
    private boolean restored;

    public FlowerCastingEntity(EntityType<? extends FlowerCastingEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    private static FlowerCastingEntity current(ServerLevel level, LivingEntity caster) {
        CompoundTag tag = caster.getPersistentData();
        if (!tag.hasUUID(CAST_TAG)) return null;
        Entity entity = level.getEntity(tag.getUUID(CAST_TAG));
        return entity instanceof FlowerCastingEntity visual && visual.caster == caster && !visual.isRemoved()
                ? visual : null;
    }

    public static void refresh(ServerLevel level, LivingEntity caster, int ticks, int duration) {
        if (!caster.isAlive() || duration <= 1) return;
        FlowerCastingEntity visual = current(level, caster);
        if (visual == null || visual.entityData.get(END) >= 0) {
            visual = BattleFocusContent.FLOWER_CASTING.get().create(level);
            if (visual == null) return;
            visual.caster = caster;
            visual.entityData.set(CASTER, caster.getId());
            visual.setPos(anchor(caster, 1));
            visual.entityData.set(START, level.getGameTime() - ticks);
            visual.entityData.set(DURATION, duration);
            if (!level.addFreshEntity(visual)) return;
            caster.getPersistentData().putUUID(CAST_TAG, visual.getUUID());
        }
        visual.lastRefresh = level.getGameTime();
        // Actual wand clock, including cast-speed modifiers; never use the config duration.
        visual.entityData.set(START, level.getGameTime() - ticks);
        visual.entityData.set(DURATION, duration);
    }

    public static void finish(ServerLevel level, LivingEntity caster) {
        FlowerCastingEntity visual = current(level, caster);
        if (visual != null) visual.beginFade();
    }

    private void beginFade() {
        if (entityData.get(END) < 0) entityData.set(END, level().getGameTime());
        if (caster != null && caster.getPersistentData().hasUUID(CAST_TAG)
                && getUUID().equals(caster.getPersistentData().getUUID(CAST_TAG))) {
            caster.getPersistentData().remove(CAST_TAG);
        }
    }

    public LivingEntity caster() {
        Entity owner = level().getEntity(entityData.get(CASTER));
        return owner instanceof LivingEntity living ? living : null;
    }

    public static Vec3 anchor(LivingEntity caster, float partial) {
        // Near the aim line, not lifted by ribbon bounds. Eight blocks of clearance lets
        // the full-size effect unfold in front of the first-person camera, including when aiming up.
        return caster.getEyePosition(partial).add(caster.getViewVector(partial).scale(8)).add(0, .35, 0);
    }

    public float age(float partial) {
        long end = entityData.get(END);
        double now = end >= 0 ? end : level().getGameTime() + partial;
        return (float) Math.max(0, now - entityData.get(START));
    }

    public float progress(float partial) {
        return Mth.clamp(age(partial) / Math.max(1, entityData.get(DURATION)), 0, 1);
    }

    public float opacity(float partial) {
        long end = entityData.get(END);
        return end < 0 ? 1 : Mth.clamp(1 - (level().getGameTime() - end + partial) / FADE_TICKS, 0, 1);
    }

    @Override public void tick() {
        super.tick();
        if (restored) { discard(); return; }
        if (!level().isClientSide) {
            if (caster == null || caster.isRemoved() || !caster.isAlive() || caster.level() != level()) {
                beginFade(); discard(); return;
            }
            if (!caster.isUsingItem() || level().getGameTime() - lastRefresh > 2) beginFade();
            setPos(anchor(caster, 1));
            if (entityData.get(END) >= 0 && level().getGameTime() - entityData.get(END) >= FADE_TICKS) discard();
        }
    }

    @Override protected void defineSynchedData() {
        entityData.define(CASTER, -1); entityData.define(START, 0L);
        entityData.define(DURATION, 1); entityData.define(END, -1L);
    }
    @Override public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(entityData.get(CASTER)); buffer.writeLong(entityData.get(START));
        buffer.writeInt(entityData.get(DURATION)); buffer.writeLong(entityData.get(END));
    }
    @Override public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(CASTER, buffer.readInt()); entityData.set(START, buffer.readLong());
        entityData.set(DURATION, buffer.readInt()); entityData.set(END, buffer.readLong());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { restored = true; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
