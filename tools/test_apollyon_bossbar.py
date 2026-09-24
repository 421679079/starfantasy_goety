"""Run the actual boss-bar event handler against recorded GUI draw calls."""
from pathlib import Path
import re
import subprocess
import sys
import time
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / 'src/main/java/com/starfantasy/goety/client/ApollyonBossBarEvents.java').read_text('utf-8')
renderer = 'static ' + source[source.index('public final class ApollyonBossBarEvents'):]
renderer = re.sub(r'@SubscribeEvent\([^)]*\)\s*', '', renderer)
glow_source = (ROOT / 'src/main/java/com/starfantasy/goety/client/ApollyonBossBarGlow.java').read_text('utf-8')
glow = 'static ' + glow_source[glow_source.index('final class ApollyonBossBarGlow'):]
fixture = r'''
import java.util.*;
import java.util.function.Supplier;
public class BossBarRegression {
 static int checks;
 static void check(boolean b,String reason){checks++;if(!b)throw new AssertionError(reason);}
 record ResourceLocation(String namespace,String path) {}
 static class StarFantasyGoetyMod {static final String MODID="starfantasy_goety";}
 static class ApollyonPageantController {static final int INACTIVE=0;}
 record Component(String text) {}
 static class RandomSource {int m_188503_(int bound){return bound-1;}}
 static class Mob {}
 static class ApollyonEntity extends Mob {
  float health=100,max=100;int f_19797_=17,f_20916_,phase=1,pageant,anti,total=100;
  float m_21223_(){return health;}float m_21233_(){return max;}
  boolean isCombatPhaseTwo(){return phase==2;}int getPageantState(){return pageant;}
  boolean isSmited(){return anti>0;}int getAntiRegen(){return anti;}int getAntiRegenTotal(){return total;}
  RandomSource m_217043_(){return new RandomSource();}Component m_5446_(){return new Component("Little Apollyon");}
 }
 static class BossBarEvent {static Map<UUID,Mob> BOSS_BARS=new HashMap<>();}
 static class Setting {boolean enabled=true;Object get(){return enabled;}}
 static class MainConfig {static Setting SpecialBossBar=new Setting();}
 static class GL11 {
  static final int GL_BLEND=3042,GL_DEPTH_TEST=2929,GL_CULL_FACE=2884,GL_DEPTH_WRITEMASK=2930,GL_SRC_ALPHA=770,GL_ONE=1,GL_ZERO=0;
  static boolean blending,depth=true,cull=true,depthWrite=true;static int srcRgb=770,dstRgb=771,srcAlpha=1,dstAlpha=0;
  static boolean glIsEnabled(int flag){return flag==GL_BLEND?blending:flag==GL_DEPTH_TEST?depth:cull;}
  static boolean glGetBoolean(int flag){return depthWrite;}
  static int glGetInteger(int flag){return flag==GL14.GL_BLEND_SRC_RGB?srcRgb:flag==GL14.GL_BLEND_DST_RGB?dstRgb:flag==GL14.GL_BLEND_SRC_ALPHA?srcAlpha:dstAlpha;}
 }
 static class GL14 {static final int GL_BLEND_SRC_RGB=32969,GL_BLEND_DST_RGB=32968,GL_BLEND_SRC_ALPHA=32971,GL_BLEND_DST_ALPHA=32970;}
 static class RenderSystem {
  static float alpha=1;
  static ShaderInstance shader=new ShaderInstance();
  static void defaultBlendFunc(){blendFuncSeparate(770,771,1,0);}
  static void blendFuncSeparate(int a,int b,int c,int d){GL11.srcRgb=a;GL11.dstRgb=b;GL11.srcAlpha=c;GL11.dstAlpha=d;}
  static void depthMask(boolean value){GL11.depthWrite=value;}
  static void disableDepthTest(){GL11.depth=false;}static void enableDepthTest(){GL11.depth=true;}
  static void disableCull(){GL11.cull=false;}static void enableCull(){GL11.cull=true;}
  static void setShader(Supplier<ShaderInstance> value){shader=value.get();}
  static ShaderInstance getShader(){return shader;}
  static void setShaderColor(float r,float g,float b,float a){alpha=a;}
  static void setShaderTexture(int slot,ResourceLocation texture){}
  static void enableBlend(){GL11.blending=true;}static void disableBlend(){GL11.blending=false;}
 }
 static class ShaderInstance {}
 static class GameRenderer {static ShaderInstance color=new ShaderInstance();static ShaderInstance m_172811_(){return color;}}
 static class Matrix4f {}
 static class Pose {Matrix4f m_252922_(){return new Matrix4f();}}
 static class PoseStack {Pose m_85850_(){return new Pose();}}
 static class VertexFormat {enum Mode {TRIANGLES}}
 static class DefaultVertexFormat {static Object f_85815_=new Object();}
 record Vertex(float x,float y,float z,int r,int g,int b,int a){}
 static class BufferBuilder {
  List<Vertex> vertices=new ArrayList<>();float x,y,z;int r,g,b,a;
  void m_166779_(VertexFormat.Mode mode,Object format){vertices.clear();check(mode==VertexFormat.Mode.TRIANGLES,"rays use triangles");}
  BufferBuilder m_252986_(Matrix4f matrix,float x,float y,float z){this.x=x;this.y=y;this.z=z;return this;}
  BufferBuilder m_6122_(int r,int g,int b,int a){this.r=r;this.g=g;this.b=b;this.a=a;return this;}
  void m_5752_(){vertices.add(new Vertex(x,y,z,r,g,b,a));}
  BufferBuilder m_231175_(){return this;}
 }
 static class Tesselator {
  static Tesselator INSTANCE=new Tesselator();BufferBuilder buffer=new BufferBuilder();
  static Tesselator m_85913_(){return INSTANCE;}BufferBuilder m_85915_(){return buffer;}
 }
 static class BufferUploader {static void m_231202_(BufferBuilder b){check(GL11.blending&&!GL11.depth&&!GL11.depthWrite&&!GL11.cull&&GL11.dstRgb==1,"glow draws additively without depth/cull interference");GuiGraphics.active.rays.addAll(b.vertices);}}
 static class Font {int f_92710_=9;int m_92852_(Component name){return name.text().length()*6;}}
 static class Window {int m_85445_(){return 400;}int m_85446_(){return 300;}}
 static class Minecraft {
  static Minecraft INSTANCE=new Minecraft();Font f_91062_=new Font();
  static Minecraft m_91087_(){return INSTANCE;}Window m_91268_(){return new Window();}
 }
 record Draw(ResourceLocation texture,int x,int y,float u,float v,int w,int h,int tw,int th,float alpha){}
 static class GuiGraphics {
  List<Draw> draws=new ArrayList<>();List<Vertex> rays=new ArrayList<>();Component name;int nameY;
  static GuiGraphics active;
  void m_280262_(){active=this;GL11.blending=false;GL11.depth=true;}
  PoseStack m_280168_(){return new PoseStack();}
  void m_280411_(ResourceLocation t,int x,int y,int w,int h,float u,float v,int sw,int sh,int tw,int th){
   check(sw==w*4&&sh==h*4,"4x art maps exactly onto GUI dimensions");
   check(GL11.blending,"liquid and frame alpha blend in steady phases too");
   draws.add(new Draw(t,x,y,u/4,v/4,w,h,tw/4,th/4,RenderSystem.alpha));
  }
  void m_280163_(ResourceLocation t,int x,int y,float u,float v,int w,int h,int tw,int th){draws.add(new Draw(t,x,y,u,v,w,h,tw,th,RenderSystem.alpha));}
  void m_280430_(Font f,Component n,int x,int y,int color){name=n;nameY=y;check(RenderSystem.alpha==1,"title does not inherit bar transparency");}
 }
 record BossEvent(UUID id){UUID m_18860_(){return id;}}
 static class CustomizeGuiOverlayEvent {
  static class BossEventProgress {
   BossEvent event=new BossEvent(UUID.randomUUID());GuiGraphics graphics=new GuiGraphics();boolean cancelled;int increment,y=20;
   BossEvent getBossEvent(){return event;}GuiGraphics getGuiGraphics(){return graphics;}int getY(){return y;}
   float getPartialTick(){return .5f;}void setCanceled(boolean value){cancelled=value;}void setIncrement(int value){increment=value;}
  }
 }
 GLOW
 RENDERER
 static CustomizeGuiOverlayEvent.BossEventProgress render(Mob mob){
  var event=new CustomizeGuiOverlayEvent.BossEventProgress();BossBarEvent.BOSS_BARS.put(event.event.id(),mob);
  ApollyonBossBarEvents.renderBossBar(event);return event;
 }
 static Draw last(List<Draw> draws){return draws.get(draws.size()-1);}
 public static void main(String[] args){
  var boss=new ApollyonEntity();var event=render(boss);var draws=event.graphics.draws;
  check(event.cancelled&&event.increment==28,"enlarged eye has room below title and before next boss");
  check(event.graphics.name.text().equals("Little Apollyon")&&event.graphics.nameY==11,"boss title preserved");
  check(draws.size()==2&&draws.get(0).w()==182&&draws.get(0).h()==8,"native fill dimensions preserved");
  check(draws.get(0).x()==109&&draws.get(0).y()==24&&last(draws).x()==98,"native fill and centered frame with unclipped side tips");
  check(draws.get(0).texture().namespace().equals("starfantasy_goety")&&last(draws).texture().namespace().equals("starfantasy_goety"),"custom sprites stay in addon namespace");
  check(last(draws).w()==204&&last(draws).h()==24&&last(draws).tw()==204&&last(draws).th()==48,"frame crop UV dimensions");
  for(int phase:new int[]{1,2})for(int pageant=0;pageant<=9;pageant++){
   ApollyonBossBarEvents.PHASE_TRANSITIONS.clear();
   boss.phase=phase;boss.pageant=pageant;draws=render(boss).graphics.draws;boolean second=phase==2;
   check(draws.get(0).v()==(second?8:0)&&last(draws).v()==(second?24:0),"only synced combat phase selects matching fill and frame");
  }
  ApollyonBossBarEvents.PHASE_TRANSITIONS.clear();boss.phase=1;boss.pageant=0;
  for(float[] sample:new float[][]{{100,17,182},{50,35,91},{25,70,45},{1,70,1}}){
   boss.health=sample[0];draws=render(boss).graphics.draws;
   check(draws.get(0).u()==sample[1]&&draws.get(0).w()==(int)sample[2],"native low-health scroll acceleration and clipping");
  }
  boss.health=100;boss.f_19797_=365;check(render(boss).graphics.draws.get(0).u()==1,"native scroll wrap period");
  boss.f_20916_=8;draws=render(boss).graphics.draws;
  check(draws.size()==3&&draws.get(1).texture().path().equals("textures/gui/boss_bar_hurt.png"),"Goety hurt texture retained");
  check(draws.get(1).u()==7&&draws.get(1).v()==7&&draws.get(1).tw()==256,"native randomized hurt sampling retained");
  boss.f_20916_=4;check(render(boss).graphics.draws.size()==2,"hurt overlay stops at native threshold");
  for(int phase:new int[]{1,2})for(int remaining:new int[]{100,50,1}){
   ApollyonBossBarEvents.PHASE_TRANSITIONS.clear();
   boss.phase=phase;boss.anti=remaining;draws=render(boss).graphics.draws;
   check(draws.size()==3&&draws.get(1).texture().namespace().equals("goety")&&draws.get(1).v()==16,"native gold Smite overlay retained in both phases");
   int recovered=(int)((1f-remaining/100f)*182);
   check(draws.get(0).v()==(phase==2?8:0)&&draws.get(1).x()==109+recovered&&draws.get(1).w()==182-recovered,"gold is confined to unrecovered region, never behind transparent recovered liquid");
   check(draws.get(1).u()==draws.get(0).u()+recovered,"Smite scroll remains aligned at moving wipe boundary");
  }
  boss.f_20916_=9;draws=render(boss).graphics.draws;
  check(draws.size()==4&&draws.get(1).texture().path().contains("hurt")&&draws.get(2).v()==16,"hurt and Smite layering order preserved");
  boss.health=0;check(render(boss).graphics.draws.size()==1,"empty health draws frame only");
  var ordinary=render(new Mob());check(!ordinary.cancelled&&ordinary.graphics.draws.isEmpty(),"ordinary apostles and other boss bars untouched");
  var transitioning=new ApollyonEntity();transitioning.f_19797_=0;render(transitioning);
  transitioning.pageant=1;transitioning.f_19797_=2;draws=render(transitioning).graphics.draws;
  check(draws.size()==2&&last(draws).v()==0,"pageant transition alone retains phase one skin");
  transitioning.phase=2;transitioning.f_19797_=10;draws=render(transitioning).graphics.draws;
  check(draws.size()==2&&last(draws).v()==0,"phase change starts with opaque first skin");
  transitioning.f_19797_=12;draws=render(transitioning).graphics.draws;
  check(draws.size()==4&&draws.get(0).alpha()==.8f&&draws.get(2).alpha()==.2f,"two ticks into crossfade");
  check(draws.get(1).v()==0&&last(draws).v()==24,"crossfade renders both frames");
  check(!GL11.blending&&RenderSystem.alpha==1,"disabled blend state and shader color restored");
  GL11.blending=true;transitioning.f_19797_=15;draws=render(transitioning).graphics.draws;
  check(draws.get(0).alpha()==.5f&&draws.get(2).alpha()==.5f,"five ticks into crossfade");
  check(GL11.blending,"preexisting enabled blend state preserved");
  transitioning.f_19797_=19;draws=render(transitioning).graphics.draws;
  check(Math.abs(last(draws).alpha()-.9f)<1e-6,"fade does not restart every render");
  transitioning.f_19797_=20;draws=render(transitioning).graphics.draws;
  check(draws.size()==2&&last(draws).v()==24&&last(draws).alpha()==1,"phase two fully visible after exactly ten ticks");
  transitioning.f_19797_=100;check(render(transitioning).graphics.draws.size()==2,"completed fade uses a single layer");
  var alreadySecond=new ApollyonEntity();alreadySecond.phase=2;draws=render(alreadySecond).graphics.draws;
  check(draws.size()==2&&last(draws).v()==24,"first seen in phase two does not flash the wrong skin");
  var independent=new ApollyonEntity();draws=render(independent).graphics.draws;
  check(draws.size()==2&&last(draws).v()==0,"different bosses have independent transition state");
  transitioning.phase=1;render(transitioning);transitioning.f_19797_=105;draws=render(transitioning).graphics.draws;
  check(last(draws).alpha()==.5f,"phase reset reverses the fade");
  transitioning.phase=2;draws=render(transitioning).graphics.draws;
  check(last(draws).alpha()==.5f,"mid-fade phase reversal is continuous");
  transitioning.f_19797_=110;draws=render(transitioning).graphics.draws;
  check(last(draws).alpha()==.75f,"reversal continues from visible mix");
  var gleaming=new ApollyonEntity();gleaming.phase=2;gleaming.f_19797_=30;
  ShaderInstance oldShader=RenderSystem.getShader();GL11.blending=false;
  var glowEvent=render(gleaming);var rays=glowEvent.graphics.rays;
  check(rays.size()==54,"exactly nine rays / 54 vertices per phase-two bar");
  check(!GL11.blending&&RenderSystem.getShader()==oldShader&&RenderSystem.alpha==1,"glow restores shader, color and disabled blend state");
  for(int i=0;i<rays.size();i+=6){
   Vertex center=rays.get(i),left=rays.get(i+1),right=rays.get(i+2);
   int anchor=i/18;
   check(center.x()==(anchor==0?200:anchor==1?110:290)&&center.y()==(anchor==0?28f:28.5f),"glow remains anchored to eye/gem centers");
   check(center.a()>0&&center.a()<=220&&left.a()==center.a()/2&&right.a()==0,"bright glow fades to transparent tips");
   double length=Math.hypot(right.x()-center.x(),right.y()-center.y());
   check(length>(anchor==0?20:11)&&length<=(anchor==0?21.01:11.71),"eye rays double and gem rays grow 1.5x around unchanged centers");
   check(center.r()==(anchor==0?255:190)&&center.g()==(anchor==0?35:55)&&center.b()==(anchor==0?50:255),"red eye rays and purple gem rays");
   check((left.x()-center.x())*(right.y()-center.y())-(left.y()-center.y())*(right.x()-center.x())<0,"GUI ray winding remains front-facing");
  }
  gleaming.f_19797_=50;var rotated=render(gleaming).graphics.rays;
  check(rotated.get(1).x()!=rays.get(1).x()&&rotated.get(0).x()==rays.get(0).x(),"rays rotate without moving emitter");
  for(boolean depth:new boolean[]{false,true})for(boolean cull:new boolean[]{false,true})for(boolean blend:new boolean[]{false,true}){
   GL11.depth=depth;GL11.cull=cull;GL11.blending=blend;GL11.depthWrite=!depth;
   RenderSystem.blendFuncSeparate(1,0,770,771);var gui=new GuiGraphics();
   ApollyonBossBarGlow.render(gui,100,20,30,1);
   check(GL11.depth==depth&&GL11.cull==cull&&GL11.blending==blend&&GL11.depthWrite==!depth,"restore caller depth/cull/blend/depth-mask, including flush mutations");
   check(GL11.srcRgb==1&&GL11.dstRgb==0&&GL11.srcAlpha==770&&GL11.dstAlpha==771,"restore both color and alpha blend factors");
  }
  var dormant=new ApollyonEntity();check(render(dormant).graphics.rays.isEmpty(),"phase one has no rays");
  dormant.phase=2;render(dormant);dormant.f_19797_+=5;var fading=render(dormant).graphics.rays;
  check(fading.size()==54&&fading.get(0).a()<=110,"rays follow the half-second phase fade");
  dormant.health=150;draws=render(dormant).graphics.draws;check(draws.get(0).w()==182,"overheal cannot extend past the aperture");
  dormant.health=-1;draws=render(dormant).graphics.draws;check(draws.stream().noneMatch(d->d.texture().path().contains("fill")),"negative health cannot draw inverted fill");
  MainConfig.SpecialBossBar.enabled=false;event=render(boss);check(!event.cancelled&&event.graphics.draws.isEmpty(),"native special-bar config respected");
  if(args.length>0){
   try(var out=new java.io.PrintWriter(args[0],java.nio.charset.StandardCharsets.UTF_8)){
    MainConfig.SpecialBossBar.enabled=true;
    for(int tick=0;tick<=180;tick+=3){
     gleaming.f_19797_=tick;var frame=render(gleaming);
     for(Vertex v:frame.graphics.rays)out.println(tick+","+v.x()+","+v.y()+","+v.z()+","+v.r()+","+v.g()+","+v.b()+","+v.a());
    }
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  System.out.println("PASS: "+checks+" rendering, phase, effect and entity-scope checks.");
 }
}
'''.replace('RENDERER', renderer).replace('GLOW', glow)
out = ROOT / 'build' / ('bossbar-check-' + str(time.time_ns()))
out.mkdir(parents=True)
path = out / 'BossBarRegression.java'
path.write_text(fixture, 'utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(out), str(path)], check=True)
subprocess.run(['java', '-cp', str(out), 'BossBarRegression', *sys.argv[1:]], check=True)

textures = ROOT / 'src/main/resources/assets/starfantasy_goety/textures/gui'
frames = Image.open(textures / 'apollyon_boss_bar.png').convert('RGBA')
fills = Image.open(textures / 'apollyon_boss_bar_fill.png').convert('RGBA')
assert frames.size == (816, 192) and fills.size == (1456, 64)
assert fills.crop((0, 0, 728, 64)).tobytes() == fills.crop((728, 0, 1456, 64)).tobytes()
for phase in range(2):
    # Channel interior and exterior are truly empty, not a gray checkerboard.
    assert frames.crop((153, phase*96+38, 278, phase*96+54)).getchannel('A').getextrema() == (0, 0)
    assert frames.crop((523, phase*96+38, 673, phase*96+54)).getchannel('A').getextrema() == (0, 0)
    assert frames.getpixel((200, phase*96))[3] == 0
    # Inspect a straight rail away from the ornaments: added gold must grow
    # inward, never beyond the original thin rails' outer bounds.
    assert frames.crop((195, phase*96, 205, phase*96+26)).getchannel('A').getextrema() == (0, 0)
    assert frames.crop((195, phase*96+70, 205, phase*96+96)).getchannel('A').getextrema() == (0, 0)
    strip = fills.crop((0, phase*32, 728, phase*32+32))
    lo, hi = strip.getchannel('A').getextrema()
    assert 150 <= lo < 210 and 215 < hi < 255, (lo, hi)
    assert strip.crop((0, 0, 1, 32)).tobytes() == strip.crop((727, 0, 728, 32)).tobytes()
    bright = sum(1 for r,g,b,a in strip.get_flattened_data() if min(r,g,b)>205 and a>215)
    assert 5 < bright < 1000, bright
assert frames.crop((368, 24, 448, 72)).tobytes() != frames.crop((368, 120, 448, 168)).tobytes()
print('PASS: compressed atlases, true-alpha aperture, enlarged phase eyes, semi-transparent liquid, sparse stars and seamless repeated scroll.')
