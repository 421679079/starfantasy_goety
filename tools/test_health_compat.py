"""Run focused Java regressions using the production health method and compat class.

Requires a JDK on PATH. Minecraft attributes/NBT are small boundary fixtures; this
does not replace the full Forge build or an in-game compatibility test.
"""
from pathlib import Path
import re
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "build" / ("health-compat-tests-" + str(time.time_ns()))
OUT.mkdir(parents=True)


def block(source, marker):
    start = source.index(marker)
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


def run_java(folder, sources, main):
    folder.mkdir(parents=True, exist_ok=True)
    files = []
    for name, content in sources.items():
        path = folder / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        files.append(str(path))
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d",
                    str(folder), *files], check=True)
    subprocess.run(["java", "-ea", "-cp", str(folder), main], check=True)


entity = (ROOT / "src/main/java/com/starfantasy/goety/entity/ApollyonEntity.java").read_text("utf-8")
health_method = block(entity, "private void applyConfiguredAttributes(boolean healToFull)")
load_marker = block(entity, "if (tag.m_128441_(CONFIGURED_HEALTH_TAG))")
save_marker = re.search(r"tag\.m_128347_\(CONFIGURED_HEALTH_TAG, this\.lastConfiguredHealth\);", entity).group()
health_fixture = r"""
import java.util.*;
public class HealthRegression {
    static class ApollyonConfig {
        static double health = 600;
        static double bossHealth() { return health; }
        static int deathExperience() { return 100; }
        static double armor() { return 10; }
    }
    static class Mth {
        static double m_14008_(double n, double min, double max) { return Math.max(min, Math.min(n, max)); }
    }
    static class Attributes { static int f_22276_=0, f_22284_=1, f_22285_=2; }
    static class AttributeInstance {
        double base=600, addition=0, multiplier=1;
        int writes;
        double m_22115_() { return base; }
        void m_22100_(double n) { base=n; writes++; }
        float value() { return (float)Math.max(1, (base+addition)*multiplier); }
    }
    static class CompoundTag {
        Map<String,Double> values=new HashMap<>();
        boolean m_128441_(String key) { return values.containsKey(key); }
        double m_128459_(String key) { return values.getOrDefault(key, 0.0); }
        void m_128347_(String key, double value) { values.put(key,value); }
    }
    static class Boss {
        static final String CONFIGURED_HEALTH_TAG="ApollyonConfiguredHealth";
        double lastConfiguredHealth=Double.NaN;
        int f_21364_;
        static class Protection {void beginRestore(){} void endRestore(){}}
        final Protection healthProtection=new Protection();
        float health;
        AttributeInstance max=new AttributeInstance();
        AttributeInstance m_21051_(int attr) { return attr==0 ? max : null; }
        float m_21233_() { return max.value(); }
        float m_21223_() { return health; }
        void m_21153_(float n) { health=Math.min(m_21233_(), Math.max(0,n)); }
        Boss() { applyConfiguredAttributes(true); }
        void loadMarker(CompoundTag tag) { LOAD_MARKER applyConfiguredAttributes(false); }
        void saveMarker(CompoundTag tag) { SAVE_MARKER }
        HEALTH_METHOD
    }
    static void eq(double expected, double actual) {
        if(Math.abs(expected-actual)>.001) throw new AssertionError(expected+" != "+actual);
    }
    public static void main(String[] args) {
        Boss b=new Boss(); eq(600,b.health);
        b.max.multiplier=2; b.health=1100;
        for(int i=0;i<200;i++) { b.applyConfiguredAttributes(false); b.m_21153_(b.health+1); }
        eq(1200,b.health); eq(1,b.max.writes);
        b.max.addition=200; b.max.multiplier=1.5;
        b.applyConfiguredAttributes(true); eq(1200,b.health);
        b.max.base=1000; b.health=1250; b.applyConfiguredAttributes(false);
        eq(1000,b.max.base); eq(1250,b.health);
        b.applyConfiguredAttributes(true); eq(1800,b.health);
        b.max.multiplier=.5; b.applyConfiguredAttributes(false); eq(600,b.health);
        b.health=599.8f; b.applyConfiguredAttributes(false); eq(599.8,b.health);
        CompoundTag saved=new CompoundTag(); b.saveMarker(saved);
        Boss reloaded=new Boss(); reloaded.max.base=1400; reloaded.health=1300;
        reloaded.loadMarker(saved); eq(1400,reloaded.max.base); eq(1300,reloaded.health);
        Boss oldSave=new Boss(); oldSave.max.base=1400; oldSave.health=1300;
        oldSave.loadMarker(new CompoundTag()); eq(1400,oldSave.max.base); eq(1300,oldSave.health);
        ApollyonConfig.health=800;
        Boss changedOffline=new Boss(); changedOffline.max.base=1400;
        changedOffline.max.multiplier=2; changedOffline.health=2800;
        changedOffline.loadMarker(saved); eq(800,changedOffline.max.base); eq(1600,changedOffline.health);
        ApollyonConfig.health=1000; changedOffline.applyConfiguredAttributes(false);
        eq(2000,changedOffline.health);
        changedOffline.health=900; ApollyonConfig.health=1200;
        changedOffline.applyConfiguredAttributes(false); eq(900,changedOffline.health);
        changedOffline.max.base=2000;
        changedOffline.applyConfiguredAttributes(false); eq(2000,changedOffline.max.base);
        System.out.println("PASS: health modifiers, external base, full heals, caps, reloads and config changes");
    }
}
""".replace("HEALTH_METHOD", health_method).replace("LOAD_MARKER", load_marker).replace("SAVE_MARKER", save_marker)
run_java(OUT / "health", {"HealthRegression.java": health_fixture}, "HealthRegression")

