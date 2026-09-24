package com.starfantasy.goety.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class ApollyonDamageRules {
    private ApollyonDamageRules() { }

    /** Vanilla /kill calls generic_kill with Float.MAX_VALUE. The type alone is not a command. */
    public static boolean commandKill(DamageSource source, float amount) {
        return source.m_276093_(DamageTypes.f_286979_) && amount >= Float.MAX_VALUE;
    }

    public static DamageSource ordinaryKill(LivingEntity victim, DamageSource source, float amount) {
        if (!source.m_276093_(DamageTypes.f_286979_) || commandKill(source,amount)) return source;
        var generic=victim.m_269291_().m_269264_().m_269150_();
        if (source.m_7640_()!=null || source.m_7639_()!=null)
            return new DamageSource(generic,source.m_7640_(),source.m_7639_());
        if (source.m_7270_()!=null) return new DamageSource(generic,source.m_7270_());
        return new DamageSource(generic);
    }
}
