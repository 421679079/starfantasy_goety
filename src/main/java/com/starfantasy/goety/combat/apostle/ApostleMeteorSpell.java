package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.starfantasy.goety.entity.ApostleMeteorEntity;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ApostleMeteorSpell {
    private ApostleMeteorSpell() {}
    public static void spawn(Apostle boss, LivingEntity target, boolean centered) {
        double maxRadius = boss.isSecondPhase() ? 14 : 10;
        double radius = centered ? 0 : Math.sqrt(4 + boss.m_217043_().m_188500_() * (maxRadius * maxRadius - 4));
        double angle = boss.m_217043_().m_188500_() * Math.PI * 2;
        double x = target.m_20185_() + Math.cos(angle) * radius;
        double z = target.m_20189_() + Math.sin(angle) * radius;
        double y = target.m_20186_();
        var level = boss.m_9236_();
        // Search near the target's floor, not from the Nether ceiling above the arena.
        for (int by = Math.min(level.m_151558_() - 1, Mth.m_14107_(y) + 3);
                by >= Math.max(level.m_141937_() + 1, Mth.m_14107_(y) - 16); by--) {
            var feet = new BlockPos(Mth.m_14107_(x), by, Mth.m_14107_(z));
            var support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) { y = by; break; }
        }
        Vec3 impact = new Vec3(x, y + .06, z);
        var meteor = new ApostleMeteorEntity(level, boss);
        meteor.m_6034_(x, impact.f_82480_ + 20, z);
        meteor.configureMeteor(impact);
        if (level.m_7967_(meteor)) StarFantasyVfx.redGroundWarningCircle(boss, impact,
                ApostleMeteorEntity.METEOR_FALL_TICKS, ApostleMeteorEntity.explosionRadius());
    }
}
