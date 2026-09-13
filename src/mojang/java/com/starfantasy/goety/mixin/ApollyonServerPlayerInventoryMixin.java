package com.starfantasy.goety.mixin;

import com.starfantasy.goety.combat.ApollyonDeathInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, remap = false)
public abstract class ApollyonServerPlayerInventoryMixin {
    @Inject(method = "m_6667_(Lnet/minecraft/world/damagesource/DamageSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_46469_()Lnet/minecraft/world/level/GameRules;", ordinal = 0),
            remap = false)
    private void starfantasy$captureEncounterDeath(DamageSource source, CallbackInfo callback) {
        ApollyonDeathInventory.captureDeath((ServerPlayer)(Object)this);
    }

    @Redirect(method = "m_9015_(Lnet/minecraft/server/level/ServerPlayer;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;m_46207_(Lnet/minecraft/world/level/GameRules$Key;)Z"),
            remap = false)
    private boolean starfantasy$restoreEncounterInventory(GameRules rules, GameRules.Key<GameRules.BooleanValue> key,
                                                         ServerPlayer original, boolean alive) {
        return ApollyonDeathInventory.rule(rules, key, original);
    }

    @Inject(method = "m_9015_(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"), remap = false)
    private void starfantasy$clearEncounterDeath(ServerPlayer original, boolean alive, CallbackInfo callback) {
        ApollyonDeathInventory.clear((ServerPlayer)(Object)this);
    }
}
