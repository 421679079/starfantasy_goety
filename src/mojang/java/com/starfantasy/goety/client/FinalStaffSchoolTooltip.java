package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.FinalStaffSchool;
import com.starfantasy.goety.magic.AscianStaffSchool;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

/** Insert school help after Goety's focus-switch hint, ahead of attributes and advanced details. */
public final class FinalStaffSchoolTooltip {
    public static void append(List<Component> lines, ItemStack stack, Component key) {
        if (AscianStaffSchool.isAscian(stack)) {
            lines.add(insertionPoint(lines), Component.translatable("tooltip.starfantasy_goety.ascian_school.select", key)
                    .withStyle(ChatFormatting.GOLD));
            return;
        }
        if (!FinalStaffSchool.isFinalStaff(stack)) return;
        int insertion = insertionPoint(lines);
        lines.add(insertion, Component.translatable("tooltip.starfantasy_goety.staff_school.select", key)
                .withStyle(ChatFormatting.GOLD));
        lines.add(insertion + 1, Component.translatable("tooltip.starfantasy_goety.staff_school.current",
                coloredLabel(FinalStaffSchool.get(stack))).withStyle(ChatFormatting.GOLD));
    }

    public static Component coloredLabel(FinalStaffSchool school) {
        return school.label();
    }

    private static int insertionPoint(List<Component> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getContents() instanceof TranslatableContents text
                    && text.getKey().equals("info.goety.wand.switch")) return i + 1;
        }
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).getString().isEmpty()
                    || lines.get(i).getContents() instanceof TranslatableContents text
                    && text.getKey().startsWith("item.modifiers.")) return i;
        }
        return lines.size();
    }

    private FinalStaffSchoolTooltip() {}
}
