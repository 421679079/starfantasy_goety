package com.starfantasy.goety.halo.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/** Goety 2.5.60 UnholyHatModel: (-13, +5), 45-degree X tilt, age * .01 Z spin. */
public final class ApostleHaloPose {
    public static void apply(PoseStack pose,float ticks){
        pose.translate(0,-13/16D,5/16D);
        pose.mulPose(Axis.XP.rotation(.7854F));pose.mulPose(Axis.ZP.rotation(ticks*.01F));
    }
    private ApostleHaloPose(){}
}
