package com.starfantasy.goety.combat.apostle;

import com.starfantasy.goety.entity.ApostleServantEntity;
import com.Polarice3.Goety.common.entities.hostile.cultists.SpellCastingCultist.SpellType;
import com.Polarice3.Goety.init.ModSounds;
import com.starfantasy.goety.entity.ApollyonCastingLightningEntity;
import com.starfantasy.goety.combat.VoidRayKnockback;
import com.starfantasy.goety.entity.ApollyonFamineWaveEntity;
import com.starfantasy.goety.entity.ApollyonGloriousSphereEntity;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/** Independent title spell timeline occupying only the native fireball/Damned slot. */
public final class ApostleServantSpell {
    private final ApostleServantEntity boss;
    private int title, ticks, duration;
    private boolean second;
    private Vec3 anchor;
    private List<Vec3> points = List.of();
    private ApostleBeamSpell beam;
    private ApostleFangSpell fangs;

    public ApostleServantSpell(ApostleServantEntity boss) { this.boss = boss; }

    public static int duration(int title, boolean second) {
        return switch (title) {
            case 0 -> second ? 60 : 40;
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

    private boolean active;
    public boolean active() { return active; }
    public void start() {
        active = true;

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

        boss.m_21573_().m_26573_();
        boss.m_5496_(ModSounds.APOSTLE_PREPARE_SPELL.get(), 2, 1);
        if (title == 9) ApollyonCastingLightningEntity.spawn(boss, duration);
    }

    public void tick() {
        if (!active) return;
        LivingEntity target = boss.m_5448_();
        if (target == null || !target.m_6084_()) { stop(); return; }
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
            case 0 -> risen(target);
            default -> { }
        }
        if(ticks>=duration) stop();
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
        if(title==0) { ApostleServantNativeSpells.particles(boss,0); return; }
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
        var source = new net.minecraft.world.damagesource.DamageSource(
                boss.m_269291_().m_269064_().m_269150_(), boss);
        for (LivingEntity enemy : ApostleSpellSupport.enemies(boss, 20))
            enemy.m_6469_(source, enemy.m_21233_() * .1F + 10.0F);
    }
    private void glorious() {
        boss.m_5496_(ApollyonSoundRegistry.CAST_GLORIOUS.get(), 2, 1);
        StarFantasyVfx.horizontalRoarWave(boss, boss.m_20182_().m_82520_(0, .06, 0), .1, 10, 10);
        ApollyonGloriousSphereEntity.spawnForCaster(boss, boss.m_20182_().m_82520_(0, 1, 0));
        for (LivingEntity player : ApostleSpellSupport.enemies(boss, 10)) {
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

    public void stop() {
        if (!active) return;
        boolean completed=ticks>=duration;
        active=false;
        // Enhanced Apostles start their extra cooldown when the cast ends.
        // Native first-phase fireball has no extra interval, but waits for its 50-tick spell cycle.
        boss.startTitleSpellCooldown(title == 0 && !second ? 50 : ApostleSpellSupport.ENHANCED_SPELL_COOLDOWN);
        VoidRayKnockback.setCasting(boss,false);
        boss.setCasting(false);
        if(beam!=null) { beam.clear(); beam=null; }
        if(fangs!=null) { fangs.finish(completed); fangs=null; }
        if(!completed) { ApostleFireTrapManager.clearForBoss(boss); ApostleLightningStormManager.clearForBoss(boss); }
        else boss.m_5496_(ModSounds.APOSTLE_CAST_SPELL.get(),2,1);
        anchor=null; points=List.of();
    }
    private void risen(LivingEntity target) {
        if (!second && ticks==40) {
            var ball=new com.Polarice3.Goety.common.entities.projectiles.HellBlast(boss,
                    target.m_20185_()-boss.m_20185_(),target.m_20227_(.5)-boss.m_20227_(.5),target.m_20189_()-boss.m_20189_(),boss.m_9236_());
            // The projectile constructor uses the owner's feet. Aim and spawn must
            // use the same origin, otherwise its first collision is the floor.
            ball.m_6034_(boss.m_20185_(),boss.m_20227_(.5),boss.m_20189_());
            boss.m_9236_().m_7967_(ball);
            boss.m_9236_().m_5898_(null,1016,boss.m_20183_(),0);
        } else if(second && ticks<=30 && ticks%10==0) {
            var mob=new com.Polarice3.Goety.common.entities.hostile.servants.Damned(
                com.Polarice3.Goety.common.entities.ModEntityType.DAMNED.get(),boss.m_9236_());
            ServerLevel level=(ServerLevel)boss.m_9236_();
            var candidate=boss.m_20183_().m_7918_(boss.m_217043_().m_188503_(7)-3,0,boss.m_217043_().m_188503_(7)-3);
            var pos=com.Polarice3.Goety.utils.BlockFinder.SummonPosition(boss,candidate).m_6625_(2);
            mob.m_20035_(pos,boss.m_146908_(),boss.m_146909_());
            mob.setTrueOwner(boss);
            // Damned's spawn hook supplies its upward launch before the dive.
            mob.m_6518_(level,level.m_6436_(pos),net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED,null,null);
            mob.setLimitedLife(100); mob.m_6710_(target);
            com.Polarice3.Goety.utils.ServerParticleUtil.addParticlesAroundSelf(level,
                    com.Polarice3.Goety.client.particles.ModParticleTypes.BIG_FIRE.get(),mob);
            level.m_7967_(mob);
        }
    }
}
