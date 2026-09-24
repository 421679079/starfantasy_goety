"""Run the production altar interaction and completion logic against a small Java world."""
from pathlib import Path
import json
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "src/church/java/com/starfantasy/goety/church/UnderworldAltarEntity.java").read_text("utf-8")


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
import java.util.function.Predicate;
public class AltarRegression {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    enum Difficulty { NORMAL, PEACEFUL }
    enum MobSpawnType { TRIGGERED }
    record BlockPos(int x,int y,int z){BlockPos above(){return new BlockPos(x,y+1,z);}}
    static class Vec3 {
        final double x,y,z;
        Vec3(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
        static Vec3 atBottomCenterOf(BlockPos p){return new Vec3(p.x()+.5,p.y(),p.z()+.5);}
    }
    static class AABB {
        double x0,y0,z0,x1,y1,z1;
        AABB(BlockPos p){this(p.x(),p.y(),p.z(),p.x()+1,p.y()+1,p.z()+1);}
        AABB(double a,double b,double c,double d,double e,double f){x0=a;y0=b;z0=c;x1=d;y1=e;z1=f;}
        AABB inflate(double r){return new AABB(x0-r,y0-r,z0-r,x1+r,y1+r,z1+r);}
        boolean contains(Vec3 p){return p.x>=x0&&p.x<=x1&&p.y>=y0&&p.y<=y1&&p.z>=z0&&p.z<=z1;}
    }
    static class Entity {
        UUID uuid=UUID.randomUUID(); boolean removed; Vec3 pos=new Vec3(0,1,0);
        UUID getUUID(){return uuid;} boolean isRemoved(){return removed;} void discard(){removed=true;}
        void setPos(Vec3 p){pos=p;}
    }
    static class Player extends Entity {
        boolean spectator,creative; List<String> messages=new ArrayList<>();
        boolean isSpectator(){return spectator;} boolean isCreative(){return creative;}
    }
    static class ApollyonEntity extends Entity {
        boolean alive=true,persistent; Player target;
        boolean isAlive(){return alive;}
        void moveTo(double x,double y,double z,float yaw,float pitch){pos=new Vec3(x,y,z);}
        void finalizeSpawn(ServerLevel l,Object difficulty,MobSpawnType type,Object a,Object b){}
        void setPersistenceRequired(){persistent=true;} void setTarget(Player p){target=p;}
    }
    static class CompoundTag {void putInt(String key,int value){}}
    static class ApollyonPageantSummonEntity extends Entity {
        UUID owner;
        ApollyonPageantSummonEntity(Object type,ServerLevel level){}
        UUID pageantOwnerUuid(){return owner;}
        void saveWithoutId(CompoundTag t){} void load(CompoundTag t){}
    }
    static class Registry<T>{T value;Registry(T v){value=v;}T get(){return value;}}
    static class BossType {
        ApollyonEntity create(ServerLevel l){return new ApollyonEntity();}
        String getDescription(){return "TRANSLATABLE_ENTITY_NAME";}
    }
    static class ApollyonEntityRegistry {
        static Registry<BossType> APOLLYON=new Registry<>(new BossType());
        static Registry<Object> APOLLYON_PAGEANT_SUMMON=new Registry<>(new Object());
    }
    static class Level {}
    static class BlockState {}
    static class ServerLevel extends Level {
        long now; int scans,arrivals; boolean collisionFree=true,acceptBoss=true;
        Difficulty difficulty=Difficulty.NORMAL; List<Entity> entities=new ArrayList<>();List<Player> players=new ArrayList<>();
        Difficulty getDifficulty(){return difficulty;} long getGameTime(){return now;}
        <T extends Entity> List<T> getEntitiesOfClass(Class<T> type,AABB box,Predicate<T> predicate){
            scans++;List<T> out=new ArrayList<>();
            for(Entity e:entities)if(type.isInstance(e)&&box.contains(e.pos)&&predicate.test(type.cast(e)))out.add(type.cast(e));
            return out;
        }
        boolean noCollision(AABB b){return collisionFree;}
        Entity getEntity(UUID id){return entities.stream().filter(e->!e.removed&&e.uuid.equals(id)).findFirst().orElse(null);}
        Player getPlayerByUUID(UUID id){return players.stream().filter(p->p.uuid.equals(id)).findFirst().orElse(null);}
        Object getCurrentDifficultyAt(BlockPos pos){return new Object();}
        boolean addFreshEntity(Entity e){if(e instanceof ApollyonEntity&&!acceptBoss)return false;entities.add(e);return true;}
        long bosses(){return entities.stream().filter(e->e instanceof ApollyonEntity&&!e.removed).count();}
    }
    static class UnderworldAltarEntity {
        Level level;BlockPos worldPosition;Map<UUID,Long> confirmations=new HashMap<>();
        int remaining;long startedAt=Long.MIN_VALUE;UUID summoner,effectId;
        UnderworldAltarEntity(ServerLevel l,BlockPos p){level=l;worldPosition=p;}
        void setChanged(){} void initializeBridgeWaystone(ServerLevel l){}
        static void playArrival(ServerLevel l,Vec3 p){l.arrivals++;}
        static void tell(Player p,String key,Object... args){p.messages.add(key+(args.length==0?"":":"+args[0]));}
        METHODS
    }
    static final String DUPLICATE="duplicate:TRANSLATABLE_ENTITY_NAME";
    static class Scene {
        ServerLevel level=new ServerLevel();Player player=new Player();
        UnderworldAltarEntity altar=new UnderworldAltarEntity(level,new BlockPos(0,0,0));
        Scene(){level.players.add(player);}
        void start(){altar.use(player);level.now++;altar.use(player);}
        void finish(){
            for(int i=0;i<100;i++){level.now++;UnderworldAltarEntity.tick(level,altar.worldPosition,new BlockState(),altar);}
        }
    }
    public static void main(String[] args){
        Scene normal=new Scene();normal.altar.use(normal.player);
        check(normal.level.scans==0 && normal.player.messages.equals(List.of("confirm")),"first click does not scan");
        normal.altar.use(normal.player);
        check(normal.level.scans==0 && normal.altar.remaining==0,"same-tick/offhand duplicate cannot confirm");
        normal.level.now++;normal.altar.use(normal.player);
        check(normal.altar.remaining==100 && normal.level.scans==2,"confirmation checks bodies and ritual circles");
        int scans=normal.level.scans;normal.altar.use(normal.player);
        check(normal.level.scans==scans && normal.player.messages.get(1).equals("busy"),"busy altar avoids scans");
        UnderworldAltarEntity.tick(normal.level,normal.altar.worldPosition,new BlockState(),normal.altar);
        check(normal.altar.remaining==100,"start tick not counted");
        for(int i=0;i<99;i++){normal.level.now++;UnderworldAltarEntity.tick(normal.level,normal.altar.worldPosition,new BlockState(),normal.altar);}
        check(normal.level.scans==scans && normal.level.bosses()==0,"no periodic scans during windup");
        normal.level.now++;UnderworldAltarEntity.tick(normal.level,normal.altar.worldPosition,new BlockState(),normal.altar);
        check(normal.level.scans==scans+1 && normal.level.bosses()==1,"one completion scan and one boss");
        check(normal.level.getEntity(normal.altar.effectId)==null,"own circle removed");

        for(boolean dead:new boolean[]{false,true}){
            Scene s=new Scene();ApollyonEntity boss=new ApollyonEntity();boss.alive=!dead;s.level.entities.add(boss);s.start();
            check(s.altar.remaining==0 && s.player.messages.get(1).equals(DUPLICATE),"living and dying bodies share localized duplicate message");
            boss.discard();s.level.now++;s.altar.use(s.player);
            check(s.altar.remaining==100,"direct removal unlocks without death event or saved lock");
        }
        Scene circles=new Scene();
        ApollyonPageantSummonEntity circle=new ApollyonPageantSummonEntity(null,circles.level);circles.level.entities.add(circle);
        circles.start();check(circles.altar.remaining==0 && circles.player.messages.get(1).equals(DUPLICATE),"other altar circle blocks");
        circle.owner=UUID.randomUUID();circles.level.now++;circles.altar.use(circles.player);
        check(circles.altar.remaining==100,"pageant combat effect is not an altar ritual");
        Scene own=new Scene();ApollyonPageantSummonEntity ownCircle=new ApollyonPageantSummonEntity(null,own.level);
        own.level.entities.add(ownCircle);own.altar.effectId=ownCircle.uuid;own.start();
        check(own.altar.remaining==100,"own circle excluded");
        Scene distant=new Scene();ApollyonEntity far=new ApollyonEntity();far.pos=new Vec3(200,1,0);
        distant.level.entities.add(far);distant.start();check(distant.altar.remaining==100,"distant boss does not lock altar");

        Scene interrupted=new Scene();interrupted.start();interrupted.level.entities.add(new ApollyonEntity());interrupted.finish();
        check(interrupted.level.bosses()==1 && interrupted.level.arrivals==0
            && interrupted.player.messages.get(1).equals(DUPLICATE),"boss arriving during ritual cancels with same message");
        Scene simultaneous=new Scene();
        UnderworldAltarEntity other=new UnderworldAltarEntity(simultaneous.level,new BlockPos(10,0,0));
        simultaneous.altar.remaining=1;other.remaining=1;
        simultaneous.altar.summoner=simultaneous.player.uuid;other.summoner=simultaneous.player.uuid;
        UnderworldAltarEntity.tick(simultaneous.level,simultaneous.altar.worldPosition,new BlockState(),simultaneous.altar);
        UnderworldAltarEntity.tick(simultaneous.level,other.worldPosition,new BlockState(),other);
        check(simultaneous.level.bosses()==1 && simultaneous.level.arrivals==1
            && simultaneous.player.messages.equals(List.of(DUPLICATE)),"resumed rituals completing together produce only one boss");
        Scene peace=new Scene();peace.level.difficulty=Difficulty.PEACEFUL;peace.altar.use(peace.player);
        check(peace.level.scans==0 && peace.player.messages.equals(List.of("peaceful")),"peaceful guard unchanged");
        Scene blocked=new Scene();blocked.level.collisionFree=false;blocked.start();
        check(blocked.altar.remaining==0 && blocked.player.messages.get(1).equals("blocked"),"space guard unchanged");
        Scene expired=new Scene();expired.altar.use(expired.player);expired.level.now=100;expired.altar.use(expired.player);
        check(expired.level.scans==0 && expired.player.messages.equals(List.of("confirm","confirm")),"expired confirmation does not scan");
        System.out.println("PASS: altar confirmation/scan budget, living/death bodies, circles, direct removal, races and unified translated message");
    }
}
'''
methods = "\n".join(block(source, marker) for marker in [
    "public void use(", "private boolean nearbyBoss(", "private boolean nearbyRitual(",
    "public static void tick(", "private static void rejectDuplicate("])
fixture = fixture.replace("METHODS", methods)
with tempfile.TemporaryDirectory(prefix="church-altar-") as directory:
    file = Path(directory) / "AltarRegression.java"
    file.write_text(fixture, encoding="utf-8")
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", str(file)], check=True)
    subprocess.run(["java", "-cp", directory, "AltarRegression"], check=True)

assert "ActiveChurchBosses" not in source
assert not (ROOT / "src/church/java/com/starfantasy/goety/church/ActiveChurchBosses.java").exists()
assert "APOLLYON.get().getDescription()" in block(source, "private static void rejectDuplicate(")
lang = json.loads((ROOT / "src/main/resources/assets/starfantasy_goety/lang/zh_cn.json").read_text("utf-8"))
assert lang["message.starfantasy_goety.altar.duplicate"] == "周围已存在%s，不可重复召唤！"
print("PASS: legacy saved lock removed; existing localized duplicate text preserved")
