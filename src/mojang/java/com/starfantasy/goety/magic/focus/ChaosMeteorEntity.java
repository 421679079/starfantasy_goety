package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.SphereExplodeParticleOption;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.magic.focus.client.ChaosMeteorTrapEffects;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** One player's meteor, including the traps left behind after the rock stops. */
public final class ChaosMeteorEntity extends Entity {
    public static final int TRAIL_TICKS = 18;
    private static final int LAND_TICK = 20;
    private static final int EXPLOSION_INTERVAL = 20;
    public static final int TRAP_HIT_TICK = 21;
    public static final int TRAP_TICKS = 30;
    private static final double EXPLOSION_RADIUS = 3;
    private static final double TRAP_RADIUS = 4.5;
    private static final EntityDataAccessor<Long> BIRTH = SynchedEntityData.defineId(ChaosMeteorEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> STOPPED = SynchedEntityData.defineId(ChaosMeteorEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<CompoundTag> TRAP_VISUALS =
            SynchedEntityData.defineId(ChaosMeteorEntity.class, EntityDataSerializers.COMPOUND_TAG);

    private UUID ownerId;
    private Vec3 start, landing, direction;
    private int lifetime, burningTicks, lastAge;
    private float damage;
    private double radiusScale;
    private boolean leavesTraps;
    private final List<Trap> traps = new ArrayList<>(5);
    private final Deque<TrailPoint> trail = new ArrayDeque<>();
    private List<TrapVisual> trapVisuals = List.of();
    private Vec3 clientDestination;
    private int interpolationTicks;

    private record Trap(Vec3 center, long born) {}
    public record TrailPoint(Vec3 position, long tick) {}
    public record TrapVisual(Vec3 center, long born, float scale) {}

    public ChaosMeteorEntity(EntityType<? extends ChaosMeteorEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    static void spawn(ServerLevel level, LivingEntity caster, Vec3 landing, float yaw, int lifetime,
                      float damage, double radiusScale, int burningTicks, boolean leavesTraps, boolean playSound) {
        if (!caster.isAlive()) return;
        Vec3 direction = Vec3.directionFromRotation(0, yaw);
        Vec3 start = landing.subtract(direction.scale(5)).add(0, 10, 0);
        if (!level.hasChunkAt(BlockPos.containing(start))) return;
        ChaosMeteorEntity meteor = BattleFocusContent.CHAOS_METEOR.get().create(level);
        if (meteor == null) return;
        meteor.ownerId = caster.getUUID();
        meteor.landing = landing;
        meteor.start = start;
        meteor.direction = direction;
        meteor.lifetime = lifetime;
        meteor.damage = damage;
        meteor.radiusScale = radiusScale;
        meteor.burningTicks = burningTicks;
        meteor.leavesTraps = leavesTraps;
        meteor.moveTo(start.x, start.y, start.z, yaw, 0);
        meteor.entityData.set(BIRTH, level.getGameTime());
        if (level.addFreshEntity(meteor) && playSound) {
            level.playSound(null, start.x, start.y, start.z, BattleFocusContent.CHAOS_METEOR_CAST.get(), SoundSource.PLAYERS, 2, 1);
        }
    }

    @Override protected void defineSynchedData() {
        entityData.define(BIRTH, -1L);
        entityData.define(STOPPED, -1L);
        entityData.define(TRAP_VISUALS, new CompoundTag());
    }

    public long visualBirth() { return entityData.get(BIRTH); }
    public long stoppedAt() { return entityData.get(STOPPED); }
    public Iterable<TrailPoint> trail() { return trail; }
    public List<TrapVisual> trapVisuals() { return trapVisuals; }

    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (!level().isClientSide || !TRAP_VISUALS.equals(key)) return;
        CompoundTag data = entityData.get(TRAP_VISUALS);
        ListTag entries = data.getList("Traps", Tag.TAG_COMPOUND);
        List<TrapVisual> visuals = new ArrayList<>(entries.size());
        float scale = data.getFloat("Scale");
        for (Tag entry : entries) {
            CompoundTag trap = (CompoundTag) entry;
            Vec3 center = new Vec3(trap.getDouble("X"), trap.getDouble("Y"), trap.getDouble("Z"));
            visuals.add(new TrapVisual(center, trap.getLong("Born"), scale));
        }
        trapVisuals = List.copyOf(visuals);
    }

    @Override public void tick() {
        super.tick();
        long now = level().getGameTime();
        if (level().isClientSide) {
            tickClient(now);
            return;
        }
        ServerLevel level = (ServerLevel) level();
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        if (!(owner instanceof LivingEntity caster) || !caster.isAlive() || caster.isRemoved()
                || caster.isSpectator() || !level.hasChunkAt(blockPosition())) {
            discard();
            return;
        }
        long age = now - visualBirth();
        if (age <= lastAge) return;
        // An unloaded or non-ticking chunk must not replay a backlog of attacks.
        if (age > lastAge + 1) {
            discard();
            return;
        }
        lastAge = (int) age;
        if (stoppedAt() < 0) {
            Vec3 next = age >= lifetime ? null : age <= LAND_TICK
                    ? start.lerp(landing, age / (double) LAND_TICK)
                    : ChaosMeteorPath.roll(level, caster, position(), direction);
            if (next == null || !level.hasChunkAt(BlockPos.containing(next))) {
                entityData.set(STOPPED, now);
            } else {
                setPos(next);
                if (age >= LAND_TICK && (age - LAND_TICK) % EXPLOSION_INTERVAL == 0) {
                    explode(level, caster, now);
                }
            }
        }
        if (isRemoved() || !caster.isAlive() || caster.isRemoved() || caster.level() != level) {
            discard();
            return;
        }
        tickTraps(level, caster, now);
        if (stoppedAt() >= 0 && now - stoppedAt() >= TRAIL_TICKS && traps.isEmpty()) discard();
    }

    private void explode(ServerLevel level, LivingEntity caster, long now) {
        Vec3 center = position();
        float visualSize = (float) (4.5 * radiusScale);
        ColorUtil fire = new ColorUtil(16746757);
        level.sendParticles(new CircleExplodeParticleOption(fire, visualSize, 1), center.x, center.y, center.z, 0, 0, 0, 0, 0);
        level.sendParticles(new SphereExplodeParticleOption(fire, visualSize, 1), center.x, center.y, center.z, 0, 0, 0, 0, 0);
        ColorUtil embers = new ColorUtil(11312015);
        for (int i = 0; i < 8; i++) {
            level.sendParticles(ModParticleTypes.BIG_CULT_SPELL.get(),
                    center.x + random.nextDouble() - .5, center.y + random.nextDouble(), center.z + random.nextDouble() - .5,
                    0, embers.red, embers.green, embers.blue, 1);
        }
        level.sendParticles(ModParticleTypes.BIG_FIRE_GROUND.get(), center.x, center.y, center.z, 8, .25, 0, .25, 0);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3,
                (.8F + random.nextFloat() * .4F) * .7F);
        hit(level, caster, center, EXPLOSION_RADIUS * radiusScale);
        if (leavesTraps && !isRemoved()) {
            traps.add(new Trap(center, now));
            publishTrapVisuals();
        }
    }

    private void tickTraps(ServerLevel level, LivingEntity caster, long now) {
        double radius = TRAP_RADIUS * radiusScale;
        boolean changed = false;
        for (var iterator = traps.iterator(); iterator.hasNext();) {
            Trap trap = iterator.next();
            Vec3 center = trap.center();
            long age = now - trap.born();
            if (age >= TRAP_TICKS || !level.hasChunkAt(BlockPos.containing(center))) {
                iterator.remove();
                changed = true;
                continue;
            }
            if (age == TRAP_HIT_TICK) {
                level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1, .5F);
                hit(level, caster, center, radius);
                if (isRemoved() || !caster.isAlive() || caster.isRemoved() || caster.level() != level) return;
            }
        }
        if (changed) publishTrapVisuals();
    }

