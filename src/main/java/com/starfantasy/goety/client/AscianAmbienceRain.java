package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.library.vfx.client.StarFantasyTranslucentWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Arrays;

/** Local rain matching the six head-flame palettes, without changing world weather. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianAmbienceRain {
    private static final ResourceLocation TEXTURE=new ResourceLocation("minecraft","textures/environment/rain.png");
    // Representative texels from the head-flame palettes in build_ascian_phase_colors.py.
    private static final float[][] COLORS={
            {245F/255,113F/255,24F/255},{72F/255,232F/255,234F/255},{226F/255,235F/255,244F/255},
            {199F/255,120F/255,250F/255},{26F/255,23F/255,34F/255},{1F,81F/255,69F/255}};
    private static final float[] PREVIOUS=new float[6], CURRENT=new float[6];
    private static ClientLevel currentLevel;

    private static void reset(ClientLevel level){
        Arrays.fill(PREVIOUS,0);Arrays.fill(CURRENT,0);currentLevel=level;
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.START)return;
        var mc=Minecraft.m_91087_();var level=mc.f_91073_;var player=mc.f_91074_;
        if(level!=currentLevel)reset(level);
        if(level==null||player==null){reset(level);return;}
        if(mc.m_91104_())return;
        System.arraycopy(CURRENT,0,PREVIOUS,0,6);
        boolean[] active=new boolean[6];
        for(AscianKingEntity boss:level.m_45976_(AscianKingEntity.class,player.m_20191_().m_82400_(128)))
            if(boss.m_6084_())active[Math.max(1,Math.min(6,boss.visualPhase()))-1]=true;
        for(int i=0;i<6;i++)CURRENT[i]=Math.max(0,Math.min(1,CURRENT[i]+(active[i]?.025F:-.04F)));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        // Reuse the shader-compatible post-composite path used by the other Ascian effects.
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!StarFantasyTranslucentWorldRenderer.shouldDefer())return;
        var mc=Minecraft.m_91087_();var level=mc.f_91073_;
        if(level==null||level!=currentLevel||mc.f_91074_==null)return;
        Vec3 camera=event.getCamera().m_90583_();float partial=event.getPartialTick();
        float age=(float)level.m_46467_()+partial;
        for(int i=0;i<6;i++){
            float intensity=PREVIOUS[i]+(CURRENT[i]-PREVIOUS[i])*partial;
            if(intensity<=.001F)continue;
            float[] color=COLORS[i];
            PoseStack pose=event.getPoseStack();pose.m_85836_();
            pose.m_85837_(-camera.f_82479_,-camera.f_82480_,-camera.f_82481_);
            StarFantasyTranslucentWorldRenderer.defer(pose,(float)camera.f_82479_,(float)camera.f_82480_,(float)camera.f_82481_,
                    (p,b)->draw(p,b.m_6299_(AscianEffectMaterials.get(TEXTURE,false,false)),camera,age,intensity,color));
            pose.m_85849_();
        }
    }
    private static void draw(PoseStack pose,VertexConsumer out,Vec3 camera,float age,float intensity,float[] color){
        int cx=(int)Math.floor(camera.f_82479_),cy=(int)Math.floor(camera.f_82480_),cz=(int)Math.floor(camera.f_82481_);
        int low=cy-5,high=cy+22;
        for(int z=cz-10;z<=cz+10;z++)for(int x=cx-10;x<=cx+10;x++){
            double dx=x+.5-camera.f_82479_,dz=z+.5-camera.f_82481_;
            float distance=(float)Math.sqrt(dx*dx+dz*dz)/10;
            if(distance>1.15F)continue;
            distance=Math.min(1,distance);
            float alpha=((1-distance*distance)*.52F+.28F)*intensity*.64F;
            if(alpha<=.01F)continue;
            long seed=rainSeed(x,z);
            double angle=(seed&1023L)/1024D*Math.PI*2,width=.34+((seed>>>10)&127L)/127D*.18;
            double ox=Math.cos(angle)*width,oz=Math.sin(angle)*width;
            float scroll=-age*.085F+((seed>>>17)&31L)*.03125F;
            float v0=low*.18F+scroll,v1=high*.18F+scroll;
            vertex(pose,out,x+.5-ox,high,z+.5-oz,0,v1,color,alpha);
            vertex(pose,out,x+.5+ox,high,z+.5+oz,1,v1,color,alpha);
            vertex(pose,out,x+.5+ox,low,z+.5+oz,1,v0,color,alpha);
            vertex(pose,out,x+.5-ox,low,z+.5-oz,0,v0,color,alpha);
        }
    }
    private static void vertex(PoseStack pose,VertexConsumer out,double x,double y,double z,float u,float v,float[] color,float alpha){
        out.m_252986_(pose.m_85850_().m_252922_(),(float)x,(float)y,(float)z).m_7421_(u,v)
                .m_85950_(color[0],color[1],color[2],alpha).m_5752_();
    }
    private static long rainSeed(int x,int z){
        long seed=(long)x*3129871L^(long)z*116129781L;
        seed^=seed>>>13;seed*=0x5DEECE66DL;seed^=seed>>>17;return seed;
    }
    private AscianAmbienceRain(){}
}
