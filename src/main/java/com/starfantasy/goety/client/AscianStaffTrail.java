package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import java.util.*;

/** Sample the actual animated staff-head endpoints; camera motion never becomes ribbon motion. */
public final class AscianStaffTrail extends GeoRenderLayer<AscianKingEntity> {
    private static final ResourceLocation FLOW=new ResourceLocation("starfantasy_goety","textures/effect/ascian/staff_flow.png");
    private static final int[] COLORS={0xff811c,0x8bddff,0xffffff,0xb271ff,0x62666d,0xff263d};
    private final Map<AscianKingEntity,History> histories=new WeakHashMap<>();
    private PoseStack base;
    private Matrix4f inverse;
    private Vec3 origin;
    private record Sample(float time,Vec3 a,Vec3 b){}
    private static final class History {int serial=-1;Vec3 owner;final List<Sample> points=new ArrayList<>();}
    public AscianStaffTrail(GeoRenderer<AscianKingEntity> renderer){super(renderer);}
    public void begin(AscianKingEntity e,PoseStack pose,float partial){
        base=new PoseStack();base.m_85850_().m_252922_().set(pose.m_85850_().m_252922_());base.m_85850_().m_252943_().set(pose.m_85850_().m_252943_());
        inverse=new Matrix4f(base.m_85850_().m_252922_()).invert();origin=e.m_20318_(partial);
    }
    @Override public void renderForBone(PoseStack pose,AscianKingEntity e,GeoBone bone,RenderType type,MultiBufferSource buffers,VertexConsumer ignored,float partial,int light,int overlay){
        if(!bone.getName().equals("wabbajack")||base==null||!StarFantasyTranslucentWorldRenderer.shouldDefer())return;
        History h=histories.computeIfAbsent(e,k->new History());int action=e.visualAction();
        if(h.serial!=e.visualSerial()||h.owner!=null&&h.owner.m_82557_(origin)>9||!e.m_6084_()||e.visualStunned()||action<10||action>13){h.points.clear();h.serial=e.visualSerial();}
        h.owner=origin;
        if(action<10||action>13||!e.m_6084_()||e.visualStunned())return;
        float age=e.visualActionAge(partial),hit=new int[]{15,16,24,23}[action-10];
        if(age<hit-6||age>hit+3.5F){h.points.clear();return;}
        Matrix4f local=new Matrix4f(inverse).mul(pose.m_85850_().m_252922_());
        Vec3 a=world(local,-18.5F/16,71.25F/16,-13F/16),b=world(local,-18.5F/16,94F/16,-13F/16);
        float time=e.f_19797_+partial;
        if(age<=hit+1.5F){
            if(!h.points.isEmpty()&&time<=h.points.get(h.points.size()-1).time)return;
            h.points.add(new Sample(time,a,b));
        }
        while(h.points.size()>2&&(time-h.points.get(0).time>10.5F||h.points.size()>240))h.points.remove(0);
        if(h.points.size()<2)return;
        List<Sample> selected=new ArrayList<>();Sample last=h.points.get(h.points.size()-1);selected.add(last);double distance=0;
        for(int i=h.points.size()-2;i>=0;i--){Sample s=h.points.get(i);double step=Math.max(last.a.m_82554_(s.a),last.b.m_82554_(s.b));
            if(distance+step>7.2){double f=(7.2-distance)/Math.max(step,1e-8);selected.add(new Sample((float)(last.time+(s.time-last.time)*f),last.a.m_165921_(s.a,f),last.b.m_165921_(s.b,f)));break;}
            selected.add(s);distance+=step;last=s;
        }
        Collections.reverse(selected);if(selected.size()<2)return;
        // Resample to 32 connected quads for consistent texture detail at any frame rate.
        Vec3[] vertices=new Vec3[66];float from=selected.get(0).time,to=selected.get(selected.size()-1).time;
        if(to-from<.001)return;
        for(int i=0;i<=32;i++){float t=from+(to-from)*i/32;int j=1;while(j<selected.size()-1&&selected.get(j).time<t)j++;
            Sample l=selected.get(j-1),r=selected.get(j);double f=Math.max(0,Math.min(1,(t-l.time)/Math.max(.0001,r.time-l.time)));
            vertices[i*2]=l.a.m_165921_(r.a,f).m_82546_(origin);vertices[i*2+1]=l.b.m_165921_(r.b,f).m_82546_(origin);
        }
        int color=COLORS[Math.max(0,Math.min(5,e.visualPhase()-1))];boolean dark=e.visualPhase()==5;
        float fade=.95F*Math.max(0,1-Math.max(0,age-hit-1.5F)/2);
        StarFantasyTranslucentWorldRenderer.defer(base,(p,buff)->{
            var out=buff.m_6299_(AscianEffectMaterials.get(FLOW,true,dark));
            for(int i=0;i<32;i++){v(p,out,vertices[i*2],0,i/32F,color,fade);v(p,out,vertices[i*2+1],1,i/32F,color,fade);v(p,out,vertices[i*2+3],1,(i+1)/32F,color,fade);v(p,out,vertices[i*2+2],0,(i+1)/32F,color,fade);}
        });
    }
    private Vec3 world(Matrix4f m,float x,float y,float z){Vector3f p=m.transformPosition(new Vector3f(x,y,z));return origin.m_82520_(p.x,p.y,p.z);}
    private static void v(PoseStack p,VertexConsumer out,Vec3 v,float u,float w,int c,float a){AscianSpellEffects.vertex(p,out,new float[]{(float)v.f_82479_,(float)v.f_82480_,(float)v.f_82481_,u,w},(c>>16&255)/255F,(c>>8&255)/255F,(c&255)/255F,a);}
}
