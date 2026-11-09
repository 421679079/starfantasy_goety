package com.starfantasy.goety.registry;

import com.starfantasy.goety.effect.UnsteadyEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Shared content namespace; these entries are owned and shipped by the Goety module. */
public final class StarFantasyEffectRegistry {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.Keys.MOB_EFFECTS, "star_fantasy");
    public static final RegistryObject<MobEffect> UNSTEADY = EFFECTS.register("unsteady", UnsteadyEffect::new);
    public static void init(IEventBus bus) { EFFECTS.register(bus); }
    private StarFantasyEffectRegistry() {}
}
