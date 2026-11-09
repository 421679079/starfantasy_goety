package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.AscianKingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Runtime geometry, matching the approved V7 preview in model pixels (16 pixels = 1 block).
 * No particle/entity spawning, global per-entity caches, or extra synchronized ticks.
 * Six membrane bays use a shared 24 x 34 mesh and a looping procedural pixel atlas.
 */
public final class AscianKingEffects extends GeoRenderLayer<AscianKingEntity> {
    private static final ResourceLocation SMOKE = texture("black_smoke.png"), WHITE = texture("white.png");
    // Same synchronized phase drives the head, membrane and detached wing wisps.
    private static final ResourceLocation[] FIRE = textures("orange_fire", "soul_fire", "white_fire", "purple_fire", "black_fire", "red_fire"),
            MEMBRANE = textures("wing_orange", "wing_soul", "wing_white", "wing_purple", "wing_smoke", "wing_red"),
            WISPS = textures("wing_orange_wisps", "wing_soul_wisps", "wing_white_wisps", "wing_purple_wisps", "black_smoke", "wing_red_wisps");
    private static final int FULL_LIGHT = 15728880;
    private static final int[] CORE_COLORS = {0xf57118,0x48e8ea,0xe2ebf4,0xc778fa,0x413b4d,0xff5145};
    private static final int NX = 24, NY = 34, MEMBRANE_FRAMES = 96;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector3f[] NODES = nodes();
    private static final Vector3f[][][] BAYS = bays();
    private static final int[][] SEGMENTS = {{0,1},{1,2},{2,3},{1,4},{4,5},{1,6},{6,7}};
    private static final float[][] WIDTHS = {{3.4F,2.7F},{2.7F,1.9F},{1.9F,.18F},{1.8F,1.35F},{1.35F,.12F},{1.65F,1.15F},{1.15F,.12F}};
    private record BonePose(Matrix4f position, Matrix3f normal) {}
    private BonePose headPose, smokePose, wingPose, staffPose;

    public AscianKingEffects(GeoRenderer<AscianKingEntity> renderer) { super(renderer); }
    private static ResourceLocation texture(String name) { return AscianKingModel.id("textures/entity/ascian_king/" + name); }
    private static ResourceLocation[] textures(String... names) {
        ResourceLocation[] result = new ResourceLocation[names.length];
        for (int i=0;i<names.length;i++) result[i]=texture(names[i]+".png");
        return result;
    }

    @Override public void preRender(PoseStack pose, AscianKingEntity entity, BakedGeoModel model, RenderType base,
            MultiBufferSource buffers, VertexConsumer ignored, float partial, int light, int overlay) {
        headPose = smokePose = wingPose = staffPose = null;
    }

    @Override public void renderForBone(PoseStack pose, AscianKingEntity entity, GeoBone bone, RenderType base,
            MultiBufferSource buffers, VertexConsumer ignored, float partial, int light, int overlay) {
        // Capture the actual render transform, including entity yaw, scale and ALL animated parents.
        // Rebuilding only the bone chain in render() loses GeoEntityRenderer's outer transform.
        String name = bone.getName();
        if (!name.equals("head") && !name.equals("main") && !name.equals("suit") && !name.equals("top")) return;
        BonePose captured = new BonePose(new Matrix4f(pose.m_85850_().m_252922_()), new Matrix3f(pose.m_85850_().m_252943_()));
        switch (name) {
            case "head" -> headPose = captured;
            case "main" -> smokePose = captured;
            case "suit" -> wingPose = captured;
            case "top" -> staffPose = captured;
        }
    }

