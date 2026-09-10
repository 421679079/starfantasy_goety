package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Purely visual Apostle halo that travels between the outer and inner pageant anchors. */
public final class ApollyonPageantHaloEntity extends Entity {
    public static final int OUTER_FLYING = 0;
    public static final int OUTER_ATTACHED = 1;
    public static final int INNER_FLYING = 2;
    public static final int INNER_ATTACHED = 3;

    public static final double OUTER_RADIUS = 21.0D;
    public static final double INNER_RADIUS = 5.0D;
    public static final double VISUAL_HEIGHT = 2.0D;
    public static final double OUTER_SPEED = 0.5D;
    public static final double OUTER_DEGREES_PER_TICK = 0.75D;
    public static final double INNER_DEGREES_PER_TICK = -3.0D;

    private static final int TRAIL_POINTS = 16;
    private static final double TRAIL_RESET_DISTANCE_SQR = 64.0D;

    private static final String OWNER_TAG = "PageantOwner";
    private static final String HOME_X_TAG = "HaloHomeX";
    private static final String HOME_Y_TAG = "HaloHomeY";
    private static final String HOME_Z_TAG = "HaloHomeZ";
    private static final String MODE_TAG = "HaloMode";
    private static final String ANGLE_TAG = "HaloAngle";
    private static final String INNER_TICKS_TAG = "HaloInnerTicks";
    private static final String VARIANT_TAG = "HaloVariant";

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.m_135353_(
                    ApollyonPageantHaloEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> OUTWARD_YAW =
            SynchedEntityData.m_135353_(
                    ApollyonPageantHaloEntity.class, EntityDataSerializers.f_135029_);

    private UUID ownerUuid;
    private Vec3 home = Vec3.f_82478_;
    private int mode = OUTER_FLYING;
    private double angleDegrees;
    private int innerTravelTicks;
    private int clientLerpSteps;
    private double clientLerpX;
    private double clientLerpY;
    private double clientLerpZ;
    private float clientLerpYaw;
    private float clientLerpPitch;
    private final double[] trailX = new double[TRAIL_POINTS];
    private final double[] trailY = new double[TRAIL_POINTS];
    private final double[] trailZ = new double[TRAIL_POINTS];
    private boolean trailInitialized;

    public ApollyonPageantHaloEntity(
            EntityType<? extends ApollyonPageantHaloEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantHaloEntity spawnOuter(
            ApollyonEntity owner, Vec3 start, int variant, double angleDegrees) {
        if (owner == null || start == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantHaloEntity halo = new ApollyonPageantHaloEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_HALO.get(), owner.m_9236_());
        halo.ownerUuid = owner.m_20148_();
        halo.home = owner.arenaHomePosition();
        halo.mode = OUTER_FLYING;
        halo.angleDegrees = angleDegrees;
        halo.setVariant(variant);
        halo.m_6034_(start.f_82479_, start.f_82480_ + VISUAL_HEIGHT, start.f_82481_);
        halo.faceOutward();
        return owner.m_9236_().m_7967_(halo) ? halo : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(VARIANT, 0);
        this.f_19804_.m_135372_(OUTWARD_YAW, 0.0F);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.m_9236_().f_46443_) {
            this.tickClientInterpolation();
            this.m_146922_(this.outwardYaw());
            this.recordTrail();
            return;
        }
        Entity owner = this.ownerUuid == null || !(this.m_9236_() instanceof net.minecraft.server.level.ServerLevel level)
                ? null : level.m_8791_(this.ownerUuid);
        if (!(owner instanceof ApollyonEntity) || !owner.m_6084_()) {
            this.m_146870_();
            return;
        }

        if (this.mode == OUTER_FLYING || this.mode == OUTER_ATTACHED) {
            this.angleDegrees += OUTER_DEGREES_PER_TICK;
            Vec3 target = this.anchor(OUTER_RADIUS);
            if (this.mode == OUTER_FLYING) {
                Vec3 delta = target.m_82546_(this.m_20182_());
                double distance = delta.m_82553_();
                if (distance <= OUTER_SPEED || distance < 1.0E-7D) {
                    this.mode = OUTER_ATTACHED;
                    this.snap(target);
                } else {
                    this.snap(this.m_20182_().m_82549_(delta.m_82490_(OUTER_SPEED / distance)));
                }
            } else {
                this.snap(target);
            }
        } else {
            this.angleDegrees += INNER_DEGREES_PER_TICK;
            Vec3 target = this.anchor(INNER_RADIUS);
            if (this.mode == INNER_FLYING && this.innerTravelTicks > 0) {
                Vec3 current = this.m_20182_();
                Vec3 step = target.m_82546_(current).m_82490_(1.0D / this.innerTravelTicks);
                this.snap(current.m_82549_(step));
                --this.innerTravelTicks;
                if (this.innerTravelTicks <= 0) {
                    this.mode = INNER_ATTACHED;
                    this.snap(target);
                }
            } else {
                this.mode = INNER_ATTACHED;
                this.snap(target);
            }
        }
        this.faceOutward();
    }

    public void beginInnerOrbit(int slot, int travelTicks) {
        this.mode = INNER_FLYING;
        this.angleDegrees = slot * 30.0D;
        this.innerTravelTicks = Math.max(1, travelTicks);
    }

    public int variant() {
        return this.f_19804_.m_135370_(VARIANT);
    }

    @Override
    public void m_6453_(double x, double y, double z, float yaw, float pitch,
                        int interpolationSteps, boolean teleport) {
        if (!this.m_9236_().f_46443_) {
            super.m_6453_(x, y, z, yaw, pitch, interpolationSteps, teleport);
            return;
        }
        this.clientLerpX = x;
        this.clientLerpY = y;
        this.clientLerpZ = z;
        this.clientLerpYaw = yaw;
        this.clientLerpPitch = pitch;
        this.clientLerpSteps = Math.max(2, interpolationSteps);
    }

    public UUID ownerUuid() {
        return this.ownerUuid;
    }

    public int trailPointCount() {
        return TRAIL_POINTS;
    }

    public float interpolatedOutwardYaw(float partialTick) {
        return Mth.m_14189_(partialTick, this.f_19859_, this.m_146908_());
    }

    public Vec3 trailPoint(int index) {
        if (!this.trailInitialized) {
            this.resetTrail(this.m_20182_());
        }
        int clamped = Mth.m_14045_(index, 0, TRAIL_POINTS - 1);
        if (clamped > 0) {
            return new Vec3(
                    (this.trailX[clamped] + this.trailX[clamped - 1]) * 0.5D,
                    (this.trailY[clamped] + this.trailY[clamped - 1]) * 0.5D,
                    (this.trailZ[clamped] + this.trailZ[clamped - 1]) * 0.5D);
        }
        return new Vec3(this.trailX[0], this.trailY[0], this.trailZ[0]);
    }

    private void setVariant(int variant) {
        this.f_19804_.m_135381_(VARIANT, Mth.m_14045_(variant, 0, 11));
    }

    private Vec3 anchor(double radius) {
        double angle = Math.toRadians(this.angleDegrees);
        return new Vec3(
                this.home.f_82479_ + Math.cos(angle) * radius,
                this.home.f_82480_ + VISUAL_HEIGHT,
                this.home.f_82481_ + Math.sin(angle) * radius);
    }

    private void snap(Vec3 position) {
        this.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        this.m_20256_(Vec3.f_82478_);
    }

    private void tickClientInterpolation() {
        if (this.clientLerpSteps <= 0) {
            return;
        }
        double steps = this.clientLerpSteps;
        double x = this.m_20185_() + (this.clientLerpX - this.m_20185_()) / steps;
        double y = this.m_20186_() + (this.clientLerpY - this.m_20186_()) / steps;
        double z = this.m_20189_() + (this.clientLerpZ - this.m_20189_()) / steps;
        float yaw = this.m_146908_()
                + Mth.m_14177_(this.clientLerpYaw - this.m_146908_()) / this.clientLerpSteps;
        float pitch = this.m_146909_()
                + (this.clientLerpPitch - this.m_146909_()) / this.clientLerpSteps;
        this.m_6034_(x, y, z);
        this.m_146922_(yaw);
        this.m_146926_(pitch);
        --this.clientLerpSteps;
    }

    private void resetTrail(Vec3 point) {
        for (int i = 0; i < TRAIL_POINTS; ++i) {
            this.trailX[i] = point.f_82479_;
            this.trailY[i] = point.f_82480_;
            this.trailZ[i] = point.f_82481_;
        }
        this.trailInitialized = true;
    }

    private void recordTrail() {
        Vec3 point = this.m_20182_();
        if (!this.trailInitialized
                || point.m_82531_(this.trailX[0], this.trailY[0], this.trailZ[0])
                > TRAIL_RESET_DISTANCE_SQR) {
            this.resetTrail(point);
            return;
        }
        for (int i = TRAIL_POINTS - 1; i > 0; --i) {
            this.trailX[i] = this.trailX[i - 1];
            this.trailY[i] = this.trailY[i - 1];
            this.trailZ[i] = this.trailZ[i - 1];
        }
        this.trailX[0] = point.f_82479_;
        this.trailY[0] = point.f_82480_;
        this.trailZ[0] = point.f_82481_;
    }

    private void faceOutward() {
        float yaw = (float) Math.toDegrees(Math.atan2(
                -(this.m_20185_() - this.home.f_82479_),
                this.m_20189_() - this.home.f_82481_));
        this.f_19804_.m_135381_(OUTWARD_YAW, yaw);
        this.m_146922_(yaw);
    }

    private float outwardYaw() {
        return this.f_19804_.m_135370_(OUTWARD_YAW);
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
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
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
    }

    @Override
    public AABB m_6921_() {
        return super.m_6921_().m_82377_(6.0D, 8.0D, 6.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.home = new Vec3(tag.m_128459_(HOME_X_TAG), tag.m_128459_(HOME_Y_TAG),
                tag.m_128459_(HOME_Z_TAG));
        this.mode = tag.m_128451_(MODE_TAG);
        this.angleDegrees = tag.m_128459_(ANGLE_TAG);
        this.innerTravelTicks = Math.max(0, tag.m_128451_(INNER_TICKS_TAG));
        this.setVariant(tag.m_128451_(VARIANT_TAG));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128347_(HOME_X_TAG, this.home.f_82479_);
        tag.m_128347_(HOME_Y_TAG, this.home.f_82480_);
        tag.m_128347_(HOME_Z_TAG, this.home.f_82481_);
        tag.m_128405_(MODE_TAG, this.mode);
        tag.m_128347_(ANGLE_TAG, this.angleDegrees);
        tag.m_128405_(INNER_TICKS_TAG, this.innerTravelTicks);
        tag.m_128405_(VARIANT_TAG, this.variant());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
