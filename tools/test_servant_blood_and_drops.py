"""Run production interaction, phase timeout and drop relocation methods in Java fixtures."""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/main/java/com/starfantasy/goety'


def block(text, marker):
    start = text.index(marker)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def servant_fixture(filename, name, second, config):
    healed = 600 if name == 'Apostle' else 350
    source = (BASE / 'entity' / filename).read_text('utf-8')
    timer = block(source, 'if(isSecondPhase()) {' if name == 'Apostle' else 'if (isCombatPhaseTwo()) {')
    # Only the actual idle/reset branch; periodic weather/meteor effects are unrelated.
    timer = timer[:timer.index('            if(isSecondPhase()' if name == 'Apostle' else '            if (isCombatPhaseTwo()')] + '\n}'
    return f'''
    static class {name} extends Base {{
        static final Object {second} = new Object();
        Data f_19804_ = new Data();
        int idleSecondTicks, meteorCooldown;
        boolean isSecondPhase() {{ return f_19804_.second; }}
        boolean isCombatPhaseTwo() {{ return f_19804_.second; }}
        void tickPhase() {{ boolean fighting = target != null && target.alive; {timer} }}
        {block(source, 'private void resetCombatPhase()')}
        {block(source, 'public InteractionResult m_6071_(')}
    }}
    static void test{name}() {{
        {name} entity = new {name}();
        Bool config = ServantConfig.{config};
        for (Tag tag : new Tag[]{{null, new Tag(false), new Tag(null)}}) {{
            Player ordinary = new Player();
            ordinary.stack.tag = tag;
            entity.health = 100;
            entity.f_19804_.second = true;
            int before = entity.sounds;
            InteractionResult result = entity.m_6071_(ordinary, InteractionHand.MAIN_HAND);
            if ("{name}".equals("Apollyon")) {{
                check(result == InteractionResult.FAIL && entity.health == 100 && ordinary.stack.count == 3
                        && entity.sounds == before && entity.f_19804_.second, "Apollyon rejects non-pure blood without effects");
                entity.health = entity.maximum;
                config.value = false;
                check(entity.m_6071_(ordinary, InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                        && entity.f_19804_.second && ordinary.message == null, "ordinary blood cannot reset full-health Apollyon");
            }} else check(result == InteractionResult.SUCCESS && entity.health == 600, "Apostle accepts ordinary blood");
        }}
        for (boolean enabled : new boolean[]{{true, false}}) {{
            config.value = enabled;
            entity.f_19804_.second = true;
            entity.idleSecondTicks = 0;
            for (int i = 0; i < 1199; ++i) entity.tickPhase();
            check(entity.f_19804_.second, "{name}: no early reset");
            entity.tickPhase();
            check(entity.f_19804_.second != enabled, "{name}: timeout obeys config");
            entity.f_19804_.second = true;
            entity.target = new Target();
            for (int i = 0; i < 1400; ++i) entity.tickPhase();
            check(entity.f_19804_.second && entity.idleSecondTicks == 0, "{name}: fighting never resets");
            entity.target = null;
            for (boolean phaseTwo : new boolean[]{{false, true}}) {{
                for (InteractionHand hand : InteractionHand.values()) {{
                    Player player = new Player();
                    entity.f_19804_.second = phaseTwo;
                    entity.health = 100;
                    entity.maximum = 1000;
                    entity.idleSecondTicks = 500;
                    int soundsBefore = entity.sounds;
                    check(entity.m_6071_(player, hand) == InteractionResult.SUCCESS, "blood handled");
                    check(entity.sounds == soundsBefore + 1, "one drinking sound per successful feeding");
                    check(entity.health == {healed}, "{name}: heal uses current maximum and servant-specific ratio");
                    check(entity.f_19804_.second == phaseTwo, "{name}: injured servants only heal");
                    check(player.message == null, "no best-condition message while only healing");
                    check(player.stack.count == 2, "consume once");
                    entity.health = 950;
                    entity.m_6071_(player, hand);
                    check(entity.health == 1000, "healing caps at maximum");
                    check(entity.f_19804_.second == phaseTwo, "reaching full health during feeding does not reset");
                    check(player.message == null, "healing to full does not announce a phase reset");
                }}
            }}
            for (InteractionHand hand : InteractionHand.values()) {{
                Player full = new Player();
                entity.f_19804_.second = false;
                entity.health = entity.maximum;
                int soundsBefore = entity.sounds;
                check(entity.m_6071_(full, hand) == InteractionResult.FAIL, "full phase one refuses blood");
                check(full.stack.count == 3 && entity.sounds == soundsBefore, "refusal neither consumes nor plays sound");
                check(full.actionbar && full.message.key.equals("message.starfantasy_goety.servant.best_condition")
                        && full.message.name == entity.name && full.message.color == ChatFormatting.RED,
                        "localized red refusal actionbar uses current target name");
                entity.f_19804_.second = true;
                full.message = null;
                full.actionbar = false;
                check(entity.m_6071_(full, hand) == InteractionResult.SUCCESS, "full phase two still accepts blood");
                check(full.stack.count == 2 && entity.sounds == soundsBefore + 1, "phase two consumes and sounds once");
                check(entity.f_19804_.second == enabled, "full phase two reset obeys config");
                if (!enabled) {{
                    check(full.actionbar && full.message.key.equals("message.starfantasy_goety.servant.restored_condition")
                            && full.message.name == entity.name && full.message.color == ChatFormatting.GREEN,
                            "full-health phase reset shows localized green current-name actionbar");
                }} else check(full.message == null, "auto-reset enabled does not announce a manual reset");
            }}
        }}
        Player creative = new Player();
        creative.abilities.f_35937_ = true;
        entity.health = 100;
        entity.m_6071_(creative, InteractionHand.MAIN_HAND);
        check(creative.stack.count == 3 && entity.health == {healed}, "creative heals without consuming");
        entity.level.f_46443_ = true;
        entity.health = 100;
        entity.f_19804_.second = true;
        Player client = new Player();
        int clientSounds = entity.sounds;
        entity.m_6071_(client, InteractionHand.OFF_HAND);
        check(entity.health == 100 && client.stack.count == 3 && entity.f_19804_.second
                && entity.sounds == clientSounds, "client does not mutate or duplicate sounds");
        entity.health = entity.maximum;
        entity.f_19804_.second = false;
        check(entity.m_6071_(client, InteractionHand.MAIN_HAND) == InteractionResult.FAIL, "client refuses full phase one too");
        check(client.message == null && client.stack.count == 3 && entity.sounds == clientSounds, "client refusal does not duplicate feedback");
        entity.level.f_46443_ = false;
        client.stack.item = new Object();
        check(entity.m_6071_(client, InteractionHand.MAIN_HAND) == InteractionResult.PASS, "other items use Goety");
        entity.alive = false;
        client.stack.item = ModItems.UNHOLY_BLOOD.get();
        check(entity.m_6071_(client, InteractionHand.MAIN_HAND) == InteractionResult.PASS, "dead servant not revived");
    }}
'''


