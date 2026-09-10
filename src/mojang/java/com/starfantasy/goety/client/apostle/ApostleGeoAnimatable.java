package com.starfantasy.goety.client.apostle;

import com.Polarice3.Goety.common.entities.ModEntityType;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class ApostleGeoAnimatable implements GeoReplacedEntity {
    public static final ApostleGeoAnimatable INSTANCE = new ApostleGeoAnimatable();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ApostleGeoAnimatable() {
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return ModEntityType.APOSTLE.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Goety's original ApostleModel remains the pose solver.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
