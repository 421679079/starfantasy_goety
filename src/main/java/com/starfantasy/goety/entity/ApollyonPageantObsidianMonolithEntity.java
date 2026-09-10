package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.hostile.servants.ObsidianMonolith;
import com.Polarice3.Goety.common.entities.neutral.AbstractObsidianMonolith;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Two-times rendered, invulnerable monolith used only by pageant phase three. */
public final class ApollyonPageantObsidianMonolithEntity extends ObsidianMonolith
        implements ApollyonPageantOwned {
    private static final String PAGEANT_OWNER_TAG = "PageantOwner";
    private static final int EMERGING_TICKS = 40;

    private UUID pageantOwnerUuid;
    private Vec3 fixedPosition;

    public ApollyonPageantObsidianMonolithEntity(
            EntityType<? extends AbstractObsidianMonolith> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantObsidianMonolithEntity spawn(
            ApollyonEntity owner, Vec3 position) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantObsidianMonolithEntity monolith =
                ApollyonEntityRegistry.APOLLYON_PAGEANT_OBSIDIAN_MONOLITH.get()
                        .m_20615_(owner.m_9236_());
        if (monolith == null) {
            return null;
        }
        monolith.pageantOwnerUuid = owner.m_20148_();
        monolith.fixedPosition = position;
        monolith.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        monolith.setAge(0);
        monolith.setActivate(false);
        monolith.setOwnerId(null);
        monolith.setOwnerClientId(-1);
        return owner.m_9236_().m_7967_(monolith) ? monolith : null;
    }

    @Override
    public void m_8107_() {
        if (!this.m_9236_().f_46443_ && this.getAge() < EMERGING_TICKS) {
            this.setAge(this.getAge() + 1);
            if (this.getAge() >= EMERGING_TICKS) {
                this.setActivate(true);
            }
        }
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_21573_().m_26573_();
        this.m_20256_(Vec3.f_82478_);
        if (!this.m_9236_().f_46443_) {
            if (this.fixedPosition == null) {
                this.fixedPosition = this.m_20182_();
            }
            this.m_6034_(this.fixedPosition.f_82479_, this.fixedPosition.f_82480_,
                    this.fixedPosition.f_82481_);
            Entity owner = this.pageantOwner();
            if (!(owner instanceof ApollyonEntity) || !owner.m_6084_()) {
                this.m_146870_();
            }
        }
    }

    @Override
    public float localEmergingTime() {
        return EMERGING_TICKS;
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6673_(DamageSource source) {
        return true;
    }

    @Override
    public boolean m_5829_() {
        return false;
    }

    @Override
    public boolean m_6087_() {
        return false;
    }

    @Override
    public boolean m_6097_() {
        return false;
    }

    @Override
    public boolean m_6094_() {
        return false;
    }

    /** Excludes this inert mechanic entity from standard living-target selection. */
    @Override
    public boolean m_142065_() {
        return false;
    }

    @Override
    public boolean m_8023_() {
        return false;
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_(PAGEANT_OWNER_TAG)) {
            this.pageantOwnerUuid = tag.m_128342_(PAGEANT_OWNER_TAG);
        }
        this.fixedPosition = this.m_20182_();
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
        if (this.pageantOwnerUuid != null) {
            tag.m_128362_(PAGEANT_OWNER_TAG, this.pageantOwnerUuid);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public UUID pageantOwnerUuid() {
        return this.pageantOwnerUuid;
    }

    public void setLinkedApostle(ApollyonPageantApostleEntity apostle) {
        if (apostle == null) {
            this.setOwnerId(null);
            this.setOwnerClientId(-1);
        } else {
            this.setOwnerId(apostle.m_20148_());
            this.setOwnerClientId(apostle.m_19879_());
        }
    }

    private Entity pageantOwner() {
        if (this.pageantOwnerUuid == null
                || !(this.m_9236_() instanceof ServerLevel level)) {
            return null;
        }
        return level.m_8791_(this.pageantOwnerUuid);
    }
}
