package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.FinalStaffSchool;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety", value = Dist.CLIENT)
public final class FinalStaffSchoolInput {
    private FinalStaffSchoolInput() {}

    @SubscribeEvent public static void overlay(RenderGuiOverlayEvent.Pre event) {
        if (Minecraft.getInstance().screen instanceof FinalStaffSchoolScreen
                && event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) event.setCanceled(true);
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        boolean pressed = false;
        while (FinalStaffSchoolKeys.SWITCH.consumeClick()) pressed = true;
        if (!pressed || mc.screen != null || mc.player == null || mc.player.isSpectator()) return;
        int slot = FinalStaffSchool.isFinalStaff(mc.player.getMainHandItem()) ? mc.player.getInventory().selected
                : FinalStaffSchool.isFinalStaff(mc.player.getOffhandItem()) ? 40 : -1;
        if (slot < 0) return;
        if (mc.player.isUsingItem()) {
            mc.player.displayClientMessage(Component.translatable("message.starfantasy_goety.school_casting"), true);
            return;
        }
        mc.setScreen(new FinalStaffSchoolScreen(slot, mc.player.getInventory().getItem(slot)));
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        if (FinalStaffSchool.isFinalStaff(event.getItemStack())) {
            event.getToolTip().add(Component.translatable("tooltip.starfantasy_goety.staff_school",
                    FinalStaffSchool.get(event.getItemStack()).label()).withStyle(ChatFormatting.GRAY));
        }
    }
}
