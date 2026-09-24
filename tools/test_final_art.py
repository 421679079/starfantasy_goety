"""Exercise Final Art production methods with Java fixtures, not an in-game test."""
from pathlib import Path
import json
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "src/mojang/java/com/starfantasy/goety/magic/focus"
entity = (BASE / "FinalArtEntity.java").read_text("utf-8")
spell = (BASE / "FinalArtSpell.java").read_text("utf-8")
item = (BASE / "BattleFocusItem.java").read_text("utf-8")


def block(text, marker):
    start = text.index(marker)
    end = text.index("{", start) + 1
    depth = 1
    while depth:
        depth += (text[end] == "{") - (text[end] == "}")
        end += 1
    return text[start:end]


fixture = r'''
import java.util.*;
public class FinalArtRegression {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void close(double a,double b,String message){check(Math.abs(a-b)<1E-6,message+": "+a+" != "+b);}
    record Vec3(double x,double y,double z) {
        static Vec3 ZERO=new Vec3(0,0,0);
        Vec3 subtract(Vec3 v){return new Vec3(x-v.x,y-v.y,z-v.z);}
        Vec3 scale(double s){return new Vec3(x*s,y*s,z*s);}
        double length(){return Math.sqrt(dot(this));}
        double dot(Vec3 v){return x*v.x+y*v.y+z*v.z;}
        double distanceToSqr(Vec3 v){Vec3 d=subtract(v);return d.dot(d);}
        Vec3 normalize(){double d=length();return d<1E-6?ZERO:scale(1/d);}
    }
    record AABB(Vec3 min,Vec3 max){AABB inflate(double r){return this;}}
    static class Level {}
    record Hit(long age,double radius,float amount,int segments,String source) {}
    static class ServerLevel extends Level {
        long time; Map<UUID,Entity> entities=new HashMap<>();List<Hit> hits=new ArrayList<>();
        Entity getEntity(UUID id){return entities.get(id);}
        List<Entity> getEntities(Entity except,AABB box,java.util.function.Predicate<Entity> filter){
            return entities.values().stream().filter(e->e!=except).filter(filter).toList();
        }
    }
    static class Entity {
        int tickCount;UUID id=UUID.randomUUID();Level level;boolean alive=true,removed,spectator;
        Vec3 pos=Vec3.ZERO,velocity=Vec3.ZERO;boolean hurtMarked,hasImpulse;int movements;
        Entity(Level l){level=l;if(l instanceof ServerLevel s)s.entities.put(id,this);}
        void tick(){tickCount++;} Level level(){return level;}
        boolean isAlive(){return alive&&!removed;}boolean isRemoved(){return removed;}boolean isSpectator(){return spectator;}
        void discard(){removed=true;} UUID getUUID(){return id;} Vec3 position(){return pos;}
        Vec3 getDeltaMovement(){return velocity;}void setDeltaMovement(Vec3 v){velocity=v;movements++;}
        Sources damageSources(){return new Sources();}
    }
    static class LivingEntity extends Entity {
        boolean eligible=true;double resistance;int hits;LivingEntity lastAttacker;
        LivingEntity(Level l){super(l);}LivingEntity getLastHurtByMob(){return lastAttacker;}
        boolean hasPassenger(java.util.function.Predicate<Entity> filter){return false;}
    }
    static class Player extends LivingEntity {Player(Level l){super(l);}}
    interface Enemy {}
    static class Monster extends Mob implements Enemy {Monster(Level l){super(l);}}
    static class FinalArtTargeting {TARGETING_METHODS}
    static class ItemEntity extends Entity {ItemEntity(Level l){super(l);}}
    static class ExperienceOrb extends Entity {ExperienceOrb(Level l){super(l);}}
    static class Mob extends LivingEntity {
        LivingEntity target;LivingEntity getTarget(){return target;}
        double attack=10;Mob(Level l){super(l);}Object getAttribute(Object attr){return attr;}
        double getAttributeValue(Object attr){return attack;}
    }
    static class Attributes {static final Object ATTACK_DAMAGE=new Object();}
    static class Sources {String indirectMagic(Entity a,Entity b){return "magic";}}
    static class MobUtil {
        static boolean areAllies(LivingEntity c,LivingEntity t){return !t.eligible;}
        static int calls;static double resistanceFactor;
        static void pull(Entity e,double x,double y,double z,double factor){
            calls++;resistanceFactor=factor;
            double resistance=e instanceof LivingEntity living?living.resistance:0;
            e.setDeltaMovement(e.velocity.subtract(new Vec3(x,y,z)).scale(Math.max(0,1-resistance*factor)));e.hasImpulse=true;
        }
    }
    static class BattleFocusContent {
        static class Sound {Object get(){return this;}}
        static Sound FINAL_ART_PULL=new Sound(),FINAL_ART_BURST=new Sound();
    }
    static class SoundEvents {
        record SoundHolder(Object value) {}
        static SoundHolder RESPAWN_ANCHOR_DEPLETE=new SoundHolder("respawn_anchor_deplete");
        static String GENERIC_EXPLODE="generic_explode";
    }
    static class BattleFocusCombat {
        static List<LivingEntity> targets(ServerLevel l,LivingEntity c,Vec3 p,double r){
            return l.entities.values().stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast)
                .filter(e->e!=c&&e.isAlive()&&e.eligible&&!e.spectator&&e.pos.distanceToSqr(p)<=r*r).toList();
        }
        static void damageFiltered(ServerLevel l,LivingEntity c,Vec3 p,double r,String source,float amount,int segments,
                                   java.util.function.Predicate<LivingEntity> filter){
            l.hits.add(new Hit(l.time,r,amount,segments,source));
            for(LivingEntity target:targets(l,c,p,r))if(filter.test(target))target.hits+=segments;
        }
    }
    static class Data {
        int duration=300;int get(Object key){return duration;}void set(Object key,int value){duration=value;}
    }
    static class FinalArtEntity extends Entity {
        static final int SPAWN_TOTAL_TICKS=20;
        CONSTANTS
        static Object DURATION=new Object();Data entityData=new Data();
        UUID ownerUuid;float damage=10,coreDamage=2;int finalHits=5;double pullRadius=16;
        boolean restored,exploded;int sounds;
        FinalArtEntity(ServerLevel l,LivingEntity owner){super(l);ownerUuid=owner.id;entityData.set(DURATION,ACTIVE_TICKS);}
        float visualAge(float p){return ((ServerLevel)level).time+p;}
        void playSound(Object s,float v,float p){sounds++;}
        static FinalArtEntity last;
        static void spawn(ServerLevel l,LivingEntity c,float core,float damage,int hits,int duration,double radius,double range){
            last=new FinalArtEntity(l,c);last.coreDamage=core;last.damage=damage;last.finalHits=hits;
            last.entityData.set(DURATION,duration);last.pullRadius=radius;last.castRange=range;
        }
        double castRange;
        ENTITY_METHODS
    }
    static class Enchantment {}
    record Holder<T>(T get) {}
    static class ModEnchantments {
        static Holder<Enchantment> POTENCY=new Holder<>(new Enchantment()),DURATION=new Holder<>(new Enchantment()),
            RANGE=new Holder<>(new Enchantment()),RADIUS=new Holder<>(new Enchantment());
    }
    static class ItemStack {Map<Enchantment,Integer> enchants=new HashMap<>();}
    static class EnchantmentHelper {static Map<Enchantment,Integer> getEnchantments(ItemStack s){return s.enchants;}}
    static class WandUtil {
        static int potency,duration,radius,range;static float multiplier=1;
        static int getPotencyLevel(LivingEntity c){return potency;}
        static int getRangeLevel(LivingEntity c){return range;}
        static int getLevels(Enchantment e,LivingEntity c){return e==ModEnchantments.DURATION.get()?duration:radius;}
        static float damageMultiply(){return multiplier;}
    }
    static class SpellStat {
        int potency,duration=300,range=16;double radius;
        int getPotency(){return potency;}int getDuration(){return duration;}int getRange(){return range;}
        double getRadius(){return radius;}
    }
    static class VoidRiftSpell {SpellStat defaultStats(){return new SpellStat();}}
    enum SpellType {NONE,VOID}
    record Value<T>(T get) {}
    static class GoetySpellConfig {static Value<Double> RuptureDamage=new Value<>(2D);}
    static class SpellConfig {
        static Value<Boolean> FINAL_ART_DAMAGE_ALL_NON_ALLIES=new Value<>(false);
        static Value<Integer> FINAL_ART_SOULS=new Value<>(500),FINAL_ART_CAST=new Value<>(100),
            FINAL_ART_COOLDOWN=new Value<>(2400),FINAL_ART_FINAL_HITS=new Value<>(5);
        static Value<Double> FINAL_ART_DAMAGE=new Value<>(10D);
    }
    static class Mth {static double clamp(double v,double lo,double hi){return Math.min(hi,Math.max(lo,v));}}
    static class BattleFocusSpell {DAMAGE_METHOD}
    static class Spell {SPELL_METHODS}
    static class FocusItem {final Spell focusSpell=new Spell();ITEM_METHODS}
    static LivingEntity owner(ServerLevel l){return new LivingEntity(l);}
    static void tick(ServerLevel l,FinalArtEntity c,int age){l.time=age;c.tick();}
    static long hits(ServerLevel l,float amount){return l.hits.stream().filter(h->h.amount==amount).count();}
    public static void main(String[] args) {
        ServerLevel l=new ServerLevel();LivingEntity owner=owner(l);FinalArtEntity cast=new FinalArtEntity(l,owner);
        for(int age=0;age<=330;age++)tick(l,cast,age);
        check(hits(l,2)==30,"15 seconds of core damage every 10 ticks");
        check(l.hits.stream().filter(h->h.amount==10&&h.segments==1).map(Hit::age).toList()
            .equals(List.of(20L,80L,140L,200L,260L)),"immediate pulse plus three-second intervals");
        check(l.hits.get(l.hits.size()-1).equals(new Hit(320,12,10,5,"magic")),"five-hit final explosion");
        check(l.hits.stream().allMatch(h->h.radius==12),"all damage radii fixed at 12");
        check(cast.sounds==7&&cast.removed,"five pulses plus two Rupture finale sounds and timed cleanup");
        check(cast.pullAge(19)==-1&&cast.pullAge(20)==0&&cast.pullAge(79)==59&&cast.pullAge(320)==-1,"phase boundaries");
        ServerLevel longWorld=new ServerLevel();FinalArtEntity longer=new FinalArtEntity(longWorld,owner(longWorld));
        longer.entityData.set(FinalArtEntity.DURATION,600);longer.pullRadius=80;
        for(int age=0;age<=630;age++)tick(longWorld,longer,age);
        check(hits(longWorld,2)==60,"duration enchantment doubles core ticks");
        check(longWorld.hits.stream().filter(h->h.amount==10&&h.segments==1).count()==10,"extra duration adds pulses");
        check(longWorld.hits.get(longWorld.hits.size()-1).age==620&&longer.removed,"extended finale and cleanup");
        check(longWorld.hits.stream().allMatch(h->h.radius==12),"radius enchantment cannot enlarge damage");
        close(longer.getRiftScale(),cast.getRiftScale(),"radius enchantment cannot enlarge visuals");
        ServerLevel customWorld=new ServerLevel();FinalArtEntity custom=new FinalArtEntity(customWorld,owner(customWorld));
        custom.damage=7;custom.finalHits=9;tick(customWorld,custom,320);custom.tick();
        check(customWorld.hits.equals(List.of(new Hit(320,12,7,9,"magic"))),"custom damage/count and one finale only");
        for(int mode=0;mode<6;mode++){
            ServerLevel world=new ServerLevel();LivingEntity p=owner(world);FinalArtEntity c=new FinalArtEntity(world,p);
            if(mode==0)p.alive=false;if(mode==1)world.entities.remove(p.id);if(mode==2)p.level=new Level();
            if(mode==3)c.restored=true;if(mode==4)p.spectator=true;if(mode==5)p.removed=true;
            tick(world,c,320);check(c.removed&&world.hits.isEmpty(),"invalid owner/restored entity cannot damage");
        }
        ServerLevel pulls=new ServerLevel();LivingEntity caster=owner(pulls);FinalArtEntity pull=new FinalArtEntity(pulls,caster);
        LivingEntity target=new Monster(pulls);target.pos=new Vec3(10,0,0);
        LivingEntity neutral=new LivingEntity(pulls);neutral.pos=target.pos;
        Player bystander=new Player(pulls);bystander.pos=target.pos;
        ItemEntity drop=new ItemEntity(pulls);drop.pos=new Vec3(10,0,0);
        ExperienceOrb orb=new ExperienceOrb(pulls);orb.pos=drop.pos;
        LivingEntity ally=new LivingEntity(pulls);ally.eligible=false;ally.pos=target.pos;
        Entity projectile=new Entity(pulls);projectile.pos=target.pos;
        ItemEntity distantDrop=new ItemEntity(pulls);distantDrop.pos=new Vec3(16.01,0,0);
        ExperienceOrb distantOrb=new ExperienceOrb(pulls);distantOrb.pos=distantDrop.pos;
        tick(pulls,pull,19);check(target.movements==0&&drop.movements==0&&orb.movements==0,"spawn animation has no attraction");
        tick(pulls,pull,20);
        check(neutral.movements==0&&neutral.hits==0&&bystander.movements==0&&bystander.hits==0,"core and pulse protect neutral creatures and players");
        close(target.velocity.x,-.28125,"pulse triples native force instead of pulling to center");
        check(drop.velocity.equals(target.velocity)&&orb.velocity.equals(target.velocity),"items and XP receive the same pulse");
        check(target.hurtMarked&&drop.hurtMarked&&orb.hurtMarked&&target.hasImpulse&&drop.hasImpulse&&orb.hasImpulse,"motion sync for all target types");
        check(ally.movements==0&&caster.movements==0&&projectile.movements==0&&distantDrop.movements==0&&distantOrb.movements==0,"target exclusions unchanged");
        target.pos=new Vec3(8,0,0);
        for(int age=20;age<140;age++){
            target.velocity=Vec3.ZERO;tick(pulls,pull,age);
            close(target.velocity.x,(age-20)%60<10?-.375:-.125,"exactly 10 pulse ticks in each 60-tick cycle at age "+age);
        }
        target.velocity=new Vec3(.2,.3,.4);tick(pulls,pull,80);
        close(target.velocity.x,-.175,"pulse adds force rather than replacing existing motion");
        close(target.velocity.y,.3,"pulse preserves vertical motion at equal height");
        close(target.velocity.z,.4,"pulse preserves tangential motion");
        target.resistance=1;target.velocity=Vec3.ZERO;tick(pulls,pull,80);
        close(target.velocity.x,-.1875,"pulse respects native knockback-resistance calculation");
        target.resistance=0;
        target.pos=new Vec3(8,0,0);target.velocity=Vec3.ZERO;drop.pos=target.pos;drop.velocity=Vec3.ZERO;
        orb.pos=target.pos;orb.velocity=Vec3.ZERO;tick(pulls,pull,30);
        close(target.velocity.x,-.125,"native continuous pull at half radius");
        close(drop.velocity.x,-.125,"items receive native continuous pull");
        close(orb.velocity.x,-.125,"XP receives native continuous pull");
        close(MobUtil.resistanceFactor,.5,"Goety resistance factor unchanged");
        target.pos=new Vec3(16,0,0);target.velocity=Vec3.ZERO;tick(pulls,pull,31);
        check(target.velocity.equals(Vec3.ZERO),"native pull decays to zero at outer edge");
        pull.pullRadius=32;target.pos=new Vec3(20,0,0);target.velocity=Vec3.ZERO;tick(pulls,pull,32);
        close(target.velocity.x,-.09375,"radius scales continuous pull range");
        target.velocity=Vec3.ZERO;tick(pulls,pull,80);close(target.velocity.x,-.28125,"expanded radius pulse is still exactly three times native force");
        drop.removed=true;int moved=drop.movements;tick(pulls,pull,81);check(drop.movements==moved,"picked-up items ignored");
        orb.removed=true;moved=orb.movements;tick(pulls,pull,82);check(orb.movements==moved,"collected XP ignored");
        target.pos=Vec3.ZERO;target.velocity=Vec3.ZERO;tick(pulls,pull,83);
        check(target.velocity.equals(Vec3.ZERO),"center force is finite and zero");
        ExperienceOrb edgeOrb=new ExperienceOrb(pulls);edgeOrb.pos=new Vec3(32,0,0);
        tick(pulls,pull,84);check(edgeOrb.velocity.equals(Vec3.ZERO),"pulse falls to zero at range boundary");
        tick(pulls,pull,85);edgeOrb.pos=new Vec3(20,0,0);edgeOrb.velocity=Vec3.ZERO;tick(pulls,pull,86);
        close(edgeOrb.velocity.x,-.28125,"radius enchantment also expands XP attraction");
        moved=edgeOrb.movements;tick(pulls,pull,320);check(edgeOrb.movements==moved,"final explosion does not pull or damage XP");
        check(neutral.movements==0&&neutral.hits==0&&bystander.movements==0&&bystander.hits==0,"all phases protect neutral creatures and players");
        ServerLevel broad=new ServerLevel();LivingEntity broadOwner=owner(broad);
        FinalArtEntity broadCast=new FinalArtEntity(broad,broadOwner);
        LivingEntity broadNeutral=new LivingEntity(broad);broadNeutral.pos=new Vec3(8,0,0);
        LivingEntity broadAlly=new LivingEntity(broad);broadAlly.eligible=false;broadAlly.pos=broadNeutral.pos;
        SpellConfig.FINAL_ART_DAMAGE_ALL_NON_ALLIES=new Value<>(true);
        tick(broad,broadCast,20);
        check(broadNeutral.hits==2&&broadNeutral.movements==1,"opt-in applies to core, pulse damage and pulse pull");
        tick(broad,broadCast,30);
        check(broadNeutral.hits==3&&broadNeutral.movements==2,"opt-in also applies to continuous pull");
        tick(broad,broadCast,320);
        check(broadNeutral.hits==8&&broadAlly.hits==0&&broadAlly.movements==0,"opt-in applies to all final hits without affecting allies");
        SpellConfig.FINAL_ART_DAMAGE_ALL_NON_ALLIES=new Value<>(false);
        Spell focus=new Spell();SpellStat stats=focus.defaultStats();
        check(focus.getSpellType()==SpellType.VOID&&focus.defaultSoulCost()==500,"void school and 500 base souls");
        focus.SpellResult(l,owner,new ItemStack(),stats);FinalArtEntity base=FinalArtEntity.last;
        check(base.finalTick()==320&&base.pullRadius==16&&base.castRange==16,"native base duration/radius/range");
        close(base.coreDamage,2,"native base core damage");close(base.damage,10,"extra damage default");
        WandUtil.potency=3;WandUtil.duration=1;WandUtil.radius=2;WandUtil.range=4;WandUtil.multiplier=2;
        stats.potency=2;stats.radius=1;
        focus.SpellResult(l,owner,new ItemStack(),stats);FinalArtEntity enhanced=FinalArtEntity.last;
        check(enhanced.finalTick()==620&&enhanced.pullRadius==64&&enhanced.castRange==20,"stats plus native enchantments");
        close(enhanced.coreDamage,5,"core: Rupture base times multiplier times five-percent potency bonus");
        close(enhanced.damage,12.5,"extra: five percent per combined potency");
        focus.SpellResult(l,new Mob(l),new ItemStack(),stats);close(FinalArtEntity.last.coreDamage,6.25,"mob base damage also gains five percent per potency");
        WandUtil.multiplier=1;stats.potency=0;
        for(int potency:new int[]{0,1,4,10}){
            WandUtil.potency=potency;focus.SpellResult(l,owner,new ItemStack(),stats);
            close(FinalArtEntity.last.coreDamage,2*(1+.05*potency),"core five percent at potency "+potency);
            close(FinalArtEntity.last.damage,10*(1+.05*potency),"pulse/finale same scaling at potency "+potency);
        }
        FocusItem focusItem=new FocusItem();ItemStack book=new ItemStack();
        for(Enchantment e:focus.acceptedEnchantments()){
            check(focusItem.canApplyAtEnchantingTable(new ItemStack(),e),"all four supported enchantments apply");book.enchants.put(e,1);
        }
        check(focusItem.isBookEnchantable(new ItemStack(),book),"supported enchanted books accepted");
        Enchantment unrelated=new Enchantment();book.enchants.put(unrelated,1);
        check(!focusItem.canApplyAtEnchantingTable(new ItemStack(),unrelated)&&!focusItem.isBookEnchantable(new ItemStack(),book),"unsupported enchantments rejected");
        System.out.println("PASS: "+checks+" Final Art clock, core/pulse/finale, duration/radius/potency, target/item pull and enchantment checks");
    }
}
'''
constants = entity[entity.index("    public static final int ACTIVE_TICKS"):entity.index("    private static final EntityDataAccessor")]
methods = "\n".join(block(entity, marker) for marker in [
    "public int finalTick(", "public int pullAge(", "public void tick(", "private void damageTargets(",
    "private void pullTarget(", "public float getRiftScale("])
