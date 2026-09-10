package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.AbstractFungusRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.goety.entity.ApollyonPageantMeteorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class ApollyonPageantMeteorRenderer
        extends AbstractFungusRenderer<ApollyonPageantMeteorEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("goety", "textures/entity/projectiles/pyroclast.png");

    public ApollyonPageantMeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 1.5F;
    }

    @Override
    protected void scale(ApollyonPageantMeteorEntity entity, PoseStack poseStack, float scale) {
        poseStack.m_85841_(3.0F, 3.0F, 3.0F);
    }

    @Override
    public ResourceLocation m_5478_(ApollyonPageantMeteorEntity entity) {
        return TEXTURE;
    }
}
