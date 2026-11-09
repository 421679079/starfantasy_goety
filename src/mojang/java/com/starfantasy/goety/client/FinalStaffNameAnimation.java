package com.starfantasy.goety.client;

import com.starfantasy.goety.item.FinalStaffName;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "starfantasy_goety", value = Dist.CLIENT)
public final class FinalStaffNameAnimation {
    @SubscribeEvent public static void frame(TickEvent.RenderTickEvent event) {
        // All name queries within a frame/tick see identical styles, including hotbar comparisons.
        if (event.phase == TickEvent.Phase.START) FinalStaffName.frame(Util.getMillis());
    }

    private FinalStaffNameAnimation() {}
}
