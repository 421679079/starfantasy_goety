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

/** Ten-tick translucent yellow sphere emitted when Glorious resolves its cast. */
public final class ApollyonGloriousSphereEntity extends Entity implements ApollyonPageantOwned {
    public static final int LIFETIME_TICKS = 10;
    public static final float MAX_RADIUS = 10.0F;
    public static final float RED_MAX_RADIUS = 20.0F;
    private static final String OWNER_TAG = "PageantOwner";
    private static final String RED_TAG = "PageantRedSphere";
    private static final EntityDataAccessor<Boolean> RED = SynchedEntityData.m_135353_(
            ApollyonGloriousSphereEntity.class, EntityDataSerializers.f_135035_);

    private UUID ownerUuid;

    public ApollyonGloriousSphereEntity(
            EntityType<? extends ApollyonGloriousSphereEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonGloriousSphereEntity spawn(ApollyonEntity owner, Vec3 position) {
        return spawn(owner, position, false);
    }

    public static ApollyonGloriousSphereEntity spawnRed(
            ApollyonEntity owner, Vec3 position) {
        return spawn(owner, position, true);
    }

    public static ApollyonGloriousSphereEntity spawnForCaster(net.minecraft.world.entity.LivingEntity owner, Vec3 position) {
        return spawn(owner, position, false);
    }

    private static ApollyonGloriousSphereEntity spawn(
            net.minecraft.world.entity.LivingEntity owner, Vec3 position, boolean red) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonGloriousSphereEntity sphere = new ApollyonGloriousSphereEntity(
                ApollyonEntityRegistry.APOLLYON_GLORIOUS_SPHERE.get(), owner.m_9236_());
        sphere.ownerUuid = owner.m_20148_();
        sphere.f_19804_.m_135381_(RED, red);
        sphere.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return owner.m_9236_().m_7967_(sphere) ? sphere : null;
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(RED, false);
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
        return super.m_6921_().m_82377_(RED_MAX_RADIUS + 1.0D, RED_MAX_RADIUS + 1.0D,
                RED_MAX_RADIUS + 1.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
        this.f_19804_.m_135381_(RED, tag.m_128471_(RED_TAG));
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        if (this.ownerUuid != null) {
            tag.m_128362_(OWNER_TAG, this.ownerUuid);
        }
        tag.m_128379_(RED_TAG, this.isRed());
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

    public boolean isRed() {
        return this.f_19804_.m_135370_(RED);
    }

    public float maxRadius() {
        return this.isRed() ? RED_MAX_RADIUS : MAX_RADIUS;
    }
}
