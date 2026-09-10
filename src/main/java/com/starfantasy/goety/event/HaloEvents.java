package com.starfantasy.goety.event;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.registry.HaloItemRegistry;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID)
public final class HaloEvents {
    private HaloEvents() {
    }

    @SubscribeEvent
    public static void onStarvation(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player
                && !player.m_9236_().f_46443_
                && event.getSource().m_276093_(DamageTypes.f_268441_)
                && CuriosApi.getCuriosInventory(player).resolve()
                        .flatMap(inventory -> inventory.findFirstCurio(
                                HaloItemRegistry.HALO_OF_THE_PROFANE.get()))
                        .isPresent()) {
            event.setCanceled(true);
        }
    }
}
