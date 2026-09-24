"""Exercise the production combat teleport against deterministic world/AI doubles."""
from pathlib import Path
import subprocess, time

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT/'src/main/java/com/starfantasy/goety/combat/ApollyonServantTeleport.java').read_text('utf-8')
body = 'static ' + source[source.index('public final class ApollyonServantTeleport'):]
fixture = r'''
public class TeleportRegression {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static class Vec3 {
  static final Vec3 f_82478_=new Vec3(0,0,0);final double f_82479_,f_82480_,f_82481_;
  Vec3(double x,double y,double z){f_82479_=x;f_82480_=y;f_82481_=z;}
  Vec3 m_82520_(double x,double y,double z){return new Vec3(f_82479_+x,f_82480_+y,f_82481_+z);}
 }
 record BlockPos(int x,int y,int z){
  static BlockPos m_274561_(double x,double y,double z){return new BlockPos((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));}
  int m_123342_(){return y;}BlockPos m_7495_(){return new BlockPos(x,y-1,z);}
 }
 static class AABB {AABB m_82386_(double x,double y,double z){return this;}}
 static class Level {}
 static class Block {boolean solid;Block(boolean s){solid=s;}boolean m_280555_(){return solid;}}
 static class Border {boolean inside=true;boolean m_61937_(BlockPos p){return inside;}}
 static class ServerLevel extends Level {
  boolean loaded=true,collision,liquid,ground=true;int particles;Border border=new Border();
  boolean m_46805_(BlockPos p){return loaded;}Border m_6857_(){return border;}int m_141937_(){return -64;}
  Block m_8055_(BlockPos p){return new Block(ground&&p.y()<=64);}
  boolean m_45756_(Object e,AABB b){return !collision;}boolean m_46855_(AABB b){return liquid;}
  void m_8767_(Object p,double x,double y,double z,int n,double dx,double dy,double dz,double speed){particles+=n;}
 }
 static class LivingEntity {
  Level level;boolean alive=true;double x=10,y=65,z=10;
  Level m_9236_(){return level;}boolean m_6084_(){return alive;}
  double m_20185_(){return x;}double m_20186_(){return y;}double m_20189_(){return z;}
 }
 static class Rand {int shot;int m_188503_(int bound){return bound==4?shot:4;}double m_188500_(){return .5;}}
 static class Sense {boolean visible=true;boolean m_148306_(LivingEntity t){return visible;}}
 static class Navigation {boolean stopped;void m_26573_(){stopped=true;}}
 static class ApollyonServantEntity extends LivingEntity {
  boolean staying,casting,riding,passengers,friendly,teleportFails;int teleports,sounds;float f_19789_=100;
  double distance=100;LivingEntity target=new LivingEntity();Rand rand=new Rand();Sense sense=new Sense();Navigation nav=new Navigation();Vec3 velocity=new Vec3(1,-3,1);
  ApollyonServantEntity(){level=new ServerLevel();target.level=level;x=0;z=0;}
  boolean isStaying(){return staying;}boolean isCastingAction(){return casting;}
  boolean m_20159_(){return riding;}boolean m_20160_(){return passengers;}boolean isFriendlyEntity(LivingEntity t){return friendly;}
  LivingEntity m_5448_(){return target;}Rand m_217043_(){return rand;}Sense m_21574_(){return sense;}
  double m_20280_(LivingEntity t){return distance;}AABB m_20191_(){return new AABB();}Vec3 m_20182_(){return new Vec3(x,y,z);}
  boolean m_20984_(double xx,double yy,double zz,boolean particles){if(teleportFails)return false;teleports++;x=xx;y=yy;z=zz;return true;}
  Navigation m_21573_(){return nav;}void m_20256_(Vec3 v){velocity=v;}float m_20206_(){return 3;}float m_20205_(){return .8f;}
  void m_5496_(SoundEvent s,float v,float p){sounds++;}double m_20208_(double v){return x;}double m_20187_(){return y+1;}double m_20262_(double v){return z;}
 }
 static class Value<T>{T value;Value(T v){value=v;}T get(){return value;}}
 static class MobsConfig{static Value<Boolean> ApostleDelayedTeleport=new Value<>(true);}
 static class SoundEvent{}
 static class ModSounds{static Value<SoundEvent> APOSTLE_PRE_TELEPORT=new Value<>(new SoundEvent()),APOSTLE_TELEPORT=new Value<>(new SoundEvent());}
 static class ParticleTypes{static Object f_123755_=new Object(),f_123762_=new Object();}
 static class ColorUtil{static Object BLACK=new Object();}
 static class AbsorbTrailParticleOption{AbsorbTrailParticleOption(Vec3 v,int c,int t){}}
 static class ServerParticleUtil{
  static void addParticlesAroundMiddleSelf(ServerLevel l,Object p,Object e){}
  static void windParticle(ServerLevel l,Object c,float a,float b,int t,Vec3 v){}
 }
 PRODUCTION
 static void ticks(ApollyonServantTeleport t,ApollyonServantEntity e,int n){for(int i=0;i<n;i++)t.tick(e.target);}
 public static void main(String[] args){
  ApollyonServantEntity e=new ApollyonServantEntity();ApollyonServantTeleport t=new ApollyonServantTeleport(e);
  ticks(t,e,100);check(!t.isPending()&&e.teleports==0,"nearby visible targets do not cause unsolicited teleports");
  e.rand.shot=1;t.onShot(e.target);check(!t.isPending(),"shot chance can miss");
  e.rand.shot=0;t.onShot(e.target);check(t.isPending()&&e.teleports==0,"shot queues a teleport");
  ticks(t,e,19);check(e.teleports==0,"full windup is required");ticks(t,e,1);
  check(e.teleports==1&&!t.isPending(),"teleport finishes at tick 20");
  check(e.y==65&&e.velocity==Vec3.f_82478_&&e.f_19789_==0&&e.nav.stopped,"grounded arrival clears motion and fall distance");
  ticks(t,e,79);t.onShot(e.target);check(!t.isPending(),"cooldown blocks another teleport through tick 79");
  ticks(t,e,1);t.onShot(e.target);check(t.isPending(),"cooldown releases at tick 80");
  t.reset();for(int i=0;i<3;i++)t.onHurt();check(!t.isPending(),"three hits do not trigger");
  e.casting=true;t.tick(e.target);e.casting=false;t.onHurt();check(t.isPending(),"four hits accumulate across a spell");
  for(int mode=0;mode<8;mode++){
   e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);t.onShot(e.target);
   switch(mode){case 0:e.staying=true;break;case 1:e.casting=true;break;case 2:e.alive=false;break;
    case 3:e.target=null;break;case 4:e.target.alive=false;break;case 5:e.friendly=true;break;
    case 6:e.riding=true;break;case 7:e.target.level=new ServerLevel();break;}
   ticks(t,e,30);check(!t.isPending()&&e.teleports==0,"invalid action cancels pending teleport: "+mode);
   t.onShot(e.target);check(!t.isPending(),"invalid action blocks new teleport: "+mode);
  }
  e=new ApollyonServantEntity();e.level=new Level();e.target.level=e.level;t=new ApollyonServantTeleport(e);
  t.onShot(e.target);t.onHurt();ticks(t,e,30);check(e.teleports==0&&!t.isPending(),"client does not teleport");
  for(boolean far:new boolean[]{false,true}){
   e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);e.distance=far?1025:100;e.sense.visible=far;
   t.tick(e.target);check(t.isPending(),"distance or lost sight triggers teleport");
  }
  for(int mode=0;mode<5;mode++){
   e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);ServerLevel l=(ServerLevel)e.level;
   switch(mode){case 0:l.loaded=false;break;case 1:l.border.inside=false;break;case 2:l.collision=true;break;
    case 3:l.liquid=true;break;case 4:l.ground=false;break;}
   t.onShot(e.target);check(!t.isPending()&&e.teleports==0,"unsafe destination rejected: "+mode);
   l.loaded=l.border.inside=l.ground=true;l.collision=l.liquid=false;t.onShot(e.target);
   check(!t.isPending(),"failed search backs off");ticks(t,e,20);t.onShot(e.target);check(t.isPending(),"failed search can retry");
  }
  e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);t.onShot(e.target);
  ((ServerLevel)e.level).collision=true;ticks(t,e,20);check(e.teleports==0&&!t.isPending(),"landing position is rechecked after windup");
  e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);e.teleportFails=true;t.onShot(e.target);ticks(t,e,20);
  check(e.teleports==0&&t.cooldown==20,"native teleport failure is handled");
  MobsConfig.ApostleDelayedTeleport.value=false;e=new ApollyonServantEntity();t=new ApollyonServantTeleport(e);t.onShot(e.target);
  check(e.teleports==0&&t.isPending(),"Apollyon ignores Goety instant teleport setting");
  ticks(t,e,19);check(e.teleports==0,"Apollyon still waits 20 ticks with Goety delay disabled");
  ticks(t,e,1);check(e.teleports==1&&!t.isPending(),"fixed Apollyon windup completes");
  t.reset();check(t.cooldown==0&&t.receivedHits==0&&!t.isPending(),"revival clears transient teleport state");
  System.out.println("PASS: "+checks+" production teleport checks.");
 }
}
'''
out = ROOT/f'build/servant-teleport-regression-{time.time_ns()}'
out.mkdir(parents=True)
java = out/'TeleportRegression.java'
java.write_text(fixture.replace('PRODUCTION', body), 'utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(java)], check=True)
subprocess.run(['java', '-cp', str(out), 'TeleportRegression'], check=True)
