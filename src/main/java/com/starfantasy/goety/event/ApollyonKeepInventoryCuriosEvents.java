package com.starfantasy.goety.event;

import com.starfantasy.goety.combat.ApollyonDeathInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ApollyonKeepInventoryCuriosEvents {
    private ApollyonKeepInventoryCuriosEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void keepCurios(DropRulesEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ApollyonDeathInventory.isProtected(player))
            event.addOverride(stack -> true, ICurio.DropRule.ALWAYS_KEEP);
    }
}
