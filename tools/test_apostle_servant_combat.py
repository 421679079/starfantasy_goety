"""Exercise the actual pure transition and damage-owner routines with small Java fixtures."""
from pathlib import Path
import re, subprocess, tempfile

root=Path(__file__).resolve().parents[1]
base=root/'src/main/java/com/starfantasy/goety'
entity=(base/'entity/ApostleServantEntity.java').read_text(encoding='utf-8')
events=(base/'event/ApostleServantEvents.java').read_text(encoding='utf-8')
def method(text,signature):
    start=text.index(signature); opening=text.index('{',start); depth=1; end=opening+1
    while depth:
        depth+=(text[end]=='{')-(text[end]=='}'); end+=1
    return text[start:end]
transition=method(entity,'public static float transitionHealth(')
owner=method(events,'public static ApostleServantEntity owner(')
scale=method(events,'public static void scaleDamage(')
fixture='''
import java.util.*;
public class ServantCombatRegression {
    static class Entity {}
    interface IOwned { Entity getTrueOwner(); }
    static class Owned extends Entity implements IOwned {
        Entity owner; Owned(Entity owner){this.owner=owner;} public Entity getTrueOwner(){return owner;}
    }
    static class Projectile extends Entity { Entity owner; Projectile(Entity e){owner=e;} Entity m_19749_(){return owner;} }
    static class ApostleServantEntity extends Entity { float multiplier=2; float damageMultiplier(){return multiplier;} }
    record Source(Entity causing,Entity direct){ Entity m_7639_(){return causing;} Entity m_7640_(){return direct;} }
    static class LivingHurtEvent {
        float amount=10; Source source; LivingHurtEvent(Entity causing,Entity direct){source=new Source(causing,direct);}
        Source getSource(){return source;} float getAmount(){return amount;} void setAmount(float v){amount=v;}
    }
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) {
        for(float max:new float[]{320,1000,10000}) for(float start:new float[]{1,max*.1F,max*.5F}) {
            check(transitionHealth(start,max,0)==start,"transition starts at current health");
            float previous=start;
            for(int tick=1;tick<=100;tick++) { float value=transitionHealth(start,max,tick);check(value>=previous && value<=max,"monotonic recovery");previous=value; }
            check(transitionHealth(start,max,100)==max,"full exactly at tick 100");
            check(transitionHealth(start,max,200)==max,"no over-healing");
        }
        ApostleServantEntity servant=new ApostleServantEntity();
        Owned inferno=new Owned(servant); Projectile fireball=new Projectile(inferno);
        for(Entity attacker:new Entity[]{servant,inferno,fireball,new Owned(inferno)}) {
            LivingHurtEvent hit=new LivingHurtEvent(attacker,fireball);scaleDamage(hit);
            check(hit.amount==20,"damage scales once through every owner chain");
        }
        LivingHurtEvent indirect=new LivingHurtEvent(null,fireball);scaleDamage(indirect);check(indirect.amount==20,"direct-source fallback");
        LivingHurtEvent unrelated=new LivingHurtEvent(new Entity(),new Entity());scaleDamage(unrelated);check(unrelated.amount==10,"other mobs unchanged");
        LivingHurtEvent noSource=new LivingHurtEvent(null,null);scaleDamage(noSource);check(noSource.amount==10,"source-free damage is scaled only at its call site");
        Owned cycle=new Owned(null);cycle.owner=cycle;check(owner(cycle)==null,"owner cycle terminates");
        servant.multiplier=0;LivingHurtEvent zero=new LivingHurtEvent(servant,fireball);scaleDamage(zero);check(zero.amount==0,"zero multiplier");
        System.out.println("PASS: 100-tick recovery for modified max health; direct/projectile/nested damage scaling; no double scaling; owner cycles");
    }
METHODS
}
'''.replace('METHODS',transition+'\n'+owner+'\n'+scale)
with tempfile.TemporaryDirectory(prefix='apostle-servant-') as directory:
    folder=Path(directory); file=folder/'ServantCombatRegression.java';file.write_text(fixture,encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','--release','17',str(file)],check=True)
    subprocess.run(['java','-cp',directory,'ServantCombatRegression'],check=True)
assert 'extends Summoned implements' in entity and not re.search(r'extends Apostle\b', entity)
assert 'ApostleServantBehaviorMixin' not in (root/'src/main/resources/starfantasy_goety.mixins.json').read_text()
teleport=(base/'combat/apostle/ApostleServantTeleport.java').read_text()
assert '++windup >= (servant.isSecondPhase() ? 20 : 40)' in teleport
assert 'servant.isInNether() && servant.isSecondPhase()' in teleport
print('PASS: independent superclass, no servant injection into Apostle, 40/20 teleport and Nether-only departure trap')
