package com.starfantasy.goety.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Additive -0.1 knockback resistance per effect level; vanilla clamps the final attribute at zero. */
public final class UnsteadyEffect extends MobEffect {
    public UnsteadyEffect() {
        super(MobEffectCategory.HARMFUL, 0xB59B69);
        this.m_19472_(Attributes.f_22278_, "62e3fce1-475a-421b-9d4a-c6f7ae859d28", -.1D,
                AttributeModifier.Operation.ADDITION);
    }
}
