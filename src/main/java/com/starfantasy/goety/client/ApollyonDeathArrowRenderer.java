package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.DeathArrowRenderer;
import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApollyonDeathArrowEntity;
import com.starfantasy.library.vfx.client.StarFantasyStarArrowVisualRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Retains Goety's death-arrow model and adds the Star Arrow ribbon trail. */
public final class ApollyonDeathArrowRenderer extends DeathArrowRenderer {
    private static final float TRAIL_ALPHA = 0.72F;
    private static final float TRAIL_WIDTH = 0.10F;
    private static final float ARROW_BODY_SCALE = 1.5F;
    private static final double TRAIL_HEAD_OFFSET = -0.4D;

    public ApollyonDeathArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(
            DeathArrow arrow,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        if (!(arrow instanceof ApollyonDeathArrowEntity pageantArrow)) {
            super.m_7392_(arrow, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        Vec3 origin = new Vec3(
                Mth.m_14139_(partialTick, arrow.f_19854_, arrow.m_20185_()),
                Mth.m_14139_(partialTick, arrow.f_19855_, arrow.m_20186_()),
                Mth.m_14139_(partialTick, arrow.f_19856_, arrow.m_20189_()));
        int trailCount = pageantArrow.starFantasyStarArrowTrailCount();
        List<Vec3> localTrail = new ArrayList<>(trailCount);
        Vec3 movement = new Vec3(
                arrow.m_20185_() - arrow.f_19854_,
                arrow.m_20186_() - arrow.f_19855_,
                arrow.m_20189_() - arrow.f_19856_);
        localTrail.add(movement.m_82490_(TRAIL_HEAD_OFFSET));
        for (int i = 1; i < trailCount; ++i) {
            Vec3 point = pageantArrow.starFantasyStarArrowTrailPoint(i);
            localTrail.add(new Vec3(
                    point.f_82479_ - origin.f_82479_,
                    point.f_82480_ - origin.f_82480_,
                    point.f_82481_ - origin.f_82481_));
        }
        int color = pageantArrow.starFantasyStarArrowColor();
        StarFantasyStarArrowVisualRenderer.renderWorldTrailDepthTested(
                localTrail, poseStack, buffer,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                TRAIL_ALPHA, TRAIL_WIDTH);
        poseStack.m_85836_();
        poseStack.m_85841_(ARROW_BODY_SCALE, ARROW_BODY_SCALE, ARROW_BODY_SCALE);
        super.m_7392_(arrow, yaw, partialTick, poseStack, buffer, packedLight);
        poseStack.m_85849_();
    }
}
