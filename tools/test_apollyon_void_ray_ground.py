"""Exercise the production Void Ray anchor against mounted and uneven-floor fixtures."""
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/starfantasy/goety'
manager = (JAVA / 'combat/ApollyonVoidRayManager.java').read_text('utf-8')
boss = (JAVA / 'entity/ApollyonEntity.java').read_text('utf-8')
servant = (JAVA / 'entity/ApollyonServantEntity.java').read_text('utf-8')
assert 'ApollyonVoidRayManager.captureAnchor(' in boss
assert 'ApollyonVoidRayManager.captureAnchor(' in servant
assert 'groundSectorWarningBatch(' in manager
assert 'ApollyonSectorEffectEntity.spawn(level, anchor,' in manager
assert 'anchor.f_82480_ + HITBOX_HEIGHT' in manager

start = manager.index('public static Vec3 captureAnchor(')
opening = manager.index('{', start)
depth = 1
end = opening + 1
while depth:
    depth += (manager[end] == '{') - (manager[end] == '}')
    end += 1
capture = manager[start:end]

fixture = r'''
import java.util.*;
public class VoidRayGroundRegression {
    record Vec3(double f_82479_, double f_82480_, double f_82481_) { }
    static class Direction {
        static final Direction UP = new Direction();
        enum Axis { Y }
    }
    record BlockPos(int x, int y, int z) {
        static BlockPos m_274561_(double x, double y, double z) {
            return new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
        }
        int m_123342_() { return y; }
        BlockPos m_7495_() { return new BlockPos(x, y - 1, z); }
    }
    record Shape(boolean empty, double top) {
        boolean m_83281_() { return empty; }
        double m_83297_(Direction.Axis axis) { return top; }
    }
    record State(Shape shape, boolean sturdy) {
        Shape m_60812_(ServerLevel level, BlockPos pos) { return shape; }
        boolean m_60783_(ServerLevel level, BlockPos pos, Direction face) { return sturdy; }
    }
    static class ServerLevel {
        boolean loaded = true;
        Map<Integer, State> blocks = new HashMap<>();
        boolean m_46805_(BlockPos pos) { return loaded; }
        int m_141937_() { return -64; }
        State m_8055_(BlockPos pos) {
            return blocks.getOrDefault(pos.y(), new State(new Shape(true, 0), false));
        }
        ServerLevel floor(int y, double top) {
            blocks.put(y, new State(new Shape(false, top), true));
            return this;
        }
    }
    static class Mob {
        ServerLevel level;
        double x, y, z;
        Mob(ServerLevel level, double x, double y, double z) {
            this.level = level; this.x = x; this.y = y; this.z = z;
        }
        Object m_9236_() { return level; }
        double m_20185_() { return x; }
        double m_20186_() { return y; }
        double m_20189_() { return z; }
        Object m_20202_() { return null; }
    }
    static class ApollyonServantEntity extends Mob {
        HadesServantEntity vehicle;
        ApollyonServantEntity(ServerLevel level, double x, double y, double z) {
            super(level, x, y, z);
        }
        @Override Object m_20202_() { return vehicle; }
    }
    static class HadesServantEntity extends Mob {
        ApollyonServantEntity rider;
        HadesServantEntity(ServerLevel level) { super(level, 0, 0, 0); }
        ApollyonServantEntity mountedApollyonServant() { return rider; }
    }
    CAPTURE
    static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        ServerLevel level = new ServerLevel().floor(0, 1);
        Mob boss = new Mob(level, 2.5, 9.2, -3.5);
        check(captureAnchor(boss).f_82480_() == 9.2,
                "boss retains its original cast height");
        ApollyonServantEntity servant = new ApollyonServantEntity(level, 2.5, 9.2, -3.5);
        check(captureAnchor(servant).f_82480_() == 9.2,
                "unmounted servant retains its original cast height");
        HadesServantEntity hades = new HadesServantEntity(level);
        servant.vehicle = hades;
        hades.rider = servant;
        Vec3 mounted = captureAnchor(servant);
        check(mounted != null && mounted.f_82480_() == 1 && mounted.f_82479_() == 2.5,
                "mounted caster must project down to floor without changing X/Z");
        level.floor(3, .5);
        check(captureAnchor(servant).f_82480_() == 3.5,
                "nearest slab surface, not the bottom floor or integer block top");
        servant.y = 2.0;
        check(captureAnchor(servant).f_82480_() == 1,
                "a floor above the caster must not be selected");
        servant.y = 9.2;
        level.blocks.put(6, new State(new Shape(false, 1), false));
        check(captureAnchor(servant).f_82480_() == 7,
                "a collidable non-sturdy surface can still support the ray");
        hades.rider = null;
        check(captureAnchor(servant).f_82480_() == 9.2,
                "invalid riding state must not project the cast");
        hades.rider = servant;
        servant.level = new ServerLevel();
        check(captureAnchor(servant) == null,
                "no solid floor must not leave an airborne ray");
        servant.level = level;
        level.loaded = false;
        check(captureAnchor(servant) == null,
                "unloaded chunks must not be scanned");
        System.out.println("PASS: boss/unmounted casts unchanged; mounted, slab and missing-floor anchors.");
    }
}
'''.replace('CAPTURE', capture)

out = ROOT / 'build' / ('void-ray-ground-check-' + str(time.time_ns()))
out.mkdir(parents=True)
source = out / 'VoidRayGroundRegression.java'
source.write_text(fixture, encoding='utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(source)], check=True)
subprocess.run(['java', '-ea', '-cp', str(out), 'VoidRayGroundRegression'], check=True)
