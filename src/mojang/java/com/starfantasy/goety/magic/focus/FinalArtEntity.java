package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.utils.MobUtil;
import com.starfantasy.library.vfx.api.FinalCurtainVisual;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

/** Doll-sized Final Curtain, driven by one server-owned clock, including its terminal explosion. */
public final class FinalArtEntity extends Entity implements FinalCurtainVisual, IEntityAdditionalSpawnData {
    public static final int ACTIVE_TICKS = 300;
    public static final int EXPLOSION_TICKS = 10;
    public static final int PULL_INTERVAL = 60, PULL_TICKS = 10;
    public static final double PULL_MULTIPLIER = 3;
    public static final int CORE_INTERVAL = 10;
    public static final double DAMAGE_RADIUS = 12;
    private static final EntityDataAccessor<Long> BIRTH = SynchedEntityData.defineId(FinalArtEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> SEED = SynchedEntityData.defineId(FinalArtEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(FinalArtEntity.class, EntityDataSerializers.INT);
    private UUID ownerUuid;
    private float damage, coreDamage;
    private double pullRadius = 16;
    private int finalHits;
    private boolean restored, exploded;

    public FinalArtEntity(EntityType<? extends FinalArtEntity> type, Level level) {
        super(type, level); noPhysics = true; setNoGravity(true);
    }
    public static void spawn(ServerLevel level, LivingEntity caster, float coreDamage, float damage,
                             int finalHits, int duration, double radius, double range) {
        if (!caster.isAlive()) return;
        Vec3 anchor = findAnchor(level, caster, range);
        if (!level.hasChunkAt(BlockPos.containing(anchor))) return;
        FinalArtEntity effect = BattleFocusContent.FINAL_ART_EFFECT.get().create(level);
        if (effect == null) return;
        effect.ownerUuid = caster.getUUID(); effect.damage = damage; effect.finalHits = finalHits;
        effect.coreDamage = coreDamage; effect.pullRadius = radius;
        effect.entityData.set(DURATION, Math.max(1, duration));
        effect.setPos(anchor); effect.setYRot(caster.getYRot()); effect.yRotO = caster.getYRot();
        effect.entityData.set(BIRTH, level.getGameTime());
        effect.entityData.set(SEED, level.random.nextFloat() * 1000F);
        if (!level.addFreshEntity(effect)) return;
        for (var player : level.players()) {
            if (player.isAlive() && !player.isSpectator()
                    && (player == caster || player.position().distanceToSqr(anchor) <= 30 * 30)) {
                player.playNotifySound(BattleFocusContent.FINAL_ART_START.get(), SoundSource.PLAYERS, 1, 1);
            }
        }
    }
    private static Vec3 findAnchor(ServerLevel level, LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition(), end = eye.add(caster.getLookAngle().scale(range));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP
                && level.getBlockState(hit.getBlockPos()).isFaceSturdy(level, hit.getBlockPos(), Direction.UP)) {
            return hit.getLocation().add(0, .01, 0);
        }
        Vec3 probe = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
        int x = Mth.floor(probe.x), z = Mth.floor(probe.z);
        if (!level.hasChunkAt(BlockPos.containing(probe))) return end.add(0, -5, 0);
        for (int y = Math.min(level.getMaxBuildHeight() - 1, Mth.floor(probe.y)); y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            var state = level.getBlockState(pos);
            var shape = state.getCollisionShape(level, pos);
            if (shape.isEmpty() || !state.isFaceSturdy(level, pos, Direction.UP)) continue;
            double surface = y + shape.max(Direction.Axis.Y);
            if (surface <= probe.y + 1.0E-4) return new Vec3(probe.x, surface + .01, probe.z);
        }
        return end.add(0, -5, 0);
    }
    @Override protected void defineSynchedData() {
        entityData.define(BIRTH, -1L); entityData.define(SEED, 0F);
        entityData.define(DURATION, ACTIVE_TICKS);
    }
    public float visualAge(float partial) {
        long birth = entityData.get(BIRTH);
        return birth < 0 ? 0 : Math.max(0, level().getGameTime() - birth + partial);
    }
    public int finalTick() { return SPAWN_TOTAL_TICKS + entityData.get(DURATION); }
    public int pullAge(int age) {
        return age < SPAWN_TOTAL_TICKS || age >= finalTick() ? -1 : (age - SPAWN_TOTAL_TICKS) % PULL_INTERVAL;
    }
    @Override public void tick() {
        super.tick();
        tickCount = (int) visualAge(0);
        if (!(level() instanceof ServerLevel server)) return;
        if (restored || tickCount >= finalTick() + EXPLOSION_TICKS) { discard(); return; }
        Entity owner = ownerUuid == null ? null : server.getEntity(ownerUuid);
        if (!(owner instanceof LivingEntity caster) || !caster.isAlive() || caster.isRemoved()
                || caster.isSpectator() || caster.level() != server) { discard(); return; }
        if (tickCount >= finalTick()) {
            if (!exploded) {
                exploded = true;
                playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 5, .5F);
                playSound(SoundEvents.GENERIC_EXPLODE, 5, .5F);
                damageTargets(server, caster, damage, finalHits);
            }
            return;
        }
        int pullAge = pullAge(tickCount);
        if (pullAge < 0) return;
        if ((tickCount - SPAWN_TOTAL_TICKS) % CORE_INTERVAL == 0) {
            damageTargets(server, caster, coreDamage, 1);
        }
        if (pullAge == 0) {
            playSound(BattleFocusContent.FINAL_ART_PULL.get(), 2, 1);
            damageTargets(server, caster, damage, 1);
        }
        for (LivingEntity target : BattleFocusCombat.targets(server, caster, position(), pullRadius)) {
            if (FinalArtTargeting.allows(caster, target)) pullTarget(target, pullAge);
        }
        for (Entity collectible : server.getEntities(this,
                new AABB(position(), position()).inflate(pullRadius),
                candidate -> (candidate instanceof ItemEntity || candidate instanceof ExperienceOrb)
                        && candidate.isAlive() && candidate.position().distanceToSqr(position()) <= pullRadius * pullRadius)) {
            pullTarget(collectible, pullAge);
        }
    }
    private void damageTargets(ServerLevel level, LivingEntity caster, float amount, int hits) {
        BattleFocusCombat.damageFiltered(level, caster, position(), DAMAGE_RADIUS,
                damageSources().indirectMagic(this, caster), amount, hits, target -> FinalArtTargeting.allows(caster, target));
    }
    private void pullTarget(Entity target, int pullAge) {
        // Both phases use Rupture's pull; a pulse only triples its force for half a second.
        Vec3 away = target.position().subtract(position());
        double strength = Math.max(0D, 1D - away.length() / pullRadius) * .25D;
        if (pullAge >= 0 && pullAge < PULL_TICKS) strength *= PULL_MULTIPLIER;
        Vec3 force = away.normalize().scale(strength);
        MobUtil.pull(target, force.x, force.y, force.z, .5D);
        target.hurtMarked = true;
    }
    @Override public float getRiftScale() { return .5F; }
    @Override public float getDepthScale() { return DEFAULT_DEPTH_SCALE; }
    @Override public float getSeed() { return entityData.get(SEED); }
    @Override public float getLifeProgress(float partial) { return 0; }
    @Override public float pullShockwaveAge(float partial) {
        float activeAge = visualAge(partial) - SPAWN_TOTAL_TICKS;
        return activeAge < 0 ? Float.POSITIVE_INFINITY : activeAge % PULL_INTERVAL;
    }
    @Override public AABB getBoundingBoxForCulling() { return new AABB(position(), position()).inflate(33); }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 256 * 256; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeLong(entityData.get(BIRTH)); buffer.writeFloat(getSeed()); buffer.writeFloat(getYRot());
        buffer.writeInt(entityData.get(DURATION));
    }
    @Override public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(BIRTH, buffer.readLong()); entityData.set(SEED, buffer.readFloat());
        setYRot(buffer.readFloat()); yRotO = getYRot();
        entityData.set(DURATION, buffer.readInt()); tickCount = (int) visualAge(0);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { restored = true; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
