package com.starfantasy.goety.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "starfantasy_goety", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FinalStaffSchoolKeys {
    public static final KeyMapping SWITCH = new KeyMapping("key.starfantasy_goety.staff_school",
            KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X, "key.categories.starfantasy_goety");
    private FinalStaffSchoolKeys() {}
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(SWITCH); }
}
