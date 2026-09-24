package com.starfantasy.goety.client;
import com.starfantasy.goety.entity.ApostleServantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ApostleServantGeoRenderer extends GeoEntityRenderer<ApostleServantEntity> {
    public ApostleServantGeoRenderer(EntityRendererProvider.Context context) {
        super(context,new ApostleServantGeoModel());
        addRenderLayer(new ApostleServantHeldItemLayer<>(this,context.m_234598_()));
        addRenderLayer(new ApostleServantAuraLayer(this));
        f_114477_=.5F;
    }
    @Override public void m_7392_(ApostleServantEntity entity,float yaw,float partial,
            com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffers,int light) {
        super.m_7392_(entity,yaw,partial,pose,buffers,light);
        ApollyonDeathLight.renderApollyon(entity.deathAge(),partial,pose,buffers);
        if(entity.starfantasy$shieldVisible() && entity.m_6084_()) {
            pose.m_85836_(); pose.m_85837_(0,1.15,0);
            ApollyonGloriousSphereRenderer.renderSphere(buffers.m_6299_(
                    com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes.translucentPositionColor()),
                    pose.m_85850_().m_252922_(),1.55F,1,.82F,.12F,.25F);
            pose.m_85849_();
        }
    }
    @Override protected float getDeathMaxRotation(ApostleServantEntity entity) { return 0; }
}
