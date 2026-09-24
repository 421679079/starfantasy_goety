"""Production guard event handlers, with a minimal vanilla hurt-order fixture."""
from pathlib import Path
from collections import Counter
import json, re, subprocess, time

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/mojang/java/com/starfantasy/goety'
source=(JAVA/'magic/guard/GuardChannel.java').read_text('utf-8')
body='static '+source[source.index('public final class GuardChannel'):]
body=re.sub(r'@SubscribeEvent(?:\([^)]*\))?\s*','',body)
rules=(JAVA/'magic/guard/GuardRules.java').read_text('utf-8')
rules='static '+rules[rules.index('public final class GuardRules'):]
fixture=r'''
import java.util.*;
public class GuardDamageRegression {
 static int checks;
 static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 static class Vec3 {
  double x,y,z;Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
  Vec3 subtract(Vec3 v){return new Vec3(x-v.x,y-v.y,z-v.z);}Vec3 add(Vec3 v){return new Vec3(x+v.x,y+v.y,z+v.z);}
  Vec3 add(double a,double b,double c){return new Vec3(x+a,y+b,z+c);}double lengthSqr(){return x*x+y*y+z*z;}
  Vec3 scale(double a){return new Vec3(x*a,y*a,z*a);}Vec3 normalize(){return scale(1/Math.sqrt(lengthSqr()));}
 }
 static class Level {long time;long getGameTime(){return time;}void addFreshEntity(Entity e){}}
 static class Entity {String type="minecraft:zombie";Object getType(){return type;}}
 interface GuardHurtMemoryAccessor {float starfantasy$getLastHurt();void starfantasy$setLastHurt(float a);}
 static class ServerPlayer extends Entity implements GuardHurtMemoryAccessor {
  Level level=new Level();ItemStack staff=new ItemStack(new Wand());boolean alive=true,using=true,creative;
  int souls=100,rewards,cooldowns,invulnerableTime,vfx;float lastHurt,health=1000;float armorFactor=1;
  Level level(){return level;}Level serverLevel(){return level;}boolean isAlive(){return alive;}
  boolean isUsingItem(){return using;}boolean isCreative(){return creative;}ItemStack getUseItem(){return staff;}
  void stopUsingItem(){using=false;}Vec3 position(){return new Vec3(0,0,0);}Vec3 getLookAngle(){return new Vec3(0,0,1);}
  public float starfantasy$getLastHurt(){return lastHurt;}public void starfantasy$setLastHurt(float a){lastHurt=a;}
 }
 interface IWand {static ItemStack getFocus(ItemStack s){return new ItemStack(new GuardFocusItem());}}
 static class Wand implements IWand{} static class GuardFocusItem{}
 static class ItemStack {Object item;ItemStack(Object i){item=i;}Object getItem(){return item;}}
 static class GuardShieldEntity extends Entity {void initialize(ServerPlayer p){}void discard(){}}
 static class ShieldType {GuardShieldEntity create(Level l){return new GuardShieldEntity();}}
 static class Value<T>{T value;Value(T v){value=v;}T get(){return value;}}
 static class SpellConfig {
  static Value<Integer> GUARD_SOUL_COST=new Value<>(20),GUARD_SOUL_REWARD=new Value<>(40),GUARD_INVULNERABILITY=new Value<>(20);
  static Value<Double> GUARD_DAMAGE_REDUCTION=new Value<>(1.0);
  static Value<List<String>> GUARD_BLACKLIST=new Value<>(List.of("minecraft:area_effect_cloud","goety:hellfire"));
 }
 static class GuardFocusContent {static Value<ShieldType> SHIELD=new Value<>(new ShieldType());static Value<GuardFocusItem> GUARD_FOCUS=new Value<>(new GuardFocusItem());}
 static class GuardSpell {static GuardSpell INSTANCE=new GuardSpell();static int duration(ItemStack s){return 10;}int spellCooldown(ServerPlayer p){return 30;}}
 static class Cooldown {void removeCooldown(ServerPlayer p,Level l,Object f){p.cooldowns=0;}}
 static class SEHelper {
  static boolean getSoulsAmount(ServerPlayer p,int a){return p.souls>=a;}static void decreaseSouls(ServerPlayer p,int a){p.souls-=a;}
  static void increaseSouls(ServerPlayer p,int a){p.souls+=a;p.rewards++;}static void sendSEUpdatePacket(ServerPlayer p){}
  static Cooldown getFocusCoolDown(ServerPlayer p){return new Cooldown();}static void addCooldown(ServerPlayer p,Object f,int n){p.cooldowns=n;}
 }
 static class StarFantasyVfx {static void guardClash(ServerPlayer p,Vec3 v){p.vfx++;}}
 static class StarFantasyLibraryNetwork {static void sendShake(ServerPlayer p,int n,float s){}}
 static class PerfectGuardEvent {
  ServerPlayer player;DamageSource source;
  PerfectGuardEvent(ServerPlayer p,DamageSource s){player=p;source=s;}
 }
 static class GuardProbeEvent extends LivingAttackEvent {
  GuardProbeEvent(ServerPlayer p,DamageSource s){super(p,s,1);}
 }
 static class GuardBus {List<PerfectGuardEvent> events=new ArrayList<>();void post(PerfectGuardEvent e){events.add(e);}}
 static class MinecraftForge {static GuardBus EVENT_BUS=new GuardBus();}
 static class Registry {Object getKey(Object t){return t;}}static class ForgeRegistries {static Registry ENTITY_TYPES=new Registry();}
 static class DamageTypeTags {static Object BYPASSES_INVULNERABILITY=new Object();}
 static class DamageSource {
  Entity direct=new Entity(),attacker=direct;Vec3 origin=new Vec3(0,0,4);boolean bypass,cooldownBypass;
  Entity getDirectEntity(){return direct;}Entity getEntity(){return attacker;}Vec3 getSourcePosition(){return origin;}
  boolean is(Object tag){return bypass;}
 }
 static class LivingAttackEvent {
  Entity player;DamageSource source;float amount;boolean canceled;
  LivingAttackEvent(Entity p,DamageSource s,float a){player=p;source=s;amount=a;}
  Entity getEntity(){return player;}DamageSource getSource(){return source;}float getAmount(){return amount;}
  void setCanceled(boolean b){canceled=b;}
 }
 static class LivingHurtEvent extends LivingAttackEvent {LivingHurtEvent(Entity p,DamageSource s,float a){super(p,s,a);}void setAmount(float a){amount=a;}}
 static class TickEvent {
  enum Phase{START,END}
  static class PlayerTickEvent {Phase phase=Phase.END;Entity player;PlayerTickEvent(Entity p){player=p;}}
 }
 static class PlayerEvent {
  Entity player;PlayerEvent(Entity p){player=p;}Entity getEntity(){return player;}
  static class PlayerLoggedOutEvent extends PlayerEvent{PlayerLoggedOutEvent(Entity p){super(p);}}
  static class PlayerChangedDimensionEvent extends PlayerEvent{PlayerChangedDimensionEvent(Entity p){super(p);}}
 }
 static class ServerStoppedEvent{}
 RULES
 CHANNEL
 static ServerPlayer start(double reduction){
  MinecraftForge.EVENT_BUS.events.clear();
  GuardChannel.stopped(new ServerStoppedEvent());SpellConfig.GUARD_DAMAGE_REDUCTION.value=reduction;
  SpellConfig.GUARD_INVULNERABILITY.value=20;ServerPlayer p=new ServerPlayer();GuardChannel.begin(p,p.staff);return p;
 }
 // Forge's Player attack hook precedes vanilla hurt immunity. LivingHurtEvent happens
 // inside actuallyHurt; the outer hurt method writes lastHurt and 20 immunity afterwards.
 static float hit(ServerPlayer p,DamageSource source,float raw){
  LivingAttackEvent attack=new LivingAttackEvent(p,source,raw);GuardChannel.attacked(attack);if(attack.canceled)return 0;
  boolean fresh=p.invulnerableTime<=10||source.cooldownBypass;
  if(!fresh&&raw<=p.lastHurt)return 0;
  LivingHurtEvent hurt=new LivingHurtEvent(p,source,fresh?raw:raw-p.lastHurt);GuardChannel.hurt(hurt);
  float result=hurt.amount*p.armorFactor;p.health-=result;p.lastHurt=raw;if(fresh)p.invulnerableTime=20;return result;
 }
 public static void main(String[] args){
  for(double r:new double[]{0,.25,.5,.75,1}){
   ServerPlayer p=start(r);float damage=hit(p,new DamageSource(),40);
   check(Math.abs(damage-40*(1-r))<.00001,"configured ratio "+r);
   check(p.rewards==1&&p.souls==120,"one charge and one reward "+r);
   check(MinecraftForge.EVENT_BUS.events.size()==(r>0?1:0),"only positive mitigation grants boss reward "+r);
   GuardChannel.finish(p);check(p.cooldowns==0,"partial and full successes waive cooldown");
  }
  ServerPlayer p=start(.5);p.armorFactor=.5f;
  check(hit(p,new DamageSource(),40)==10,"remaining damage still passes through armor");
  p=start(.5);SpellConfig.GUARD_INVULNERABILITY.value=80;
  LivingAttackEvent a=new LivingAttackEvent(p,new DamageSource(),40);GuardChannel.attacked(a);
  check(!a.canceled&&p.invulnerableTime==0&&p.rewards==0,"partial guard cannot apply immunity before damage");
  check(hit(p,a.source,40)==20&&p.invulnerableTime==20,"partial hit lands before extra immunity");
  float memory=p.lastHurt;GuardChannel.finish(p);GuardChannel.tick(new TickEvent.PlayerTickEvent(p));
  check(p.invulnerableTime==80&&p.lastHurt==memory,"extra duration survives vanilla assignment and cast ending, without overwriting hurt memory");
  check(hit(p,new DamageSource(),40)==0,"subsequent equal damage obeys hurt immunity");
  p=start(1);SpellConfig.GUARD_INVULNERABILITY.value=80;
  check(hit(p,new DamageSource(),40)==0&&p.invulnerableTime==80&&p.lastHurt==40,"default preserves immediate full cancellation and hurt memory");
  for(double r:new double[]{.5,1}){
   p=start(r);SpellConfig.GUARD_INVULNERABILITY.value=0;DamageSource s=new DamageSource();s.cooldownBypass=true;
   hit(p,s,10);hit(p,s,10);hit(p,s,10);
   check(MinecraftForge.EVENT_BUS.events.size()==3,"each guarded hit notifies boss independently of soul reward "+r);
   check(MinecraftForge.EVENT_BUS.events.get(0).source==s&&MinecraftForge.EVENT_BUS.events.get(0).player==p,"original attacker/projectile source and player preserved");
   check(p.rewards==1&&p.vfx==1,"only first reward and one feedback per tick "+r);
   p.level.time++;hit(p,s,10);check(p.rewards==1&&p.vfx==2,"later feedback does not duplicate reward");
   check(!GuardChannel.PENDING_IMMUNITY.containsKey(p),"zero configured immunity does not queue extension");
  }
  for(double r:new double[]{.5,1})for(int invalid=0;invalid<10;invalid++){
   p=start(r);DamageSource s=new DamageSource();
   switch(invalid){case 0:s.origin=new Vec3(0,0,-4);break;case 1:s.direct=s.attacker=null;break;
    case 2:s.origin=null;break;case 3:s.direct=s.attacker=p;break;case 4:s.bypass=true;break;
    case 5:s.direct.type="goety:hellfire";break;case 6:s.attacker=new Entity();s.attacker.type="minecraft:area_effect_cloud";break;
    case 7:p.level.time=10;break;case 8:p.using=false;break;case 9:p.staff=new ItemStack(new Wand());break;}
   LivingAttackEvent attack=new LivingAttackEvent(p,s,40);GuardChannel.attacked(attack);
   LivingHurtEvent hurt=new LivingHurtEvent(p,s,40);GuardChannel.hurt(hurt);
   check(!attack.canceled&&hurt.amount==40&&p.rewards==0,"unchanged eligibility rules "+r+"/"+invalid);
   check(MinecraftForge.EVENT_BUS.events.isEmpty(),"invalid guard cannot grant boss reward");
   GuardProbeEvent probe=new GuardProbeEvent(p,s);GuardChannel.attacked(probe);
   check(!probe.canceled&&MinecraftForge.EVENT_BUS.events.isEmpty(),"probe enforces the same eligibility rules");
  }
  for(double r:new double[]{0,.5,1}){
   p=start(r);DamageSource s=new DamageSource();GuardProbeEvent probe=new GuardProbeEvent(p,s);
   GuardChannel.attacked(probe);
   check(probe.canceled==(r>0),"full and partial guards resolve probe without LivingHurtEvent "+r);
   check(MinecraftForge.EVENT_BUS.events.size()==(r>0?1:0)&&p.health==1000,"probe grants one reward without health damage");
   if(r>0)check(MinecraftForge.EVENT_BUS.events.get(0).source==s,"probe preserves scripted shadow source");
  }
  p=start(.5);p.level.time=10;GuardChannel.tick(new TickEvent.PlayerTickEvent(p));
  check(p.cooldowns==30&&!p.using,"unsuccessful cast still expires with cooldown");
  p=start(.5);hit(p,new DamageSource(),20);GuardChannel.logout(new PlayerEvent.PlayerLoggedOutEvent(p));
  check(!GuardChannel.PENDING_IMMUNITY.containsKey(p),"logout clears pending extension");
  p=start(.5);hit(p,new DamageSource(),20);GuardChannel.changedDimension(new PlayerEvent.PlayerChangedDimensionEvent(p));
  check(!GuardChannel.PENDING_IMMUNITY.containsKey(p),"dimension change clears pending extension");
  p=start(.5);hit(p,new DamageSource(),20);GuardChannel.stopped(new ServerStoppedEvent());
  check(GuardChannel.PENDING_IMMUNITY.isEmpty(),"server stop clears extension state");
  System.out.println("PASS: "+checks+" production guard event checks.");
 }
}
'''
out=ROOT/f'build/guard-damage-regression-{time.time_ns()}'
out.mkdir(parents=True)
p=out/'GuardDamageRegression.java'
p.write_text(fixture.replace('RULES',rules).replace('CHANNEL',body),'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'GuardDamageRegression'],check=True)
recipe=json.loads((ROOT/'src/main/resources/data/starfantasy_goety/recipes/guard_focus.json').read_text('utf-8'))
assert recipe['activation_item']=={'item':'goety:bulwark_focus'}
assert Counter(i['item'] for i in recipe['ingredients'])==Counter({
    'minecraft:enchanted_golden_apple':4,'minecraft:echo_shard':1,'minecraft:totem_of_undying':1,
    'minecraft:netherite_ingot':1,'goety:soul_ruby':1})
assert (recipe['craftType'],recipe['duration'],recipe['soulCost'])==('magic',10,1)
print('PASS: exact eight ingredients, Bulwark center, and existing ritual duration/cost.')
