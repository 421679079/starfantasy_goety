package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.AscianKingEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class AscianKingRenderer extends GeoEntityRenderer<AscianKingEntity> {
    private final AscianStaffTrail staffTrail;
    private final AscianSummonMesh summonMesh=new AscianSummonMesh();
    public AscianKingRenderer(EntityRendererProvider.Context context) {
        super(context, new AscianKingModel());
        this.f_114477_ = 0;
        addRenderLayer(new AscianKingEffects(this));
        staffTrail=new AscianStaffTrail(this);addRenderLayer(staffTrail);
    }
    @Override public void m_7392_(AscianKingEntity entity, float yaw, float partial,
            com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        staffTrail.begin(entity,pose,partial);
        if(entity.isSummoning()) {
            float age=entity.summonAge(partial);
            super.m_7392_(entity,yaw,partial,pose,summonMesh.capture(pose,buffers,false),light);
            float bottom=summonMesh.bottom()-.05F,top=summonMesh.top()+.1F;
            if(summonMesh.usable())summonMesh.render(pose,buffers,bottom+(top-bottom)*AscianSummonMesh.reveal(age),false,1);
            return;
        }
        super.m_7392_(entity, yaw, partial, pose, buffers, light);
        ApollyonDeathLight.renderAscian(entity, partial, pose, buffers);
    }
    @Override public boolean m_5523_(AscianKingEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.m_6000_(x, y, z) && frustum.m_113029_(entity.m_21224_()
                ? entity.m_20191_().m_82400_(26) : entity.m_20191_().m_82377_(9, 3, entity.isSummoning()?6:4));
    }
    @Override protected float getDeathMaxRotation(AscianKingEntity entity) { return 0; }
    @Override public int getPackedOverlay(AscianKingEntity entity, float partial) {
        return entity.m_21224_() ? net.minecraft.client.renderer.texture.OverlayTexture.f_118083_ : super.getPackedOverlay(entity, partial);
    }
}
