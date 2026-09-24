"""Run the actual judgment methods against player and owned-mob fixtures."""

from pathlib import Path
import subprocess
import tempfile


source = (Path(__file__).resolve().parents[1]
          / "src/main/java/com/starfantasy/goety/combat/ApollyonPageantController.java").read_text("utf-8")


def block(marker):
    start = source.index(marker)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


methods = "\n".join(block(marker) for marker in (
    "private void resolveJudgment(",
    "private static boolean ownedByPassedPlayer(",
))

fixture = r"""
import java.util.*;

public class JudgmentOwnerRegression {
    static final float FIFTH_JUDGMENT_SHAKE_INTENSITY = 1;
    static class Vec3 { double f_82479_, f_82480_, f_82481_; }
    static class BlockPos {
        static BlockPos m_274561_(double x, double y, double z) { return new BlockPos(); }
    }
    static class ServerLevel { void m_5594_(Object... ignored) {} }
    static class SoundSource { static final Object HOSTILE = new Object(); }
    static class Holder { Object get() { return new Object(); } }
    static class ApollyonSoundRegistry { static final Holder CAST_OBSIDIAN = new Holder(); }
    static class StarFantasyVfx { static void areaImpactShake(Object... ignored) {} }
    static class LivingEntity {
        boolean alive = true;
        int shields, shieldTime, killCalls;
        boolean m_6084_() { return alive; }
        void m_6074_() { killCalls++; alive = false; }
    }
    static class ServerPlayer extends LivingEntity {
        final UUID id = UUID.randomUUID();
        int whiteouts, trueKills;
        UUID m_20148_() { return id; }
    }
    interface OwnableEntity { UUID m_21805_(); }
    interface IOwned { UUID getOwnerId(); }
    static class VanillaPet extends LivingEntity implements OwnableEntity {
        final UUID owner;
        VanillaPet(UUID owner) { this.owner = owner; }
        public UUID m_21805_() { return owner; }
    }
    static class GoetyServant extends LivingEntity implements IOwned {
        final UUID owner;
        GoetyServant(UUID owner) { this.owner = owner; }
        public UUID getOwnerId() { return owner; }
    }
    static class MiscCapHelper {
        static int getShields(LivingEntity entity) { return entity.shields; }
        static void setShields(LivingEntity entity, int value) { entity.shields = value; }
        static void setShieldTime(LivingEntity entity, int value) { entity.shieldTime = value; }
    }
    static class StarFantasyGoetyNetwork {
        static void startPageantWhiteout(ServerPlayer player) { player.whiteouts++; }
    }
    static class StarFantasyTrueKillHelper {
        static void trueKillPlayer(ServerPlayer player, float damage, String key) {
            player.trueKills++;
            player.alive = false;
        }
    }
    static class ApollyonEntity {
        Vec3 arenaHomePosition() { return new Vec3(); }
    }
    static class Controller {
        final ApollyonEntity boss = new ApollyonEntity();
        final List<LivingEntity> targets = new ArrayList<>();
        List<LivingEntity> arenaLivingTargets(ServerLevel level) { return targets; }
        METHODS
    }
    static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var pass = new ServerPlayer(); pass.shields = 2; pass.shieldTime = 50;
        var fail = new ServerPlayer();
        var vanillaPet = new VanillaPet(pass.id);
        var goetyServant = new GoetyServant(pass.id);
        var failedPet = new VanillaPet(fail.id);
        var absentPet = new VanillaPet(UUID.randomUUID());
        var shieldedPet = new VanillaPet(fail.id); shieldedPet.shields = 1;
        var wildMob = new LivingEntity();
        var alreadyDead = new LivingEntity(); alreadyDead.alive = false;
        var controller = new Controller();
        // Pets precede players to verify that the result does not depend on entity order.
        Collections.addAll(controller.targets, vanillaPet, failedPet, goetyServant,
                wildMob, shieldedPet, absentPet, alreadyDead, fail, pass);
        controller.resolveJudgment(new ServerLevel(), List.of(pass, fail));
        check(pass.alive && pass.shields == 0 && pass.shieldTime == 0,
                "passing player consumes shield and survives");
        check(!fail.alive && fail.trueKills == 1, "failing player is executed");
        check(pass.whiteouts == 1 && fail.whiteouts == 1, "both players see whiteout");
        check(vanillaPet.alive && goetyServant.alive,
                "vanilla pets and Goety servants of passing players survive");
        check(!failedPet.alive && !absentPet.alive && !wildMob.alive,
                "other unshielded mobs are still killed");
        check(shieldedPet.alive && shieldedPet.shields == 0,
                "a mob with its own shield retains the original survival rule");
        check(alreadyDead.killCalls == 0, "already dead mobs are not killed again");
        System.out.println("PASS: judgment player results precede owned-mob execution");
    }
}
""".replace("METHODS", methods)

with tempfile.TemporaryDirectory(prefix="judgment-owner-test-") as directory:
    java_file = Path(directory) / "JudgmentOwnerRegression.java"
    java_file.write_text(fixture, "utf-8")
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", directory,
                    str(java_file)], check=True)
    subprocess.run(["java", "-ea", "-cp", directory,
                    "JudgmentOwnerRegression"], check=True)
