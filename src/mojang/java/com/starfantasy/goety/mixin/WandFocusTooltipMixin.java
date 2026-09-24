package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.items.magic.DarkWand;
import com.starfantasy.goety.client.FocusTooltip;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DarkWand.class, remap = false)
public abstract class WandFocusTooltipMixin {
    @Inject(method = "addInformationAfterShift", at = @At("HEAD"), cancellable = true, remap = false)
    private void starfantasy$descriptionLines(Item focus, List<Component> tooltip, CallbackInfo callback) {
        if (FocusTooltip.append(focus, tooltip)) callback.cancel();
    }
}
