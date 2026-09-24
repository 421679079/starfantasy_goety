"""Execute production spell policies, cast clocks and damage loops with small Java world fixtures."""
from pathlib import Path
import json
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/mojang/java/com/starfantasy/goety/magic/focus'


def block(text, marker):
    start = text.index(marker)
    end = text.index('{', start) + 1
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def source(name):
    return (BASE / name).read_text('utf-8')


spell = source('BattleFocusSpell.java')
# A superclass-interface stop call is rewritten into virtual dispatch by RevelationFix,
# re-entering this override until the server stack overflows. Goety's default is empty.
stop_method = block(spell, 'public void stopSpell(')
assert 'ISpell.super.stopSpell(' not in stop_method
assert 'if (flower) FlowerCastingEntity.finish(level, caster);' in stop_method
for path in BASE.rglob('*.java'):
    assert 'ISpell.super.stopSpell(' not in path.read_text('utf-8'), path
assert 'public int spellCooldown(' not in spell and 'hasCustomCooldown(' not in spell and 'addCooldown(' not in spell, 'must inherit Goety cooldown reduction and normal application'
item = source('BattleFocusItem.java')
flower = source('FlowerArrowRain.java')
night = source('EvernightCast.java')
combat = source('BattleFocusCombat.java')
immunity = source('BattleFocusHitImmunity.java')
geometry = (ROOT.parent / 'star_fantasy_library/src/main/java/com/starfantasy/library/vfx/StarFantasyCageGeometry.java').read_text('utf-8')
spell_methods = '\n'.join(block(spell, sig) for sig in [
    'public int defaultSoulCost()', 'public int defaultCastDuration()', 'public int defaultSpellCooldown()',
    'public SpellType getSpellType()', 'public List<Enchantment> acceptedEnchantments()',
    'public static float damagePerHit(', 'public void SpellResult('])
