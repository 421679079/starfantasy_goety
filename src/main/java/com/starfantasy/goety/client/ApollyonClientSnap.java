package com.starfantasy.goety.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Client-only endpoint for pageant teleports that must never visibly interpolate. */
public final class ApollyonClientSnap {
    private ApollyonClientSnap() {
    }

    public static void apply(
            int entityId, double x, double y, double z, float yaw, float pitch) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return;
        }
        Entity entity = minecraft.f_91073_.m_6815_(entityId);
        if (entity == null) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            living.m_6453_(x, y, z, yaw, pitch, 0, true);
        }
        entity.m_19890_(x, y, z, yaw, pitch);
    }
}
