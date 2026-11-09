package com.starfantasy.goety.combat.ascian;

import java.util.ArrayList;
import java.util.List;

/** Frozen horizontal hit geometry and cosmetic layout, independent of client/Minecraft classes. */
public final class AscianMeleeShape {
    private static final double EPS=1e-9, HALF_ANGLE=Math.toRadians(75);
    public record Point(double x,double z) {}
    public record Bounds(double minX,double minY,double minZ,double maxX,double maxY,double maxZ) {}
    public record Wave(int delay,int styleOffset,List<Point> points) {
        public Wave { points=List.copyOf(points); }
    }
    private final int segment;
    private final double x,z,fx,fz,minY,maxY;

    public AscianMeleeShape(int segment,double x,double z,double forwardX,double forwardZ,double targetY) {
        if(segment<0||segment>3)throw new IllegalArgumentException("Melee segment");
        this.segment=segment;this.x=x;this.z=z;
        double length=Math.hypot(forwardX,forwardZ);
        fx=length>1e-8?forwardX/length:0;fz=length>1e-8?forwardZ/length:1;
        // Keep the old target-relative vertical band, including its one-block base cell.
        int expansion=segment<2?3:2;
        minY=Math.floor(targetY)-expansion;maxY=Math.floor(targetY)+1+expansion;
    }
    private Point point(double forward,double left) {
        return new Point(x+fx*forward+fz*left,z+fz*forward-fx*left);
    }
    public Bounds bounds() {
        Point center=point(segment>=2?4:0,0);
        double ex=segment==2?Math.abs(fx)*4+Math.abs(fz)*2.5:7;
        double ez=segment==2?Math.abs(fz)*4+Math.abs(fx)*2.5:7;
        return new Bounds(center.x-ex,minY,center.z-ez,center.x+ex,maxY,center.z+ez);
    }
    public boolean intersects(double ax,double ay,double az,double bx,double by,double bz) {
        if(by<=minY||ay>=maxY)return false;
        if(segment==3){Point p=point(4,0);return disk(ax-p.x,az-p.z,bx-p.x,bz-p.z);}
        if(segment==2){
            // Separating axes for the oriented 5 x 8 rectangle and the victim's world AABB.
            Point c=point(4,0);double dx=(ax+bx)/2-c.x,dz=(az+bz)/2-c.z,hx=(bx-ax)/2,hz=(bz-az)/2;
            return Math.abs(dx)<=hx+Math.abs(fx)*4+Math.abs(fz)*2.5+EPS
                    &&Math.abs(dz)<=hz+Math.abs(fz)*4+Math.abs(fx)*2.5+EPS
                    &&Math.abs(dx*fx+dz*fz)<=4+hx*Math.abs(fx)+hz*Math.abs(fz)+EPS
                    &&Math.abs(dx*fz-dz*fx)<=2.5+hx*Math.abs(fz)+hz*Math.abs(fx)+EPS;
        }
        ax-=x;bx-=x;az-=z;bz-=z;
        if(!disk(ax,az,bx,bz))return false;
        if(ax<=0&&bx>=0&&az<=0&&bz>=0)return true;
        if(inside(ax,az)||inside(ax,bz)||inside(bx,az)||inside(bx,bz))return true;
        // A boundary crossing must meet a radial edge or the circular arc.
        for(int side:new int[]{-1,1}){
            double f=7*Math.cos(HALF_ANGLE),l=side*7*Math.sin(HALF_ANGLE);
            if(segmentBox(fx*f+fz*l,fz*f-fx*l,ax,az,bx,bz))return true;
        }
        return arcVertical(ax,az,bz)||arcVertical(bx,az,bz)||arcHorizontal(az,ax,bx)||arcHorizontal(bz,ax,bx);
    }
    private static boolean disk(double ax,double az,double bx,double bz) {
        double cx=Math.max(ax,Math.min(0,bx)),cz=Math.max(az,Math.min(0,bz));
        return cx*cx+cz*cz<=49+EPS;
    }
    private boolean inside(double dx,double dz) {
        double r2=dx*dx+dz*dz;
        return r2<=49+EPS&&dx*fx+dz*fz>=Math.sqrt(r2)*Math.cos(HALF_ANGLE)-EPS;
    }
    private boolean arcVertical(double dx,double lo,double hi) {
        if(Math.abs(dx)>7)return false;double dz=Math.sqrt(Math.max(0,49-dx*dx));
        return (dz>=lo-EPS&&dz<=hi+EPS&&inside(dx,dz))||(-dz>=lo-EPS&&-dz<=hi+EPS&&inside(dx,-dz));
    }
    private boolean arcHorizontal(double dz,double lo,double hi) {
        if(Math.abs(dz)>7)return false;double dx=Math.sqrt(Math.max(0,49-dz*dz));
        return (dx>=lo-EPS&&dx<=hi+EPS&&inside(dx,dz))||(-dx>=lo-EPS&&-dx<=hi+EPS&&inside(-dx,dz));
    }
    private static boolean segmentBox(double dx,double dz,double ax,double az,double bx,double bz) {
        double enter=0,exit=1;
        double[] direction={dx,dz},low={ax,az},high={bx,bz};
        for(int i=0;i<2;i++){
            if(Math.abs(direction[i])<EPS){if(low[i]>0||high[i]<0)return false;}
            else {double a=low[i]/direction[i],b=high[i]/direction[i];enter=Math.max(enter,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));}
        }
        return enter<=exit+EPS;
    }
    public List<Wave> waves() {
        List<Wave> waves=new ArrayList<>();
        if(segment<2){
            for(int i=0;i<5;i++){
                double angle=Math.toRadians((segment==0?1:-1)*(60-i*30));
                waves.add(new Wave(i,i,List.of(point(6*Math.cos(angle),6*Math.sin(angle)))));
            }
        }else if(segment==2){
            for(int i=0;i<5;i++)waves.add(new Wave(i,i,List.of(point(3+i,0))));
        }else{
            Point c=point(4,0);
            for(int ring=0;ring<2;ring++){
                int count=ring==0?5:10;double radius=ring==0?3:5;
                List<Point> points=new ArrayList<>();
                for(int i=0;i<count;i++){
                    double a=i*Math.PI*2/count;
                    points.add(new Point(c.x+radius*(fx*Math.cos(a)+fz*Math.sin(a)),c.z+radius*(fz*Math.cos(a)-fx*Math.sin(a))));
                }
                waves.add(new Wave(ring*4,0,points)); // One five-style cycle inside, two cycles outside.
            }
        }
        return List.copyOf(waves);
    }
}
