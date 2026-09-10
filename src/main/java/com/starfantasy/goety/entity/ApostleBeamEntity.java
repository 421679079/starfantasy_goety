package com.starfantasy.goety.entity;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkHooks;

/** Visual and warning anchor only. The ordinary Apostle's goal owns aim and damage. */
public final class ApostleBeamEntity extends Entity {
    public static final float LENGTH = 48;
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.m_135353_(ApostleBeamEntity.class, EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Float> PRECISE_YAW = SynchedEntityData.m_135353_(ApostleBeamEntity.class, EntityDataSerializers.f_135029_);
    private Apostle owner;
    private boolean clientYawInitialized;
    public ApostleBeamEntity(EntityType<? extends ApostleBeamEntity> type, Level level) {
        super(type, level);
        m_20242_(true);
    }
    public static ApostleBeamEntity spawn(Apostle owner, float yaw) {
        var beam = new ApostleBeamEntity(ApollyonEntityRegistry.APOSTLE_BEAM.get(), owner.m_9236_());
        beam.owner = owner;
        beam.update(owner, yaw, 1);
        beam.f_19859_ = yaw;
        return owner.m_9236_().m_7967_(beam) ? beam : null;
    }
    public void update(Apostle caster, float yaw, int age) {
        m_6034_(caster.m_20185_(), caster.m_20186_(), caster.m_20189_());
        m_146922_(yaw);
        f_19804_.m_135381_(PRECISE_YAW, yaw);
        f_19804_.m_135381_(AGE, age);
    }
    public boolean active() { return f_19804_.m_135370_(AGE) > 40; }
    public int duration() { return 60; }
    public float visualAge(float partialTick) { return Math.max(0, f_19804_.m_135370_(AGE) - 41 + partialTick); }
    public float visualYaw(float partialTick) { return Mth.m_14189_(partialTick, f_19859_, m_146908_()); }
    @Override protected void m_8097_() {
        f_19804_.m_135372_(AGE, 0);
        // No angle until the initial metadata arrives; do not turn toward a default zero.
        f_19804_.m_135372_(PRECISE_YAW, Float.NaN);
    }
    @Override public void m_8119_() {
        super.m_8119_();
        if (m_9236_().f_46443_) advanceClientYaw(f_19804_.m_135370_(PRECISE_YAW));
        if (!m_9236_().f_46443_ && (owner == null || !owner.m_6084_()
                || !owner.isCasting() || f_19797_ > 102)) m_146870_();
    }
    private void advanceClientYaw(float target) {
        if (!Float.isFinite(target)) return;
        float previous = clientYawInitialized ? m_146908_() : target;
        clientYawInitialized = true;
        f_19859_ = previous;
        m_146922_(previous + Mth.m_14177_(target - previous));
    }
    @Override public void m_6453_(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        // Position still uses vanilla packets. Their byte-quantized yaw must not overwrite
        // the precise tick endpoints also read by the tracking rectangle's renderer.
        float previous = f_19859_;
        super.m_6453_(x, y, z, m_146908_(), pitch, steps, teleport);
        f_19859_ = previous;
    }
    @Override public boolean m_6094_() { return false; }
    @Override public boolean m_5829_() { return false; }
    @Override public AABB m_6921_() { return super.m_6921_().m_82400_(LENGTH + 2); }
    @Override public boolean m_6783_(double distance) { return distance < 262144; }
    @Override protected void m_7378_(CompoundTag tag) { }
    @Override protected void m_7380_(CompoundTag tag) { }
    @Override public Packet<ClientGamePacketListener> m_5654_() { return NetworkHooks.getEntitySpawningPacket(this); }
}
