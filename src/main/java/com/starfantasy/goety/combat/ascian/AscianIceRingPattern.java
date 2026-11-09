package com.starfantasy.goety.combat.ascian;

import java.util.UUID;
import net.minecraft.world.phys.Vec3;

/** Analytic visual motion shared by the server schedule and client meshes. */
public final class AscianIceRingPattern {
    public static final int EXPAND=20,ORBIT_END=80,LAUNCH=100,FLIGHT=20,STUN=40;
    public static final double SPEED=4,HIT_HEIGHT=.15;
    public record Ring(long id,UUID target,int targetId,long startTick,int index,boolean hard,Vec3 center,boolean frozen){
        public Ring {if(index<0||index>1||index==1&&!hard)throw new IllegalArgumentException("Invalid ice ring");}
        public double radius(){return index==0?8:12;}
        public int count(){return index==0?10:16;}
        public int warningTicks(){return index==0?22:23;}
        public int damageAge(){return ORBIT_END+warningTicks();}
        public double damageScale(){return index==0?1:1.5;}
        public Ring at(Vec3 p,boolean locked){return new Ring(id,target,targetId,startTick,index,hard,p,locked);}
        public double angle(double age){
            double t=Math.max(0,Math.min(LAUNCH,age)),brake=Math.max(0,t-ORBIT_END);
            return (t-brake*brake/40)*Math.PI/60*(index==0?1:-1);
        }
        public Vec3 position(int slot,double age,Vec3 anchor){
            double r=age<EXPAND?radius()*Math.max(0,age)/EXPAND:age<LAUNCH?radius():radius()-SPEED*(age-LAUNCH);
            double a=slot*Math.PI*2/count()+angle(age);
            return anchor.m_82520_(Math.cos(a)*r,.38,Math.sin(a)*r);
        }
    }
    private AscianIceRingPattern(){}
}
