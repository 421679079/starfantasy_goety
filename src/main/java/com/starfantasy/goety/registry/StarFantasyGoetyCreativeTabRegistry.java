package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** The dedicated Star Fantasy Goety creative-mode tab. */
public final class StarFantasyGoetyCreativeTabRegistry {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.f_279569_, StarFantasyGoetyMod.MODID);

    public static final RegistryObject<CreativeModeTab> STAR_FANTASY_GOETY = CREATIVE_MODE_TABS.register(
            "star_fantasy_goety",
            () -> CreativeModeTab.builder()
                    .m_257941_(Component.m_237115_("itemGroup.starfantasy_goety"))
                    .m_257737_(() -> HaloItemRegistry.HALO_OF_HADES.get().m_7968_())
                    .m_257501_((parameters, output) -> {
                        output.m_246326_((ItemLike)com.starfantasy.goety.church.ChurchContent.ALTAR_ITEM.get());
                        for (ResourceLocation staffId : StaffItemRegistry.STAFF_IDS) {
                            addStaff(output, staffId);
                        }
                        for (RegistryObject<Item> halo : HaloItemRegistry.ALL_HALOS) {
                            output.m_246326_((ItemLike)halo.get());
                        }
                    })
                    .m_257652_()
    );

    private StarFantasyGoetyCreativeTabRegistry() {
    }

    public static void init(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }

    private static void addStaff(CreativeModeTab.Output output, ResourceLocation id) {
        Item item = StaffItemRegistry.get(id);
        if (item != null) {
            output.m_246326_((ItemLike)item);
        }
    }
}
