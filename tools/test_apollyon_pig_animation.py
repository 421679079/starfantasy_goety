"""Load the supplied assets and run the production controller against real GeckoLib 4.8.4."""
from pathlib import Path
from zipfile import ZipFile
import os
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
out = ROOT / 'build' / ('pig-animation-check-' + str(time.time_ns()))
out.mkdir(parents=True)
args_file = max(ROOT.glob('build/reproducible-*/main-partial.args'), key=lambda p: p.stat().st_mtime)
args = [s.strip().strip('"') for s in args_file.read_text('utf-8-sig').splitlines()]
classpath = args[args.index('-classpath') + 1]
gecko = ROOT.parent / 'private-deps/goety/geckolib-forge-1.20.1-4.8.4.jar'
with ZipFile(gecko) as jar:
    (out / 'mclib.jar').write_bytes(jar.read('META-INF/jarjar/mclib-20.jar'))
# The build's broad compile path also contains older Minecraft libraries. Use 1.20.1's
# FastUtil first when executing GeckoLib (8.2.1 lacks ObjectArrayList.of).
fastutil = Path('D:/MC/.minecraft/libraries/it/unimi/dsi/fastutil/8.5.9/fastutil-8.5.9.jar')
classpath = os.pathsep.join((str(out), str(out / 'mclib.jar'), str(fastutil), classpath))
stub = out / 'com/starfantasy/goety/entity/ApollyonServantEntity.java'
stub.parent.mkdir(parents=True)
stub.write_text('''package com.starfantasy.goety.entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
public class ApollyonServantEntity implements GeoAnimatable {
    public boolean pig=true,alive=true,casting,staying;
    public int duration,death; public long start;
    public final Level level=new Level();
    public static class Level {public long time;public long m_46467_(){return time;}}
    public Level m_9236_(){return level;}
    public boolean isPigVariant(){return pig;}
    public boolean m_6084_(){return alive;}
    public boolean isCastingAction(){return casting;}
    public boolean isStaying(){return staying;}
    public int castingAnimationDuration(){return duration;}
    public long castingAnimationStart(){return start;}
    public int deathAge(){return death;}
    public void registerControllers(AnimatableManager.ControllerRegistrar c){}
    public AnimatableInstanceCache getAnimatableInstanceCache(){return null;}
    public double getTick(Object o){return level.time;}
}
''', encoding='utf-8')
runner = out / 'PigAnimationRegression.java'
runner.write_text(r'''
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import com.google.gson.*;
import com.starfantasy.goety.animation.ApollyonPigAnimationController;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animatable.model.*;
import software.bernie.geckolib.core.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.util.JsonUtil;
public class PigAnimationRegression {
    static int checks;
    static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    static class PigModel implements CoreGeoModel<ApollyonServantEntity> {
        final BakedAnimations animations;
        final AnimationProcessor<ApollyonServantEntity> processor=new AnimationProcessor<>(this);
        final Map<String,CoreGeoBone> bones=new LinkedHashMap<>();
        final Map<String,BoneSnapshot> snapshots=new HashMap<>();
        PigModel(BakedAnimations animations){
            this.animations=animations;
            for(Animation a:animations.animations().values())for(var b:a.boneAnimations()){
                if(bones.containsKey(b.boneName()))continue;
                GeoBone bone=new GeoBone(null,b.boneName(),false,0D,false,false);
                bone.saveInitialSnapshot();bones.put(b.boneName(),bone);
                snapshots.put(b.boneName(),bone.saveSnapshot());processor.registerGeoBone(bone);
            }
        }
        public CoreBakedGeoModel getBakedGeoModel(String s){return null;}
        public AnimationProcessor<ApollyonServantEntity> getAnimationProcessor(){return processor;}
        public Animation getAnimation(ApollyonServantEntity e,String s){return animations.getAnimation(s);}
        public void handleAnimations(ApollyonServantEntity e,long id,AnimationState<ApollyonServantEntity> s){}
    }
    static void frame(ApollyonPigAnimationController c,ApollyonServantEntity e,PigModel m,long time,boolean moving){
        e.level.time=time;
        var s=new AnimationState<>(e,0F,0F,0F,moving).withController(c);
        c.process(m,s,m.bones,m.snapshots,time,true);
    }
    static double clock(ApollyonPigAnimationController c,double t)throws Exception{
        Method m=ApollyonPigAnimationController.class.getDeclaredMethod("adjustTick",double.class);m.setAccessible(true);
        return (double)m.invoke(c,t);
    }
    static boolean clip(ApollyonPigAnimationController c,String name){
        return c.getCurrentRawAnimation().getAnimationStages().get(0).animationName().endsWith("."+name);
    }
    public static void main(String[] args)throws Exception{
        JsonObject raw=JsonParser.parseString(Files.readString(Path.of(args[0]))).getAsJsonObject();
        BakedAnimations animations=JsonUtil.GEO_GSON.fromJson(raw.get("animations"),BakedAnimations.class);
        check(animations.animations().size()==4,"all four real animation clips decode");
        check(animations.getAnimation("animation.apollyon_pig.feed").length()==72,"actual feed length matches speed calculation");
        check(JsonUtil.GEO_GSON.fromJson(Files.readString(Path.of(args[1])),Model.class)!=null,"supplied geometry decodes");
        PigModel m=new PigModel(animations);
        for(int duration:new int[]{35,40,41,51,61,60,80,90,100,115,130}){
            ApollyonServantEntity e=new ApollyonServantEntity();e.casting=true;e.duration=duration;e.start=100;
            var c=new ApollyonPigAnimationController(e);
            for(int elapsed=0;elapsed<=duration;elapsed++){
                frame(c,e,m,100+elapsed,false);
                check(clip(c,"feed"),"casting never switches to an attack/idle pose");
                if(elapsed>1)check(Math.abs(clock(c,100+elapsed)-elapsed*72D/duration)<1e-6,"clip phase follows full cast duration: d="+duration+" elapsed="+elapsed+" actual="+clock(c,100+elapsed)+" state="+c.getAnimationState()+" speed="+c.getAnimationSpeed());
            }
            check(Math.abs(clock(c,100+duration)-72)<1e-6,"feed reaches the last frame at spell end");
            e.start=100+duration;e.duration=35;
            frame(c,e,m,e.start,false);frame(c,e,m,e.start+1,false);frame(c,e,m,e.start+2,false);
            check(Math.abs(clock(c,e.start+2)-144D/35)<1e-6,"consecutive feed restarts at the next spell");
        }
        ApollyonServantEntity e=new ApollyonServantEntity();var c=new ApollyonPigAnimationController(e);
        frame(c,e,m,0,false);check(clip(c,"idle"),"normal idle");
        frame(c,e,m,1,true);frame(c,e,m,2,true);check(clip(c,"walk"),"movement uses authored walk");
        e.staying=true;frame(c,e,m,3,true);check(clip(c,"idle"),"standby remains idle");
        e.pig=false;frame(c,e,m,4,false);check(c.getAnimationState()==AnimationController.State.STOPPED,"rename removes pig controller");
        e.pig=true;e.staying=false;e.casting=true;e.start=0;e.duration=100;
        frame(c,e,m,40,false);frame(c,e,m,41,false);frame(c,e,m,42,false);
        check(Math.abs(clock(c,42)-30.24)<1e-6,"rename during casting joins the current frame");
        e.alive=false;
        for(int age=1;age<=79;age++){
            e.death=age;frame(c,e,m,100+age,false);
            check(clip(c,"death"),"death overrides feed and holds through tick 79");
        }
        System.out.println("PASS: "+checks+" real GeckoLib asset, animation timing, chained cast, rename and death checks.");
    }
}
''', encoding='utf-8')
controller = ROOT / 'src/main/java/com/starfantasy/goety/animation/ApollyonPigAnimationController.java'

def run_java(command, arguments):
    argfile = out / (command + '.args')
    argfile.write_text('\n'.join('"' + str(a).replace('\\', '/') + '"' for a in arguments), encoding='utf-8')
    subprocess.run([command, '@' + str(argfile)], check=True, cwd=ROOT)

run_java('javac', ['--release','17','-encoding','UTF-8','-proc:none','-cp',classpath,'-d',out,stub,controller,runner])
run_java('java', ['-cp',classpath,'PigAnimationRegression',
    ROOT/'src/main/resources/assets/starfantasy_goety/animations/entity/apollyon/apollyon_pig.animation.json',
    ROOT/'src/main/resources/assets/starfantasy_goety/geo/entity/apollyon/apollyon_pig.geo.json'])
