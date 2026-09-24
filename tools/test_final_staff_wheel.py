"""Headless checks of the real screen lifecycle; native drawing itself needs in-game QA."""
from pathlib import Path
import re
import subprocess
import time
root = Path(__file__).resolve().parents[1]
source = (root / 'src/mojang/java/com/starfantasy/goety/client/FinalStaffSchoolScreen.java').read_text('utf-8')
source = re.sub(r'^(?:package|import) .*;\s*$', '', source, flags=re.M)
source = source.replace('public final class FinalStaffSchoolScreen', 'public static final class FinalStaffSchoolScreen')
fixture = r"""
import java.util.*;
public class WheelRegression {
 static int checks;
 static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
 record Component(String value){String getString(){return value;}static Component literal(String s){return new Component(s);}static Component translatable(String key,Object... args){return new Component(key);}}
 record ResourceLocation(String namespace,String path){}
 static class Font {List<Component> split(Component c,int width){return List.of(c);}int lineHeight=9;int width(Component c){return c.value.length()*6;}}
 static class Pose {void pushPose(){}void popPose(){}void translate(float x,float y,int z){}void scale(float x,float y,int z){}}
 static class GuiGraphics {void drawCenteredString(Font f,Component c,int x,int y,int color){}void renderTooltip(Font f,Object o,int x,int y){}void flush(){}Pose pose(){return new Pose();}void drawString(Font f,Component c,int x,int y,int color,boolean shadow){}void fill(int a,int b,int c,int d,int color){}}
 static class RenderSystem {static Pose getModelViewStack(){return new Pose();}static void applyModelViewMatrix(){}static void setShaderColor(int a,int b,int c,int d){}}
 static class ItemStack {final int id;ItemStack(int id){this.id=id;}ItemStack copy(){return new ItemStack(id);}static boolean matches(ItemStack a,ItemStack b){return a.id==b.id;}}
 static class Inventory {int selected=2;ItemStack stack=new ItemStack(1);ItemStack getItem(int slot){return stack;}}
 static class Player {boolean alive=true,spectator,using;Inventory inventory=new Inventory();boolean isAlive(){return alive;}boolean isSpectator(){return spectator;}boolean isUsingItem(){return using;}Inventory getInventory(){return inventory;}}
 static class Window {long getWindow(){return 1;}int getScreenWidth(){return 640;}int getScreenHeight(){return 480;}}
 static class Minecraft {Window getWindow(){return new Window();}Object level=new Object();boolean active=true;Player player=new Player();boolean isWindowActive(){return active;}}
 static class Screen {Minecraft minecraft=new Minecraft();Font font=new Font();int width=320,height=240;boolean closed;Screen(Component title){}protected void init(){}void tick(){}boolean isPauseScreen(){return true;}void onClose(){closed=true;}boolean mouseClicked(double x,double y,int b){return false;}boolean mouseReleased(double x,double y,int b){return false;}boolean keyPressed(int k,int s,int m){if(k==256)onClose();return false;}static boolean hasShiftDown(){return false;}void render(GuiGraphics g,int x,int y,float f){}}
 interface IRadialMenuHost {Screen getScreen();Font getFontRenderer();void renderTooltip(GuiGraphics g,ItemStack s,int x,int y);}
 record DrawingContext(GuiGraphics guiGraphics,float x,float y){}
 static class GenericRadialMenu {float radiusIn=30,radiusOut=60,itemRadius=45;List<RadialMenuItem> items=new ArrayList<>();boolean ready=true,closed;int hovered=-1,nextHover=-1,mouseX,mouseY;GenericRadialMenu(Minecraft m,ResourceLocation r,IRadialMenuHost h){}void setCentralItem(ItemStack s){}void add(RadialMenuItem i){items.add(i);}void tick(){}boolean isClosed(){return closed;}boolean isReady(){return ready&&!closed;}void close(){closed=true;}void onClickOutside(){}void clickItem(){if(isReady()&&hovered>=0)items.get(hovered).onClick();else onClickOutside();}void cyclePrevious(){}void cycleNext(){}void draw(GuiGraphics g,float f,int x,int y){hovered=nextHover;mouseX=x;mouseY=y;}}
 abstract static class RadialMenuItem {boolean visible;RadialMenuItem(GenericRadialMenu m){}void setVisible(boolean v){visible=v;}boolean isHovered(){return false;}abstract void draw(DrawingContext c);abstract void drawTooltips(DrawingContext c);boolean onClick(){return false;}}
 enum FinalStaffSchool {ILL,NECROMANCY,GEOMANCY,WIND,STORM,FROST,WILD,ABYSS,VOID,NETHER;final String id=name();int color=0xFF00FF;static FinalStaffSchool get(ItemStack s){return NECROMANCY;}Component label(){return new Component(id);}}
 record ServerboundStaffSchoolPacket(int slot,String school,String previousSchool){}
 static class StarFantasyGoetyNetwork {static List<ServerboundStaffSchoolPacket> sent=new ArrayList<>();static void selectStaffSchool(ServerboundStaffSchoolPacket p){sent.add(p);}}
 enum KeyConflictContext {UNIVERSAL}
 static class Modifier {boolean held=true;boolean isActive(KeyConflictContext c){return held;}}
 static class Key {Modifier modifier=new Modifier();Modifier getKeyModifier(){return modifier;}}
 static class FinalStaffSchoolKeys {static Key SWITCH=new Key();}
 static class ClientEvents {static boolean held=true;static boolean isKeyDown0(Key k){return held;}}
 static class GLFW {static void glfwGetCursorPos(long w,double[] x,double[] y){}static void glfwSetCursorPos(long w,double x,double y){}static final int GLFW_KEY_ENTER=257,GLFW_KEY_KP_ENTER=335,GLFW_KEY_RIGHT=262,GLFW_KEY_DOWN=264,GLFW_KEY_TAB=258,GLFW_KEY_LEFT=263,GLFW_KEY_UP=265;}
 SCREEN
 static FinalStaffSchoolScreen open(){ClientEvents.held=true;FinalStaffSchoolKeys.SWITCH.modifier.held=true;StarFantasyGoetyNetwork.sent.clear();var s=new FinalStaffSchoolScreen(2,new ItemStack(1));s.init();return s;}
 static void frame(FinalStaffSchoolScreen s){s.render(new GuiGraphics(),0,0,0);}
 public static void main(String[] args){
  var s=open();check(s.menu.items.size()==10,"ten native entries");check(s.menu.items.stream().allMatch(i->i.visible),"native entries must explicitly be visible");
  s.menu.nextHover=5;frame(s);check(StarFantasyGoetyNetwork.sent.isEmpty(),"hover alone must not spam updates");
  ClientEvents.held=false;frame(s);check(StarFantasyGoetyNetwork.sent.size()==1,"release commits without a mouse click");check(StarFantasyGoetyNetwork.sent.get(0).school().equals("FROST"),"latest hovered sector committed");frame(s);check(StarFantasyGoetyNetwork.sent.size()==1,"closing animation cannot send twice");
  s=open();s.menu.nextHover=5;frame(s);s.menu.nextHover=-1;ClientEvents.held=false;frame(s);check(StarFantasyGoetyNetwork.sent.isEmpty()&&s.finished,"returning to centre cancels previous hover");
  s=open();s.menu.nextHover=1;ClientEvents.held=false;frame(s);check(StarFantasyGoetyNetwork.sent.isEmpty(),"unchanged selection sends nothing");
  s=open();s.menu.nextHover=9;s.keyPressed(256,0,0);check(s.closed&&StarFantasyGoetyNetwork.sent.isEmpty(),"Escape cancels");
  s=open();s.menu.nextHover=9;s.mouseClicked(0,0,1);check(s.closed&&StarFantasyGoetyNetwork.sent.isEmpty(),"right click cancels");
  s=open();s.menu.nextHover=9;s.minecraft.player.inventory.stack=new ItemStack(2);ClientEvents.held=false;frame(s);check(StarFantasyGoetyNetwork.sent.isEmpty(),"replaced stack never receives selection");
  s=open();s.minecraft.active=false;s.tick();check(s.closed&&StarFantasyGoetyNetwork.sent.isEmpty(),"alt-tab cancels safely");
  s=open();s.minecraft.player.using=true;s.tick();check(s.closed,"casting closes wheel");
  s=open();s.menu.ready=false;ClientEvents.held=false;frame(s);check(!s.finished,"release during opening waits for native animation");s.menu.ready=true;s.menu.nextHover=0;frame(s);check(StarFantasyGoetyNetwork.sent.size()==1,"early release resolves after opening");
  s=open();s.menu.nextHover=2;FinalStaffSchoolKeys.SWITCH.modifier.held=false;frame(s);check(StarFantasyGoetyNetwork.sent.size()==1,"modifier release also confirms");
  s=open();s.menu.nextHover=3;frame(s);s.mouseReleased(0,0,0);check(StarFantasyGoetyNetwork.sent.size()==1,"native left mouse release confirms");
  s=open();s.width=640;s.height=360;check(s.wheelScale()==1.5F,"normal window scales wheel to 1.5");s.render(new GuiGraphics(),390,180,0);check(s.menu.mouseX==367&&s.menu.mouseY==180,"enlarged icon centre maps to native sector centre");
  s.width=320;s.height=180;check(s.wheelScale()<2&&120*s.wheelScale()+52<=180.001,"small window keeps wheel and labels visible");
  s=open();s.width=640;s.height=360;s.configureWheelRadii(s.menu,true);
  check(s.menu.radiusIn*s.wheelScale()==30,"centre dead zone remains original size");
  check(s.menu.radiusOut*s.wheelScale()==90,"outer radius is 1.5 times original");
  check(Math.abs(s.menu.itemRadius*s.wheelScale()-70)<.001,"larger icons placed on 70px orbit");
  for(float progress:new float[]{.1F,.5F,1}){s.menu.radiusOut=60*progress;s.configureWheelRadii(s.menu,false);check(Math.abs(s.menu.radiusIn*s.wheelScale()-30*progress)<.001,"inner radius follows native animation");}
  s.menu.radiusOut=60;s.configureWheelRadii(s.menu,false);float drawn=s.menu.radiusIn;s.configureWheelRadii(s.menu,true);check(s.menu.radiusIn==drawn,"render and hover dead zones match");
  for(float radius:new float[]{29,30,31,45,70,89,90}){float nativeRadius=radius/s.wheelScale();check((nativeRadius>=s.menu.radiusIn&&nativeRadius<s.menu.radiusOut)==(radius>=30&&radius<90),"annular hit boundary "+radius);}
  System.out.println("PASS: "+checks+" real-screen interaction/lifecycle checks (headless API fixtures)");
 }
}
""".replace('SCREEN',source)
out=root/'build'/('staff-wheel-check-'+str(time.time_ns()))
out.mkdir(parents=True)
(out/'WheelRegression.java').write_text(fixture,'utf-8')
subprocess.run(['javac','--release','17','-encoding','UTF-8','-d',str(out),str(out/'WheelRegression.java')],check=True)
subprocess.run(['java','-cp',str(out),'WheelRegression'],check=True)
