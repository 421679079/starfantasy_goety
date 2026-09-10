package com.starfantasy.goety.entity;

import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.client.particles.GatherFrostParticleOption;
import com.Polarice3.Goety.common.entities.projectiles.IceChunk;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.config.ApollyonConfig;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Scripted Frost Impact prop for pageant stage four. It uses Goety's native
 * translucent-purple rotating formation, rises from Y+4 to Y+6, and drops during
 * the final five warning ticks. The landed blue-ice obstacle is a separate entity.
 */
public final class ApollyonPageantIceChunkEntity extends IceChunk
        implements ApollyonPageantOwned {
    private static final int FORMATION_TICKS = 20;
    private static final int RISE_TICKS = 20;
    private static final EntityDataAccessor<Integer> FALL_START_TICK =
            SynchedEntityData.m_135353_(ApollyonPageantIceChunkEntity.class,
                    EntityDataSerializers.f_135028_);
    private static final String FALL_START_TAG = "PageantIceFallStart";
    private static final int FALL_TICKS = 5;
    private static final double SPAWN_HEIGHT = 4.0D;
    private static final double HOLD_HEIGHT = 6.0D;
    private static final String OWNER_TAG = "PageantOwner";
    private static final String TIMELINE_TAG = "PageantIceTimeline";
    private static final String GROUND_Y_TAG = "PageantIceGroundY";

    private UUID ownerUuid;
    private int timelineTick;
    private double groundY;
    private boolean groundReady;

    public ApollyonPageantIceChunkEntity(
            EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.hovering = 0;
        this.m_20242_(true);
    }

    public static ApollyonPageantIceChunkEntity spawn(
            ApollyonEntity owner, Vec3 groundPosition) {
        if (owner == null || groundPosition == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantIceChunkEntity chunk = new ApollyonPageantIceChunkEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_ICE_CHUNK.get(), owner.m_9236_());
        chunk.ownerUuid = owner.m_20148_();
        chunk.f_19804_.m_135381_(FALL_START_TICK, ApollyonConfig.hardMode() ? 75 : 115);
        chunk.setOwner(owner);
        chunk.groundY = groundPosition.f_82480_;
        chunk.groundReady = true;
        chunk.m_6034_(groundPosition.f_82479_,
                groundPosition.f_82480_ + SPAWN_HEIGHT, groundPosition.f_82481_);
        return owner.m_9236_().m_7967_(chunk) ? chunk : null;
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(FALL_START_TICK, 115);
    }

    private int fallStartTick() {
        return this.f_19804_.m_135370_(FALL_START_TICK);
    }

    @Override
    public void m_8119_() {
        // Avoid IceChunk's native targeting, damage and auto-discard timeline.
        this.m_6075_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (!this.groundReady) {
            this.groundY = this.m_20186_() - SPAWN_HEIGHT;
            this.groundReady = true;
        }
        this.hovering = Math.min(20,
                (this.timelineTick + 1) * 20 / FORMATION_TICKS);
        if (this.m_9236_().f_46443_) {
            // Run the same deterministic motion on both sides. Entity rendering
            // interpolates between the previous and current positions, including
            // the deliberately sharp five-tick fall.
            this.f_19854_ = this.m_20185_();
            this.f_19855_ = this.m_20186_();
            this.f_19856_ = this.m_20189_();
            this.m_6034_(this.m_20185_(),
                    this.scriptedY(this.timelineTick + 1), this.m_20189_());
        } else {
            this.tickServerEffects();
            double nextY = this.scriptedY(this.timelineTick + 1);
            this.m_6034_(this.m_20185_(), nextY, this.m_20189_());
        }
        ++this.timelineTick;
    }

    private double scriptedY(int age) {
        if (age <= FORMATION_TICKS) {
            return this.groundY + SPAWN_HEIGHT;
        }
        if (age < FORMATION_TICKS + RISE_TICKS) {
            double progress = (age - FORMATION_TICKS) / (double) RISE_TICKS;
            return this.groundY + SPAWN_HEIGHT
                    + (HOLD_HEIGHT - SPAWN_HEIGHT) * progress;
        }
        if (age < this.fallStartTick()) {
            return this.groundY + HOLD_HEIGHT;
        }
        double progress = Math.min(1.0D,
                (age - this.fallStartTick()) / (double) FALL_TICKS);
        return this.groundY + HOLD_HEIGHT * (1.0D - progress);
    }

    private void tickServerEffects() {
        if (!(this.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (this.timelineTick == 0) {
            this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_IDLE.get(), 2.0F, 1.0F);
        }
        if (this.timelineTick < FORMATION_TICKS) {
            ServerParticleUtil.outerCircleParticles(level,
                    new GatherFrostParticleOption(
                            this.m_20182_().m_82520_(0.0D, 1.0D, 0.0D)),
                    this, 8.0F);
        }
        if (this.timelineTick == this.fallStartTick()) {
            this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_DROP.get(), 2.0F, 1.0F);
        }
    }

    @Override
    public void m_6453_(double x, double y, double z, float yRot, float xRot,
                        int interpolationSteps, boolean teleport) {
        if (!this.m_9236_().f_46443_ || !this.groundReady) {
            super.m_6453_(x, y, z, yRot, xRot, interpolationSteps, teleport);
            return;
        }
        // Once the spawn position establishes groundY, client motion is driven by
        // scriptedY so network lerp cannot leave the fast fall visually trailing.
        this.m_146922_(yRot);
        this.m_146926_(xRot);
    }

    public Vec3 groundPosition() {
        return new Vec3(this.m_20185_(), this.groundY, this.m_20189_());
    }

    public void playDoubleImpact(ServerLevel level) {
        if (level == null) {
            return;
        }
        this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_HIT.get(), 2.0F, 1.0F);
        level.m_8767_(new BlockParticleOption(
                        ParticleTypes.f_123794_, Blocks.f_50354_.m_49966_()),
                this.m_20185_(), this.groundY + 1.5D, this.m_20189_(),
                512, 1.5D, 1.5D, 1.5D, 1.0D);
        level.m_8767_(new CircleExplodeParticleOption(
                        new ColorUtil(0xFFFFBF), 10.0F, 1),
                this.m_20185_(), this.groundY, this.m_20189_(),
                1, 0.0D, 0.0D, 0.0D, 0.5D);
    }

    @Override
    public boolean isStarting() {
        return this.timelineTick <= FORMATION_TICKS;
    }

    @Override
    public boolean m_5829_() {
        return false;
    }

    @Override
    public boolean m_6094_() {
        return false;
    }

    @Override
    public void m_7334_(Entity entity) {
        // The obstacle is immovable.
    }

    @Override
    public boolean m_6087_() {
        return false;
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.timelineTick = Math.max(0, tag.m_128451_(TIMELINE_TAG));
        this.f_19804_.m_135381_(FALL_START_TICK, tag.m_128441_(FALL_START_TAG)
                ? Math.max(FORMATION_TICKS + RISE_TICKS, tag.m_128451_(FALL_START_TAG))
                : (ApollyonConfig.hardMode() ? 75 : 115));
        this.groundY = tag.m_128459_(GROUND_Y_TAG);
        this.groundReady = true;
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128405_(TIMELINE_TAG, this.timelineTick);
        tag.m_128405_(FALL_START_TAG, this.fallStartTick());
        tag.m_128347_(GROUND_Y_TAG, this.groundY);
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }
}
