package com.starfantasy.goety.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible counter: amplifier 9 is displayed as Multishot X. */
public final class ApollyonMultishotEffect extends MobEffect {
    public ApollyonMultishotEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x303030);
    }
}
