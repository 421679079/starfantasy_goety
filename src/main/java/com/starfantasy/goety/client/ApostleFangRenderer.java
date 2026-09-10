package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.world.entity.projectile.EvokerFangs;

public final class ApostleFangRenderer extends EvokerFangsRenderer {
    public ApostleFangRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void m_7392_(EvokerFangs fang, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffer, int light) {
        pose.m_85836_();
        pose.m_85841_(1.5F, 1.5F, 1.5F);
        super.m_7392_(fang, yaw, partialTick, pose, buffer, light);
        pose.m_85849_();
    }
}
