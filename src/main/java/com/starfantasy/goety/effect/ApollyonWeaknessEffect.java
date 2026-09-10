package com.starfantasy.goety.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Reduces final outgoing damage by ten percent per level. */
public final class ApollyonWeaknessEffect extends MobEffect {
    public ApollyonWeaknessEffect() {
        super(MobEffectCategory.HARMFUL, 0x484D48);
    }
}
