package com.starfantasy.goety.combat.ascian;

/** Encounter rules are internal; only the boss attributes and phase effects are configurable. */
public final class AscianCombatTuning {
    public static final int GUARDS = 4, FINAL_GUARDS = 8, STUN_TICKS = 100,
            ATTACK_COOLDOWN = 0, METEOR_WARNING = 40;
    public static final double DEBUFF_RADIUS = 64;
    public static final int TRANSITION_HIT = 46, TRANSITION_DURATION = 60, TRANSITION_RING_INTERVAL = 2;

    public static int transitionRingAt(int age) {
        int elapsed = age - TRANSITION_HIT;
        return elapsed >= 0 && elapsed < 4 * TRANSITION_RING_INTERVAL && elapsed % TRANSITION_RING_INTERVAL == 0
                ? elapsed / TRANSITION_RING_INTERVAL : -1;
    }

    public static double meleeDamage(int index) {
        return switch (index) {
            case 0, 1 -> 15;
            case 2 -> 25;
            case 3 -> 40;
            default -> throw new IllegalArgumentException("Invalid melee index: " + index);
        };
    }

    private AscianCombatTuning() {}
}
