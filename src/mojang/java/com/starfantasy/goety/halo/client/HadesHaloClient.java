package com.starfantasy.goety.halo.client;

import com.starfantasy.goety.halo.HadesHaloContent;
import com.starfantasy.goety.halo.HadesHaloContents;
import com.starfantasy.goety.halo.HadesHaloMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class HadesHaloClient {
    @SubscribeEvent public static void rightClick(ScreenEvent.MouseButtonPressed.Pre event){
        if(event.getButton()!=1||!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                ||screen.getMenu() instanceof HadesHaloMenu)return;
        var player=Minecraft.getInstance().player;var slot=screen.getSlotUnderMouse();
        if(player==null||slot==null||slot.container!=player.getInventory()||!slot.mayPickup(player)
                ||!screen.getMenu().getCarried().isEmpty()||!HadesHaloContents.isHades(slot.getItem()))return;
        event.setCanceled(true);
        HadesHaloContent.CHANNEL.sendToServer(new HadesHaloContent.OpenRequest(screen.getMenu().containerId,slot.getContainerSlot()));
    }
    @Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->MenuScreens.register(HadesHaloContent.MENU.get(),HadesHaloScreen::new));}
    }
    private HadesHaloClient(){}
}
