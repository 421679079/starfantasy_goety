package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.AscianPhantomBladeEntity;
import com.starfantasy.library.vfx.client.blade.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Uses the exact shared crescent and wake used by Maledictus, tinted purple. */
public final class AscianPhantomBladeRenderer extends EntityRenderer<AscianPhantomBladeEntity> {
    private final DriveRibbonMeshRenderer mesh=new DriveRibbonMeshRenderer();
    public AscianPhantomBladeRenderer(EntityRendererProvider.Context context){super(context);}
    @Override public void m_7392_(AscianPhantomBladeEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffer,int light){
        float age=e.f_19797_+partial;pose.m_85836_();
        DriveRibbonGeometry.applyTransform(pose.m_85850_().m_252922_(),Mth.m_14189_(partial,e.f_19859_,e.m_146908_()),Mth.m_14179_(partial,e.f_19860_,e.m_146909_()),e.roll(),3);
        mesh.render(buffer.m_6299_(SwordEffectRenderTypes.slashRibbon(DriveRibbonMeshRenderer.TEXTURE)),pose.m_85850_().m_252922_(),AscianPhantomBladeEntity.COLOR,
                e.m_20238_(this.f_114476_.f_114358_.m_90583_())<1024,age,AscianPhantomBladeEntity.LIFETIME,(e.m_19879_()*.618034F)%1,1,1,age);
        pose.m_85849_();
    }
    @Override public ResourceLocation m_5478_(AscianPhantomBladeEntity e){return DriveRibbonMeshRenderer.TEXTURE;}
}
