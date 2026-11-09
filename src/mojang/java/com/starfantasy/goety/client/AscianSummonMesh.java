package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.renderer.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Shared-workspace Creation transfer clipping, specialized for the Ascian materialization renderer. */
public final class AscianSummonMesh {
    public static float reveal(float age){float t=Math.max(0,Math.min(1,(age-8)/46F));return t*t*(3-2*t);}
    private static final int STRIDE=16,MAX_VERTICES=65536;
    private final Map<RenderType,Batch> batches=new LinkedHashMap<>();
    private int vertices;private boolean overflow;
    private float minY,maxY;
    private final float[][] polygon=new float[8][STRIDE],clipped=new float[8][STRIDE];
    private static final class Batch {float[] data=new float[2048];int size;}
    public int vertices(){return vertices;}
    public float bottom(){return Float.isFinite(minY)?minY:0;}
    public float top(){return Float.isFinite(maxY)&&maxY>bottom()+.1F?maxY:bottom()+1.8F;}
    public boolean usable(){return !overflow&&vertices>0;}
    public void reset(){vertices=0;overflow=false;minY=Float.POSITIVE_INFINITY;maxY=Float.NEGATIVE_INFINITY;batches.values().forEach(b->b.size=0);}
    public MultiBufferSource capture(PoseStack root,MultiBufferSource original,boolean forward){
        reset();var inverse=new Matrix4f(root.last().pose()).invert();var normal=new Matrix3f(root.last().normal()).invert();
        return type->{
            // Text/nameplates do not define model bounds and must not become part of a frozen body.
            boolean model=type.mode()==VertexFormat.Mode.QUADS&&type.format().getElements().contains(DefaultVertexFormat.ELEMENT_NORMAL);
            return new Recorder(model?batches.computeIfAbsent(type,t->new Batch()):null,inverse,normal,forward?original.getBuffer(type):null);
        };
    }
    private final class Recorder implements VertexConsumer {
        final Batch batch;final Matrix4f inverse;final Matrix3f inverseNormal;final VertexConsumer delegate;
        final float[] v=new float[STRIDE];final Vector3f point=new Vector3f(),normal=new Vector3f();
        Recorder(Batch b,Matrix4f i,Matrix3f n,VertexConsumer d){batch=b;inverse=i;inverseNormal=n;delegate=d;v[3]=v[4]=v[5]=v[6]=255;v[12]=240;v[11]=240;v[14]=1;}
        public VertexConsumer vertex(double x,double y,double z){v[0]=(float)x;v[1]=(float)y;v[2]=(float)z;return this;}
        public VertexConsumer color(int r,int g,int b,int a){v[3]=r;v[4]=g;v[5]=b;v[6]=a;return this;}
        public VertexConsumer uv(float u,float vv){v[7]=u;v[8]=vv;return this;}
        public VertexConsumer overlayCoords(int u,int vv){v[9]=u;v[10]=vv;return this;}
        public VertexConsumer uv2(int u,int vv){v[11]=u;v[12]=vv;return this;}
        public VertexConsumer normal(float x,float y,float z){v[13]=x;v[14]=y;v[15]=z;return this;}
        public void defaultColor(int r,int g,int b,int a){color(r,g,b,a);}
        public void unsetDefaultColor(){}
        public void endVertex(){
            if(delegate!=null)emit(delegate,v,null,1);
            if(batch==null||overflow)return;
            if(vertices>=MAX_VERTICES){overflow=true;return;}
            if(batch.size+STRIDE>batch.data.length)batch.data=Arrays.copyOf(batch.data,batch.data.length*2);
            int offset=batch.size;System.arraycopy(v,0,batch.data,offset,STRIDE);
            var p=inverse.transformPosition(point.set(v[0],v[1],v[2]));var n=inverseNormal.transform(normal.set(v[13],v[14],v[15]));
            batch.data[offset]=p.x;batch.data[offset+1]=p.y;batch.data[offset+2]=p.z;
            batch.data[offset+13]=n.x;batch.data[offset+14]=n.y;batch.data[offset+15]=n.z;
            minY=Math.min(minY,p.y);maxY=Math.max(maxY,p.y);batch.size+=STRIDE;vertices++;
        }
    }
    /** Geometry clipping retains the original material/shader, including Oculus entity materials. */
    public void render(PoseStack pose,MultiBufferSource buffers,float cut,boolean above,float alpha){
        for(var entry:batches.entrySet()){
            var b=entry.getValue();if(b.size==0)continue;var out=buffers.getBuffer(entry.getKey());
            for(int offset=0;offset+STRIDE*4<=b.size;offset+=STRIDE*4){
                for(int j=0;j<4;j++)System.arraycopy(b.data,offset+j*STRIDE,polygon[j],0,STRIDE);
                int count=0;
                for(int j=0;j<4;j++){
                    var a=polygon[j];var z=polygon[(j+1)%4];boolean inside=above?a[1]>=cut:a[1]<=cut,next=above?z[1]>=cut:z[1]<=cut;
                    if(inside)System.arraycopy(a,0,clipped[count++],0,STRIDE);
                    if(inside!=next){float k=(cut-a[1])/(z[1]-a[1]);for(int n=0;n<STRIDE;n++)clipped[count][n]=a[n]+(z[n]-a[n])*k;count++;}
                }
                if(count==4){for(int j=0;j<4;j++)emit(out,clipped[j],pose,alpha);}
                else for(int j=1;j<count-1;j++){emit(out,clipped[0],pose,alpha);emit(out,clipped[j],pose,alpha);emit(out,clipped[j+1],pose,alpha);emit(out,clipped[j+1],pose,alpha);}
            }
        }
    }
    private static void emit(VertexConsumer out,float[] v,PoseStack pose,float alpha){
        float r=v[3]/255F,g=v[4]/255F,b=v[5]/255F,a=v[6]/255F*alpha;
        if(pose==null)out.vertex(v[0],v[1],v[2]);else out.vertex(pose.last().pose(),v[0],v[1],v[2]);
        out.color(r,g,b,a).uv(v[7],v[8]).overlayCoords(Math.round(v[9]),Math.round(v[10])).uv2(Math.round(v[11]),Math.round(v[12]));
        if(pose==null)out.normal(v[13],v[14],v[15]);else out.normal(pose.last().normal(),v[13],v[14],v[15]);out.endVertex();
    }
}
