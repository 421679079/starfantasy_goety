"""Exercise the actual flight limiter and compare its bounded ground scan to bosses."""
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / 'src/mojang/java/com/starfantasy/goety/combat/ApollyonArenaFlight.java').read_text('utf-8')
reference = (ROOT.parent / 'star_fantasy_bosses/src/main/java/com/starfantasy/bosses/effect/CagedBirdMobEffect.java').read_text('utf-8')


def method(text, marker):
    start = text.index(marker)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


fixture = r'''
import java.util.*;
public class ArenaFlightRegression {
 static int checks;
 static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 static void near(double a,double b,String message){check(Math.abs(a-b)<1e-9,message+": "+a+" != "+b);}
 enum Direction {UP}
 record BlockPos(int x,int y,int z) {}
 static class Vec3 {
  final double x,y,z;Vec3(double a,double b,double c){x=a;y=b;z=c;}
  double distanceToSqr(Vec3 b){return (x-b.x)*(x-b.x)+(y-b.y)*(y-b.y)+(z-b.z)*(z-b.z);}
 }
 static class MobEffects {static final Object JUMP=new Object();}
 record MobEffectInstance(int amplifier){int getAmplifier(){return amplifier;}}
 static class BlockState {
  boolean solid;BlockState(boolean s){solid=s;}
  boolean isFaceSturdy(ServerLevel l,BlockPos p,Direction d){return solid;}
 }
 static class ServerLevel {
  List<ServerPlayer> players=new ArrayList<>();Set<Integer> supports=new HashSet<>();int reads;
  List<ServerPlayer> players(){return players;}
  int getMinBuildHeight(){return -64;}int getMaxBuildHeight(){return 320;}
  BlockState getBlockState(BlockPos p){reads++;return new BlockState(supports.contains(p.y()));}
 }
 static class Abilities {boolean flying,mayfly;}
 static class ClientboundSetEntityMotionPacket {ClientboundSetEntityMotionPacket(ServerPlayer p){}}
 static class Connection {int packets;void send(ClientboundSetEntityMotionPacket p){packets++;}}
 static class ServerPlayer {
  ServerLevel level;double x=100,y=64,z=-200;Vec3 motion=new Vec3(0,0,0);
  boolean alive=true,creative,spectator,hurtMarked;float fallDistance=25;
  Abilities abilities=new Abilities();Connection connection=new Connection();int abilityPackets;MobEffectInstance jump;
  ServerPlayer(ServerLevel l){level=l;l.players.add(this);}
  boolean isAlive(){return alive;}boolean isCreative(){return creative;}boolean isSpectator(){return spectator;}
  double getX(){return x;}double getY(){return y;}double getZ(){return z;}
  int getBlockX(){return (int)Math.floor(x);}int getBlockY(){return (int)Math.floor(y);}int getBlockZ(){return (int)Math.floor(z);}
  Vec3 position(){return new Vec3(x,y,z);}ServerLevel serverLevel(){return level;}
  Abilities getAbilities(){return abilities;}void onUpdateAbilities(){abilityPackets++;}
  MobEffectInstance getEffect(Object effect){return jump;}
  Vec3 getDeltaMovement(){return motion;}void setDeltaMovement(double a,double b,double c){motion=new Vec3(a,b,c);}
 }
 LIMITER
 REFERENCE
 static void tick(ServerLevel level){ApollyonArenaFlight.tick(level,new Vec3(100,64,-200),20,48*48);}
 static ServerLevel arena(){var level=new ServerLevel();level.supports.add(63);return level;}
 public static void main(String[] args){
  var level=arena();var player=new ServerPlayer(level);player.abilities.flying=player.abilities.mayfly=true;tick(level);
  check(!player.abilities.flying&&player.abilities.mayfly,"cancel flight without revoking entitlement");
  check(player.abilityPackets==1&&player.connection.packets==0,"sync ability cancellation without pushing grounded player");
  tick(level);check(player.abilityPackets==1,"do not resend unchanged flight abilities");
  player.abilities.flying=true;tick(level);check(!player.abilities.flying&&player.abilityPackets==2,"re-enabled mod flight is cancelled again");
  player.x=120.01;player.abilities.flying=true;tick(level);check(player.abilities.flying,"flight can resume outside arena");

  for(boolean spectator:new boolean[]{false,true}){
   var safeLevel=arena();var safe=new ServerPlayer(safeLevel);safe.y=90;safe.creative=!spectator;safe.spectator=spectator;
   safe.abilities.flying=safe.abilities.mayfly=true;safe.motion=new Vec3(2,1,3);tick(safeLevel);
   check(safe.abilities.flying&&safe.abilityPackets==0,"creative and spectator flight exempt");
   check(safe.motion.y==1&&safe.fallDistance==25&&!safe.hurtMarked&&safe.connection.packets==0&&safeLevel.reads==0,"creative and spectator height exempt");
  }
  for(double[] pos:new double[][]{{120.01,70,-200},{115,70,-185},{100,112.01,-200}}){
   var outsideLevel=arena();var outside=new ServerPlayer(outsideLevel);outside.x=pos[0];outside.y=pos[1];outside.z=pos[2];outside.abilities.flying=true;tick(outsideLevel);
   check(outside.abilities.flying&&outside.connection.packets==0,"outside horizontal or combat range untouched");
  }
  var edgeLevel=arena();var edge=new ServerPlayer(edgeLevel);edge.x=120;edge.abilities.flying=true;tick(edgeLevel);
  check(!edge.abilities.flying,"radius 20 edge included");edge.x=100;edge.y=112;tick(edgeLevel);check(edge.motion.y<0,"combat distance 48 edge included");
  var deadLevel=arena();var dead=new ServerPlayer(deadLevel);dead.alive=false;dead.abilities.flying=true;dead.y=80;tick(deadLevel);
  check(dead.abilities.flying&&dead.connection.packets==0,"dead players untouched");
  var otherLevel=arena();var other=new ServerPlayer(otherLevel);other.y=80;other.abilities.flying=true;tick(level);
  check(other.abilities.flying&&other.connection.packets==0,"other dimension untouched");

  var heights=arena();var jumper=new ServerPlayer(heights);double allowed=ApollyonArenaFlight.estimatedJumpHeight(jumper)+3;
  near(allowed,4.252203352512,"ordinary jump allowance");
  for(double offset:new double[]{-1e-8,0,1e-8}){
   jumper.y=64+allowed+offset;
   check(ApollyonArenaFlight.isTooHigh(jumper,allowed)==(offset>0),"strict height boundary including fractional feet");
  }
  jumper.y=69;check(ApollyonArenaFlight.isTooHigh(jumper,allowed),"five blocks is too high without jump boost");
  jumper.jump=new MobEffectInstance(1);double boosted=ApollyonArenaFlight.estimatedJumpHeight(jumper)+3;
  check(boosted>5&&!ApollyonArenaFlight.isTooHigh(jumper,boosted),"jump boost raises permitted height");
  jumper.jump=null;jumper.y=74;jumper.motion=new Vec3(4,0,-8);tick(heights);
  near(jumper.motion.y,-.35,"downward acceleration");near(jumper.motion.x,1,"horizontal damping X");near(jumper.motion.z,-2,"horizontal damping Z");
  check(jumper.fallDistance==0&&jumper.hurtMarked&&jumper.connection.packets==1,"fall distance and velocity synchronized");
  for(int i=0;i<12;i++)tick(heights);near(jumper.motion.y,-2.5,"forced fall speed cap");
  jumper.y=65;int packets=jumper.connection.packets;Vec3 motion=jumper.motion;tick(heights);
  check(jumper.connection.packets==packets&&jumper.motion==motion,"force stops below height allowance");
  jumper.y=100;heights.reads=0;check(ApollyonArenaFlight.isTooHigh(jumper,allowed)&&heights.reads<=6,"high altitude scan stays bounded");

  // Compare against the existing bosses implementation on uneven, empty,
  // below-zero and above-world columns, including fractional height boundaries.
  Random random=new Random(640532);
  for(int trial=0;trial<1200;trial++){
   var column=new ServerLevel();var subject=new ServerPlayer(column);
   subject.y=-80+random.nextDouble()*480;
   subject.jump=trial%4==0?null:new MobEffectInstance(trial%7);
   for(int i=0;i<24;i++)if(random.nextBoolean())column.supports.add(-64+random.nextInt(384));
   double allowance=ApollyonArenaFlight.estimatedJumpHeight(subject)+3;
   boolean expected=heightAboveGround(subject)>allowance;
   check(ApollyonArenaFlight.isTooHigh(subject,allowance)==expected,"bounded scan matches bosses full-depth scan");
  }
  System.out.println("PASS: "+checks+" arena flight, mode exemptions, motion and ground-scan checks.");
 }
}
'''.replace('LIMITER', 'static ' + source[source.index('public final class ApollyonArenaFlight'):]).replace('REFERENCE', method(reference, 'private static double heightAboveGround('))

boss = (ROOT / 'src/main/java/com/starfantasy/goety/entity/ApollyonEntity.java').read_text('utf-8')
tick_arena = method(boss, 'private void tickArenaState(')
assert tick_arena.index('if (!active)') < tick_arena.index('ApollyonArenaFlight.tick(')
assert tick_arena[tick_arena.index('if (!active)'):tick_arena.index('ApollyonArenaFlight.tick(')].count('return;') == 1
assert 'ARENA_RADIUS, ARENA_DISENGAGE_DISTANCE_SQR' in tick_arena
boss_tick = method(boss, 'public void m_8119_(')
assert boss_tick.index('this.tickArenaState(target)') < boss_tick.index('if (this.pageant.isCombatLocked())')

out = ROOT / 'build' / ('arena-flight-check-' + str(time.time_ns()))
out.mkdir(parents=True)
path = out / 'ArenaFlightRegression.java'
path.write_text(fixture, 'utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(path)], check=True)
subprocess.run(['java', '-cp', str(out), 'ArenaFlightRegression'], check=True)
print('PASS: limiter is called in active arenas before the pageant early return.')
