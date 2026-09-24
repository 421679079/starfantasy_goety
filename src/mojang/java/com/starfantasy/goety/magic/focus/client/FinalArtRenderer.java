package com.starfantasy.goety.magic.focus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.magic.focus.FinalArtEntity;
import com.starfantasy.library.vfx.client.finalcurtain.FinalCurtainRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class FinalArtRenderer extends EntityRenderer<FinalArtEntity> {
    private final FinalCurtainRenderer<FinalArtEntity> curtain;
    public FinalArtRenderer(EntityRendererProvider.Context context) {
        super(context); curtain = new FinalCurtainRenderer<>(context);
    }
    @Override public void render(FinalArtEntity entity, float yaw, float partial, PoseStack poses, MultiBufferSource buffer, int light) {
        float age = entity.visualAge(partial);
        if (age < entity.finalTick()) curtain.render(entity, yaw, partial, poses, buffer, light);
        else FinalArtExplosion.render(age, entity.getSeed(),
                (age - entity.finalTick()) / FinalArtEntity.EXPLOSION_TICKS, poses, buffer);
    }
    @Override public ResourceLocation getTextureLocation(FinalArtEntity entity) { return curtain.getTextureLocation(entity); }
}
