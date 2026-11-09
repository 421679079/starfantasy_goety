package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Full-bright, depth-tested world meshes; transparent flame sheets never write depth. */
public final class AscianMeteorMaterials extends RenderType {
    private static final Map<ResourceLocation,RenderType> ROCK=new HashMap<>();
    public static RenderType rock(ResourceLocation texture){return ROCK.computeIfAbsent(texture,t->create("ascian_meteor/"+t,
            DefaultVertexFormat.POSITION_COLOR_TEX,VertexFormat.Mode.QUADS,4096,false,false,
            CompositeState.builder().setShaderState(POSITION_COLOR_TEX_SHADER).setTextureState(new TextureStateShard(t,false,false))
                    .setTransparencyState(NO_TRANSPARENCY).setCullState(CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false)));}
    public static RenderType flame(ResourceLocation texture){return RenderType.entityTranslucentEmissive(texture);}
    private AscianMeteorMaterials(String n,com.mojang.blaze3d.vertex.VertexFormat f,com.mojang.blaze3d.vertex.VertexFormat.Mode m,int s,boolean c,boolean sorted,Runnable a,Runnable b){super(n,f,m,s,c,sorted,a,b);}
}
