package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.HadesEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers Hades under the shared Star Fantasy content namespace. */
public final class HadesEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, StarFantasyGoetyMod.CONTENT_NAMESPACE);

    public static final RegistryObject<EntityType<HadesEntity>> HADES =
            ENTITY_TYPES.register("hades", () -> EntityType.Builder
                    .m_20704_(HadesEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20719_()
                    .m_20699_(10.0F, 20.0F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(StarFantasyGoetyMod.CONTENT_NAMESPACE, "hades").toString()));

    private HadesEntityRegistry() {
    }

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
