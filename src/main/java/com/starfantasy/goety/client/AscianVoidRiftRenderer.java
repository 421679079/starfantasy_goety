package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.starfantasy.goety.entity.AscianVoidRiftEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Goety's camera-facing black-hole textures; no Goety gameplay entity is created or ticked. */
public final class AscianVoidRiftRenderer extends EntityRenderer<AscianVoidRiftEntity> {
    private static final ResourceLocation[] ACTIVE=new ResourceLocation[5],CLOSING=new ResourceLocation[3];
    static{for(int i=0;i<5;i++)ACTIVE[i]=texture("rift_"+(i+1));for(int i=0;i<3;i++)CLOSING[i]=texture("open_"+(i+1));}
    private static ResourceLocation texture(String name){return new ResourceLocation("goety","textures/entity/projectiles/void_rift/"+name+".png");}
    public AscianVoidRiftRenderer(EntityRendererProvider.Context context){super(context);}
    public static void startSound(AscianVoidRiftEntity e){com.Polarice3.Goety.client.audio.LoopSoundPlayer.playSound(e,com.Polarice3.Goety.init.ModSounds.VOID_RIFT.get(),5,1);}
    @Override public ResourceLocation m_5478_(AscianVoidRiftEntity e){
        int remaining=e.getDuration()-e.f_19797_;return remaining<=20?CLOSING[Math.max(0,Math.min(2,remaining*3/21))]:ACTIVE[(e.f_19797_/20)%5];
    }
    @Override public void m_7392_(AscianVoidRiftEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.m_85836_();var owner=e.owner();
        if(owner!=null){Vec3 anchor=owner.m_20318_(partial).m_82520_(0,AscianVoidRiftEntity.HEIGHT,0),current=e.m_20318_(partial);pose.m_85837_(anchor.f_82479_-current.f_82479_,anchor.f_82480_-current.f_82480_,anchor.f_82481_-current.f_82481_);}
        pose.m_85841_(6,6,6);pose.m_252781_(this.f_114476_.m_253208_());pose.m_252781_(Axis.f_252436_.m_252977_(180));
        VertexConsumer out=buffers.m_6299_(RenderType.m_110458_(m_5478_(e)));
        vertex(pose,out,-.5F,-.5F,0,1);vertex(pose,out,.5F,-.5F,1,1);vertex(pose,out,.5F,.5F,1,0);vertex(pose,out,-.5F,.5F,0,0);pose.m_85849_();
    }
    private static void vertex(PoseStack p,VertexConsumer out,float x,float y,float u,float v){out.m_252986_(p.m_85850_().m_252922_(),x,y,0).m_6122_(255,255,255,255).m_7421_(u,v).m_86008_(OverlayTexture.f_118083_).m_85969_(15728880).m_252939_(p.m_85850_().m_252943_(),0,1,0).m_5752_();}
}