    private void publishTrapVisuals() {
        ListTag entries = new ListTag();
        for (Trap trap : traps) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("X", trap.center().x);
            entry.putDouble("Y", trap.center().y);
            entry.putDouble("Z", trap.center().z);
            entry.putLong("Born", trap.born());
            entries.add(entry);
        }
        CompoundTag snapshot = new CompoundTag();
        snapshot.putFloat("Scale", (float) (2.25 * radiusScale));
        snapshot.put("Traps", entries);
        entityData.set(TRAP_VISUALS, snapshot);
    }

    private void hit(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        if (damage <= 0) return;
        var source = ModDamageSource.hellfire(this, caster);
        for (LivingEntity target : BattleFocusCombat.sphereTargets(level, caster, center, radius)) {
            if (isRemoved() || !caster.isAlive() || caster.isRemoved() || caster.level() != level) return;
            if (target.isAlive() && BattleFocusCombat.hurtSegment(target, source, damage)
                    && burningTicks > 0 && target.isAlive() && !target.fireImmune()) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), burningTicks));
            }
        }
    }

    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        clientDestination = new Vec3(x, y, z);
        interpolationTicks = teleport ? 1 : 3;
    }

    private void tickClient(long now) {
        if (interpolationTicks > 0) {
            setPos(position().lerp(clientDestination, 1.0 / interpolationTicks--));
        }
        if (visualBirth() >= 0 && (stoppedAt() < 0 || now < stoppedAt())) {
            trail.addLast(new TrailPoint(position(), now));
        }
        while (!trail.isEmpty() && trail.getFirst().tick() <= now - TRAIL_TICKS) trail.removeFirst();
        if (!trapVisuals.isEmpty()) ChaosMeteorTrapEffects.emit(this, now);
    }

    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().inflate(9, 12, 9); }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 256 * 256; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
