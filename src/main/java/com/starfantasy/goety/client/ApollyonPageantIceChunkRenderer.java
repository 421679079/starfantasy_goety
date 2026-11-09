package com.starfantasy.goety.client;

import com.starfantasy.goety.entity.ApollyonPageantIceChunkEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Two-times Goety IceChunk model while preserving its rotating formation render. */
public final class ApollyonPageantIceChunkRenderer extends LocalIceChunkRenderer<ApollyonPageantIceChunkEntity> {
    public ApollyonPageantIceChunkRenderer(EntityRendererProvider.Context context) {
        super(context,2);
        this.f_114477_ = 8.0F;
    }

}
