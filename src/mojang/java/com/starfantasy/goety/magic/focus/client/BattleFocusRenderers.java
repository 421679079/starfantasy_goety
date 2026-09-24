package com.starfantasy.goety.magic.focus.client;

import com.starfantasy.goety.magic.focus.BattleFocusContent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BattleFocusRenderers {
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BattleFocusContent.FLOWER_CASTING.get(), FlowerCastingRenderer::new);
        event.registerEntityRenderer(BattleFocusContent.FLOWER_ARROW.get(), FlowerArrowRenderer::new);
        event.registerEntityRenderer(BattleFocusContent.FLOWER_BURST_RIBBON.get(), FlowerBurstRibbonRenderer::new);
        event.registerEntityRenderer(BattleFocusContent.EVERNIGHT_CAGE.get(), EvernightCageRenderer::new);
        event.registerEntityRenderer(BattleFocusContent.FINAL_ART_EFFECT.get(), FinalArtRenderer::new);
    }
}
