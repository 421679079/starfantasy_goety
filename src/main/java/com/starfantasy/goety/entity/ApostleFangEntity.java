package com.starfantasy.goety.entity;

import net.minecraft.world.entity.Mob;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Native fang animation only; the spell owns its square-pattern, exactly timed damage. */
public final class ApostleFangEntity extends EvokerFangs {
    private int visualTicks;
    public ApostleFangEntity(EntityType<? extends ApostleFangEntity> type, Level level) {
        super(type, level);
        m_20225_(true); // One sound per wave, not forty overlapping sounds.
    }
    public static ApostleFangEntity spawn(Mob owner, Vec3 point, float yaw) {
        var fang = new ApostleFangEntity(ApollyonEntityRegistry.APOSTLE_FANG.get(), owner.m_9236_());
        fang.m_36938_(owner);
        fang.m_6034_(point.f_82479_, point.f_82480_, point.f_82481_);
        fang.m_146922_(yaw);
        return owner.m_9236_().m_7967_(fang) ? fang : null;
    }
    @Override public void m_8119_() {
        if (m_9236_().f_46443_) {
            super.m_8119_();
            return;
        }
        m_6075_();
        var owner = m_19749_();
        if (owner == null || !owner.m_6084_()) { m_146870_(); return; }
        // Do not call EvokerFangs.tick on the server: its private damage method would
        // add a second, smaller hitbox and a fixed six damage on top of this spell.
        if (++visualTicks == 1) m_9236_().m_7605_(this, (byte) 4);
        if (visualTicks >= 23) m_146870_();
    }
    @Override public Packet<ClientGamePacketListener> m_5654_() { return NetworkHooks.getEntitySpawningPacket(this); }
}
