package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.library.music.client.StarFantasyBossMusicClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianMusicEvents {
    @SubscribeEvent public static void tick(LivingEvent.LivingTickEvent event){
        if(event.getEntity() instanceof AscianKingEntity boss && boss.m_9236_().f_46443_ && audible(boss))
            StarFantasyBossMusicClient.playBossMusic(ApollyonSoundRegistry.ASCIAN_KING.get(),boss,.7F,1,AscianMusicEvents::audible,m->!m.m_6084_());
    }
    private static boolean audible(Mob boss){
        var player=Minecraft.m_91087_().f_91074_;
        return player!=null&&player.m_9236_()==boss.m_9236_()&&boss.m_6084_()&&!boss.m_213877_()&&player.m_20280_(boss)<=96*96;
    }
    private AscianMusicEvents(){}
}
