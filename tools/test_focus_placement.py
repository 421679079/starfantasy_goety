"""Run production placement policies against deterministic terrain/collision fixtures."""
from pathlib import Path
import re
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
base = root / 'src/mojang/java/com/starfantasy/goety/magic/focus'
source = (base / 'BattleFocusPlacement.java').read_text('utf-8')
source = re.sub(r'^(package|import) .*;\n', '', source, flags=re.M)
fixture = r'''
class Vec3 {
    double x,y,z;
    Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
    double length(){return Math.sqrt(x*x+y*y+z*z);}
    Vec3 scale(double s){return new Vec3(x*s,y*s,z*s);}
}
class BlockPos { static BlockPos containing(Vec3 p){return new BlockPos();} }
class HitResult {
    enum Type { BLOCK, MISS }
    Type type; Vec3 point;
    HitResult(Type t,Vec3 p){type=t;point=p;}
    boolean inside;
    Type getType(){return type;} Vec3 getLocation(){return point;} boolean isInside(){return inside;}
}
class ClipContext {
    enum Block { COLLIDER } enum Fluid { NONE }
    Vec3 start,end;
    ClipContext(Vec3 a,Vec3 b,Block c,Fluid d,LivingEntity e){start=a;end=b;}
}
class ServerLevel {
    boolean loaded=true; double[] floors={64}; ClipContext last; double solidTop=-100;
    boolean hasChunkAt(BlockPos p){return loaded;}
    int getMinBuildHeight(){return -64;} int getMaxBuildHeight(){return 320;}
    HitResult clip(ClipContext c){
        last=c; double best=-Double.MAX_VALUE;
        if(c.start.y<solidTop){HitResult hit=new HitResult(HitResult.Type.BLOCK,c.start);hit.inside=true;return hit;}
        for(double y:floors) if(y<=c.start.y && y>=c.end.y) best=Math.max(best,y);
        return new HitResult(best==-Double.MAX_VALUE?HitResult.Type.MISS:HitResult.Type.BLOCK,
            new Vec3(c.start.x,best,c.start.z));
    }
}
enum MoverType { SELF }
class LivingEntity {
    double x,y,z; boolean noPhysics,blocked; int moves;
    double getX(){return x;} double getY(){return y;} double getZ(){return z;}
    void move(MoverType type,Vec3 v){moves++;if(!blocked){x+=v.x;y+=v.y;z+=v.z;}}
}
class ServerPlayer extends LivingEntity {
    class Connection { int calls; void teleport(double x,double y,double z,float yaw,float pitch){calls++;} }
    Connection connection=new Connection(); float getYRot(){return 0;} float getXRot(){return 0;}
}
class BattleFocusCombat {
    static java.util.List<LivingEntity> selected=new java.util.ArrayList<>();
    static java.util.List<LivingEntity> targets(ServerLevel l,LivingEntity c,Vec3 p,double r){return selected;}
}
public class PlacementRegression {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        ServerLevel l=new ServerLevel(); LivingEntity caster=new LivingEntity();
        Vec3 p=BattleFocusPlacement.ground(l,caster,new Vec3(15,250,3));
        check(p.x==15 && p.y==64 && p.z==3,"flying caster projects onto ground");
        l.floors=new double[]{20.5,80};
        check(BattleFocusPlacement.ground(l,caster,new Vec3(0,30,0)).y==20.5,"cave floor, not roof; partial block top");
        l.solidTop=100;l.floors=new double[]{100};
        check(BattleFocusPlacement.ground(l,caster,new Vec3(0,64,0)).y==100,"embedded mountain anchor rises to surface");
        l.solidTop=-100;
        l.floors=new double[]{};
        check(BattleFocusPlacement.ground(l,caster,new Vec3(0,200,0))==null,"void has no floating anchor");
        l.loaded=false;
        check(BattleFocusPlacement.ground(l,caster,new Vec3(0,200,0))==null,"unloaded chunk rejected");
        ServerPlayer enemy=new ServerPlayer();enemy.x=8;enemy.y=64;
        LivingEntity near=new LivingEntity();near.x=1;near.y=64;
        LivingEntity wall=new LivingEntity();wall.x=7;wall.blocked=true;
        LivingEntity ghost=new LivingEntity();ghost.x=7;ghost.noPhysics=true;
        BattleFocusCombat.selected=java.util.List.of(enemy,near,wall,ghost);
        BattleFocusPlacement.gather(l,caster,new Vec3(0,64,0),8);
        check(enemy.x==4 && enemy.y==64 && enemy.moves==1 && enemy.connection.calls==1,"one horizontal move and player sync");
        check(near.x==1 && near.moves==0,"center targets are not pushed past center");
        check(wall.x==7,"collision response retained");
        check(ghost.moves==0,"no collision bypass");
        System.out.println("PASS: ground projection and single inward displacement policies");
    }
}
'''
with tempfile.TemporaryDirectory() as directory:
    path = Path(directory) / 'PlacementRegression.java'
    path.write_text(source + fixture, 'utf-8')
    subprocess.run(['javac', '-encoding', 'UTF-8', str(path)], check=True)
    subprocess.run(['java', '-cp', directory, 'PlacementRegression'], check=True)

night = (base / 'EvernightCast.java').read_text('utf-8')
assert night.count('BattleFocusPlacement.gather(') == 1
assert night.index('BattleFocusPlacement.gather(') < night.index('cast.controlTargets();')
assert 'BattleFocusPlacement.gather(' not in night.split('public static void tick(')[1]
for name in ['EvernightCast.java', 'FlowerArrowRain.java']:
    text = (base / name).read_text('utf-8')
    assert 'BattleFocusPlacement.ground(' in text and 'if (center == null) return;' in text
print('PASS: both anchors grounded; gathering only on spawn, before Tangled')
