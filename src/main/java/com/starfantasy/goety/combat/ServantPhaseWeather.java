package com.starfantasy.goety.combat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class ServantPhaseWeather {
    private ServantPhaseWeather() { }

    private static boolean allowed(LivingEntity servant) {
        if (servant instanceof com.starfantasy.goety.entity.ApollyonServantEntity)
            return com.starfantasy.goety.config.ServantConfig.APOLLYON_ALLOW_WEATHER_CHANGE.get();
        return servant instanceof com.starfantasy.goety.entity.ApostleServantEntity
                && com.starfantasy.goety.config.ServantConfig.APOSTLE_ALLOW_WEATHER_CHANGE.get();
    }

    /** Native Apostle storm duration, restricted to the Overworld. */
    public static void thunder(LivingEntity servant) {
        if (allowed(servant) && servant.m_9236_() instanceof ServerLevel level && level.m_46472_() == Level.f_46428_)
            level.m_8606_(0, 6000, true, true);
    }

    public static void death(LivingEntity servant) {
        if (allowed(servant) && servant.m_9236_() instanceof ServerLevel level && level.m_46472_() == Level.f_46428_)
            level.m_8606_(6000, 0, false, false);
    }
}
