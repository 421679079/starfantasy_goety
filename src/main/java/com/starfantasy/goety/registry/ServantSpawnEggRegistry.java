package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApostleServantEntity;
import com.starfantasy.goety.item.StarFantasyServantSpawnEggItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ServantSpawnEggRegistry {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.Keys.ITEMS, StarFantasyGoetyMod.MODID);
    private static final int[] TITLE_COLORS = {
            0xC8A86B, 0xAD3C68, 0x6AA45C, 0x5553B0,
            0x7387C4, 0x9D55AF, 0xE57F41, 0xB6A179,
            0x8BC9E8, 0xD75B59, 0xF3D777, 0x5E5973
    };

    public static final RegistryObject<Item> HADES_SERVANT = ITEMS.register("hades_servant_spawn_egg",
            () -> new StarFantasyServantSpawnEggItem(HadesEntityRegistry.HADES_SERVANT,
                    0x17131F, 0x9F5D85, -1));
    public static final RegistryObject<Item> APOLLYON_SERVANT = ITEMS.register("apollyon_servant_spawn_egg",
            () -> new StarFantasyServantSpawnEggItem(ApollyonEntityRegistry.APOLLYON_SERVANT,
                    0x24172E, 0xD7A8ED, -1));
    public static final List<RegistryObject<Item>> APOSTLE_SERVANTS = registerApostleTitles();

    private ServantSpawnEggRegistry() {
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private static List<RegistryObject<Item>> registerApostleTitles() {
        List<RegistryObject<Item>> eggs = new ArrayList<>();
        for (int index = 0; index < ApostleServantEntity.TITLES.length; index++) {
            final int title = index;
            eggs.add(ITEMS.register(ApostleServantEntity.TITLES[index] + "_apostle_servant_spawn_egg",
                    () -> new StarFantasyServantSpawnEggItem(ApollyonEntityRegistry.APOSTLE_SERVANT,
                            0x211B2E, TITLE_COLORS[title], title)));
        }
        return List.copyOf(eggs);
    }
}
