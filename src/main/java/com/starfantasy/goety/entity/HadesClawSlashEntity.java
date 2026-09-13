package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Short-lived, visual-only attack effects used by Hades. */
public final class HadesClawSlashEntity extends Entity {
    public static final int LIFETIME_TICKS = 10;
    public static final int DIVE_RAY_LIFETIME_TICKS = 30;
    private static final String RADIUS_TAG = "Radius";
    private static final String MODE_TAG = "Mode";
    private static final String DIRECTION_YAW_TAG = "DirectionYaw";
    private static final String WARNING_WIDTH_TAG = "WarningWidth";
    private static final String WARNING_LENGTH_TAG = "WarningLength";
    private static final String CURVE_SIGN_TAG = "CurveSign";
    public static final int MODE_ROUNDHOUSE = 0;
    public static final int MODE_RECTANGLE = 1;
    public static final int MODE_DIVE_RAY_LASER = 2;
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> VISUAL_SCALE =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class, EntityDataSerializers.f_135029_);
    private java.util.UUID ownerUuid;
    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> DIRECTION_YAW =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> WARNING_WIDTH =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> WARNING_LENGTH =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> CURVE_SIGN =
            SynchedEntityData.m_135353_(HadesClawSlashEntity.class,
                    EntityDataSerializers.f_135029_);

    public HadesClawSlashEntity(
            EntityType<? extends HadesClawSlashEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static HadesClawSlashEntity spawn(
            Entity source, Vec3 position, float radius) {
        if (source == null || position == null || source.m_9236_().f_46443_) {
            return null;
        }
        HadesClawSlashEntity slash = new HadesClawSlashEntity(
                ApollyonEntityRegistry.HADES_CLAW_SLASH.get(), source.m_9236_());
        slash.f_19804_.m_135381_(MODE, MODE_ROUNDHOUSE);
        slash.bindOwner(source);
        slash.f_19804_.m_135381_(RADIUS, Math.max(1.0F, radius));
        slash.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return source.m_9236_().m_7967_(slash) ? slash : null;
    }

    public static HadesClawSlashEntity spawnRectangle(
            Entity source, Vec3 position, Vec3 direction,
            float width, float length, boolean first) {
        if (source == null || position == null || direction == null
                || source.m_9236_().f_46443_) {
            return null;
        }
        HadesClawSlashEntity slash = new HadesClawSlashEntity(
                ApollyonEntityRegistry.HADES_CLAW_SLASH.get(), source.m_9236_());
        slash.f_19804_.m_135381_(MODE, MODE_RECTANGLE);
        slash.bindOwner(source);
        slash.f_19804_.m_135381_(DIRECTION_YAW, (float) Math.toDegrees(
                Math.atan2(-direction.f_82479_, direction.f_82481_)));
        slash.f_19804_.m_135381_(WARNING_WIDTH, Math.max(1.0F, width));
        slash.f_19804_.m_135381_(WARNING_LENGTH, Math.max(1.0F, length));
        slash.f_19804_.m_135381_(CURVE_SIGN, first ? 1.0F : -1.0F);
        slash.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return source.m_9236_().m_7967_(slash) ? slash : null;
    }

    public static HadesClawSlashEntity spawnDiveRayLaser(
            Entity source, Vec3 position, Vec3 direction,
            float width, float length) {
        if (source == null || position == null || direction == null
                || source.m_9236_().f_46443_) {
            return null;
        }
        HadesClawSlashEntity laser = new HadesClawSlashEntity(
                ApollyonEntityRegistry.HADES_CLAW_SLASH.get(), source.m_9236_());
        laser.f_19804_.m_135381_(MODE, MODE_DIVE_RAY_LASER);
        laser.bindOwner(source);
        laser.f_19804_.m_135381_(DIRECTION_YAW, (float) Math.toDegrees(
                Math.atan2(-direction.f_82479_, direction.f_82481_)));
        laser.f_19804_.m_135381_(WARNING_WIDTH, Math.max(1.0F, width));
        laser.f_19804_.m_135381_(WARNING_LENGTH, Math.max(1.0F, length));
        laser.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return source.m_9236_().m_7967_(laser) ? laser : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(VISUAL_SCALE, 1.0F);
        this.f_19804_.m_135372_(RADIUS, 20.0F);
        this.f_19804_.m_135372_(MODE, MODE_ROUNDHOUSE);
        this.f_19804_.m_135372_(DIRECTION_YAW, 0.0F);
        this.f_19804_.m_135372_(WARNING_WIDTH, 20.0F);
        this.f_19804_.m_135372_(WARNING_LENGTH, 40.0F);
        this.f_19804_.m_135372_(CURVE_SIGN, 1.0F);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.ownerUuid != null && this.m_9236_() instanceof net.minecraft.server.level.ServerLevel level) {
            Entity owner = level.m_8791_(this.ownerUuid);
            if (owner == null || !owner.m_6084_() || owner.m_213877_()) {
                this.m_146870_();
                return;
            }
        }
        if (!this.m_9236_().f_46443_ && this.f_19797_ >= this.lifetimeTicks()) {
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
        return distanceSqr < 262144.0D;
    }

    @Override
    public AABB m_6921_() {
        return super.m_6921_().m_82377_(32.0D, 24.0D, 32.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        this.ownerUuid = tag.m_128403_("Owner") ? tag.m_128342_("Owner") : null;
        this.f_19804_.m_135381_(VISUAL_SCALE, tag.m_128441_("VisualScale") ? tag.m_128457_("VisualScale") : 1.0F);
        this.f_19804_.m_135381_(RADIUS, Math.max(1.0F, tag.m_128457_(RADIUS_TAG)));
        this.f_19804_.m_135381_(MODE, tag.m_128451_(MODE_TAG));
        this.f_19804_.m_135381_(DIRECTION_YAW, tag.m_128457_(DIRECTION_YAW_TAG));
        this.f_19804_.m_135381_(WARNING_WIDTH,
                Math.max(1.0F, tag.m_128457_(WARNING_WIDTH_TAG)));
        this.f_19804_.m_135381_(WARNING_LENGTH,
                Math.max(1.0F, tag.m_128457_(WARNING_LENGTH_TAG)));
        this.f_19804_.m_135381_(CURVE_SIGN,
                tag.m_128457_(CURVE_SIGN_TAG) < 0.0F ? -1.0F : 1.0F);
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) tag.m_128362_("Owner", this.ownerUuid);
        tag.m_128350_("VisualScale", this.visualScale());
        tag.m_128350_(RADIUS_TAG, this.radius());
        tag.m_128405_(MODE_TAG, this.mode());
        tag.m_128350_(DIRECTION_YAW_TAG, this.directionYaw());
        tag.m_128350_(WARNING_WIDTH_TAG, this.warningWidth());
        tag.m_128350_(WARNING_LENGTH_TAG, this.warningLength());
        tag.m_128350_(CURVE_SIGN_TAG, this.curveSign());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float radius() {
        return this.f_19804_.m_135370_(RADIUS);
    }

    private void bindOwner(Entity source) {
        this.ownerUuid = source.m_20148_();
        this.f_19804_.m_135381_(VISUAL_SCALE, source instanceof HadesServantEntity ? 0.5F : 1.0F);
    }

    public void setVisualScale(float scale) {
        this.f_19804_.m_135381_(VISUAL_SCALE, Math.max(0.01F, scale));
    }

    public float visualScale() { return Math.max(0.01F, this.f_19804_.m_135370_(VISUAL_SCALE)); }

    public int mode() {
        return this.f_19804_.m_135370_(MODE);
    }

    public boolean isRectangleSlash() {
        return this.mode() == MODE_RECTANGLE;
    }

    public boolean isDiveRayLaser() {
        return this.mode() == MODE_DIVE_RAY_LASER;
    }

    public int lifetimeTicks() {
        return this.isDiveRayLaser() ? DIVE_RAY_LIFETIME_TICKS : LIFETIME_TICKS;
    }

    public float directionYaw() {
        return this.f_19804_.m_135370_(DIRECTION_YAW);
    }

    public float warningWidth() {
        return this.f_19804_.m_135370_(WARNING_WIDTH);
    }

    public float warningLength() {
        return this.f_19804_.m_135370_(WARNING_LENGTH);
    }

    public float curveSign() {
        return this.f_19804_.m_135370_(CURVE_SIGN);
    }

    public float visualAge(float partialTick) {
        return this.f_19797_ + partialTick;
    }
}
