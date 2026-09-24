package com.starfantasy.goety.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Short-lived visual-only reconstruction of Warcraft III's t_cleave Birth effect. */
public final class ApollyonCleaveEffectEntity extends Entity {
    public static final int CRACK_EXPAND_TICKS = 40;
    public static final int CRACK_HOLD_TICKS = 20;
    public static final int BURST_TICKS = 2;
    public static final int PEAK_PULSE_TICKS = 40;
    public static final int FADE_TICKS = 5;
    public static final int BURST_START_TICK = CRACK_EXPAND_TICKS + CRACK_HOLD_TICKS;
    public static final int PULSE_START_TICK = BURST_START_TICK + BURST_TICKS;
    public static final int FADE_START_TICK = PULSE_START_TICK + PEAK_PULSE_TICKS;
    public static final int LIFETIME_TICKS = FADE_START_TICK + FADE_TICKS;
    private static final EntityDataAccessor<Boolean> SERVANT = SynchedEntityData.m_135353_(
            ApollyonCleaveEffectEntity.class, EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Integer> START_TICK = SynchedEntityData.m_135353_(
            ApollyonCleaveEffectEntity.class, EntityDataSerializers.f_135028_);
    private UUID servantUuid;

    public ApollyonCleaveEffectEntity(
            EntityType<? extends ApollyonCleaveEffectEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    @Override
    protected void m_8097_() {
        this.f_19804_.m_135372_(SERVANT, false);
        this.f_19804_.m_135372_(START_TICK, 0);
    }

    public void configureServant(HadesServantEntity servant) {
        this.servantUuid = servant.m_20148_();
        this.f_19804_.m_135381_(SERVANT, true);
        this.f_19804_.m_135381_(START_TICK, (int) this.m_9236_().m_46467_());
    }

    public boolean isServantEffect() { return this.f_19804_.m_135370_(SERVANT); }
    public int lifetimeTicks() { return this.isServantEffect() ? BURST_START_TICK + 40 : LIFETIME_TICKS; }
    public int fadeStartTick() { return this.lifetimeTicks() - FADE_TICKS; }

    @Override
    public void m_8119_() {
        super.m_8119_();
        this.m_20242_(true);
        this.m_20256_(Vec3.f_82478_);
        if (this.m_9236_() instanceof ServerLevel level) {
            if (this.isServantEffect() && (this.servantUuid == null
                    || !(level.m_8791_(this.servantUuid) instanceof HadesServantEntity servant)
                    || !servant.m_6084_() || servant.attackType() != HadesServantEntity.INFERNAL_JUDGMENT)) {
                this.m_146870_();
            } else if (this.visualAge(0) >= this.lifetimeTicks()) this.m_146870_();
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
        return super.m_6921_().m_82377_(21.0D, this.isServantEffect() ? 31.0D : 18.0D, 21.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
        this.f_19804_.m_135381_(SERVANT, tag.m_128471_("ServantEffect"));
        this.f_19804_.m_135381_(START_TICK, tag.m_128451_("StartTick"));
        this.servantUuid = tag.m_128403_("Servant") ? tag.m_128342_("Servant") : null;
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
        tag.m_128379_("ServantEffect", this.isServantEffect());
        tag.m_128405_("StartTick", this.f_19804_.m_135370_(START_TICK));
        if (this.servantUuid != null) tag.m_128362_("Servant", this.servantUuid);
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float visualAge(float partialTick) {
        if (this.isServantEffect()) return Math.max(0,
                (int) this.m_9236_().m_46467_() - this.f_19804_.m_135370_(START_TICK)) + partialTick;
        return this.f_19797_ + partialTick;
    }
}
