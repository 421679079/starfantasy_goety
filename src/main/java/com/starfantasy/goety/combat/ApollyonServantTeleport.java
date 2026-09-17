package com.starfantasy.goety.combat;

import com.Polarice3.Goety.client.particles.AbsorbTrailParticleOption;
import com.Polarice3.Goety.config.MobsConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Apollyon's combat teleport without arena/home behavior. Owner following remains Goety's job. */
public final class ApollyonServantTeleport {
    private final ApollyonServantEntity servant;
    private int cooldown, windup, receivedHits;
    private Vec3 pending;

    public ApollyonServantTeleport(ApollyonServantEntity servant) { this.servant = servant; }
    public boolean isPending() { return pending != null; }
    public void clear() { pending = null; windup = 0; }
    public void reset() { clear(); cooldown = 0; receivedHits = 0; }

    private boolean canAct(LivingEntity target) {
        return servant.m_9236_() instanceof ServerLevel && servant.m_6084_()
                && !servant.isStaying() && !servant.isCastingAction()
                && !servant.m_20159_() && !servant.m_20160_()
                && target != null && target.m_6084_() && target.m_9236_() == servant.m_9236_()
                && !servant.isFriendlyEntity(target);
    }

    public void onHurt() {
        if (servant.m_9236_() instanceof ServerLevel && ++receivedHits >= 4) {
            receivedHits = 0;
            tryTeleport(servant.m_5448_());
        }
    }

    public void onShot(LivingEntity target) {
        if (servant.m_217043_().m_188503_(4) == 0) tryTeleport(target);
    }

    public void tick(LivingEntity target) {
        if (cooldown > 0) --cooldown;
        if (!canAct(target)) { clear(); return; }
        ServerLevel level = (ServerLevel) servant.m_9236_();
        if (pending != null) {
            if (!Boolean.TRUE.equals(MobsConfig.ApostleDelayedTeleport.get()) || ++windup >= 20) {
                Vec3 destination = pending;
                clear();
                finish(level, destination);
            } else windupParticles(level, pending);
        } else if (cooldown <= 0 && (servant.m_20280_(target) > 1024.0D
                || !servant.m_21574_().m_148306_(target))) tryTeleport(target);
    }

    private boolean tryTeleport(LivingEntity target) {
        if (!canAct(target) || cooldown > 0 || pending != null) return false;
        ServerLevel level = (ServerLevel) servant.m_9236_();
        Vec3 destination = null;
        for (int attempt = 0; attempt < 16 && destination == null; ++attempt) {
            double x = target.m_20185_() + (servant.m_217043_().m_188500_() - 0.5D) * 24.0D;
            double y = target.m_20186_() + servant.m_217043_().m_188503_(9) - 4;
            double z = target.m_20189_() + (servant.m_217043_().m_188500_() - 0.5D) * 24.0D;
            destination = ground(level, x, y, z);
        }
        // An ordinary world has no arena home to fall back to. Retry later if every position is unsafe.
        if (destination == null) { cooldown = 20; return false; }
        servant.m_5496_((SoundEvent) ModSounds.APOSTLE_PRE_TELEPORT.get(), 2.0F, 1.0F);
        if (Boolean.TRUE.equals(MobsConfig.ApostleDelayedTeleport.get())) {
            pending = destination;
            windup = 0;
            return true;
        }
        return finish(level, destination);
    }

    private Vec3 ground(ServerLevel level, double x, double y, double z) {
        BlockPos cursor = BlockPos.m_274561_(x, y, z);
        if (!level.m_46805_(cursor) || !level.m_6857_().m_61937_(cursor)) return null;
        while (cursor.m_123342_() > level.m_141937_()) {
            BlockPos below = cursor.m_7495_();
            if (level.m_8055_(below).m_280555_()) {
                // Use the block's top rather than retaining an arbitrary fractional Y.
                Vec3 destination = new Vec3(x, cursor.m_123342_(), z);
                return safe(level, destination) ? destination : null;
            }
            cursor = below;
        }
        return null;
    }

    private boolean safe(ServerLevel level, Vec3 destination) {
        BlockPos pos = BlockPos.m_274561_(destination.f_82479_, destination.f_82480_, destination.f_82481_);
        AABB bounds = servant.m_20191_().m_82386_(destination.f_82479_ - servant.m_20185_(),
                destination.f_82480_ - servant.m_20186_(), destination.f_82481_ - servant.m_20189_());
        return level.m_46805_(pos) && level.m_6857_().m_61937_(pos)
                && level.m_45756_(servant, bounds) && !level.m_46855_(bounds);
    }

    private boolean finish(ServerLevel level, Vec3 destination) {
        Vec3 old = servant.m_20182_();
        if (!safe(level, destination) || !servant.m_20984_(
                destination.f_82479_, destination.f_82480_, destination.f_82481_, true)) {
            cooldown = 20;
            return false;
        }
        servant.m_21573_().m_26573_();
        servant.m_20256_(Vec3.f_82478_);
        servant.f_19789_ = 0;
        particles(level, old, 0);
        particles(level, servant.m_20182_(), servant.m_20206_() * 0.5D);
        servant.m_5496_((SoundEvent) ModSounds.APOSTLE_TELEPORT.get(), 2.0F, 1.0F);
        cooldown = 80;
        return true;
    }

    private void particles(ServerLevel level, Vec3 position, double height) {
        level.m_8767_(ParticleTypes.f_123755_, position.f_82479_, position.f_82480_ + height,
                position.f_82481_, 32, 0.8D, 1.4D, 0.8D, 0.04D);
    }

    private void windupParticles(ServerLevel level, Vec3 destination) {
        ServerParticleUtil.addParticlesAroundMiddleSelf(level, ParticleTypes.f_123762_, servant);
        level.m_8767_(ParticleTypes.f_123755_, destination.f_82479_, destination.f_82480_ + 0.25D,
                destination.f_82481_, 1, 0, 0, 0, 0);
        ServerParticleUtil.windParticle(level, ColorUtil.BLACK, 2.0F, 0.25F, -1, destination);
        for (int i = 0; i < 16; ++i) {
            Vec3 trail = destination.m_82520_(
                    servant.m_20205_() * (2 * servant.m_217043_().m_188500_() - 1) * 0.5D,
                    servant.m_20206_() * servant.m_217043_().m_188500_(),
                    servant.m_20205_() * (2 * servant.m_217043_().m_188500_() - 1) * 0.5D);
            level.m_8767_(new AbsorbTrailParticleOption(trail, 0xFF7700, 10), servant.m_20208_(0.5D),
                    servant.m_20187_(), servant.m_20262_(0.5D), 1, 0, 0, 0, 1);
        }
    }
}
