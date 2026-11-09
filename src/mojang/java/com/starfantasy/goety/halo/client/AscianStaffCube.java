package com.starfantasy.goety.halo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Same translucent emissive shell and luminous edges as the boss's staff core. */
public final class AscianStaffCube {
    // Keep the boss's local dimensions, scaled by the same amount as the extracted staff.
    private static final float MODEL_SCALE = .36F;
    private static final int FULL_LIGHT = 15728880;
    private static final ResourceLocation WHITE = new ResourceLocation("starfantasy_goety", "textures/entity/ascian_king/white.png");
    private static final int[][] FACES={{0,4,6,2},{1,3,7,5},{0,1,5,4},{2,6,7,3},{0,2,3,1},{4,5,7,6}};

    public static void render(PoseStack pose,MultiBufferSource buffers,float time,int color,int overlay){
        pose.pushPose();
        pose.translate((18.49794-18.5)*MODEL_SCALE/16D,(84-21)*MODEL_SCALE/16D,(-13.00393+13)*MODEL_SCALE/16D);
        pose.scale(MODEL_SCALE/16F,MODEL_SCALE/16F,MODEL_SCALE/16F);
        pose.mulPose(new Quaternionf().rotationY(time*2.55F/20F).rotateZ(.45F).rotateX(.3F));
        var out=buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        box(pose,out,0,0,0,4.8F,4.8F,4.8F,color,.52F,overlay);
        for(int a:new int[]{-1,1})for(int b:new int[]{-1,1}){
            box(pose,out,0,a*2.4F,b*2.4F,4.88F,.10F,.10F,color,.9F,overlay);
            box(pose,out,a*2.4F,0,b*2.4F,.10F,4.88F,.10F,color,.9F,overlay);
            box(pose,out,a*2.4F,b*2.4F,0,.10F,.10F,4.88F,color,.9F,overlay);
        }
        pose.popPose();
    }
    private static void box(PoseStack pose,VertexConsumer out,float x,float y,float z,float sx,float sy,float sz,int color,float alpha,int overlay){
        Vector3f[] corners=new Vector3f[8];
        for(int i=0;i<8;i++)corners[i]=new Vector3f(x+((i&1)==0?-sx:sx)/2,y+((i&2)==0?-sy:sy)/2,z+((i&4)==0?-sz:sz)/2);
        for(int[] face:FACES)for(int i:face){
            Vector3f p=corners[i];
            out.vertex(pose.last().pose(),p.x,p.y,p.z).color((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,alpha)
                    .uv(.5F,.5F).overlayCoords(overlay).uv2(FULL_LIGHT).normal(pose.last().normal(),0,1,0).endVertex();
        }
    }
    private AscianStaffCube(){}
}