spell_methods = "\n".join(block(spell, marker) for marker in [
    "public SpellStat defaultStats(", "public int defaultSoulCost(", "public int defaultCastDuration(",
    "public int defaultSpellCooldown(", "public SpellType getSpellType(", "public List<Enchantment> acceptedEnchantments(",
    "public void SpellResult("])
item_methods = "\n".join(block(item, marker) for marker in [
    "public boolean canApplyAtEnchantingTable(", "public boolean isBookEnchantable("])
damage_method = block((BASE / "BattleFocusSpell.java").read_text("utf-8"), "public static float damagePerHit(")
for key, value in dict(CONSTANTS=constants, ENTITY_METHODS=methods, SPELL_METHODS=spell_methods,
                       ITEM_METHODS=item_methods, DAMAGE_METHOD=damage_method,
                       TARGETING_METHODS="\n".join(block((BASE / "FinalArtTargeting.java").read_text("utf-8"), m)
                           for m in ["static boolean allows(", "private static boolean isCasterSide("])).items():
    fixture = fixture.replace(key, value)
fixture = fixture.replace("com.Polarice3.Goety.config.SpellConfig", "GoetySpellConfig")
with tempfile.TemporaryDirectory(prefix="final-art-") as directory:
    file = Path(directory) / "FinalArtRegression.java"
    file.write_text(fixture, encoding="utf-8")
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", str(file)], check=True)
    subprocess.run(["java", "-cp", directory, "FinalArtRegression"], check=True)

