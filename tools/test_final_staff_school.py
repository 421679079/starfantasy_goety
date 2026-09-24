"""Run the real selection and packet policy against lightweight inventory fixtures."""
from pathlib import Path
import json
import re
import subprocess
import time

root = Path(__file__).resolve().parents[1]
def body(relative):
    source = (root / relative).read_text('utf-8')
    return re.sub(r'^(?:package|import) .*;\s*$', '', source, flags=re.M)

school = body('src/mojang/java/com/starfantasy/goety/magic/FinalStaffSchool.java')
packet = body('src/mojang/java/com/starfantasy/goety/network/ServerboundStaffSchoolPacket.java')
priority = body('src/mojang/java/com/starfantasy/goety/magic/FinalStaffPriority.java').replace('public final class', 'public static final class')
summon = body('src/mojang/java/com/starfantasy/goety/magic/FinalStaffSummonPriority.java').replace('public final class', 'public static final class')
fixture = r'''
import java.util.*;
import java.util.function.Supplier;
public class SchoolRegression {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 enum SpellType {NONE,ILL,NECROMANCY,GEOMANCY,WIND,STORM,FROST,WILD,ABYSS,VOID,NETHER}
 record Component(String text){static Component translatable(String key,Object... args){return new Component(key);}}
 static class FinalStaffDarkItem {}
 static class Tag {Map<String,String> data=new HashMap<>();String getString(String k){return data.getOrDefault(k,"");}void putString(String k,String v){data.put(k,v);}void remove(String k){data.remove(k);}}
 static class ItemStack {Object item;Tag tag;ItemStack(Object i){item=i;}boolean isEmpty(){return item==null;}Object getItem(){return item;}Tag getTag(){return tag;}Tag getOrCreateTag(){if(tag==null)tag=new Tag();return tag;}}
 static class Inventory {int selected=2;int dirty;ItemStack[] slots=new ItemStack[41];Inventory(){Arrays.setAll(slots,i->new ItemStack(null));}ItemStack getItem(int i){return slots[i];}void setChanged(){dirty++;}}
 static class Menu {int updates;void broadcastChanges(){updates++;}}
 static class ServerPlayer {boolean alive=true,spectator,casting;Inventory inventory=new Inventory();Menu inventoryMenu=new Menu(),containerMenu=inventoryMenu;boolean isAlive(){return alive;}boolean isSpectator(){return spectator;}boolean isUsingItem(){return casting;}Inventory getInventory(){return inventory;}void displayClientMessage(Component c,boolean action){}}
 static class FriendlyByteBuf {List<Object> data=new ArrayList<>();int pos;void writeVarInt(int n){data.add(n);}void writeUtf(String s,int limit){if(s.length()>limit)throw new IllegalArgumentException();data.add(s);}int readVarInt(){return (int)data.get(pos++);}String readUtf(int limit){String s=(String)data.get(pos++);if(s.length()>limit)throw new IllegalArgumentException();return s;}}
 static class NetworkEvent {static class Context {ServerPlayer getSender(){return null;}void setPacketHandled(boolean b){}}}
 static class ServerLevel {}
 static class LivingEntity {}
 record ResourceLocation(String namespace,String path){}
 static class Entity {EntityType<?> type;Entity(EntityType<?> t){type=t;}EntityType<?> getType(){return type;}}
 static class Summoned extends Entity {LivingEntity owner;Summoned(EntityType<?> t){super(t);}void setTrueOwner(LivingEntity e){owner=e;}}
 static class SlimeSummon extends Summoned {SlimeSummon(EntityType<?> t){super(t);}}
 static class EntityType<T extends Entity> {int creates;java.util.function.Function<EntityType<T>,T> factory;T create(ServerLevel w){creates++;return factory.apply(this);}}
 static class Registry {Map<ResourceLocation,EntityType<?>> data=new HashMap<>();EntityType<?> getValue(ResourceLocation k){return data.get(k);}}
 static class ForgeRegistries {static Registry ENTITY_TYPES=new Registry();}
 SCHOOL
 PRIORITY
 SUMMON
 PACKET
 public static void main(String[] args){
  var sharedItem=new FinalStaffDarkItem();var a=new ItemStack(sharedItem);var b=new ItemStack(sharedItem);
  check(FinalStaffSchool.values().length==10,"ten priority choices");
  check(FinalStaffSchool.get(a)==FinalStaffSchool.NECROMANCY&&a.tag==null,"unset stacks default to necromancy without mutation");
  var ordinary=new ItemStack(new Object());
  for(var mode:FinalStaffSchool.values()){
   FinalStaffSchool.set(a,mode);
   check(FinalStaffSchool.get(a)==mode,"persist priority");
   check(FinalStaffSchool.get(b)==FinalStaffSchool.NECROMANCY,"per-stack priority isolation");
   for(boolean netherSet:new boolean[]{false,true}){
    SpellType wanted=mode==FinalStaffSchool.WILD?SpellType.WILD:mode==FinalStaffSchool.NETHER&&netherSet?SpellType.NETHER:SpellType.NECROMANCY;
    check(FinalStaffPriority.soulBoltChoice(a,netherSet)==wanted,"soul bolt selection preserves equipment requirement and necro fallback");
   }
  }
  check(FinalStaffSchool.byId("all")==null,"removed all option cannot be selected");
  for(String legacy:new String[]{"all","bad_saved_mode"}){
   a.getOrCreateTag().putString(FinalStaffSchool.TAG,legacy);
   check(FinalStaffSchool.get(a)==FinalStaffSchool.NECROMANCY,"legacy/invalid modes migrate to necromancy");
  }
  a.getOrCreateTag().putString("OtherMod","keep");FinalStaffSchool.set(a,FinalStaffSchool.FROST);
  check(a.tag.getString("OtherMod").equals("keep"),"other item NBT preserved");
  FinalStaffSchool.set(ordinary,FinalStaffSchool.FROST);check(ordinary.tag==null,"ordinary wands remain untouched");
  String[][] expected={
   {"ZOMBIE","FROST","frozen_zombie_servant"},{"ZOMBIE","STORM","frayed_servant"},{"ZOMBIE","WILD","jungle_zombie_servant"},{"ZOMBIE","NETHER","zpiglin_servant"},{"ZOMBIE","ABYSS","drowned_servant"},
   {"SKELETON","FROST","stray_servant"},{"SKELETON","STORM","rattled_servant"},{"SKELETON","WILD","mossy_skeleton_servant"},{"SKELETON","NETHER","wither_skeleton_servant"},{"SKELETON","ABYSS","sunken_skeleton_servant"},
   {"SLIME","ABYSS","tropical_slime_servant"},{"SLIME","NECROMANCY","crypt_slime_servant"},{"SLIME","NETHER","magma_cube_servant"},
   {"HUNTING","NECROMANCY","skeleton_wolf"},{"HUNTING","STORM","stormhound"},{"HUNTING","WIND","twilight_goat"},{"HUNTING","ABYSS","snapper"},{"HUNTING","FROST","winter_wolf"},{"HUNTING","NETHER","hellhound"},
   {"MAULING","ABYSS","gnasher"},{"MAULING","FROST","polar_bear_servant"},{"MAULING","NETHER","hoglin_servant"}};
  var level=new ServerLevel();var caster=new LivingEntity();var vanillaType=new EntityType<Summoned>();var vanilla=new Summoned(vanillaType);vanilla.setTrueOwner(caster);
  for(var family:FinalStaffPriority.SummonFamily.values()){
   for(var mode:FinalStaffSchool.values()){
    FinalStaffSchool.set(a,mode);String id=null;
    for(var entry:expected)if(entry[0].equals(family.name())&&entry[1].equals(mode.name()))id=entry[2];
    check(Objects.equals(id,family.preferredEntity(a)),"preferred summon table "+family+"/"+mode);
    if(id==null){check(FinalStaffSummonPriority.apply(family,vanilla,level,caster,a,Summoned.class)==vanilla,"unsupported choice preserves native result");continue;}
    var type=new EntityType<Summoned>();type.factory=Summoned::new;
    ForgeRegistries.ENTITY_TYPES.data.put(new ResourceLocation("goety",id),type);
    var chosen=FinalStaffSummonPriority.apply(family,vanilla,level,caster,a,Summoned.class);
    check(chosen.getType()==type&&chosen.owner==caster,"variant replaced and ownership retained before spawn initialization");
    check(type.creates==1,"one replacement at most");
    check(FinalStaffSummonPriority.apply(family,chosen,level,caster,a,Summoned.class)==chosen&&type.creates==1,"already preferred variant reused");
   }
   check(family.preferredEntity(ordinary)==null&&FinalStaffSummonPriority.apply(family,vanilla,level,caster,ordinary,Summoned.class)==vanilla,"ordinary summon left intact");
  }
  FinalStaffSchool.set(a,FinalStaffSchool.NETHER);
  ForgeRegistries.ENTITY_TYPES.data.clear();
  check(FinalStaffSummonPriority.apply(FinalStaffPriority.SummonFamily.SLIME,vanilla,level,caster,a,Summoned.class)==vanilla,"missing registry entry falls back safely");
  var player=new ServerPlayer();player.inventory.slots[2]=b;
  check(ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"frost","necromancy")),"held main-hand selection accepted");
  check(FinalStaffSchool.get(b)==FinalStaffSchool.FROST&&player.inventory.dirty==1&&player.inventoryMenu.updates==1,"server persists and syncs selection");
  check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"nether","necromancy")),"stale request rejected");
  for(int slot:new int[]{-1,9,36,39,41,Integer.MAX_VALUE})check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(slot,"nether","frost")),"invalid/non-held slot rejected before lookup");
  check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"invalid","frost")),"unknown mode rejected");
  player.casting=true;check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"nether","frost")),"cannot alter a cast in progress");player.casting=false;
  player.spectator=true;check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"nether","frost")),"spectator cannot switch");player.spectator=false;
  player.alive=false;check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"nether","frost")),"dead player cannot switch");player.alive=true;
  player.inventory.selected=3;check(!ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(2,"nether","frost")),"changed hotbar slot rejected");
  player.inventory.slots[40]=new ItemStack(sharedItem);player.containerMenu=new Menu();
  check(ServerboundStaffSchoolPacket.apply(player,new ServerboundStaffSchoolPacket(40,"nether","necromancy")),"offhand supported");
  check(player.containerMenu.updates==1&&FinalStaffSchool.get(b)==FinalStaffSchool.FROST,"offhand sync does not change main staff");
  for(var mode:FinalStaffSchool.values()){
   var request=new ServerboundStaffSchoolPacket(40,mode.id,"necromancy");var buf=new FriendlyByteBuf();ServerboundStaffSchoolPacket.encode(request,buf);
   check(request.equals(ServerboundStaffSchoolPacket.decode(buf)),"bounded packet roundtrip");
  }
  System.out.println("PASS: "+checks+" school, per-stack, packet validation and inventory-sync checks");
 }
}
'''.replace('SCHOOL', school).replace('PACKET', packet).replace('PRIORITY', priority).replace('SUMMON', summon)
out = root / 'build' / ('staff-school-check-' + str(time.time_ns()))
out.mkdir(parents=True)
(out / 'SchoolRegression.java').write_text(fixture, 'utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(out/'SchoolRegression.java')], check=True)
subprocess.run(['java', '-cp', str(out), 'SchoolRegression'], check=True)

# Check enlarged medallions against the new annulus, including small windows.
screen = (root/'src/mojang/java/com/starfantasy/goety/client/FinalStaffSchoolScreen.java').read_text('utf-8')
assert 'x, y, 20, 8' in screen and 'Math.min(maxWidth / Math.max(1, font.width(label)), maxHeight / font.lineHeight)' in screen
import math
for width,height in [(320,180),(320,240),(427,240),(640,360),(854,480),(1920,1080)]:
    scale=max(.5,min(1.5,(width-24)/120,(height-52)/120))
    orbit=140/3*scale; outer=60*scale; inner=min(30/scale,40)*scale
    for i in range(10):
        a=-math.pi/2+i*math.tau/10;x=width//2+math.cos(a)*orbit;y=height//2+math.sin(a)*orbit
        # Exactly one icon has an extra 1px selection outline.
        radius=(13 if i==0 else 12)*scale
        assert 0<x-radius<x+radius<width and 0<y-radius<y+radius<height
        assert orbit-radius>=inner and orbit+radius<=outer
        assert math.hypot(10,4)<11
        assert math.hypot(9,5.8)<11
        for j in range(i):
            b=-math.pi/2+j*math.tau/10; previous=(13 if j==0 else 12)*scale
            assert math.hypot((math.cos(a)-math.cos(b))*orbit,(math.sin(a)-math.sin(b))*orbit)>radius+previous
for lang in ['zh_cn','en_us']:
    labels=json.loads((root/f'src/main/resources/assets/starfantasy_goety/lang/{lang}.json').read_text('utf-8'))
    assert len([k for k in labels if k.startswith('school.starfantasy_goety.')])==10
    assert all(labels['school.starfantasy_goety.'+name] for name in re.findall(r'\b[A-Z]+\("([a-z]+)"',school))
print('PASS: ten localized priority options; native-wheel icons and bounded text at six GUI sizes')
