package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.client.gui.radial.GenericRadialMenu;
import com.starfantasy.goety.client.FinalStaffSchoolScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Change only our wheel; Goety's focus and brew wheels retain their geometry. */
@Mixin(value = GenericRadialMenu.class, remap = false)
public abstract class FinalStaffSchoolWheelMixin {
    @Inject(method = "draw", at = @At(value = "FIELD",
            target = "Lcom/Polarice3/Goety/client/gui/radial/GenericRadialMenu;itemRadius:F",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER), remap = false)
    private void starfantasy$schoolRadii(GuiGraphics graphics, float partialTick, int mouseX, int mouseY,
                                        CallbackInfo callback) {
        GenericRadialMenu wheel = (GenericRadialMenu) (Object) this;
        if (wheel.host instanceof FinalStaffSchoolScreen screen) screen.configureWheelRadii(wheel, false);
    }

    @Inject(method = "processMouse", at = @At("HEAD"), remap = false)
    private void starfantasy$schoolHitRadii(int mouseX, int mouseY, CallbackInfo callback) {
        // Native hit testing precedes this frame's radius assignments. Set the
        // same ready-state geometry here, including the first frame after resize.
        GenericRadialMenu wheel = (GenericRadialMenu) (Object) this;
        if (wheel.host instanceof FinalStaffSchoolScreen screen) screen.configureWheelRadii(wheel, true);
    }
}
