package com.starfantasy.goety.entity;

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

    public ApollyonCleaveEffectEntity(
            EntityType<? extends ApollyonCleaveEffectEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
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
        return super.m_6921_().m_82377_(21.0D, 18.0D, 21.0D);
    }

    @Override
    protected void m_7378_(CompoundTag tag) {
    }

    @Override
    protected void m_7380_(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float visualAge(float partialTick) {
        return this.f_19797_ + partialTick;
    }
}
