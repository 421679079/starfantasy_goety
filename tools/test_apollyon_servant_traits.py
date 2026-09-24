"""Run the actual servant trait, standby, launch-point and temporary-attribute methods."""
from pathlib import Path
import json, re, subprocess, time
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/starfantasy/goety'
source=(JAVA/'entity/ApollyonServantEntity.java').read_text('utf-8')
def block(text, marker):
    start=text.index(marker); end=text.index('{',start)+1; depth=1
    while depth:
        depth+=(text[end]=='{')-(text[end]=='}');end+=1
    return text[start:end]
methods='\n'.join(block(source,x) for x in [
    'public MobType m_6336_(', 'public boolean m_6040_(', 'public void m_7311_(',
    'public boolean m_7301_(', 'public boolean m_142535_(', 'protected float m_6431_(',
    'public boolean m_6779_(', 'private HadesServantEntity mountedHadesServant(',
    'private LivingEntity mountedTarget(', 'public void overrideSetTarget(', 'public void setPriorityTarget(',
    'private void holdStandby(', 'public void m_7023_(', 'private void setVoidRayMovementLocked(',
    'private void updateKnockbackResistance(', 'public void setStaying(', 'public Vec3 arrowOrigin(',
])
methods=methods.replace('com.Polarice3.Goety.common.effects.GoetyEffects', 'GoetyEffects')
methods=methods.replace('net.minecraft.world.effect.MobEffects', 'MobEffects')
mounted_sync=block(block(source,'public void m_8119_()'),'if (!this.m_9236_().f_46443_)')
registry=(JAVA/'registry/ApollyonEntityRegistry.java').read_text('utf-8')
for servant in ('APOSTLE_SERVANT', 'APOLLYON_SERVANT'):
    registration=registry.split(servant+' =',1)[1].split(';',1)[0]
    assert '.m_20719_()' in registration, servant+' must register as fire immune'
helper=(JAVA/'combat/VoidRayKnockback.java').read_text('utf-8')
helper='static '+helper[helper.index('public final class VoidRayKnockback'):]
for name in ['ApollyonEntity','ApollyonServantEntity']:
    text=(JAVA/f'entity/{name}.java').read_text('utf-8')
    assert 'setVoidRayMovementLocked(spell == CastingSpell.VOID_RAY)' in text
    assert text.count('setVoidRayMovementLocked(false)')>=2
apostle=(JAVA/'combat/apostle/ApostleTitleSpellGoal.java').read_text('utf-8')
assert 'VoidRayKnockback.setCasting(boss, title == 3)' in block(apostle,'public void m_8056_()')
assert block(apostle,'public void m_8041_()').index('setCasting(boss, false)') < block(apostle,'public void m_8041_()').index('ApostleSpellSupport.original')
assert '.m_22268_(Attributes.f_22278_, 0.75D)' in source
assert 'Attributes.f_22278_).m_22100_(0.75D)' in block(source,'public void m_7378_(')
assert 'arrow.m_6034_(start.f_82479_, start.f_82480_, start.f_82481_)' in block(source,'private void fireVolley(')

