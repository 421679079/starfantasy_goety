package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfantasy.goety.entity.ApollyonSectorEffectEntity;
import com.starfantasy.library.vfx.client.StarFantasyVfxRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Draws configurable radial warning sectors and the short corrupted-beam impact. */
public final class ApollyonSectorEffectRenderer
        extends EntityRenderer<ApollyonSectorEffectEntity> {
    private static final ResourceLocation WHITE_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/white_concrete.png");
    private static final ResourceLocation BEAM_GLOW =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_glow.png");
    private static final ResourceLocation BEAM_MAIN =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_main.png");
    private static final ResourceLocation BEAM_CORE =
            new ResourceLocation("goety", "textures/entity/corrupted/beacon_beam_core.png");
    private static final int FULL_BRIGHT = 0xF000F0;

    public ApollyonSectorEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void m_7392_(ApollyonSectorEffectEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float age = entity.getVisualAge(partialTick);
        if (age < entity.getDuration()) {
            if (entity.getMode() == ApollyonSectorEffectEntity.MODE_VOID_RAY) {
                this.renderVoidRay(entity, age, poseStack, buffer);
            } else {
                this.renderWarning(entity, age, poseStack, buffer);
            }
        }
        super.m_7392_(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonSectorEffectEntity entity) {
        return entity.getMode() == ApollyonSectorEffectEntity.MODE_VOID_RAY
                ? BEAM_MAIN : WHITE_TEXTURE;
    }

    private void renderWarning(ApollyonSectorEffectEntity entity, float age,
                               PoseStack poseStack, MultiBufferSource buffer) {
        float duration = Math.max(1.0F, entity.getDuration());
        float fraction = Mth.m_14036_(age / duration, 0.0F, 1.0F);
        float eased = Mth.m_14031_(fraction * (float) (Math.PI * 0.5D));
        float visibleAngle = entity.getSectorAngle() * eased * eased;
        float fadeIn = Mth.m_14036_(age / 2.0F, 0.0F, 1.0F);
        float pulse = 0.9F + 0.1F * Mth.m_14031_(age * 0.9F);
        float[] rgb = rgb(entity.getColor());

        VertexConsumer consumer = buffer.m_6299_(
                StarFantasyVfxRenderTypes.translucentPositionColor());
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        drawPatternColor(consumer, matrix, entity,
                entity.getRadius(), entity.getSectorAngle(), 0.025F,
                rgb[0], rgb[1], rgb[2], 0.12F * fadeIn);
        if (visibleAngle > 0.02F) {
            drawPatternColor(consumer, matrix, entity,
                    entity.getRadius(), visibleAngle, 0.045F,
                    rgb[0], rgb[1], rgb[2], 0.46F * fadeIn * pulse);
        }
    }

    private void renderVoidRay(ApollyonSectorEffectEntity entity, float age,
                               PoseStack poseStack, MultiBufferSource buffer) {
        float visibleAngle;
        float alpha;
        if (age < 5.0F) {
            float grow = Mth.m_14036_(age / 5.0F, 0.0F, 1.0F);
            float easeOut = 1.0F - (float) Math.pow(1.0F - grow, 3.0D);
            visibleAngle = entity.getSectorAngle() * easeOut;
            alpha = grow;
        } else {
            float shrink = Mth.m_14036_((age - 5.0F) / 10.0F, 0.0F, 1.0F);
            visibleAngle = entity.getSectorAngle() * (1.0F - shrink);
            alpha = 1.0F - shrink;
        }
        if (visibleAngle <= 0.02F || alpha <= 0.01F) {
            return;
        }

        float scroll = -(entity.m_9236_().m_46467_() + age) * 0.06F;
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        drawPatternTexture(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_GLOW)),
                matrix, entity, visibleAngle * 1.12F, 0.10F,
                scroll, 0.58F * alpha);
        drawPatternTexture(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_MAIN)),
                matrix, entity, visibleAngle, 0.135F,
                scroll * 1.35F, 0.86F * alpha);
        drawPatternTexture(buffer.m_6299_(StarFantasyVfxRenderTypes.depthParticle(BEAM_CORE)),
                matrix, entity, visibleAngle * 0.56F, 0.17F,
                scroll * 1.8F, alpha);
    }

    private static void drawPatternColor(VertexConsumer consumer, Matrix4f matrix,
                                         ApollyonSectorEffectEntity entity,
                                         float radius, float angleWidth, float y,
                                         float red, float green, float blue, float alpha) {
        for (int sector = 0; sector < entity.getSectorCount(); ++sector) {
            float center = entity.getRotationOffset() + sector * entity.getSectorSpacing();
            drawSectorColor(consumer, matrix, radius, angleWidth, center,
                    y, red, green, blue, alpha);
        }
    }

    private static void drawSectorColor(VertexConsumer consumer, Matrix4f matrix,
                                        float radius, float angleWidth, float centerAngle,
                                        float y, float red, float green, float blue, float alpha) {
        int segments = Math.max(1, Mth.m_14167_(
                angleWidth / ApollyonSectorEffectEntity.TESSELLATION_DEGREES));
        float start = centerAngle - angleWidth * 0.5F;
        for (int segment = 0; segment < segments; ++segment) {
            float angleA = start + angleWidth * segment / segments;
            float angleB = start + angleWidth * (segment + 1) / segments;
            float xA = radialX(angleA, radius);
            float zA = radialZ(angleA, radius);
            float xB = radialX(angleB, radius);
            float zB = radialZ(angleB, radius);
            colorVertex(consumer, matrix, 0.0F, y, 0.0F, red, green, blue, alpha);
            colorVertex(consumer, matrix, xA, y, zA, red, green, blue, alpha);
            colorVertex(consumer, matrix, xB, y, zB, red, green, blue, alpha);
            colorVertex(consumer, matrix, 0.0F, y, 0.0F, red, green, blue, alpha);
        }
    }

    private static void drawPatternTexture(VertexConsumer consumer, Matrix4f matrix,
                                           ApollyonSectorEffectEntity entity,
                                           float angleWidth, float y,
                                           float scroll, float alpha) {
        for (int sector = 0; sector < entity.getSectorCount(); ++sector) {
            float center = entity.getRotationOffset() + sector * entity.getSectorSpacing();
            drawSectorTexture(consumer, matrix, entity.getRadius(), angleWidth,
                    center, y, scroll, alpha);
        }
    }

    private static void drawSectorTexture(VertexConsumer consumer, Matrix4f matrix,
                                          float radius, float angleWidth, float centerAngle,
                                          float y, float scroll, float alpha) {
        int segments = Math.max(1, Mth.m_14167_(
                angleWidth / ApollyonSectorEffectEntity.TESSELLATION_DEGREES));
        float start = centerAngle - angleWidth * 0.5F;
        float farV = scroll + radius * 0.18F;
        for (int segment = 0; segment < segments; ++segment) {
            float angleA = start + angleWidth * segment / segments;
            float angleB = start + angleWidth * (segment + 1) / segments;
            float uA = segment / (float) segments;
            float uB = (segment + 1) / (float) segments;
            float xA = radialX(angleA, radius);
            float zA = radialZ(angleA, radius);
            float xB = radialX(angleB, radius);
            float zB = radialZ(angleB, radius);
            textureVertex(consumer, matrix, 0.0F, y, 0.0F, uA, scroll, alpha);
            textureVertex(consumer, matrix, xA, y, zA, uA, farV, alpha);
            textureVertex(consumer, matrix, xB, y, zB, uB, farV, alpha);
            textureVertex(consumer, matrix, 0.0F, y, 0.0F, uB, scroll, alpha);
        }
    }

    private static float radialX(float degrees, float radius) {
        return Mth.m_14031_(degrees * ((float) Math.PI / 180.0F)) * radius;
    }

    private static float radialZ(float degrees, float radius) {
        return Mth.m_14089_(degrees * ((float) Math.PI / 180.0F)) * radius;
    }

    private static float[] rgb(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }

    private static void colorVertex(VertexConsumer consumer, Matrix4f matrix,
                                    float x, float y, float z,
                                    float red, float green, float blue, float alpha) {
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(red, green, blue, alpha)
                .m_5752_();
    }

    private static void textureVertex(VertexConsumer consumer, Matrix4f matrix,
                                      float x, float y, float z,
                                      float u, float v, float alpha) {
        consumer.m_252986_(matrix, x, y, z)
                .m_85950_(1.0F, 1.0F, 1.0F, alpha)
                .m_7421_(u, v)
                .m_85969_(FULL_BRIGHT)
                .m_5752_();
    }
}
