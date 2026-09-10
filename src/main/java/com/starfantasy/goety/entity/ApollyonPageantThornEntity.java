package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.projectiles.BlossomThorn;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Native Goety blossom-thorn visual with a synchronized pageant-only scale. */
public final class ApollyonPageantThornEntity extends BlossomThorn
        implements ApollyonPageantOwned {
    private static final String OWNER_TAG = "PageantOwner";
    private static final String SCALE_TAG = "PageantVisualScale";
    private static final EntityDataAccessor<Float> VISUAL_SCALE =
            SynchedEntityData.m_135353_(
                    ApollyonPageantThornEntity.class, EntityDataSerializers.f_135029_);

    private UUID ownerUuid;
    private final float baseHeight;

    public ApollyonPageantThornEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.baseHeight = this.height;
    }

    public void initialize(ApollyonEntity owner, Vec3 position, float visualScale) {
        this.ownerUuid = owner.m_20148_();
        this.setOwner(owner);
        this.warmupDelayTicks = 0;
        this.f_19804_.m_135381_(VISUAL_SCALE,
                Mth.m_14036_(visualScale, 0.1F, 8.0F));
        this.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        this.applyVisualScale();
    }

    @Override
    protected void m_8097_() {
        super.m_8097_();
        this.f_19804_.m_135372_(VISUAL_SCALE, 1.0F);
    }

    @Override
    public void m_8119_() {
        this.applyVisualScale();
        super.m_8119_();
    }

    private void applyVisualScale() {
        this.height = this.baseHeight * this.f_19804_.m_135370_(VISUAL_SCALE);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(VISUAL_SCALE,
                Mth.m_14036_(tag.m_128457_(SCALE_TAG), 0.1F, 8.0F));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128350_(SCALE_TAG, this.f_19804_.m_135370_(VISUAL_SCALE));
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }
}
