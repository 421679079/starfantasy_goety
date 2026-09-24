package com.starfantasy.goety.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

/** Shared lava movement for the two permanent Apostle servants. */
public final class ServantMobility {
    private ServantMobility() { }

    public static void configure(Mob servant) {
        servant.m_21441_(BlockPathTypes.LAVA, 0.0F);
        servant.m_21441_(BlockPathTypes.DANGER_FIRE, 0.0F);
        servant.m_21441_(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    public static boolean canStandOn(FluidState fluid) {
        return fluid.m_205070_(FluidTags.f_13132_);
    }

    public static void floatInLava(Mob servant) {
        if (!servant.m_6084_() || !servant.m_20077_()) return;
        BlockPos feet = servant.m_20183_();
        FluidState fluid = servant.m_9236_().m_6425_(feet);
        if (!canStandOn(fluid)) return;
        Vec3 velocity = servant.m_20184_();
        double surface = feet.m_123342_() + fluid.m_76155_(servant.m_9236_(), feet);
        double depth = surface - servant.m_20186_();
        if (depth <= .02D) {
            servant.m_6853_(true);
            servant.m_20256_(new Vec3(velocity.f_82479_, Math.max(0, velocity.f_82480_), velocity.f_82481_));
        } else {
            double rise = Math.min(.12D, Math.max(.04D, depth * .1D));
            servant.m_20256_(new Vec3(velocity.f_82479_ * .5D, rise, velocity.f_82481_ * .5D));
        }
        servant.f_19789_ = 0;
    }

}
