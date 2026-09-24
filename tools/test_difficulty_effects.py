"""Exercise production arrow/pageant effect methods with minimal Java entity fixtures."""
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]


def method(source, signature):
    start = source.index(signature)
    end = source.index("{", start) + 1
    depth = 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


arrow = (ROOT / "src/main/java/com/starfantasy/goety/entity/ApollyonStarArrowEntity.java").read_text("utf-8")
apostle = (ROOT / "src/main/java/com/starfantasy/goety/entity/ApollyonPageantApostleEntity.java").read_text("utf-8")
fixture = r"""
import java.util.*;
public class DifficultyRegression {
    static class Entity {}
    record MobEffect(String id) {}
    record MobEffectInstance(MobEffect effect, int duration, int amplifier) {}
    record Holder(MobEffect value) { MobEffect get() { return value; } }
    static class MobEffects {
        static final MobEffect f_19602_=new MobEffect("harm"),
            f_19614_=new MobEffect("poison"), f_19615_=new MobEffect("wither"),
            f_216964_=new MobEffect("darkness"), f_19613_=new MobEffect("weakness"),
            f_19612_=new MobEffect("hunger"), f_19597_=new MobEffect("slowness");
    }
    static class GoetyEffects {
        static final Holder SAPPED=new Holder(new MobEffect("sapped"));
        static final Holder CURSED=new Holder(new MobEffect("cursed"));
        static final Holder BURN_HEX=new Holder(new MobEffect("burn_hex"));
    }
    static class ApollyonEffectRegistry {
        static final Holder WEAKNESS=new Holder(new MobEffect("custom_weakness"));
    }
    static class ApollyonConfig { static boolean hard; static boolean hardMode() { return hard; } }
    static class LivingEntity extends Entity {
        List<MobEffectInstance> effects=new ArrayList<>();
        Entity source;
        int fireSeconds;
        boolean alive=true;
        boolean m_6084_() { return alive; }
        void m_147207_(MobEffectInstance effect, Entity source) {
            effects.add(effect); this.source=source;
        }
        void m_20254_(int seconds) { fireSeconds=seconds; }
    }
    static class Choice {
        int calls;
        int m_188503_(int bound) {
            if(bound!=8) throw new AssertionError("Unexpected debuff pool size: "+bound);
            return calls++ % bound;
        }
    }
    static class Arrow {
        Choice f_19796_=new Choice();
        ARROW_METHOD
        APPLY_METHOD
    }
    static class Actor extends Entity {
        static final int RISEN=0, WITCH_KING=5;
        int variant;
        float healed;
        Actor(int variant) { this.variant=variant; }
        int variant() { return variant; }
        boolean isPageantArcher() { return variant==RISEN || variant==WITCH_KING; }
        float m_21233_() { return 1000; }
        void m_5634_(float amount) { healed+=amount; }
        ACTOR_METHOD
    }
    static void check(boolean value) { if(!value) throw new AssertionError(); }
    static void effect(LivingEntity target, MobEffect effect, int amplifier) {
        check(target.effects.stream().anyMatch(e->e.effect().equals(effect)
            && e.duration()==200 && e.amplifier()==amplifier));
    }
    public static void main(String[] args) {
        Arrow arrow=new Arrow(); Entity owner=new Entity();
        Set<MobEffect> seen=new HashSet<>();
        for(int i=0;i<16;i++) {
            LivingEntity target=new LivingEntity();
            arrow.applyApostleTitleEffects(target,owner);
            check(target.effects.size()==1);
            MobEffectInstance effect=target.effects.get(0);
            check(!effect.effect().equals(GoetyEffects.BURN_HEX.get()));
            check(effect.amplifier()==0);
            check(effect.duration()==(effect.effect()==MobEffects.f_19602_ ? 1 : 200));
            check(target.source==owner && target.fireSeconds==5);
            seen.add(effect.effect());
        }
        check(seen.size()==8 && arrow.f_19796_.calls==16);
        ApollyonConfig.hard=true;
        LivingEntity hardTarget=new LivingEntity();
        arrow.applyApostleTitleEffects(hardTarget,owner);
        check(hardTarget.effects.size()==8 && arrow.f_19796_.calls==16);
        check(new HashSet<>(hardTarget.effects.stream().map(MobEffectInstance::effect).toList()).equals(seen));
        for(MobEffectInstance effect:hardTarget.effects) {
            check(effect.duration()==(effect.effect()==MobEffects.f_19602_ ? 1 : 200));
            check(effect.amplifier()==0);
        }
        check(hardTarget.fireSeconds==5 && hardTarget.source==owner);
        for(boolean hard:new boolean[]{false,true}) {
            ApollyonConfig.hard=hard;
            Actor risen=new Actor(Actor.RISEN); LivingEntity victim=new LivingEntity();
            risen.onDeathArrowHitPlayer(victim);
            check(risen.healed==40 && victim.effects.size()==(hard?1:0));
            if(hard) effect(victim,ApollyonEffectRegistry.WEAKNESS.get(),2);
            Actor witch=new Actor(Actor.WITCH_KING); victim=new LivingEntity();
            witch.onDeathArrowHitPlayer(victim);
            check(witch.healed==0 && victim.effects.size()==(hard?2:1));
            effect(victim,GoetyEffects.SAPPED.get(),1);
            if(hard) effect(victim,GoetyEffects.CURSED.get(),0);
            victim=new LivingEntity(); victim.alive=false;
            witch.onDeathArrowHitPlayer(victim); check(victim.effects.isEmpty());
            Actor other=new Actor(7); victim=new LivingEntity();
            other.onDeathArrowHitPlayer(victim); check(victim.effects.isEmpty() && other.healed==0);
            witch.onDeathArrowHitPlayer(null);
        }
        System.out.println("PASS: normal/hard arrow pool, burn-hex exclusion, pageant debuffs and retained healing/sapped");
    }
}
"""
fixture = fixture.replace("ARROW_METHOD", method(arrow, "private void applyApostleTitleEffects("))
fixture = fixture.replace("APPLY_METHOD", method(arrow, "private static void apply("))
fixture = fixture.replace("ACTOR_METHOD", method(apostle, "public void onDeathArrowHitPlayer("))
out = ROOT / "build" / ("difficulty-tests-" + str(time.time_ns()))
out.mkdir(parents=True)
source = out / "DifficultyRegression.java"
source.write_text(fixture, encoding="utf-8")
subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", str(out), str(source)], check=True)
subprocess.run(["java", "-ea", "-cp", str(out), "DifficultyRegression"], check=True)
