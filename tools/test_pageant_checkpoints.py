"""Run the controller's actual retry/NBT/transition methods against Java world fixtures."""
from pathlib import Path
import re
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "src/main/java/com/starfantasy/goety/combat/ApollyonPageantController.java").read_text("utf-8")


def block(marker):
    start = source.index(marker)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


methods = "\n".join(block(marker) for marker in [
    "public void tickServer()", "public void read(", "public void write(",
    "private void beginTransition()", "private void beginConfiguredPageant(",
    "private void resetToPending(", "private void setState(",
    "private int restartPhase()", "private static int phaseForState(",
    "private void resolveThirdEnrage(",
])
constants = "\n".join(re.findall(r"    (?:public|private) static final (?:int|String) "
    r"(?:INACTIVE|FADE_OUT|SUMMONING|FIRST_TRIO|GLORIOUS_STAGE|SECOND_TRIO|THIRD_DPS|"
    r"FOURTH_STAGE|FIFTH_STAGE|PENDING_RESTART|STATE_TAG|TEST_PHASE_TAG|CHECKPOINT_TAG|"
    r"LEGACY_PHASE_TWO_STATE|FADE_TICKS|FIFTH_RETURN_TICK) = .*?;", source))
fixture = r"""
import java.util.*;
public class CheckpointRegression {
    CONSTANTS
    static final int THIRD_EXPLOSION_COLOR=0, THIRD_EXPLOSION_LIFETIME=50,
        THIRD_SHAKE_HOLD_TICKS=40, THIRD_SHAKE_FADE_TICKS=20;
    static final float THIRD_EXPLOSION_ALPHA=.76f, THIRD_SHAKE_INTENSITY=3;
    static final double THIRD_EXPLOSION_SIZE=2.0/3;
    static class Mth {
        static int m_14045_(int n,int low,int high) { return Math.max(low,Math.min(n,high)); }
    }
    static class CompoundTag {
        Map<String,Integer> values=new HashMap<>();
        boolean m_128441_(String key) { return values.containsKey(key); }
        int m_128451_(String key) { return values.getOrDefault(key,0); }
        void m_128405_(String key,int n) { values.put(key,n); }
    }
    static class ApollyonConfig {
        static boolean hard, transition=true;
        static boolean hardMode() { return hard; }
        static boolean transitionPageant() { return transition; }
    }
    static class LivingEntity { boolean alive=true, killedByExplosion=true; }
    static class ServerPlayer extends LivingEntity {}
    static class ServerLevel {
        long m_46467_() { return 42; }
        void m_5594_(Object... args) {}
    }
    static class Vec3 { double f_82479_,f_82480_,f_82481_; }
    static class BlockPos {
        static BlockPos m_274561_(double x,double y,double z) { return new BlockPos(); }
    }
    static class SoundSource { static final Object HOSTILE=new Object(); }
    static class MobEffect {}
    static class Holder { MobEffect get() { return new MobEffect(); } }
    static class GoetyEffects { static final Holder SAPPED=new Holder(); }
    static class ApollyonSoundRegistry { static final Holder CAST_OBSIDIAN=new Holder(); }
    static class StarFantasyVfx {
        static void finalExplosion(Object... args) {}
        static void areaImpactShake(Object... args) {}
    }
    static class DamageSource {}
    static class DamageSources {
        DamageSource m_269036_(Object a,Object b) { return new DamageSource(); }
    }
    static class ApollyonEntity {
        static final int COMBAT_PHASE_ONE=1,COMBAT_PHASE_TWO=2;
        int combatPhase=1, visualState, held;
        boolean alive=true;
        float health=50, opacity;
        ServerLevel level=new ServerLevel();
        List<ServerPlayer> players=new ArrayList<>();
        LivingEntity target;
        ApollyonEntity() { players.add(new ServerPlayer()); target=players.get(0); }
        Object m_9236_() { return level; }
        boolean isCombatPhaseOne() { return combatPhase==1; }
        boolean m_6084_() { return alive; }
        float m_21223_() { return health; }
        float m_21233_() { return 100; }
        LivingEntity m_5448_() { return target; }
        void m_6710_(LivingEntity target) { this.target=target; }
        List<ServerPlayer> validArenaPlayers() { return players; }
        void setCombatPhase(int phase) { combatPhase=phase; }
        void clearCombatForPageant() {}
        void holdAtHomeForPageantRetry() { held++; }
        void holdForPageantTransition() {}
        void setPageantState(int state) { visualState=state; }
        void setPageantOpacity(float opacity) { this.opacity=opacity; }
        Vec3 arenaHomePosition() { return new Vec3(); }
        DamageSources m_269291_() { return new DamageSources(); }
    }
    static class Controller {
        ApollyonEntity boss=new ApollyonEntity();
        int state=INACTIVE,stateTicks,directTransitionHealTicks,testPhase=1,checkpointPhase=1;
        int cleanupCalls, startedPhase;
        boolean cleanupAfterLoad, monolithBroken;
        long haloOrbitEpoch;
        boolean[] occupiedOuterHaloAnchors=new boolean[12];
        UUID[] thirdActorUuids={UUID.randomUUID(),UUID.randomUUID()};
        boolean isValidPageantTarget(LivingEntity target) { return target!=null && target.alive; }
        LivingEntity ensureTarget() {
            if(isValidPageantTarget(boss.target)) return boss.target;
            boss.target=boss.players.stream().filter(p->p.alive).findFirst().orElse(null);
            return boss.target;
        }
        void clearEncounter(ServerLevel level,boolean clearPersistent) { cleanupCalls++; }
        void returnForFifthStage(ServerLevel level) {}
        void beginSummoning(ServerLevel level,LivingEntity target) { startedPhase=1; setState(SUMMONING,0); }
        void beginGloriousStage(ServerLevel level) { startedPhase=2; setState(GLORIOUS_STAGE,0); }
        void beginThirdStage(ServerLevel level,LivingEntity target) { startedPhase=3; setState(THIRD_DPS,0); }
        void beginFourthStage(ServerLevel level,LivingEntity target) { startedPhase=4; setState(FOURTH_STAGE,0); }
        void beginFifthStage(ServerLevel level) { startedPhase=5; setState(FIFTH_STAGE,0); }
        void tickFade(ServerLevel level,List<ServerPlayer> players,LivingEntity target) {
            beginConfiguredPageant(level,target);
        }
        void tickSummoning(Object... args) {}
        void tickFirstTrio(Object... args) {}
        void tickGloriousStage(Object... args) {}
        void tickSecondTrio(Object... args) {}
        void tickThirdDps(Object... args) {}
        void tickFourthStage(Object... args) {}
        void tickFifthStage(Object... args) {}
        List<LivingEntity> arenaLivingTargets(ServerLevel level) { return new ArrayList<>(boss.players); }
        void breakThirdMonolith(ServerLevel level) { monolithBroken=true; }
        void departActor(ServerLevel level,UUID id) {}
        void damagePageantTarget(LivingEntity target,DamageSource source,MobEffect effect) {
            check(checkpointPhase==3,"checkpoint advanced before explosion damage");
            if(target.killedByExplosion) target.alive=false;
        }
        METHODS
    }
    static void check(boolean ok,String reason) { if(!ok) throw new AssertionError(reason); }
    static CompoundTag save(Controller c) { CompoundTag tag=new CompoundTag(); c.write(tag); return tag; }
    static void wipe(Controller c) {
        c.boss.players.forEach(p->p.alive=false); c.tickServer();
        check(c.state==PENDING_RESTART,"did not enter pending restart");
    }
    static void retry(Controller c) {
        c.boss.players.add(new ServerPlayer()); c.tickServer();
        check(c.state==FADE_OUT,"retry did not use transition entrance");
        check(c.stateTicks==0,"phase timer was not reset");
        c.tickServer();
    }
    public static void main(String[] args) {
        int[] entrances={SUMMONING,GLORIOUS_STAGE,THIRD_DPS,FOURTH_STAGE,FIFTH_STAGE};
        for(boolean hard:new boolean[]{false,true}) {
            ApollyonConfig.hard=hard;
            for(int phase=1;phase<=5;phase++) {
                Controller c=new Controller(); c.setState(entrances[phase-1],0);
                c.stateTicks=150; wipe(c);
                int expected=hard?1:phase;
                check(c.checkpointPhase==expected,"wrong wipe checkpoint");
                CompoundTag saved=save(c);
                check(saved.m_128451_(CHECKPOINT_TAG)==expected,"wrong NBT checkpoint");
                Controller restored=new Controller();
                restored.read(saved,true);
                check(restored.state==PENDING_RESTART && restored.cleanupAfterLoad,"reload is not a clean retry");
                restored.tickServer();
                check(restored.state==FADE_OUT,"reload target did not restart");
                restored.tickServer();
                check(restored.startedPhase==expected,"wrong phase after save/reload");
                check(restored.cleanupCalls>=2,"reload did not clean old encounter");
                retry(c);
                check(c.startedPhase==expected,"wrong live retry phase");
                c.setState(INACTIVE,1);
                check(c.checkpointPhase==1,"completion left a checkpoint");
            }
        }
        ApollyonConfig.hard=false;
        Controller c=new Controller(); c.setState(THIRD_DPS,0);
        c.resolveThirdEnrage(c.boss.level,c.boss.players);
        check(c.monolithBroken && c.state==PENDING_RESTART && c.checkpointPhase==3,"third-stage wipe incorrectly passed");
        retry(c); check(c.startedPhase==3,"explosion wipe did not retry stage 3");
        c=new Controller(); c.setState(THIRD_DPS,0);
        ServerPlayer survivor=new ServerPlayer(); survivor.killedByExplosion=false; c.boss.players.add(survivor);
        c.resolveThirdEnrage(c.boss.level,c.boss.players);
        check(c.state==FOURTH_STAGE && c.checkpointPhase==4,"surviving group did not earn stage 4");
        c=new Controller(); c.setState(FIRST_TRIO,0); c.boss.players.get(0).alive=false;
        c.setState(GLORIOUS_STAGE,0);
        check(c.checkpointPhase==1,"end-of-stage lethal damage advanced checkpoint");
        Controller restored=new Controller(); restored.read(save(c),true);
        check(restored.checkpointPhase==1,"reload guessed a later stage despite saved checkpoint");
        for(int phase=1;phase<=5;phase++) {
            CompoundTag oldSave=new CompoundTag(); oldSave.m_128405_(STATE_TAG,entrances[phase-1]);
            restored=new Controller(); restored.read(oldSave,true);
            check(restored.checkpointPhase==phase,"legacy active checkpoint inference");
        }
        CompoundTag legacy=new CompoundTag(); legacy.m_128405_(STATE_TAG,LEGACY_PHASE_TWO_STATE);
        restored=new Controller(); restored.read(legacy,false);
        check(restored.state==INACTIVE && restored.boss.combatPhase==2 && restored.checkpointPhase==1,"legacy combat-phase migration");
        c=new Controller(); c.setState(SECOND_TRIO,0); check(c.checkpointPhase==2,"second trio is part of stage 2");
        c.setState(FOURTH_STAGE,0); ApollyonConfig.hard=true; wipe(c); ApollyonConfig.hard=false;
        retry(c); check(c.startedPhase==1,"hard-mode reset retained normal progress");
        c=new Controller(); c.testPhase=3; c.beginConfiguredPageant(c.boss.level,c.ensureTarget());
        check(c.startedPhase==3,"manual test entrance was lost");
        ApollyonConfig.hard=true; c.beginConfiguredPageant(c.boss.level,c.ensureTarget());
        check(c.startedPhase==3 && c.checkpointPhase==1,"hard mode uses explicit test entrance, without checkpoints");
        ApollyonConfig.transition=false; c.beginTransition();
        check(c.state==FIFTH_STAGE && c.stateTicks==FIFTH_RETURN_TICK,"disabled pageant transition changed");
        System.out.println("PASS: five normal/hard checkpoints, NBT reloads, cleanup/retry dispatch, explosion wipe/survivor and legacy/test entrances");
    }
}
""".replace("CONSTANTS", constants).replace("METHODS", methods)
out = ROOT / "build" / ("pageant-checkpoint-tests-" + str(time.time_ns()))
out.mkdir(parents=True)
java = out / "CheckpointRegression.java"
java.write_text(fixture, encoding="utf-8")
subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", str(out), str(java)], check=True)
subprocess.run(["java", "-ea", "-cp", str(out), "CheckpointRegression"], check=True)
