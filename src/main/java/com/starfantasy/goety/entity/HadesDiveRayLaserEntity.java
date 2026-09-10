package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Faithful, horizontal adaptation of Bosses' Denia laser-column visual. */
public final class HadesDiveRayLaserEntity extends Entity {
    private static final String OWNER_TAG = "Owner";
    private static final EntityDataAccessor<Float> DIRECTION_X =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> DIRECTION_Y =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> DIRECTION_Z =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> WIDTH =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> LENGTH =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> ALPHA =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> SEED =
            SynchedEntityData.m_135353_(HadesDiveRayLaserEntity.class,
                    EntityDataSerializers.f_135028_);

    public static final int BEAM_VISIBLE_TICKS = 20;
    public static final int EXTEND_TICKS = 10;
    public static final int RING_LIFETIME_TICKS = 20;
    public static final int DEFAULT_DURATION_TICKS =
            EXTEND_TICKS + RING_LIFETIME_TICKS;
    public static final float DEFAULT_ALPHA = 0.36F;

    private UUID ownerUuid;

    public HadesDiveRayLaserEntity(
            EntityType<? extends HadesDiveRayLaserEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static HadesDiveRayLaserEntity spawn(
            HadesEntity owner, Vec3 origin, Vec3 forward,
            double width, double length) {
        if (owner == null || origin == null || forward == null
                || !owner.m_6084_() || owner.m_213877_()
                || owner.m_9236_().f_46443_) {
            return null;
        }
        HadesDiveRayLaserEntity laser = new HadesDiveRayLaserEntity(
                ApollyonEntityRegistry.HADES_DIVE_RAY_LASER.get(),
                owner.m_9236_());
        laser.ownerUuid = owner.m_20148_();
        laser.f_19804_.m_135381_(WIDTH,
                (float) Math.max(0.1D, Math.min(64.0D, width)));
        laser.f_19804_.m_135381_(LENGTH,
                (float) Math.max(0.1D, Math.min(256.0D, length)));
        laser.f_19804_.m_135381_(ALPHA, DEFAULT_ALPHA);
        laser.f_19804_.m_135381_(DURATION, DEFAULT_DURATION_TICKS);
        laser.f_19804_.m_135381_(SEED,
                owner.m_9236_().m_213780_().m_188503_(Integer.MAX_VALUE));
        laser.setLaserForward(forward);
        laser.m_6034_(origin.f_82479_, origin.f_82480_, origin.f_82481_);
        return owner.m_9236_().m_7967_(laser) ? laser : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(DIRECTION_X, 0.0F);
        this.f_19804_.m_135372_(DIRECTION_Y, 0.0F);
        this.f_19804_.m_135372_(DIRECTION_Z, 1.0F);
        this.f_19804_.m_135372_(WIDTH, 6.0F);
        this.f_19804_.m_135372_(LENGTH, 40.0F);
        this.f_19804_.m_135372_(ALPHA, DEFAULT_ALPHA);
        this.f_19804_.m_135372_(DURATION, DEFAULT_DURATION_TICKS);
        this.f_19804_.m_135372_(SEED, 0);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (!this.m_9236_().f_46443_ && !this.hasLiveOwner()) {
            this.m_146870_();
            return;
        }
        if (!this.m_9236_().f_46443_ && this.f_19797_ >= this.durationTicks()) {
            this.m_146870_();
        }
    }

    @Override
    public boolean m_6094_() {
        return false;
    }

    @Override
    public boolean m_5829_() {
        return false;
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 16384.0D;
    }

    @Override
    public AABB m_6921_() {
        return super.m_6921_().m_82377_(128.0D, 128.0D, 128.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(DIRECTION_X, tag.m_128457_("DirX"));
        this.f_19804_.m_135381_(DIRECTION_Y, tag.m_128457_("DirY"));
        this.f_19804_.m_135381_(DIRECTION_Z,
                tag.m_128403_("DirZ") ? tag.m_128457_("DirZ") : 1.0F);
        this.f_19804_.m_135381_(WIDTH,
                Math.max(0.1F, tag.m_128457_("Width")));
        this.f_19804_.m_135381_(LENGTH,
                Math.max(0.1F, tag.m_128457_("Length")));
        this.f_19804_.m_135381_(ALPHA,
                tag.m_128403_("Alpha") ? clamp01(tag.m_128457_("Alpha"))
                        : DEFAULT_ALPHA);
        this.f_19804_.m_135381_(DURATION,
                tag.m_128403_("Duration")
                        ? Math.max(1, tag.m_128451_("Duration"))
                        : DEFAULT_DURATION_TICKS);
        this.f_19804_.m_135381_(SEED, tag.m_128451_("Seed"));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        Vec3 direction = this.beamDirection();
        tag.m_128350_("DirX", (float) direction.f_82479_);
        tag.m_128350_("DirY", (float) direction.f_82480_);
        tag.m_128350_("DirZ", (float) direction.f_82481_);
        tag.m_128350_("Width", this.visualWidth());
        tag.m_128350_("Length", this.visualLength());
        tag.m_128350_("Alpha", this.visualAlpha());
        tag.m_128405_("Duration", this.durationTicks());
        tag.m_128405_("Seed", this.visualSeed());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public Vec3 beamDirection() {
        Vec3 direction = new Vec3(
                this.f_19804_.m_135370_(DIRECTION_X),
                this.f_19804_.m_135370_(DIRECTION_Y),
                this.f_19804_.m_135370_(DIRECTION_Z));
        return direction.m_82556_() > 1.0E-6D
                ? direction.m_82541_() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    public float visualWidth() {
        return Math.max(0.1F, this.f_19804_.m_135370_(WIDTH));
    }

    public float visualLength() {
        return Math.max(0.1F, this.f_19804_.m_135370_(LENGTH));
    }

    public float visualAlpha() {
        return clamp01(this.f_19804_.m_135370_(ALPHA));
    }

    public int durationTicks() {
        return Math.max(1, this.f_19804_.m_135370_(DURATION));
    }

    public int visualSeed() {
        return this.f_19804_.m_135370_(SEED);
    }

    public float visualAge(float partialTick) {
        return this.f_19797_ + partialTick;
    }

    private void setLaserForward(Vec3 forward) {
        Vec3 normalized = forward.m_82556_() > 1.0E-6D
                ? forward.m_82541_() : new Vec3(0.0D, 0.0D, 1.0D);
        this.f_19804_.m_135381_(DIRECTION_X, (float) normalized.f_82479_);
        this.f_19804_.m_135381_(DIRECTION_Y, (float) normalized.f_82480_);
        this.f_19804_.m_135381_(DIRECTION_Z, (float) normalized.f_82481_);
    }

    private boolean hasLiveOwner() {
        if (this.ownerUuid == null || !(this.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        Entity entity = level.m_8791_(this.ownerUuid);
        return entity instanceof HadesEntity hades
                && hades.m_6084_() && !hades.m_213877_();
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
