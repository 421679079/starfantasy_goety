"""Exercise the production Void Ray hit policy and servant attack scaling."""
from pathlib import Path
import re
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/starfantasy/goety'
manager = (JAVA / 'combat/ApollyonVoidRayManager.java').read_text('utf-8')
servant = (JAVA / 'entity/ApollyonServantEntity.java').read_text('utf-8')

def method(source, marker):
    start = source.index(marker)
    end = source.index('{', start) + 1
    depth = 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]

constants = '\n'.join(re.findall(r'private static final (?:float|int) (?:DAMAGE|VOID_TOUCHED_TICKS|VOID_TOUCHED_AMPLIFIER) = [^;]+;', manager))
fixture = r'''
public class VoidRayDamageRegression {
 static int checks;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 record DamageSource(String type, LivingEntity direct, LivingEntity owner) {}
 static class DamageSources {
  DamageSource m_269104_(LivingEntity direct,LivingEntity owner){return new DamageSource("indirectMagic",direct,owner);}
  DamageSource m_269341_(){return new DamageSource("fellOutOfWorld",null,null);}
 }
 static class MobEffect {}
 record MobEffectInstance(MobEffect effect,int duration,int amplifier) {}
 static class GoetyEffects {
  static final GoetyEffects VOID_TOUCHED=new GoetyEffects();
  final MobEffect effect=new MobEffect(); Object get(){return effect;}
 }
 static class LivingEntity {
  double attack=10; float maxHealth=20; boolean accepts=true; DamageSource source; float damage; MobEffectInstance effect;
  DamageSources m_269291_(){return new DamageSources();}
  double m_21133_(Object attribute){return attack;}
  boolean m_6469_(DamageSource s,float amount){source=s;damage=amount;return accepts&&amount>0;}
  void m_7292_(MobEffectInstance e){effect=e;}
 }
 static class Mob extends LivingEntity {}
 static class Attributes {static final Object f_22281_=new Object();}
 static class ApollyonServantEntity extends Mob { SCALING }
 static class ApollyonSpellSupport {
  static float damage(Mob caster,LivingEntity target,float base,float maxHealthFraction){
   return base+target.maxHealth*maxHealthFraction;
  }
 }
 CONSTANTS
 HIT_METHOD
 public static void main(String[] args){
  var caster=new ApollyonServantEntity();
  for(double attack:new double[]{10,20,4.5,1}){
   caster.attack=attack;var target=new LivingEntity();hurtTarget(caster,target);
   check(target.damage==(float)(attack*5),"live attack * 5 per ray hit");
   check(target.source.type().equals("indirectMagic")&&target.source.direct()==caster&&target.source.owner()==caster,"magic source credits the actual servant");
   check(target.effect!=null&&target.effect.effect()==GoetyEffects.VOID_TOUCHED.get()&&target.effect.duration()==200&&target.effect.amplifier()==1,"successful hits retain Void Touched II for 200 ticks");
  }
  var rejected=new LivingEntity();rejected.accepts=false;hurtTarget(caster,rejected);
  check(rejected.effect==null,"rejected hits do not apply Void Touched");
  var boss=new Mob();var victim=new LivingEntity();hurtTarget(boss,victim);
  check(victim.damage==16&&victim.source.type().equals("fellOutOfWorld"),"boss retains 15 base plus 5% max health void damage");
  check(victim.source.direct()==null&&victim.source.owner()==null,"boss remains deliberately anonymous");
  check(victim.effect!=null&&victim.effect.duration()==200,"boss effect retained");
  System.out.println("PASS: "+checks+" Void Ray damage, attribution, attack scaling and effect checks.");
 }
}
'''.replace('SCALING', method(servant, 'public float scaleOutgoingDamage(')).replace('CONSTANTS', constants).replace('HIT_METHOD', method(manager, 'private static void hurtTarget('))
out = ROOT / 'build' / ('void-ray-damage-check-' + str(time.time_ns()))
out.mkdir(parents=True)
source = out / 'VoidRayDamageRegression.java'
source.write_text(fixture, 'utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(source)], check=True)
subprocess.run(['java', '-cp', str(out), 'VoidRayDamageRegression'], check=True)
