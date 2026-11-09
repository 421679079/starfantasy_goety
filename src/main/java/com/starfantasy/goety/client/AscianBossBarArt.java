package com.starfantasy.goety.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Pixel-aligned HUD art. The redrawn central mask follows the approved model silhouette. */
final class AscianBossBarArt {
    static final int WIDTH=288, HEIGHT=64;
    static final int DISPLAY_WIDTH=204, TOP_SHIFT=8;
    static float displayScale(int screenWidth){return Math.min(DISPLAY_WIDTH,Math.max(1,screenWidth-8))/(float)WIDTH;}
    static float top(int eventY){return Math.max(2,eventY-TOP_SHIFT);}
    static int postureY(int eventY,float scale){return (int)Math.ceil(top(eventY)+HEIGHT*scale+4);}
    private static final int FRAME_HEIGHT=58, FLAME_HEIGHT=20, FLAME_FRAMES=32;
    private static final int[] LIQUID={0xff9b27,0x48e8ea,0xf2f8ff,0xc778fa,0x494253,0xff5145};
    private static final float[] ROW_LIGHT={.45F,1.15F,1F,.92F,.84F,.73F,.62F,.39F};
    private static final ResourceLocation MASK=texture("mask");
    private static final ResourceLocation[] FRAMES=new ResourceLocation[6], FIRE=new ResourceLocation[6];
    static {for(int i=0;i<6;i++){
        FRAMES[i]=texture("frame_"+(i+1));FIRE[i]=texture("fire_"+(i+1));
    }}
    private static ResourceLocation texture(String name){return new ResourceLocation("starfantasy_goety","textures/gui/ascian_king/"+name+".png");}

    static void draw(GuiGraphics gui,int x,int y,float progress,int phase,float age){
        int palette=Math.max(0,Math.min(5,phase-1));
        int end=53+(int)(Math.max(0,Math.min(1,progress))*182);
        gui.m_280262_();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
        // Continuous flame roots attach behind the rails; the upper fringe is less tall.
        for(int side=0;side<2;side++){
            int frame=((int)(age/2)+side*9)&(FLAME_FRAMES-1);
            int height=side==0?12:FLAME_HEIGHT;
            // Overlap the frame by two pixels so the animated root cannot leave an air gap.
            int fy=side==0?y+27-height:y+42;
            gui.m_280411_(FIRE[palette],x+53,fy,182,height,0,(side*FLAME_FRAMES+frame)*FLAME_HEIGHT,
                    182,FLAME_HEIGHT,182,FLAME_HEIGHT*FLAME_FRAMES*2);
        }
        // Health and rails remain unbroken under the mask, including its open eye sockets.
        for(int row=0;row<8;row++)if(end>53)
            gui.m_280509_(x+53,y+31+row,x+end,y+32+row,shade(LIQUID[palette],ROW_LIGHT[row]));
        // Static wings are part of the 256x52 packed frame; keep the existing HUD layout.
        gui.m_280411_(FRAMES[palette],x,y,WIDTH,FRAME_HEIGHT,0,0,256,52,256,52);
        // 125% of the original 40-pixel display size, centered at (144,34).
        gui.m_280411_(MASK,x+119,y+9,50,50,0,0,64,64,64,64);
    }
    private static int shade(int color,float light){
        int r=Math.min(255,Math.round(((color>>16)&255)*light));
        int g=Math.min(255,Math.round(((color>>8)&255)*light));
        int b=Math.min(255,Math.round((color&255)*light));
        return 0xff000000|(r<<16)|(g<<8)|b;
    }
    private AscianBossBarArt(){}
}
