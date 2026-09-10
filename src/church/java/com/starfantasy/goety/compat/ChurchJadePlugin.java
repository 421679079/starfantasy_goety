package com.starfantasy.goety.compat;

import com.starfantasy.goety.church.UnderworldAltarBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Loaded by Jade only when that optional mod is installed. */
@WailaPlugin
public final class ChurchJadePlugin implements IWailaPlugin {
    @Override public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AltarHint.INSTANCE, UnderworldAltarBlock.class);
    }

    public enum AltarHint implements IBlockComponentProvider {
        INSTANCE;
        @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("tooltip.starfantasy_goety.underworld_altar").withStyle(ChatFormatting.GRAY));
        }
        @Override public ResourceLocation getUid() {
            return new ResourceLocation("starfantasy_goety", "underworld_altar");
        }
    }
}
