package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApostleMeteorEntity;
import com.starfantasy.library.vfx.client.StarFantasyStarArrowVisualRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the generic renderer supplied by Star Fantasy Library; no bow-upgrade code
 * or assets are referenced.
 */
public final class ApostleMeteorRenderer extends EntityRenderer<ApostleMeteorEntity> {
    public ApostleMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(
            ApostleMeteorEntity arrow,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        if (arrow.isExploded()) {
            renderLingeringTrail(arrow, partialTick, poseStack, buffer);
        } else {
            StarFantasyStarArrowVisualRenderer.renderAtEntityOrigin(
                    arrow,
                    arrow,
                    partialTick,
                    poseStack,
                    buffer,
                    Minecraft.m_91087_().f_91063_.m_109153_());
        }
        super.m_7392_(arrow, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderLingeringTrail(
            ApostleMeteorEntity arrow,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer) {
        float remaining = Mth.m_14036_(
                (ApostleMeteorEntity.EXPLOSION_TRAIL_TICKS
                        - arrow.getExplosionTrailAge() - partialTick)
                        / ApostleMeteorEntity.EXPLOSION_TRAIL_TICKS,
                0.0F,
                1.0F);
        if (remaining <= 0.0F) {
            return;
        }

        Vec3 origin = new Vec3(
                Mth.m_14139_(partialTick, arrow.f_19854_, arrow.m_20185_()),
                Mth.m_14139_(partialTick, arrow.f_19855_, arrow.m_20186_()),
                Mth.m_14139_(partialTick, arrow.f_19856_, arrow.m_20189_()));
        List<Vec3> localTrail = new ArrayList<>(arrow.starFantasyStarArrowTrailCount());
        for (int i = 0; i < arrow.starFantasyStarArrowTrailCount(); ++i) {
            Vec3 point = arrow.starFantasyStarArrowTrailPoint(i);
            localTrail.add(new Vec3(
                    point.f_82479_ - origin.f_82479_,
                    point.f_82480_ - origin.f_82480_,
                    point.f_82481_ - origin.f_82481_));
        }

        int color = arrow.starFantasyStarArrowColor();
        StarFantasyStarArrowVisualRenderer.renderWorldTrail(
                localTrail,
                poseStack,
                buffer,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                0.52F * remaining,
                Math.max(0.24F, arrow.starFantasyStarArrowVisualScale() * 0.55F));
    }

    @Override
    public ResourceLocation m_5478_(ApostleMeteorEntity entity) {
        return StarFantasyStarArrowVisualRenderer.starTexture();
    }
}
