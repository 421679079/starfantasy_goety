package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * One synchronized anchor describes a complete configurable radial sector pattern.
 * The client expands it into all warning or beam sectors without per-sector packets.
 */
public final class ApollyonSectorEffectEntity extends Entity {
    public static final int MODE_WARNING = 0;
    public static final int MODE_VOID_RAY = 1;
    public static final float TESSELLATION_DEGREES = 2.0F;

    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> START_TICK =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> RADIUS =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> SECTOR_ANGLE =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Float> SECTOR_SPACING =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> SECTOR_COUNT =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> ROTATION_OFFSET =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.m_135353_(
                    ApollyonSectorEffectEntity.class, EntityDataSerializers.f_135028_);

    public ApollyonSectorEffectEntity(
            EntityType<? extends ApollyonSectorEffectEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonSectorEffectEntity spawn(
            Level level, Vec3 center, int mode, int duration,
            float radius, float sectorAngle, float sectorSpacing,
            int sectorCount, float rotationOffset, int color) {
        if (level == null || center == null || level.f_46443_) {
            return null;
        }
        ApollyonSectorEffectEntity effect = new ApollyonSectorEffectEntity(
                ApollyonEntityRegistry.APOLLYON_SECTOR_EFFECT.get(), level);
        effect.m_6034_(center.f_82479_, center.f_82480_, center.f_82481_);
        effect.f_19804_.m_135381_(MODE, mode);
        effect.f_19804_.m_135381_(DURATION, Math.max(1, duration));
        effect.f_19804_.m_135381_(START_TICK, (int) level.m_46467_());
        effect.f_19804_.m_135381_(RADIUS, Math.max(0.1F, radius));
        effect.f_19804_.m_135381_(SECTOR_ANGLE, Math.max(0.1F, sectorAngle));
        effect.f_19804_.m_135381_(SECTOR_SPACING, sectorSpacing);
        effect.f_19804_.m_135381_(SECTOR_COUNT, Math.max(1, sectorCount));
        effect.f_19804_.m_135381_(ROTATION_OFFSET, rotationOffset);
        effect.f_19804_.m_135381_(COLOR, color & 0xFFFFFF);
        return level.m_7967_(effect) ? effect : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(MODE, MODE_WARNING);
        this.f_19804_.m_135372_(DURATION, 1);
        this.f_19804_.m_135372_(START_TICK, 0);
        this.f_19804_.m_135372_(RADIUS, 1.0F);
        this.f_19804_.m_135372_(SECTOR_ANGLE, 10.0F);
        this.f_19804_.m_135372_(SECTOR_SPACING, 20.0F);
        this.f_19804_.m_135372_(SECTOR_COUNT, 1);
        this.f_19804_.m_135372_(ROTATION_OFFSET, 0.0F);
        this.f_19804_.m_135372_(COLOR, 0xA020F0);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.getVisualAge(0.0F) >= this.getDuration()) {
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
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
    }

    @Override
    public AABB m_6921_() {
        double radius = this.getRadius() + 2.0D;
        return super.m_6921_().m_82377_(radius, 4.0D, radius);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        this.f_19804_.m_135381_(MODE, tag.m_128451_("Mode"));
        this.f_19804_.m_135381_(DURATION, Math.max(1, tag.m_128451_("Duration")));
        this.f_19804_.m_135381_(START_TICK, tag.m_128451_("StartTick"));
        this.f_19804_.m_135381_(RADIUS, Math.max(0.1F, tag.m_128457_("Radius")));
        this.f_19804_.m_135381_(SECTOR_ANGLE, Math.max(0.1F, tag.m_128457_("SectorAngle")));
        this.f_19804_.m_135381_(SECTOR_SPACING, tag.m_128457_("SectorSpacing"));
        this.f_19804_.m_135381_(SECTOR_COUNT, Math.max(1, tag.m_128451_("SectorCount")));
        this.f_19804_.m_135381_(ROTATION_OFFSET, tag.m_128457_("RotationOffset"));
        this.f_19804_.m_135381_(COLOR, tag.m_128451_("Color") & 0xFFFFFF);
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        tag.m_128405_("Mode", this.getMode());
        tag.m_128405_("Duration", this.getDuration());
        tag.m_128405_("StartTick", this.getStartTick());
        tag.m_128350_("Radius", this.getRadius());
        tag.m_128350_("SectorAngle", this.getSectorAngle());
        tag.m_128350_("SectorSpacing", this.getSectorSpacing());
        tag.m_128405_("SectorCount", this.getSectorCount());
        tag.m_128350_("RotationOffset", this.getRotationOffset());
        tag.m_128405_("Color", this.getColor());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public int getMode() {
        return this.f_19804_.m_135370_(MODE);
    }

    public int getDuration() {
        return this.f_19804_.m_135370_(DURATION);
    }

    public int getStartTick() {
        return this.f_19804_.m_135370_(START_TICK);
    }

    public float getRadius() {
        return this.f_19804_.m_135370_(RADIUS);
    }

    public float getSectorAngle() {
        return this.f_19804_.m_135370_(SECTOR_ANGLE);
    }

    public float getSectorSpacing() {
        return this.f_19804_.m_135370_(SECTOR_SPACING);
    }

    public int getSectorCount() {
        return this.f_19804_.m_135370_(SECTOR_COUNT);
    }

    public float getRotationOffset() {
        return this.f_19804_.m_135370_(ROTATION_OFFSET);
    }

    public int getColor() {
        return this.f_19804_.m_135370_(COLOR);
    }

    public float getVisualAge(float partialTick) {
        int currentTick = (int) this.m_9236_().m_46467_();
        return Math.max(0.0F, currentTick - this.getStartTick() + partialTick);
    }
}
