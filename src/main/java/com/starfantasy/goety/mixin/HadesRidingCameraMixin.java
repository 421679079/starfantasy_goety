package com.starfantasy.goety.mixin;

import com.starfantasy.goety.client.HadesSeatInterpolation;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.starfantasy.goety.entity.HadesServantEntity;

@Mixin(value = Camera.class, remap = false)
public abstract class HadesRidingCameraMixin {
    @Shadow protected abstract void m_90584_(double x, double y, double z);
    @Shadow private Entity f_90551_;

    // Supply the desired distance before vanilla's eight camera collision rays shorten it.
    @ModifyArg(method = "m_90575_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;m_90566_(D)D", remap = false), index = 0, remap = false)
    private double starfantasy$ridingDistance(double vanillaDistance) {
        return this.f_90551_ != null && this.f_90551_.m_20202_() instanceof HadesServantEntity
                ? 12.0 : vanillaDistance;
    }

    @Redirect(method = "m_90575_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;m_90584_(DDD)V", ordinal = 0, remap = false), remap = false)
    private void starfantasy$seatCamera(Camera camera, double x, double y, double z,
                                       BlockGetter level, Entity cameraEntity,
                                       boolean thirdPerson, boolean frontView, float partialTick) {
        Vec3 offset = HadesSeatInterpolation.renderOffset(cameraEntity, partialTick);
        m_90584_(x + offset.f_82479_, y + offset.f_82480_, z + offset.f_82481_);
    }
}
