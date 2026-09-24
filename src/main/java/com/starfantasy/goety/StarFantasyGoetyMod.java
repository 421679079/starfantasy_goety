/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  org.slf4j.Logger
 */
package com.starfantasy.goety;

import com.starfantasy.goety.config.StaffConfig;
import com.starfantasy.goety.config.ApollyonConfig;
import com.starfantasy.goety.event.StaffAttributeEvents;
import com.starfantasy.goety.registry.StaffItemRegistry;
import com.starfantasy.goety.registry.SpellAttributeRegistry;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.ApollyonSoundRegistry;
import com.starfantasy.goety.registry.HadesEntityRegistry;
import com.starfantasy.goety.registry.HaloItemRegistry;
import com.starfantasy.goety.registry.StarFantasyGoetyCreativeTabRegistry;
import com.starfantasy.goety.registry.ServantSpawnEggRegistry;
import com.starfantasy.goety.network.StarFantasyGoetyNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value="starfantasy_goety")
public final class StarFantasyGoetyMod {
    public static final String MODID = "starfantasy_goety";
    public static final String CONTENT_NAMESPACE = MODID;
    public static final String STAFF_CONFIG_PATH = "starfantasy_goety/staff.toml";
    public static final String APOLLYON_CONFIG_PATH = "starfantasy_goety/apollyon.toml";
    public StarFantasyGoetyMod() {
        StarFantasyGoetyNetwork.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                com.starfantasy.goety.config.SpellConfig.SPEC, "starfantasy_goety/spell.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                com.starfantasy.goety.config.HaloConfig.SPEC, "starfantasy_goety/halo.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)StaffConfig.SPEC, STAFF_CONFIG_PATH);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)ApollyonConfig.SPEC, APOLLYON_CONFIG_PATH);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                com.starfantasy.goety.config.ServantConfig.SPEC, "starfantasy_goety/servant.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                com.starfantasy.goety.config.ApostleConfig.SPEC, "starfantasy_goety/apostle.toml");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        com.starfantasy.goety.church.ChurchContent.init(modBus);
        com.starfantasy.goety.magic.guard.GuardFocusContent.init(modBus);
        com.starfantasy.goety.magic.focus.BattleFocusContent.init(modBus);
        ApollyonEntityRegistry.init(modBus);
        ApollyonEffectRegistry.init(modBus);
        ApollyonParticleRegistry.init(modBus);
        ApollyonSoundRegistry.init(modBus);
        com.starfantasy.goety.registry.ApostleAppearanceSounds.init(modBus);
        HadesEntityRegistry.init(modBus);
        com.starfantasy.goety.registry.HadesRitualRegistry.init(modBus);
        SpellAttributeRegistry.init(modBus);
        HaloItemRegistry.init(modBus);
        ServantSpawnEggRegistry.init(modBus);
        StarFantasyGoetyCreativeTabRegistry.init(modBus);
        modBus.register(StaffItemRegistry.class);
        MinecraftForge.EVENT_BUS.register(StaffAttributeEvents.class);
    }
}
