package com.starfantasy.goety.api;

/** Clears only a post-hit cooldown, never an encounter/phase lock or damage cap. */
public interface HitCooldownAccess {
    void starfantasy$clearHitCooldown();
}
