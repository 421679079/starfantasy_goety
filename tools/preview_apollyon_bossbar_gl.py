"""Render production ray vertices and packed textures using a hidden OpenGL window.

This is a GPU asset/rendering check, not a Minecraft gameplay screenshot. Run
test_apollyon_bossbar.py <output>/rays.csv first to export the actual Java rays.
"""
from pathlib import Path
import argparse
import subprocess
from PIL import Image

parser = argparse.ArgumentParser()
parser.add_argument('output', type=Path)
parser.add_argument('--libraries', type=Path, default=Path('D:/MC/.minecraft/libraries'))
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
output = args.output.resolve()
output.mkdir(parents=True, exist_ok=True)
build = output / 'gl-check'
build.mkdir(exist_ok=True)
jars = []
for artifact in ('lwjgl', 'lwjgl-glfw', 'lwjgl-opengl'):
    for suffix in ('', '-natives-windows'):
        jar = args.libraries / 'org/lwjgl' / artifact / '3.3.1' / f'{artifact}-3.3.1{suffix}.jar'
        assert jar.exists(), jar
        jars.append(str(jar))

java = r'''
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import static org.lwjgl.opengl.GL33C.*;
import java.nio.*;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.*;

public class BossBarGpuCheck {
 static final int W=960,H=384;
 static int program,vao,vbo,frameTex,fillTex,titleTex;
 static Map<Integer,ArrayList<float[]>> rays=new TreeMap<>();
 static int shader(int type,String text){int s=glCreateShader(type);glShaderSource(s,text);glCompileShader(s);
  if(glGetShaderi(s,GL_COMPILE_STATUS)==0)throw new AssertionError(glGetShaderInfoLog(s));return s;}
 static int texture(BufferedImage im){
  int t=glGenTextures();glBindTexture(GL_TEXTURE_2D,t);ByteBuffer pixels=BufferUtils.createByteBuffer(im.getWidth()*im.getHeight()*4);
  for(int y=0;y<im.getHeight();y++)for(int x=0;x<im.getWidth();x++){int c=im.getRGB(x,y);pixels.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}
  pixels.flip();glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,im.getWidth(),im.getHeight(),0,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
  glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
  glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);return t;
 }
 static void draw(float[] values,int texture){
  glUseProgram(program);glUniform1i(glGetUniformLocation(program,"textured"),texture==0?0:1);
  glBindTexture(GL_TEXTURE_2D,texture);glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
  FloatBuffer data=BufferUtils.createFloatBuffer(values.length);data.put(values).flip();glBufferData(GL_ARRAY_BUFFER,data,GL_STREAM_DRAW);
  glDrawArrays(GL_TRIANGLES,0,values.length/8);
 }
 static void quad(int texture,float x,float y,float width,float height,float u,float v,float uw,float vh){
  draw(new float[]{x,y,1,1,1,1,u,v, x,y+height,1,1,1,1,u,v+vh, x+width,y+height,1,1,1,1,u+uw,v+vh,
   x,y,1,1,1,1,u,v, x+width,y+height,1,1,1,1,u+uw,v+vh, x+width,y,1,1,1,1,u+uw,v},texture);
 }
 static void scene(int tick,boolean glow){
  glViewport(0,0,W,H);glClearColor(.075f,.31f,.32f,1);glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
  glEnable(GL_BLEND);glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA,GL_ONE,GL_ZERO);glDisable(GL_DEPTH_TEST);
  for(int phase=0;phase<2;phase++){
   float x=20,y=20+phase*38;
   float fill=phase==0?182:136;
   quad(fillTex,x+9,y+4,fill,8,(tick%364)/364f,phase*.5f,fill/364f,.5f);
   quad(frameTex,x-2,y-4,204,24,0,phase*.5f,1,.5f);
  }
  if(glow){
   // Match the production GUI overlay state; even stale depth/cull cannot hide it.
   glEnable(GL_DEPTH_TEST);glDepthFunc(GL_NEVER);glEnable(GL_CULL_FACE);
   glDisable(GL_DEPTH_TEST);glDepthMask(false);glDisable(GL_CULL_FACE);
   glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE,GL_ZERO,GL_ONE);
   var points=rays.get(tick);float[] vertices=new float[points.size()*8];int i=0;
   for(float[] p:points){vertices[i++]=p[0]-100+20;vertices[i++]=p[1]-20+58;
    vertices[i++]=p[3]/255;vertices[i++]=p[4]/255;vertices[i++]=p[5]/255;vertices[i++]=p[6]/255;
    vertices[i++]=0;vertices[i++]=0;}
   draw(vertices,0);glDepthMask(true);glDepthFunc(GL_LEQUAL);glBlendFuncSeparate(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA,GL_ONE,GL_ZERO);
  }
  // Like the production handler, draw the name after the glow overlay.
  for(int phase=0;phase<2;phase++)quad(titleTex,106.5f,11+phase*38,27,9,0,0,1,1);
  glFinish();if(glGetError()!=GL_NO_ERROR)throw new AssertionError("OpenGL error");
 }
 static BufferedImage capture(){
  ByteBuffer data=BufferUtils.createByteBuffer(W*H*4);glReadPixels(0,0,W,H,GL_RGBA,GL_UNSIGNED_BYTE,data);
  BufferedImage im=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
  for(int y=0;y<H;y++)for(int x=0;x<W;x++){int i=((H-1-y)*W+x)*4;
   im.setRGB(x,y,0xff000000|((data.get(i)&255)<<16)|((data.get(i+1)&255)<<8)|(data.get(i+2)&255));}return im;
 }
 public static void main(String[] args)throws Exception{
  Path out=Path.of(args[0]),textures=Path.of(args[1]);
  for(String line:Files.readAllLines(out.resolve("rays.csv"))){String[] c=line.split(",");int tick=Integer.parseInt(c[0]);float[] p=new float[7];
   for(int k=0;k<7;k++)p[k]=Float.parseFloat(c[k+1]);rays.computeIfAbsent(tick,ignored->new ArrayList<>()).add(p);}
  if(!GLFW.glfwInit())throw new AssertionError("GLFW init failed");
  GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR,3);GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR,3);
  GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE,GLFW.GLFW_OPENGL_CORE_PROFILE);
  long window=GLFW.glfwCreateWindow(W,H,"Bossbar GPU check",0,0);if(window==0)throw new AssertionError("Hidden window failed");
  try{
   GLFW.glfwMakeContextCurrent(window);GL.createCapabilities();
   System.out.println("GPU: "+glGetString(GL_VENDOR)+" / "+glGetString(GL_RENDERER));
   String vs="#version 150\nin vec2 p;in vec4 color;in vec2 uv;out vec4 c;out vec2 tex;void main(){gl_Position=vec4(p.x/120.0-1.0,1.0-p.y/48.0,0.0,1.0);c=color;tex=uv;}";
   String fs="#version 150\nuniform sampler2D image;uniform int textured;in vec4 c;in vec2 tex;out vec4 outColor;void main(){outColor=c*(textured==1?texture(image,tex):vec4(1));}";
   program=glCreateProgram();glAttachShader(program,shader(GL_VERTEX_SHADER,vs));glAttachShader(program,shader(GL_FRAGMENT_SHADER,fs));
   glBindAttribLocation(program,0,"p");glBindAttribLocation(program,1,"color");glBindAttribLocation(program,2,"uv");glLinkProgram(program);
   if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new AssertionError(glGetProgramInfoLog(program));
   vao=glGenVertexArrays();vbo=glGenBuffers();glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
   glVertexAttribPointer(0,2,GL_FLOAT,false,32,0);glVertexAttribPointer(1,4,GL_FLOAT,false,32,8);glVertexAttribPointer(2,2,GL_FLOAT,false,32,24);
   glEnableVertexAttribArray(0);glEnableVertexAttribArray(1);glEnableVertexAttribArray(2);
   frameTex=texture(ImageIO.read(textures.resolve("apollyon_boss_bar.png").toFile()));fillTex=texture(ImageIO.read(textures.resolve("apollyon_boss_bar_fill.png").toFile()));
   BufferedImage title=new BufferedImage(108,36,BufferedImage.TYPE_INT_ARGB);Graphics2D g=title.createGraphics();g.setFont(new Font("Microsoft YaHei",Font.BOLD,32));
   g.setColor(Color.BLACK);g.drawString("亚小妹",3,32);g.setColor(Color.WHITE);g.drawString("亚小妹",1,30);g.dispose();titleTex=texture(title);
   Files.createDirectories(out.resolve("gpu-frames"));scene(0,false);BufferedImage baseline=capture();ImageIO.write(baseline,"PNG",out.resolve("gpu-no-glow.png").toFile());
   BufferedImage first=null;int changed=0,peak=0;
   for(int tick:rays.keySet()){scene(tick,true);BufferedImage im=capture();
    if(first==null){first=im;for(int y=0;y<H;y++)for(int x=0;x<W;x++){
     int a=im.getRGB(x,y),b=baseline.getRGB(x,y),d=0;for(int shift:new int[]{0,8,16})d=Math.max(d,((a>>shift)&255)-((b>>shift)&255));
     if(d>4)changed++;peak=Math.max(peak,d);}}
    ImageIO.write(im,"PNG",out.resolve("gpu-frames/frame_"+String.format("%03d",tick)+".png").toFile());}
   if(changed<100||peak<25)throw new AssertionError("Glow not sufficiently visible: "+changed+", "+peak);
   System.out.println("PASS: real GPU glow changed "+changed+" pixels, peak channel brightening "+peak+"/255; "+rays.size()+" rotation frames captured.");
  }finally{GLFW.glfwDestroyWindow(window);GLFW.glfwTerminate();}
 }
}
'''
source = build / 'BossBarGpuCheck.java'
source.write_text(java, 'utf-8')
classpath = ';'.join(jars)
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-cp', classpath,
                '-d', str(build), str(source)], check=True)
subprocess.run(['java', '-Djava.awt.headless=true', '-cp', str(build)+';'+classpath,
                'BossBarGpuCheck', str(output), str(root/'src/main/resources/assets/starfantasy_goety/textures/gui')], check=True)
frames = [Image.open(path).convert('RGB') for path in sorted((output/'gpu-frames').glob('frame_*.png'))]
frames[0].save(output/'gpu-preview.gif', save_all=True, append_images=frames[1:], duration=150, loop=0)
print('GPU preview:', output/'gpu-preview.gif')
