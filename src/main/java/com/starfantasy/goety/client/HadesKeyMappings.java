package com.starfantasy.goety.client;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StarFantasyGoetyMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HadesKeyMappings {
    public static final KeyMapping STOP_RIDING = new KeyMapping("key.starfantasy_goety.stop_riding",
            KeyConflictContext.IN_GAME, com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R, "key.categories.starfantasy_goety");
    private HadesKeyMappings() { }
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(STOP_RIDING); }
    public static MutableComponent hint() {
        return Component.m_237110_("message.starfantasy_goety.stop_riding", STOP_RIDING.m_90863_());
    }
}
