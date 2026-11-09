package com.starfantasy.goety.loot;

import com.mojang.logging.LogUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.starfantasy.goety.config.ApollyonConfig;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "starfantasy_goety")
public final class ConfiguredApollyonLoot {
    private static final ResourceLocation APOLLYON = new ResourceLocation("starfantasy_goety", "entities/apollyon");
    private static final Pattern ENTRY = Pattern.compile("([^:{}]+:[^:{}]+)(?::([0-9]+)(?:-([0-9]+))?)?(\\{.*\\})?", Pattern.DOTALL);

    private ConfiguredApollyonLoot() {}

    @SubscribeEvent
    public static void load(LootTableLoadEvent event) {
        if (!APOLLYON.equals(event.getName())) return;
        List<String> configured = ApollyonConfig.deathDrops();
        if (!configured.equals(ApollyonConfig.defaultDeathDrops())) {
            LootTable table = createTable(configured);
            table.setLootTableId(event.getName());
            event.setTable(table);
        }
    }

    private static LootTable createTable(List<String> entries) {
        var table = LootTable.lootTable().setParamSet(LootContextParamSets.ENTITY);
        int index = 0;
        for (String entry : entries) {
            if (entry == null || entry.isBlank() || entry.trim().startsWith("#")) continue;
            var match = ENTRY.matcher(entry.trim());
            try {
                if (!match.matches()) throw new IllegalArgumentException("Invalid item/count syntax");
                ResourceLocation id = ResourceLocation.tryParse(match.group(1));
                var item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
                if (item == null || item == Items.AIR) throw new IllegalArgumentException("Unknown item");
                int a = match.group(2) == null ? 1 : Integer.parseInt(match.group(2));
                int b = match.group(3) == null ? a : Integer.parseInt(match.group(3));
                int min = Math.max(1, Math.min(item.getMaxStackSize(), Math.min(a, b)));
                int max = Math.max(1, Math.min(item.getMaxStackSize(), Math.max(a, b)));
                var count = min == max ? ConstantValue.exactly(min) : UniformGenerator.between(min, max);
                String name = "configured_" + index++;
                var loot = LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count));
                if (match.group(4) != null) loot.apply(SetNbtFunction.setTag(TagParser.parseTag(match.group(4))));
                table.withPool(LootPool.lootPool().name(name)
                        .setRolls(ConstantValue.exactly(1))
                        .add(loot));
            } catch (IllegalArgumentException | CommandSyntaxException ex) {
                LogUtils.getLogger().warn("Ignoring invalid Apollyon drop '{}': {}", entry, ex.getMessage());
            }
        }
        return table.build();
    }
}
