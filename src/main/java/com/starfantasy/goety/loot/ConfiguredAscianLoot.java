package com.starfantasy.goety.loot;

import com.starfantasy.goety.config.AscianKingConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Keep resource-pack defaults, or replace the actual loot table when deathDrops is customized. */
@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ConfiguredAscianLoot {
    private static final ResourceLocation TABLE = new ResourceLocation("starfantasy_goety", "entities/ascian_king");

    @SubscribeEvent public static void load(LootTableLoadEvent event) {
        if (!TABLE.equals(event.getName()) || !AscianKingConfig.SPEC.isLoaded()) return;
        var configured = AscianKingConfig.DROPS.get();
        if (configured.equals(AscianKingConfig.DEFAULT_DEATH_DROPS)) return;
        var table = AscianLootTable.create(configured);
        table.setLootTableId(event.getName());
        event.setTable(table);
    }

    private ConfiguredAscianLoot() {}
}
