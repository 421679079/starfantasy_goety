package com.starfantasy.goety.magic.focus.client;

/** Free-ended travelling waves. Only the material and center star are shared with the finale ring. */
public final class FlowerCastingGeometry {
    public static final int ARMS = 4, SEGMENTS = 96;
    public static final float RADIUS = 7.5F;
    private static final double[] ARM_PHASE = {0, 1.6, 2.4, .8};
    private FlowerCastingGeometry() { }

    public static float reach(float progress) {
        return Math.max(0, Math.min(1, progress));
    }

    /** Smooth 2.4-second breathing cycle, independent of cast speed and extension progress. */
    public static float pulse(float age) {
        return .5F - .5F * (float) Math.cos(age * Math.PI * 2 / 48);
    }

    public static void point(int arm, int ribbon, float u, float progress, float age,
                             float width, float side, float[] out) {
        double sx = arm < 2 ? 1 : -1, sy = (arm & 1) == 0 ? 1 : -1;
        // Negative time phase sends crests OUTWARD, never a rigid rotation of a fixed curve.
        // ~0.8 second main cycle, plus a shorter flutter travelling down the same free end.
        double time = age * .38, phase = ARM_PHASE[arm];
        double wave = u * Math.PI * 3.2 - time + phase;
        double flutter = u * Math.PI * 6.4 - time * 1.65 + phase * .7;
        double amplitude = .085 + .055 * reach(progress);
        double envelope = u * (1.2 - .2 * u);
        double oscillation = amplitude * Math.sin(wave) + .028 * Math.sin(flutter);
        double y = .38 * u + envelope * oscillation;
        double root = Math.min(1, u * 8);
        double twist = .40 * Math.sin(u * Math.PI * 3 - time * .8 + phase);
        // One continuous sheet; no offset color copies on the casting ribbons.
        double offset = side * width * (.55 + .25 * u) * root;
        double planar = offset * Math.cos(twist);
        // Sweep each cross-section in Y/Z at a fixed longitudinal X: even fast, tight waves
        // remain an open fabric sheet instead of self-intersecting at concave bends.
        out[0] = (float) (sx * u);
        out[1] = (float) (sy * (y + planar));
        out[2] = (float) (.045 * envelope * Math.sin(u * Math.PI * 2 - time * .8 + phase)
                + offset * Math.sin(twist));
    }
}