    @Override public void render(PoseStack pose, AscianKingEntity entity, BakedGeoModel model, RenderType base,
            MultiBufferSource buffers, VertexConsumer ignored, float partial, int light, int overlay) {
        float seconds = (entity.f_19797_ + partial) / 20F;
        float fade = entity.effectOpacity(partial);
        int phase = Math.max(1, Math.min(6, entity.visualPhase())) - 1;
        attach(pose, headPose, () -> fire(pose, buffers, seconds, fade, overlay, phase));
        attach(pose, staffPose, () -> staffCore(pose, buffers, seconds, fade, overlay, phase));
        attach(pose, smokePose, () -> smoke(pose, buffers, seconds, fade, overlay));
        if (entity.wingsVisible()) attach(pose, wingPose, () -> wings(pose, buffers, seconds, entity.wingScale(), fade, light, overlay, phase));
        headPose = smokePose = wingPose = staffPose = null;
    }

    private static void attach(PoseStack pose, BonePose captured, Runnable draw) {
        if (captured != null) {
            pose.m_85836_();
            pose.m_85850_().m_252922_().set(captured.position());
            pose.m_85850_().m_252943_().set(captured.normal());
            // Bedrock model X is inverted by GeckoLib's cube builder.
            pose.m_85841_(-1F / 16F, 1F / 16F, 1F / 16F);
            draw.run();
            pose.m_85849_();
        }
    }

    private static void fire(PoseStack pose, MultiBufferSource buffers, float time, float fade, int overlay, int phase) {
        flame(pose,buffers,time,fade,overlay,phase,0,87,-3,17,21);
    }

    private static void flame(PoseStack pose, MultiBufferSource buffers, float time, float fade, int overlay, int phase,
            float x,float y,float z,float width,float height) {
        VertexConsumer out = buffers.m_6299_(RenderType.m_110454_(FIRE[phase], false));
        int frame = (int)(time * 10) % 8;
        pose.m_85836_();
        pose.m_85837_(x,y,z);
        for (int i = 0; i < 2; i++) {
            pose.m_85836_();
            pose.m_252781_(new Quaternionf().rotationY((float)Math.PI * (.25F + i * .5F)));
            plane(pose, out, width, height, frame / 8F, (frame + 1) / 8F, fade, overlay);
            pose.m_85849_();
        }
        pose.m_85849_();
    }

    private static void staffCore(PoseStack pose,MultiBufferSource buffers,float time,float fade,int overlay,int phase) {
        // Original staff-head core center, in the animated 'top' bone's model coordinates.
        pose.m_85836_();pose.m_85837_(18.49794,84,-13.00393);
        pose.m_252781_(new Quaternionf().rotationY(time*2.55F).rotateZ(.45F).rotateX(.3F));
        VertexConsumer out=buffers.m_6299_(RenderType.m_234338_(WHITE));
        int color=CORE_COLORS[phase];
        coreBox(pose,out,0,0,0,4.8F,4.8F,4.8F,color,.52F*fade,overlay);
        // Thin luminous edges keep the translucent cube's outline readable.
        for(int a:new int[]{-1,1})for(int b:new int[]{-1,1}){
            coreBox(pose,out,0,a*2.4F,b*2.4F,4.88F,.10F,.10F,color,.9F*fade,overlay);
            coreBox(pose,out,a*2.4F,0,b*2.4F,.10F,4.88F,.10F,color,.9F*fade,overlay);
            coreBox(pose,out,a*2.4F,b*2.4F,0,.10F,.10F,4.88F,color,.9F*fade,overlay);
        }
        pose.m_85849_();
    }

