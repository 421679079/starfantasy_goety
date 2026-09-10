package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.projectiles.Pyroclast;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Controlled straight-falling, three-times-scale copy of Goety's Pyroclast projectile. */
public final class ApollyonPageantMeteorEntity extends Pyroclast implements ApollyonPageantOwned {
    public static final int FALL_TICKS = 20;
    private static final String OWNER_TAG = "PageantOwner";

    private UUID ownerUuid;

    public ApollyonPageantMeteorEntity(
            EntityType<? extends ApollyonPageantMeteorEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
        this.f_19811_ = true;
    }

    public static ApollyonPageantMeteorEntity spawn(ApollyonEntity owner, Vec3 impact) {
        if (owner == null || impact == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantMeteorEntity meteor = new ApollyonPageantMeteorEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_METEOR.get(), owner.m_9236_());
        meteor.ownerUuid = owner.m_20148_();
        meteor.m_6034_(impact.f_82479_, impact.f_82480_ + FALL_TICKS, impact.f_82481_);
        meteor.m_20256_(new Vec3(0.0D, -1.0D, 0.0D));
        return owner.m_9236_().m_7967_(meteor) ? meteor : null;
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.f_19811_ = true;
        this.m_20256_(new Vec3(0.0D, -1.0D, 0.0D));
        if (!this.m_9236_().f_46443_ && this.f_19797_ > FALL_TICKS + 5) {
            this.m_146870_();
        }
    }

    @Override
    public void explode() {
        // Damage and explosion visuals are fired once by the pageant controller.
    }

    @Override
    protected void m_6532_(HitResult result) {
        // Ignore terrain and entity collision so the descent remains exactly one block per tick.
    }

    @Override
    protected boolean m_5603_(Entity entity) {
        return false;
    }

    @Override
    public boolean m_6469_(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean m_6087_() {
        return false;
    }

    @Override
    public void m_7378_(CompoundTag tag) {
        super.m_7378_(tag);
        if (tag.m_128441_(OWNER_TAG)) {
            this.ownerUuid = tag.m_128342_(OWNER_TAG);
        }
    }

    @Override
    public void m_7380_(CompoundTag tag) {
        super.m_7380_(tag);
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
