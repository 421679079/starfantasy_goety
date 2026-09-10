package com.starfantasy.goety.entity;

import com.Polarice3.Goety.client.particles.CircleExplodeParticleOption;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Blue-ice obstacle with a 4-by-4 collision box left by a pageant Frost Impact. */
public final class ApollyonPageantBlueIceEntity extends Entity
        implements ApollyonPageantOwned {
    private static final String OWNER_TAG = "PageantOwner";

    private UUID ownerUuid;

    public ApollyonPageantBlueIceEntity(
            EntityType<? extends ApollyonPageantBlueIceEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public static ApollyonPageantBlueIceEntity spawn(
            ApollyonEntity owner, Vec3 position) {
        if (owner == null || position == null || owner.m_9236_().f_46443_) {
            return null;
        }
        ApollyonPageantBlueIceEntity ice = new ApollyonPageantBlueIceEntity(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_BLUE_ICE.get(), owner.m_9236_());
        ice.ownerUuid = owner.m_20148_();
        ice.m_6034_(position.f_82479_, position.f_82480_, position.f_82481_);
        return owner.m_9236_().m_7967_(ice) ? ice : null;
    }

    @Override
    protected void m_8097_() {
    }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
    }

    public void playDoubleImpact(ServerLevel level) {
        if (level == null) {
            return;
        }
        this.m_5496_((SoundEvent) ModSounds.ICE_CHUNK_HIT.get(), 2.0F, 1.0F);
        level.m_8767_(new BlockParticleOption(
                        ParticleTypes.f_123794_, Blocks.f_50354_.m_49966_()),
                this.m_20185_(), this.m_20186_() + 1.5D, this.m_20189_(),
                512, 1.5D, 1.5D, 1.5D, 1.0D);
        level.m_8767_(new CircleExplodeParticleOption(
                        new ColorUtil(0xFFFFBF), 10.0F, 1),
                this.m_20185_(), this.m_20186_(), this.m_20189_(),
                1, 0.0D, 0.0D, 0.0D, 0.5D);
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
        // The obstacle is solid but cannot be pushed by colliding entities.
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
    public boolean m_6783_(double distanceSqr) {
        return distanceSqr < 262144.0D;
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
