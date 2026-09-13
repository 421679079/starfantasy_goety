package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.guard.GuardChannel;
import com.starfantasy.goety.magic.guard.GuardRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client input debounce only: cooldown, fuel and blocking remain server authoritative. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety", value = Dist.CLIENT)
public final class GuardUseInput {
    private static final GuardRules.UsePress PRESS = new GuardRules.UsePress();
    private static LocalPlayer lastPlayer;
    private GuardUseInput() {}

    public static void attempted(Player player, ItemStack staff) {
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && GuardChannel.isGuardStaff(staff)) {
            lastPlayer = mc.player;
            PRESS.started();
        }
    }

    @SubscribeEvent public static void use(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.isUseItem() && mc.player != null && mc.screen == null
                && PRESS.blocks(GuardChannel.isGuardStaff(mc.player.getItemInHand(event.getHand())))) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    // Raw release events also catch release/re-press pairs occurring between game ticks.
    @SubscribeEvent public static void mouse(InputEvent.MouseButton.Post event) {
        if (event.getAction() == GLFW.GLFW_RELEASE
                && Minecraft.getInstance().options.keyUse.matchesMouse(event.getButton())) PRESS.release();
    }
    @SubscribeEvent public static void key(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_RELEASE
                && Minecraft.getInstance().options.keyUse.matches(event.getKey(), event.getScanCode())) PRESS.release();
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != lastPlayer || mc.player == null || !mc.isWindowActive()
                || !mc.options.keyUse.isDown()) PRESS.release();
        lastPlayer = mc.player;
    }
}
