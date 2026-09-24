"""Execute the servant's real AI tick and spell timelines using small Java world fixtures."""
from pathlib import Path
import re
import subprocess
import time

ROOT=Path(__file__).resolve().parents[1]
source=(ROOT/'src/main/java/com/starfantasy/goety/entity/ApollyonServantEntity.java').read_text('utf-8')
boss=(ROOT/'src/main/java/com/starfantasy/goety/entity/ApollyonEntity.java').read_text('utf-8')

def block(text, marker):
    start=text.index(marker); end=text.index('{',start)+1; depth=1
    while depth:
        depth+=(text[end]=='{')-(text[end]=='}'); end+=1
    return text[start:end]

constants=source[source.index('    private static final int SPELL_CAST_TICKS'):source.index('    private static final EntityDataAccessor')]
outer='\n'.join(block(source,m) for m in [
    'public boolean isHalfHealth()', 'private boolean isCombatPhaseTwo()',
    'public static int shotInterval(', 'public int shotIntervalTicks()', 'private int bowDrawTicks()',
    'public float scaleOutgoingDamage(', 'public boolean isFriendlyEntity(',
    'private void setSpellDelay(', 'private void setPoolSpellDelay()', 'private void setPhaseTwoSpellDelay()',
    'public float absorbShield(', 'private void activatePhaseTwoShield()',
    'public float applyIncomingDamageReductions(', 'public float finishIncomingDamage(',
    'public boolean m_6469_(',
    'private void beginCastingAnimation(', 'private void playPigActionSound(', 'public static boolean isPigName(',
    'private enum CastingSpell', 'public void m_8119_()',
])
goal=block(source,'private final class CombatActionGoal')
tick=block(goal,'public void m_8037_()')
timeline='\n'.join(block(goal,m) for m in [
    'private void startSpell(CastingSpell spell)', 'private void startSpell(CastingSpell spell, boolean skipWindup)',
    'private int fastStartTick(', 'private void tickSpell(', 'private void tickLightningStorm(',
    'private int castDuration()',
    'private void finishFangFeast(',
])
# Shared spell forms, hit timings and damage types must keep the boss's existing values.
for name in ['FIRE_TRAP_CAST_TICKS','FROST_IMPACT_CAST_TICKS','WILD_SURGE_CAST_TICKS',
             'VOID_RAY_CAST_TICKS','FIRE_TRAP_FIRST_WAVE_TICK','VOID_RAY_FIRST_HIT_TICK','VOID_RAY_SECOND_HIT_TICK']:
    pattern=rf'{name} = (\d+);'
    assert re.search(pattern,source).group(1)==re.search(pattern,boss).group(1), name
assert 'summonObsidianMonoliths' not in source and '.pageant.' not in source
assert 'GroundWarning' not in source and 'groundSectorWarning' not in source

