package com.starfantasy.goety.client;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class ApollyonServantAuraLayer extends GeoRenderLayer<ApollyonServantEntity> {
    private static final ResourceLocation AURA=new ResourceLocation("goety","textures/entity/cultist/apostle_aura.png");
    public ApollyonServantAuraLayer(GeoRenderer<ApollyonServantEntity> renderer) { super(renderer); }
    @Override public void render(PoseStack pose,ApollyonServantEntity entity,BakedGeoModel model,RenderType base,
            MultiBufferSource buffers,VertexConsumer vertices,float partial,int light,int overlay) {
        if(!entity.monolithVisible() || !entity.m_6084_()) return;
        float age=entity.f_19797_+partial;
        RenderType type=RenderType.m_110436_(AURA,(float)Math.cos(age*.02F)*3%1,age*.01F%1);
        getRenderer().reRender(model,pose,buffers,entity,type,buffers.m_6299_(type),partial,light,overlay,.5F,.5F,.5F,1);
    }
}
