package com.starfantasy.goety.combat;

import java.util.UUID;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Temporary casting defense; never changes the caster's base attributes. */
public final class VoidRayKnockback {
    private static final UUID MODIFIER = UUID.fromString("45e246a3-58a6-40da-aecd-f1198681bebf");
    private VoidRayKnockback() {}

    public static void setCasting(Mob caster, boolean casting) {
        if (caster.m_9236_().f_46443_) return;
        var attribute = caster.m_21051_(Attributes.f_22278_);
        if (attribute == null) return;
        boolean present = attribute.m_22111_(MODIFIER) != null;
        if (casting && !present) {
            attribute.m_22118_(new AttributeModifier(MODIFIER,
                    "starfantasy_goety.void_ray_knockback", 1.0D, AttributeModifier.Operation.ADDITION));
        } else if (!casting && present) attribute.m_22120_(MODIFIER);
    }
}
