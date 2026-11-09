package com.starfantasy.goety.client;

import com.starfantasy.goety.halo.client.HaloYsmMount;
import com.starfantasy.goety.halo.client.YsmHaloBridge;
import com.starfantasy.goety.registry.HaloItemRegistry;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/** YSM replaces the vanilla Curios render layer. Replay visible equipped halos once. */
@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class HaloYsmEvents {
    private static final Map<String,HaloCurioRenderer> RENDERERS=new HashMap<>();
    @SubscribeEvent public static void post(RenderLivingEvent.Post<?,?> event){
        if(!ModList.get().isLoaded("yes_steve_model")||event.getRenderer() instanceof PlayerRenderer
                ||!(event.getEntity() instanceof AbstractClientPlayer player)||player.m_5833_()||player.m_20145_()
                ||com.starfantasy.library.vfx.client.StarFantasyShaderCompat.isRenderingShaderShadowPass())return;
        var inventory=CuriosApi.getCuriosInventory(player).resolve();if(inventory.isEmpty())return;
        HaloYsmMount.Frames frames=null;
        for(var halo:HaloItemRegistry.ALL_HALOS)for(var equipped:inventory.get().findCurios(halo.get())){
            if(!equipped.slotContext().visible())continue;
            if(frames==null){frames=YsmHaloBridge.capture(event.getRenderer(),player);if(frames==null)return;}
            String id=halo.getId().m_135815_();var pose=event.getPoseStack();pose.m_85836_();
            try{
                RENDERERS.computeIfAbsent(id,HaloCurioRenderer::new).renderYsm(player,event.getPartialTick(),pose,event.getMultiBufferSource(),frames,equipped.stack());
            }finally{pose.m_85849_();}
        }
    }
    private HaloYsmEvents(){}
}
