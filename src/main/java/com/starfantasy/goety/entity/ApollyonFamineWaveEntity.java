package com.starfantasy.goety.entity;

import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Ten-tick, arena-wide poison-fog wave emitted when Profane applies famine. */
public final class ApollyonFamineWaveEntity extends Entity implements ApollyonPageantOwned {
    public static final int LIFETIME_TICKS = 10;
    public static final float MAX_RADIUS = 20.0F;
    private static final String OWNER_TAG = "PageantOwner";

    private UUID ownerUuid;

    public ApollyonFamineWaveEntity(
            EntityType<? extends ApollyonFamineWaveEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonFamineWaveEntity spawn(ApollyonEntity owner, Vec3 position) {
        return spawnForCaster(owner, position);
    }

    public static ApollyonFamineWaveEntity spawnForCaster(net.minecraft.world.entity.LivingEntity owner, Vec3 position) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonFamineWaveEntity wave = new ApollyonFamineWaveEntity(
                ApollyonEntityRegistry.APOLLYON_FAMINE_WAVE.get(), owner.m_9236_());
        wave.ownerUuid = owner.m_20148_();
        wave.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return owner.m_9236_().m_7967_(wave) ? wave : null;
    }

    @Override
    protected void m_8097_() {
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (!this.m_9236_().f_46443_ && this.f_19797_ >= LIFETIME_TICKS) {
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
        return super.m_6921_().m_82377_(MAX_RADIUS + 2.0D, 4.0D, MAX_RADIUS + 2.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.ownerUuid;
    }

    public float visualAge(float partialTick) {
        return this.f_19797_ + partialTick;
    }
}
