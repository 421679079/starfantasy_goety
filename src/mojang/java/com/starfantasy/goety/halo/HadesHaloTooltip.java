package com.starfantasy.goety.halo;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Collection text only; existing effect descriptions and Curios attributes retain their own rules. */
public final class HadesHaloTooltip {
    public static void append(ItemStack halo, List<Component> tooltip) {
        int mask = HadesHaloContents.collectedMask(halo);
        tooltip.add(Component.translatable("tooltip.starfantasy_goety.halo_of_hades.collected_count",
                Integer.bitCount(mask)).withStyle(ChatFormatting.GRAY));
        for (int i = 0; i < 12; i++) if ((mask & 1 << i) != 0) {
            tooltip.add(Component.translatable("tooltip.starfantasy_goety.halo_of_hades.collected."
                    + HadesHaloContents.IDS.get(i)).withStyle(ChatFormatting.GRAY));
        }
    }

    private HadesHaloTooltip() {}
}
