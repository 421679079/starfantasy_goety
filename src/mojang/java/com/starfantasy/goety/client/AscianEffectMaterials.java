package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import java.io.IOException;

/** Unlit and world-depth-tested. Submitted through Library's post-composite pass. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class AscianEffectMaterials extends RenderType {
    private static ShaderInstance surface,trail,trailDark;
    private static final Map<String,RenderType> CACHE=new HashMap<>();
    private static final TransparencyStateShard ADD=new TransparencyStateShard("ascian_add",()->{
        RenderSystem.enableBlend();RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
    },()->{RenderSystem.disableBlend();RenderSystem.defaultBlendFunc();});
    private AscianEffectMaterials(String n,VertexFormat f,VertexFormat.Mode m,int s,boolean c,boolean sort,Runnable a,Runnable b){super(n,f,m,s,c,sort,a,b);}
    @SubscribeEvent public static void register(RegisterShadersEvent e)throws IOException{
        e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("starfantasy_goety","ascian_surface"),DefaultVertexFormat.POSITION_TEX_COLOR),s->surface=s);
        e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("starfantasy_goety","ascian_trail"),DefaultVertexFormat.POSITION_TEX_COLOR),s->trail=s);
        e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("starfantasy_goety","ascian_trail_dark"),DefaultVertexFormat.POSITION_TEX_COLOR),s->trailDark=s);
    }
    public static RenderType get(ResourceLocation tex,boolean ribbon,boolean dark){
        return CACHE.computeIfAbsent(tex+"/"+ribbon+"/"+dark,k->create("ascian/"+k,DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,65536,false,false,
            CompositeState.builder().setShaderState(new ShaderStateShard(()->ribbon?(dark?trailDark:trail):surface))
                .setTextureState(new TextureStateShard(tex,ribbon,false))
                .setTransparencyState(ribbon&&!dark?ADD:TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL)
                .setWriteMaskState(COLOR_WRITE).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(false)));
    }
    public static RenderType pillar(ResourceLocation tex){
        return CACHE.computeIfAbsent("pillar/"+tex,k->create("ascian/"+k,DefaultVertexFormat.POSITION_TEX_COLOR,VertexFormat.Mode.QUADS,65536,false,true,
            CompositeState.builder().setShaderState(new ShaderStateShard(()->surface))
                .setTextureState(new TextureStateShard(tex,false,false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(CULL).setWriteMaskState(COLOR_WRITE).setDepthTestState(LEQUAL_DEPTH_TEST).createCompositeState(false)));
    }
}
