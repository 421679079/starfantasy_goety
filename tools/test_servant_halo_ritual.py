"""Run the real halo ritual's conversion/revival/ownership/spawn-failure branches."""
from pathlib import Path
import json, subprocess, time
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/starfantasy/goety'
source=(JAVA/'ritual/ServantHaloRitual.java').read_text('utf-8')
body='static '+source[source.index('public final class ServantHaloRitual'):]
fixture=r'''
import java.util.*;
public class HaloRitualRegression {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static class CompoundTag {
        UUID id;CompoundTag m_128469_(String key){return this;}
        boolean m_128403_(String key){return id!=null;}UUID m_128342_(String key){return id;}
    }
    static class Entity {UUID id=UUID.randomUUID();}
    static class Summoned extends Entity {UUID owner;boolean prepared,tamed;String equipment="retained";UUID getOwnerId(){return owner;}}
    static class HadesServantEntity extends Summoned {void prepareRevival(){prepared=true;}}
    static class ApollyonServantEntity extends Summoned {void prepareRevival(){prepared=true;}}
    static class Player {UUID id=UUID.randomUUID();UUID m_20148_(){return id;}}
    static class ServerPlayer extends Player {}
    static class Level {boolean acceptSpawn=true;List<Entity> spawned=new ArrayList<>();
        boolean m_7967_(Entity e){if(!acceptSpawn)return false;spawned.add(e);return true;}}
    static class Server {List<ServerLevel> levels=new ArrayList<>();List<ServerLevel> m_129785_(){return levels;}}
    static class ServerLevel extends Level {Server server;Map<UUID,Entity> loaded=new HashMap<>();
        ServerLevel(Server s){server=s;s.levels.add(this);}Server m_7654_(){return server;}
        Entity m_8791_(UUID id){return loaded.get(id);}}
    static class ItemStack {Summoned saved;int count=1;CompoundTag tag=new CompoundTag();Object item=HaloItemRegistry.FADED_HALO.get();
        ItemStack(Summoned s){saved=s;tag.id=s.id;}CompoundTag m_41783_(){return tag;}void m_41774_(int n){count-=n;}
        boolean m_150930_(Object expected){return item==expected;}}
    static class ItemHolder {final Object item=new Object();Object get(){return item;}}
    static class HaloItemRegistry {static final ItemHolder FADED_HALO=new ItemHolder(),FADED_CROWN=new ItemHolder();}
    static class FadedHaloItem {
        static boolean belongsTo(ItemStack s,Player p){return s!=null && s.count>0 && p!=null && p.id.equals(s.saved.owner);}
        static boolean isApollyon(ItemStack s){return s.saved instanceof ApollyonServantEntity;}
    }
    static class ReviveServantItem {static Entity getSummon(ItemStack s,Level l){return s.saved;}}
    static class EntityType {boolean apollyon;EntityType(boolean ap){apollyon=ap;}
        Entity m_20615_(Level l){return apollyon?new ApollyonServantEntity():new HadesServantEntity();}}
    static class Holder {EntityType type=new EntityType(true);EntityType get(){return type;}}
    static class ApollyonEntityRegistry {static final Holder APOLLYON_SERVANT=new Holder();}
    static class RitualRecipe {EntityType type;RitualRecipe(boolean ap){type=ap?ApollyonEntityRegistry.APOLLYON_SERVANT.get():new EntityType(false);}
        EntityType getEntityToSummon(){return type;}}
    static class RitualRequirements {static EntityType checked;static boolean allow=true;
        static boolean canSummon(Level l,Player p,EntityType t){checked=t;return allow;}}
    static class ServantOwnershipData {static Set<UUID> recorded=new HashSet<>();
        static boolean contains(ServerLevel l,UUID id){return recorded.contains(id);}}
    static class BlockPos {}static class DarkAltarBlockEntity {}static class Ingredient {}
    static class CriteriaTriggers {static final CriteriaTriggers f_10580_=new CriteriaTriggers();void m_68256_(ServerPlayer p,Entity e){}}
    static class Ritual {
        RitualRecipe recipe;int completed;Ritual(RitualRecipe r){recipe=r;}
        public boolean identify(Level l,BlockPos b,Player p,ItemStack s){return true;}
        public boolean isValid(Level l,BlockPos b,DarkAltarBlockEntity a,Player p,ItemStack s,List<Ingredient> i){return true;}
        public void finish(Level l,BlockPos b,DarkAltarBlockEntity a,Player p,ItemStack s){completed++;}
        void prepareLivingEntityForSpawn(Summoned s,Level l,BlockPos b,DarkAltarBlockEntity a,Player p,boolean tame){
            if(tame){s.owner=p.id;s.tamed=true;}
        }
    }
    RITUAL
    public static void main(String[] args){
        Server server=new Server();ServerLevel level=new ServerLevel(server),otherDimension=new ServerLevel(server);
        Player owner=new ServerPlayer();BlockPos pos=new BlockPos();DarkAltarBlockEntity altar=new DarkAltarBlockEntity();
        ServantHaloRitual ap=new ServantHaloRitual(new RitualRecipe(true)),hades=new ServantHaloRitual(new RitualRecipe(false));
        ServantHaloRitual crownRitual=new ServantHaloRitual(new RitualRecipe(true),true);
        HadesServantEntity previous=new HadesServantEntity();previous.owner=owner.id;ItemStack ring=new ItemStack(previous);
        check(ap.identify(level,pos,owner,ring),"Hades halo accepted by Apollyon sacrifice recipe");
        check(RitualRequirements.checked==ap.recipe.type,"summon limits checked for destination Apollyon type");
        ap.finish(level,pos,altar,owner,ring);
        Entity result=level.spawned.get(0);
        check(result instanceof ApollyonServantEntity && !result.id.equals(previous.id),"Hades halo creates a new Apollyon identity");
        check(((Summoned)result).owner.equals(owner.id) && ((Summoned)result).prepared && ((Summoned)result).tamed,"new Apollyon prepared and assigned to caster");
        check(ring.count==0 && ap.completed==1,"successful conversion consumes one ring");
        ApollyonServantEntity saved=new ApollyonServantEntity();saved.owner=owner.id;saved.equipment="enchanted bow";
        ring=new ItemStack(saved);
        check(hades.identify(level,pos,owner,ring) && hades.isValid(level,pos,altar,owner,ring,List.of()),"Apollyon halo accepts Hades Sabbath recipe");
        hades.finish(level,pos,altar,owner,ring);
        result=level.spawned.get(1);
        check(result instanceof HadesServantEntity && !result.id.equals(saved.id),"Apollyon halo creates new Hades identity");
        check(((Summoned)result).prepared && ((Summoned)result).tamed && ((Summoned)result).owner.equals(owner.id),"converted Hades is prepared and owned");
        check(!((Summoned)result).equipment.equals(saved.equipment) && ring.count==0,"conversion consumes ring without copying other species equipment");
        ring=new ItemStack(saved);
        ap.finish(level,pos,altar,owner,ring);
        check(level.spawned.get(2)==saved && saved.prepared && !saved.tamed && saved.equipment.equals("enchanted bow"),"Apollyon revival keeps saved identity owner and equipment");
        check(ring.count==0,"successful revival consumes ring");
        ring=new ItemStack(previous);int before=level.spawned.size();
        ap.finish(level,pos,altar,new Player(),ring);
        check(ring.count==1 && level.spawned.size()==before,"other player cannot convert owner's ring");
        otherDimension.loaded.put(previous.id,previous);
        ap.finish(level,pos,altar,owner,ring);
        check(ring.count==1 && level.spawned.size()==before,"loaded identity in another dimension blocks duplicate conversion");
        otherDimension.loaded.clear();ServantOwnershipData.recorded.add(previous.id);
        ap.finish(level,pos,altar,owner,ring);
        check(ring.count==1 && level.spawned.size()==before,"unloaded identity blocks copied halo conversion");
        ServantOwnershipData.recorded.clear();level.acceptSpawn=false;
        ap.finish(level,pos,altar,owner,ring);
        check(ring.count==1 && level.spawned.size()==before,"failed spawn does not consume ring");
        level.acceptSpawn=true;hades.finish(level,pos,altar,owner,ring);
        check(ring.count==0 && level.spawned.get(before)==previous && previous.prepared,"Hades Sabbath revival remains available");
        ring=new ItemStack(saved);RitualRequirements.allow=false;
        check(!ap.identify(level,pos,owner,ring),"destination summon limit enforced");
        ap.finish(level,pos,altar,owner,ring);check(ring.count==1,"failed requirement does not consume ring");
        RitualRequirements.allow=true;
        ring=new ItemStack(saved);ring.item=HaloItemRegistry.FADED_CROWN.get();before=level.spawned.size();
        check(crownRitual.identify(level,pos,owner,ring) && crownRitual.isValid(level,pos,altar,owner,ring,List.of()),"crown accepted by separate Sabbath revival");
        check(!ap.identify(level,pos,owner,ring) && !hades.identify(level,pos,owner,ring),"crown cannot be mistaken for either halo recipe");
        ap.finish(level,pos,altar,owner,ring);hades.finish(level,pos,altar,owner,ring);
        check(ring.count==1 && level.spawned.size()==before,"wrong recipe finish leaves crown untouched");
        crownRitual.finish(level,pos,altar,owner,ring);
        check(ring.count==0 && level.spawned.get(before)==saved && saved.equipment.equals("enchanted bow"),"Sabbath crown revival restores exact servant and equipment");
        ring=new ItemStack(previous);ring.item=HaloItemRegistry.FADED_CROWN.get();
        check(!crownRitual.identify(level,pos,owner,ring),"crown revival rejects Hades data");
        crownRitual.finish(level,pos,altar,owner,ring);check(ring.count==1,"invalid crown not consumed");
        ring=new ItemStack(saved);
        check(!crownRitual.identify(level,pos,owner,ring),"old halo retains original recipe and cannot ambiguously match new crown ritual");
        ring.item=HaloItemRegistry.FADED_CROWN.get();
        crownRitual.finish(level,pos,altar,new Player(),ring);check(ring.count==1,"only original owner can revive crown");
        RitualRequirements.allow=false;crownRitual.finish(level,pos,altar,owner,ring);
        check(ring.count==1,"crown revival still enforces player ownership limit");
        System.out.println("PASS: "+checks+" actual ritual conversion/revival and failure checks.");
    }
}
'''.replace('    RITUAL',body)
out=ROOT/'build'/('halo-ritual-regression-'+str(time.time_ns()));out.mkdir(parents=True)
p=out/'HaloRitualRegression.java';p.write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'HaloRitualRegression'],check=True)
data=ROOT/'src/main/resources/data/starfantasy_goety'
recipe=json.loads((data/'recipes/summon_apollyon_servant.json').read_text('utf-8'))
assert recipe['ritual_type']=='starfantasy_goety:summon_apollyon'
assert recipe['craftType']=='expert_nether' and recipe['research']=='forbidden'
assert recipe['activation_item']=={'item':'starfantasy_goety:faded_halo'}
assert {x['item'] for x in recipe['ingredients']}=={'goety:unholy_hat','goety:unholy_robe','minecraft:bow'}
assert len(recipe['ingredients'])==3
tag=recipe['entity_to_sacrifice']['tag'].split(':')[1]
assert json.loads((data/f'tags/entity_types/{tag}.json').read_text('utf-8'))['values']==['goety:apostle']
print('PASS: exact materials, Apostle sacrifice, Expert Nether and forbidden research recipe fields.')
revive=json.loads((data/'recipes/revive_apollyon_servant.json').read_text('utf-8'))
assert revive['activation_item']=={'item':'starfantasy_goety:faded_crown'}
assert revive['ritual_type']=='starfantasy_goety:revive_apollyon' and revive['craftType']=='sabbath'
assert revive['ingredients']==recipe['ingredients']
assert 'entity_to_sacrifice' not in revive and 'research' not in revive
assert revive['soulCost']==1 and revive['duration']==15
print('PASS: separate crown activation, same materials, Sabbath revival with no sacrifice.')
