package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.AttributesConfig;
import com.starfantasy.goety.config.ApostleConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public final class ApostleSpellSupport {
    private static final String SPELL_READY = "StarFantasyApostleSpellReady";
    private ApostleSpellSupport() {}
    public static boolean original(Apostle boss) { return boss.getClass() == Apostle.class; }
    public static boolean enhanced(Apostle boss) {
        return original(boss) && ApostleConfig.ENHANCED_FIREBALL.get() && supported(boss.getTitleNumber());
    }
    public static boolean supported(int title) {
        return title == 1 || title == 2 || title == 3 || title == 4 || title == 5 || title == 6 || title == 7
                || title == 8 || title == 9 || title == 10 || title == 11;
    }
    public static boolean spellCoolingDown(Apostle boss) {
        return enhanced(boss)
                && boss.m_9236_().m_46467_() < boss.getPersistentData().m_128454_(SPELL_READY);
    }
    public static void startSpellCooldown(Apostle boss) {
        boss.getPersistentData().m_128356_(SPELL_READY, boss.m_9236_().m_46467_() + 200);
    }
    public static float damage(double multiplier) {
        return (float) (AttributesConfig.ApostleMagicDamage.get() * multiplier);
    }
    public static boolean friendly(Apostle boss, LivingEntity target) {
        return boss == target || boss.m_7307_(target);
    }
    public static Vec3 castingHand(Apostle boss) {
        float angle = boss.f_20883_ * ((float) Math.PI / 180)
                + Mth.m_14089_(boss.f_19797_ * .6662F) * .25F;
        double side = boss.m_5737_() == HumanoidArm.RIGHT ? 1 : -1;
        return new Vec3(boss.m_20185_() + Mth.m_14089_(angle) * .6 * side,
                boss.m_20186_() + 1.8, boss.m_20189_() + Mth.m_14031_(angle) * .6 * side);
    }
}
