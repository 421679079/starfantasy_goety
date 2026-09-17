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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Transient anchor for the hand-bound lightning used while casting a storm. */
public final class ApollyonCastingLightningEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER_ID =
            SynchedEntityData.m_135353_(
                    ApollyonCastingLightningEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DURATION =
            SynchedEntityData.m_135353_(
                    ApollyonCastingLightningEntity.class, EntityDataSerializers.f_135028_);

    public ApollyonCastingLightningEntity(
            EntityType<? extends ApollyonCastingLightningEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    private ApollyonCastingLightningEntity(Level level, LivingEntity owner, int duration) {
        this(ApollyonEntityRegistry.APOLLYON_CASTING_LIGHTNING.get(), level);
        this.f_19804_.m_135381_(OWNER_ID, owner.m_19879_());
        this.f_19804_.m_135381_(DURATION, Math.max(1, duration));
        this.follow(owner);
    }

    public static void spawn(LivingEntity owner, int duration) {
        if (owner == null || owner.m_9236_().f_46443_) {
            return;
        }
        owner.m_9236_().m_7967_(new ApollyonCastingLightningEntity(
                owner.m_9236_(), owner, duration));
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(OWNER_ID, -1);
        this.f_19804_.m_135372_(DURATION, 1);
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.f_19797_ >= this.getDuration()) {
            this.m_146870_();
            return;
        }
        Entity owner = this.m_9236_().m_6815_(this.f_19804_.m_135370_(OWNER_ID));
        if (owner instanceof LivingEntity living && living.m_6084_()) {
            this.follow(living);
            return;
        }
        // A client can receive this spawn packet one frame before the owner entity.
        // Let the server remain authoritative instead of deleting the visual locally.
        if (!this.m_9236_().f_46443_) {
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
        return distanceSqr < 65536.0D;
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        this.f_19804_.m_135381_(OWNER_ID, tag.m_128451_("OwnerId"));
        this.f_19804_.m_135381_(DURATION, Math.max(1, tag.m_128451_("Duration")));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        tag.m_128405_("OwnerId", this.f_19804_.m_135370_(OWNER_ID));
        tag.m_128405_("Duration", this.getDuration());
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public int getDuration() {
        return this.f_19804_.m_135370_(DURATION);
    }

    public int getVisualAge() {
        return this.f_19797_;
    }

    private void follow(LivingEntity owner) {
        Vec3 hand;
        if (owner instanceof ApollyonEntity apollyon) {
            hand = apollyon.castingHandPosition();
        } else if (owner instanceof ApollyonServantEntity servant) {
            hand = servant.castingHandPosition();
        } else if (owner instanceof ApollyonPageantApostleEntity actor) {
            hand = actor.castingHandPosition();
        } else if (owner instanceof com.Polarice3.Goety.common.entities.boss.Apostle apostle) {
            hand = com.starfantasy.goety.combat.apostle.ApostleSpellSupport.castingHand(apostle);
        } else {
            hand = owner.m_20182_().m_82520_(0.0D, owner.m_20192_() * 0.8D, 0.0D);
        }
        this.m_6034_(hand.f_82479_, hand.f_82480_, hand.f_82481_);
    }
}
