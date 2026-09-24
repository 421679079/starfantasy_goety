"""Exercise soul deduction hooks, owner routing and judgment timing using production methods."""
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
MAIN = ROOT / 'src/main/java/com/starfantasy/goety'


def method(text, marker):
    start = text.index(marker)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


entity = (MAIN / 'entity/HadesServantEntity.java').read_text('utf-8')
hooks = (MAIN / 'mixin/SEHelperSoulConsumptionMixin.java').read_text('utf-8')
tracker = (MAIN / 'combat/HadesJudgmentSouls.java').read_text('utf-8')
fixture = r'''
import java.util.*;
public class HadesJudgmentRegression {
    static void check(boolean value, String name) { if (!value) throw new AssertionError(name); }
    static class Level { boolean f_46443_; }
    static class ServerLevel extends Level {
        int time, cleaveTick=-1, soundTick=-1;
        void m_7967_(Object entity) { cleaveTick=time; }
        void m_5594_(Object player,Object pos,Object sound,Object category,int volume,int pitch) {soundTick=time;}
    }
    static class Player {
        UUID id=UUID.randomUUID(); Level level=new Level();
        UUID m_20148_(){return id;} Level m_9236_(){return level;}
    }
    static class LivingEntity { boolean alive=true; }
    static class ISoulEnergy {
        int souls; int getSoulEnergy(){return souls;}
        boolean decreaseSE(int amount){if(souls<=0)return false;souls=Math.max(0,souls-amount);return true;}
    }
    static class ItemStack { int souls; }
    static class ITotem {
        static int currentSouls(ItemStack stack){return stack.souls;}
        static void decreaseSouls(ItemStack stack,int amount){stack.souls=Math.max(0,stack.souls-amount);}
    }
    HOOKS
    static class HadesJudgmentSouls {
        static final Map<UUID,Set<HadesServantEntity>> LOADED=new HashMap<>();
        TRACKER
    }
    static class Value {int value=20000; Integer get(){return value;} }
    static class ServantConfig {static Value JUDGMENT_SOUL_COST=new Value();}
    static class Vec3 {
        static Vec3 f_82478_=new Vec3(0,0,0);
        double f_82479_,f_82480_,f_82481_;
        Vec3(double x,double y,double z){f_82479_=x;f_82480_=y;f_82481_=z;}
        Vec3 m_82520_(double x,double y,double z){return new Vec3(f_82479_+x,f_82480_+y,f_82481_+z);}
        Vec3 m_82549_(Vec3 v){return m_82520_(v.f_82479_,v.f_82480_,v.f_82481_);}
        Vec3 m_82490_(double s){return new Vec3(f_82479_*s,f_82480_*s,f_82481_*s);}
        double m_82553_(){return Math.sqrt(f_82479_*f_82479_+f_82480_*f_82480_+f_82481_*f_82481_);}
        Vec3 m_82541_(){double len=m_82553_();return len<1e-6?f_82478_:m_82490_(1/len);}
    }
    static class BlockPos {static Object m_274561_(double x,double y,double z){return new Object();}}
    static class Holder {Object get(){return new Object();}}
    static class ApollyonEntityRegistry {static Holder APOLLYON_CLEAVE_EFFECT=new Holder();}
    static class ApollyonSoundRegistry {static Holder CAST_OBSIDIAN=new Holder(),CAST_HADES=new Holder();}
    static class ApollyonCleaveEffectEntity {
        static final int BURST_START_TICK=60;
        ApollyonCleaveEffectEntity(Object type,ServerLevel level){}
        void configureServant(HadesServantEntity servant){}
        void m_6034_(double x,double y,double z){}
    }
    static class StarFantasyVfx {
        static void stomp(Object a,Object b,int c,int d){}
        static void slamShockwave(Object a,Object b,int c){}
        static void areaShake(Object a,Object b,int c,int d,float e){}
        static void areaImpactShake(Object a,Object b,int c,int d,int e,int f,int g){}
    }
    static class BattleFocusCombat {
        static final List<Integer> times=new ArrayList<>();
        static final List<Float> damage=new ArrayList<>();
        static void sonicDamage(ServerLevel level,HadesServantEntity caster,Vec3 pos,double radius,
                                float amount,java.util.function.Predicate<LivingEntity> filter){
            check(radius==15,"damage radius");
            check(filter.test(new LivingEntity()),"living enemy permitted");
            times.add(level.time);damage.add(amount);
        }
    }
    static class Data {
        Map<Integer,Integer> values=new HashMap<>();
        void m_135381_(int key,int value){values.put(key,value);}
    }
    static class Navigation {void m_26573_(){} }
    static class Mob {
        LivingEntity target;
        void m_6710_(LivingEntity entity){target=entity;}
    }
    static class HadesServantEntity extends Mob {
        CONSTANTS
        static int ATTACK=0,AGE=1;
        Data f_19804_=new Data();
        ServerLevel level=new ServerLevel(); UUID owner;
        int judgmentSouls,judgmentCooldown;
        double cooldownWork,attackPower=10;
        boolean added=true,removed,alive=true;
        Vec3 attackOrigin,attackForward,judgmentCenter=new Vec3(0,0,5);
        float attackYaw;
        Level m_9236_(){return level;}
        boolean m_6084_(){return alive;}
        boolean m_213877_(){return removed;}
        LivingEntity m_5448_(){return target;}
        boolean canHarm(LivingEntity victim){return victim.alive;}
        boolean isAddedToWorld(){return added;}
        UUID getOwnerId(){return owner;}
        Vec3 m_20182_(){return Vec3.f_82478_;}
        Navigation m_21573_(){return new Navigation();}
        void lockFacing(){}
        void sound(Object s,int volume){}
        Object m_5720_(){return new Object();}
        double attackDamage(double multiplier){return attackPower*multiplier;}
        int attackType(){return f_19804_.values.getOrDefault(ATTACK,0);}
        ENTITY_METHODS
    }
    public static void main(String[] args) {
        Player owner=new Player(),other=new Player();
        HadesServantEntity hades=new HadesServantEntity();hades.owner=owner.id;
        HadesJudgmentSouls.track(hades);
        ISoulEnergy energy=new ISoulEnergy();energy.souls=30000;
        starfantasy$recordArcaSpend(energy,5000,owner,5000);
        check(hades.judgmentSouls==0,"idle spending ignored");
        hades.m_6710_(new LivingEntity());
        starfantasy$recordArcaSpend(energy,5000,other,5000);
        check(hades.judgmentSouls==0,"other player's spending ignored");
        starfantasy$recordArcaSpend(energy,1000,owner,5000);
        check(hades.judgmentSouls==1000,"discounted committed amount only");
        starfantasy$recordArcaSpend(energy,0,owner,5000);
        check(hades.judgmentSouls==1000,"cancelled/zero deduction ignored");
        energy.souls=73;
        starfantasy$recordArcaSpend(energy,1000,owner,1000);
        check(hades.judgmentSouls==1073,"insufficient balance counts 73, not 1000");
        check(!starfantasy$recordArcaSpend(energy,1000,owner,1000),"empty Arca result preserved");
        check(hades.judgmentSouls==1073,"empty Arca earns no charge");
        ItemStack totem=new ItemStack();totem.souls=50;
        starfantasy$recordTotemSpend(totem,200,owner,200);
        check(hades.judgmentSouls==1123,"totem depletion counts actual amount");
        owner.level.f_46443_=true;totem.souls=50;
        starfantasy$recordTotemSpend(totem,20,owner,20);
        check(hades.judgmentSouls==1123,"client observation ignored");
        owner.level.f_46443_=false;
        hades.recordJudgmentSouls(18877);
        check(hades.judgmentReady(),"threshold reached");
        hades.cooldownWork=5;hades.startAttack(1,new Vec3(0,0,1));
        check(hades.attackType()==0&&hades.judgmentSouls==20000,"current recovery not interrupted");
        hades.cooldownWork=0;hades.startAttack(1,new Vec3(0,0,1));
        check(hades.attackType()==4,"judgment overrides next normal selection");
        check(hades.judgmentSouls==0&&hades.judgmentCooldown==600,"charge consumed and cooldown starts");
        check(hades.judgmentCenter.f_82481_==5,"half-size impact offset");
        hades.recordJudgmentSouls(Integer.MAX_VALUE);
        check(hades.judgmentSouls==20000&&!hades.judgmentReady(),"can recharge during cooldown without overflow");
        hades.judgmentCooldown=0;
        check(hades.judgmentReady(),"recharged skill ready after cooldown");
        hades.m_6710_(null);
        check(hades.judgmentSouls==0,"lost target clears immediately");
        hades.m_6710_(new LivingEntity());hades.recordJudgmentSouls(500);
        hades.target.alive=false;hades.recordJudgmentSouls(100);
        check(hades.judgmentSouls==0,"dead target also clears");
        hades.m_6710_(new LivingEntity());
        HadesJudgmentSouls.untrack(hades,owner.id);energy.souls=1000;
        starfantasy$recordArcaSpend(energy,100,owner,100);
        check(hades.judgmentSouls==0&&HadesJudgmentSouls.LOADED.isEmpty(),"unloaded servant released");
        for(int age=0;age<=140;age++){
            hades.level.time=age;
            if(age==96)hades.attackPower=15;
            hades.tickJudgment(age);
        }
        check(hades.level.cleaveTick==26&&hades.level.soundTick==86,"boss cleave/burst timing");
        check(BattleFocusCombat.times.size()==20,"exactly twenty hits");
        for(int i=0;i<20;i++){
            check(BattleFocusCombat.times.get(i)==86+i,"one hit each tick");
            check(BattleFocusCombat.damage.get(i)==(i<10?10f:15f),"live attack damage, no max-health addition");
        }
        check(HadesServantEntity.JUDGMENT_END_TICK==126,"recovery ends forty ticks after burst");
        System.out.println("PASS: actual soul deductions, ownership, combat reset, priority/cooldown, twenty timed hits and scaled anchor.");
    }
}
'''
constants = re.findall(r'(?:public|private) static final (?:int|double) [^;]+;', entity)
constants = [c for c in constants if any(key in c for key in ('ROUNDHOUSE =', 'JUDGMENT_'))]
fixture = fixture.replace('CONSTANTS', '\n'.join(constants))
fixture = fixture.replace('HOOKS', '\n'.join(method(hooks, signature) for signature in (
    'private static boolean starfantasy$recordArcaSpend(', 'private static void starfantasy$recordTotemSpend(')))
fixture = fixture.replace('TRACKER', '\n'.join(method(tracker, signature) for signature in (
    'public static void track(', 'public static void untrack(', 'public static void spent(')))
fixture = fixture.replace('ENTITY_METHODS', '\n'.join(method(entity, signature) for signature in (
    'private boolean hasCombatTarget()', 'public void m_6710_(', 'public void recordJudgmentSouls(',
    'private boolean judgmentReady()', 'private void startAttack(int type, Vec3 aim)', 'private void tickJudgment(')))
fixture = fixture.replace('net.minecraft.core.BlockPos', 'BlockPos')
with tempfile.TemporaryDirectory(prefix='hades-judgment-') as temp:
    java = Path(temp) / 'HadesJudgmentRegression.java'
    java.write_text(fixture, encoding='utf-8')
    subprocess.run(['javac', '-encoding', 'UTF-8', str(java)], check=True)
    subprocess.run(['java', '-cp', temp, 'HadesJudgmentRegression'], check=True)
