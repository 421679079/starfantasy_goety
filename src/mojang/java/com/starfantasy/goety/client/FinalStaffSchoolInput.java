package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.FinalStaffSchool;
import com.starfantasy.goety.magic.AscianStaffSchool;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
        boolean ascianPressed=false;
        while(FinalStaffSchoolKeys.ASCIAN_SWITCH.consumeClick())ascianPressed=true;
        if ((!pressed&&!ascianPressed) || mc.screen != null || mc.player == null || mc.player.isSpectator()) return;
        int slot = selectable(mc.player.getMainHandItem(),pressed,ascianPressed) ? mc.player.getInventory().selected
                : selectable(mc.player.getOffhandItem(),pressed,ascianPressed) ? 40 : -1;
        if (slot < 0) return;
        if (mc.player.isUsingItem()) {
            boolean ascian=AscianStaffSchool.isAscian(mc.player.getInventory().getItem(slot));
            mc.player.displayClientMessage(Component.translatable(ascian?"message.starfantasy_goety.ascian_school_casting":"message.starfantasy_goety.school_casting"), true);
            return;
        }
        mc.setScreen(new FinalStaffSchoolScreen(slot, mc.player.getInventory().getItem(slot)));
    }

    private static boolean selectable(net.minecraft.world.item.ItemStack stack,boolean finalPressed,boolean ascianPressed){
        return finalPressed&&FinalStaffSchool.isFinalStaff(stack)||ascianPressed&&AscianStaffSchool.isAscian(stack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) public static void tooltip(ItemTooltipEvent event) {
        if (AscianStaffSchool.isAscian(event.getItemStack())) {
            FinalStaffSchoolTooltip.append(event.getToolTip(), event.getItemStack(),
                    FinalStaffSchoolKeys.ASCIAN_SWITCH.getTranslatedKeyMessage());
        } else if (FinalStaffSchool.isFinalStaff(event.getItemStack())) {
            FinalStaffSchoolTooltip.append(event.getToolTip(), event.getItemStack(),
                    FinalStaffSchoolKeys.SWITCH.getTranslatedKeyMessage());
        }
    }
}
