package com.starfantasy.goety.client;

import com.Polarice3.Goety.config.MainConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.starfantasy.goety.entity.AscianKingEntity;
import com.starfantasy.library.posture.client.PostureHudClientState;
import com.starfantasy.library.posture.client.PostureHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

@Mod.EventBusSubscriber(modid="starfantasy_goety",value=Dist.CLIENT)
public final class AscianBossBarEvents {
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void render(CustomizeGuiOverlayEvent.BossEventProgress event){
        if(!((Boolean)MainConfig.SpecialBossBar.get()))return;
        var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;
        // Library's existing posture packet maps the server boss-event UUID to its tracked entity.
        var state=PostureHudClientState.get(event.getBossEvent().m_18860_());
        if(state==null||!(mc.f_91073_.m_6815_(state.entityId()) instanceof AscianKingEntity boss))return;
        event.setCanceled(true);
        var gui=event.getGuiGraphics();int width=mc.m_91268_().m_85445_(),y=event.getY();
        boolean blend=GL11.glIsEnabled(GL11.GL_BLEND);
        int srcRgb=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dstRgb=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        float[] color=RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.setShaderColor(1,1,1,1);
            float scale=AscianBossBarArt.displayScale(width),top=AscianBossBarArt.top(y);
            var pose=gui.m_280168_();pose.m_85836_();
            try {
                pose.m_85837_(width/2F-AscianBossBarArt.WIDTH*scale/2,top,0);
                pose.m_85841_(scale,scale,1);
                AscianBossBarArt.draw(gui,0,0,event.getBossEvent().m_142717_(),boss.visualPhase(),boss.f_19797_+event.getPartialTick());
            } finally {pose.m_85849_();}
            AscianBossBarText.draw(gui,mc.f_91062_,event.getBossEvent().m_18861_().getString(),
                    width/2F,top,scale,boss.visualPhase(),(boss.f_19797_+event.getPartialTick())/20.0);
            // The canceled event prevents the ordinary posture handler from drawing over the mask.
            // Invoke its public renderer once, below the bottom flame tips, retaining its live bead state.
            int postureY=AscianBossBarArt.postureY(y,scale);
            event.setIncrement(postureY-y+AscianBossBarArt.TOP_SHIFT);
            pose.m_85836_();
            try {pose.m_85837_(0,postureY-y-10,0);PostureHudRenderer.onBossEventProgress(event);}
            finally {pose.m_85849_();}
            gui.m_280262_();
        } finally {
            RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);
            RenderSystem.blendFuncSeparate(srcRgb,dstRgb,srcAlpha,dstAlpha);
            if(blend)RenderSystem.enableBlend();else RenderSystem.disableBlend();
        }
    }
    private AscianBossBarEvents(){}
}
