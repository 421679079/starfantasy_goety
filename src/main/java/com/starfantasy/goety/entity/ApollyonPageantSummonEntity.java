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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Time-compressed visual copy of Goety's Apostle summoning circle. */
public final class ApollyonPageantSummonEntity extends Entity implements ApollyonPageantOwned {
    public static final int DEFAULT_LIFETIME_TICKS = 20;
    private static final String OWNER_TAG = "PageantOwner";
    private static final String DURATION_TAG = "Duration";
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.m_135353_(
                    ApollyonPageantSummonEntity.class, EntityDataSerializers.f_135028_);

    private UUID ownerUuid;

    public ApollyonPageantSummonEntity(
            EntityType<? extends ApollyonPageantSummonEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantSummonEntity spawn(ApollyonEntity owner, Vec3 position) {
        return spawn(owner, position, DEFAULT_LIFETIME_TICKS);
    }

    public static ApollyonPageantSummonEntity spawn(
            ApollyonEntity owner, Vec3 position, int duration) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantSummonEntity effect = new ApollyonPageantSummonEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_SUMMON.get(), owner.m_9236_());
        effect.ownerUuid = owner.m_20148_();
        effect.f_19804_.m_135381_(DURATION, Math.max(1, duration));
        effect.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return owner.m_9236_().m_7967_(effect) ? effect : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(DURATION, DEFAULT_LIFETIME_TICKS);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (!this.m_9236_().f_46443_ && this.f_19797_ >= this.duration()) {
            this.m_146870_();
        }
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
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(DURATION,
                Math.max(1, tag.m_128451_(DURATION_TAG)));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128405_(DURATION_TAG, this.duration());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }

    public float compressedVisualAge(float partialTick) {
        return (this.f_19797_ + partialTick) * (450.0F / this.duration());
    }

    public int duration() {
        return this.f_19804_.m_135370_(DURATION);
    }
}
