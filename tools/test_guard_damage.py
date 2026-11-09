"""Production guard event handlers, with a minimal vanilla hurt-order fixture."""
from pathlib import Path
from collections import Counter
import json, re, subprocess, time

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/mojang/java/com/starfantasy/goety'
source=(JAVA/'magic/guard/GuardChannel.java').read_text('utf-8')
body='static '+source[source.index('public final class GuardChannel'):]
body=re.sub(r'@SubscribeEvent(?:\([^)]*\))?\s*','',body)
immunity=(JAVA/'magic/guard/GuardInvulnerability.java').read_text('utf-8')
immunity='static '+immunity[immunity.index('public final class GuardInvulnerability'):]
immunity=re.sub(r'@SubscribeEvent(?:\([^)]*\))?\s*','',immunity)
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
 static class Level {boolean isClientSide;long time;long getGameTime(){return time;}void addFreshEntity(Entity e){}}
 static class ServerLevel extends Level {}
 static class Entity {String type="minecraft:zombie";Object getType(){return type;}}
 interface GuardHurtMemoryAccessor {float starfantasy$getLastHurt();void starfantasy$setLastHurt(float a);}
 static class LivingEntity extends Entity implements GuardHurtMemoryAccessor {
  Level level=new ServerLevel();ItemStack staff=new ItemStack(new Wand());boolean alive=true,using=true,creative,removed;
  int souls=100,rewards,cooldowns,invulnerableTime,vfx;float lastHurt,health=1000;float armorFactor=1;
  Level level(){return level;}Level serverLevel(){return level;}boolean isAlive(){return alive;}boolean isRemoved(){return removed;}
  boolean isUsingItem(){return using;}boolean isCreative(){return creative;}ItemStack getUseItem(){return staff;}
  void stopUsingItem(){using=false;}Vec3 position(){return new Vec3(0,0,0);}Vec3 getLookAngle(){return new Vec3(0,0,1);}
  public float starfantasy$getLastHurt(){return lastHurt;}public void starfantasy$setLastHurt(float a){lastHurt=a;}
 }
 static class Player extends LivingEntity {}
 static class ServerPlayer extends Player {}
 interface IWand {static ItemStack getFocus(ItemStack s){return new ItemStack(new GuardFocusItem());}}
 static class Wand implements IWand{} static class GuardFocusItem{}
 static class ItemStack {Object item;ItemStack(Object i){item=i;}Object getItem(){return item;}}
 static class GuardShieldEntity extends Entity {boolean removed;void initialize(LivingEntity p){}void discard(){removed=true;}}
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
 static class StarFantasyVfx {static void guardClash(LivingEntity p,Vec3 v){p.vfx++;}}
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
  LivingEntity player;DamageSource source;float amount;boolean canceled;
  LivingAttackEvent(LivingEntity p,DamageSource s,float a){player=p;source=s;amount=a;}
  LivingEntity getEntity(){return player;}DamageSource getSource(){return source;}float getAmount(){return amount;}
  void setCanceled(boolean b){canceled=b;}
 }
 static class LivingHurtEvent extends LivingAttackEvent {LivingHurtEvent(LivingEntity p,DamageSource s,float a){super(p,s,a);}void setAmount(float a){amount=a;}}
 static class TickEvent {
  enum Phase{START,END}
  static class PlayerTickEvent {Phase phase=Phase.END;Entity player;PlayerTickEvent(Entity p){player=p;}}
  static class ServerTickEvent {Phase phase=Phase.END;}
 }
 static class PlayerEvent {
  Entity player;PlayerEvent(Entity p){player=p;}Entity getEntity(){return player;}
  static class PlayerLoggedOutEvent extends PlayerEvent{PlayerLoggedOutEvent(Entity p){super(p);}}
  static class PlayerChangedDimensionEvent extends PlayerEvent{PlayerChangedDimensionEvent(Entity p){super(p);}}
 }
 static class LivingDeathEvent extends LivingAttackEvent {LivingDeathEvent(LivingEntity p){super(p,new DamageSource(),0);}}
 static class ServerStoppedEvent{}
 RULES
 IMMUNITY
 CHANNEL
 static ServerPlayer start(double reduction){
  MinecraftForge.EVENT_BUS.events.clear();
  GuardChannel.stopped(new ServerStoppedEvent());GuardInvulnerability.stopped(new ServerStoppedEvent());SpellConfig.GUARD_DAMAGE_REDUCTION.value=reduction;
  SpellConfig.GUARD_INVULNERABILITY.value=20;ServerPlayer p=new ServerPlayer();GuardChannel.begin(p,p.staff);return p;
 }
 // Forge's Player attack hook precedes vanilla hurt immunity. LivingHurtEvent happens
 // inside actuallyHurt; the outer hurt method writes lastHurt and 20 immunity afterwards.
 static float hit(ServerPlayer p,DamageSource source,float raw){
  LivingAttackEvent attack=new LivingAttackEvent(p,source,raw);GuardChannel.attacked(attack);if(attack.canceled)return 0;GuardInvulnerability.attack(attack);if(attack.canceled)return 0;
  boolean fresh=p.invulnerableTime<=10||source.cooldownBypass;
  if(!fresh&&raw<=p.lastHurt)return 0;
  LivingHurtEvent hurt=new LivingHurtEvent(p,source,fresh?raw:raw-p.lastHurt);GuardInvulnerability.hurt(hurt);if(hurt.canceled)return 0;GuardChannel.hurt(hurt);
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
  check(p.invulnerableTime==20&&p.lastHurt==memory&&GuardInvulnerability.active(p),"extra duration survives vanilla assignment and cast ending, without overwriting hurt memory");
  check(hit(p,new DamageSource(),40)==0,"subsequent equal damage obeys hurt immunity");
  p=start(1);SpellConfig.GUARD_INVULNERABILITY.value=80;
  check(hit(p,new DamageSource(),40)==0&&p.invulnerableTime==0&&p.lastHurt==0&&GuardInvulnerability.active(p),"default preserves immediate full cancellation and hurt memory");
  for(double r:new double[]{.5,1}){
   p=start(r);SpellConfig.GUARD_INVULNERABILITY.value=0;DamageSource s=new DamageSource();s.cooldownBypass=true;
   hit(p,s,10);hit(p,s,10);hit(p,s,10);
   check(MinecraftForge.EVENT_BUS.events.size()==3,"each guarded hit notifies boss independently of soul reward "+r);
   check(MinecraftForge.EVENT_BUS.events.get(0).source==s&&MinecraftForge.EVENT_BUS.events.get(0).player==p,"original attacker/projectile source and player preserved");
   check(p.rewards==1&&p.vfx==1,"only first reward and one feedback per tick "+r);
   p.level.time++;hit(p,s,10);check(p.rewards==1&&p.vfx==2,"later feedback does not duplicate reward");
   check(!GuardInvulnerability.WINDOWS.containsKey(p),"zero configured immunity does not queue extension");
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
  check(!GuardInvulnerability.WINDOWS.containsKey(p),"logout clears pending extension");
  p=start(.5);hit(p,new DamageSource(),20);GuardChannel.changedDimension(new PlayerEvent.PlayerChangedDimensionEvent(p));
  check(!GuardInvulnerability.WINDOWS.containsKey(p),"dimension change clears pending extension");
  p=start(.5);hit(p,new DamageSource(),20);GuardInvulnerability.stopped(new ServerStoppedEvent());
  check(GuardInvulnerability.WINDOWS.isEmpty(),"server stop clears extension state");
  for(double r:new double[]{.5,1}){
   start(r);LivingEntity mob=new LivingEntity();mob.using=false;
   GuardChannel.beginSpell(mob,mob.staff,12);
   GuardShieldEntity shield=GuardChannel.ACTIVE.get(mob).shield;
   check(GuardChannel.isActive(mob),"non-player channel needs no item-use state");
   LivingAttackEvent attack=new LivingAttackEvent(mob,new DamageSource(),40);GuardChannel.attacked(attack);
   LivingHurtEvent hurt=new LivingHurtEvent(mob,attack.source,40);if(!attack.canceled)GuardChannel.hurt(hurt);
   check(r==1?attack.canceled:hurt.amount==20,"non-player full/partial mitigation");
   check(mob.rewards==0&&MinecraftForge.EVENT_BUS.events.isEmpty(),"non-player does not use player soul/reward APIs");
   GuardChannel.tickSpells(new TickEvent.ServerTickEvent());
   check(mob.invulnerableTime==0&&GuardInvulnerability.active(mob),"non-player immunity applied after hit");
   mob.level.time=2;GuardChannel.refreshSpell(mob);mob.level.time=4;
   check(GuardChannel.isActive(mob),"spell refresh keeps channel alive");
   mob.level.time=5;GuardChannel.tickSpells(new TickEvent.ServerTickEvent());
   check(!GuardChannel.isActive(mob)&&shield.removed,"missing stop callback expires safely after refresh stops");
   GuardChannel.beginSpell(mob,mob.staff,12);shield=GuardChannel.ACTIVE.get(mob).shield;
   GuardChannel.finish(mob);check(shield.removed&&!GuardChannel.isActive(mob),"explicit stop removes non-player shield");
   GuardChannel.beginSpell(mob,mob.staff,12);mob.level=new ServerLevel();GuardChannel.tickSpells(new TickEvent.ServerTickEvent());
   check(!GuardChannel.ACTIVE.containsKey(mob),"dimension change closes non-player channel");
   GuardChannel.beginSpell(mob,mob.staff,12);mob.removed=true;GuardChannel.tickSpells(new TickEvent.ServerTickEvent());
   check(!GuardChannel.ACTIVE.containsKey(mob),"removed caster closes non-player channel");
  }
  p=start(.5);SpellConfig.GUARD_INVULNERABILITY.value=10;hit(p,new DamageSource(),20);GuardChannel.finish(p);
  p.invulnerableTime=0;DamageSource immuneSource=new DamageSource();immuneSource.cooldownBypass=true;
  check(hit(p,immuneSource,500)==0,"extra immunity blocks larger damage even with native frames cleared");
  p.level.time=9;check(GuardInvulnerability.active(p),"10 ticks covers time 9");
  p.level.time=10;check(!GuardInvulnerability.active(p),"immunity expires at time 10");
  GuardInvulnerability.grant(p,10);check(GuardInvulnerability.clear(p)&&!GuardInvulnerability.active(p),"mirror API clears only independent immunity");
  check(hit(p,immuneSource,500)==500,"damage lands after explicit clear");
  GuardInvulnerability.grant(p,10);DamageSource bypass=new DamageSource();bypass.bypass=true;bypass.cooldownBypass=true;
  check(hit(p,bypass,5)==5,"bypass-invulnerability damage is not swallowed");
  GuardInvulnerability.grant(p,10);p.level=new ServerLevel();check(!GuardInvulnerability.active(p),"dimension identity invalidates immunity");
  GuardInvulnerability.grant(p,10);p.alive=false;check(!GuardInvulnerability.active(p),"dead entity cannot keep immunity");
  System.out.println("PASS: "+checks+" production guard event checks.");
 }
}
'''
out=ROOT/f'build/guard-damage-regression-{time.time_ns()}'
out.mkdir(parents=True)
p=out/'GuardDamageRegression.java'
p.write_text(fixture.replace('RULES',rules).replace('CHANNEL',body).replace('IMMUNITY',immunity),'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'GuardDamageRegression'],check=True)
recipe=json.loads((ROOT/'src/main/resources/data/starfantasy_goety/recipes/guard_focus.json').read_text('utf-8'))
assert recipe['activation_item']=={'item':'goety:bulwark_focus'}
assert Counter(i['item'] for i in recipe['ingredients'])==Counter({
    'minecraft:enchanted_golden_apple':4,'minecraft:echo_shard':1,'minecraft:totem_of_undying':1,
    'minecraft:netherite_ingot':1,'goety:soul_ruby':1})
assert (recipe['craftType'],recipe['duration'],recipe['soulCost'])==('magic',10,1)
print('PASS: exact eight ingredients, Bulwark center, and existing ritual duration/cost.')
