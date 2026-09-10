/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.starfantasy.goety.event;

import com.starfantasy.goety.registry.StaffItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="starfantasy_goety", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class StaffCreativeTabEvents {
    private StaffCreativeTabEvents() {
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals((Object)CreativeModeTabs.f_256797_) && !event.getTabKey().equals((Object)CreativeModeTabs.f_256750_)) {
            return;
        }
        StaffCreativeTabEvents.add(event, StaffItemRegistry.OMINOUS_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.GEO_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.WIND_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.STORM_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.FROST_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.WILD_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.ABYSS_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.VOID_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.NETHER_STAFF_2);
        StaffCreativeTabEvents.add(event, StaffItemRegistry.FINAL_STAFF);
    }

    private static void add(BuildCreativeModeTabContentsEvent event, ResourceLocation id) {
        Item item = StaffItemRegistry.get(id);
        if (item != null) {
            event.m_246326_((ItemLike)item);
        }
    }
}
