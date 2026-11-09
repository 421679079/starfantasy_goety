package com.starfantasy.goety.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** RGB text styles only. The clock is updated once per client frame, never stored in item NBT. */
public final class FinalStaffName {
    private static long frameMillis;

    public static void frame(long millis) { frameMillis = millis; }
    public static long time() { return frameMillis; }

    public static Component create() {
        String name = Component.translatable("item.starfantasy_goety.final_staff").getString();
        int[] glyphs = name.codePoints().toArray();
        MutableComponent result = Component.empty();
        for (int i = 0; i < glyphs.length; i++) {
            int color = color(glyphs.length <= 1 ? 0 : (float)i / (glyphs.length - 1), frameMillis);
            result.append(Component.literal(new String(Character.toChars(glyphs[i])))
                    .withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    static int color(float position, long millis) {
        int base = mix(0xFF3030, 0xFFFFFF, Math.max(0, Math.min(1, position)));
        float center = Math.floorMod(millis, 2400L) / 2400F * 1.7F - .35F;
        float shine = Math.max(0, 1 - Math.abs(position - center) / .32F);
        shine = shine * shine * (3 - 2 * shine);
        return mix(base, 0xFFE64D, shine * .95F);
    }

    private static int mix(int start, int end, float amount) {
        int r = Math.round((start >> 16 & 255) * (1-amount) + (end >> 16 & 255) * amount);
        int g = Math.round((start >> 8 & 255) * (1-amount) + (end >> 8 & 255) * amount);
        int b = Math.round((start & 255) * (1-amount) + (end & 255) * amount);
        return r << 16 | g << 8 | b;
    }

    private FinalStaffName() {}
}
