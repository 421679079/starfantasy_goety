package com.starfantasy.goety.client;

/** Timing and expanding ripple geometry; the flower outline comes entirely from its texture. */
public final class GuardShieldGeometry {
    public static final float ROTATION_RADIANS_PER_TICK = .056F;
    private static final float RIPPLE_LIFETIME = 8.0F;
    private static final float RIPPLE_INTERVAL = 3.0F;
    private static final int RIPPLE_SEGMENTS = 96;
    private static final float[] RIPPLE_BANDS = {-1, -.45F, 0, .45F, 1};
    private static final float[] RIPPLE_ALPHA = {0, .22F, .85F, .22F, 0};
    private GuardShieldGeometry() {}

    /** Emits independent triangles, including the ripple strips. */
    @FunctionalInterface public interface Sink {
        void vertex(float x, float y, float z, int red, int green, int blue, int alpha);
    }
    public static float opening(float age) { return .05F + .95F * Math.max(0, Math.min(1, age)); }

    public static void drawRipples(float age, float opacity, Sink sink) {
        if (!Float.isFinite(age) || age < 0) return;
        float opening = opening(age);
        int newest = (int)Math.floor(age / RIPPLE_INTERVAL);
        for (int i = newest; i >= 0 && i > newest - 3; i--) {
            float elapsed = age - i * RIPPLE_INTERVAL;
            if (elapsed >= RIPPLE_LIFETIME) continue;
            float progress = elapsed / RIPPLE_LIFETIME;
            float strength = Math.min(1, elapsed / .55F) * (1 - progress) * (1 - progress);
            ripple(sink, (.10F + 1.12F * progress) * opening,
                    (.038F + .035F * progress) * opening, strength * opacity);
        }
    }

    private static void ripple(Sink sink, float radius, float width, float strength) {
        for (int i = 0; i < RIPPLE_SEGMENTS; i++) {
            double a = i * Math.PI * 2 / RIPPLE_SEGMENTS, b = (i + 1) * Math.PI * 2 / RIPPLE_SEGMENTS;
            for (int band = 0; band < RIPPLE_BANDS.length - 1; band++) {
                float inner = radius + width * RIPPLE_BANDS[band];
                float outer = radius + width * RIPPLE_BANDS[band + 1];
                float ai = strength * RIPPLE_ALPHA[band], ao = strength * RIPPLE_ALPHA[band + 1];
                rippleVertex(sink, inner, a, ai); rippleVertex(sink, inner, b, ai);
                rippleVertex(sink, outer, b, ao);
                rippleVertex(sink, inner, a, ai); rippleVertex(sink, outer, b, ao);
                rippleVertex(sink, outer, a, ao);
            }
        }
    }
    private static void rippleVertex(Sink sink, float radius, double angle, float opacity) {
        sink.vertex(radius * (float)Math.cos(angle), radius * (float)Math.sin(angle), -.003F,
                255, 180, 240, alpha(opacity));
    }
    private static int alpha(float value) { return Math.round(255 * Math.max(0, Math.min(1, value))); }
}
