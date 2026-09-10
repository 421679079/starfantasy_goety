package com.starfantasy.goety.effect;

import java.util.List;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

/** Non-curable pageant debuff. Its server-side food drain is handled by the Forge event hook. */
public final class ApollyonFamineEffect extends MobEffect {
    public ApollyonFamineEffect() {
        super(MobEffectCategory.HARMFUL, 0x285D2A);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
