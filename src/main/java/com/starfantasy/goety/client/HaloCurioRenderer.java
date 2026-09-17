package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** The original model's halo texture on a fixed, double-sided plane behind the head. */
public final class HaloCurioRenderer implements ICurioRenderer {
    private final ResourceLocation texture;
    private final float u0, v0, u1, v1, width, height;

    public HaloCurioRenderer(String id) {
        if (id.equals("halo_of_hades")) {
            texture = new ResourceLocation("starfantasy_goety", "textures/entity/hades/hades.png");
            // hades_apollyon_eye_halo: use its original 47 x 19 atlas region and aspect ratio.
            u0 = 704 / 1024F; v0 = 256 / 1024F;
            u1 = 751 / 1024F; v1 = 275 / 1024F;
            // Match Apollyon's ysmGlowbone24 plane (62.66667 x 25.33333 model units)
            // at the entity renderer's 0.65 scale, rather than the ordinary apostle halo size.
            width = 62.66667F * 0.65F / 16F; height = width * 19 / 47;
        } else {
            texture = new ResourceLocation("starfantasy_goety",
                    "textures/entity/apostle/" + id.replace("halo_of_", "apostle_") + ".png");
            int atlasWidth = id.equals("halo_of_the_great_shadow") ? 512 : 256;
            // All twelve halo1 planes use UV (192, 0), size (64, 64), on their own entity atlas.
            u0 = 192F / atlasWidth; v0 = 0;
            u1 = 256F / atlasWidth; v1 = 64 / 512F;
            width = height = 12 / 16F;
        }
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext context, PoseStack pose, RenderLayerParent<T, M> parent,
            MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!context.visible() || !(parent.m_7200_() instanceof HeadedModel headModel)) return;
        pose.m_85836_();
        headModel.m_5585_().m_104299_(pose);
        // Apostle a_head -> halo: pivot delta (0, +12, +5) in Bedrock model units.
        // Minecraft's model Y points down; +Z stays behind the head. No bob, tilt or spin.
        pose.m_85837_(0, -12 / 16D, 5 / 16D);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        strip(buffer.m_6299_(HaloCurioRenderType.get(texture, false)), matrix, normal, 0, 1, 1, 1);

        // At most two coplanar strips brighten the same UVs. No new texture, mesh or particles.
        float time = context.entity().f_19797_ + partialTick;
        float center = (time % 120) / 120F * 1.4F - 0.2F;
        if (center + 0.16F > 0 && center - 0.16F < 1) {
            VertexConsumer glow = buffer.m_6299_(HaloCurioRenderType.get(texture, true));
            strip(glow, matrix, normal, center - 0.16F, center, 0, 0.55F);
            strip(glow, matrix, normal, center, center + 0.16F, 0.55F, 0);
        }
        pose.m_85849_();
    }

    private void strip(VertexConsumer out, Matrix4f matrix, Matrix3f normal,
                       float left, float right, float alphaLeft, float alphaRight) {
        float start = Math.max(0, left), end = Math.min(1, right);
        if (start >= end) return;
        float a = alphaLeft + (alphaRight - alphaLeft) * (start - left) / (right - left);
        float b = alphaLeft + (alphaRight - alphaLeft) * (end - left) / (right - left);
        vertex(out, matrix, normal, start, 0, a);
        vertex(out, matrix, normal, start, 1, a);
        vertex(out, matrix, normal, end, 1, b);
        vertex(out, matrix, normal, end, 0, b);
    }

    private void vertex(VertexConsumer out, Matrix4f matrix, Matrix3f normal, float x, float y, float alpha) {
        out.m_252986_(matrix, (x - 0.5F) * width, (y - 0.5F) * height, 0)
                .m_85950_(1, 1, 1, alpha)
                .m_7421_(u0 + (u1 - u0) * x, v0 + (v1 - v0) * y)
                .m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0)
                .m_252939_(normal, 0, 0, -1).m_5752_();
    }
}
