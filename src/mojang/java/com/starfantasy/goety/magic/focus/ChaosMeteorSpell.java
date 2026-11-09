package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.starfantasy.goety.combat.HaloProtection;
import com.starfantasy.goety.config.SpellConfig;
import com.starfantasy.goety.registry.HaloItemRegistry;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public final class ChaosMeteorSpell implements ISpell {
    public static final ChaosMeteorSpell INSTANCE = new ChaosMeteorSpell();
    private static final List<Float> SINGLE = List.of(0F);
    private static final List<Float> TRIPLE = List.of(0F, -22.5F, 22.5F);
    private static final List<Float> QUINTUPLE = List.of(0F, -15F, 15F, -30F, 30F);
    private static final ResourceLocation ASCIAN_MASK = new ResourceLocation("starfantasy_goety", "ascian_mask");

    @Override public int defaultSoulCost() { return SpellConfig.CHAOS_METEOR_SOULS.get(); }
    @Override public int defaultCastDuration() { return SpellConfig.CHAOS_METEOR_CAST.get(); }
    @Override public int defaultSpellCooldown() { return SpellConfig.CHAOS_METEOR_COOLDOWN.get(); }
    @Override public SpellType getSpellType() { return SpellType.NETHER; }
    @Override public SpellStat defaultStats() { return new SpellStat(0, 0, 0, 0, 0, 0); }

    @Override public List<Enchantment> acceptedEnchantments() {
        return List.of(ModEnchantments.POTENCY.get(), ModEnchantments.DURATION.get(),
                ModEnchantments.RANGE.get(), ModEnchantments.RADIUS.get(), ModEnchantments.BURNING.get());
    }

    @Override public void SpellResult(ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        ItemStack focus = IWand.getFocus(staff);
        int potency = stats.getPotency() + focus.getEnchantmentLevel(ModEnchantments.POTENCY.get());
        int duration = stats.getDuration() + focus.getEnchantmentLevel(ModEnchantments.DURATION.get());
        int rangeBonus = stats.getRange() + 2 * focus.getEnchantmentLevel(ModEnchantments.RANGE.get());
        double radius = stats.getRadius() + focus.getEnchantmentLevel(ModEnchantments.RADIUS.get());
        int burning = stats.getBurning() + focus.getEnchantmentLevel(ModEnchantments.BURNING.get());
        float damage = BattleFocusSpell.damagePerHit(SpellConfig.CHAOS_METEOR_DAMAGE.get(), potency);
        int lifetime = 45 + 10 * Mth.clamp(duration, 0, 6);
        double radiusScale = 1 + .05 * Mth.clamp(radius, 0, 4);
        int fireTicks = 40 * Mth.clamp(burning, 0, Integer.MAX_VALUE / 40);
        boolean traps = typeStaff(staff, SpellType.NETHER);

        Vec3 probe = ChaosMeteorPath.aim(level, caster, 15 + Mth.clamp(rangeBonus, 0, 30));
        for (float angle : angles(caster)) {
            // Minecraft's horizontal vector rotates in the opposite sense to yaw.
            Vec3 offset = probe.subtract(caster.position()).yRot(-angle * Mth.DEG_TO_RAD);
            Vec3 anchor = ChaosMeteorPath.anchor(level, caster, caster.position().add(offset));
            if (anchor != null) {
                ChaosMeteorEntity.spawn(level, caster, anchor, caster.getYRot() + angle,
                        lifetime, damage, radiusScale, fireTicks, traps, angle == 0);
            }
        }
    }

    private static List<Float> angles(LivingEntity caster) {
        var pyre = HaloItemRegistry.HALO_OF_THE_PYRE_LORD.get();
        if (HaloProtection.inherited(caster, pyre)) return QUINTUPLE;
        var mask = ForgeRegistries.ITEMS.getValue(ASCIAN_MASK);
        if (HaloProtection.wears(caster, pyre) || (mask != null && HaloProtection.wears(caster, mask))) return TRIPLE;
        return SINGLE;
    }

    private ChaosMeteorSpell() {}
}
