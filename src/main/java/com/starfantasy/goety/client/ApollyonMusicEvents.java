package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.combat.ApollyonPageantController;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.music.client.StarFantasyBossMusicClient;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT)
public final class ApollyonMusicEvents {
    private ApollyonMusicEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ApollyonEntity boss
                && boss.m_9236_().f_46443_ && shouldKeepPlaying(boss)) {
            StarFantasyBossMusicClient.playBossMusic(
                    ApollyonSoundRegistry.APOLLYON_BGM.get(), boss, 0.7F, 1.0F,
                    ApollyonMusicEvents::shouldKeepPlaying,
                    ApollyonMusicEvents::shouldFadeImmediately);
        }
    }

    private static boolean shouldKeepPlaying(Mob owner) {
        if (!(owner instanceof ApollyonEntity boss)
                || !boss.m_6084_() || boss.m_213877_() || boss.isPlayingDeathAnimation()) {
            return false;
        }
        // Read synced state: the pageant controller itself only advances on the server.
        int pageant = boss.getPageantState();
        return pageant != ApollyonPageantController.PENDING_RESTART
                && (boss.isArenaActive() || pageant != ApollyonPageantController.INACTIVE);
    }

    private static boolean shouldFadeImmediately(Mob owner) {
        return owner instanceof ApollyonEntity boss && boss.isPlayingDeathAnimation();
    }
}
