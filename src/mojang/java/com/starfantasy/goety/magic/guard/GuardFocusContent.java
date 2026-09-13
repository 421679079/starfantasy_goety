package com.starfantasy.goety.magic.guard;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GuardFocusContent {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "starfantasy_goety");
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "starfantasy_goety");
    public static final RegistryObject<Item> GUARD_FOCUS = ITEMS.register("guard_focus", GuardFocusItem::new);
    public static final RegistryObject<EntityType<GuardShieldEntity>> SHIELD = ENTITIES.register("guard_shield",
            () -> EntityType.Builder.<GuardShieldEntity>of(GuardShieldEntity::new, MobCategory.MISC)
                    .sized(3.2F, 3.2F).noSave().noSummon().fireImmune().clientTrackingRange(8).updateInterval(1)
                    .build("starfantasy_goety:guard_shield"));
    private GuardFocusContent() {}
    public static void init(IEventBus bus) { ITEMS.register(bus); ENTITIES.register(bus); }
}