compat = (ROOT / "src/mojang/java/com/starfantasy/goety/compat/ApostleCompatibility.java").read_text("utf-8")
for installed in (False, True):
    getter = """
        public boolean flag, broken;
        public boolean allTitlesApostle_1_20_1$isApollyon() {
            if(broken) throw new IllegalStateException("fixture getter failure");
            return flag;
        }
    """ if installed else ""
    checks = """
        boss.flag=true;
        check(!ApostleCompatibility.original(boss));
        check(ApostleCompatibility.isRevelationApollyon(boss));
        boss.flag=false; check(ApostleCompatibility.original(boss));
        boss.broken=true; check(!ApostleCompatibility.original(boss));
        boss.broken=false; check(ApostleCompatibility.original(boss));
    """ if installed else ""
    sources = {
        "com/starfantasy/goety/compat/ApostleCompatibility.java": compat,
        "com/Polarice3/Goety/common/entities/boss/Apostle.java":
            "package com.Polarice3.Goety.common.entities.boss; public class Apostle {"+getter+"}",
        "org/slf4j/Logger.java": "package org.slf4j; public interface Logger { void warn(String s, Throwable t); }",
        "com/mojang/logging/LogUtils.java":
            "package com.mojang.logging; public class LogUtils { public static org.slf4j.Logger getLogger() { return (s,t)->{}; } }",
        "CompatRegression.java": """
            import com.Polarice3.Goety.common.entities.boss.Apostle;
            import com.starfantasy.goety.compat.ApostleCompatibility;
            public class CompatRegression {
                static class Subclass extends Apostle {}
                static void check(boolean b) { if(!b) throw new AssertionError(); }
                public static void main(String[] args) {
                    Apostle boss=new Apostle();
                    check(ApostleCompatibility.original(boss));
                    check(!ApostleCompatibility.original(new Subclass()));
                    check(!ApostleCompatibility.original(null));
                    check(!ApostleCompatibility.isRevelationApollyon(null));
                    check(!ApostleCompatibility.isRevelationApollyon(new Object()));
                    CHECKS
                    System.out.println("PASS: optional Revelation flag INSTALLED");
                }
            }
        """.replace("CHECKS", checks).replace("INSTALLED", str(installed)),
    }
    run_java(OUT / ("revelation" if installed else "without-revelation"), sources, "CompatRegression")

print("All focused regressions passed. Fixtures:", OUT)
