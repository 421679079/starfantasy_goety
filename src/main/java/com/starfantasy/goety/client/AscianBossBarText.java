package com.starfantasy.goety.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Two inward-aligned labels, using VoidWormBossBarRenderer's per-glyph cosine shimmer. */
final class AscianBossBarText {
    private static final int[][] COLORS={
            {0xe78025,0xffc36b,0xfff1ce},{0x35becb,0x88f1f0,0xe4ffff},
            {0xabbacb,0xe5f0ff,0xffffff},{0x9760cf,0xd5a0fa,0xf7e5ff},
            {0x655e72,0x968ba5,0xcdc4da},{0xd43a39,0xff8b79,0xffdfce}
    };
    static String[] split(String full){
        int start=full.lastIndexOf('「'),end=full.lastIndexOf('」');
        if(start<0||end<=start)return new String[]{full.trim(),""};
        return new String[]{full.substring(0,start).trim(),full.substring(start+1,end).trim()};
    }
    static void draw(GuiGraphics gui,Font font,String full,float center,float top,float artScale,int phase,double seconds){
        String[] labels=split(full);
        int longest=Math.max(font.m_92895_(labels[0]),font.m_92895_(labels[1]));
        float fontScale=Math.min(1F,65*artScale/Math.max(1,longest));
        float textTop=Math.max(2,top+21*artScale-font.f_92710_*fontScale-1);
        float leftEnd=center-26*artScale,rightStart=center+26*artScale;
        label(gui,font,labels[0],leftEnd-font.m_92895_(labels[0])*fontScale,textTop,fontScale,phase,seconds);
        label(gui,font,labels[1],rightStart,textTop,fontScale,phase,seconds);
    }
    private static void label(GuiGraphics gui,Font font,String text,float x,float y,float scale,int phase,double seconds){
        var pose=gui.m_280168_();pose.m_85836_();
        try {
            pose.m_85837_(x,y,0);pose.m_85841_(scale,scale,1);
            int pen=0;
            for(int code:text.codePoints().toArray()){
                String glyph=new String(Character.toChars(code));int width=font.m_92895_(glyph);
                gui.m_280056_(font,glyph,pen,0,0xff000000|color((x/scale)+pen+width*.5,seconds,phase),true);
                pen+=width;
            }
        } finally {pose.m_85849_();}
    }
    static int color(double x,double seconds,int phase){
        double p=(1+Math.cos(x*.13-seconds*2.7))*.5;
        p=p*p*(3-2*p);
        int part=p<.5?0:1;double t=p<.5?p*2:p*2-1;
        int[] colors=COLORS[Math.max(0,Math.min(5,phase-1))];int color=0;
        for(int shift:new int[]{16,8,0}){
            int a=(colors[part]>>shift)&255,b=(colors[part+1]>>shift)&255;
            color|=(int)Math.round(a+(b-a)*t)<<shift;
        }
        return color;
    }
    private AscianBossBarText(){}
}
