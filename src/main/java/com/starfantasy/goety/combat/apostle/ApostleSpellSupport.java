package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.config.AttributesConfig;
import com.starfantasy.goety.config.ApostleConfig;
import com.starfantasy.goety.compat.ApostleCompatibility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public final class ApostleSpellSupport {
    public static final int ENHANCED_SPELL_COOLDOWN = 200;
    private static final String SPELL_READY = "StarFantasyApostleSpellReady";
    private ApostleSpellSupport() {}
    public static boolean original(Apostle boss) { return ApostleCompatibility.original(boss); }
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
        boss.getPersistentData().m_128356_(SPELL_READY, boss.m_9236_().m_46467_() + ENHANCED_SPELL_COOLDOWN);
    }
    public static boolean casting(net.minecraft.world.entity.Mob caster) {
        return caster instanceof Apostle apostle ? apostle.isCasting()
            : caster instanceof com.starfantasy.goety.entity.ApostleServantEntity servant && servant.isCasting();
    }
    public static boolean transitioning(net.minecraft.world.entity.Mob caster) {
        return caster instanceof Apostle apostle ? apostle.isSettingUpSecond()
            : caster instanceof com.starfantasy.goety.entity.ApostleServantEntity servant && servant.isSettingUpSecond();
    }
    public static boolean showWarnings(net.minecraft.world.entity.Entity caster) {
        return !(caster instanceof com.starfantasy.goety.entity.ApostleServantEntity);
    }
    public static boolean secondPhase(net.minecraft.world.entity.Mob caster) {
        return caster instanceof Apostle apostle ? apostle.isSecondPhase()
                : caster instanceof com.starfantasy.goety.entity.ApostleServantEntity servant && servant.isSecondPhase();
    }
    public static float damage(double multiplier) {
        return (float) (AttributesConfig.ApostleMagicDamage.get() * multiplier);
    }
    public static boolean friendly(net.minecraft.world.entity.Mob boss, LivingEntity target) {
        return boss == target || boss.m_7307_(target);
    }
    public static boolean canAffect(net.minecraft.world.entity.Mob caster, LivingEntity target) {
        return target.m_6084_() && !friendly(caster, target) && !target.m_5833_()
                && !(target instanceof net.minecraft.world.entity.player.Player player && player.m_7500_());
    }
    public static java.util.List<LivingEntity> enemies(net.minecraft.world.entity.Mob caster, double range) {
        return caster.m_9236_().m_45976_(LivingEntity.class, caster.m_20191_().m_82400_(range)).stream()
                .filter(target -> canAffect(caster, target) && target.m_20280_(caster) <= range * range).toList();
    }
    public static Vec3 castingHand(net.minecraft.world.entity.Mob boss) {
        float angle = boss.f_20883_ * ((float) Math.PI / 180)
                + Mth.m_14089_(boss.f_19797_ * .6662F) * .25F;
        double side = boss.m_5737_() == HumanoidArm.RIGHT ? 1 : -1;
        return new Vec3(boss.m_20185_() + Mth.m_14089_(angle) * .6 * side,
                boss.m_20186_() + 1.8, boss.m_20189_() + Mth.m_14031_(angle) * .6 * side);
    }
}
