"""Run the real anchor interaction and pageant aggro handlers with small world fixtures."""
from pathlib import Path
import json
import re
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
aggro = (ROOT / 'src/mojang/java/com/starfantasy/goety/combat/ApollyonPageantAggro.java').read_text('utf-8')
anchor = (ROOT / 'src/church/java/com/starfantasy/goety/church/EternalRespawnAnchorBlock.java').read_text('utf-8')
controller = (ROOT / 'src/main/java/com/starfantasy/goety/combat/ApollyonPageantController.java').read_text('utf-8')

def method(source, marker):
    start = source.index(marker)
    end = source.index('{', start) + 1
    depth = 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]

aggro = 'static ' + aggro[aggro.index('public final class ApollyonPageantAggro'):]
aggro = re.sub(r'@SubscribeEvent(?:\([^)]*\))?\s*', '', aggro)
aggro = aggro.replace('net.minecraft.server.level.ServerLevel', 'ServerLevel')
states = '\n'.join(re.findall(r'public static final int (?:INACTIVE|FADE_OUT|SUMMONING|FIRST_TRIO|GLORIOUS_STAGE|SECOND_TRIO|THIRD_DPS|FOURTH_STAGE|FIFTH_STAGE|PENDING_RESTART) = [^;]+;', controller))
fixture = r'''
import java.util.*;
public class RecallPageantRegression {
 static int checks;
 static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 static final ServerLevel WORLD=new ServerLevel();
 static class Entity {
  final UUID id=UUID.randomUUID(); ServerLevel level=WORLD;double x,y,z;boolean alive=true;
  Entity(){level.entities.put(id,this);}UUID getUUID(){return id;}Level level(){return level;}
  Level m_9236_(){return level;}boolean isAlive(){return alive;}boolean m_6084_(){return alive;}
  double getX(){return x;}double getY(){return y;}double getZ(){return z;}
 }
 static class LivingEntity extends Entity {
  float health=20;float getHealth(){return health;}float m_21223_(){return health;}
  double m_20280_(Entity e){return (x-e.x)*(x-e.x)+(y-e.y)*(y-e.y)+(z-e.z)*(z-e.z);}
 }
 static class Vec3 {double x,y,z;}
 static class Level { boolean client; boolean isClientSide(){return client;} }
 static class ServerLevel extends Level {
  final Map<UUID,Entity> entities=new HashMap<>(); Entity getEntity(UUID id){return entities.get(id);}
 }
 static class Navigation {int stops; void stop(){stops++;}}
 static class MemoryModuleType<T> {
  static final MemoryModuleType<LivingEntity> ATTACK_TARGET=new MemoryModuleType<>(),HURT_BY_ENTITY=new MemoryModuleType<>();
  static final MemoryModuleType<Object> HURT_BY=new MemoryModuleType<>();
  static final MemoryModuleType<Object> LOOK_TARGET=new MemoryModuleType<>();
  static final MemoryModuleType<WalkTarget> WALK_TARGET=new MemoryModuleType<>();
  static final MemoryModuleType<UUID> ANGRY_AT=new MemoryModuleType<>();
 }
 static class EntityTracker {Entity entity;EntityTracker(Entity e){entity=e;}Entity getEntity(){return entity;}}
 record WalkTarget(Object target){Object getTarget(){return target;}}
 static class Brain {
  final Map<MemoryModuleType<?>,Object> values=new HashMap<>();
  boolean hasMemoryValue(MemoryModuleType<?> m){return values.containsKey(m);}
  <T> Optional<T> getMemory(MemoryModuleType<T> m){if(!values.containsKey(m))throw new AssertionError("unregistered memory read");return Optional.of((T)values.get(m));}
  <T> void setMemory(MemoryModuleType<T> m,T value){values.put(m,value);}
  <T> void setMemoryWithExpiry(MemoryModuleType<T> m,T value,long ticks){setMemory(m,value);}
  void eraseMemory(MemoryModuleType<?> m){values.remove(m);}
 }
 static class Mob extends LivingEntity {
  LivingEntity target,lastHurt,lastVictim;
  final Brain brain=new Brain();final Navigation nav=new Navigation();boolean using=true,aggressive=true;
  LivingEntity getTarget(){return target;}void setTarget(LivingEntity t){
   var e=new LivingChangeTargetEvent(this,t);ApollyonPageantAggro.changeTarget(e);if(!e.canceled)target=e.target;
  }
  LivingEntity getLastHurtByMob(){return lastHurt;}void setLastHurtByMob(LivingEntity t){lastHurt=t;}
  LivingEntity getLastHurtMob(){return lastVictim;}void setLastHurtMob(LivingEntity t){lastVictim=t;}
  Brain getBrain(){return brain;}Navigation getNavigation(){return nav;}
  void stopUsingItem(){using=false;}void setAggressive(boolean b){aggressive=b;}
 }
 static class Summoned extends Mob {LivingEntity priority;int time=100;
  LivingEntity getPriorityTarget(){return priority;}void setPriorityTarget(LivingEntity t){priority=t;}void setPriorityTime(int n){time=n;}}
 interface NeutralMob {UUID getPersistentAngerTarget();void setPersistentAngerTarget(UUID id);void setRemainingPersistentAngerTime(int n);}
 static class AngryMob extends Mob implements NeutralMob {UUID anger;int remaining=100;
  public UUID getPersistentAngerTarget(){return anger;}public void setPersistentAngerTarget(UUID id){anger=id;}public void setRemainingPersistentAngerTime(int n){remaining=n;}}
 enum AngerLevel {ANGRY;int getMinimumAnger(){return 80;}}
 static class Warden extends Mob {int angerCalls;LivingEntity angerTarget;
  void increaseAngerAt(Entity e,int amount,boolean listen){angerCalls++;angerTarget=(LivingEntity)e;}
  void setAttackTarget(LivingEntity t){brain.setMemory(MemoryModuleType.ATTACK_TARGET,t);}}
 static class ApollyonPageantController {
  STATES
  final ApollyonEntity boss;UUID gloriousUuid;UUID[] thirdActorUuids=new UUID[2];
  ApollyonPageantController(ApollyonEntity b){boss=b;}
  static ApollyonPageantApostleEntity actor(ServerLevel level,UUID id){return level.getEntity(id) instanceof ApollyonPageantApostleEntity a?a:null;}
  int state;
  LOCKED
  INVULNERABLE
  REDIRECT_TARGET
  ATTACKABLE_ACTOR
 }
 static class ApollyonEntity extends Mob implements ApollyonPageantAggro.Encounter {
  static final double ARENA_RADIUS=20;final ApollyonPageantController controller=new ApollyonPageantController(this);
  boolean isPageantCombatLocked(){return controller.isCombatLocked();}int getPageantState(){return controller.state;}
  Vec3 arenaHomePosition(){return new Vec3();}
  public ApollyonPageantApostleEntity pageantRedirectTarget(LivingEntity e){return controller.redirectTarget(e);}
 }
 static class ApollyonPageantApostleEntity extends Mob {
  final UUID owner;boolean protectedByPillar,damageable=true;
  ApollyonPageantApostleEntity(ApollyonEntity b){owner=b.id;}
  UUID pageantOwnerUuid(){return owner;}boolean isMonolithProtected(){return protectedByPillar;}
  boolean isPageantDamageable(){return damageable;}
 }
 static class LivingChangeTargetEvent {
   Mob entity;LivingEntity target;boolean canceled;
  LivingChangeTargetEvent(LivingEntity e){this(new Mob(),e);}
  LivingChangeTargetEvent(Mob m,LivingEntity e){entity=m;target=e;}
   LivingEntity getEntity(){return entity;}LivingEntity getNewTarget(){return target;}void setNewTarget(LivingEntity e){target=e;}
   void setCanceled(boolean value){canceled=value;}
 }
 static class LivingEvent {static class LivingTickEvent {LivingEntity entity;LivingTickEvent(LivingEntity e){entity=e;}LivingEntity getEntity(){return entity;}}}
 AGGRO
 static void tick(Mob m){ApollyonPageantAggro.tick(new LivingEvent.LivingTickEvent(m));}

 static class BlockPos {} static class BlockHitResult {} enum InteractionHand {MAIN_HAND,OFF_HAND}
 enum InteractionResult {PASS,CONSUME}
 static class ItemStack {Object item;boolean bound;ItemStack(Object i){item=i;}Object getItem(){return item;}}
 static class Player {ItemStack held;ItemStack getItemInHand(InteractionHand h){return held;}}
 interface IWand {static ItemStack getFocus(ItemStack s){return ((Wand)s.item).focus;}}
 static class Wand implements IWand {ItemStack focus;Wand(ItemStack f){focus=f;}}
 static class RecallFocus {static boolean hasRecall(ItemStack s){return s.bound;}}
 static class BlockState {int charge;BlockState setValue(Object key,int c){charge=c;return this;}}
 static class AnchorBase {static final Object CHARGE=new Object();static final int MAX_CHARGES=4;int used;
  public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){used++;return InteractionResult.CONSUME;}}
 static class Anchor extends AnchorBase {ANCHOR_USE}
 static InteractionResult click(Anchor a,ItemStack held){Player p=new Player();p.held=held;return a.use(new BlockState(),new Level(),new BlockPos(),p,InteractionHand.MAIN_HAND,new BlockHitResult());}
 public static void main(String[] args){
  var boss=new ApollyonEntity();var other=new Mob();
  for(int state:new int[]{Controller.FADE_OUT,Controller.SUMMONING,Controller.FIRST_TRIO,Controller.GLORIOUS_STAGE,Controller.SECOND_TRIO,Controller.THIRD_DPS,Controller.FOURTH_STAGE,Controller.FIFTH_STAGE,Controller.PENDING_RESTART}){
   boss.controller.state=state;check(boss.controller.isInvulnerable(),"every pageant state is protected");
    var event=new LivingChangeTargetEvent(boss);ApollyonPageantAggro.changeTarget(event);
    check(event.canceled&&event.target==boss,"new target rejected without passing null to brain AI");
   var m=new Mob();m.target=m.lastHurt=m.lastVictim=boss;tick(m);
   check(m.target==null&&m.lastHurt==null&&m.lastVictim==null,"existing target and retaliation cleared");
   check(m.nav.stops==1&&!m.using&&!m.aggressive,"stop shooting and chasing host");
  }
  var summoned=new Summoned();summoned.priority=boss;tick(summoned);check(summoned.priority==null&&summoned.time==0,"Goety priority cleared");
  var brainMob=new Mob();var b=brainMob.brain;brainMob.level.entities.put(boss.id,boss);
  b.setMemory(MemoryModuleType.ATTACK_TARGET,boss);b.setMemory(MemoryModuleType.HURT_BY_ENTITY,boss);b.setMemory(MemoryModuleType.HURT_BY,new Object());
  b.setMemory(MemoryModuleType.LOOK_TARGET,new EntityTracker(boss));b.setMemory(MemoryModuleType.WALK_TARGET,new WalkTarget(new EntityTracker(boss)));b.setMemory(MemoryModuleType.ANGRY_AT,boss.id);
  tick(brainMob);check(b.values.isEmpty()&&brainMob.nav.stops==1,"brain attack/look/chase/anger cleared");
  var angry=new AngryMob();angry.anger=boss.id;angry.level.entities.put(boss.id,boss);tick(angry);check(angry.anger==null&&angry.remaining==0,"persistent neutral anger cleared");
  var mixed=new Mob();mixed.target=other;mixed.lastHurt=boss;mixed.brain.setMemory(MemoryModuleType.ATTACK_TARGET,other);tick(mixed);
  check(mixed.target==other&&mixed.lastHurt==null&&mixed.nav.stops==0&&mixed.using,"other combat unaffected");
  boss.controller.state=Controller.INACTIVE;check(!boss.controller.isInvulnerable(),"normal combat resumes");
  var normal=new Mob();normal.target=normal.lastHurt=boss;tick(normal);check(normal.target==boss&&normal.lastHurt==boss&&normal.nav.stops==0,"inactive boss remains targetable");
  var event=new LivingChangeTargetEvent(boss);ApollyonPageantAggro.changeTarget(event);check(event.target==boss,"inactive target accepted");
  event=new LivingChangeTargetEvent(other);ApollyonPageantAggro.changeTarget(event);check(event.target==other,"pageant apostles and other mobs remain targetable");
  boss.controller.state=Controller.FIRST_TRIO;normal.level=new ServerLevel();normal.level.client=true;tick(normal);check(normal.target==boss,"client AI untouched");
  REDIRECTION_CHECKS
  var anchor=new Anchor();var recall=new ItemStack(new RecallFocus());
  check(click(anchor,recall)==InteractionResult.PASS&&anchor.used==0,"loose unbound focus reaches native binding");
  check(click(anchor,new ItemStack(new Wand(recall)))==InteractionResult.PASS&&anchor.used==0,"wand focus reaches native binding");
  recall.bound=true;check(click(anchor,new ItemStack(new Wand(recall)))==InteractionResult.CONSUME,"bound focus preserves anchor interaction");
  check(click(anchor,new ItemStack(new Object()))==InteractionResult.CONSUME,"other items preserve anchor interaction");
  check(click(anchor,new ItemStack(new Wand(new ItemStack(new Object()))))==InteractionResult.CONSUME,"other spells preserve anchor interaction");
  System.out.println("PASS: "+checks+" anchor binding, pageant invulnerability and aggro checks.");
 }
}
'''.replace('AGGRO', aggro).replace('ANCHOR_USE', method(anchor, '@Override public InteractionResult use(')).replace('STATES', states).replace('LOCKED', method(controller, 'public boolean isCombatLocked(')).replace('INVULNERABLE', method(controller, 'public boolean isInvulnerable(')).replace('REDIRECT_TARGET', method(controller, 'public ApollyonPageantApostleEntity redirectTarget(')).replace('ATTACKABLE_ACTOR', method(controller, 'private static boolean isAttackableActor('))
fixture = re.sub(r'\bController\.', 'ApollyonPageantController.', fixture)
fixture = fixture.replace('REDIRECTION_CHECKS', r'''
  var encounter=new ApollyonEntity();encounter.controller.state=Controller.FIRST_TRIO;
  var waiting=new Summoned();waiting.setTarget(encounter);waiting.priority=encounter;
  tick(waiting);check(waiting.target==null&&waiting.priority==null,"transition stops attacks on host");
  for(int i=0;i<200;i++)tick(waiting);
  var glorious=new ApollyonPageantApostleEntity(encounter);encounter.controller.gloriousUuid=glorious.id;
  encounter.controller.state=Controller.GLORIOUS_STAGE;tick(waiting);
  check(waiting.target==glorious&&waiting.priority==glorious,"combatant resumes after empty phase");
  check(waiting.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)==glorious,"brain follows redirect");
  check(waiting.brain.getMemory(MemoryModuleType.ANGRY_AT).orElseThrow().equals(glorious.id),"anger UUID follows redirect");
  int stops=waiting.nav.stops;for(int i=0;i<200;i++)tick(waiting);
  check(waiting.nav.stops==stops,"stable target does not interrupt ongoing attacks every tick");
  var newcomer=new Mob();newcomer.setTarget(encounter);
  check(newcomer.target==glorious,"new boss aggro routes immediately instead of being erased");tick(newcomer);
  var brainOnly=new Mob();brainOnly.brain.setMemory(MemoryModuleType.ATTACK_TARGET,encounter);tick(brainOnly);
  check(brainOnly.target==glorious,"brain-only attacker redirects");
  var priorityOnly=new Summoned();priorityOnly.priority=encounter;tick(priorityOnly);
  check(priorityOnly.target==glorious&&priorityOnly.priority==glorious,"Goety priority-only attacker redirects");
  var unrelatedMob=new Mob();tick(unrelatedMob);check(unrelatedMob.target==null,"idle bystander is not recruited");
  glorious.alive=false;encounter.controller.state=Controller.SECOND_TRIO;tick(waiting);
  check(waiting.target==null&&waiting.priority==null&&!waiting.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET),"retired actor clears all attack routes");
  var risen=new ApollyonPageantApostleEntity(encounter);var witch=new ApollyonPageantApostleEntity(encounter);
  risen.x=2;witch.x=15;risen.protectedByPillar=true;
  encounter.controller.thirdActorUuids=new UUID[]{risen.id,witch.id};encounter.controller.state=Controller.THIRD_DPS;tick(waiting);
  check(waiting.target==witch&&waiting.priority==witch,"ignore closer protected archer");
  witch.protectedByPillar=true;risen.protectedByPillar=false;tick(waiting);
  check(waiting.target==risen&&waiting.priority==risen,"pillar protection switches target and priority together");
  check(waiting.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)==risen,"pillar switch updates brain too");
  risen.protectedByPillar=true;tick(waiting);check(waiting.target==null&&waiting.priority==null,"no attackable actor means pause");
  risen.protectedByPillar=false;tick(waiting);check(waiting.target==risen,"resume when protection ends");
  witch.protectedByPillar=false;risen.x=19;witch.x=1;stops=waiting.nav.stops;tick(waiting);
  check(waiting.target==risen&&waiting.nav.stops==stops,"valid current actor retained despite closer alternative");
  var nearest=new Mob();nearest.setTarget(encounter);check(nearest.target==witch,"fresh aggro chooses nearest valid actor");
  var protectedNew=new Mob();risen.protectedByPillar=true;protectedNew.setTarget(risen);
  check(protectedNew.target==witch,"direct target attempt on protected actor redirects");
  var phantom=new ApollyonPageantApostleEntity(encounter);phantom.damageable=false;protectedNew.setTarget(phantom);
  check(protectedNew.target==witch,"invulnerable pageant prop never becomes redirect target");
  var warden=new Warden();warden.setTarget(encounter);tick(warden);
  check(warden.target==witch&&warden.angerTarget==witch&&warden.angerCalls==1,"warden receives replacement anger and brain target");
  tick(warden);check(warden.angerCalls==1,"warden anger is not pumped every tick");
  var neutral=new AngryMob();neutral.target=encounter;neutral.anger=encounter.id;tick(neutral);
  check(neutral.target==witch&&neutral.anger.equals(witch.id),"neutral persistent anger follows replacement");
  for(double[] position:new double[][]{{20.01,0,0},{0,8.01,0},{15,0,15}}){
   var outside=new Mob();outside.x=position[0];outside.y=position[1];outside.z=position[2];outside.setTarget(encounter);tick(outside);
   check(outside.target==null&&!ApollyonPageantAggro.PARTICIPANTS.containsKey(outside),"outside arena is not enlisted");
  }
  var edge=new Mob();edge.x=20;edge.y=8;edge.setTarget(encounter);check(edge.target==witch,"arena edge is included");
  var leaving=new Mob();encounter.controller.state=Controller.SECOND_TRIO;leaving.setTarget(encounter);tick(leaving);
  leaving.x=21;tick(leaving);leaving.x=0;encounter.controller.state=Controller.THIRD_DPS;tick(leaving);
  check(leaving.target==null,"leaving arena clears participation across phase gap");
  var cancelled=new Mob();encounter.controller.state=Controller.SECOND_TRIO;cancelled.setTarget(encounter);tick(cancelled);
  encounter.controller.state=Controller.PENDING_RESTART;tick(cancelled);encounter.controller.state=Controller.THIRD_DPS;tick(cancelled);
  check(cancelled.target==null,"wipe/reset forgets previous encounter");
  var ended=new Mob();encounter.controller.state=Controller.SECOND_TRIO;ended.setTarget(encounter);tick(ended);
  encounter.controller.state=Controller.INACTIVE;tick(ended);encounter.controller.state=Controller.THIRD_DPS;tick(ended);
  check(ended.target==null,"pageant completion forgets previous encounter");
  var offworld=new Mob();offworld.setTarget(encounter);offworld.level=new ServerLevel();tick(offworld);
  check(!ApollyonPageantAggro.PARTICIPANTS.containsKey(offworld),"dimension change forgets encounter");
  var defeated=new Mob();defeated.setTarget(encounter);defeated.alive=false;tick(defeated);
  check(!ApollyonPageantAggro.PARTICIPANTS.containsKey(defeated),"dead attacker forgets encounter");
  var distracted=new Mob();encounter.controller.state=Controller.SECOND_TRIO;distracted.setTarget(encounter);tick(distracted);
  distracted.setTarget(other);encounter.controller.state=Controller.THIRD_DPS;tick(distracted);
  check(distracted.target==other,"unrelated combat is not stolen by remembered participation");
  var mixedCombat=new Mob();mixedCombat.target=other;mixedCombat.brain.setMemory(MemoryModuleType.ATTACK_TARGET,encounter);tick(mixedCombat);
  check(mixedCombat.target==other&&mixedCombat.using,"clearing stale brain aggro does not stop unrelated combat");
  var deadHostWaiter=new Mob();encounter.controller.state=Controller.SECOND_TRIO;deadHostWaiter.setTarget(encounter);tick(deadHostWaiter);
  encounter.alive=false;tick(deadHostWaiter);check(!ApollyonPageantAggro.PARTICIPANTS.containsKey(deadHostWaiter),"dead host releases participants");
'''.replace('Controller.', 'ApollyonPageantController.'))
out = ROOT / 'build' / ('recall-pageant-check-' + str(time.time_ns()))
out.mkdir(parents=True)
source = out / 'RecallPageantRegression.java'
source.write_text(fixture, 'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(source)],check=True)
subprocess.run(['java','-cp',str(out),'RecallPageantRegression'],check=True)
tag=json.loads((ROOT/'src/main/resources/data/goety/tags/blocks/recall_blocks.json').read_text('utf-8'))
assert tag['replace'] is False and tag['values']==['starfantasy_goety:eternal_respawn_anchor']
protection=json.loads((ROOT/'src/main/resources/data/star_fantasy_library/tags/blocks/protected_interactions.json').read_text('utf-8'))
assert 'starfantasy_goety:eternal_respawn_anchor' in protection['values']
bosses=json.loads((ROOT/'src/main/resources/data/forge/tags/entity_types/bosses.json').read_text('utf-8'))
assert bosses['replace'] is False and bosses['values']==['starfantasy_goety:apollyon']
print('PASS: additive Goety recall/Boss tags and church interaction allowlist.')
