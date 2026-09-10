package com.starfantasy.goety.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Entity-specific sound identities allow each client to choose its own appearance. */
public final class ApostleAppearanceSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "starfantasy_goety");
    public static final RegistryObject<SoundEvent> AMBIENT = register("original_apostle_ambient");
    public static final RegistryObject<SoundEvent> HURT = register("original_apostle_hurt");
    public static final RegistryObject<SoundEvent> DEATH = register("original_apostle_death");
    public static final RegistryObject<SoundEvent> MOE_HURT = register("apostle_moe_hurt");

    private ApostleAppearanceSounds() {}
    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation("starfantasy_goety", name)));
    }
    public static void init(IEventBus bus) { SOUNDS.register(bus); }
}
