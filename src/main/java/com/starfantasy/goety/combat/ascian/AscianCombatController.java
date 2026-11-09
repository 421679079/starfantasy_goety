package com.starfantasy.goety.combat.ascian;

import com.starfantasy.goety.config.AscianKingConfig;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.library.posture.MeleeClashWindow;
import com.starfantasy.library.posture.PostureHudSync;
import com.starfantasy.library.vfx.StarFantasyVfx;
import com.starfantasy.library.vfx.particle.WarningColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import java.util.*;

/** All counters are server ticks. The legacy AI advanced once every two ticks. */
public final class AscianCombatController {
    private static final int[] HIT_TICKS={15,31,55,78};
    private static final int STRAIGHT_BARRAGE=6,FAN_BARRAGE=7;
    // Action 4 selects either barrage variant; action 3 casts the current phase's spell.
    private static final int[][] CYCLES={
            {STRAIGHT_BARRAGE,FAN_BARRAGE,3,2,1},
            {3,FAN_BARRAGE,STRAIGHT_BARRAGE,2,4,1},
            {3,FAN_BARRAGE,STRAIGHT_BARRAGE,STRAIGHT_BARRAGE,2,1},
            {3,STRAIGHT_BARRAGE,FAN_BARRAGE,2,4,1},
            {3,2,STRAIGHT_BARRAGE,FAN_BARRAGE,1},
            {3,STRAIGHT_BARRAGE,FAN_BARRAGE,STRAIGHT_BARRAGE,2,1}
    };
    private static final float[] THRESHOLDS={1,.85F,.70F,.55F,.40F,.25F};
    private static final String[] TITLES={"业火焚世","绝对零度","万雷归寂","扭转万象","生者必灭","万物终结"};
    private static final BossEvent.BossBarColor[] COLORS={BossEvent.BossBarColor.YELLOW,BossEvent.BossBarColor.BLUE,BossEvent.BossBarColor.YELLOW,BossEvent.BossBarColor.PURPLE,BossEvent.BossBarColor.WHITE,BossEvent.BossBarColor.RED};
    public final AscianKingEntity boss;
    public final AscianSpells spells;
    private final MeleeClashWindow clash=new MeleeClashWindow(5);
    private int phase=1, transitionTarget, transitionAge, action, actionAge, actionLength;
    private int meleeCooldown, teleportCooldown, spellCooldown, idleCooldown, sequence;
    private int posture=-1, postureMaximum, stun, stunPositionTicks;
    private int lastGuard=Integer.MIN_VALUE, lastHit=Integer.MIN_VALUE, ambienceCooldown, ambienceSequence;
    private int guardableTick=Integer.MIN_VALUE, guardableSegment=-1, rewardedSegment=-1;
    private boolean stunOwnsAi, aiBeforeStun;
    private boolean forceSpell, meleeDamage;
    private boolean transitionRear;
    private int guardedHitTick=-1;
    private final Set<UUID> guardedPlayers=new HashSet<>();
    public AscianCombatController(AscianKingEntity boss) { this.boss=boss; spells=new AscianSpells(this); }
    public int phase() { return phase; }
    public int action() { return action; }
    public int actionAge() { return actionAge; }
    public boolean transitioning() { return transitionTarget>0; }
    public boolean stunned() { return stun>0; }
    public boolean meleeDamage() { return meleeDamage; }
    public boolean parryAllowed() {
        return !boss.m_9236_().f_46443_ && boss.m_6084_() && !stunned() && !transitioning() && action==2
                && guardableTick!=Integer.MIN_VALUE && boss.f_19797_-guardableTick<=2;
    }
    public int requiredGuards() { return (transitioning()?transitionTarget:phase)==6?AscianCombatTuning.FINAL_GUARDS:AscianCombatTuning.GUARDS; }
    public int posture() { ensurePosture(); return posture; }
    private void ensurePosture() {
        int maximum=requiredGuards();
        if(posture<0 || postureMaximum!=maximum) { postureMaximum=maximum; posture=stunned()?0:maximum; }
        posture=Math.max(0,Math.min(posture,maximum));
    }
    public boolean friendly(Entity entity) {
        if(entity==null) return false;
        if(entity==boss) return true;
        return entity instanceof com.Polarice3.Goety.common.entities.neutral.Owned owned && owned.getTrueOwner()==boss;
    }
    public boolean skip(LivingEntity target) {
        return !target.m_6084_() || friendly(target) || target.m_20147_()
                || target instanceof Player p && (p.m_7500_()||p.m_5833_());
    }
    public void tick() {
        long spellEpoch=spells.epoch();
        LivingEntity castTarget=boss.m_5448_();
        if(castTarget==null||skip(castTarget))spells.stopCasting();
        ensurePosture();
        if(transitioning()) {
            ++transitionAge;
            boss.m_20256_(new Vec3(0,boss.m_20184_().f_82480_,0));
            if(transitionAge==AscianCombatTuning.TRANSITION_HIT) {
                phase=transitionTarget;
                spells.transitionArea(boss.m_20182_(),phase,transitionRear);
                boss.m_5496_(SoundEvents.f_11913_,5,1);
            }
            int ring=AscianCombatTuning.transitionRingAt(transitionAge);
            if(ring>=0)spells.transitionRing(transitionTarget,ring);
            if(transitionAge>=AscianCombatTuning.TRANSITION_DURATION) { transitionTarget=0; forceSpell=true; idleCooldown=0; start(0,0); }
        } else if(stun>0) {
            tickPostureStun();
        } else {
            int computed=phaseFor(boss.m_21223_()/boss.m_21233_());
            if(computed>phase) { boss.setPhaseHealth(boss.m_21233_()*THRESHOLDS[phase]); beginTransition(phase+1); }
            else {
                if(computed<phase) {
                    phase=computed;
                    interruptAttack();
                    resetPosture();
                    forceSpell=false; idleCooldown=0;
                }
                tickAttack();
            }
        }
        // Existing field hazards and cast spells survive a posture break. Death clears everything.
        if(!boss.m_6084_()||spells.epoch()!=spellEpoch)return;
        spells.tick();
        if(!boss.m_6084_()||spells.epoch()!=spellEpoch)return;
        tickAmbience();
        if(boss.f_19797_%20==0) {
            refreshEffects();
            if(phase==6 && boss.m_21223_()<boss.m_21233_()*.25F)
                boss.m_5634_((float)Math.min(AscianKingConfig.FINAL_HEAL_PER_SECOND.get(),boss.m_21233_()*.25F-boss.m_21223_()));
            syncHud();
        }
        boss.bossBar.m_6456_(Component.m_237113_("无影之王 「"+TITLES[phase-1]+"」"));
        boss.bossBar.m_6451_(COLORS[phase-1]);
    }
    public boolean interceptHealthLoss(float loss) {
        if(phase>=6 || transitioning()) return false;
        float gate=boss.m_21233_()*THRESHOLDS[phase];
        if(boss.m_21223_()-loss>gate) return false;
        boss.setPhaseHealth(gate); // Fractional gates must also work when configured maximum health is 1.
        beginTransition(phase+1);
        return true;
    }
    private void beginTransition(int next) {
        transitionTarget=next; transitionAge=0;
        transitionRear=next==6||AscianKingConfig.HARD_MODE.get();
        interruptAttack();
        resetPosture();
        boss.combatHealthProtection().setInvulnerabilityTicks(0);
        clash.clear();
        start(1,AscianCombatTuning.TRANSITION_DURATION);
        WarningColor color=transitionRear?WarningColor.PURPLE:WarningColor.RED;
        if(transitionRear)StarFantasyVfx.purpleWarningRing(boss,AscianCombatTuning.TRANSITION_HIT,9,2.8);
        else StarFantasyVfx.redWarningRing(boss,AscianCombatTuning.TRANSITION_HIT,9,2.8);
        StarFantasyVfx.groundWarningCircleOwnedStatic(boss,AscianEffects.ground(boss,boss),AscianCombatTuning.TRANSITION_HIT,9,color);
        syncHud();
    }
    private void tickAttack() {
        LivingEntity target=boss.m_5448_();
        if(target==null || skip(target)) { if(action!=0) start(0,0); clash.clear(); sequence=0; return; }
        if(meleeCooldown>0) --meleeCooldown;
        if(teleportCooldown>0) --teleportCooldown;
        if(spellCooldown>0) --spellCooldown;
        if(idleCooldown>0) { --idleCooldown; return; }
        boss.m_21573_().m_26573_();
        face(target);
        if(action==0) {
            if(forceSpell)sequence=0;
            int chosen=CYCLES[phase-1][sequence];
            forceSpell=false;
            if(chosen==2) {
                if(boss.m_20182_().m_82546_(target.m_20182_()).m_82542_(1,0,1).m_82556_()>4)teleportNear(target,2);
                meleeCooldown=240; start(2,90); beginClash(0,0);
            }
            else if(chosen==3) { spellCooldown=300; start(3,60); }
            else if(chosen==4) { beginBarrage(target,boss.m_217043_().m_188499_()); }
            else if(chosen==STRAIGHT_BARRAGE||chosen==FAN_BARRAGE) { beginBarrage(target,chosen==FAN_BARRAGE); }
            else start(1,60);
            return; // Frame zero schedules warnings; frame one starts on the next server tick.
        }
        ++actionAge;
        if(action==1) {
            if(actionAge<20&&actionAge%4==0)teleportParticles(false);
            if(actionAge==26) spells.summon();
            if(actionAge==46) teleportAttack(target);
        } else if(action==2) {
            for(int i=0;i<4;i++) if(actionAge==HIT_TICKS[i]) {
                chase(target);
                var result=clash.consume("melee",i);
                if(result.clashed()) {
                    StarFantasyVfx.guardClash(boss,boss.m_20182_().m_82520_(0,2.8,0));
                    rewardMeleeGuard(result.playerId()==null?null:((ServerLevel)boss.m_9236_()).m_8791_(result.playerId()),i);
                } else meleeHit(target,i);
                if(action==2 && !stunned() && !transitioning() && i<3) beginClash(i+1,HIT_TICKS[i]);
                break;
            }
        } else if(action==3) {
            if(actionAge==10) boss.m_5496_(SoundEvents.f_12616_,1,1.5F);
            if(actionAge==30) spells.cast(phase,target);
        }
        if(action!=0 && actionAge>=actionLength) finish();
    }
    private void start(int next,int length) {
        action=next; actionAge=0; actionLength=length;
        guardableTick=Integer.MIN_VALUE; guardableSegment=-1; rewardedSegment=-1;
        boss.action(next==2?10:next);
    }
    private void beginBarrage(LivingEntity target,boolean fan) {
        // The selected mode owns the entire windup and firing action, never a previous spell's flag.
        start(fan?5:4,fan?56:54);
        boss.syncAction();
        spells.barrage(target,phase,fan);
    }
    private void finish() {
        if(action==1) teleportCooldown=600;
        float modifier=action==1?.5F:1;
        idleCooldown=Math.max(0,(int)Math.floor(AscianCombatTuning.ATTACK_COOLDOWN*modifier-(int)Math.floor(boss.m_21233_()/Math.max(1,boss.m_21223_()))*4)*2);
        sequence=(sequence+1)%CYCLES[phase-1].length;
        clash.clear(); start(0,0);
    }
    private void beginClash(int segment,int start) {
        if(segment>0)boss.action(10+segment);
        int duration=HIT_TICKS[segment]-start;
        clash.begin("melee",segment,boss.f_19797_,boss.f_19797_+duration,5.5,2.8);
        warnBody(duration,WarningColor.YELLOW);
    }
    public void recordClash(DamageSource source) {
        if(action!=2 || stunned() || transitioning()) return;
        if(clash.tryRecord(boss,source)==MeleeClashWindow.RecordResult.POLLUTED) {
            guardableTick=Integer.MIN_VALUE; guardableSegment=-1;
            warnBody(Math.max(1,clash.remainingTicks(boss.f_19797_)),WarningColor.RED);
        }
    }
    public void recordPerfectGuard(Entity player) {
        if(!parryAllowed())return;
        rewardMeleeGuard(player,guardableSegment);
    }
    private void rewardMeleeGuard(Entity player,int segment) {
        if(guardedHitTick!=boss.f_19797_){guardedHitTick=boss.f_19797_;guardedPlayers.clear();}
        if(player!=null)guardedPlayers.add(player.m_20148_());
        if(segment<0 || rewardedSegment==segment)return;
        rewardedSegment=segment;
        guard(player);
    }
    private void guard(Entity player) {
        if(boss.m_9236_().f_46443_||!boss.m_6084_()||stunned()||transitioning()||boss.f_19797_==lastGuard)return;
        ensurePosture(); lastGuard=boss.f_19797_;
        posture=Math.max(0,posture-1);
        if(posture==0)startPostureStun(player);
        else syncHud();
    }
    private void recoverPostureFromMeleeHit() {
        if(!boss.m_6084_()||stunned()||transitioning()||boss.f_19797_==lastGuard||boss.f_19797_==lastHit)return;
        ensurePosture();
        if(posture>=requiredGuards())return;
        lastHit=boss.f_19797_; ++posture;
        syncHud();
    }
    private void interruptAttack() {
        clash.clear(); sequence=0; meleeDamage=false;
        start(0,0);
        boss.m_21573_().m_26573_();
    }
    private void resetPosture() {
        releaseStunAi(); stun=0; stunPositionTicks=0;
        postureMaximum=requiredGuards(); posture=postureMaximum;
        guardableTick=Integer.MIN_VALUE; guardableSegment=-1; rewardedSegment=-1;
        lastGuard=Integer.MIN_VALUE; lastHit=Integer.MIN_VALUE;
        guardedHitTick=-1; guardedPlayers.clear();
        syncHud();
    }
    private void startPostureStun(Entity player) {
        posture=0; stun=AscianCombatTuning.STUN_TICKS; stunPositionTicks=5;
        interruptAttack(); lockStunAi();
        boss.m_20256_(Vec3.f_82478_);
        syncPosition(); syncHud();
        com.starfantasy.goety.network.StarFantasyGoetyNetwork.sendAscianEffects(boss,5,java.util.List.of(boss.m_20182_()));
    }
    private void lockStunAi() {
        if(!stunOwnsAi) { aiBeforeStun=boss.m_21525_(); stunOwnsAi=true; }
        boss.m_21557_(true); boss.m_21561_(false); boss.m_21573_().m_26573_();
        boss.m_20256_(new Vec3(0,boss.m_20184_().f_82480_,0));
    }
    private void releaseStunAi() {
        if(stunOwnsAi) { boss.m_21557_(aiBeforeStun); stunOwnsAi=false; }
    }
    public void beforeEntityTick() { if(stunned())lockStunAi(); }
    private void tickPostureStun() {
        lockStunAi();
        if(stunPositionTicks>0) { --stunPositionTicks; syncPosition(); }
        if(--stun<=0)finishPostureStun();
    }
    private void finishPostureStun() {
        phase=Math.min(phase,phaseFor(boss.m_21223_()/boss.m_21233_()));
        interruptAttack(); resetPosture(); idleCooldown=0; forceSpell=false;
        // Every phase ends in teleport: resume there, then wrap to the first slot.
        sequence=CYCLES[phase-1].length-1;
    }
    private void meleeHit(LivingEntity target,int index) {
        Vec3 forward=boss.m_20154_().m_82542_(1,0,1).m_82541_();
        AscianMeleeShape shape=new AscianMeleeShape(index,boss.m_20185_(),boss.m_20189_(),forward.f_82479_,forward.f_82481_,target.m_20186_());
        var bounds=shape.bounds();
        AABB box=new AABB(bounds.minX(),bounds.minY(),bounds.minZ(),bounds.maxX(),bounds.maxY(),bounds.maxZ());
        spells.meleeVisuals(shape,phase,target.m_20186_());
        boolean yellow=!clash.isPolluted("melee",index), playerDamaged=false;
        guardableTick=yellow?boss.f_19797_:Integer.MIN_VALUE; guardableSegment=yellow?index:-1;
        meleeDamage=true;
        try { for(LivingEntity victim:boss.m_9236_().m_45976_(LivingEntity.class,box)) {
            if(stunned()||transitioning()||action!=2||!boss.m_6084_())break;
            var victimBox=victim.m_20191_();
            if(skip(victim)||!shape.intersects(victimBox.f_82288_,victimBox.f_82289_,victimBox.f_82290_,victimBox.f_82291_,victimBox.f_82292_,victimBox.f_82293_))continue;
            float before=victim.m_21223_()+victim.m_6103_();
            boolean hit=victim.m_6469_(boss.m_269291_().m_269333_(boss),AscianDamage.meleeAmount(index,victim.m_21233_(),AscianKingConfig.DAMAGE_MULTIPLIER.get()));
            boolean guarded=guardedHitTick==boss.f_19797_&&guardedPlayers.contains(victim.m_20148_());
            if(hit && before>victim.m_21223_()+victim.m_6103_() && !guarded){
                if(victim instanceof net.minecraft.world.entity.player.Player)playerDamaged=true;
                if(phase==6)AscianEffects.hit(victim,6);
            }
            if(stunned()||transitioning()||action!=2||!boss.m_6084_())break;
            if(hit&&!guarded)victim.m_147240_(boss.m_217043_().m_188500_(),boss.m_20185_()-victim.m_20185_(),boss.m_20189_()-victim.m_20189_());
        }} finally { meleeDamage=false; }
        if(AscianKingConfig.HARD_MODE.get()&&playerDamaged)recoverPostureFromMeleeHit();
    }
    public void warnBody(int ticks,WarningColor color) {
        if(color==WarningColor.YELLOW) StarFantasyVfx.warningRing(boss,ticks,5.5,2.8);
        else if(color==WarningColor.PURPLE) StarFantasyVfx.purpleWarningRing(boss,ticks,5.5,2.8);
        else StarFantasyVfx.redWarningRing(boss,ticks,5.5,2.8);
    }
    private void face(LivingEntity target) { boss.m_21391_(target,180,180); boss.m_21563_().m_24946_(target.m_20185_(),target.m_20188_(),target.m_20189_()); }
    private void chase(LivingEntity target) {
        Vec3 d=target.m_20182_().m_82546_(boss.m_20182_()).m_82542_(1,0,1); double distance=d.m_82553_();
        double step=distance<=4?Math.min(2,distance*.1):Math.min(2,distance-4);
        if(step>.02)boss.m_6478_(MoverType.SELF,d.m_82541_().m_82490_(step));
        boss.m_20256_(Vec3.f_82478_); syncPosition();
    }
    private void teleportNear(LivingEntity target,double distance) {
        Vec3 away=boss.m_20182_().m_82546_(target.m_20182_()).m_82542_(1,0,1);
        if(away.m_82556_()<.0001)away=new Vec3(1,0,0);
        Vec3 desired=target.m_20182_().m_82549_(away.m_82541_().m_82490_(distance));
        teleport(AscianEffects.ground(boss.m_9236_(),desired.f_82479_,Math.max(target.m_20186_(),boss.m_20186_())+4,desired.f_82481_),target);
    }
    private void teleportAttack(LivingEntity target) {
        double distance=boss.m_20182_().m_82546_(target.m_20182_()).m_82542_(1,0,1).m_82553_();
        if(distance>=24) { teleportNear(target,5); return; }
        for(int i=0;i<10;i++) {
            double angle=boss.m_217043_().m_188500_()*Math.PI*2;
            if(distance<=6)angle=Math.atan2(boss.m_20189_()-target.m_20189_(),boss.m_20185_()-target.m_20185_())+(boss.m_217043_().m_188500_()-.5)*Math.PI;
            double radius=12+boss.m_217043_().m_188500_()*12;
            Vec3 p=AscianEffects.ground(boss.m_9236_(),target.m_20185_()+Math.cos(angle)*radius,boss.m_20186_()+boss.m_217043_().m_188503_(48),target.m_20189_()+Math.sin(angle)*radius);
            if(p.f_82480_<boss.m_20186_() || !boss.m_9236_().m_6425_(net.minecraft.core.BlockPos.m_274446_(p)).m_76178_())continue;
            if(boss.m_9236_().m_45756_(boss,boss.m_20191_().m_82383_(p.m_82546_(boss.m_20182_())))) { teleport(p,target); return; }
        }
        teleportSound(); // The original cast sounds even if all landing candidates fail.
    }
    private void teleport(Vec3 pos,LivingEntity target) {
        teleportSound(); // Sound belongs to the departure point, before changing position.
        boss.m_6034_(pos.f_82479_,pos.f_82480_,pos.f_82481_); boss.m_20256_(Vec3.f_82478_); face(target); syncPosition();
        teleportParticles(true);
    }
    private void teleportSound() { boss.m_5496_(SoundEvents.f_11852_,1,1); }
    private void teleportParticles(boolean arrival) {
        com.starfantasy.goety.network.StarFantasyGoetyNetwork.sendAscianEffects(boss,
                arrival?com.starfantasy.goety.network.ClientboundAscianEffectPacket.TELEPORT_ARRIVAL:com.starfantasy.goety.network.ClientboundAscianEffectPacket.TELEPORT_WINDUP,
                java.util.List.of(boss.m_20182_()));
    }
    private void syncPosition() { ((ServerLevel)boss.m_9236_()).m_7726_().m_8445_(boss,new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(boss)); }
    private void tickAmbience() {
        if(transitioning()||stunned())return;
        if(ambienceCooldown-->0)return;
        if(players().isEmpty()) { ambienceCooldown=20; return; }
        int style=phase==6?1+(ambienceSequence++%5):phase;
        spells.ambience(style); ambienceCooldown=phase==6?4:20;
    }
    public Set<ServerPlayer> players() {
        Set<ServerPlayer> result=new LinkedHashSet<>(boss.bossBar.m_8324_());
        if(boss.m_5448_() instanceof ServerPlayer p)result.add(p);
        result.addAll(boss.m_9236_().m_45976_(ServerPlayer.class,boss.m_20191_().m_82400_(AscianCombatTuning.DEBUFF_RADIUS)));
        result.removeIf(this::skip); return result;
    }
    private void refreshEffects() {
        Set<net.minecraft.world.effect.MobEffect> current=new HashSet<>();
        for(var e:AscianEffects.parse(AscianKingConfig.BUFFS.get(phase-1).get(),true))current.add(e.type());
        for(var list:AscianKingConfig.BUFFS)for(var e:AscianEffects.parse(list.get(),true)) if(!current.contains(e.type()))boss.m_21195_(e.type());
        AscianEffects.apply(boss,AscianKingConfig.BUFFS.get(phase-1).get(),true);
        for(var player:players())AscianEffects.apply(player,AscianKingConfig.DEBUFFS.get(phase-1).get(),false);
    }
    public void syncHud() {
        ensurePosture();
        PostureHudSync.sync(boss,boss.bossBar,posture,requiredGuards(),boss.m_6084_());
    }
    public void clear() { releaseStunAi(); stun=0; interruptAttack(); spells.clear(); StarFantasyVfx.clearWarningsForOwner(boss); com.starfantasy.goety.network.StarFantasyGoetyNetwork.clearFireTraps(boss); com.starfantasy.goety.network.StarFantasyGoetyNetwork.sendAscianEffects(boss,0,java.util.List.of()); PostureHudSync.hide(boss,boss.bossBar,requiredGuards()); }
    public void save(CompoundTag n) {
        ensurePosture();
        n.m_128405_("AscianPhase",phase); n.m_128405_("AscianTransition",transitionTarget); n.m_128405_("AscianTransitionAge",transitionAge);
        n.m_128379_("AscianTransitionRear",transitionRear);
        n.m_128405_("AscianPosture",posture); n.m_128405_("AscianStun",stun);
        n.m_128379_("AscianStunOwnsAi",stunOwnsAi); n.m_128379_("AscianAiBeforeStun",aiBeforeStun);
    }
    public void load(CompoundTag n) {
        releaseStunAi(); interruptAttack();
        phase=Math.max(1,Math.min(6,n.m_128451_("AscianPhase")));
        transitionTarget=Math.max(0,Math.min(6,n.m_128451_("AscianTransition")));
        transitionAge=Math.max(0,Math.min(AscianCombatTuning.TRANSITION_DURATION,n.m_128451_("AscianTransitionAge")));
        transitionRear=n.m_128441_("AscianTransitionRear")?n.m_128471_("AscianTransitionRear"):transitionTarget==6||AscianKingConfig.HARD_MODE.get();
        resetPosture();
        stun=transitioning()?0:Math.max(0,n.m_128451_("AscianStun"));
        // Upgrade existing entities saved by the accumulated-guard implementation.
        int saved=n.m_128441_("AscianPosture")?n.m_128451_("AscianPosture"):requiredGuards()-Math.max(0,n.m_128451_("AscianGuards"));
        posture=stunned()?0:transitioning()?requiredGuards():Math.max(0,Math.min(requiredGuards(),saved));
        if(posture==0&&!stunned())posture=requiredGuards(); // Repair old empty-but-not-stunned saves.
        if(n.m_128471_("AscianStunOwnsAi"))boss.m_21557_(n.m_128471_("AscianAiBeforeStun"));
        if(stunned()){stunPositionTicks=5;lockStunAi();}
        forceSpell=true;
        syncHud();
    }
    public static int phaseFor(float health) { for(int i=1;i<6;i++)if(health>THRESHOLDS[i])return i; return 6; }
}
