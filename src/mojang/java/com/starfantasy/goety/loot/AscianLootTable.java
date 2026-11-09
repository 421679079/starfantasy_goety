package com.starfantasy.goety.loot;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;

/** Configured drops use the same vanilla pools/functions that JER reads from resource tables. */
public final class AscianLootTable {
    private static final Pattern ENTRY = Pattern.compile("([^:]+:[^:]+)(?::([0-9]+)(?:-([0-9]+))?)?");

    public static LootTable create(List<? extends String> entries) {
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
                int low = match.group(2) == null ? 1 : Integer.parseInt(match.group(2));
                int high = match.group(3) == null ? low : Math.max(low, Integer.parseInt(match.group(3)));
                low = Math.min(100000, low);
                high = Math.min(100000, high);
                if (high == 0) continue;
                var count = low == high ? ConstantValue.exactly(low) : UniformGenerator.between(low, high);
                table.withPool(LootPool.lootPool().name("configured_" + index++)
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(count))));
            } catch (IllegalArgumentException ex) {
                LogUtils.getLogger().warn("Ignoring invalid Ascian King drop '{}': {}", entry, ex.getMessage());
            }
        }
        return table.build();
    }

    private AscianLootTable() {}
}
