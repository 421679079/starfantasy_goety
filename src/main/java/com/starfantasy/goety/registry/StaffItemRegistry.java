/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Polarice3.Goety.api.magic.SpellType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraftforge.common.ForgeConfigSpec$DoubleValue
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegisterEvent
 *  net.minecraftforge.registries.RegisterEvent$RegisterHelper
 */
package com.starfantasy.goety.registry;

import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.config.StaffConfig;
import com.starfantasy.goety.item.ConfigurableDarkStaffItem;
import com.starfantasy.goety.item.FinalStaffDarkItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid="starfantasy_goety", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class StaffItemRegistry {
    public static final ResourceLocation OMINOUS_STAFF_2 = StaffItemRegistry.rl("ominous_staff_2");
    public static final ResourceLocation GEO_STAFF_2 = StaffItemRegistry.rl("geo_staff_2");
    public static final ResourceLocation WIND_STAFF_2 = StaffItemRegistry.rl("wind_staff_2");
    public static final ResourceLocation STORM_STAFF_2 = StaffItemRegistry.rl("storm_staff_2");
    public static final ResourceLocation FROST_STAFF_2 = StaffItemRegistry.rl("frost_staff_2");
    public static final ResourceLocation WILD_STAFF_2 = StaffItemRegistry.rl("wild_staff_2");
    public static final ResourceLocation ABYSS_STAFF_2 = StaffItemRegistry.rl("abyss_staff_2");
    public static final ResourceLocation VOID_STAFF_2 = StaffItemRegistry.rl("void_staff_2");
    public static final ResourceLocation NETHER_STAFF_2 = StaffItemRegistry.rl("nether_staff_2");
    public static final ResourceLocation FINAL_STAFF = StaffItemRegistry.rl("final_staff");
    public static final List<ResourceLocation> STAFF_IDS = List.of(
            OMINOUS_STAFF_2,
            GEO_STAFF_2,
            WIND_STAFF_2,
            STORM_STAFF_2,
            FROST_STAFF_2,
            WILD_STAFF_2,
            ABYSS_STAFF_2,
            VOID_STAFF_2,
            NETHER_STAFF_2,
            FINAL_STAFF
    );
    private static final Map<ResourceLocation, Item> REGISTERED = new HashMap<ResourceLocation, Item>();

    private StaffItemRegistry() {
    }

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        if (!event.getRegistryKey().equals((Object)ForgeRegistries.Keys.ITEMS)) {
            return;
        }
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, OMINOUS_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.ILL, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, GEO_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.GEOMANCY, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, WIND_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.WIND, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, STORM_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.STORM, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, FROST_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.FROST, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, WILD_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.WILD, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, ABYSS_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.ABYSS, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, VOID_STAFF_2, (Item)new ConfigurableDarkStaffItem(12.0, SpellType.VOID, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            Item.Properties netherProps = new Item.Properties().m_41486_();
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, NETHER_STAFF_2, (Item)new ConfigurableDarkStaffItem(netherProps, 12.0, SpellType.NETHER, () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.TIER2_ENTITY_RANGE).get()));
            StaffItemRegistry.register((RegisterEvent.RegisterHelper<Item>)helper, FINAL_STAFF, (Item)new FinalStaffDarkItem(() -> ((ForgeConfigSpec.DoubleValue)StaffConfig.FINAL_ATTACK_DAMAGE).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.FINAL_ATTACK_SPEED).get(), () -> ((ForgeConfigSpec.DoubleValue)StaffConfig.FINAL_ENTITY_RANGE).get()));
        });
    }

    public static Item get(ResourceLocation id) {
        return REGISTERED.get(id);
    }

    private static void register(RegisterEvent.RegisterHelper<Item> helper, ResourceLocation id, Item item) {
        helper.register(id, item);
        REGISTERED.put(id, item);
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                StarFantasyGoetyMod.CONTENT_NAMESPACE, path);
    }
}
