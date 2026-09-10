package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.effect.ApollyonFamineEffect;
import com.starfantasy.goety.effect.ApollyonMultishotEffect;
import com.starfantasy.goety.effect.ApollyonWeaknessEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ApollyonEffectRegistry {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.Keys.MOB_EFFECTS, StarFantasyGoetyMod.CONTENT_NAMESPACE);

    public static final RegistryObject<MobEffect> MULTISHOT =
            EFFECTS.register("apollyon_multishot", ApollyonMultishotEffect::new);
    public static final RegistryObject<MobEffect> FAMINE =
            EFFECTS.register("apollyon_famine", ApollyonFamineEffect::new);
    public static final RegistryObject<MobEffect> WEAKNESS =
            EFFECTS.register("weakness", ApollyonWeaknessEffect::new);

    private ApollyonEffectRegistry() {
    }

    public static void init(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
