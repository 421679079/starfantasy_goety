package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.client.ApostleShieldRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Covers both the native renderer and Doki's GeoReplacedEntityRenderer. */
@Mixin(value = EntityRenderDispatcher.class, remap = false)
public abstract class ApostleShieldRenderMixin {
    @Inject(method = "m_114384_", at = @At("TAIL"))
    private void starfantasy$shield(Entity entity, double x, double y, double z, float yaw,
            float partialTick, PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (entity instanceof Apostle boss)
            ApostleShieldRenderer.render(boss, x, y, z, partialTick, pose, buffer);
    }
}
