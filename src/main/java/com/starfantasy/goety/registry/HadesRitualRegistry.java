package com.starfantasy.goety.registry;

import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.ritual.ServantHaloRitual;
import com.starfantasy.goety.ritual.HadesSummonRitual;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class HadesRitualRegistry {
    private static final DeferredRegister<ModRitualFactory> RITUALS = DeferredRegister.create(
            new ResourceLocation("goety", "ritual_factory"), StarFantasyGoetyMod.MODID);
    public static final RegistryObject<ModRitualFactory> REVIVE_HADES = RITUALS.register(
            "revive_hades", () -> new ModRitualFactory(ServantHaloRitual::new));
    public static final RegistryObject<ModRitualFactory> SUMMON_HADES = RITUALS.register(
            "summon_hades", () -> new ModRitualFactory(HadesSummonRitual::new));
    public static final RegistryObject<ModRitualFactory> SUMMON_APOLLYON = RITUALS.register(
            "summon_apollyon", () -> new ModRitualFactory(ServantHaloRitual::new));
    public static final RegistryObject<ModRitualFactory> REVIVE_APOLLYON = RITUALS.register(
            "revive_apollyon", () -> new ModRitualFactory(recipe -> new ServantHaloRitual(recipe, true)));
    public static void init(IEventBus bus) { RITUALS.register(bus); }
    private HadesRitualRegistry() { }
}
