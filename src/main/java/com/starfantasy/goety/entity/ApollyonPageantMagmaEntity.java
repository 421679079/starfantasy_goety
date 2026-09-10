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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Two-block solid, harmless magma prop left by the pageant meteor. */
public final class ApollyonPageantMagmaEntity extends Entity implements ApollyonPageantOwned {
    public static final int MAX_LIFETIME_TICKS = 160;
    private static final String OWNER_TAG = "PageantOwner";

    private UUID ownerUuid;

    public ApollyonPageantMagmaEntity(
            EntityType<? extends ApollyonPageantMagmaEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantMagmaEntity spawn(ApollyonEntity owner, Vec3 position) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantMagmaEntity magma = new ApollyonPageantMagmaEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_MAGMA.get(), owner.m_9236_());
        magma.ownerUuid = owner.m_20148_();
        magma.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return owner.m_9236_().m_7967_(magma) ? magma : null;
    }

    @Override
    protected void m_8097_() {
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (!this.m_9236_().f_46443_ && this.f_19797_ >= MAX_LIFETIME_TICKS) {
            this.m_146870_();
        }
    }

    @Override
    public boolean m_5829_() {
        return true;
    }

    @Override
    public boolean m_6094_() {
        return true;
    }

    @Override
    public void m_7334_(Entity entity) {
        // Players collide with the prop, but cannot shove the safe-zone anchor away.
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
}