    private static void coreBox(PoseStack pose,VertexConsumer out,float x,float y,float z,float sx,float sy,float sz,int color,float alpha,int overlay){
        Vector3f[] corners=new Vector3f[8];
        for(int i=0;i<8;i++)corners[i]=new Vector3f(x+((i&1)==0?-sx:sx)/2,y+((i&2)==0?-sy:sy)/2,z+((i&4)==0?-sz:sz)/2);
        int[][] faces={{0,4,6,2},{1,3,7,5},{0,1,5,4},{2,6,7,3},{0,2,3,1},{4,5,7,6}};
        for(int[] face:faces)for(int i:face)vertex(pose,out,corners[i],.5F,.5F,(color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,alpha,FULL_LIGHT,overlay);
    }

    private static void smoke(PoseStack pose, MultiBufferSource buffers, float time, float fade, int overlay) {
        VertexConsumer out = buffers.m_6299_(RenderType.m_110454_(SMOKE, false));
        for (int i = 0; i < 38; i++) {
            float age = fract(time / (2.8F + rand(i + 77) * 1.8F) + rand(i + 1));
            float angle = rand(i + 41) * (float)Math.PI * 2 + age * .24F;
            float x, y, z;
            if (i < 14) {
                x = ((i % 7) / 6F * 2 - 1) * 6.5F + (float)Math.sin(angle) * age * .65F;
                y = 37.8F - age * 10 + rand(i + 26) * .6F;
                z = (i < 7 ? -3.7F : 2.4F) + (float)Math.cos(angle) * age * .8F;
            } else {
                x = (((i - 14) % 8) / 7F * 2 - 1) * 6.5F + (float)Math.sin(angle) * age * .8F;
                y = 29 - age * (29 - AscianKingVisualConstants.HEM_Y - 3.4F);
                z = (float)Math.sin(angle) * 3.8F - 1;
            }
            float size = (4.1F + rand(i + 92) * 2.7F) * (.65F + (float)Math.sin(age * Math.PI) * .4F);
            float alpha = (i < 14 ? .90F : .82F) * Math.min(1, age * 12) * Math.min(1, (1 - age) * 5);
            puff(pose, out, new Vector3f(x,y,z), size, age, alpha * fade, overlay);
        }
    }

    private static void wings(PoseStack pose, MultiBufferSource buffers, float time, float scale, float fade, int light, int overlay, int phase) {
        // Finish opaque bone geometry first, then membranes and edge wisps.
        for (int pass = 0; pass < 3; pass++) {
            // Standard translucent entity material is supported by Oculus/Iris shader packs.
            // The emissive material routes dark alpha textures through the eyes/glow pass.
            VertexConsumer out = buffers.m_6299_(pass == 0 ? RenderType.m_110473_(WHITE) : RenderType.m_110454_(pass == 1 ? MEMBRANE[phase] : WISPS[phase], false));
            for (int side : new int[]{-1,1}) {
                pose.m_85836_();
                pose.m_85837_(side * 12, 75, 9);
                pose.m_252781_(new Quaternionf().rotationY(side * (.045F + (float)Math.sin(time * .85) * .025F))
                        .rotateZ(side * (float)Math.sin(time * .85) * .012F));
                pose.m_85841_(side * scale * (.75F + .25F*fade), scale * (.75F + .25F*fade), scale * (.75F + .25F*fade));
                if (pass == 0) {
                    for (int i = 0; i < SEGMENTS.length; i++) bone(pose,out,NODES[SEGMENTS[i][0]],NODES[SEGMENTS[i][1]],WIDTHS[i][0],WIDTHS[i][1],light,overlay);
                    bone(pose,out,NODES[1],new Vector3f(NODES[1]).add(1.3F,3,-.4F),2,.05F,light,overlay);
                    for (int k : new int[]{0,1,2,4,6}) {
                        pose.m_85836_(); pose.m_85837_(NODES[k].x,NODES[k].y,NODES[k].z);
                        pose.m_252781_(new Quaternionf().rotationZ(-.35F));
                        bone(pose,out,new Vector3f(0,0,-1.2F),new Vector3f(0,0,1.2F),k==0?3.5F:2.1F,2.1F,light,overlay);
                        pose.m_85849_();
                    }
                } else if (pass == 1) {
                    int frame = (int)(time * 12) % MEMBRANE_FRAMES;
                    for (int bay = 0; bay < 3; bay++) for (int j = 0; j < NY; j++) for (int i = 0; i < NX; i++) {
                        float u0 = (bay * NX + i) / (float)(NX * 3), u1 = (bay * NX + i + 1) / (float)(NX * 3);
                        float v0 = (frame * NY + j) / (float)(NY * MEMBRANE_FRAMES), v1 = (frame * NY + j + 1) / (float)(NY * MEMBRANE_FRAMES);
                        Vector3f[] grid = BAYS[bay][j], next = BAYS[bay][j+1];
                        Vector3f a=flutter(grid[i],i,j,time), b=flutter(grid[i+1],i+1,j,time),
                                c=flutter(next[i+1],i+1,j+1,time), d=flutter(next[i],i,j+1,time);
                        Vector3f normal=new Vector3f(b).sub(a).cross(new Vector3f(d).sub(a));
                        if(normal.lengthSquared()<1e-10F) normal.set(0,0,1); else normal.normalize();
                        surfaceVertex(pose,out,a,u0,v0,fade,overlay,normal);
                        surfaceVertex(pose,out,b,u1,v0,fade,overlay,normal);
                        surfaceVertex(pose,out,c,u1,v1,fade,overlay,normal);
                        surfaceVertex(pose,out,d,u0,v1,fade,overlay,normal);
                    }
                } else {
                    for(int bay=0;bay<3;bay++) for(int i=0;i<5;i++) {
                        float age=fract(time/3+rand(i+bay*11));
                        Vector3f p=bayPoint(bay,(i+.5F)/5,1).add(age*1.6F,-age*3,age*3);
                        puff(pose,out,p,(2.8F+rand(i+bay*9)*2)*(.8F+age*.4F),age,.38F*fade*(float)Math.sin(age*Math.PI),overlay);
                    }
                }
                pose.m_85849_();
            }
        }
    }

    private static Vector3f flutter(Vector3f p,int i,int j,float time) {
        float s=i/(float)NX,t=j/(float)NY;
        return new Vector3f(p).add(0,0,(float)(Math.sin(t*13+time*1.4+s*4)*Math.sin(s*Math.PI)*Math.sin(t*Math.PI)*.75));
    }
    private static void surfaceVertex(PoseStack pose,VertexConsumer out,Vector3f p,float u,float v,float alpha,int overlay,Vector3f normal) {
        out.m_252986_(pose.m_85850_().m_252922_(),p.x,p.y,p.z).m_85950_(1,1,1,alpha).m_7421_(u,v)
                .m_86008_(overlay).m_85969_(FULL_LIGHT).m_252939_(pose.m_85850_().m_252943_(),normal.x,normal.y,normal.z).m_5752_();
    }
    private static void puff(PoseStack pose,VertexConsumer out,Vector3f p,float size,float age,float alpha,int overlay) {
        // A mirrored wing has a negative determinant: extracting a quaternion from that
        // matrix distorts the billboard. Transform its center, then draw in view space.
        Matrix4f matrix=pose.m_85850_().m_252922_();
        Vector3f center=matrix.transformPosition(new Vector3f(p));
        float half=size*.5F*(float)Math.sqrt(matrix.m00()*matrix.m00()+matrix.m01()*matrix.m01()+matrix.m02()*matrix.m02());
        int frame=Math.min(7,(int)(age*8));
        billboardVertex(out,center.x-half,center.y-half,center.z,0,(frame+1)/8F,alpha,overlay);
        billboardVertex(out,center.x+half,center.y-half,center.z,1,(frame+1)/8F,alpha,overlay);
        billboardVertex(out,center.x+half,center.y+half,center.z,1,frame/8F,alpha,overlay);
        billboardVertex(out,center.x-half,center.y+half,center.z,0,frame/8F,alpha,overlay);
    }
    private static void billboardVertex(VertexConsumer out,float x,float y,float z,float u,float v,float alpha,int overlay) {
        out.m_252986_(IDENTITY,x,y,z).m_85950_(1,1,1,alpha).m_7421_(u,v).m_86008_(overlay)
                .m_85969_(FULL_LIGHT).m_5601_(0,0,1).m_5752_();
    }
    private static void plane(PoseStack pose,VertexConsumer out,float w,float h,float v0,float v1,float alpha,int overlay) {
        vertex(pose,out,new Vector3f(-w/2,-h/2,0),0,v1,1,1,1,alpha,FULL_LIGHT,overlay);
        vertex(pose,out,new Vector3f(w/2,-h/2,0),1,v1,1,1,1,alpha,FULL_LIGHT,overlay);
        vertex(pose,out,new Vector3f(w/2,h/2,0),1,v0,1,1,1,alpha,FULL_LIGHT,overlay);
        vertex(pose,out,new Vector3f(-w/2,h/2,0),0,v0,1,1,1,alpha,FULL_LIGHT,overlay);
    }
    private static void bone(PoseStack pose,VertexConsumer out,Vector3f a,Vector3f b,float wa,float wb,int light,int overlay) {
        Vector3f d=new Vector3f(b).sub(a); float length=d.length();
        pose.m_85836_();pose.m_85837_(a.x,a.y,a.z);
        pose.m_252781_(new Quaternionf().rotationTo(new Vector3f(0,0,1),d.normalize()));
        Vector3f[] p=new Vector3f[8];
        for(int k=0;k<8;k++){float w=k<4?wa:wb;int c=k%4;p[k]=new Vector3f((c==0||c==3?-1:1)*w/2,(c<2?-1:1)*w/2,k<4?0:length);}
        int[][] faces={{0,3,2,1},{4,5,6,7},{0,1,5,4},{1,2,6,5},{2,3,7,6},{3,0,4,7}};
        for(int f=0;f<6;f++) for(int k:faces[f]) {
            float shade=f%2==0?1:.72F;
            vertex(pose,out,p[k],.5F,.5F,.294F*shade,.275F*shade,.333F*shade,1,light,overlay);
        }
        pose.m_85849_();
    }
    private static void vertex(PoseStack pose,VertexConsumer out,Vector3f p,float u,float v,float r,float g,float b,float a,int light,int overlay) {
        out.m_252986_(pose.m_85850_().m_252922_(),p.x,p.y,p.z).m_85950_(r,g,b,a).m_7421_(u,v)
                .m_86008_(overlay).m_85969_(light).m_252939_(pose.m_85850_().m_252943_(),0,1,0).m_5752_();
    }
    private static Vector3f[] nodes() {
        float[][] values={{2.82F,-5.68F,2.14F},{8.05F,3.67F,5.04F},{17.8F,2.1F,10.02F},{26.16F,-4.41F,14.2F},{13.85F,.1F,7.85F},{16.22F,-4.01F,9.01F},{11,-2.28F,6.32F},{12.2F,-6.26F,6.88F}};
        Vector3f[] n=new Vector3f[8];for(int i=0;i<8;i++) n[i]=new Vector3f(values[i]).sub(2.82F,-5.68F,2.14F).mul(2.35F,1.85F,1.2F);
        n[5].y-=4;n[7].y-=7;return n;
    }
    private static Vector3f ray(int path,float t) {
        Vector3f a=NODES[1],b,c;
        if(path<3){int[] ids={2,4,6};b=NODES[ids[path]];c=NODES[ids[path]+1];}
        else {b=new Vector3f(NODES[0]).lerp(NODES[1],.48F);c=NODES[0];}
        return t<.53F?new Vector3f(a).lerp(b,t/.53F):new Vector3f(b).lerp(c,(t-.53F)/.47F);
    }
    private static Vector3f bayPoint(int bay,float s,float t) {
        return ray(bay,t).lerp(ray(bay+1,t),s).lerp(NODES[1],(float)Math.sin(s*Math.PI)*t*t*t*.24F);
    }
    private static Vector3f[][][] bays() {
        Vector3f[][][] result=new Vector3f[3][NY+1][NX+1];
        for(int b=0;b<3;b++)for(int j=0;j<=NY;j++)for(int i=0;i<=NX;i++)result[b][j][i]=bayPoint(b,i/(float)NX,j/(float)NY).add(0,0,.25F);
        return result;
    }
    private static float fract(float v){return v-(float)Math.floor(v);}
    private static float rand(int i){double v=Math.sin(i*127.1+31.7)*43758.5453;return (float)(v-Math.floor(v));}
}
