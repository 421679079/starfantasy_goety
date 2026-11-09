package com.starfantasy.goety.item;

import com.Polarice3.Goety.api.magic.SpellType;
import com.starfantasy.goety.config.StaffConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class AscianStaffItem extends ConfigurableDarkStaffItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AscianStaffItem() {
        super(12, SpellType.NETHER, () -> StaffConfig.ASCIAN_ATTACK_DAMAGE.get(),
                () -> StaffConfig.ASCIAN_ATTACK_SPEED.get(), () -> StaffConfig.ASCIAN_ENTITY_RANGE.get());
    }

    @Override public Component m_7626_(ItemStack stack) { return AscianStaffName.create(); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        super.initializeClient(wand -> com.starfantasy.goety.client.AscianStaffItemRenderer.initialize(consumer,wand));
    }
}
