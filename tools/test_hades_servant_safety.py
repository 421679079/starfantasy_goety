"""Exercise the production Hades dismissal/fall methods with Java boundary fixtures.

The fixtures model Goety's 60-tick confirmation and vanilla passenger fall forwarding;
this is a focused regression check, not a replacement for an in-game riding test.
"""
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "build" / ("hades-safety-tests-" + str(time.time_ns()))


def method(source, marker):
    start = source.index(marker)
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


entity = (ROOT / "src/main/java/com/starfantasy/goety/entity/HadesServantEntity.java").read_text("utf-8")
events = (ROOT / "src/main/java/com/starfantasy/goety/event/HadesServantCombatEvents.java").read_text("utf-8")
fixture = r"""
public class HadesSafetyRegression {
    static class DamageTypes { static final String f_268671_ = "fall"; }
    record DamageSource(String type) {
        boolean m_276093_(String key) { return type.equals(key); }
    }
    static class Entity {
        Entity vehicle;
        float f_19789_;
        Entity m_20202_() { return vehicle; }
    }
    static class Player extends Entity { }
    static class Summoned extends Entity {
        int killChance, warnings, dismissals, forwardedFalls;
        int getKillChance() { return killChance; }
        void warnKill(Player player) { killChance = 60; warnings++; }
        public void tryKill(Player player) { dismissals++; }
        void tick() { if (killChance > 0) killChance--; }
        public boolean m_142535_(float distance, float multiplier, DamageSource source) {
            forwardedFalls++;
            return true;
        }
    }
    static class HadesServantEntity extends Summoned {
        DISMISS_METHOD
        FALL_METHOD
    }
    static class LivingAttackEvent {
        final Entity entity;
        final DamageSource source;
        boolean canceled;
        LivingAttackEvent(Entity entity, String type) {
            this.entity = entity; source = new DamageSource(type);
        }
        Entity getEntity() { return entity; }
        DamageSource getSource() { return source; }
        void setCanceled(boolean value) { canceled = value; }
    }
    PROTECT_METHOD
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static LivingAttackEvent hit(Entity entity, String type) {
        LivingAttackEvent event = new LivingAttackEvent(entity, type);
        protectRiderFromFall(event);
        return event;
    }
    public static void main(String[] args) {
        Player owner = new Player();
        HadesServantEntity hades = new HadesServantEntity();
        hades.tryKill(owner);
        check(hades.warnings == 1 && hades.dismissals == 0, "First attempt must only warn");
        for (int i = 0; i < 59; i++) hades.tick();
        hades.tryKill(owner);
        check(hades.dismissals == 1, "Second attempt within 60 ticks must dismiss");

        hades = new HadesServantEntity();
        hades.tryKill(owner);
        for (int i = 0; i < 60; i++) hades.tick();
        hades.tryKill(owner);
        check(hades.warnings == 2 && hades.dismissals == 0, "Expired confirmation must warn again");
        hades.tryKill(owner);
        check(hades.dismissals == 1, "Fresh confirmation must still work");
        HadesServantEntity other = new HadesServantEntity();
        other.tryKill(owner);
        check(other.dismissals == 0, "Confirmation belongs to the individual servant");

        Summoned vanilla = new Summoned();
        DamageSource fall = new DamageSource("fall");
        vanilla.m_142535_(250, 1, fall);
        check(vanilla.forwardedFalls == 1, "Fixture must represent vanilla fall propagation");
        hades.f_19789_ = 250;
        check(!hades.m_142535_(250, 1, fall), "Hades landing must not report fall damage");
        check(hades.forwardedFalls == 0 && hades.f_19789_ == 0,
                "Hades must not forward its accumulated fall distance to riders");

        owner.vehicle = hades;
        owner.f_19789_ = 250;
        check(hit(owner, "fall").canceled && owner.f_19789_ == 0, "Mounted player must ignore fall damage");
        for (String type : new String[]{"mob_attack", "arrow", "lava", "out_of_world", "fly_into_wall"}) {
            owner.f_19789_ = 17;
            check(!hit(owner, type).canceled && owner.f_19789_ == 17,
                    "Riding must not grant immunity to " + type);
        }
        owner.vehicle = null;
        owner.f_19789_ = 100;
        check(!hit(owner, "fall").canceled && owner.f_19789_ == 100,
                "Dismounted players must take normal fall damage");
        owner.vehicle = vanilla;
        check(!hit(owner, "fall").canceled, "Other mounts must be unaffected");
        Entity mob = new Entity(); mob.vehicle = hades;
        check(!hit(mob, "fall").canceled, "The event protection is limited to players");
        System.out.println("PASS: dismissal confirmation/expiry, no passenger fall forwarding, mounted-only fall immunity");
    }
}
"""
fixture = fixture.replace("DISMISS_METHOD", method(entity, "public void tryKill(Player player)"))
fixture = fixture.replace("FALL_METHOD", method(entity, "public boolean m_142535_(float fallDistance"))
fixture = fixture.replace("PROTECT_METHOD", method(events, "public static void protectRiderFromFall("))
OUT.mkdir(parents=True)
source = OUT / "HadesSafetyRegression.java"
source.write_text(fixture, encoding="utf-8")
subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", str(OUT), str(source)], check=True)
subprocess.run(["java", "-ea", "-cp", str(OUT), "HadesSafetyRegression"], check=True)
