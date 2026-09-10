package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ApollyonParticleRegistry {
    private static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, StarFantasyGoetyMod.MODID);

    public static final RegistryObject<SimpleParticleType> CASTING_SMOKE =
            PARTICLES.register("apollyon_casting_smoke", () -> new SimpleParticleType(true) {
            });

    public static final RegistryObject<SimpleParticleType> PROFANE_SPELL =
            PARTICLES.register("apollyon_profane_spell", () -> new SimpleParticleType(true) {
            });

    private ApollyonParticleRegistry() {
    }

    public static void init(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
