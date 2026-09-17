package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.SpellCastingCultist.SpellType;
import com.Polarice3.Goety.init.ModSounds;
import com.starfantasy.goety.entity.ApollyonCastingLightningEntity;
import com.starfantasy.goety.combat.VoidRayKnockback;
import com.starfantasy.goety.entity.ApollyonFamineWaveEntity;
import com.starfantasy.goety.entity.ApollyonGloriousSphereEntity;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Independent title spell timeline occupying only the native fireball/Damned slot. */
public final class ApostleTitleSpellGoal extends Goal {
    private final Apostle boss;
    private int title, ticks, duration;
    private boolean second;
    private Vec3 anchor;
    private List<Vec3> points = List.of();
    private ApostleBeamSpell beam;
    private ApostleFangSpell fangs;

    public ApostleTitleSpellGoal(Apostle boss) { this.boss = boss; }

    public static int duration(int title, boolean second) {
        return switch (title) {
            case 1 -> 100;
            case 4 -> 80;
            case 5 -> second ? 80 : 60;
            case 9 -> second ? 100 : 91; // Last tracking warning resolves at tick 91.
            case 3 -> second ? 100 : 85; // First ray at 70, visible for 15 ticks.
            case 8 -> 115;
            case 2 -> second ? 90 : 70;
            case 11 -> second ? 90 : 60;
            default -> 60;
        };
    }

    @Override public boolean m_8036_() {
        LivingEntity target = boss.m_5448_();
        return ApostleSpellSupport.enhanced(boss) && target != null && target.m_6084_()
                && !ApostleSpellSupport.spellCoolingDown(boss)
                && boss.getSpellCycle() == 0 && !boss.isCasting() && !boss.isSpellcasting()
                && !boss.isSettingUpSecond() && !boss.aboutToShoot()
                && !((ApostleCastAccess) boss).starfantasy$teleportPending()
                && (!boss.isSecondPhase() || (boss.getCoolDown() >= boss.spellStart()
                    && boss.getDamnedCoolDown() <= 0))
                && boss.m_21574_().m_148306_(target);
    }

    @Override public boolean m_8045_() {
        return ApostleSpellSupport.original(boss)
                && ticks < duration && boss.m_6084_() && !boss.isSettingUpSecond()
                && boss.isSecondPhase() == second && boss.m_5448_() != null
                && boss.m_5448_().m_6084_();
    }
    @Override public boolean m_183429_() { return true; }

    @Override public void m_8056_() {
        title = boss.getTitleNumber();
        VoidRayKnockback.setCasting(boss, title == 3);
        second = boss.isSecondPhase();
        ticks = 0;
        duration = duration(title, second);
        anchor = null;
        points = List.of();
        if (title == 1) beam = new ApostleBeamSpell(boss);
        if (title == 5) fangs = new ApostleFangSpell(boss);
        boss.m_5810_();
        boss.m_21557_(false);
        boss.setCasting(true);
        ((ApostleCastAccess) boss).starfantasy$castTimer(duration + 1);
        boss.setSpellType(title == 6 ? SpellType.FIRE : SpellType.RANGED);
        boss.m_21573_().m_26573_();
        boss.m_5496_(ModSounds.APOSTLE_PREPARE_SPELL.get(), 2, 1);
        if (title == 9) ApollyonCastingLightningEntity.spawn(boss, duration);
    }

    @Override public void m_8037_() {
        if (!ApostleSpellSupport.original(boss)) return;
        LivingEntity target = boss.m_5448_();
        if (target == null || !target.m_6084_()) return;
        boss.m_21573_().m_26573_();
        boss.m_21563_().m_24960_(target, 30, 30);
        if (title == 3) boss.m_20256_(new Vec3(0, boss.m_20184_().f_82480_, 0));
        castingParticles();
        ++ticks;
        switch (title) {
            case 5 -> fangs.tick(ticks, second, target);
            case 1 -> beam.tick(ticks, second, target);
            case 4 -> {
                int interval = second ? 2 : 4;
                if (ticks <= 40 && (ticks - 1) % interval == 0)
                    ApostleMeteorSpell.spawn(boss, target, ticks == 1);
            }
            case 6 -> { if (ticks == 40) ApostleFireTrapManager.cast(boss, target); }
            case 9 -> lightning(target);
            case 3 -> rays();
            case 8 -> {
                if (ticks >= 41 && ticks <= 71 && (ticks - 41) % 10 == 0) {
                    int index = (ticks - 41) / 10;
                    if (second || index == 0 || index == 2)
                        ApostleFrostImpactManager.spawnChunk(boss, target, index);
                }
            }
            case 2, 11 -> wild(target);
            case 7 -> { if (ticks == 60) famine(); }
            case 10 -> { if (ticks == 60) glorious(); }
            default -> { }
        }
    }

