"""Execute persistent ownership and native summon-limit behavior without launching Minecraft."""
from pathlib import Path
import subprocess, time

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT/'src/mojang/java/com/starfantasy/goety/servant/ServantOwnershipData.java').read_text('utf-8')
body = 'static ' + source[source.index('public final class ServantOwnershipData'):]
body = body.replace('@SubscribeEvent ', '')
fixture = r'''
import java.util.*;
import java.util.function.*;
public class ServantOwnershipRegression {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static class CompoundTag {
        Map<String,Object> values=new HashMap<>();
        ListTag getList(String k,int t){return (ListTag)values.getOrDefault(k,new ListTag());}
        String getString(String k){return (String)values.getOrDefault(k,"");}
        boolean hasUUID(String k){return values.get(k) instanceof UUID;}
        UUID getUUID(String k){return (UUID)values.get(k);}
        void putUUID(String k,UUID v){values.put(k,v);}
        void putString(String k,String v){values.put(k,v);}
        void put(String k,ListTag v){values.put(k,v);}
        void put(String k,CompoundTag v){values.put(k,v);}
        CompoundTag getCompound(String k){return (CompoundTag)values.getOrDefault(k,new CompoundTag());}
        Set<String> getAllKeys(){return values.keySet();}
        int getInt(String k){return (int)values.getOrDefault(k,0);}
        void putInt(String k,int v){values.put(k,v);}
    }
    static class ListTag extends ArrayList<CompoundTag> {CompoundTag getCompound(int i){return get(i);}}
    static class Tag {static final int TAG_COMPOUND=10;}
    static abstract class SavedData {int dirty;void setDirty(){dirty++;}public abstract CompoundTag save(CompoundTag t);}
    static class Storage {
        Map<String,SavedData> data=new HashMap<>();
        <T extends SavedData>T computeIfAbsent(Function<CompoundTag,T> loader,Supplier<T> creator,String name){
            return (T)data.computeIfAbsent(name,k->creator.get());
        }
        void restart(){data.replaceAll((key,value)->ServantOwnershipData.load(value.save(new CompoundTag())));}
    }
    static class Level {}
    static class Server {ServerLevel overworld;ServerLevel overworld(){return overworld;}}
    static class ServerLevel extends Level {
        Server server;Storage storage=new Storage();
        ServerLevel(Server s){server=s;if(s.overworld==null)s.overworld=this;}
        Server getServer(){return server;}Storage getDataStorage(){return storage;}
    }
    static class BuiltInRegistries {static final BuiltInRegistries ENTITY_TYPE=new BuiltInRegistries();String getKey(String type){return type;}}
    enum RemovalReason {
        KILLED(true),DISCARDED(true),UNLOADED_TO_CHUNK(false),UNLOADED_WITH_PLAYER(false),CHANGED_DIMENSION(false);
        boolean destroy;RemovalReason(boolean b){destroy=b;}boolean shouldDestroy(){return destroy;}
    }
    static class Entity {
        UUID id=UUID.randomUUID();String type;Level level;RemovalReason reason;boolean added;
        Entity(String t,Level l){type=t;level=l;}String getType(){return type;}UUID getUUID(){return id;}
        Level level(){return level;}RemovalReason getRemovalReason(){return reason;}boolean isAddedToWorld(){return added;}
    }
    static class LivingEntity extends Entity {LivingEntity(Level l){super("minecraft:player",l);}}
    static class Owned extends Entity {UUID owner;Owned(String t,Level l,LivingEntity p){super(t,l);owner=p.getUUID();}UUID getOwnerId(){return owner;}}
    static class EntityLeaveLevelEvent {Entity e;EntityLeaveLevelEvent(Entity e){this.e=e;}Entity getEntity(){return e;}Level getLevel(){return e.level();}}
    DATA
    static final String HADES="starfantasy_goety:hades_servant", AP="starfantasy_goety:apollyon_servant";
    // Mirrors Goety RitualRequirements: loaded matching owned servants >= the entity's getSummonLimit.
    static boolean nativeCanSummon(String type,LivingEntity player,List<Owned> loaded){
        int count=0;for(Owned e:loaded)if(e.type.equals(type)&&e.owner.equals(player.id))count++;
        return count<ServantOwnershipData.summonLimit(new Entity(type,player.level()),player);
    }
    static void join(Owned e){e.added=true;e.reason=null;ServantOwnershipData.track(e);}
    static void leave(Owned e,RemovalReason reason){e.reason=reason;e.added=false;ServantOwnershipData.removed(new EntityLeaveLevelEvent(e));}
    public static void main(String[] args){
        Server server=new Server();ServerLevel world=new ServerLevel(server),nether=new ServerLevel(server);
        LivingEntity player=new LivingEntity(world),friend=new LivingEntity(world);
        List<Owned> empty=List.of();
        check(nativeCanSummon(HADES,player,empty)&&nativeCanSummon(AP,player,empty),"one free slot for each type");
        check(ServantOwnershipData.canOwnAnother(HADES,player),"owned Hades egg has a free slot");
        Owned hades=new Owned(HADES,world,player);
        ServantOwnershipData.track(hades);
        check(nativeCanSummon(HADES,player,empty),"probe creation, halo NBT reading and cancelled spawns do not reserve a slot");
        join(hades);
        check(!nativeCanSummon(HADES,player,List.of(hades)),"native check blocks a second loaded Hades");
        check(!ServantOwnershipData.canOwnAnother(HADES,player),"owned Hades egg blocks at the same limit");
        check(nativeCanSummon(AP,player,List.of(hades)),"Hades does not occupy Apollyon slot");
        check(nativeCanSummon(HADES,friend,List.of(hades)),"other player has independent slots");
        int dirty=ServantOwnershipData.get(world).dirty;ServantOwnershipData.track(hades);
        check(ServantOwnershipData.get(world).dirty==dirty,"idempotent tracking does not dirty save again");
        leave(hades,RemovalReason.UNLOADED_TO_CHUNK);
        check(!nativeCanSummon(HADES,player,empty),"unloaded Hades still blocks native ritual");
        check(ServantOwnershipData.contains(nether,hades.id),"all dimensions share halo identity guard");
        world.storage.restart();
        check(!nativeCanSummon(HADES,player,empty),"server restart preserves occupied slot");
        check(ServantOwnershipData.contains(world,hades.id),"NBT restores original identity");
        hades.level=nether;join(hades);leave(hades,RemovalReason.CHANGED_DIMENSION);
        check(!nativeCanSummon(HADES,player,empty),"dimension transfer never releases slot");
        hades.level=world;join(hades);
        Owned ap=new Owned(AP,nether,player);join(ap);
        check(!nativeCanSummon(AP,player,empty)&&!nativeCanSummon(HADES,player,empty),"one of each can coexist");
        leave(hades,RemovalReason.KILLED);
        check(nativeCanSummon(HADES,player,empty)&&!nativeCanSummon(AP,player,empty),"Hades death releases only Hades slot");
        check(!ServantOwnershipData.contains(nether,hades.id),"dead identity can be revived from halo");
        join(hades);
        check(!nativeCanSummon(HADES,player,empty),"revival reclaims slot");
        leave(ap,RemovalReason.DISCARDED);
        check(nativeCanSummon(AP,player,empty),"permanent removal releases slot");
        hades.owner=friend.id;ServantOwnershipData.track(hades);
        check(nativeCanSummon(HADES,player,empty)&&!nativeCanSummon(HADES,friend,empty),"ownership change updates both owners");
        hades.owner=null;ServantOwnershipData.track(hades);
        check(nativeCanSummon(HADES,friend,empty),"clearing owner removes old ownership record");
        hades.owner=player.id;ServantOwnershipData.track(hades);
        Owned legacyDuplicate=new Owned(HADES,world,player);join(legacyDuplicate);
        leave(hades,RemovalReason.KILLED);
        check(!nativeCanSummon(HADES,player,empty),"removing one pre-update duplicate cannot release remaining servant's slot");
        leave(legacyDuplicate,RemovalReason.KILLED);
        check(nativeCanSummon(HADES,player,empty),"all legacy servants gone releases slot");
        Owned unrelated=new Owned("goety:apostle",world,player);join(unrelated);
        check(!ServantOwnershipData.contains(world,unrelated.id),"unrelated entity types never tracked");
        Owned clientOnly=new Owned(HADES,new Level(),player);join(clientOnly);
        check(nativeCanSummon(HADES,player,empty),"client rendering creates no server reservation");
        join(hades);
        Server otherSave=new Server();ServerLevel otherWorld=new ServerLevel(otherSave);
        LivingEntity samePlayerOtherSave=new LivingEntity(otherWorld);samePlayerOtherSave.id=player.id;
        check(nativeCanSummon(HADES,samePlayerOtherSave,empty),"independent saves do not share slots");
        check(!nativeCanSummon(HADES,player,empty),"second summon sees first reservation immediately");
        String apostle="starfantasy_goety:apostle_servant";
        List<Owned> apostles=new ArrayList<>();
        for(int i=0;i<12;i++) {
            check(nativeCanSummon(apostle,player,apostles),"available apostle slot "+i);
            Owned e=new Owned(apostle,world,player);join(e);apostles.add(e);
        }
        check(!nativeCanSummon(apostle,player,apostles),"thirteenth Apostle blocked");
        check(!ServantOwnershipData.canOwnAnother(apostle,player),"all Apostle egg titles share twelve slots");
        apostles.forEach(e->leave(e,RemovalReason.UNLOADED_TO_CHUNK));world.storage.restart();
        check(!nativeCanSummon(apostle,player,empty),"twelve unloaded Apostles persist across restart");
        check(nativeCanSummon(apostle,friend,empty),"Apostle limit per player");
        leave(apostles.get(0),RemovalReason.KILLED);
        check(nativeCanSummon(apostle,player,empty),"Apostle death frees one slot");
        ServantOwnershipData.creditPillar(world,ap.id);ServantOwnershipData.creditPillar(world,ap.id);
        world.storage.restart();
        check(ServantOwnershipData.claimPillars(nether,ap.id)==2,"pending pillars survive restart and dimension changes");
        check(ServantOwnershipData.claimPillars(world,ap.id)==0,"pillar claims are consumed once");
        UUID retired=UUID.randomUUID();ServantOwnershipData.retirePillar(world,retired);world.storage.restart();
        check(ServantOwnershipData.consumeRetiredPillar(nether,retired),"unloaded pillar death cleanup survives restart");
        check(!ServantOwnershipData.consumeRetiredPillar(world,retired),"retirement marker consumed after cleanup");
        System.out.println("PASS: "+checks+" persistent ownership/native-limit lifecycle checks.");
    }
}
'''.replace('    DATA', body)
out=ROOT/'build'/('servant-ownership-regression-'+str(time.time_ns()));out.mkdir(parents=True)
p=out/'ServantOwnershipRegression.java';p.write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'ServantOwnershipRegression'],check=True)

# Ensure both real entity classes invoke the tested methods only from lifecycle hooks.
for name in ['HadesServantEntity','ApollyonServantEntity']:
    text=(ROOT/f'src/main/java/com/starfantasy/goety/entity/{name}.java').read_text('utf-8')
    assert 'return ServantOwnershipData.summonLimit(this, owner);' in text
    assert 'super.onAddedToWorld();\n        ServantOwnershipData.track(this);' in text
    assert 'super.setOwnerId(owner);\n        ServantOwnershipData.track(this);' in text
    assert text.count('ServantOwnershipData.track(this);')==2
print('PASS: both entity types track successful joins and ownership changes without a tick loop.')
