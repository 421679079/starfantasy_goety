package com.starfantasy.goety.mixin;

import com.starfantasy.goety.combat.ApollyonDeathInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Player.class, remap = false)
public abstract class ApollyonPlayerInventoryMixin {
    @Redirect(method = {"m_5907_()V", "m_213860_()I"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;m_46207_(Lnet/minecraft/world/level/GameRules$Key;)Z"),
            remap = false)
    private boolean starfantasy$keepEncounterInventory(GameRules rules, GameRules.Key<GameRules.BooleanValue> key) {
        return ApollyonDeathInventory.rule(rules, key, (Player)(Object)this);
    }
}