    private void lightning(LivingEntity target) {
        int age = ticks - 40;
        if (age > 0 && age <= 30 && age % 5 == 0)
            ApostleLightningStormManager.queueTrackingStrike(boss, target);
        if (!second) return;
        if (age == 30) {
            anchor = ApostleLightningStormManager.captureAnchor(boss, target);
            ApostleLightningStormManager.queueRing(boss, anchor, 24, 24);
        } else if (age == 40) ApostleLightningStormManager.queueRing(boss, anchor, 16, 16);
        else if (age == 50) ApostleLightningStormManager.queueRing(boss, anchor, 8, 8);
        else if (age == 60) ApostleLightningStormManager.queueCenter(boss, anchor);
    }

    private void castingParticles() {
        if (title == 9 || (title == 3 && ticks >= 40)) return;
        var hand = ApostleSpellSupport.castingHand(boss);
        net.minecraft.core.particles.ParticleOptions particle;
        double x, y, z;
        if (title == 6 || title == 8) {
            particle = title == 6 ? com.Polarice3.Goety.client.particles.ModParticleTypes.BIG_FIRE.get()
                    : com.Polarice3.Goety.client.particles.ModParticleTypes.FROST.get();
            double azimuth = boss.m_217043_().m_188500_() * Math.PI * 2;
            double elevation = boss.m_217043_().m_188500_() * Math.PI * .5;
            double speed = .14 + boss.m_217043_().m_188500_() * .08;
            x = Math.cos(azimuth) * Math.cos(elevation) * speed;
            y = Math.sin(elevation) * speed;
            z = Math.sin(azimuth) * Math.cos(elevation) * speed;
        } else {
            particle = com.Polarice3.Goety.client.particles.ModParticleTypes.BIG_CULT_SPELL.get();
            x = title == 3 ? .65 : .45; y = title == 3 ? .15 : 1; z = title == 3 ? .9 : .45;
            if (title == 1) { x = .65; y = .15; z = .9; }
            if (title == 5) { x = .5; y = .5; z = .5; }
            if (title == 4) {
                int color = net.minecraft.util.Mth.m_14169_((boss.f_19797_ % 40) / 40F, .8F, 1);
                x = ((color >> 16) & 255) / 255D;
                y = ((color >> 8) & 255) / 255D;
                z = (color & 255) / 255D;
            }
            if (title == 10) { x = 1; y = .82; z = .12; }
        }
        ((ServerLevel) boss.m_9236_()).m_8767_(particle,
                hand.f_82479_, hand.f_82480_, hand.f_82481_, 0, x, y, z, 1);
    }

    private void rays() {
        if (ticks == 40) {
            anchor = ApostleVoidRayManager.captureAnchor(boss);
            ApostleVoidRayManager.warn(boss, anchor, 0, 30);
        } else if (ticks == 70) {
            ApostleVoidRayManager.detonate(boss, anchor, 0);
            if (second) ApostleVoidRayManager.warn(boss, anchor, 15, 20);
        } else if (ticks == 90 && second) ApostleVoidRayManager.detonate(boss, anchor, 15);
    }

    private void wild(LivingEntity target) {
        boolean thorns = title == 2;
        int firstHit = thorns ? 70 : 60;
        if (ticks == 40) {
            anchor = ApostleWildSurgeManager.captureAnchor(boss, target);
            points = thorns && !second ? ApostleWildSurgeManager.thornRingPoints(boss, anchor)
                    : ApostleWildSurgeManager.earthRingPoints(boss, anchor);
            ApostleWildSurgeManager.warnPoints(boss, points, thorns, firstHit - 40);
        } else if (ticks == firstHit) {
            spawnWild(thorns);
            if (second) {
                points = ApostleWildSurgeManager.thornRingPoints(boss, anchor);
                ApostleWildSurgeManager.warnPoints(boss, points, thorns, 90 - firstHit);
            }
        } else if (ticks == 90 && second) spawnWild(thorns);
    }
    private void spawnWild(boolean thorns) {
        if (thorns) ApostleWildSurgeManager.spawnThornRings(boss, points);
        else ApostleWildSurgeManager.spawnEarthPoints(boss, points);
    }

