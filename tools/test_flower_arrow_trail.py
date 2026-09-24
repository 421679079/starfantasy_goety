"""Run production arrow state transitions against a small clock/entity fixture."""
from pathlib import Path
import subprocess
import tempfile
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'src/mojang/java/com/starfantasy/goety/magic/focus/entity/FlowerArrowEntity.java'
if len(sys.argv) > 1:
    SOURCE = Path(sys.argv[1])
text = SOURCE.read_text('utf-8')


def method(signature):
    start = text.index(signature)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


fixture = r'''
public class FlowerTrailRegression {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    record Vec3(double x, double y, double z) {
        static Vec3 ZERO = new Vec3(0,0,0);
        Vec3 add(Vec3 p) {return new Vec3(x+p.x,y+p.y,z+p.z);}
        Vec3 scale(double n) {return new Vec3(x*n,y*n,z*n);}
    }
    static class Data { int value=-1; int get(int key){return value;} void set(int key,int value){this.value=value;} }
    static class Level { boolean isClientSide; }
    static class Entity {
        boolean removed; Vec3 position=Vec3.ZERO, motion=Vec3.ZERO;
        void tick(){} void discard(){removed=true;} boolean isRemoved(){return removed;}
        void setPos(Vec3 p){position=p;} void setDeltaMovement(Vec3 v){motion=v;}
    }
    static class Arrow extends Entity {
        static final int EXPLODED_AT=0, EXPLOSION_TRAIL_TICKS=20;
        Data entityData=new Data(); Vec3 start=Vec3.ZERO; boolean restored;
        Level level=new Level(); double clock; int travel=30;
        Level level(){return level;} double age(){return clock;} int travelTicks(){return travel;}
        Vec3 path(){return new Vec3(30,0,0);} void rotate(Vec3 direction){}
        METHODS
    }
    public static void main(String[] args) {
        Arrow natural=new Arrow();
        for(int tick=0;tick<50;tick++) {
            natural.clock=tick; natural.tick(); check(!natural.removed);
            check(natural.isExploded()==(tick>=30));
            if(tick>=30) check(natural.position.x==30 && natural.motion.equals(Vec3.ZERO));
        }
        natural.clock=50; natural.tick(); check(natural.removed);
        Arrow finalArrow=new Arrow(); finalArrow.travel=49; finalArrow.clock=40; finalArrow.explode();
        double frozen=finalArrow.position.x;
        check(frozen>0 && frozen<30);
        for(int tick=40;tick<60;tick++) {
            finalArrow.clock=tick; finalArrow.tick(); finalArrow.explode();
            check(!finalArrow.removed && finalArrow.position.x==frozen);
        }
        finalArrow.clock=60;finalArrow.tick();check(finalArrow.removed);
        Arrow client=new Arrow();client.level.isClientSide=true;client.clock=30;client.tick();
        client.clock=51;client.tick();check(!client.removed);
        Arrow restored=new Arrow();restored.restored=true;restored.tick();check(restored.removed);
        System.out.println("PASS: natural and final explosions freeze position; exactly 20 tick trail; repeated explosion cannot extend it; server removal");
    }
}
'''
fixture = fixture.replace('METHODS', '\n'.join(method(signature) for signature in [
    'public Vec3 sample(', 'public boolean isExploded(', 'public double getExplosionTrailAge(',
    'public void explode(', 'public void tick(']))
with tempfile.TemporaryDirectory() as directory:
    java = Path(directory) / 'FlowerTrailRegression.java'
    java.write_text(fixture, 'utf-8')
    subprocess.run(['javac', '-encoding', 'UTF-8', str(java)], check=True)
    subprocess.run(['java', '-cp', directory, 'FlowerTrailRegression'], check=True)