item_methods = '\n'.join(block(item, sig) for sig in ['public boolean canApplyAtEnchantingTable(', 'public boolean isBookEnchantable('])
fixture = r'''
import java.util.*;
public class BattleFocusRegression {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    enum SpellType { NONE, NECROMANCY }
    static class Enchantment {}
    record Holder<T>(T get) {}
    static class ModEnchantments { static Holder<Enchantment> POTENCY=new Holder<>(new Enchantment()); }
    static class EnchantmentHelper { static Map<Enchantment,Integer> getEnchantments(ItemStack s){return s.enchants;} }
    static class ItemStack {
        Object item=new Object(); Map<Enchantment,Integer> enchants=new HashMap<>();
        Object getItem(){return item;} int getEnchantmentLevel(Enchantment e){return enchants.getOrDefault(e,0);}
    }
    static class IWand { static ItemStack getFocus(ItemStack stack){return stack;} }
    static class SpellStat {
        int potency; SpellStat(){this(0);} SpellStat(int potency){this.potency=potency;}
        int getPotency(){return potency;}
    }
    static class WandUtil {
        static int enchantment, virtualPower;
        static boolean enchantedFocus(LivingEntity caster){return enchantment>0 || virtualPower>0;}
        static int getPotencyLevel(LivingEntity caster){return enchantment+virtualPower;}
    }
    static class Value { int value; Value(int v){value=v;} int get(){return value;} }
    static class DoubleValue { double value; DoubleValue(double value){this.value=value;} double get(){return value;} }
    static class SpellConfig {
        static boolean allNonAllies;
        static class BooleanValue { boolean get(){return allNonAllies;} }
        static BooleanValue FINAL_ART_DAMAGE_ALL_NON_ALLIES=new BooleanValue();
        static Value FLOWER_SOULS=new Value(2500), EVERNIGHT_SOULS=new Value(100), FLOWER_CAST=new Value(80), EVERNIGHT_CAST=new Value(20),
            FLOWER_COOLDOWN=new Value(600), EVERNIGHT_COOLDOWN=new Value(200), FLOWER_FINAL_HITS=new Value(12);
        static DoubleValue FLOWER_DAMAGE=new DoubleValue(10), EVERNIGHT_DAMAGE=new DoubleValue(5);
        static Value EVERNIGHT_TOTAL_HITS=new Value(8);
    }
    static class Cooldown {
        Object item; int ticks=-1, writes;
        void addCooldown(Player p, Level l, Object item, int ticks){this.item=item;this.ticks=ticks;writes++;}
    }
    static class SEHelper { static Cooldown cooldown=new Cooldown(); static Cooldown getFocusCoolDown(Player p){return cooldown;} }
    static class Spell {
        boolean flower; Spell(boolean flower){this.flower=flower;}
        SPELL_METHODS
    }
    static class FocusItem { final Spell focusSpell=new Spell(true); ITEM_METHODS }
    static class TickEvent {
        enum Phase { START, END }
        record ServerTickEvent(Phase phase) {}
    }
    static class Level { boolean isClientSide; }
    static class ServerLevel extends Level {
        long time; boolean loaded=true; List<Entity> entities=new ArrayList<>();
        long getGameTime(){return time;} boolean hasChunkAt(Object pos){return loaded;}
        List<Entity> getEntities(Entity except,AABB box,java.util.function.Predicate<Entity> predicate){return entities;}
        int sounds; void playSound(Object p,double x,double y,double z,Object sound,Object type,float volume,float pitch){sounds++;}
        Registry registryAccess(){return new Registry();}
    }
    static class Registry { Registry registryOrThrow(Object type){return this;} Object getHolderOrThrow(Object type){return type;} }
    static class Registries { static Object DAMAGE_TYPE=new Object(); }
    static class DamageTypes { static String WITHER="wither"; }
    static class DamageSource { Object kind; DamageSource(Object kind,Entity direct,Entity owner){this.kind=kind;} }
    static class DamageSources {
        DamageSource indirectMagic(Entity a,Entity b){return new DamageSource("magic",a,b);}
        DamageSource sonicBoom(Entity caster){return new DamageSource("sonic_boom",caster,caster);}
    }
    static class Entity {
        Vec3 pos=new Vec3(0,0,0); boolean alive=true,removed; int id=next++; static int next=0;
        boolean isAlive(){return alive;} boolean isRemoved(){return removed;} int getId(){return id;}
        Vec3 position(){return pos;} void discard(){removed=true;}
    }
    static class LivingEntity extends Entity {
        LivingEntity lastAttacker; boolean playerPassenger;
        LivingEntity getLastHurtByMob(){return lastAttacker;}
        boolean hasPassenger(java.util.function.Predicate<Entity> filter){return playerPassenger && filter.test(new Player(level));}
        ServerLevel level; float health=10000; int invulnerableTime=20, hurtTime=10, hurtDuration=10, hits; boolean ally,spectator,attackable=true,canceled;
        String namespace="minecraft"; String getType(){return namespace;}
        Vec3 motion=new Vec3(1,2,3); List<Float> damage=new ArrayList<>(); Object lastSource,lastEffect; int effectTicks;
        LivingEntity(ServerLevel level){this.level=level;} ServerLevel level(){return level;}
        boolean isSpectator(){return spectator;} boolean isAttackable(){return attackable;}
        Vec3 getDeltaMovement(){return motion;} void setDeltaMovement(Vec3 v){motion=v;}
        boolean hurt(DamageSource source,float amount){if(canceled || invulnerableTime>0)return false;hits++;damage.add(amount);lastSource=source.kind;health-=amount;invulnerableTime=20;hurtTime=10;hurtDuration=10;return true;}
        DamageSources damageSources(){return new DamageSources();} Random getRandom(){return new Random(1);}
        void addEffect(MobEffectInstance effect,LivingEntity caster){lastEffect=effect.effect;effectTicks=effect.duration;}
    }
    record ResourceLocation(String getNamespace) {}
    static class EntityRegistry { ResourceLocation getKey(String type){return type==null?null:new ResourceLocation(type);} }
    static class ForgeRegistries { static EntityRegistry ENTITY_TYPES=new EntityRegistry(); }
    interface HitCooldownAccess { void starfantasy$clearHitCooldown(); }
    interface CombatHealthEntity { Protection combatHealthProtection(); }
    static class Protection { int ticks=20; void setInvulnerabilityTicks(int value){ticks=value;} }
    static class ProtectedBoss extends LivingEntity implements CombatHealthEntity {
        final Protection protection=new Protection(); boolean locked; int thirdPartyTimer=40;
        ProtectedBoss(ServerLevel level,String mod){super(level);namespace=mod;}
        public Protection combatHealthProtection(){return protection;}
        @Override boolean hurt(DamageSource source,float amount) {
            if(locked || protection.ticks>0)return false;
            boolean hit=super.hurt(source,Math.min(10,amount));if(hit)protection.ticks=20;return hit;
        }
    }
    static class GoetyBoss extends LivingEntity {
        int moddedInvul=20,obsidianInvul,spawnTicks,thirdPartyTimer=40;
        GoetyBoss(ServerLevel level){super(level);namespace="goety";}
        @Override boolean hurt(DamageSource source,float amount){
            if(moddedInvul>0 || obsidianInvul>0 || spawnTicks>0)return false;
            boolean hit=super.hurt(source,amount);if(hit)moddedInvul=20;return hit;
        }
    }
    static class Apostle extends GoetyBoss { Apostle(ServerLevel level){super(level);} }
    static class EnderKeeper extends GoetyBoss { EnderKeeper(ServerLevel level){super(level);} }
    static class Vizier extends GoetyBoss { Vizier(ServerLevel level){super(level);} }
    static class ApostleServant extends LivingEntity implements HitCooldownAccess {
        int cooldown=20; boolean phase;
        ApostleServant(ServerLevel level){super(level);namespace="starfantasy_goety";}
        public void starfantasy$clearHitCooldown(){cooldown=0;}
        @Override boolean hurt(DamageSource source,float amount){
            if(cooldown>0 || phase)return false;
            boolean hit=super.hurt(source,amount);if(hit)cooldown=20;return hit;
        }
    }
    static class BattleFocusHitImmunity { IMMUNITY_METHOD }
    static class Player extends LivingEntity { boolean creative,pvp=true; Player(ServerLevel l){super(l);} boolean isCreative(){return creative;} boolean canHarmPlayer(Player p){return pvp;} }
    static class PartEntity<T> extends Entity { Entity parent; PartEntity(Entity parent){this.parent=parent;} Entity getParent(){return parent;} }
    static class MobUtil { static boolean areAllies(Entity caster,Entity target){return target instanceof LivingEntity l && l.ally;} }
    static class BlockPos { static Object containing(Vec3 pos){return pos;} }
    static class AABB { AABB(Vec3 a,Vec3 b){} AABB inflate(double v){return this;} }
    static class Vec3 {
        static Vec3 ZERO=new Vec3(0,0,0); final double x,y,z;
        Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
        Vec3 add(Vec3 v){return new Vec3(x+v.x,y+v.y,z+v.z);} Vec3 add(double a,double b,double c){return add(new Vec3(a,b,c));}
        Vec3 subtract(Vec3 v){return new Vec3(x-v.x,y-v.y,z-v.z);} Vec3 scale(double n){return new Vec3(x*n,y*n,z*n);}
        double distanceToSqr(Vec3 v){Vec3 d=subtract(v);return d.x*d.x+d.y*d.y+d.z*d.z;}
        static Vec3 directionFromRotation(float pitch,float yaw){double t=Math.toRadians(yaw);return new Vec3(-Math.sin(t),0,Math.cos(t));}
    }
    static class Mth { static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));} }
    static class StarFantasyVfx {
        static int finales, smallExplosions; static void swordExplosion(Entity e,Vec3 p,double size){smallExplosions++;}
        static void finalExplosion(Entity e,Vec3 p,int color,double size,boolean sphere){finales++;}
        static void areaShake(Entity e,Vec3 p,double radius,int ticks,float amount){}
        static void areaShake(Entity e,Vec3 p,double radius,int in,int hold,int out,float amount){}
    }
    static class SoundSource { static Object PLAYERS=new Object(); }
    static class SoundEvents { static Object GENERIC_EXPLODE=new Object(); }
    static class BattleFocusContent { static Holder<Object> FLOWER_FINAL=new Holder<>(new Object()), EVERNIGHT_BURST=new Holder<>(new Object()); }
    static class FlowerArrowEntity extends Entity { boolean exploded; int explosions; void explode(){if(!exploded){exploded=true;explosions++;}} boolean isExploded(){return exploded;} }
    static class FlowerBurstRibbonEntity { static void spawn(ServerLevel l,Vec3 p,int c,boolean f){} }
    static class EvernightCageEntity extends Entity { Vec3 worldPoint(Vec3 local){return pos.add(local);} }
    static class GoetyEffects { static Holder<Object> TANGLED=new Holder<>(new Object()); }
    static class MobEffectInstance {
        Object effect; int duration;
        MobEffectInstance(Object effect,int duration,int amplifier,boolean ambient,boolean visible,boolean icon){this.effect=effect;this.duration=duration;}
    }
    static class BattleFocusCombat { COMBAT_METHODS }
    static class FinalArtTargeting { FINAL_ART_TARGETING }
    interface Enemy {}
    static class Mob extends LivingEntity {
        LivingEntity target; Mob(ServerLevel l){super(l);} LivingEntity getTarget(){return target;}
    }
    static class Monster extends Mob implements Enemy { Monster(ServerLevel l){super(l);} }
    static class StarFantasyCageGeometry { GEOMETRY_BODY }
    static class FlowerArrowRain {
        FLOWER_CONSTANTS
        static Map<UUID,Rain> ACTIVE=new LinkedHashMap<>(); static float spawnedDamage; static int spawnedHits;
        static void spawn(ServerLevel l,LivingEntity caster,float damage,int hits){spawnedDamage=damage;spawnedHits=hits;}
        FLOWER_TICK
        FLOWER_RAIN
    }
    static class EvernightCast {
        static Map<UUID,Cast> ACTIVE=new LinkedHashMap<>(); static float spawnedDamage; static int spawnedHits;
        static void spawn(ServerLevel l,LivingEntity caster,float damage,int hits){spawnedDamage=damage;spawnedHits=hits;}
        NIGHT_TICK
        NIGHT_CAST
    }
    public static void main(String[] args) {
        double[] originalX={-5.3,-3,0,3,5.3}, originalY={6,7.8,8.7,7.8,6};
        int[] perimeter={2,4,3,1,0};
        for(int i=0;i<5;i++) {
            Vec3 p=StarFantasyCageGeometry.impact(i);
            check(p.x==originalX[i] && p.y==originalY[i],"preserve bloom lateral position and height");
            check((p.x*p.x+p.z*p.z)/64+p.y*p.y/100<1,"all bloom endpoints inside cage dome");
            check(StarFantasyCageGeometry.bloom(i,StarFantasyCageGeometry.arrivalTick(i)).distanceToSqr(p)<1e-12,"flight reaches new endpoint");
            check(StarFantasyCageGeometry.bloom(i,StarFantasyCageGeometry.burstTick(i)).distanceToSqr(p)<1e-12,"charge and burst share endpoint");
            Vec3 a=StarFantasyCageGeometry.impact(perimeter[i]),b=StarFantasyCageGeometry.impact(perimeter[(i+1)%5]),c=StarFantasyCageGeometry.impact(perimeter[(i+2)%5]);
            check((b.x-a.x)*(c.z-b.z)-(b.z-a.z)*(c.x-b.x)<-1,"strict convex pentagon from above");
        }
        ServerLevel level=new ServerLevel(); Player player=new Player(level); ItemStack focus=new ItemStack();
        Spell flower=new Spell(true), night=new Spell(false);
        check(flower.defaultSoulCost()==2500 && night.defaultSoulCost()==100,"configured soul costs");
        check(flower.defaultCastDuration()==80 && night.defaultCastDuration()==20,"configured casting durations");
        check(flower.getSpellType()==SpellType.NONE && night.getSpellType()==SpellType.NECROMANCY,"magic school");
        check(flower.defaultSpellCooldown()==600 && night.defaultSpellCooldown()==200,"base cooldown ticks");
        SpellConfig.FLOWER_COOLDOWN.value=7;
        check(flower.defaultSpellCooldown()==7,"configured cooldown ticks remain exact");
        SpellConfig.FLOWER_COOLDOWN.value=600;
        FocusItem item=new FocusItem(); Enchantment other=new Enchantment();
        check(item.canApplyAtEnchantingTable(focus,ModEnchantments.POTENCY.get()),"Potency table accepted");
        check(!item.canApplyAtEnchantingTable(focus,other),"other table enchant rejected");
        focus.enchants.put(ModEnchantments.POTENCY.get(),4);
        check(item.isBookEnchantable(focus,focus),"Potency book accepted");
        focus.enchants.put(other,1);check(!item.isBookEnchantable(focus,focus),"mixed book rejected");focus.enchants.remove(other);
        WandUtil.enchantment=4;
        flower.SpellResult(level,player,focus,new SpellStat());night.SpellResult(level,player,focus,new SpellStat());
        check(FlowerArrowRain.spawnedDamage==12 && EvernightCast.spawnedDamage==6 && FlowerArrowRain.spawnedHits==12 && EvernightCast.spawnedHits==8,"5 percent per potency, applied once");
        check(Spell.damagePerHit(20,-1)==20,"negative potency ignored");
        SpellConfig.EVERNIGHT_DAMAGE.value=17;SpellConfig.EVERNIGHT_TOTAL_HITS.value=11;
        night.SpellResult(level,player,focus,new SpellStat());
        check(Math.abs(EvernightCast.spawnedDamage-20.4)<.0001 && EvernightCast.spawnedHits==11,"configured wither damage and total segments reach cast");
        SpellConfig.EVERNIGHT_DAMAGE.value=5;SpellConfig.EVERNIGHT_TOTAL_HITS.value=8;
        // Keep the actual stack enchanted to detect any accidental return to direct NBT reads.
        for(int[] sample:new int[][]{{0,0,0},{0,3,0},{4,0,0},{4,3,0},{4,3,2},{0,0,2}}) {
            WandUtil.enchantment=sample[0];WandUtil.virtualPower=sample[1];
            SpellStat stats=new SpellStat(sample[2]);
            flower.SpellResult(level,player,focus,stats);night.SpellResult(level,player,focus,stats);
            float multiplier=1+(sample[0]+sample[1]+sample[2])*.05F;
            check(Math.abs(FlowerArrowRain.spawnedDamage-10*multiplier)<.0001,"flower effective potency including staff and stats");
            check(Math.abs(EvernightCast.spawnedDamage-5*multiplier)<.0001,"evernight effective potency including staff and stats");
        }
        WandUtil.enchantment=4;WandUtil.virtualPower=0;


        LivingEntity target=new LivingEntity(level);target.pos=new Vec3(0,0,15);level.entities.add(target);
        LivingEntity ally=new LivingEntity(level);ally.ally=true;ally.pos=target.pos;level.entities.add(ally);
        level.entities.add(new PartEntity<>(target));
        FlowerArrowRain.Rain rain=new FlowerArrowRain.Rain(level,player,target.pos,20,12);
        for(int i=0;i<36;i++) rain.arrows.add(new FlowerArrowEntity());
        Arrays.fill(rain.impacts,target.pos);FlowerArrowRain.ACTIVE.put(UUID.randomUUID(),rain);
        for(int tick=0;tick<=40;tick++) {
            level.time=tick;FlowerArrowRain.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
            int hits=tick<30?0:tick<40?tick-29:22;
            check(target.hits==hits,"ten small impacts then twelve final hits at exact ticks");
            if(hits>0)check(target.lastSource.equals("sonic_boom"),"all flower impact and finale damage uses vanilla sonic boom");
        }
        check(StarFantasyVfx.smallExplosions==0,"old explosion particles removed; synced arrows render black fire");
        check(level.sounds==11,"ten impact sounds plus only one final sound");
        for(FlowerArrowEntity arrow:rain.arrows) check(arrow.explosions==1 && !arrow.removed,"every arrow explodes once and keeps its trail");
        check(ally.hits==0 && target.health==9560 && target.lastSource.equals("sonic_boom"),"sonic boom totals, no allies or multipart duplication");
        FlowerArrowRain.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));check(target.hits==22,"finisher cannot repeat");
        EvernightCageEntity visual=new EvernightCageEntity();visual.pos=target.pos;
        EvernightCast.Cast cast=new EvernightCast.Cast(level,player,visual,10,8);EvernightCast.ACTIVE.put(UUID.randomUUID(),cast);
        int prior=target.hits;
        for(int tick=0;tick<=48;tick++) {
            level.time=40+tick;EvernightCast.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
            int hits=tick<40?0:tick<48?(tick-40)/2+1:8;
            check(target.hits==prior+hits,"five cage impacts then three final hits");
            if(tick<48)check(target.lastEffect==GoetyEffects.TANGLED.get() && target.effectTicks<=2,"refresh short Tangled");
        }
        check(target.lastSource.equals("wither") && target.health==9480,"wither totals");
        EvernightCast.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));check(target.hits==prior+8,"cage finisher cannot repeat");
        check(target.invulnerableTime==0 && target.hurtTime==0 && target.hurtDuration==0 && target.motion.x==1 && target.motion.y==2 && target.motion.z==3,"clear hit immunity after segments and avoid knockback");
        target.pos=new Vec3(0,0,1000);check(BattleFocusCombat.targets(level,player,Vec3.ZERO,30).isEmpty(),"radius enforced");
        Player victim=new Player(level);level.entities.add(victim);player.pvp=false;
        check(BattleFocusCombat.targets(level,player,Vec3.ZERO,30).isEmpty(),"PVP permissions enforced");
        player.pvp=true;victim.creative=true;
        check(BattleFocusCombat.targets(level,player,Vec3.ZERO,30).isEmpty(),"creative immunity");
        victim.creative=false;victim.spectator=true;
        check(BattleFocusCombat.targets(level,player,Vec3.ZERO,30).isEmpty(),"spectator immunity");
        victim.spectator=false;check(BattleFocusCombat.targets(level,player,Vec3.ZERO,30).contains(victim),"eligible PvP target included");
        victim.pos=new Vec3(0,0,1000);target.pos=new Vec3(0,0,15);
        FlowerArrowRain.Rain custom=new FlowerArrowRain.Rain(level,player,target.pos,7,3);
        int before=target.hits;custom.finish();check(target.hits==before+3 && target.damage.get(target.damage.size()-1)==7,"configured final segments and per-hit damage");
        FlowerArrowRain.Rain abandoned=new FlowerArrowRain.Rain(level,player,target.pos,20,12);
        FlowerArrowRain.ACTIVE.put(UUID.randomUUID(),abandoned);level.loaded=false;
        FlowerArrowRain.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        check(FlowerArrowRain.ACTIVE.isEmpty() && target.hits==before+3,"unloaded arena cancels cast without damage");
        level.loaded=true;
        for(int total:new int[]{6,8,13}) {
            EvernightCast.Cast configured=new EvernightCast.Cast(level,player,visual,7,total);
            int previous=target.hits;
            for(int i=0;i<5;i++) configured.impact(i);
            configured.finish();
            check(target.hits==previous+total,"five small explosions plus configured final segments");
            check(target.damage.get(target.damage.size()-1)==7,"configured per-segment wither damage");
        }
        // Production two-focus damage loop + production allowlisted immunity helper.
        ServerLevel arena=new ServerLevel(); Player caster=new Player(arena);
        DamageSource magic=caster.damageSources().indirectMagic(caster,caster);
        for(GoetyBoss boss:new GoetyBoss[]{new Apostle(arena),new EnderKeeper(arena),new Vizier(arena)}) {
            arena.entities.clear();arena.entities.add(boss);
            BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
            check(boss.hits==3 && boss.moddedInvul==0 && boss.thirdPartyTimer==40,"Goety native timer only, all segments hit");
            boss.obsidianInvul=20;BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
            check(boss.hits==3 && boss.obsidianInvul==20,"monolith protection preserved");
            boss.obsidianInvul=0;boss.spawnTicks=20;BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
            check(boss.hits==3 && boss.spawnTicks==20,"spawn protection preserved");
        }
        for(String mod:new String[]{"star_fantasy_bosses","starfantasy_goety"}) {
            ProtectedBoss boss=new ProtectedBoss(arena,mod);arena.entities.clear();arena.entities.add(boss);
            BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,100,3);
            check(boss.hits==3 && boss.health==9970 && boss.protection.ticks==0,"our hit timers cleared, damage cap retained per segment");
            boss.locked=true;BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,100,3);
            check(boss.hits==3 && boss.health==9970 && boss.thirdPartyTimer==40,"phase locks and third-party state preserved");
        }
        ProtectedBoss foreign=new ProtectedBoss(arena,"other_mod");arena.entities.clear();arena.entities.add(foreign);
        BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,100,3);
        check(foreign.hits==0 && foreign.protection.ticks==20 && foreign.thirdPartyTimer==40 && foreign.invulnerableTime==0,"foreign library-interface adopter: only vanilla timer touched");
        Apostle foreignSubclass=new Apostle(arena);foreignSubclass.namespace="other_addon";arena.entities.clear();arena.entities.add(foreignSubclass);
        BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
        check(foreignSubclass.hits==0 && foreignSubclass.moddedInvul==20,"foreign Goety subclass not opted in");
        ApostleServant servant=new ApostleServant(arena);arena.entities.clear();arena.entities.add(servant);
        BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
        check(servant.hits==3 && servant.cooldown==0,"our servant cooldown cleared");
        servant.phase=true;BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
        check(servant.hits==3,"servant transition preserved");
        servant.phase=false;servant.canceled=true;BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,20,3);
        check(servant.hits==3,"canceled hurt is never forced through");
        servant.cooldown=20;servant.invulnerableTime=20;
        BattleFocusCombat.damage(arena,caster,Vec3.ZERO,30,magic,0,3);
        check(servant.cooldown==20 && servant.invulnerableTime==20,"zero damage does not clear immunity");
        arena.isClientSide=true;BattleFocusHitImmunity.clear(servant);
        check(servant.cooldown==20 && servant.invulnerableTime==20,"client does not mutate immunity");
        BattleFocusHitImmunity.clear(null);
        ServerLevel selective=new ServerLevel();Player owner=new Player(selective),otherPlayer=new Player(selective);
        Monster hostile=new Monster(selective),friendlyMonster=new Monster(selective),mounted=new Monster(selective);
        friendlyMonster.ally=true;mounted.playerPassenger=true;
        LivingEntity cow=new LivingEntity(selective),villager=new LivingEntity(selective),pet=new LivingEntity(selective);
        pet.ally=true;
        Mob retaliating=new Mob(selective),attackingOwner=new Mob(selective),attackingAlly=new Mob(selective),unrelatedFight=new Mob(selective);
        retaliating.lastAttacker=owner;attackingOwner.target=owner;attackingAlly.target=pet;unrelatedFight.target=cow;
        selective.entities.addAll(List.of(owner,otherPlayer,hostile,friendlyMonster,mounted,cow,villager,pet,retaliating,attackingOwner,attackingAlly,unrelatedFight));
        DamageSource selectiveMagic=owner.damageSources().indirectMagic(owner,owner);
        java.util.function.Predicate<LivingEntity> allowed=t->FinalArtTargeting.allows(owner,t);
        var selected=BattleFocusCombat.targets(selective,owner,Vec3.ZERO,12).stream().filter(allowed).toList();
        check(new HashSet<>(selected).equals(Set.of(hostile,retaliating,attackingOwner,attackingAlly)),"default selection: monsters and caster-side combat only");
        BattleFocusCombat.damageFiltered(selective,owner,Vec3.ZERO,12,selectiveMagic,10,5,allowed);
        for(LivingEntity e:List.of(hostile,retaliating,attackingOwner,attackingAlly))check(e.hits==5,"all final segments hit qualified targets");
        for(LivingEntity e:List.of(owner,otherPlayer,friendlyMonster,mounted,cow,villager,pet,unrelatedFight))
            check(e.hits==0&&e.invulnerableTime==20&&e.hurtTime==10,"excluded targets retain health and immunity");
        SpellConfig.allNonAllies=true;
        BattleFocusCombat.damageFiltered(selective,owner,Vec3.ZERO,12,selectiveMagic,10,1,allowed);
        for(LivingEntity e:List.of(otherPlayer,mounted,cow,villager,unrelatedFight))check(e.hits==1,"opt-in permits all non-allies");
        for(LivingEntity e:List.of(owner,friendlyMonster,pet))check(e.hits==0,"opt-in cannot bypass self or Goety allies");
        otherPlayer.creative=true;int pvpHits=otherPlayer.hits;
        BattleFocusCombat.damageFiltered(selective,owner,Vec3.ZERO,12,selectiveMagic,10,1,allowed);
        check(otherPlayer.hits==pvpHits,"opt-in retains creative protection");
        otherPlayer.creative=false;otherPlayer.spectator=true;
        BattleFocusCombat.damageFiltered(selective,owner,Vec3.ZERO,12,selectiveMagic,10,1,allowed);
        check(otherPlayer.hits==pvpHits,"opt-in retains spectator protection");
        otherPlayer.spectator=false;owner.pvp=false;
        BattleFocusCombat.damageFiltered(selective,owner,Vec3.ZERO,12,selectiveMagic,10,1,allowed);
        check(otherPlayer.hits==pvpHits,"opt-in retains PvP protection");
        SpellConfig.allNonAllies=false;
        int cowHits=cow.hits;
        BattleFocusCombat.damage(selective,owner,Vec3.ZERO,12,selectiveMagic,10,1);
        check(cow.hits==cowHits+1,"flower/evernight targeting unaffected by Final Art config");
        System.out.println("PASS: two-focus timelines; Final Art hostile/combat targeting and config; ally/player protections; immunity, caps and event cancellation");
    }
}
'''
parts = {
    'SPELL_METHODS': spell_methods, 'ITEM_METHODS': item_methods,
    'COMBAT_METHODS': '\n'.join(block(combat, m) for m in ['static List<LivingEntity> targets(', 'static void damage(', 'static void damageFiltered(']),
    'FINAL_ART_TARGETING': '\n'.join(block(source('FinalArtTargeting.java'), m) for m in ['static boolean allows(', 'private static boolean isCasterSide(']),
    'IMMUNITY_METHOD': block(immunity, 'static void clear('),
    'GEOMETRY_BODY': geometry[geometry.index('    public static final int BLOOMS'):geometry.rfind('}')],
    'FLOWER_CONSTANTS': flower[flower.index('    public static final int ARROW_COUNT'):flower.index('    private static final Map')],
    'FLOWER_TICK': block(flower, 'public static void tick('), 'FLOWER_RAIN': block(flower, 'private static final class Rain'),
    'NIGHT_TICK': block(night, 'public static void tick('), 'NIGHT_CAST': block(night, 'private static final class Cast'),
}
for key, value in parts.items():
    fixture = re.sub(r'\b' + key + r'\b', lambda match: value, fixture)