    private List<ServerPlayer> players(double range) {
        return ((ServerLevel) boss.m_9236_()).m_45976_(ServerPlayer.class,
                boss.m_20191_().m_82400_(range)).stream()
                .filter(p -> p.m_6084_() && !p.m_7500_() && !p.m_5833_()
                        && p.m_20280_(boss) <= range * range).toList();
    }
    private void famine() {
        ApollyonFamineWaveEntity.spawnForCaster(boss, boss.m_20182_().m_82520_(0, .04, 0));
        boss.m_5496_(ApollyonSoundRegistry.CAST_PROFANE.get(), 3, 1);
        ServerLevel level = (ServerLevel) boss.m_9236_();
        for (int i = 0; i < 48; i++) {
            double angle = boss.m_217043_().m_188500_() * Math.PI * 2;
            double speed = .16 + boss.m_217043_().m_188500_() * .28;
            double radius = boss.m_217043_().m_188500_() * 1.5;
            level.m_8767_(ApollyonParticleRegistry.PROFANE_SPELL.get(),
                    boss.m_20185_() + Math.cos(angle) * radius,
                    boss.m_20186_() + .25 + boss.m_217043_().m_188500_() * 1.5,
                    boss.m_20189_() + Math.sin(angle) * radius, 0,
                    Math.cos(angle) * speed, .15 + boss.m_217043_().m_188500_() * .3,
                    Math.sin(angle) * speed, 1);
        }
        for (ServerPlayer player : players(20)) {
            player.m_36324_().m_38717_(0);
            int food = player.m_36324_().m_38702_();
            player.m_36324_().m_38705_(food - food / 2);
            player.m_147207_(new MobEffectInstance(ApollyonEffectRegistry.FAMINE.get(),
                    second ? 200 : 100, 0, false, false, true), boss);
        }
    }
    private void glorious() {
        boss.m_5496_(ApollyonSoundRegistry.CAST_GLORIOUS.get(), 2, 1);
        StarFantasyVfx.horizontalRoarWave(boss, boss.m_20182_().m_82520_(0, .06, 0), .1, 10, 10);
        ApollyonGloriousSphereEntity.spawnForCaster(boss, boss.m_20182_().m_82520_(0, 1, 0));
        for (ServerPlayer player : players(10)) {
            Vec3 delta = player.m_20182_().m_82546_(boss.m_20182_());
            double length = Math.sqrt(delta.f_82479_ * delta.f_82479_ + delta.f_82481_ * delta.f_82481_);
            double dx = length > 1.0E-7 ? delta.f_82479_ / length : 1;
            double dz = length > 1.0E-7 ? delta.f_82481_ / length : 0;
            player.m_20256_(new Vec3(dx * 3, .55, dz * 3));
            player.f_19812_ = true;
            player.f_19864_ = true;
        }
        if (second) ApostleShield.set(boss, boss.m_21233_() * .2F);
    }

    @Override public void m_8041_() {
        VoidRayKnockback.setCasting(boss, false);
        if (!ApostleSpellSupport.original(boss)) {
            if (beam != null) { beam.clear(); beam = null; }
            if (fangs != null) { fangs.finish(false); fangs = null; }
            ApostleFireTrapManager.clearForBoss(boss);
            ApostleLightningStormManager.clearForBoss(boss);
            anchor = null;
            points = List.of();
            return;
        }
        boolean completed = ticks >= duration;
        boss.setCasting(false);
        boss.setSpellType(SpellType.NONE);
        ((ApostleCastAccess) boss).starfantasy$castTimer(0);
        boss.resetCoolDown();
        boss.setSpellCycle(1);
        if (second) boss.setDamnedCoolDown(200);
        ApostleSpellSupport.startSpellCooldown(boss);
        if (beam != null) { beam.clear(); beam = null; }
        if (fangs != null) { fangs.finish(completed); fangs = null; }
        if (completed) boss.m_5496_(ModSounds.APOSTLE_CAST_SPELL.get(), 2, 1);
        else {
            ApostleFireTrapManager.clearForBoss(boss);
            ApostleLightningStormManager.clearForBoss(boss);
        }
        anchor = null;
        points = List.of();
    }
}
