package com.starfantasy.goety.combat.apostle;

import net.minecraft.world.entity.Mob;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SPlayLoopSoundPacket;
import com.Polarice3.Goety.init.ModSounds;
import com.starfantasy.goety.entity.ApostleBeamEntity;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

/** 40 warning ticks followed by 60 beam ticks; one damage gate shared by all four rays. */
public final class ApostleBeamSpell {
    private final Mob boss;
    private final List<ApostleBeamEntity> beams = new ArrayList<>();
    private final List<Entity> warnings = new ArrayList<>();
    private final Map<UUID, Integer> nextHit = new HashMap<>();
    private float yaw;
    public ApostleBeamSpell(Mob boss) { this.boss = boss; }
    public static float angularSpeed(int beamTick) {
        return 1 + Math.min(1, Math.max(0, (beamTick - 1) / 39F));
    }
    public static float track(float yaw, float targetYaw, float speed) {
        return Mth.m_14177_(yaw + Mth.m_14036_(Mth.m_14177_(targetYaw - yaw), -speed, speed));
    }
    private float targetYaw(LivingEntity target) {
        return (float) Math.toDegrees(Math.atan2(-(target.m_20185_() - boss.m_20185_()), target.m_20189_() - boss.m_20189_()));
    }
    public void tick(int tick, boolean second, LivingEntity target) {
        if (tick == 1) {
            yaw = targetYaw(target);
            for (int i = 0; i < (second ? 4 : 1); i++) {
                var beam = ApostleBeamEntity.spawn(boss, yaw + i * 90);
                if (beam == null) continue;
                beams.add(beam);
                var warning = ApostleSpellSupport.showWarnings(boss) ? StarFantasyVfx.groundRectangleWarningTrackingGround(beam, 40,
                        ApostleBeamEntity.HALF_WIDTH * 2, ApostleBeamEntity.LENGTH, 0, 0xFF0000, false, true) : null;
                if (warning != null) warnings.add(warning);
            }
        } else {
            float speed = tick <= 40 ? 1 : angularSpeed(tick - 40);
            yaw = second ? Mth.m_14177_(yaw + speed) : track(yaw, targetYaw(target), speed);
        }
        for (int i = 0; i < beams.size(); i++) beams.get(i).update(boss, yaw + i * 90, tick);
        if (tick == 41) {
            for (Entity warning : warnings) warning.m_146870_();
            warnings.clear();
            if (!beams.isEmpty()) ModNetwork.sentToTrackingEntity(beams.get(0),
                    new SPlayLoopSoundPacket(beams.get(0), ModSounds.CORRUPT_BEAM_LOOP.get(), 2, 1));
        }
        if (tick <= 40) return;
        for (LivingEntity player : boss.m_9236_().m_45976_(LivingEntity.class, boss.m_20191_().m_82400_(50))) {
            if (!ApostleSpellSupport.canAffect(boss, player)
                    || tick < nextHit.getOrDefault(player.m_20148_(), 0)) continue;
            for (int i = 0; i < beams.size(); i++) {
                if (intersects(player, boss, yaw + i * 90)) {
                    if (player.m_6469_(boss.m_269291_().m_269104_(beams.get(i), boss), ApostleSpellSupport.damage(3)))
                        nextHit.put(player.m_20148_(), tick + 10);
                    break;
                }
            }
        }
    }
    public static boolean intersects(LivingEntity target, Mob boss, float yaw) {
        double angle = Math.toRadians(yaw), fx = -Math.sin(angle), fz = Math.cos(angle);
        var box = target.m_20191_();
        double halfLength = ApostleBeamEntity.LENGTH / 2D;
        double ex = (box.f_82291_ - box.f_82288_) / 2D, ez = (box.f_82293_ - box.f_82290_) / 2D;
        double dx = (box.f_82288_ + box.f_82291_) / 2D - boss.m_20185_() - fx * halfLength;
        double dz = (box.f_82290_ + box.f_82293_) / 2D - boss.m_20189_() - fz * halfLength;
        double width = ApostleBeamEntity.HALF_WIDTH;
        // Four separating axes: the beam's rectangle and the target's actual AABB.
        // A circular width approximation misses wide mobs at diagonal beam angles.
        return Math.abs(dx) <= ex + Math.abs(fx) * halfLength + Math.abs(fz) * width
                && Math.abs(dz) <= ez + Math.abs(fz) * halfLength + Math.abs(fx) * width
                && Math.abs(dx * fx + dz * fz) <= halfLength + Math.abs(fx) * ex + Math.abs(fz) * ez
                && Math.abs(-dx * fz + dz * fx) <= width + Math.abs(fz) * ex + Math.abs(fx) * ez
                && box.f_82292_ >= boss.m_20186_() + ApostleBeamEntity.CENTER_HEIGHT - ApostleBeamEntity.HALF_HEIGHT
                && box.f_82289_ <= boss.m_20186_() + ApostleBeamEntity.CENTER_HEIGHT + ApostleBeamEntity.HALF_HEIGHT;
    }
    public void clear() {
        beams.forEach(Entity::m_146870_);
        warnings.forEach(Entity::m_146870_);
        beams.clear(); warnings.clear(); nextHit.clear();
    }
}
