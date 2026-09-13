package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonFamineWaveEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Expands the generated hollow poison-fog ring to the arena radius in ten ticks. */
public final class ApollyonFamineWaveRenderer
        extends EntityRenderer<ApollyonFamineWaveEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            StarFantasyGoetyMod.MODID, "textures/particle/apollyon_famine_wave.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public ApollyonFamineWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.0F; // Visual-only effect: no vanilla ground shadow.
    }

    @Override
    public void m_7392_(ApollyonFamineWaveEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float progress = Mth.m_14036_(
                entity.visualAge(partialTick) / ApollyonFamineWaveEntity.LIFETIME_TICKS,
                0.0F, 1.0F);
        if (progress < 1.0F) {
            float eased = Mth.m_14031_(progress * (float) (Math.PI * 0.5D));
            float radius = ApollyonFamineWaveEntity.MAX_RADIUS * eased;
            float fade = progress <= 0.65F
                    ? 1.0F
                    : 1.0F - (progress - 0.65F) / 0.35F;
            VertexConsumer consumer = buffer.m_6299_(
                    StarFantasyVfxRenderTypes.depthParticle(TEXTURE));
            Matrix4f matrix = poseStack.m_85850_().m_252922_();
            vertex(consumer, matrix, -radius, 0.08F, -radius, 0.0F, 0.0F, fade);
            vertex(consumer, matrix, -radius, 0.08F, radius, 0.0F, 1.0F, fade);
            vertex(consumer, matrix, radius, 0.08F, radius, 1.0F, 1.0F, fade);
            vertex(consumer, matrix, radius, 0.08F, -radius, 1.0F, 0.0F, fade);
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonFamineWaveEntity entity) {
        return TEXTURE;
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float x, float y, float z, float u, float v, float alpha) {
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(1.0F, 1.0F, 1.0F, alpha)
                .m_7421_(u, v)
                .m_85969_(FULL_BRIGHT)
                .m_5752_();
    }
}
