package com.starfantasy.goety.client;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.combat.apostle.ApostleCastAccess;
import com.starfantasy.goety.combat.apostle.ApostleSpellSupport;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

/** Uses the same sphere geometry as Apollyon, independently of the installed Apostle model. */
public final class ApostleShieldRenderer {
    private ApostleShieldRenderer() {}
    public static void render(Apostle boss, double x, double y, double z, float partialTick,
                              PoseStack pose, MultiBufferSource buffer) {
        if (!ApostleSpellSupport.original(boss) || !boss.m_6084_()
                || !((ApostleCastAccess) boss).starfantasy$shieldVisible()) return;
        float age = boss.f_19797_ + partialTick;
        pose.m_85836_();
        pose.m_85837_(x, y + 1.15, z);
        ApollyonGloriousSphereRenderer.renderSphere(
                buffer.m_6299_(StarFantasyVfxRenderTypes.translucentPositionColor()),
                pose.m_85850_().m_252922_(), 1.55F + .025F * Mth.m_14031_(age * .22F),
                1, .82F, .12F, .22F + .035F * Mth.m_14031_(age * .15F));
        pose.m_85849_();
    }
}
