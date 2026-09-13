package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.item.HaloCurioItem;
import com.Polarice3.Goety.api.magic.SpellType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** Registers the twelve Apostle halos and Hades' eye halo. */
public final class HaloItemRegistry {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.Keys.ITEMS, StarFantasyGoetyMod.CONTENT_NAMESPACE);

    public static final RegistryObject<Item> HALO_OF_THE_RISEN =
            registerSchool("halo_of_the_risen", SpellType.NECROMANCY);
    public static final RegistryObject<Item> HALO_OF_THE_ABHORRENT =
            registerSchool("halo_of_the_abhorrent", SpellType.ABYSS);
    public static final RegistryObject<Item> HALO_OF_THE_DEFILER =
            registerSchool("halo_of_the_defiler", SpellType.WILD);
    public static final RegistryObject<Item> HALO_OF_THE_DARK =
            registerSchool("halo_of_the_dark", SpellType.VOID);
    public static final RegistryObject<Item> HALO_OF_THE_GREAT_SHADOW =
            registerSchool("halo_of_the_great_shadow", SpellType.WIND);
    public static final RegistryObject<Item> HALO_OF_THE_WITCH_KING =
            registerSchool("halo_of_the_witch_king", SpellType.ILL);
    public static final RegistryObject<Item> HALO_OF_THE_PYRE_LORD =
            registerSchool("halo_of_the_pyre_lord", SpellType.NETHER);
    public static final RegistryObject<Item> HALO_OF_THE_PROFANE =
            registerUnschooled("halo_of_the_profane");
    public static final RegistryObject<Item> HALO_OF_THE_CRUEL =
            registerSchool("halo_of_the_cruel", SpellType.FROST);
    public static final RegistryObject<Item> HALO_OF_THE_TERRIBLE =
            registerSchool("halo_of_the_terrible", SpellType.STORM);
    public static final RegistryObject<Item> HALO_OF_THE_GLORIOUS =
            registerUnschooled("halo_of_the_glorious");
    public static final RegistryObject<Item> HALO_OF_THE_ATROCIOUS =
            registerSchool("halo_of_the_atrocious", SpellType.GEOMANCY);
    public static final RegistryObject<Item> HALO_OF_HADES =
            ITEMS.register("halo_of_hades", HaloCurioItem::hades);
    public static final RegistryObject<Item> FADED_HALO =
            ITEMS.register("faded_halo", com.starfantasy.goety.item.FadedHaloItem::new);

    public static final List<RegistryObject<Item>> APOSTLE_HALOS = List.of(
            HALO_OF_THE_RISEN,
            HALO_OF_THE_ABHORRENT,
            HALO_OF_THE_DEFILER,
            HALO_OF_THE_DARK,
            HALO_OF_THE_GREAT_SHADOW,
            HALO_OF_THE_WITCH_KING,
            HALO_OF_THE_PYRE_LORD,
            HALO_OF_THE_PROFANE,
            HALO_OF_THE_CRUEL,
            HALO_OF_THE_TERRIBLE,
            HALO_OF_THE_GLORIOUS,
            HALO_OF_THE_ATROCIOUS
    );

    public static final List<RegistryObject<Item>> ALL_HALOS = List.of(
            HALO_OF_THE_RISEN,
            HALO_OF_THE_ABHORRENT,
            HALO_OF_THE_DEFILER,
            HALO_OF_THE_DARK,
            HALO_OF_THE_GREAT_SHADOW,
            HALO_OF_THE_WITCH_KING,
            HALO_OF_THE_PYRE_LORD,
            HALO_OF_THE_PROFANE,
            HALO_OF_THE_CRUEL,
            HALO_OF_THE_TERRIBLE,
            HALO_OF_THE_GLORIOUS,
            HALO_OF_THE_ATROCIOUS,
            HALO_OF_HADES
    );

    private HaloItemRegistry() {
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    private static RegistryObject<Item> registerSchool(String path, SpellType school) {
        return ITEMS.register(path, () -> HaloCurioItem.school(school));
    }

    private static RegistryObject<Item> registerUnschooled(String path) {
        return ITEMS.register(path, HaloCurioItem::unschooled);
    }
}
