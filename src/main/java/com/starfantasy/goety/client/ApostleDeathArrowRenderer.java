package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.DeathArrowRenderer;
import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import com.starfantasy.goety.config.ApostleConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Visual override for the original death-arrow type; projectile logic stays in Goety. */
public final class ApostleDeathArrowRenderer extends DeathArrowRenderer {
    public ApostleDeathArrowRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void m_7392_(DeathArrow arrow, float yaw, float partialTick,
                                 PoseStack pose, MultiBufferSource buffer, int light) {
        if (!ApostleConfig.IMPROVED_ARCHERY.get()) {
            super.m_7392_(arrow, yaw, partialTick, pose, buffer, light);
            return;
        }
        ApostleArrowTrail.render(arrow, partialTick, pose, buffer);
        pose.m_85836_();
        pose.m_85841_(1.5F, 1.5F, 1.5F);
        super.m_7392_(arrow, yaw, partialTick, pose, buffer, light);
        pose.m_85849_();
    }
}
