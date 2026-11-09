package com.starfantasy.goety.halo.client;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Final Staff's four great-circle orbits, stretched over a shallow eye-local ellipsoid. */
public final class HadesHaloOrbit {
    public static final int TRAIL_SEGMENTS = 6;
    public static final float TRAIL_STEP = 15F;
    public static final float DEGREES_PER_TICK = 3F;
    public static final float HALF_DEPTH = .25F;
    public static final float TRAIL_WIDTH_TO_SIZE = .045F;
    private static final float[] PHASES = {0F, 105F, 0F, 105F};
    private static final Quaternionf[] ROTATIONS = rotations();
    public static final int[] LIGHT = {0x71D4C5, 0xD41023, 0xA4669C, 0x909090,
            0xF1F1F3, 0xF759B8, 0xFF9B25, 0x4CD962, 0x95D6F1, 0xFCA5B8, 0xE7C870, 0xA6C3EF};

    public static float degrees(int slot, float time) {
        return (slot / 4) * 120F + PHASES[slot % 4] + (time % 120F) * DEGREES_PER_TICK;
    }

    private static Quaternionf[] rotations() {
        Quaternionf[] result = new Quaternionf[4];
        for (int plane = 0; plane < result.length; plane++) {
            result[plane] = new Quaternionf().rotationY((float)Math.toRadians(45F + plane * 90F))
                    .rotateX((float)Math.acos(1D / Math.sqrt(3D)));
        }
        return result;
    }

    public static Vector3f point(int slot, float degrees, float width, float height, Vector3f target) {
        double angle = Math.toRadians(degrees);
        return target.set((float)Math.cos(angle), 0, (float)Math.sin(angle))
                .rotate(ROTATIONS[slot % 4]).mul(width * .5F, height * .5F, HALF_DEPTH);
    }

    /** Same rigid plane orientation as Final Staff; stretch only the orbit, not the icon. */
    public static Quaternionf orientation(int slot, float degrees, Quaternionf target) {
        return target.set(ROTATIONS[slot % 4]).rotateY((float)Math.toRadians(-degrees - 90F));
    }

    public static float fade(int segment) {
        return Math.max(0, 1F - (float)segment / TRAIL_SEGMENTS);
    }

    private HadesHaloOrbit() {}
}