fixture=r'''
import java.util.*;
public class TraitsRegression {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static class Vec3 {
        static final Vec3 f_82478_=new Vec3(0,0,0);
        final double f_82479_,f_82480_,f_82481_;
        Vec3(double x,double y,double z){f_82479_=x;f_82480_=y;f_82481_=z;}
        Vec3 m_82520_(double x,double y,double z){return new Vec3(f_82479_+x,f_82480_+y,f_82481_+z);}
        Vec3 m_82549_(Vec3 v){return m_82520_(v.f_82479_,v.f_82480_,v.f_82481_);}
        Vec3 m_82490_(double n){return new Vec3(f_82479_*n,f_82480_*n,f_82481_*n);}
    }
    enum HumanoidArm { LEFT,RIGHT }
    enum MobEffectCategory { HARMFUL,BENEFICIAL,NEUTRAL }
    static class MobEffectInstance {
        MobEffectCategory category;MobEffectInstance(MobEffectCategory c){category=c;}
        MobEffectInstance m_19544_(){return this;} MobEffectCategory m_19483_(){return category;}
    }
    static class GoetyEffects {
        static final java.util.function.Supplier<MobEffectInstance> BURN_HEX =
            () -> MobEffects.f_19615_;
    }
    static class MobEffects {
        static final MobEffectInstance f_19615_=new MobEffectInstance(MobEffectCategory.HARMFUL);
    }
    static class MobType {static final MobType f_21641_=new MobType();}
    static class Pose {} static class EntityDimensions {} static class DamageSource {}
    static class Level {boolean f_46443_;}
    static class LivingEntity {
        LivingEntity vehicle;boolean alive=true;
        LivingEntity m_20202_(){return vehicle;}
        boolean m_6084_(){return alive;}
    }
    static class HadesServantEntity extends LivingEntity {
        Servant passenger;LivingEntity target;
        Servant mountedApollyonServant(){return passenger;}
        LivingEntity m_5448_(){return target;}
        boolean canHarm(LivingEntity other){return other!=null && other.m_6084_();}
    }
    static class AttributeModifier {
        enum Operation {ADDITION}
        UUID id;double value;AttributeModifier(UUID id,String name,double value,Operation op){this.id=id;this.value=value;}
    }
    static class Attribute {
        double base=.75;Map<UUID,AttributeModifier> modifiers=new HashMap<>();
        AttributeModifier m_22111_(UUID id){return modifiers.get(id);}
        void m_22118_(AttributeModifier mod){modifiers.put(mod.id,mod);}
        void m_22120_(UUID id){modifiers.remove(id);}
        double value(){return Math.min(1,base+modifiers.values().stream().mapToDouble(m->m.value).sum());}
    }
    static class Attributes {static final Object f_22278_=new Object();}
    static class Navigation {boolean stopped;void m_26573_(){stopped=true;}}
    static class MoveControl {void m_24988_(float x,float y){}}
    static class Mob extends LivingEntity {
        Level level=new Level();Attribute attr=new Attribute();Navigation nav=new Navigation();
        LivingEntity target,priority,hurtBy,hurtOther;int priorityTime,fire;boolean staying,using,casting,aggressive;
        float yaw,pitch,f_19859_,f_19860_,f_20883_,f_20884_,f_20885_,f_20886_;
        Vec3 velocity=Vec3.f_82478_,travelInput,forward=new Vec3(0,0,1);HumanoidArm arm=HumanoidArm.RIGHT;
        Level m_9236_(){return level;}Attribute m_21051_(Object type){return attr;}
        boolean isStaying(){return staying;}boolean isFriendlyEntity(LivingEntity e){return false;}
        void setStaying(boolean value){staying=value;}
        void overrideSetTarget(LivingEntity t){target=t;}
        void m_6710_(LivingEntity t){overrideSetTarget(t);}
        void setPriorityTarget(LivingEntity t){overrideSetTarget(t);priority=t;}
        LivingEntity getPriorityTarget(){return priority;}
        LivingEntity m_5448_(){return target;}
        void setPriorityTime(int value){priorityTime=value;}
        void m_6703_(LivingEntity t){hurtBy=t;}void m_21335_(LivingEntity t){hurtOther=t;}
        void setCasting(boolean b){casting=b;}void m_5810_(){using=false;}void m_21561_(boolean b){aggressive=b;}
        Navigation m_21573_(){return nav;}MoveControl m_21566_(){return new MoveControl();}
        void m_7910_(float f){}void m_21567_(float f){}
        Vec3 m_20184_(){return velocity;}void m_20256_(Vec3 v){velocity=v;}
        float m_146908_(){return yaw;}void m_146922_(float f){yaw=f;}void m_146926_(float f){pitch=f;}
        void m_7023_(Vec3 v){travelInput=v;}
        void m_7311_(int n){fire=n;}boolean m_7301_(MobEffectInstance effect){return true;}
        boolean m_6779_(LivingEntity e){return true;}
        Vec3 m_20182_(){return new Vec3(0,64,0);}Vec3 m_20154_(){return forward;}HumanoidArm m_5737_(){return arm;}
    }
    static class ApollyonFireTrapManager {static void clearForBoss(Object o){}}
    static class ApollyonLightningStormManager {static void clearForBoss(Object o){}}
    static class ApollyonWildSurgeManager {static void clearManagedThornsForBoss(Object o){}}
    HELPER
    static class Servant extends Mob {
        static class Teleport { void clear(){} }
        final Teleport combatTeleport=new Teleport();
        static final UUID DEFENSIVE_KNOCKBACK=UUID.randomUUID();
        boolean standbyLocked,voidRayMovementLocked,shieldHitInProgress,followingMountTarget;float standbyYaw,shield;
        boolean hasCooperativeShield(){return shield>0;}
        METHODS
        void syncMountedTarget(){SYNC}
    }
    public static void main(String[] args){
        Servant a=new Servant();LivingEntity enemy=new LivingEntity();
        check(a.m_6336_()==MobType.f_21641_,"undead type");
        a.m_7311_(400);check(a.fire<=0,"cannot ignite");
        check(a.m_6040_(),"can breathe underwater");
        check(!a.m_7301_(new MobEffectInstance(MobEffectCategory.HARMFUL)),"harmful effects rejected");
        check(a.m_7301_(new MobEffectInstance(MobEffectCategory.BENEFICIAL)),"positive buffs accepted");
        check(a.m_7301_(new MobEffectInstance(MobEffectCategory.NEUTRAL)),"neutral effects accepted");
        check(!a.m_142535_(100,1,new DamageSource()),"same fall immunity as boss");
        check(a.m_6431_(new Pose(),new EntityDimensions())<1.6,"eye height follows body rather than tall halo hitbox");
        a.setStaying(true);check(a.attr.value()==1,"standby grants full resistance immediately");
        a.yaw=45;a.pitch=20;a.target=a.priority=a.hurtBy=a.hurtOther=enemy;
        a.priorityTime=100;a.casting=a.using=a.aggressive=true;a.setVoidRayMovementLocked(true);
        a.velocity=new Vec3(3,-.2,2);a.holdStandby();
        check(a.target==null && a.priority==null && a.hurtBy==null && a.hurtOther==null,"standby clears targets and retaliation memories");
        check(a.priorityTime==0 && !a.casting && !a.using && !a.aggressive && !a.voidRayMovementLocked,"standby stops combat and casting");
        check(a.nav.stopped && a.velocity.f_82479_==0 && a.velocity.f_82481_==0 && a.velocity.f_82480_==-.2,"no horizontal motion; gravity retained");
        a.overrideSetTarget(enemy);a.setPriorityTarget(enemy);
        check(a.target==null && a.priority==null && !a.m_6779_(enemy),"native, priority and retaliation targeting blocked while staying");
        a.yaw=120;a.f_20885_=-80;a.pitch=25;a.holdStandby();
        check(a.yaw==45 && a.pitch==0 && a.f_20883_==45 && a.f_20885_==45,"standby locks body/head/pitch");
        check(a.f_19859_==45 && a.f_19860_==0 && a.f_20884_==45 && a.f_20886_==45,"old interpolation angles also locked");
        a.m_7023_(new Vec3(1,0,1));check(a.travelInput==Vec3.f_82478_,"standby travel ignores input");
        a.setStaying(false);check(a.attr.value()==.75 && a.attr.modifiers.isEmpty(),"leaving standby restores base resistance immediately");
        a.overrideSetTarget(enemy);
        check(a.target==enemy && a.m_6779_(enemy),"combat available after leaving standby");
        HadesServantEntity hades=new HadesServantEntity();LivingEntity other=new LivingEntity();
        a.vehicle=hades;hades.passenger=a;hades.target=enemy;a.syncMountedTarget();
        check(a.target==enemy,"mounted servant adopts Hades target");
        a.overrideSetTarget(other);a.setPriorityTarget(other);
        check(a.target==enemy && a.priority==null,"independent and priority targets cannot override mount");
        hades.target=other;a.syncMountedTarget();
        check(a.target==other,"servant follows mount target changes");
        hades.target=null;a.syncMountedTarget();
        check(a.target==null,"servant stops targeting when mount does");
        hades.target=enemy;a.setStaying(true);a.syncMountedTarget();
        check(a.target==null,"standby still blocks mount target");
        a.setStaying(false);a.syncMountedTarget();
        check(a.target==enemy,"leaving standby restores mount target");
        a.vehicle=null;a.syncMountedTarget();
        check(a.target==null && a.priorityTime==0,"dismount clears shared target");
        a.overrideSetTarget(other);
        check(a.target==other,"independent targeting resumes after dismount");
        for(double base:new double[]{0,.2,.75,1}){
            a=new Servant();a.attr.base=base;
            a.setVoidRayMovementLocked(true);a.setVoidRayMovementLocked(true);
            check(a.attr.value()==1 && a.attr.modifiers.size()==1,"ray gives full resistance without duplicate modifiers");
            a.shield=120;a.updateKnockbackResistance();a.setVoidRayMovementLocked(false);
            check(a.attr.value()==1,"ending ray keeps shield resistance");
            a.shield=0;a.shieldHitInProgress=true;a.updateKnockbackResistance();
            check(a.attr.value()==1,"shield-breaking hit remains resistant through knockback");
            a.shieldHitInProgress=false;a.updateKnockbackResistance();
            check(a.attr.value()==base && a.attr.base==base && a.attr.modifiers.isEmpty(),"ending effects restores original attributes");
            a.setStaying(true);a.shield=120;a.updateKnockbackResistance();
            check(a.attr.value()==1 && a.attr.modifiers.size()==1,"standby and shield share one temporary modifier");
            a.setStaying(false);check(a.attr.value()==1,"leaving standby retains an active shield's resistance");
            a.setStaying(true);a.shield=0;a.updateKnockbackResistance();
            check(a.attr.value()==1,"losing shield retains standby resistance");
            a.setVoidRayMovementLocked(true);a.setStaying(false);
            check(a.attr.value()==1 && a.attr.modifiers.size()==1,"leaving standby preserves ray resistance");
            a.setVoidRayMovementLocked(false);
            check(a.attr.value()==base && a.attr.modifiers.isEmpty(),"ending all defenses restores original resistance");
        }
        a=new Servant();Vec3 origin=a.arrowOrigin();
        check(Math.abs(origin.f_82480_-65.3)<1e-6 && origin.f_82481_==.5 && origin.f_82479_==-.15,"arrow starts by drawn bow below the head");
        a.arm=HumanoidArm.LEFT;check(a.arrowOrigin().f_82479_==.15,"left-handed bow mirrors launch side");
        a.yaw=90;a.forward=new Vec3(-1,0,0);origin=a.arrowOrigin();
        check(Math.abs(origin.f_82479_+.5)<1e-6 && Math.abs(origin.f_82481_-.15)<1e-6,"launch origin rotates with shooter");
        System.out.println("PASS: "+checks+" servant traits, standby, launch-point and temporary knockback checks.");
    }
}
'''.replace('    HELPER\n',helper+'\n').replace('        METHODS\n',methods+'\n').replace('SYNC',mounted_sync)
out=ROOT/'build'/('servant-traits-regression-'+str(time.time_ns()));out.mkdir(parents=True)
p=out/'TraitsRegression.java';p.write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'TraitsRegression'],check=True)

assets=ROOT/'src/main/resources/assets/starfantasy_goety'
for lang in ['zh_cn','en_us']:
    d=json.loads((assets/f'lang/{lang}.json').read_text('utf-8'))
    assert d['entity.starfantasy_goety.apollyon_servant']==('亚小妹仆从' if lang=='zh_cn' else 'Little Apollyon Servant')
animations=json.loads((assets/'animations/entity/apollyon/apollyon.animation.json').read_text('utf-8'))['animations']
assert set(animations)=={'standby'}
bones={b['name'] for b in json.loads((assets/'geo/entity/apollyon/apollyon.geo.json').read_text('utf-8'))['minecraft:geometry'][0]['bones']}
assert set(animations['standby']['bones'])<=bones
assert animations['standby']['bones']['Root']['position']==[0,-5,5]
assert animations['standby']['bones']['Throne']['scale']==[1,1,1]
print('PASS: correct entity translation namespace, original throne tracks, and no unused walk/stand animations.')
