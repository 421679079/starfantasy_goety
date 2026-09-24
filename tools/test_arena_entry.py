"""Exercise production arena methods with Java fixtures (not an in-game test)."""
from pathlib import Path
import re
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "src/main/java/com/starfantasy/goety/entity/ApollyonEntity.java").read_text("utf-8")
controller = (ROOT / "src/main/java/com/starfantasy/goety/combat/ApollyonPageantController.java").read_text("utf-8")
damage_rules = (ROOT / "src/main/java/com/starfantasy/goety/combat/ApollyonDamageRules.java").read_text("utf-8")
damage_rules = re.sub(r"^(?:package|import) .*?;\s*", "", damage_rules, flags=re.MULTILINE)
damage_rules = damage_rules.replace("public final class ApollyonDamageRules", "static final class ApollyonDamageRules")


def block(marker):
    start = source.index(marker)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


methods = "\n".join(block(marker) for marker in [
    "private boolean canEnterArena(", "public boolean isValidArenaTarget(",
    "public boolean isWithinArenaCombatRange(", "private boolean isArenaDamageSource(",
    "public boolean m_6779_(", "public void m_6710_(", "private void clearDistantAggro(",
    "private void applyArenaMembership(", "private void constrainArenaPlayers(",
    "private void constrainArenaEntity(", "public List<ServerPlayer> validArenaPlayers(",
    "private boolean isHorizontalPositionInsideArena(", "public boolean m_6469_(",
])
constants = "\n".join(re.findall(
    r"    (?:public|private) static final (?:double|int) "
    r"(?:ARENA_SIZE|ARENA_RADIUS|ARENA_JOIN_RADIUS|ARENA_ENTRY_HEIGHT|ARENA_DISENGAGE_DISTANCE_SQR|"
    r"SAFE_BOUNDARY_KNOCKBACK_INTERVAL_TICKS|SAFE_BOUNDARY_KNOCKBACK_HORIZONTAL|"
    r"SAFE_BOUNDARY_KNOCKBACK_VERTICAL) = .*?;", source))
# Both ordinary targeting and pageant retries must share the eligibility gate.
assert "!this.boss.isValidArenaTarget(target)" in controller
assert "teleportArenaTargetHome" not in source
assert block("public boolean m_6469_(").index("isArenaDamageSource") < block("public boolean m_6469_(").index("setAntiRegen")
assert "isValidArenaTarget(target)" in block("private void tickArenaState(")
assert "this.arenaPlayers.clear()" in block("private void setArenaActive(")

