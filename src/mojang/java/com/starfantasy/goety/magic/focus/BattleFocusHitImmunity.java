package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.boss.EnderKeeper;
import com.Polarice3.Goety.common.entities.boss.Vizier;
import com.starfantasy.goety.api.HitCooldownAccess;
import com.starfantasy.library.combat.CombatHealthEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/** Used by our battle foci; no global damage hook or third-party reflection. */
final class BattleFocusHitImmunity {
    static void clear(LivingEntity target) {
        if (target == null || target.level().isClientSide) return;
        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;

        var id = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (id == null) return;
        String namespace = id.getNamespace();
        boolean ours = namespace.equals("star_fantasy_bosses") || namespace.equals("starfantasy_goety");
        // Other mods may adopt the library interface: they are NOT implicitly opted in here.
        if (ours) {
            if (target instanceof CombatHealthEntity boss) boss.combatHealthProtection().setInvulnerabilityTicks(0);
            if (target instanceof HitCooldownAccess access) access.starfantasy$clearHitCooldown();
        }
        if (namespace.equals("goety") || ours) {
            // Goety's own post-hit timer only. Leave obsidianInvul, spawn/phase invulnerability,
            // shields, damage limits and mixin-added third-party timers untouched.
            if (target instanceof Apostle apostle) apostle.moddedInvul = 0;
            else if (target instanceof EnderKeeper keeper) keeper.moddedInvul = 0;
            else if (target instanceof Vizier vizier) vizier.moddedInvul = 0;
        }
    }

    private BattleFocusHitImmunity() { }
}