fixture=r'''
import java.util.*;
public class ServantRegression {
    static int checks;
    static void check(boolean ok,String message) { checks++; if(!ok) throw new AssertionError(message); }
    static class Vec3 { static final Vec3 ZERO=new Vec3(); }
    static class Level { boolean f_46443_; long tick; long m_46467_(){return tick;} }
    static class ServerLevel extends Level {}
    static class Entity {
        UUID id=UUID.randomUUID(); boolean allied;
        UUID m_20148_(){return id;} boolean m_7307_(Entity e){return allied;}
    }
    static class LivingEntity extends Entity { boolean alive=true; boolean m_6084_(){return alive;} }
    static class Owned extends LivingEntity {
        UUID ownerId; UUID getOwnerId(){return ownerId;}
    }
    interface OwnableEntity { UUID m_21805_(); }
    static class Pet extends LivingEntity implements OwnableEntity {
        UUID owner; public UUID m_21805_(){return owner;}
    }
    static class Attributes { static final Object f_22281_=new Object(),f_22283_=new Object(); }
    static class Data { float shield; int castDuration; long castStart;
        void m_135381_(Object k,float value){shield=value;}
        void m_135381_(Object k,int value){castDuration=value;}
        void m_135381_(Object k,long value){castStart=value;}
    }
    static class DamageTypeTags { static final Object f_268731_=new Object(),f_268738_=new Object(); }
    static class DamageSource {
        boolean magic,bypass;
        boolean m_269533_(Object tag){return tag==DamageTypeTags.f_268731_?magic:bypass;}
    }
    static class ItemStack {}
    static class Items { static final ItemStack f_42411_=new ItemStack(); }
    static class ProjectileUtil { static Object m_37297_(Object a,Object b){return b;} }
    static class Navigation { void m_26573_(){} }
    static class Look { void m_24960_(Object a,float b,float c){} }
    static class Sense { boolean visible=true; boolean m_148306_(Object target){return visible;} }
    static class Rand { int m_188503_(int n){return 0;} }
    static class Base extends LivingEntity {
        void m_8119_(){}
        boolean m_6469_(DamageSource source,float amount){
            ApollyonServantEntity a=(ApollyonServantEntity)this;
            a.health-=a.finishIncomingDamage(source,a.applyIncomingDamageReductions(source,amount));
            a.f_19802_=20;
            return true;
        }
    }
    static class SoundEvent {}
    static class SoundEvents { static final SoundEvent f_12233_=new SoundEvent(); }
    static class Holder<T> { T value; Holder(T v){value=v;} T get(){return value;} }
    static class ServantConfig {
        static final Holder<Double> APOLLYON_DAMAGE_REDUCTION=new Holder<>(.25),
            APOLLYON_MAGIC_RESISTANCE=new Holder<>(.35),APOLLYON_DAMAGE_CAP=new Holder<>(20.0),
            APOLLYON_REGENERATION=new Holder<>(1.0);
    }
    static class ApollyonConfig {
        static double nonCastingMultiplier=.5;
        static int invulnerabilityTime=10;
        static int bossInvulnerabilityTime(){return invulnerabilityTime;}
        static double damageTakenMultiplier(){return nonCastingMultiplier;}
    }
    static class ModSounds { static final Holder<SoundEvent> APOSTLE_PREPARE_SPELL=new Holder<>(new SoundEvent()),APOSTLE_CAST_SPELL=new Holder<>(new SoundEvent()); }
    static class ApollyonEffectRegistry { static final Holder<Object> MULTISHOT=new Holder<>(new Object()); }
    static class MobEffectInstance { MobEffectInstance(Object a,int b,int c,boolean d,boolean e){} }
    static final List<String> hits=new ArrayList<>();
    static final class ApollyonFireTrapManager {
        static void cast(Object a,Object b){hits.add("fire");}
    }
    static final class ApollyonFrostImpactManager {
        static void spawnChunk(Object a,Object b,int sequence){hits.add("ice:"+sequence);}
    }
    static final class ApollyonLightningStormManager {
        static Vec3 captureAnchor(Object a,Object b){return Vec3.ZERO;}
        static void queueTrackingStrike(Object a,Object b){hits.add("tracking");}
        static void queueRing(Object a,Object b,double r,int count){hits.add("ring:"+count);}
        static void queueCenter(Object a,Object b){hits.add("center");}
    }
    static final class ApollyonWildSurgeManager {
        static Vec3 captureAnchor(Object a,Object b){return Vec3.ZERO;}
        static void warnEarthRing(Object a,Object b){}
        static List<Vec3> spawnEarthRingAndWarnThorns(Object a,Object b){hits.add("earth");return List.of(Vec3.ZERO);}
        static void spawnThornRings(Object a,Object b){hits.add("thorn");}
    }
    static final class ApollyonVoidRayManager {
        static final int FIRST_WARNING_TICKS=30,SECOND_WARNING_TICKS=20; static final float SECOND_WAVE_ROTATION=15;
        static Vec3 captureAnchor(Object a){return Vec3.ZERO;}
        static void warn(Object a,Object b,float c,int d){}
        static void detonate(Object a,Object b,float c){hits.add("void:"+c);}
    }
    static final class ApollyonFangFeastSpell {
        static final int CAST_TICKS=80;
        ApollyonFangFeastSpell(Object a){}
        void tick(int n,Object target){if(n==40||n==80)hits.add("fang");}
        void finish(boolean completed){}
    }
    static final class ApollyonCastingLightningEntity { static void spawn(Object a,int b){} }
    static final class ApollyonMeteorManager {
        static List<Long> ticks=new ArrayList<>();
        static void spawn(ApollyonServantEntity a){ticks.add(a.level.tick);}
    }
    static class ApollyonServantEntity extends Base {
        static class Teleport { void onHurt(){} void tick(LivingEntity target){} boolean isPending(){return false;} }
        final Teleport combatTeleport=new Teleport();
        CONSTANTS
        static final Object SHIELD=new Object(),CAST_DURATION=new Object(),CAST_STARTED_AT=new Object();
        boolean pig; int pigSounds; boolean isPigVariant(){return pig;}
        // Use a custom 600 health in this fixture to keep shield/threshold expectations explicit.
        double attack=10,speed=1; float health=600,max=600; UUID ownerId; LivingEntity owner;
        ServerLevel level=new ServerLevel(); Data f_19804_=new Data();
        LivingEntity target=new LivingEntity(); Sense sense=new Sense();
        int multishotCooldown=10000,spellCooldown=10000,meteorCooldown,drawTicks=-1,multishots;
        int invulnerabilityTicks,f_19802_,f_19797_; boolean immune;
        long nextShotTick; boolean voidRayMovementLocked,casting,shieldHitInProgress,standbyLocked,staying;
        List<Long> shots=new ArrayList<>(); CastingSpell nextSpell=CastingSpell.FIRE_TRAP;
        final CombatActionGoal goal=new CombatActionGoal();
        float m_21223_(){return health;} float m_21233_(){return max;}
        void m_5634_(float amount){health=Math.min(max,health+amount);}
        boolean m_6673_(DamageSource source){return immune;}
        double m_21133_(Object attribute){return attribute==Attributes.f_22281_?attack:speed;}
        Level m_9236_(){return level;}
        LivingEntity m_5448_(){return target;} void m_6710_(LivingEntity value){target=value;}
        UUID getOwnerId(){return ownerId;} LivingEntity getTrueOwner(){return owner;}
        Rand m_217043_(){return new Rand();}
        Navigation m_21573_(){return new Navigation();}
        Look m_21563_(){return new Look();}
        Sense m_21574_(){return sense;}
        boolean m_6117_(){return drawTicks>=0;} int m_21252_(){return drawTicks;}
        void m_6672_(Object hand){drawTicks=0;}
        void m_5810_(){drawTicks=-1;}
        void m_21557_(boolean value){}
        void m_5496_(SoundEvent sound,float volume,float pitch){if(sound==SoundEvents.f_12233_)pigSounds++;}
        void m_7292_(MobEffectInstance effect){multishots++;}
        boolean hasCooperativeShield(){return getCooperativeShield()>0.001f;}
        float getCooperativeShield(){return f_19804_.shield;}
        void setCasting(boolean value){casting=value;}
        boolean isCastingAction(){return casting;}
        boolean isStaying(){return staying;}
        void holdStandby(){target=null;casting=false;}
        void updateKnockbackResistance(){}
        void setVoidRayMovementLocked(boolean locked){voidRayMovementLocked=locked;}
        void fireVolley(LivingEntity t){shots.add(level.tick);}
        CastingSpell drawPoolSpell(){return nextSpell;}
        void showCastingSmoke(){} void showFireTrapCastingParticles(){} void showFrostImpactCastingParticles(){}
        void showWildSurgeCastingParticles(){} void showFangFeastCastingParticles(){} void showVoidRayCastingParticles(){}
        OUTER
        private final class CombatActionGoal {
            int castTicks,seeTime,phaseTwoChainSpellsRemaining;
            CastingSpell activeSpell=CastingSpell.NONE; boolean activeSpellSkippedWindup;
            Vec3 lightningStormAnchor,wildSurgeAnchor,voidRayAnchor;
            List<Vec3> wildSurgeThornPoints=List.of(); ApollyonFangFeastSpell fangFeast;
            void updateMovement(LivingEntity target,boolean seen){seeTime=seen?seeTime+1:seeTime-1;}
            void lockVoidRayMovement(){}
            TICK
            TIMELINE
        }
        void advance(int n){for(int i=0;i<n;i++){level.tick++;f_19797_++;if(drawTicks>=0)drawTicks++;goal.m_8037_();m_8119_();}}
    }
    public static void main(String[] args) {
        for(String name:new String[]{"亚小猪","Little Apollyon Pig","little apollyon pig","Little Apollyon pig"})
            check(ApollyonServantEntity.isPigName(name),"accepted pig name");
        for(String name:new String[]{"亚小妹","Little Apollyon","Little Apollyon Piglet"," 亚小猪",null})
            check(!ApollyonServantEntity.isPigName(name),"unrelated name unchanged");
        for(boolean immediate:new boolean[]{false,true})for(ApollyonServantEntity.CastingSpell spell:ApollyonServantEntity.CastingSpell.values()){
            if(spell==ApollyonServantEntity.CastingSpell.NONE)continue;
            ApollyonServantEntity actor=new ApollyonServantEntity();actor.pig=true;actor.level.tick=100;
            actor.goal.startSpell(spell,immediate);
            int duration=actor.f_19804_.castDuration;long start=actor.f_19804_.castStart;
            check(actor.pigSounds==1,"one pig cry per spell start");
            if(immediate)actor.goal.tickSpell(actor.target);
            int remaining=duration-(immediate?1:0);
            actor.advance(remaining-1);
            check(actor.casting,"feed remains active until final spell tick");
            actor.advance(1);
            check(!actor.casting && actor.level.tick-start==duration,"feed clock ends exactly with spell including skipped windup");
        }
        for(boolean half:new boolean[]{false,true}) for(double speed:new double[]{.5,1,2,4,100}) {
            ApollyonServantEntity a=new ApollyonServantEntity();a.speed=speed;if(half)a.health=200;
            a.advance(500);int expected=ApollyonServantEntity.shotInterval(half,speed);
            check(a.shots.size()>2,"enough shots");
            for(int i=1;i<a.shots.size();i++)check(a.shots.get(i)-a.shots.get(i-1)==expected,"whole draw+cooldown interval "+half+"/"+speed);
        }
        ApollyonServantEntity a=new ApollyonServantEntity();
        for(float base:new float[]{15,30,40}){
            check(a.scaleOutgoingDamage(base)==base,"base damage unchanged at attack 10");
            a.attack=20;check(a.scaleOutgoingDamage(base)==base*2,"live attack doubles each segment");a.attack=10;
        }
        check(a.scaleOutgoingDamage(10)*3==30,"three servant arrow segments total 30 at attack 10");
        a.health=300;check(a.isHalfHealth(),"half threshold inclusive");a.max=400;check(!a.isHalfHealth(),"uses actual modified max health");
        for(boolean half:new boolean[]{false,true}){
            a=new ApollyonServantEntity();a.health=half?200:600;
            a.goal.startSpell(ApollyonServantEntity.CastingSpell.MULTISHOT);a.advance(40);
            check(a.multishots==1,"one multishot granted");
            check(a.multishotCooldown==(half?799:1199),"40/60 second cooldown set once on completion");
            check(a.getCooperativeShield()==(half?120:0),"shield only on low-health multishot");
            a.advance(1);check(a.multishots==1,"no repeated buff grant");
        }
        a=new ApollyonServantEntity();a.health=300;a.activatePhaseTwoShield();
        DamageSource d=new DamageSource();check(a.absorbShield(d,20)==0 && a.getCooperativeShield()==100,"physical shield absorption");
        d.magic=true;check(a.absorbShield(d,30)==5 && a.getCooperativeShield()==0,"magic shield uses original 4x depletion");
        for(ApollyonServantEntity.CastingSpell spell:ApollyonServantEntity.SPELL_POOL){
            hits.clear();a=new ApollyonServantEntity();a.goal.startSpell(spell);a.advance(125);
            check(!hits.isEmpty(),"spell resolves: "+spell);
            if(spell==ApollyonServantEntity.CastingSpell.FROST_IMPACT)check(hits.size()==4,"four ice chunks");
            if(spell==ApollyonServantEntity.CastingSpell.VOID_RAY)check(hits.size()==2,"two ray waves");
            if(spell==ApollyonServantEntity.CastingSpell.WILD_SURGE)check(hits.equals(List.of("earth","thorn")),"earth followed by thorns");
            if(spell==ApollyonServantEntity.CastingSpell.LIGHTNING_STORM)check(hits.size()==10,"six tracking plus three rings and center");
        }
        a=new ApollyonServantEntity();a.health=200;a.spellCooldown=0;
        a.goal.m_8037_();a.advance(60);
        check(a.goal.activeSpell!=ApollyonServantEntity.CastingSpell.NONE && a.goal.activeSpellSkippedWindup,"half-health chains second spell without full windup");
        check(a.goal.phaseTwoChainSpellsRemaining==0,"chain limited to two spells");
        a.advance(41);check(a.goal.activeSpell==ApollyonServantEntity.CastingSpell.NONE,"chain completes");
        a=new ApollyonServantEntity();Apoll yonMeteorManager.ticks.clear();a.advance(50);
        check(ApollyonMeteorManager.ticks.isEmpty(),"no meteors above half");a.health=200;a.advance(41);
        check(ApollyonMeteorManager.ticks.size()==5,"meteors every 10 ticks below half");
        for(int i=1;i<5;i++)check(ApollyonMeteorManager.ticks.get(i)-ApollyonMeteorManager.ticks.get(i-1)==10,"meteor interval");
        a.target=null;a.m_8119_();check(a.meteorCooldown==0,"no meteor without combat target");
        a=new ApollyonServantEntity();LivingEntity owner=new LivingEntity();a.owner=owner;a.ownerId=owner.id;
        check(a.isFriendlyEntity(owner) && a.isFriendlyEntity(a),"owner and self protected");
        Owned sibling=new Owned();sibling.ownerId=owner.id;check(a.isFriendlyEntity(sibling),"same-owner Goety servant protected");
        Pet pet=new Pet();pet.owner=owner.id;check(a.isFriendlyEntity(pet),"same-owner vanilla pet protected");
        LivingEntity hostile=new LivingEntity();check(!a.isFriendlyEntity(hostile),"enemy damage permitted");
        hostile.allied=true;check(a.isFriendlyEntity(hostile),"team ally protected");
        a=new ApollyonServantEntity();d=new DamageSource();
        check(a.applyIncomingDamageReductions(d,40)==15,"idle physical damage includes the hidden boss multiplier");
        a.drawTicks=10;
        check(a.applyIncomingDamageReductions(d,40)==15,"drawing a bow remains non-casting");
        a.drawTicks=-1;a.casting=true;
        check(a.applyIncomingDamageReductions(d,40)==30,"configured 25% common mitigation");
        d.magic=true;check(Math.abs(a.applyIncomingDamageReductions(d,40)-19.5f)<.001,"magic resistance multiplies common mitigation");
        a.casting=false;
        check(Math.abs(a.applyIncomingDamageReductions(d,40)-9.75f)<.001,"idle magic damage includes all three factors");
        ApollyonConfig.nonCastingMultiplier=.2;
        d.magic=false;check(a.applyIncomingDamageReductions(d,40)==6,"reads live boss config instead of caching a servant default");
        a.casting=true;
        check(a.applyIncomingDamageReductions(d,40)==30,"casting ignores changed boss multiplier");
        a.casting=false;ApollyonConfig.nonCastingMultiplier=0;
        check(a.applyIncomingDamageReductions(d,40)==0,"zero boss multiplier is honored");
        ApollyonConfig.nonCastingMultiplier=2;
        check(a.applyIncomingDamageReductions(d,40)==60,"boss multiplier may also increase non-casting damage");
        ApollyonConfig.nonCastingMultiplier=.5;
        check(a.finishIncomingDamage(d,100)==20,"configured final damage cap");
        ServantConfig.APOLLYON_DAMAGE_CAP.value=0.0;
        check(a.finishIncomingDamage(d,100)==100,"zero disables cap");
        ServantConfig.APOLLYON_DAMAGE_CAP.value=20.0;
        a.m_6469_(d,20);float after=a.health;
        check(a.invulnerabilityTicks==10 && a.f_19802_==0,"custom invulnerability replaces vanilla frames");
        check(!a.m_6469_(d,80) && a.health==after,"larger followup cannot bypass custom frames");
        a.advance(9);check(!a.m_6469_(d,20),"immune through tick 9");
        a.advance(1);check(a.m_6469_(d,20),"damage resumes at tick 10");
        a.advance(10);ApollyonConfig.invulnerabilityTime=3;
        check(a.m_6469_(d,20) && a.invulnerabilityTicks==3,"next hit reads changed boss invulnerability config");
        a.advance(2);check(!a.m_6469_(d,20),"shorter configured duration still protects tick 2");
        a.advance(1);ApollyonConfig.invulnerabilityTime=0;
        check(a.m_6469_(d,20) && a.invulnerabilityTicks==0 && a.m_6469_(d,20),"zero boss duration disables servant special frames");
        ApollyonConfig.invulnerabilityTime=10;
        a.activatePhaseTwoShield();check(a.invulnerabilityTicks==0,"shield activation clears stale frames");
        after=a.health;a.m_6469_(d,20);
        check(a.health==after && a.invulnerabilityTicks==0,"shield absorbs without health-damage invulnerability");
        d.bypass=true;check(a.applyIncomingDamageReductions(d,100)==100 && a.finishIncomingDamage(d,100)==100,"bypass damage ignores reductions, cap and shield");
        a.invulnerabilityTicks=10;check(a.m_6469_(d,100),"bypass damage ignores custom frames");
        a=new ApollyonServantEntity();a.health=400;a.max=800;
        a.advance(19);check(a.health==400,"regeneration not applied before 20 ticks");
        a.advance(1);check(a.health==401,"regeneration uses configured per-second amount");
        a.health=799;a.advance(20);check(a.health==800,"regeneration follows live maximum and cannot exceed it");
        System.out.println("PASS: "+checks+" actual AI checks: shot periods, six timelines, chain, shield, damage scaling, meteors, allies and configurable defenses.");
    }
}
'''.replace('Apoll yon','Apollyon')
fixture=fixture.replace('\n        CONSTANTS\n','\n'+constants+'\n').replace('\n        OUTER\n','\n'+outer+'\n')
fixture=fixture.replace('\n            TICK\n','\n'+tick+'\n').replace('\n            TIMELINE\n','\n'+timeline+'\n')
out=ROOT/'build'/('apollyon-servant-regression-'+str(time.time_ns()));out.mkdir(parents=True)
java=out/'ServantRegression.java';java.write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(java)],check=True)
subprocess.run(['java','-cp',str(out),'ServantRegression'],check=True)
print('Production-method fixture:',out)
