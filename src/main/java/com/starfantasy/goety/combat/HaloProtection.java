package com.starfantasy.goety.combat;

import com.Polarice3.Goety.common.effects.GoetyEffects;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.starfantasy.goety.registry.HaloItemRegistry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

public final class HaloProtection {
    public static boolean wears(Entity entity, Item halo) {
        return entity instanceof Player player
                && CuriosApi.getCuriosInventory(player).resolve()
                    .flatMap(inventory -> inventory.findFirstCurio(halo)).isPresent();
    }

    public static boolean preventsFreezing(Entity entity) {
        return entity instanceof Player && wears(entity, HaloItemRegistry.HALO_OF_THE_CRUEL.get());
    }

    public static boolean blocksEffect(Entity entity, MobEffect effect) {
        if (!(entity instanceof Player)) {
            return false;
        }
        if (effect == MobEffects.f_19614_ || effect == GoetyEffects.ACID_VENOM.get()) {
            return wears(entity, HaloItemRegistry.HALO_OF_THE_DEFILER.get());
        }
        if (effect == GoetyEffects.FREEZING.get()) {
            return preventsFreezing(entity);
        }
        if (effect == GoetyEffects.SPASMS.get()) {
            return wears(entity, HaloItemRegistry.HALO_OF_THE_TERRIBLE.get());
        }
        if (effect == GoetyEffects.VOID_TOUCHED.get()) {
            return wears(entity, HaloItemRegistry.HALO_OF_THE_DARK.get());
        }
        if (effect == MobEffects.f_216964_) {
            return wears(entity, HaloItemRegistry.HALO_OF_THE_GREAT_SHADOW.get());
        }
        return false;
    }

    /** Also remove conditions acquired before equipping or restored from saved data. */
    public static void clearBlockedEffects(Item halo, Player player) {
        if (halo == HaloItemRegistry.HALO_OF_THE_CRUEL.get()) {
            player.m_146917_(0);
            removeIfPresent(player, GoetyEffects.FREEZING.get());
        } else if (halo == HaloItemRegistry.HALO_OF_THE_DEFILER.get()) {
            removeIfPresent(player, MobEffects.f_19614_);
            removeIfPresent(player, GoetyEffects.ACID_VENOM.get());
        } else if (halo == HaloItemRegistry.HALO_OF_THE_TERRIBLE.get()) {
            removeIfPresent(player, GoetyEffects.SPASMS.get());
        } else if (halo == HaloItemRegistry.HALO_OF_THE_DARK.get()) {
            removeIfPresent(player, GoetyEffects.VOID_TOUCHED.get());
        } else if (halo == HaloItemRegistry.HALO_OF_THE_GREAT_SHADOW.get()) {
            removeIfPresent(player, MobEffects.f_216964_);
        }
    }

    private static void removeIfPresent(Player player, MobEffect effect) {
        if (player.m_21023_(effect)) {
            player.m_21195_(effect);
        }
    }

    public static float damageMultiplier(Entity entity, DamageSource source) {
        if (!(entity instanceof Player)) {
            return 1.0F;
        }
        float multiplier = 1.0F;
        if ((source.m_269533_(DamageTypeTags.f_268419_)
                || source.m_269533_(ModTags.DamageTypes.FROST_ATTACKS))
                && wears(entity, HaloItemRegistry.HALO_OF_THE_CRUEL.get())) {
            multiplier *= 0.75F;
        }
        if ((source.m_276093_(ModDamageSource.ACID) || source.m_276093_(ModDamageSource.VENOM))
                && wears(entity, HaloItemRegistry.HALO_OF_THE_DEFILER.get())) {
            multiplier *= 0.75F;
        }
        if (source.m_269533_(ModTags.DamageTypes.SHOCK_ATTACKS)
                && wears(entity, HaloItemRegistry.HALO_OF_THE_TERRIBLE.get())) {
            multiplier *= 0.75F;
        }
        if ((source.m_276093_(ModDamageSource.VOIDED) || source.m_276093_(DamageTypes.f_268724_))
                && wears(entity, HaloItemRegistry.HALO_OF_THE_DARK.get())) {
            multiplier *= 0.90F;
        }
        if (source.m_269533_(DamageTypeTags.f_268524_)
                && wears(entity, HaloItemRegistry.HALO_OF_THE_GREAT_SHADOW.get())) {
            multiplier *= 0.85F;
        }
        return multiplier;
    }

    private HaloProtection() {}
}
