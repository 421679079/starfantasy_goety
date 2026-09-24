package com.starfantasy.goety.church;

/** Shared aperture dimensions for the visible opening and server-side entry test. */
public final class ChurchRiftShape {
    public static final double HALF_WIDTH = 6.5;
    public static final double HALF_HEIGHT = 3.4;
    // Align the opening with the player's view; the narrow bottom tip extends below their feet.
    public static final double CENTER_ABOVE_FEET = HALF_HEIGHT - 0.75;
    public static final float VISUAL_PADDING = 1.0F;
    public static final int CHARGE_TICKS = 80;
    public static final int OPEN_TICKS = 12;
    public static final int CLOSE_TICKS = 20;
    public static final int IDLE_TICKS = 300;

    private ChurchRiftShape() {}

    public static double halfWidthAt(double y) {
        double remaining = 1.0 - Math.pow(Math.abs(y) / HALF_HEIGHT, 0.68);
        return remaining <= 0.0 ? 0.0 : HALF_WIDTH * Math.pow(remaining, 1.0 / 0.68);
    }

    /** Swept body-center test: fast flight cannot skip through the portal plane. */
    public static boolean crosses(double fromX, double fromY, double fromZ,
                                  double toX, double toY, double toZ) {
        double dz = toZ - fromZ;
        double t = Math.abs(dz) > 1.0E-8 ? Math.max(0.0, Math.min(1.0, -fromZ / dz)) : 1.0;
        double x = fromX + (toX - fromX) * t;
        double y = fromY + (toY - fromY) * t;
        double z = fromZ + dz * t;
        return Math.abs(z) <= 0.55 && Math.abs(y) < HALF_HEIGHT - 0.3
                && Math.abs(x) < halfWidthAt(y) - 0.08;
    }
}
