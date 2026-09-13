package com.starfantasy.goety.client;

import com.starfantasy.goety.magic.guard.GuardRules;

/** Packs the artist's monochrome silhouette into a single sevenfold opacity mask. */
public final class GuardShieldMask {
    public static final int SIZE = 512;
    public static final float EXTENT = 1.95F;
    private static final float LOBE_ROOT = .28F;
    private static final float LOBE_LENGTH = 1.47F;
    private GuardShieldMask() {}

    /** Source pixels are packed ABGR, matching NativeImage. RGB is used only as an opacity mask. */
    public static float[] build(int[] source, int width, int height) {
        float[] lobe = new float[source.length];
        int minX = width, minY = height, maxX = -1, maxY = -1;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int color = source[y * width + x];
            float grey = ((color & 255) + (color >>> 8 & 255) + (color >>> 16 & 255)) / 765F;
            float value = smooth((grey - .10F) / .80F);
            lobe[y * width + x] = value;
            if (value > .5F) {
                minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                minY = Math.min(minY, y); maxY = Math.max(maxY, y);
            }
        }
        if (maxX <= minX || maxY <= minY) throw new IllegalArgumentException("Empty guard shield silhouette");
        float fullWidth = LOBE_LENGTH * (maxX - minX) / (maxY - minY);
        float[] cosine = new float[GuardRules.PETALS], sine = new float[GuardRules.PETALS];
        for (int i = 0; i < GuardRules.PETALS; i++) {
            cosine[i] = (float)Math.cos(GuardRules.petalAngle(i));
            sine[i] = (float)Math.sin(GuardRules.petalAngle(i));
        }
        float[] field = new float[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            float px = ((x + .5F) / SIZE * 2 - 1) * EXTENT;
            float py = (1 - (y + .5F) / SIZE * 2) * EXTENT;
            float value = 1 - smooth(((float)Math.hypot(px, py) - .84F) / .025F);
            for (int i = 0; i < GuardRules.PETALS; i++) {
                float radial = px * cosine[i] + py * sine[i];
                float sideways = -px * sine[i] + py * cosine[i];
                float u = .5F + sideways / fullWidth, v = 1 - (radial - LOBE_ROOT) / LOBE_LENGTH;
                if (u < 0 || u > 1 || v < 0 || v > 1) continue;
                float sampled = sample(lobe, width, height,
                        minX + u * (maxX - minX), minY + v * (maxY - minY));
                // Union rather than additive stamping: overlapping roots cannot form bright seams.
                value = Math.max(value, sampled);
            }
            field[y * SIZE + x] = value;
        }
        float[] glow = blur(field);
        for (int i = 0; i < field.length; i++) {
            // One flat density inside. Soft bloom is restricted to outside the union's contour.
            field[i] = Math.min(.62F, .62F * field[i] + .65F * glow[i] * (1 - field[i]));
        }
        return field;
    }
    private static float sample(float[] data, int width, int height, float x, float y) {
        int x0 = (int)x, y0 = (int)y, x1 = Math.min(width - 1, x0 + 1), y1 = Math.min(height - 1, y0 + 1);
        float dx = x - x0, dy = y - y0;
        return (data[y0 * width + x0] * (1 - dx) + data[y0 * width + x1] * dx) * (1 - dy)
                + (data[y1 * width + x0] * (1 - dx) + data[y1 * width + x1] * dx) * dy;
    }
    private static float[] blur(float[] data) {
        int radius = 8;
        float[] kernel = new float[radius * 2 + 1];
        float sum = 0;
        for (int i = -radius; i <= radius; i++) sum += kernel[i + radius] = (float)Math.exp(-i * i / 24F);
        for (int i = 0; i < kernel.length; i++) kernel[i] /= sum;
        float[] horizontal = new float[data.length], result = new float[data.length];
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            for (int i = -radius; i <= radius; i++) {
                int sx = Math.max(0, Math.min(SIZE - 1, x + i));
                horizontal[y * SIZE + x] += data[y * SIZE + sx] * kernel[i + radius];
            }
        }
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            for (int i = -radius; i <= radius; i++) {
                int sy = Math.max(0, Math.min(SIZE - 1, y + i));
                result[y * SIZE + x] += horizontal[sy * SIZE + x] * kernel[i + radius];
            }
        }
        return result;
    }
    private static float smooth(float value) {
        float x = Math.max(0, Math.min(1, value));
        return x * x * (3 - 2 * x);
    }
}
