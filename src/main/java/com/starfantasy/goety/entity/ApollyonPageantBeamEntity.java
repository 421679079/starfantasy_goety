package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
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

/** Pure visual, caster-bound corruption beam; the pageant controller owns damage. */
public final class ApollyonPageantBeamEntity extends Entity implements ApollyonPageantOwned {
    public static final float LENGTH = 20.0F;
    private static final String OWNER_TAG = "PageantOwner";
    private static final EntityDataAccessor<Integer> CASTER_ID =
            SynchedEntityData.m_135353_(
                    ApollyonPageantBeamEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.m_135353_(
                    ApollyonPageantBeamEntity.class, EntityDataSerializers.f_135028_);

    private UUID ownerUuid;

    public ApollyonPageantBeamEntity(
            EntityType<? extends ApollyonPageantBeamEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantBeamEntity spawn(
            ApollyonEntity owner, ApollyonPageantApostleEntity caster, int duration) {
        if (owner == null || caster == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantBeamEntity beam = new ApollyonPageantBeamEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_BEAM.get(), owner.m_9236_());
        beam.ownerUuid = owner.m_20148_();
        beam.f_19804_.m_135381_(CASTER_ID, caster.m_19879_());
        beam.f_19804_.m_135381_(DURATION, Math.max(1, duration));
        beam.follow(caster);
        beam.f_19859_ = beam.m_146908_();
        return owner.m_9236_().m_7967_(beam) ? beam : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(CASTER_ID, -1);
        this.f_19804_.m_135372_(DURATION, 1);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.f_19797_ >= this.duration()) {
            this.m_146870_();
            return;
        }
        Entity caster = this.m_9236_().m_6815_(this.f_19804_.m_135370_(CASTER_ID));
        if (caster instanceof ApollyonPageantApostleEntity actor && actor.m_6084_()) {
            this.f_19859_ = this.m_146908_();
            this.follow(actor);
        } else if (!this.m_9236_().f_46443_) {
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
        return super.m_6921_().m_82400_(LENGTH + 2.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(DURATION, Math.max(1, tag.m_128451_("Duration")));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128405_("Duration", this.duration());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }

    public int duration() {
        return this.f_19804_.m_135370_(DURATION);
    }

    public float visualAge(float partialTick) {
        return this.f_19797_ + partialTick;
    }

    private void follow(ApollyonPageantApostleEntity caster) {
        this.m_6034_(caster.m_20185_(), caster.m_20186_() + 1.35D, caster.m_20189_());
        this.m_146922_(caster.pageantBodyYaw());
    }
}
