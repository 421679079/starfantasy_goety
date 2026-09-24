package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.focus.BattleFocusItem;
import com.starfantasy.goety.magic.guard.GuardFocusItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

/** Vanilla item tooltips require one component per explicit line. */
public final class FocusTooltip {
    public static boolean append(Item item, List<Component> tooltip) {
        if (!(item instanceof BattleFocusItem) && !(item instanceof GuardFocusItem)) return false;
        String description = Component.translatable(item.getDescriptionId() + ".info").getString();
        for (String line : description.split("\\R", -1)) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        return true;
    }
    private FocusTooltip() {}
}