fixture = r"""
import java.util.*;
public class ArenaEntryRegression {
    CONSTANTS
    DAMAGE_RULES
    static int checks;
    static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    record Vec3(double f_82479_, double f_82480_, double f_82481_) {
        double m_82557_(Vec3 b) {
            return Math.pow(f_82479_-b.f_82479_,2)+Math.pow(f_82480_-b.f_82480_,2)+Math.pow(f_82481_-b.f_82481_,2);
        }
    }
    static class Level { boolean f_46443_; long m_46467_() { return 100; } }
    static class ServerLevel extends Level {
        Map<UUID,ServerPlayer> players = new HashMap<>();
        Collection<ServerPlayer> m_6907_() { return players.values(); }
        ServerLevel m_7654_() { return this; }
        ServerLevel m_6846_() { return this; }
        ServerPlayer m_11259_(UUID id) { return players.get(id); }
    }
    static class Entity {
        Level level; UUID id=UUID.randomUUID(); Vec3 pos, velocity=new Vec3(0,0,0);
        boolean f_19864_,f_19812_; float f_19789_; int f_19802_;
        Entity(Level level, double x, double y, double z) { this.level=level; move(x,y,z); }
        void move(double x, double y, double z) { pos=new Vec3(x,y,z); }
        double m_20185_() { return pos.f_82479_; }
        double m_20186_() { return pos.f_82480_; }
        double m_20189_() { return pos.f_82481_; }
        UUID m_20148_() { return id; }
        Level m_9236_() { return level; }
        Vec3 m_20182_() { return pos; }
        Vec3 m_20184_() { return velocity; }
        void m_20256_(Vec3 v) { velocity=v; }
        void m_6021_(double x,double y,double z) { move(x,y,z); }
        float m_146908_() { return 0; }
        float m_146909_() { return 0; }
    }
    static class LivingEntity extends Entity {
        boolean alive=true; int hurts,effects; LivingEntity target,attacker,victim;
        Player f_20888_;
        LivingEntity(Level level,double x,double y,double z) { super(level,x,y,z); }
        boolean m_6084_() { return alive; }
        LivingEntity m_5448_() { return target; }
        LivingEntity m_21188_() { return attacker; }
        LivingEntity m_21214_() { return victim; }
        void m_6703_(LivingEntity t) { attacker=t; }
        void m_21335_(LivingEntity t) { victim=t; }
        void m_6598_(Player t) { f_20888_=t; }
        void m_6710_(LivingEntity t) { target=t; }
        boolean m_6779_(LivingEntity t) { return t != this && t.alive; }
        boolean m_6469_(DamageSource s,float amount) { hurts++; return true; }
        void m_6074_() { alive=false; }
        void m_147207_(MobEffectInstance e,LivingEntity cause) { effects++; }
        DamageSources m_269291_() { return new DamageSources(); }
    }
    static class Player extends LivingEntity {
        boolean creative,spectator;
        Player(Level level,double x,double y,double z) { super(level,x,y,z); }
        boolean m_7500_() { return creative; }
        boolean m_5833_() { return spectator; }
    }
    static class ServerPlayer extends Player {
        final Connection f_8906_=new Connection(); int teleports;
        ServerPlayer(ServerLevel level,double x,double y,double z) { super(level,x,y,z); level.players.put(id,this); }
        class Connection {
            void m_9774_(double x,double y,double z,float yaw,float pitch) { move(x,y,z); teleports++; }
        }
    }
    static class Projectile extends Entity {
        Entity owner;
        Projectile(Level level,Entity owner) { super(level,100,64,-200); this.owner=owner; }
        Entity m_19749_() { return owner; }
    }
    static class Owned extends LivingEntity {
        Entity owner;
        Owned(Level level,Entity owner) { super(level,100,64,-200); this.owner=owner; }
        Entity getTrueOwner() { return owner; }
    }
    static class DamageSource {
        Entity cause,direct; Vec3 position; Object type;
        DamageSource(Entity cause,Entity direct,Vec3 position) { this.cause=cause; this.direct=direct; this.position=position; }
        DamageSource(Object type) { this.type=type; }
        DamageSource(Object type,Vec3 position) { this.type=type; this.position=position; }
        DamageSource(Object type,Entity direct,Entity cause) { this.type=type; this.direct=direct; this.cause=cause; }
        Object m_269150_() { return type; }
        Entity m_7639_() { return cause; }
        Entity m_7640_() { return direct; }
        Vec3 m_7270_() { return position != null ? position : direct == null ? null : direct.pos; }
        boolean m_276093_(Object key) { return type == key; }
    }
    static class DamageTypes { static final Object f_286979_=new Object(),f_268612_=new Object(),f_268671_=new Object(); }
    static class DamageSources {
        DamageSource m_269341_() { return new DamageSource(null,null,(Vec3)null); }
        DamageSource m_269264_() { return new DamageSource(new Object()); }
    }
    static class Navigation { int stops; void m_26573_() { stops++; } }
    static class Pageant { int kills; boolean isInvulnerable() { return false; } void discardFromKill() { kills++; } }
    static class ApollyonConfig { static boolean hard; static boolean hardMode() { return hard; } }
    static class StarFantasyTrueKillHelper {
        static void trueKillPlayer(ServerPlayer player,float amount,String reason) { player.alive=false; }
    }
    static class MobEffectInstance { MobEffectInstance(Object effect,int duration,int level) {} }
    static class DoomHolder { Object get() { return this; } }
    static final DoomHolder DOOM=new DoomHolder();
    static class EnchantmentHelper { static int m_44836_(Object e,LivingEntity t) { return 1; } }
    static class Enchantments { static final Object f_44978_=new Object(); }
    static class Mth { static int m_14045_(int n,int min,int max) { return Math.min(max,Math.max(min,n)); } }
    static class Boss extends LivingEntity {
        boolean arenaHomeInitialized=true,lethal,shieldHitInProgress; int receivedHits,antiRegen,combatTeleports;
        static class Protection {int ticks;int invulnerabilityTicks(){return ticks;} void setInvulnerabilityTicks(int value){ticks=value;}}
        final Protection healthProtection=new Protection();
        final Map<UUID,ServerPlayer> arenaPlayers=new HashMap<>();
        final Map<UUID,Long> safeBoundaryKnockbackReadyTicks=new HashMap<>();
        final Navigation navigation=new Navigation(); final Pageant pageant=new Pageant();
        Vec3 home=new Vec3(100,64,-200);
        Boss(ServerLevel level) { super(level,100,64,-200); }
        Vec3 arenaHomePosition() { return home; }
        void ensureArenaHome() { if (!arenaHomeInitialized) { home=pos; arenaHomeInitialized=true; } }
        Navigation m_21573_() { return navigation; }
        void clearPendingTeleport() { combatTeleports=0; }
        boolean isPageantBoundaryLethal() { return lethal; }
        boolean isFriendlyDamageSource(DamageSource s) { return false; }
        boolean isMonolithPower() { return false; }
        boolean hasCooperativeShield() { return false; }
        void updateShieldKnockbackResistance() {}
        void setAntiRegen(int a,int b) { antiRegen=a; }
        void tryCombatTeleport(LivingEntity t) { combatTeleports++; }
        float scaleOutgoingDamage(float amount) { return amount; }
        METHODS
    }
    public static void main(String[] args) {
        ServerLevel level=new ServerLevel(); Boss boss=new Boss(level);
        ServerPlayer newcomer=new ServerPlayer(level,121,64,-200);
        check(!boss.m_6779_(newcomer),"outside player must not be acquired");
        boss.m_6710_(newcomer);
        check(boss.target==null,"direct/retaliation assignment outside must be rejected");
        boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(newcomer.teleports==0 && newcomer.hurts==0,"approaching spectator must not be constrained");
        newcomer.move(120,64,-200);
        check(boss.m_6779_(newcomer),"exact radius 20 is inside");
        boss.m_6710_(newcomer); boss.lethal=true;
        boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(!boss.arenaPlayers.containsKey(newcomer.id),"radius 20 does not finish entry");
        newcomer.move(119.01,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(!boss.arenaPlayers.containsKey(newcomer.id),"outer entry band is not registered");
        newcomer.move(119,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(boss.arenaPlayers.get(newcomer.id)==newcomer,"entry must register participant");
        check(newcomer.hurts==0 && newcomer.teleports==0 && newcomer.effects==0,"inward entry must be safe even during lethal stage");
        check(boss.validArenaPlayers().size()==1,"participant visible to pageant");
        ServerPlayer observer=new ServerPlayer(level,130,64,-200);
        boss.m_6710_(observer);
        check(boss.target==newcomer,"outside observer must not replace active target");
        boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(observer.teleports==0 && observer.hurts==0,"active encounter must not pull outsiders");
        observer.move(119.9,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(boss.validArenaPlayers().size()==1,"late entrant must first reach radius 19");
        observer.move(120.01,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(observer.alive && observer.teleports==0 && observer.hurts==0,"late entrant crossing back out must not be punished");
        observer.move(119,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(boss.validArenaPlayers().size()==2 && observer.hurts==0,"late entrant joins safely");
        observer.move(119.9,64,-200); boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(boss.arenaPlayers.get(observer.id)==observer,"returning to entry band does not unregister participant");

        newcomer.move(125,64,-200);
        check(boss.m_6779_(newcomer),"existing target remains valid outside 20");
        newcomer.f_19802_=20; boss.constrainArenaPlayers();
        check(newcomer.teleports==1 && newcomer.hurts==1 && newcomer.f_19802_==0,"existing normal lethal boundary punishment preserved");
        newcomer.move(148,64,-200);
        check(boss.isValidArenaTarget(newcomer),"exact 48 still retained");
        newcomer.move(148.01,64,-200); boss.attacker=newcomer; boss.victim=newcomer; boss.f_20888_=newcomer;
        boss.clearDistantAggro(); boss.constrainArenaPlayers();
        check(boss.target==null && boss.attacker==null && boss.victim==null && boss.f_20888_==null,"beyond 48 must clear all aggro memory");
        check(newcomer.teleports==1 && !boss.arenaPlayers.containsKey(newcomer.id),"beyond 48 exits without pull");

        observer.alive=false;
        ServerPlayer respawn=new ServerPlayer(level,130,64,-200);
        level.players.remove(respawn.id); respawn.id=observer.id; level.players.put(respawn.id,respawn);
        check(!boss.isValidArenaTarget(respawn),"same UUID respawn must not inherit entry");
        check(boss.validArenaPlayers().isEmpty(),"old dead player excluded from pageant");
        boss.applyArenaMembership(); boss.constrainArenaPlayers();
        check(!boss.arenaPlayers.containsKey(respawn.id) && respawn.teleports==0,"respawn outside must not be pulled");
        respawn.move(119,64,-200); boss.applyArenaMembership();
        check(boss.isValidArenaTarget(respawn),"respawn can rejoin after entering");

        ServerPlayer below=new ServerPlayer(level,100,55.9,-200);
        ServerPlayer above=new ServerPlayer(level,100,72.1,-200);
        check(!boss.m_6779_(below) && !boss.m_6779_(above),"entry limited to arena height");
        above.move(100,72,-200); check(boss.m_6779_(above),"entry height endpoint");
        above.creative=true; check(!boss.m_6779_(above),"creative excluded");
        above.creative=false; above.spectator=true; check(!boss.m_6779_(above),"spectator excluded");
        ServerPlayer diagonal=new ServerPlayer(level,115,64,-185);
        check(!boss.m_6779_(diagonal),"entry is circle, not bounding square");
        below.move(100,64,-200); below.level=new ServerLevel();
        check(!boss.m_6779_(below),"other dimension excluded");
        Boss noHome=new Boss(level); noHome.arenaHomeInitialized=false; noHome.move(300,10,400);
        check(noHome.canEnterArena(new LivingEntity(level,320,10,400)),"uninitialized home uses spawn position");
        LivingEntity mob=new LivingEntity(level,119,64,-200);
        boss.m_6710_(mob); mob.move(125,64,-200);
        check(boss.isValidArenaTarget(mob),"existing nonplayer target retained");

        ServerPlayer attacker=new ServerPlayer(level,130,64,-200);
        Projectile arrow=new Projectile(level,attacker);
        DamageSource ranged=new DamageSource(attacker,arrow,boss.pos);
        int before=boss.hurts;
        check(!boss.m_6469_(ranged,10) && boss.hurts==before && boss.antiRegen==0,"outside shooter rejected before hurt side effects");
        check(!boss.isArenaDamageSource(new DamageSource(null,arrow,(Vec3)null)),"recover owner from direct projectile");
        check(!boss.isArenaDamageSource(new DamageSource(new Owned(level,attacker),null,(Vec3)null)),"summon uses owner position");
        attacker.move(120,64,-200); arrow.move(140,64,-200);
        check(boss.m_6469_(ranged,10) && boss.hurts==before+1,"inside shooter accepted regardless of projectile coordinate");
        boss.target=null;
        check(boss.m_6469_(new DamageSource(attacker,attacker,(Vec3)null),10),"no idle invulnerability; inside first hit works");
        attacker.move(121,64,-200);
        check(!boss.isArenaDamageSource(ranged),"source checked at impact using current shooter position");
        check(boss.isArenaDamageSource(new DamageSource(null,null,new Vec3(120,64,-200))),"position-only inside accepted");
        check(!boss.isArenaDamageSource(new DamageSource(null,null,new Vec3(120.01,64,-200))),"position-only outside rejected");
        check(!boss.m_6469_(new DamageSource(null,null,(Vec3)null),10),"fully anonymous damage immune");
        Entity direct=new Entity(level,119,64,-200);
        check(boss.isArenaDamageSource(new DamageSource(null,direct,(Vec3)null)),"direct-only entity inside accepted");
        direct.level=new ServerLevel();
        check(!boss.isArenaDamageSource(new DamageSource(direct,null,(Vec3)null)),"damage owner in other dimension rejected");
        check(!boss.isArenaDamageSource(new DamageSource(null,null,new Vec3(Double.NaN,64,-200))),"invalid coordinates rejected");
        DamageSource kill=new DamageSource(null,null,(Vec3)null); kill.type=DamageTypes.f_286979_;
        check(boss.m_6469_(kill,Float.MAX_VALUE) && boss.pageant.kills==1,"existing administrator kill path preserved");

        ServerLevel hardLevel=new ServerLevel(); Boss hardBoss=new Boss(hardLevel);
        hardBoss.lethal=true; ApollyonConfig.hard=true;
        ServerPlayer firstPlayer=new ServerPlayer(hardLevel,100,64,-200);
        ServerPlayer hardPlayer=new ServerPlayer(hardLevel,120,64,-200);
        hardBoss.applyArenaMembership(); hardBoss.constrainArenaPlayers();
        check(hardBoss.arenaPlayers.get(firstPlayer.id)==firstPlayer,"first player already participating");
        for (double radius : new double[]{20.01,19.99,20.01,19.01,20.1}) {
            hardPlayer.move(100+radius,64,-200);
            hardBoss.applyArenaMembership(); hardBoss.constrainArenaPlayers();
            check(hardPlayer.alive && hardPlayer.teleports==0 && !hardBoss.arenaPlayers.containsKey(hardPlayer.id),
                    "late entrant edge jitter must not trigger hard-mode execution at radius "+radius);
        }
        hardPlayer.move(119,64,-200); hardBoss.applyArenaMembership(); hardBoss.constrainArenaPlayers();
        check(hardPlayer.alive && hardBoss.arenaPlayers.get(hardPlayer.id)==hardPlayer,"hard-mode registration exactly at 19");
        hardPlayer.move(120,64,-200); hardBoss.applyArenaMembership(); hardBoss.constrainArenaPlayers();
        check(hardPlayer.alive && hardBoss.arenaPlayers.get(hardPlayer.id)==hardPlayer,"registered player can move up to 20 without resetting entry");
        hardPlayer.move(121,64,-200); hardBoss.constrainArenaPlayers();
        check(!hardPlayer.alive,"hard-mode exit execution preserved");
        for (double[] direction : new double[][]{{1,0},{-1,0},{0,1},{0,-1},{0.6,0.8},{-0.6,-0.8}}) {
            ServerLevel diagonalLevel=new ServerLevel(); Boss diagonalBoss=new Boss(diagonalLevel); diagonalBoss.lethal=true;
            ServerPlayer entrant=new ServerPlayer(diagonalLevel,100+direction[0]*19.01,64,-200+direction[1]*19.01);
            diagonalBoss.applyArenaMembership();
            check(!diagonalBoss.arenaPlayers.containsKey(entrant.id),"entry margin applies in every direction");
            entrant.move(100+direction[0]*18.99,64,-200+direction[1]*18.99); diagonalBoss.applyArenaMembership();
            check(diagonalBoss.arenaPlayers.get(entrant.id)==entrant,"circular entry works in every direction");
            entrant.move(100+direction[0]*20.01,64,-200+direction[1]*20.01); diagonalBoss.constrainArenaPlayers();
            check(!entrant.alive,"registered exit remains lethal in every direction");
        }
        System.out.println("PASS: "+checks+" arena entry, multiplayer/respawn, boundary and source-damage checks.");
    }
}
"""
fixture = fixture.replace("CONSTANTS", constants).replace("METHODS", methods).replace("DAMAGE_RULES", damage_rules)
fixture = fixture.replace("com.starfantasy.goety.combat.ApollyonDamageRules", "ApollyonDamageRules")
fixture = fixture.replace("com.Polarice3.Goety.common.effects.GoetyEffects.DOOM", "DOOM")
out = ROOT / "build" / ("arena-entry-regression-" + str(time.time_ns()))
out.mkdir(parents=True)
java = out / "ArenaEntryRegression.java"
java.write_text(fixture, "utf-8")
subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", str(out), str(java)], check=True)
subprocess.run(["java", "-cp", str(out), "ArenaEntryRegression"], check=True)
print("Production-method fixture:", out)
