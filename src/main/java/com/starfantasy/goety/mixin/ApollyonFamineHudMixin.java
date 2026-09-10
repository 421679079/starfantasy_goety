package com.starfantasy.goety.mixin;

import com.starfantasy.goety.registry.ApollyonEffectRegistry;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Makes the custom Famine effect select vanilla Hunger's green food-bar sprites. */
@Mixin(value = Gui.class, remap = false)
public abstract class ApollyonFamineHudMixin {
    @Redirect(
            method = "m_280173_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;m_21023_(Lnet/minecraft/world/effect/MobEffect;)Z",
                    remap = false),
            remap = false)
    private boolean starfantasy$famineUsesHungerFoodBar(Player player, MobEffect queriedEffect) {
        return player.m_21023_(queriedEffect)
                || queriedEffect == MobEffects.f_19612_
                && player.m_21023_(ApollyonEffectRegistry.FAMINE.get());
    }
}
