package com.starfantasy.goety.combat;

import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonIceChunkEntity;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Timelines and custom damage ownership for Apollyon's frost impact spell. */
public final class ApollyonFrostImpactManager {
    private static final double SPAWN_HEIGHT = 2.0D;
    private static final double[][] CARDINAL_OFFSETS = {
            {4.0D, 0.0D},
            {0.0D, 4.0D},
            {-4.0D, 0.0D},
            {0.0D, -4.0D}
    };

    private ApollyonFrostImpactManager() {
    }

    public static void spawnChunk(ApollyonEntity boss, LivingEntity target, int sequenceIndex) {
        if (boss == null || target == null || !boss.m_6084_() || !target.m_6084_()
                || boss.m_9236_().f_46443_) {
            return;
        }

        double[] offset = CARDINAL_OFFSETS[Math.floorMod(sequenceIndex, CARDINAL_OFFSETS.length)];
        Vec3 center = groundCenterAt(boss.m_9236_(),
                target.m_20185_() + offset[0],
                Math.max(target.m_20186_(), boss.m_20186_()) + 8.0D,
                target.m_20189_() + offset[1],
                Mth.m_14107_(target.m_20185_() + offset[0]),
                Mth.m_14107_(target.m_20189_() + offset[1]));

        ApollyonIceChunkEntity chunk = new ApollyonIceChunkEntity(
                ApollyonEntityRegistry.APOLLYON_ICE_CHUNK.get(), boss.m_9236_());
        chunk.m_6034_(center.f_82479_, center.f_82480_ + SPAWN_HEIGHT, center.f_82481_);
        chunk.initialize(boss, target, center.f_82480_);
        chunk.setExtraDamage(0.0F);
        chunk.m_20256_(Vec3.f_82478_);
        boss.m_9236_().m_7967_(chunk);
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
