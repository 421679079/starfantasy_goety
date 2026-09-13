package com.starfantasy.goety.mixin;

import com.starfantasy.goety.client.HadesKeyMappings;
import com.starfantasy.goety.entity.HadesServantEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPacketListener.class, remap = false)
public abstract class HadesRidingHintMixin {
    @Redirect(method = "m_6403_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;m_237110_(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;",
            remap = false), remap = false)
    private MutableComponent starfantasy$correctDismountHint(String key, Object[] arguments) {
        var player = Minecraft.m_91087_().f_91074_;
        return player != null && player.m_20202_() instanceof HadesServantEntity
                ? HadesKeyMappings.hint() : Component.m_237110_(key, arguments);
    }
}
