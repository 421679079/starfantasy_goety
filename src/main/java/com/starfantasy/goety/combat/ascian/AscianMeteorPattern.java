package com.starfantasy.goety.combat.ascian;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import net.minecraft.world.phys.Vec3;

/** Immutable ground samples shared by the server timeline and client meshes. */
public final class AscianMeteorPattern {
    public static final int WARNING_TICKS=20, METEOR_TICKS=85, TRAP_DAMAGE_TICKS=21, TRAP_TICKS=30;
    public static final double RADIUS=3, TRAP_RADIUS=4.5, ROLL_SPEED=.4;
    public static final float TRAP_VISUAL_SCALE=2.25F, METEOR_VISUAL_SIZE=4.5F;
    public static final List<Integer> EXPLOSIONS=List.of(20,40,60,80);
    private static final int[] ANGLES={-30,-15,0,15,30};
    @FunctionalInterface public interface Ground { Vec3 at(double x,double z); }

    public record Path(long id,long startTick,List<Vec3> points) {
        public Path { points=List.copyOf(points); if(points.size()!=5)throw new IllegalArgumentException("Five ground samples required"); }
        public Vec3 position(double age) {
            age=Math.max(0,Math.min(METEOR_TICKS,age));
            if(age<20) {
                Vec3 a=points.get(0),b=points.get(1);
                double dx=(b.f_82479_-a.f_82479_)/8,dz=(b.f_82481_-a.f_82481_)/8,t=1-age/20;
                return a.m_82520_(-dx*5*t,10*t,-dz*5*t);
            }
            int segment=age<80?Math.min(2,(int)((age-20)/20)):3;
            double t=segment==3?(age-80)/5:(age-20-segment*20)/20;
            Vec3 a=points.get(segment),b=points.get(segment+1);
            return new Vec3(a.f_82479_+(b.f_82479_-a.f_82479_)*t,
                    a.f_82480_+(b.f_82480_-a.f_82480_)*t,a.f_82481_+(b.f_82481_-a.f_82481_)*t);
        }
        public float yaw() { Vec3 a=points.get(0),b=points.get(1);return (float)Math.atan2(b.f_82479_-a.f_82479_,b.f_82481_-a.f_82481_); }
    }
    public record Trap(long id,long startTick,Vec3 center) {}

    public static List<Path> create(Vec3 boss,Vec3 target,Vec3 look,long startTick,LongSupplier ids,Ground ground) {
        double dx=target.f_82479_-boss.f_82479_,dz=target.f_82481_-boss.f_82481_,length=Math.hypot(dx,dz);
        double distance=Math.max(5,Math.min(35,length));
        if(length<1E-6){dx=look.f_82479_;dz=look.f_82481_;if(Math.hypot(dx,dz)<1E-6){dx=0;dz=1;}}
        double base=Math.atan2(dx,dz);
        List<Path> paths=new ArrayList<>(5);
        for(int degrees:ANGLES){
            double a=base+Math.toRadians(degrees),x=Math.sin(a),z=Math.cos(a);
            List<Vec3> samples=new ArrayList<>(5);
            for(double ahead:new double[]{0,8,16,24,26})samples.add(ground.at(boss.f_82479_+x*(distance+ahead),boss.f_82481_+z*(distance+ahead)));
            paths.add(new Path(ids.getAsLong(),startTick,samples));
        }
        return List.copyOf(paths);
    }
    private AscianMeteorPattern() {}
}