fixture = '''
import java.util.*;
public class BloodDropsRegression {
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    enum InteractionHand { MAIN_HAND, OFF_HAND }
    enum InteractionResult { SUCCESS, PASS, FAIL; static InteractionResult m_19078_(boolean client) { return SUCCESS; } }
    enum ChatFormatting { GREEN, RED }
    static class Component {
        String key; Object name; ChatFormatting color;
        static Component m_237110_(String key, Object name) { Component c=new Component(); c.key=key;c.name=name;return c; }
        Component m_130940_(ChatFormatting color) { this.color=color;return this; }
    }
    static class SoundEvents { static Object f_11911_ = new Object(); }
    static class Bool { boolean value = true; boolean get() { return value; } }
    static class ServantConfig { static Bool APOSTLE_AUTO_RESET_PHASE = new Bool(), APOLLYON_AUTO_RESET_PHASE = new Bool(); }
    static class Holder { Object value = new Object(); Object get() { return value; } }
    static class ModItems { static Holder UNHOLY_BLOOD = new Holder(); }
    static class Tag {
        Boolean pure; Tag(Boolean pure) { this.pure=pure; }
        boolean m_128471_(String key) { return key.equals("Pure") && Boolean.TRUE.equals(pure); }
    }
    static class ItemStack {
        Object item = ModItems.UNHOLY_BLOOD.get(); int count = 3;
        Tag tag = new Tag(true); Tag m_41783_() { return tag; }
        boolean m_150930_(Object expected) { return item == expected; }
        void m_41774_(int amount) { count -= amount; }
    }
    static class Abilities { boolean f_35937_; }
    static class Player {
        ItemStack stack = new ItemStack(); Abilities abilities = new Abilities();
        Component message; boolean actionbar;
        void m_5661_(Component message, boolean actionbar) { this.message=message;this.actionbar=actionbar; }
        ItemStack m_21120_(InteractionHand hand) { return stack; }
        Abilities m_150110_() { return abilities; }
    }
    static class Level { boolean f_46443_; }
    static class Target { boolean alive = true; boolean m_6084_() { return alive; } }
    static class Data { boolean second; void m_135381_(Object key, boolean value) { second = value; } }
    static class Base {
        float health = 100, maximum = 1000; boolean alive = true; Level level = new Level(); Target target;
        int sounds; Object name = new Object(); Object m_7755_() { return name; }
        void m_5496_(Object sound, float volume, float pitch) { check(sound == SoundEvents.f_11911_, "drinking sound");sounds++; }
        boolean m_6084_() { return alive; } Level m_9236_() { return level; }
        Target m_5448_() { return target; }
        float m_21223_() { return health; } float m_21233_() { return maximum; }
        void m_21153_(float value) { health = value; }
        public InteractionResult m_6071_(Player p, InteractionHand h) { return InteractionResult.PASS; }
    }
    static class Vec3 { double f_82479_, f_82480_, f_82481_; Vec3(double x,double y,double z){ f_82479_=x;f_82480_=y;f_82481_=z; } }
    static class ApollyonEntity { Vec3 arenaHomePosition() { return new Vec3(100, 80, -200); } }
    static class ItemEntity {
        double x = 99, y = 30, z = 99; Vec3 velocity;
        void m_6034_(double x, double y, double z) { this.x=x;this.y=y;this.z=z; }
        void m_20256_(Vec3 value) { velocity=value; }
    }
    record LivingDropsEvent(Object entity, List<ItemEntity> drops) {
        Object getEntity() { return entity; } List<ItemEntity> getDrops() { return drops; }
    }
    public static void main(String[] args) {
        testApostle(); testApollyon();
        List<ItemEntity> drops = List.of(new ItemEntity(),new ItemEntity(),new ItemEntity());
        onApollyonDrops(new LivingDropsEvent(new Object(), drops));
        check(drops.get(0).y == 30, "other entities' drops unchanged");
        onApollyonDrops(new LivingDropsEvent(new ApollyonEntity(), drops));
        for (ItemEntity drop : drops) check(drop.x == 100 && drop.y == 80.5 && drop.z == -200
                && drop.velocity.f_82479_ == 0 && drop.velocity.f_82481_ == 0, "all drops relocated without lateral scatter");
        System.out.println("PASS: both servants, config/phase matrix, 1200-tick timeout, combat, both hands, health cap, creative/client/dead handling, boss drops");
    }
FIXTURES
}
'''
events = (BASE / 'event/ApollyonCombatEvents.java').read_text('utf-8')
methods = servant_fixture('ApostleServantEntity.java', 'Apostle', 'SECOND', 'APOSTLE_AUTO_RESET_PHASE')
methods += servant_fixture('ApollyonServantEntity.java', 'Apollyon', 'SECOND_PHASE', 'APOLLYON_AUTO_RESET_PHASE')
methods += block(events, 'public static void onApollyonDrops(').replace('net.minecraft.world.phys.Vec3', 'Vec3')
for package in ['net.minecraft.network.chat.', 'net.minecraft.sounds.', 'net.minecraft.']:
    methods = methods.replace(package, '')
fixture = fixture.replace('FIXTURES', methods)
with tempfile.TemporaryDirectory(prefix='goety-blood-drops-') as directory:
    source = Path(directory) / 'BloodDropsRegression.java'
    source.write_text(fixture, encoding='utf-8')
    subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', str(source)], check=True)
    subprocess.run(['java', '-cp', directory, 'BloodDropsRegression'], check=True)
