package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.ModModelLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.LocalIceChunkEntity;
import com.starfantasy.library.vfx.client.StarFantasyShaderCompat;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the installed Goety model layer without ticking or constructing a Goety projectile. */
public class LocalIceChunkRenderer<T extends LocalIceChunkEntity> extends EntityRenderer<T> {
    private final ModelPart root;
    private final float size;
    public LocalIceChunkRenderer(EntityRendererProvider.Context context) { this(context,1); }
    protected LocalIceChunkRenderer(EntityRendererProvider.Context context,float size) {
        super(context);this.size=size;root=context.m_174023_(ModModelLayer.ICE_CHUNK);
    }
    @Override public ResourceLocation m_5478_(T entity) {
        return new ResourceLocation("goety","textures/entity/projectiles/ice_chunk"+(entity.isStarting()?"_spawn":"")+".png");
    }
    @Override public void m_7392_(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light) {
        if(StarFantasyShaderCompat.isRenderingShaderShadowPass())return;
        boolean starting=entity.isStarting();
        float scale=(starting?entity.hovering/10F:2)*size;
        float spin=starting?entity.f_19797_+partial:0;
        ResourceLocation texture=m_5478_(entity);
        // The post-composite world pass prevents shader packs from relighting this material.
        if(StarFantasyTranslucentWorldRenderer.shouldDefer())
            StarFantasyTranslucentWorldRenderer.defer(pose,0,1.45F*scale,0,
                    (p,b)->draw(starting,scale,spin,texture,yaw,p,b,true,15728880));
        else draw(starting,scale,spin,texture,yaw,pose,buffer,true,15728880);
        super.m_7392_(entity,yaw,partial,pose,buffer,light);
    }
    private void draw(boolean starting,float scale,float spin,ResourceLocation texture,float yaw,PoseStack pose,MultiBufferSource buffer,boolean unlit,int light){
        pose.m_85836_();pose.m_252781_(Axis.f_252436_.m_252977_(90-yaw));
        pose.m_85841_(-scale,-scale,scale);pose.m_85837_(0,-1.45,0);
        root.m_171324_("chunk").f_104204_=spin;
        RenderType type=starting?RenderType.m_110488_(texture):unlit?LocalIceMaterials.body(texture):RenderType.m_110452_(texture);
        root.m_104306_(pose,buffer.m_6299_(type),starting?15728880:light,OverlayTexture.f_118083_,1,1,1,starting||!unlit?.15F:1);
        pose.m_85849_();
    }
}
