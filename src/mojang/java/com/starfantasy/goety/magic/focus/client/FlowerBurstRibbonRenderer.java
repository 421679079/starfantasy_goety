package com.starfantasy.goety.magic.focus.client;

import com.starfantasy.library.vfx.StarFantasyRibbonGeometry;

import com.mojang.blaze3d.vertex.*;
import com.starfantasy.goety.magic.focus.entity.FlowerBurstRibbonEntity;
import com.starfantasy.library.vfx.client.StarFantasyDeferredWorldRenderer;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class FlowerBurstRibbonRenderer extends EntityRenderer<FlowerBurstRibbonEntity> {
    public FlowerBurstRibbonRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public ResourceLocation getTextureLocation(FlowerBurstRibbonEntity entity){return null;}
    @Override public boolean shouldRender(FlowerBurstRibbonEntity entity,Frustum frustum,double x,double y,double z) {
        return frustum.isVisible(entity.getBoundingBox().inflate(entity.visualRadius()*2.5F+2));
    }
    @Override public void render(FlowerBurstRibbonEntity entity,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light) {
        if(StarFantasyShaderCompat.isRenderingShaderShadowPass())return;
        if(StarFantasyDeferredWorldRenderer.shouldDefer()) {
            StarFantasyDeferredWorldRenderer.defer(poses,(restored,deferred)->render(entity,yaw,partial,restored,deferred,light));
            return;
        }
        float age=entity.visualAge(partial);
        float progress=age/entity.animationTicks();
        float fade=StarFantasyRibbonGeometry.opacity(age,entity.lifetime());if(fade<=0)return;
        float radius=entity.visualRadius();
        var camera=entityRenderDispatcher.cameraOrientation();
        var worldUp=new org.joml.Vector3f(0,1,0).rotate(new org.joml.Quaternionf(camera).conjugate());
        // Lift in world space BEFORE billboarding. Include the downward arms, not just the center.
        float lift=StarFantasyRibbonGeometry.groundLift(progress,radius,worldUp.x,worldUp.y,worldUp.z);
        poses.pushPose();poses.translate(0,lift,0);poses.mulPose(camera);poses.scale(radius,radius,radius);
        Matrix4f pose=poses.last().pose();VertexConsumer buffer=buffers.getBuffer(StarFantasyVfxRenderTypes.translucentPositionColor());
        float[] point=new float[3];
        FlowerRibbonMesh.strip(buffer,pose,fade,0,1,StarFantasyRibbonGeometry.SEGMENTS,false,
                (ribbon,u,width,side,out)->StarFantasyRibbonGeometry.point(ribbon,u,progress,width,side,out));
        // Small blue/pink glints follow the flowing arms; their positions are deterministic per effect.
        for(int i=0;i<StarFantasyRibbonGeometry.RIBBONS*6;i++) {
            float u=.18F+(i%6)*.14F;
            StarFantasyRibbonGeometry.point(i/6,u,progress,0,0,point);
            float x=point[0]+(float)Math.sin(i*2.3)*.07F,y=point[1]+(float)Math.cos(i*3.1)*.07F;
            float a=fade*(.4F+.4F*(float)Math.sin(entity.visualAge(partial)*.6+i));
            FlowerRibbonMesh.star(buffer,pose,x,y,.009F,.0013F,.82F,.83F,1,a);
        }
        FlowerRibbonMesh.star(buffer,pose,0,0,StarFantasyRibbonGeometry.CENTER_GLOW_LENGTH,StarFantasyRibbonGeometry.CENTER_GLOW_WIDTH,.78F,.85F,1,fade*.22F);
        FlowerRibbonMesh.star(buffer,pose,0,0,StarFantasyRibbonGeometry.CENTER_STAR_LENGTH,StarFantasyRibbonGeometry.CENTER_STAR_WIDTH,1,.94F,1,fade);
        poses.popPose();
    }
}
