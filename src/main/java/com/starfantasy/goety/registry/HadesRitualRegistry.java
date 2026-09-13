package com.starfantasy.goety.registry;

import com.Polarice3.Goety.common.ritual.ModRitualFactory;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.ritual.HadesRevivalRitual;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class HadesRitualRegistry {
    private static final DeferredRegister<ModRitualFactory> RITUALS = DeferredRegister.create(
            new ResourceLocation("goety", "ritual_factory"), StarFantasyGoetyMod.MODID);
    public static final RegistryObject<ModRitualFactory> REVIVE_HADES = RITUALS.register(
            "revive_hades", () -> new ModRitualFactory(HadesRevivalRitual::new));
    public static void init(IEventBus bus) { RITUALS.register(bus); }
    private HadesRitualRegistry() { }
}
