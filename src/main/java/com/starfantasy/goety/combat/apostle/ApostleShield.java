package com.starfantasy.goety.combat.apostle;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

/** Same absorption rules as Apollyon's cooperative shield, with independent storage. */
public final class ApostleShield {
    private static final String TAG = "StarFantasyApostleShield";
    private static final UUID KNOCKBACK = UUID.fromString("2ec095b2-af35-4c4e-8702-87a31a6e9c5d");
    private ApostleShield() {}
    public static void clear(Apostle boss) {
        boss.getPersistentData().m_128473_(TAG);
        if (!boss.m_9236_().f_46443_)
            ((ApostleCastAccess) boss).starfantasy$shieldVisible(false);
        AttributeInstance attr = boss.m_21051_(Attributes.f_22278_);
        if (attr != null && attr.m_22111_(KNOCKBACK) != null) attr.m_22120_(KNOCKBACK);
    }
    public static float get(Apostle boss) { return boss.getPersistentData().m_128457_(TAG); }
    public static void set(Apostle boss, float value) {
        boss.getPersistentData().m_128350_(TAG, Math.max(0, value));
        updateKnockback(boss, false);
    }
    public static void updateKnockback(Apostle boss, boolean hitInProgress) {
        if (!boss.m_9236_().f_46443_)
            ((ApostleCastAccess) boss).starfantasy$shieldVisible(get(boss) > 0);
        AttributeInstance attr = boss.m_21051_(Attributes.f_22278_);
        if (attr == null) return;
        boolean present = attr.m_22111_(KNOCKBACK) != null;
        if (get(boss) > 0 || hitInProgress) {
            if (!present) attr.m_22118_(new AttributeModifier(KNOCKBACK,
                    "starfantasy_goety.apostle_shield", 1, AttributeModifier.Operation.ADDITION));
        } else if (present) attr.m_22120_(KNOCKBACK);
    }
}
