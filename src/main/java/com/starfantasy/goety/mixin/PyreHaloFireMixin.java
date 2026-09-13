package com.starfantasy.goety.mixin;

import com.starfantasy.goety.registry.HaloItemRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(value = Entity.class, remap = false)
public abstract class PyreHaloFireMixin {
    // setRemainingFireTicks: prevent ignition before lava/fire can set the burning flag.
    @ModifyVariable(method = "m_7311_(I)V", at = @At("HEAD"), argsOnly = true, remap = false)
    private int starfantasy$preventPyreHaloIgnition(int ticks) {
        return ticks > 0 && starfantasy$wearsPyreHalo() ? 0 : ticks;
    }

    // isOnFire: also suppress an older burning flag still arriving on the client.
    @Inject(method = "m_6060_()Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void starfantasy$suppressPyreHaloFire(CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() && starfantasy$wearsPyreHalo()) {
            callback.setReturnValue(false);
        }
    }

    @Unique
    private boolean starfantasy$wearsPyreHalo() {
        if (!((Object) this instanceof Player player)) {
            return false;
        }
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(inventory -> inventory.findFirstCurio(HaloItemRegistry.HALO_OF_THE_PYRE_LORD.get()))
                .isPresent();
    }
}
