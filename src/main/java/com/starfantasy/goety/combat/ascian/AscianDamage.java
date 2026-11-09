package com.starfantasy.goety.combat.ascian;

/** Damage profiles shared by spells, projectiles and arena hazards. */
public final class AscianDamage {
    public enum Element { HELLFIRE, FROST, LIGHTNING, MAGIC, WITHER }

    public record Profile(double base, double maxHealthFraction, Element element) {
        public float amount(double targetMaxHealth, double multiplier) {
            return (float) (base * multiplier + targetMaxHealth * maxHealthFraction);
        }
    }

    public static Profile spell(int style) {
        return new Profile(30, .10, element(style));
    }

    public static Profile phantomBlade(){return new Profile(30,.10,Element.MAGIC);}

    public static float meleeAmount(int index,double targetMaxHealth,double multiplier){
        return (float)(AscianCombatTuning.meleeDamage(index)*multiplier+targetMaxHealth*.10);
    }

    public static Profile barrage(int style, int phase) {
        return new Profile(15, .05, element(style));
    }

    public static Profile ambience(int style) {
        return new Profile(10, 0, element(style));
    }

    public static Profile transition(int phase) {
        return new Profile(60, .20, phase == 6 ? Element.WITHER : Element.MAGIC);
    }

    private static Element element(int style) {
        return switch (style) {
            case 1 -> Element.HELLFIRE;
            case 2 -> Element.FROST;
            case 3 -> Element.LIGHTNING;
            case 4 -> Element.MAGIC;
            case 5 -> Element.WITHER;
            default -> throw new IllegalArgumentException("Invalid spell style: " + style);
        };
    }

    private AscianDamage() {}
}
