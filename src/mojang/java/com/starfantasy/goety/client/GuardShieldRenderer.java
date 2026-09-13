package com.starfantasy.goety.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.starfantasy.goety.magic.guard.GuardChannel;
import com.starfantasy.goety.magic.guard.GuardFocusContent;
import com.starfantasy.goety.magic.guard.GuardShieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One textured light curtain with independently animated expanding ripples. */
public final class GuardShieldRenderer extends EntityRenderer<GuardShieldEntity> {
    public GuardShieldRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(GuardShieldEntity shield) { return GuardShieldTextures.TEXTURE; }
    @Override public void render(GuardShieldEntity shield, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        LivingEntity owner = shield.owner();
        if (owner == null || !owner.isUsingItem() || !GuardChannel.isGuardStaff(owner.getUseItem())) return;
        Vec3 centre = owner.getEyePosition(partial).add(owner.getViewVector(partial).scale(1.5D));
        Vec3 drawnAt = new Vec3(Mth.lerp(partial, shield.xOld, shield.getX()),
                Mth.lerp(partial, shield.yOld, shield.getY()), Mth.lerp(partial, shield.zOld, shield.getZ()));
        Vec3 offset = centre.subtract(drawnAt);
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);
        pose.mulPose(Axis.YP.rotationDegrees(-owner.getViewYRot(partial)));
        pose.mulPose(Axis.XP.rotationDegrees(owner.getViewXRot(partial)));
        pose.scale(.9F, .9F, .9F);
        Minecraft mc = Minecraft.getInstance();
        float opacity = owner == mc.player && mc.options.getCameraType().isFirstPerson() ? .68F : 1;
        float age = shield.age(partial);
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotation(age * GuardShieldGeometry.ROTATION_RADIANS_PER_TICK));
        float size = GuardShieldMask.EXTENT * GuardShieldGeometry.opening(age);
        VertexConsumer curtain = buffers.getBuffer(CurtainRenderType.TEXTURED_LIGHT);
        int alpha = Math.round(255 * opacity);
        curtainVertex(curtain, pose, -size, -size, 0, 1, alpha);
        curtainVertex(curtain, pose, size, -size, 1, 1, alpha);
        curtainVertex(curtain, pose, size, size, 1, 0, alpha);
        curtainVertex(curtain, pose, -size, size, 0, 0, alpha);
        pose.popPose();
        VertexConsumer out = buffers.getBuffer(CurtainRenderType.LIGHT);
        GuardShieldGeometry.drawRipples(age, opacity, (x, y, z, r, g, b, a) ->
                out.vertex(pose.last().pose(), x, y, z).color(r, g, b, a).endVertex());
        pose.popPose();
    }
    private static void curtainVertex(VertexConsumer out, PoseStack pose, float x, float y, float u, float v, int alpha) {
        out.vertex(pose.last().pose(), x, y, 0).uv(u, v).color(255, 36, 200, alpha).endVertex();
    }
    private static final class CurtainRenderType extends RenderType {
        private static final RenderType TEXTURED_LIGHT = create("starfantasy_goety:guard_textured_light_curtain",
                // Match the shader's attribute order, including under Oculus.
                DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
                CompositeState.builder().setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                        .setTextureState(new TextureStateShard(GuardShieldTextures.TEXTURE, true, false))
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
        // Additive, unlit color makes the curtain luminous. No overlapping layers or quad sorting.
        private static final RenderType LIGHT = create("starfantasy_goety:guard_light_curtain",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 262144, false, false,
                CompositeState.builder().setShaderState(POSITION_COLOR_SHADER)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY).setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
        private CurtainRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int size,
                                  boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }
    }
    @Mod.EventBusSubscriber(modid = "starfantasy_goety", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void textures(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new GuardShieldTextures());
        }
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(GuardFocusContent.SHIELD.get(), GuardShieldRenderer::new);
        }
    }
}
