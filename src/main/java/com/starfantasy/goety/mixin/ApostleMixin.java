package com.starfantasy.goety.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.hostile.cultists.SpellCastingCultist;
import com.starfantasy.goety.combat.apostle.*;
import com.starfantasy.goety.config.ApostleConfig;
import com.starfantasy.goety.compat.ApostleCompatibility;
import com.starfantasy.library.vfx.StarFantasyVfx;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = Apostle.class, remap = false)
public abstract class ApostleMixin extends SpellCastingCultist implements ApostleCastAccess {
    @Shadow public int toTeleportTime;
    @Shadow public int moddedInvul;
    @Unique private boolean starfantasy$shieldHit;
    @Unique private static final EntityDataAccessor<Boolean> starfantasy$shieldVisibleData =
            SynchedEntityData.m_135353_(Apostle.class, EntityDataSerializers.f_135035_);
    protected ApostleMixin(EntityType<? extends SpellCastingCultist> type, Level level) { super(type, level); }
    @Override public void starfantasy$castTimer(int ticks) { this.spellTicks = ticks; }
    @Override public boolean starfantasy$teleportPending() { return toTeleportTime > 0; }
    @Override public boolean starfantasy$shieldVisible() { return this.f_19804_.m_135370_(starfantasy$shieldVisibleData); }
    @Override public void starfantasy$shieldVisible(boolean visible) { this.f_19804_.m_135381_(starfantasy$shieldVisibleData, visible); }
    @Inject(method = "m_8097_", at = @At("TAIL"))
    private void starfantasy$defineShield(CallbackInfo ci) {
        this.f_19804_.m_135372_(starfantasy$shieldVisibleData, false);
    }

    @Inject(method = "m_8099_", at = @At("TAIL"))
    private void starfantasy$goals(CallbackInfo ci) {
        Apostle boss = (Apostle) (Object) this;
        if (ApostleSpellSupport.original(boss)) this.f_21345_.m_25352_(3, new ApostleTitleSpellGoal(boss));
    }
    @ModifyConstant(method = "m_6515_", constant = @Constant(doubleValue = 0.15D))
    private double starfantasy$hardMagic(double original) {
        return ApostleSpellSupport.original((Apostle) (Object) this)
                ? 1 - ApostleConfig.HARD_MAGIC_RESISTANCE.get() : original;
    }

    @Inject(method = "m_6469_", at = @At("HEAD"))
    private void starfantasy$shieldBefore(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        Apostle boss = (Apostle) (Object) this;
        starfantasy$shieldHit = ApostleSpellSupport.original(boss) && ApostleShield.get(boss) > 0;
        if (starfantasy$shieldHit) {
            this.f_19802_ = 0;
            this.moddedInvul = 0;
            ApostleShield.updateKnockback(boss, true);
        }
    }
    @Inject(method = "m_6469_", at = @At("RETURN"))
    private void starfantasy$shieldAfter(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        if (starfantasy$shieldHit) {
            this.f_19802_ = 0;
            this.moddedInvul = 0;
            starfantasy$shieldHit = false;
            ApostleShield.updateKnockback((Apostle) (Object) this, false);
        }
    }
    @Inject(method = "m_8107_", at = @At("TAIL"))
    private void starfantasy$archery(CallbackInfo ci) {
        Apostle boss = (Apostle) (Object) this;
        if (this.m_9236_().f_46443_) return;
        if (ApostleCompatibility.isRevelationApollyon(boss)) {
            // Remove only our old shield state when loading/upgrading an affected entity.
            ApostleShield.clear(boss);
            return;
        }
        if (!ApostleSpellSupport.original(boss)) return;
        ApostleShield.updateKnockback(boss, false);
        LivingEntity target = boss.m_5448_();
        if (ApostleConfig.IMPROVED_ARCHERY.get() && target != null
                && boss.m_6117_() && boss.m_21252_() == 10) {
            double dx = target.m_20185_() - boss.m_20185_();
            double dz = target.m_20189_() - boss.m_20189_();
            double length = Mth.m_14008_(Math.sqrt(dx * dx + dz * dz) + 8, 16, 40);
            StarFantasyVfx.groundRectangleWarningTrackingGroundAimed(
                    boss, target, 10, .65, length, 0, 0, 0xB00000, false, true);
        }
    }
    @ModifyArg(method = "m_8107_", at = @At(value = "INVOKE",
            target = "Lcom/Polarice3/Goety/common/entities/boss/Apostle;setSpellCycle(I)V"), index = 0)
    private int starfantasy$enhancedCycle(int cycle) {
        // Do this before the native choice of cycle 2/3, so other spells remain available.
        return cycle == 0 && ApostleSpellSupport.spellCoolingDown((Apostle) (Object) this) ? 1 : cycle;
    }
    @Redirect(method = "m_8107_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;m_8767_(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private int starfantasy$replaceOldWarning(net.minecraft.server.level.ServerLevel level,
            net.minecraft.core.particles.ParticleOptions particle, double x, double y, double z,
            int count, double dx, double dy, double dz, double speed) {
        if (particle instanceof com.Polarice3.Goety.client.particles.ShootIndicatorParticleOption
                && ApostleSpellSupport.original((Apostle) (Object) this)
                && ApostleConfig.IMPROVED_ARCHERY.get()) return 0;
        return level.m_8767_(particle, x, y, z, count, dx, dy, dz, speed);
    }
}
