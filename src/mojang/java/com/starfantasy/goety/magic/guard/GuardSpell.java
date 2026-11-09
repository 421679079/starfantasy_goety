package com.starfantasy.goety.magic.guard;

import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.api.magic.IChargingSpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.config.SpellConfig;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** A reactive channel: player wands use GuardWandMixin; other casters use the spell lifecycle. */
public final class GuardSpell implements IChargingSpell {
    public static final GuardSpell INSTANCE = new GuardSpell();
    private GuardSpell() {}
    @Override public int defaultSoulCost() { return SpellConfig.GUARD_SOUL_COST.get(); }
    @Override public int soulCost(LivingEntity caster, ItemStack staff) { return SpellConfig.GUARD_SOUL_COST.get(); }
    @Override public int defaultCastDuration() { return SpellConfig.GUARD_DURATION.get(); }
    @Override public int castDuration(LivingEntity caster, ItemStack staff) {
        // Providers may pass a focus bag as staff. Resolve the selected focus through Goety.
        return GuardRules.duration(SpellConfig.GUARD_DURATION.get(),
                WandUtil.getLevels(ModEnchantments.DURATION.get(), caster));
    }
    public static int duration(ItemStack staff) {
        return GuardRules.duration(SpellConfig.GUARD_DURATION.get(),
                staff.getItem() instanceof IWand
                        ? IWand.getFocus(staff).getEnchantmentLevel(ModEnchantments.DURATION.get()) : 0);
    }
    @Override public void startSpell(ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        if (!(caster instanceof Player)) GuardChannel.beginSpell(caster, staff, castDuration(caster, staff));
    }
    @Override public void useSpell(ServerLevel level, LivingEntity caster, ItemStack staff, int ticks, SpellStat stats) {
        if (!(caster instanceof Player)) GuardChannel.refreshSpell(caster);
    }
    @Override public void stopSpell(ServerLevel level, LivingEntity caster, ItemStack staff, ItemStack focus, int ticks, SpellStat stats) {
        if (!(caster instanceof Player)) GuardChannel.finish(caster);
    }
    @Override public int defaultSpellCooldown() { return SpellConfig.GUARD_COOLDOWN.get(); }
    @Override public int Cooldown() { return 1; }
    @Override public SpellType getSpellType() { return SpellType.NONE; }
    @Override public List<Enchantment> acceptedEnchantments() { return List.of(ModEnchantments.DURATION.get()); }
    @Override public boolean hasCustomCooldown(LivingEntity caster, ItemStack staff, ItemStack focus, int ticks) {
        return true;
    }
}
