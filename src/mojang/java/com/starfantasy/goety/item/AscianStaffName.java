package com.starfantasy.goety.item;

import net.minecraft.network.chat.Component;

public final class AscianStaffName {
    public static Component create() {
        int[] glyphs=Component.translatable("item.starfantasy_goety.ascian_staff").getString().codePoints().toArray();
        var text=Component.empty();
        float center=Math.floorMod(FinalStaffName.time(),2400L)/2400F*1.7F-.35F;
        for(int i=0;i<glyphs.length;i++){
            float position=glyphs.length<=1?0:(float)i/(glyphs.length-1);
            float glow=Math.max(0,1-Math.abs(position-center)/.32F);glow=glow*glow*(3-2*glow);
            int channel=Math.round(170+85*glow),rgb=channel<<16|channel<<8|channel;
            text.append(Component.literal(new String(Character.toChars(glyphs[i]))).withStyle(style->style.withColor(rgb)));
        }
        return text;
    }
    private AscianStaffName(){}
}
