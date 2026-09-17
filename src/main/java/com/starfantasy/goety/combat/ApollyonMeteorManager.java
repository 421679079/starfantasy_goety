package com.starfantasy.goety.combat;

import net.minecraft.world.entity.Mob;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.entity.ApollyonStarArrowEntity;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Spawns independent star-arrow meteors while Apollyon has an active target. */
public final class ApollyonMeteorManager {
    private static final double IMPACT_DISTANCE_MIN = 12.0D;
    private static final double IMPACT_DISTANCE_MAX = 18.0D;
    private static final double SPAWN_HEIGHT = 20.0D;
    private static final int WARNING_TICKS = ApollyonStarArrowEntity.METEOR_FALL_TICKS;

    private ApollyonMeteorManager() {
    }

    public static void spawn(Mob boss) {
        if (boss == null || !boss.m_6084_()
                || !(boss.m_9236_() instanceof ServerLevel level)) {
            return;
        }

        Vec3 impact = randomImpactAroundBoss(boss);
        Vec3 start = impact.m_82520_(0.0D, SPAWN_HEIGHT, 0.0D);
        ApollyonStarArrowEntity meteor = new ApollyonStarArrowEntity(level, boss);
        meteor.m_6034_(start.f_82479_, start.f_82480_, start.f_82481_);
        meteor.configureMeteor(impact);
        level.m_7967_(meteor);
        if (ApollyonSpellSupport.warnings(boss)) {
            StarFantasyVfx.redGroundWarningCircle(
                boss,
                impact.m_82520_(0.0D, 0.06D, 0.0D),
                WARNING_TICKS,
                ApollyonStarArrowEntity.explosionRadius());
        }
    }

    private static Vec3 randomImpactAroundBoss(Mob boss) {
        RandomSource random = boss.m_217043_();
        boolean hardMode = ApollyonSpellSupport.warnings(boss) && ApollyonConfig.hardMode();
        Vec3 center = hardMode ? ApollyonSpellSupport.home(boss) : boss.m_20182_();
        double distance = Mth.m_14139_(random.m_188500_(),
                hardMode ? 2.0D : IMPACT_DISTANCE_MIN, IMPACT_DISTANCE_MAX);
        double angle = random.m_188500_() * Math.PI * 2.0D;
        double x = center.f_82479_ + Math.cos(angle) * distance;
        double z = center.f_82481_ + Math.sin(angle) * distance;
        return groundCenterAt(
                boss.m_9236_(),
                x,
                center.f_82480_ + SPAWN_HEIGHT,
                z,
                Mth.m_14107_(x),
                Mth.m_14107_(z));
    }

    private static Vec3 groundCenterAt(Level level, double preciseX, double searchY,
                                       double preciseZ, int blockX, int blockZ) {
        int startY = Math.min(level.m_151558_() - 1, Mth.m_14107_(searchY));
        int minY = level.m_141937_() + 1;
        for (int y = startY; y >= minY; --y) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos support = feet.m_7495_();
            if (level.m_8055_(support).m_60783_(level, support, Direction.UP)
                    && !level.m_8055_(feet).m_280555_()) {
                return new Vec3(preciseX, y + 0.06D, preciseZ);
            }
        }
        return new Vec3(preciseX, searchY + 0.06D, preciseZ);
    }
}
