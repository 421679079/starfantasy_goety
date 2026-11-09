package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.AscianKingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AscianKingRegistry {
    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarFantasyGoetyMod.MODID);
    public static final RegistryObject<EntityType<AscianKingEntity>> ASCIAN_KING = TYPES.register("ascian_king", () ->
            EntityType.Builder.m_20704_(AscianKingEntity::new, MobCategory.MONSTER)
                    .m_20699_(2.5F, 6.3F).m_20702_(16).m_20717_(1).m_20719_()
                    .m_20712_(StarFantasyGoetyMod.MODID + ":ascian_king"));
    private AscianKingRegistry() {}
    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.AscianVoidRiftEntity>> VOID_RIFT=TYPES.register("ascian_void_rift",()->
            EntityType.Builder.<com.starfantasy.goety.entity.AscianVoidRiftEntity>m_20704_(com.starfantasy.goety.entity.AscianVoidRiftEntity::new,MobCategory.MISC)
                    .m_20699_(1,1).m_20702_(16).m_20717_(1).m_20712_(StarFantasyGoetyMod.MODID+":ascian_void_rift"));
    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.AscianPhantomBladeEntity>> PHANTOM_BLADE=TYPES.register("ascian_phantom_blade",()->
            EntityType.Builder.<com.starfantasy.goety.entity.AscianPhantomBladeEntity>m_20704_(com.starfantasy.goety.entity.AscianPhantomBladeEntity::new,MobCategory.MISC)
                    .m_20699_(.5F,.5F).m_20702_(16).m_20717_(1).m_20712_(StarFantasyGoetyMod.MODID+":ascian_phantom_blade"));
    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.AscianProjectileEntity>> PROJECTILE = TYPES.register("ascian_projectile", () ->
            EntityType.Builder.<com.starfantasy.goety.entity.AscianProjectileEntity>m_20704_(com.starfantasy.goety.entity.AscianProjectileEntity::new,MobCategory.MISC)
                    .m_20699_(.5F,.5F).m_20702_(16).m_20717_(1).m_20712_(StarFantasyGoetyMod.MODID+":ascian_projectile"));
    public static void init(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((EntityAttributeCreationEvent event) -> event.put(ASCIAN_KING.get(), AscianKingEntity.createAttributes().m_22265_()));
    }
}
