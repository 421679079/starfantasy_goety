package com.starfantasy.goety.animation;

import com.starfantasy.goety.entity.ApollyonServantEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.state.BoneSnapshot;

import java.util.Map;

/** Name-only appearance; its action clock follows the servant's authoritative spell timeline. */
public final class ApollyonPigAnimationController extends AnimationController<ApollyonServantEntity> {
    // The supplied asset is 3.6 seconds (72 ticks), including its final return to idle.
    public static final double FEED_TICKS = 72.0D;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.apollyon_pig.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.apollyon_pig.walk");
    private static final RawAnimation FEED = RawAnimation.begin().thenPlayAndHold("animation.apollyon_pig.feed");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.apollyon_pig.death");
    private long lastCastStart = Long.MIN_VALUE;
    private boolean wasPig;
    private float partialTick;

    public ApollyonPigAnimationController(ApollyonServantEntity entity) {
        super(entity, "pig", 0, state -> ((ApollyonPigAnimationController) state.getController()).select(state));
    }

    private PlayState select(AnimationState<ApollyonServantEntity> state) {
        if (!this.animatable.isPigVariant()) {
            this.wasPig = false;
            this.lastCastStart = Long.MIN_VALUE;
            return PlayState.STOP;
        }
        if (!this.wasPig) {
            this.forceAnimationReset();
            this.wasPig = true;
        }
        RawAnimation animation;
        double speed = 1.0D;
        if (!this.animatable.m_6084_()) {
            animation = DEATH;
        } else if (this.animatable.isCastingAction() && this.animatable.castingAnimationDuration() > 0) {
            long start = this.animatable.castingAnimationStart();
            if (start != this.lastCastStart) this.forceAnimationReset();
            this.lastCastStart = start;
            speed = FEED_TICKS / this.animatable.castingAnimationDuration();
            animation = FEED;
        } else {
            this.lastCastStart = Long.MIN_VALUE;
            // Bow use never selects an attack pose; the pig keeps idling or walking.
            animation = !this.animatable.isStaying() && state.isMoving() ? WALK : IDLE;
        }
        if (this.getAnimationSpeed() != speed) this.setAnimationSpeed(speed);
        this.setAnimation(animation);
        return PlayState.CONTINUE;
    }

    @Override
    public void process(CoreGeoModel<ApollyonServantEntity> model,
                        AnimationState<ApollyonServantEntity> state,
                        Map<String, CoreGeoBone> bones, Map<String, BoneSnapshot> snapshots,
                        double seekTime, boolean crashOnMissingBone) {
        this.partialTick = state.getPartialTick();
        super.process(model, state, bones, snapshots, seekTime, crashOnMissingBone);
    }

    @Override
    protected double adjustTick(double seekTime) {
        double tick = super.adjustTick(seekTime);
        // Preserve transition setup, but keep the same clock while holding the final frame.
        // Falling back to GeckoLib's local clock in PAUSED would briefly rewind a finished clip.
        if ((this.getAnimationState() != State.RUNNING && this.getAnimationState() != State.PAUSED)
                || !this.animatable.isPigVariant()) return tick;
        if (FEED.equals(this.getCurrentRawAnimation()) && this.animatable.isCastingAction()
                && this.animatable.castingAnimationDuration() > 0) {
            double elapsed = this.animatable.m_9236_().m_46467_()
                    - this.animatable.castingAnimationStart() + this.partialTick;
            // Late tracking or renaming during a spell joins the current frame, not a fresh 72-tick clip.
            return Math.max(0.0D, Math.min(FEED_TICKS,
                    elapsed * FEED_TICKS / this.animatable.castingAnimationDuration()));
        }
        if (DEATH.equals(this.getCurrentRawAnimation()) && !this.animatable.m_6084_()) {
            return Math.max(0.0D, this.animatable.deathAge() - 1 + this.partialTick);
        }
        return tick;
    }
}
