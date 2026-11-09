package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** A separate, unlit entity-format shader; never replaces Goety or vanilla materials. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class LocalIceMaterials extends RenderType {
    private static ShaderInstance shader;
    private static final Map<ResourceLocation,RenderType> CACHE=new HashMap<>();
    private LocalIceMaterials(String n,VertexFormat f,VertexFormat.Mode m,int s,boolean c,boolean sort,Runnable a,Runnable b){super(n,f,m,s,c,sort,a,b);}
    @SubscribeEvent public static void register(RegisterShadersEvent event)throws IOException{
        event.registerShader(new ShaderInstance(event.getResourceProvider(),new ResourceLocation("starfantasy_goety","local_ice"),
                DefaultVertexFormat.NEW_ENTITY),s->shader=s);
    }
    public static RenderType body(ResourceLocation texture){
        return CACHE.computeIfAbsent(texture,t->create("local_ice/"+t,DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,4096,false,false,
                CompositeState.builder().setShaderState(new ShaderStateShard(()->shader))
                        .setTextureState(new TextureStateShard(t,false,false)).setTransparencyState(NO_TRANSPARENCY)
                        .setCullState(CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_DEPTH_WRITE)
                        .createCompositeState(false)));
    }
}
