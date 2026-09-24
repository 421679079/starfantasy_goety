"""Exercise the shared death trajectory, servant finale and saved-halo ownership methods."""
from pathlib import Path
import subprocess, time
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/starfantasy/goety'
def method(s, marker):
    start=s.index(marker);end=s.index('{',start)+1;depth=1
    while depth:
        depth+=(s[end]=='{')-(s[end]=='}');end+=1
    return s[start:end]
servant=(JAVA/'entity/ApollyonServantEntity.java').read_text('utf-8')
effects=(JAVA/'combat/ApollyonDeathEffects.java').read_text('utf-8')
halo=(JAVA/'item/FadedHaloItem.java').read_text('utf-8')
fixture=r'''
import java.util.*;
public class ServantDeathRegression {
    static int checks;
    static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static class CompoundTag {
        Map<String,Object> values=new HashMap<>();
        boolean m_128403_(String k){return values.get(k) instanceof UUID;}
        UUID m_128342_(String k){return (UUID)values.get(k);}
        void m_128362_(String k,UUID v){values.put(k,v);}
        String m_128461_(String k){return (String)values.getOrDefault(k,"");}
        CompoundTag m_128469_(String k){return (CompoundTag)values.getOrDefault(k,new CompoundTag());}
    }
    static class Vec3 {static final Vec3 f_82478_=new Vec3(0,0,0);double x,y,z;Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}}
    static class Level {boolean f_46443_;int explosions;List<Object> spawned=new ArrayList<>();void m_7967_(Object o){spawned.add(o);}void m_7605_(Object o,byte b){}}
    static class Entity {
        enum RemovalReason {KILLED}
        Level level=new Level();double y=64;boolean removed;
        Level m_9236_(){return level;}double m_20185_(){return 0;}double m_20186_(){return y;}double m_20189_(){return 0;}
        double m_20208_(double x){return 0;}double m_20187_(){return y+1;}double m_20262_(double z){return 0;}
        void m_6478_(Object type,Vec3 delta){y+=delta.y;}boolean m_213877_(){return removed;}
        void m_142687_(RemovalReason r){removed=true;}
    }
    static class LivingEntity extends Entity {}
    static class Player extends LivingEntity {UUID id=UUID.randomUUID();UUID m_20148_(){return id;}}
    static class Data {int age;void m_135381_(Object key,int v){age=v;}}
    static class Summoned extends LivingEntity {
        UUID ownerId;Player owner;String type="starfantasy_goety:apollyon_servant";
        UUID getOwnerId(){return ownerId;}Player getTrueOwner(){return owner;}
    }
    static class ApollyonServantEntity extends Summoned {}
    static class ItemStack {
        Object item;CompoundTag tag;ItemStack(Object item){this.item=item;}
        boolean m_150930_(Object o){return item==o;}CompoundTag m_41783_(){return tag;}
        CompoundTag m_41784_(){if(tag==null)tag=new CompoundTag();return tag;}
    }
    static class Holder {final Object value=new Object();Object get(){return value;}}
    static class HaloItemRegistry {static final Holder FADED_HALO=new Holder(),FADED_CROWN=new Holder();}
    static class ModEntityType {static final Holder FLYING_ITEM=new Holder();}
    static class FlyingItem {Player owner;ItemStack item;FlyingItem(Object type,Level l,double x,double y,double z){}
        void setOwner(Player p){owner=p;}void setItem(ItemStack s){item=s;}void setSecondsCool(int i){}}
    static class ItemEntity {UUID ownerId;ItemStack item;ItemEntity(Level l,double x,double y,double z,ItemStack s){item=s;}
        void m_266426_(UUID id){ownerId=id;}}
    static class FadedHaloItem {
        static void setSummon(Summoned s,ItemStack item){
            CompoundTag data=new CompoundTag();data.values.put("entity",s.type);
            if(s.ownerId!=null)data.m_128362_("Owner",s.ownerId);
            item.m_41784_().values.put("entity",data);
        }
        static void setOwnerName(Player p,ItemStack item){}
        HALO_METHODS
    }
    static class MoverType {static final Object SELF=new Object();}
    static class Explosion {enum BlockInteraction {KEEP}}
    static class LootingExplosion {enum Mode {LOOT}}
    static class ExplosionUtil {
        static void lootExplode(Level l,LivingEntity e,double x,double y,double z,float strength,boolean fire,Object interaction,Object mode){
            check(strength==0 && !fire && interaction==Explosion.BlockInteraction.KEEP,"death explosions preserve blocks and do no blast damage");
            l.explosions++;
        }
    }
    static class ApollyonDeathEffects {
        static final int APOLLYON_DEATH_TICKS=80;
        TRAJECTORY
        static void explodeApollyon(LivingEntity e){((Servant)e).finalExplosions++;}
    }
    static class Servant extends ApollyonServantEntity {
        boolean pig,pigDeathSoundPlayed; int pigCries;
        boolean isPigVariant(){return pig;}
        void m_5496_(Object sound,float volume,float pitch){check(sound==SoundEvents.f_12234_,"death uses pig death cry");pigCries++;}
        static final Object DEATH_AGE=new Object();
        Data f_19804_=new Data();int f_20919_,finalExplosions;double deathGroundY=64;
        boolean recallDropped,f_19812_,casting,noGravity;
        int deathAge(){return f_19804_.age;}
        void setCasting(boolean b){casting=b;}void m_20256_(Vec3 v){}void m_20242_(boolean b){noGravity=b;}
        DEATH_TICK
        PIG_DEATH_SOUND
    }
    static class SoundEvents {static final Object f_12234_=new Object();}
    public static void main(String[] args){
        Servant pig=new Servant();pig.pig=true;pig.owner=new Player();pig.ownerId=pig.owner.id;
        pig.playPigDeathSound();pig.playPigDeathSound();check(pig.pigCries==1,"death cry plays once");
        for(int age=1;age<80;age++){
            pig.m_6153_();
            check(pig.y==64 && pig.level.explosions==0 && pig.finalExplosions==0,"pig waits without rising or small explosions");
            check(!pig.removed && pig.level.spawned.isEmpty(),"no early removal or reward");
        }
        pig.m_6153_();check(pig.y==64 && pig.finalExplosions==1 && pig.removed,"pig explodes once at tick 80");
        pig.m_6153_();check(pig.finalExplosions==1 && pig.level.spawned.size()==1,"pig reward and explosion are once-only");
        Servant clientPig=new Servant();clientPig.pig=true;clientPig.level.f_46443_=true;
        clientPig.playPigDeathSound();check(clientPig.pigCries==0,"client does not duplicate pig cry");
        Servant normal=new Servant();normal.playPigDeathSound();check(normal.pigCries==0,"normal servant has no pig cry");
        Servant a=new Servant();a.owner=new Player();a.ownerId=a.owner.id;
        for(int age=1;age<=8;age++)a.m_6153_();
        check(a.y==64 && !a.removed,"first eight ticks remain at initial height");
        for(int age=9;age<=72;age++)a.m_6153_();
        check(Math.abs(a.y-73.6)<1e-8 && a.level.explosions==72,"rises 9.6 blocks with the same boss explosion timing");
        check(a.level.spawned.isEmpty(),"no early halo delivery");
        double prev=a.y;
        for(int age=73;age<80;age++){a.m_6153_();check(a.y<prev && !a.removed,"fall before final explosion");prev=a.y;}
        a.m_6153_();
        check(Math.abs(a.y-64)<1e-8 && a.removed && a.finalExplosions==1,"returns to starting height and finishes at tick 80");
        check(a.recallDropped && a.level.spawned.size()==1 && a.level.spawned.get(0) instanceof FlyingItem,"exactly one halo flies to online owner");
        FlyingItem flying=(FlyingItem)a.level.spawned.get(0);
        check(flying.owner==a.owner && FadedHaloItem.belongsTo(flying.item,a.owner) && FadedHaloItem.isApollyon(flying.item),"halo retains servant type and matching owner");
        check(flying.item.item==HaloItemRegistry.FADED_CROWN.get(),"Apollyon death returns the new faded crown");
        a.m_6153_();check(a.level.spawned.size()==1 && a.finalExplosions==1,"removed entity cannot deliver again");
        check(!FadedHaloItem.belongsTo(flying.item,new Player()),"other players cannot revive saved servant");
        flying.item.tag.m_128469_("entity").values.put("entity","minecraft:zombie");
        check(!FadedHaloItem.belongsTo(flying.item,a.owner),"unsupported entity data is rejected");
        for(boolean wasDelivered:new boolean[]{false,true}){
            Servant loaded=new Servant();loaded.ownerId=UUID.randomUUID();loaded.recallDropped=wasDelivered;
            loaded.f_19804_.age=79;loaded.y=65.2;loaded.deathGroundY=64;
            loaded.m_6153_();
            check(loaded.y==64 && loaded.removed,"resumed finale returns to saved initial height");
            check(loaded.level.spawned.size()==(wasDelivered?0:1),"persisted delivery flag prevents repeated rewards");
            if(!wasDelivered)check(((ItemEntity)loaded.level.spawned.get(0)).ownerId.equals(loaded.ownerId),"offline owner gets owned drop");
        }
        a=new Servant();a.f_19804_.age=79;a.m_6153_();check(a.level.spawned.isEmpty(),"unowned servant produces no owner halo");
        a=new Servant();a.level.f_46443_=true;a.m_6153_();
        check(a.deathAge()==0 && a.level.explosions==0 && a.level.spawned.isEmpty(),"client cannot run death progression or rewards");
        Summoned hades=new Summoned();hades.type="starfantasy_goety:hades_servant";hades.owner=new Player();hades.ownerId=hades.owner.id;
        ItemStack legacy=FadedHaloItem.capture(hades);
        check(legacy.item==HaloItemRegistry.FADED_HALO.get() && FadedHaloItem.belongsTo(legacy,hades.owner) && !FadedHaloItem.isApollyon(legacy),"Hades continues dropping a valid faded halo");
        ItemStack forgedCrown=new ItemStack(HaloItemRegistry.FADED_CROWN.get());forgedCrown.tag=legacy.tag;
        check(!FadedHaloItem.belongsTo(forgedCrown,hades.owner),"crown cannot contain a different species");
        legacy.tag.m_128469_("entity").m_128362_("Owner",UUID.randomUUID());
        check(!FadedHaloItem.belongsTo(legacy,hades.owner),"both outer and saved owner must agree");
        System.out.println("PASS: "+checks+" death trajectory, finale, once-only delivery and halo ownership checks.");
    }
}
'''
fixture=fixture.replace('        TRAJECTORY',method(effects,'public static boolean tickApollyon('))
fixture=fixture.replace('        DEATH_TICK',method(servant,'protected void m_6153_()'))
fixture=fixture.replace('        PIG_DEATH_SOUND',method(servant,'private void playPigDeathSound()'))
fixture=fixture.replace('        HALO_METHODS','\n'.join(method(halo,m) for m in ['public static ItemStack capture(', 'public static void returnToOwner(', 'public static boolean isApollyon(', 'public static boolean belongsTo(']))
out=ROOT/'build'/('servant-death-regression-'+str(time.time_ns()));out.mkdir(parents=True)
p=out/'ServantDeathRegression.java';p.write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(p)],check=True)
subprocess.run(['java','-cp',str(out),'ServantDeathRegression'],check=True)
# Both entities call the same trajectory/finale; the servant never starts a boss encounter.
boss=(JAVA/'entity/ApollyonEntity.java').read_text('utf-8')
assert 'ApollyonDeathEffects.tickApollyon(this,' in method(boss,'protected void m_6153_()')
assert '.pageant.' not in servant
save=method(servant,'public void m_7380_(');load=method(servant,'public void m_7378_(')
for key in ['ApollyonServantDeathAge','ApollyonServantDeathGroundY','ApollyonServantRecallDropped']:
    assert key in save and key in load
revive=method(servant,'public void prepareRevival()')
for reset in ['DEATH_AGE, 0', 'this.recallDropped = false', 'this.f_19812_ = false', 'this.m_20242_(false)', 'this.m_21153_(this.m_21233_())']:
    assert reset in revive
print('PASS: shared boss sequence, persisted death state and revival resets are wired.')