config = (ROOT / "src/mojang/java/com/starfantasy/goety/config/SpellConfig.java").read_text("utf-8")
section = config.split('b.push("final_art_focus");')[1].split("b.pop();")[0]
defaults = dict(re.findall(r'defineInRange\("([^"]+)",\s*(\d+)D?', section))
assert defaults == {"cast_duration_ticks": "100", "cooldown_ticks": "2400", "soul_cost": "500", "damage_per_hit": "10", "final_hit_count": "5"}
assert "spellCooldown(" not in spell and "addCooldown(" not in spell
assert "buffer.writeInt(entityData.get(DURATION))" in entity and "entityData.set(DURATION, buffer.readInt())" in entity
assert "effect.entityData.set(DURATION, Math.max(1, duration))" in entity
assert '.comment("黑洞会伤害所有非友方生物").define("damage_all_non_allies", false)' in section
assert "BattleFocusCombat.damageFiltered(" in block(entity, "private void damageTargets(")
assert "FinalArtTargeting.allows(caster, target)" in block(entity, "private void damageTargets(")
renderer = (BASE / "client/FinalArtRenderer.java").read_text("utf-8")
assert "new FinalCurtainRenderer<>(context)" in renderer and "FinalArtExplosion.render(" in renderer
assert renderer.count("entity.finalTick()") == 2 and "FINAL_TICK" not in renderer
assets = ROOT / "src/main/resources/assets/starfantasy_goety"
library = ROOT.parent / "star_fantasy_library/src/main/resources/assets/star_fantasy_library"
sounds = json.loads((assets / "sounds.json").read_text("utf-8"))
for event in ["final_art_start", "final_art_pull"]:
    assert sounds[event]["sounds"][0]["name"] == "starfantasy_goety:" + event
    assert (assets / ("sounds/" + event + ".ogg")).is_file()
assert "final_art_burst" not in sounds
assert 'playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 5, .5F)' in entity
assert 'playSound(SoundEvents.GENERIC_EXPLODE, 5, .5F)' in entity
assert not (library / "textures/effect/denia_black_hole_starfield.png").exists()
for event in ["final_art_start", "final_art_pull", "final_art_burst"]:
    assert not (library / ("sounds/" + event + ".ogg")).exists()
for file in [BASE / "FinalArtEntity.java", BASE / "FinalArtSpell.java", BASE / "client/FinalArtRenderer.java"]:
    assert "com.starfantasy.bosses" not in file.read_text("utf-8")
print("PASS: six config defaults, shared damage/immunity path, synchronized duration and fixed-size shared visuals")
