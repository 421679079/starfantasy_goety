"""Exercise production health-write policy and entity guards against combat boundary fixtures.

The complementary --health mode in test_final_staff_mixins.py verifies injection
into real Minecraft bytecode. Neither test claims an in-game modpack run.
"""
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'build' / f'combat-health-tests-{time.time_ns()}'
OUT.mkdir(parents=True)


def method(source, marker):
    start = source.index(marker)
    end = source.index('{', start) + 1
    depth = 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


entity = (ROOT / 'src/main/java/com/starfantasy/goety/entity/ApollyonEntity.java').read_text('utf-8')
methods = '\n'.join(method(entity, m) for m in [
    'public com.starfantasy.library.combat.CombatHealthProtection combatHealthProtection(',
    'public double combatDamageCap(', 'public int combatHitInvulnerabilityTicks(',
    'public boolean combatHealthLocked(', 'public boolean bypassCombatHitImmunity(',
    'public float absorbCombatHealthLoss(', 'public boolean handleCombatHealthLoss(',
    'protected void m_6475_(',
    'public float clampFinalDamage(',
])
fixture = r'''
import com.starfantasy.library.combat.*;
public class CombatHealthRegression {
 static int checks;
 static void eq(float expected,float actual) {
  checks++; if(Float.isNaN(actual)||Math.abs(expected-actual)>.0001f)
   throw new AssertionError(expected+" != "+actual);
 }
 static class ApollyonConfig {
  static int immunity=10; static double cap=30;
  static int bossInvulnerabilityTime(){return immunity;}
  static double damageCap(){return cap;}
 }
 static class Mth {static double m_14008_(double a,double b,double c){return Math.max(b,Math.min(c,a));}}
 static class DamageSource {Object m_7639_(){return this;}}
 static class Level extends net.minecraft.world.level.Level {}
 static class Pageant {boolean protectedNow;boolean isInvulnerable(){return protectedNow;}}
 static abstract class Base extends net.minecraft.world.entity.LivingEntity {
  protected void m_6475_(DamageSource source,float amount) {
   Boss b=(Boss)this;
   if(b.throwInDamage)throw new IllegalStateException("test event exception");
   // Simulated Forge final-damage boundary, using production cap and the shield fixture.
   amount=b.absorbCooperativeShield(source,b.clampFinalDamage(amount));
   b.write(b.health-amount);
  }
 }
 static class Boss extends Base implements CombatHealthEntity {
  CombatHealthProtection healthProtection=new CombatHealthProtection();
  Boss(){healthProtection.enable();}
  public float getHealth(){return health;}public float getMaxHealth(){return maximum;}
  public Level level(){return level;}
  boolean shieldHitInProgress,monolith,transition,throwInDamage;
  int damageProcessingDepth,vanillaInvulnerability;
  int shieldCalls,transitionCalls;
  float health=100,maximum=100,shield;
  Level level=new Level();Pageant pageant=new Pageant();
  Level m_9236_(){return level;}
  float m_21223_(){return health;}float m_21233_(){return maximum;}
  boolean hasCooperativeShield(){return shield>0;}
  boolean isMonolithPower(){return monolith;}
  float absorbCooperativeShield(DamageSource source,float amount){
   shieldCalls++;float absorbed=Math.min(shield,amount);shield-=absorbed;return amount-absorbed;
  }
  boolean tryStartPageantFromFinalDamage(float amount){
   transitionCalls++;if(transition&&health-amount<=0){pageant.protectedNow=true;write(maximum);return true;}
   return false;
  }
  void write(float requested){health=healthProtection.constrain(this,this,requested);}
  METHODS
 }
 public static void main(String[] args) {
  Boss b=new Boss(); b.write(0);eq(70,b.health);eq(10,b.healthProtection.invulnerabilityTicks());
  b.vanillaInvulnerability=0;b.write(0);eq(70,b.health);
  b.m_6475_(new DamageSource(),1000);eq(70,b.health);eq(1,b.shieldCalls);
  for(int tick=0;tick<10;tick++)b.healthProtection.tick();
  b.write(0);eq(40,b.health);
  b.write(90);eq(90,b.health); // Healing is allowed during immunity.
  b.write(Float.NaN);eq(90,b.health);
  b.write(Float.POSITIVE_INFINITY);eq(100,b.health);
  b.healthProtection.setInvulnerabilityTicks(0);b.write(Float.NEGATIVE_INFINITY);eq(70,b.health);
  b=new Boss();b.m_6475_(new DamageSource(),12);eq(88,b.health);eq(1,b.shieldCalls);
  b=new Boss();b.m_6475_(new DamageSource(),1000);eq(70,b.health);eq(1,b.shieldCalls);
  b=new Boss();b.pageant.protectedNow=true;b.write(0);eq(100,b.health);
  b.m_6475_(new DamageSource(),1000);eq(100,b.health);eq(0,b.shieldCalls);
  b=new Boss();b.monolith=true;b.write(0);eq(100,b.health);
  b.m_6475_(new DamageSource(),1000);eq(0,b.shieldCalls);
  b=new Boss();b.shield=50;b.write(0);eq(100,b.health);eq(20,b.shield);eq(0,b.healthProtection.invulnerabilityTicks());
  b.write(0);eq(90,b.health);eq(0,b.shield); // Raw writes follow the existing shield exemption.
  b=new Boss();b.shield=20;b.shieldHitInProgress=true;
  b.m_6475_(new DamageSource(),1000);eq(90,b.health);eq(1,b.shieldCalls);
  b=new Boss();b.healthProtection.beginRestore();b.write(5);eq(5,b.health);eq(0,b.healthProtection.invulnerabilityTicks());
  b.healthProtection.endRestore();b.write(0);eq(0,b.health);
  b=new Boss();b.healthProtection=new CombatHealthProtection();b.write(5);eq(5,b.health);
  b=new Boss();b.level.isClientSide=true;b.write(5);eq(5,b.health);
  b=new Boss();b.health=20;b.transition=true;b.write(0);eq(100,b.health);eq(1,b.transitionCalls);
  b=new Boss();b.health=20;b.write(0);eq(0,b.health); // Normal death remains possible.
  b=new Boss();b.throwInDamage=true;
  try {b.m_6475_(new DamageSource(),50);throw new AssertionError();}catch(IllegalStateException expected){}
  eq(0,b.damageProcessingDepth);eq(0,b.healthProtection.invulnerabilityTicks());
  ApollyonConfig.cap=0;b=new Boss();b.write(0);eq(0,b.health);
  ApollyonConfig.cap=30;ApollyonConfig.immunity=0;b=new Boss();b.write(0);b.write(0);eq(40,b.health);
  eq(0,b.healthProtection.invulnerabilityTicks());
  eq(88,CombatHealthPolicy.constrain(100,88,100,30,false));
  ApollyonConfig.immunity=10;
  Boss first=new Boss(),second=new Boss();first.write(0);second.write(0);eq(70,first.health);eq(70,second.health);
  first.healthProtection.tick();eq(9,first.healthProtection.invulnerabilityTicks());eq(10,second.healthProtection.invulnerabilityTicks());
  first.pageant.protectedNow=true;first.shield=100;first.write(0);eq(70,first.health);eq(100,first.shield);
  first.write(100);eq(100,first.health); // Phase lock still allows healing.
  first.healthProtection.beginRestore();first.healthProtection.beginRestore();first.healthProtection.endRestore();
  first.write(2);eq(2,first.health);first.healthProtection.endRestore();first.write(0);eq(2,first.health);
  try {first.healthProtection.endRestore();throw new AssertionError();}catch(IllegalStateException expected){}
  Boss custom=new Boss(){public double combatDamageCap(){return getMaxHealth()*.1;}public int combatHitInvulnerabilityTicks(){return 3;}};
  custom.write(0);eq(90,custom.health);eq(3,custom.healthProtection.invulnerabilityTicks());
  custom.maximum=200;custom.healthProtection.setInvulnerabilityTicks(0);custom.write(0);eq(70,custom.health);
  System.out.println("PASS "+checks+" health checks: raw writes, cleared vanilla timer, cap, healing, shield, phases, restore, death and exception cleanup");
 }
}
'''.replace('METHODS', methods)
library = ROOT.parent / 'star_fantasy_library/src/main/java/com/starfantasy/library/combat'
level = OUT / 'net/minecraft/world/level/Level.java'
level.parent.mkdir(parents=True, exist_ok=True)
level.write_text('package net.minecraft.world.level; public class Level { public boolean isClientSide; }')
living = OUT / 'net/minecraft/world/entity/LivingEntity.java'
living.parent.mkdir(parents=True, exist_ok=True)
living.write_text('package net.minecraft.world.entity; public abstract class LivingEntity { public abstract float getHealth(); public abstract float getMaxHealth(); public abstract net.minecraft.world.level.Level level(); }')
policies = [library / (name + '.java') for name in ['CombatHealthPolicy', 'CombatHealthEntity', 'CombatHealthProtection']]
(OUT / 'CombatHealthRegression.java').write_text(fixture, encoding='utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(OUT),
                *map(str, policies), str(level), str(living), str(OUT / 'CombatHealthRegression.java')], check=True)
subprocess.run(['java', '-ea', '-cp', str(OUT), 'CombatHealthRegression'], check=True)
print('Fixtures:', OUT)
