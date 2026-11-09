package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.AscianProjectileEntity;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Independent skull model and the existing tweaks barrage visuals. */
public final class AscianProjectileRenderer extends EntityRenderer<AscianProjectileEntity> {
    private final SkullModel skull;
    private final AscianBarrageRenderer barrage;
    public AscianProjectileRenderer(EntityRendererProvider.Context context){super(context);skull=new SkullModel(context.m_174023_(ModelLayers.f_171220_));barrage=new AscianBarrageRenderer(context);}
    @Override public ResourceLocation m_5478_(AscianProjectileEntity entity){return new ResourceLocation("minecraft","textures/entity/wither/wither.png");}
    @Override public void m_7392_(AscianProjectileEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light){
        if(entity.style()!=5){barrage.m_7392_(entity,yaw,partial,pose,buffer,light);return;}
        if(entity.style()==5){
            pose.m_85836_();pose.m_85841_(-1.5F,-1.5F,1.5F);
            skull.m_6251_(0,Mth.m_14189_(partial,entity.f_19859_,entity.m_146908_()),Mth.m_14179_(partial,entity.f_19860_,entity.m_146909_()));
            skull.m_7695_(pose,buffer.m_6299_(skull.m_103119_(m_5478_(entity))),15728880,OverlayTexture.f_118083_,1,1,1,1);
            pose.m_85849_();
        }
        super.m_7392_(entity,yaw,partial,pose,buffer,light);
    }
}