with tempfile.TemporaryDirectory(prefix='battle-focus-') as directory:
    file = Path(directory) / 'BattleFocusRegression.java'
    file.write_text(fixture, encoding='utf-8')
    subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', str(file)], check=True)
    subprocess.run(['java', '-cp', directory, 'BattleFocusRegression'], check=True)

# Resource links must be valid without installing the source SlashBlade mod.
resources = ROOT / 'src/main/resources/assets/starfantasy_goety'
for name in ['blooms_and_plumes_focus', 'evernight_focus', 'final_art_focus']:
    data = json.loads((resources / f'models/item/{name}.json').read_text('utf-8'))
    assert data['textures']['layer0'] == f'starfantasy_goety:item/{name}'
    assert (resources / f'textures/item/{name}.png').is_file()
    for locale in ['zh_cn', 'en_us']:
        lang = json.loads((resources / f'lang/{locale}.json').read_text('utf-8'))
        assert f'item.starfantasy_goety.{name}' in lang and f'item.starfantasy_goety.{name}.info' in lang
for file in BASE.rglob('*.java'):
    text = file.read_text('utf-8')
    assert 'com.starfantasy.slashart' not in text and 'mods.flammpfeil' not in text
print('PASS: item models/textures/translations; no runtime SlashBlade dependency')

config = (ROOT / "src/mojang/java/com/starfantasy/goety/config/SpellConfig.java").read_text("utf-8")
for focus, souls, damage in [("blooms_and_plumes_focus", 2500, 10), ("evernight_focus", 100, 5)]:
    section = config.split(f'b.push("{focus}");')[1].split("b.pop();")[0]
    assert f'defineInRange("soul_cost", {souls},' in section
    assert f'defineInRange("damage_per_hit", {damage}D,' in section
assert 'defineInRange("total_hit_count", 8, 6, 200)' in config
assert 'comment("每段爆炸的凋零伤害")' in config and 'comment("总计爆炸段数")' in config
assert "BattleFocusConfig" not in (ROOT / "src/main/java/com/starfantasy/goety/StarFantasyGoetyMod.java").read_text("utf-8")
print("PASS: unified Spell config; Evernight damage and total segments; minimum 6")
