package com.starfantasy.goety.mixin;

import com.starfantasy.goety.client.HadesSeatInterpolation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerRenderer.class, remap = false)
public abstract class HadesRidingPlayerRenderMixin {
    @Inject(method = "m_7860_(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"), cancellable = true, remap = false)
    private void starfantasy$seatPlayer(AbstractClientPlayer player, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        cir.setReturnValue(cir.getReturnValue().m_82549_(HadesSeatInterpolation.renderOffset(player, partialTick)));
    }
}
