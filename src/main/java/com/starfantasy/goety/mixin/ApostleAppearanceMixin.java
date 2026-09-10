package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.SpellCastingCultist;
import com.starfantasy.goety.api.ApostleAppearanceAccess;
import com.starfantasy.goety.registry.ApostleAppearanceSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Apostle.class, remap = false)
public abstract class ApostleAppearanceMixin extends SpellCastingCultist implements ApostleAppearanceAccess {
    @Shadow public int titleNumber;
    @Unique private static final EntityDataAccessor<Integer> starfantasy$appearanceTitle =
            SynchedEntityData.m_135353_(Apostle.class, EntityDataSerializers.f_135028_);

    protected ApostleAppearanceMixin(EntityType<? extends SpellCastingCultist> type, Level level) {
        super(type, level);
    }

    @Inject(method = "m_8097_", at = @At("TAIL"))
    private void starfantasy$defineTitle(CallbackInfo ci) {
        this.f_19804_.m_135372_(starfantasy$appearanceTitle, -1);
    }

    @Override
    public int starfantasy$visualTitle() {
        return this.f_19804_.m_135370_(starfantasy$appearanceTitle);
    }

    @Inject(method = "setTitleNumber", at = @At("TAIL"))
    private void starfantasy$titleChanged(Integer title, CallbackInfo ci) {
        starfantasy$syncTitle();
    }

    @Inject(method = "m_8107_", at = @At("HEAD"))
    private void starfantasy$trackDirectTitleWrites(CallbackInfo ci) {
        // The upstream field is public. Catch direct writes too; unchanged values send no data.
        starfantasy$syncTitle();
    }

    @Unique
    private void starfantasy$syncTitle() {
        if (!this.m_9236_().f_46443_ && starfantasy$visualTitle() != titleNumber) {
            this.f_19804_.m_135381_(starfantasy$appearanceTitle, titleNumber);
        }
    }

    @Unique
    private boolean starfantasy$isOriginal() {
        return ((Object) this).getClass() == Apostle.class;
    }

    @Inject(method = "m_7515_", at = @At("RETURN"), cancellable = true)
    private void starfantasy$ambient(CallbackInfoReturnable<SoundEvent> ci) {
        if (starfantasy$isOriginal() && ci.getReturnValue() != null)
            ci.setReturnValue(ApostleAppearanceSounds.AMBIENT.get());
    }

    @Inject(method = "m_7975_", at = @At("RETURN"), cancellable = true)
    private void starfantasy$hurt(CallbackInfoReturnable<SoundEvent> ci) {
        if (starfantasy$isOriginal() && ci.getReturnValue() != null)
            ci.setReturnValue(ApostleAppearanceSounds.HURT.get());
    }

    @Inject(method = "getTrueDeathSound", at = @At("RETURN"), cancellable = true)
    private void starfantasy$death(CallbackInfoReturnable<SoundEvent> ci) {
        if (starfantasy$isOriginal() && ci.getReturnValue() != null)
            ci.setReturnValue(ApostleAppearanceSounds.DEATH.get());
    }
}
