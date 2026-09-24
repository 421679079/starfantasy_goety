package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.config.SpellConfig;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import com.starfantasy.goety.magic.focus.entity.FlowerCastingEntity;

/** Normal Goety casts; results launch independent server-owned timelines. */
public final class BattleFocusSpell implements ISpell {
    public static final BattleFocusSpell FLOWER = new BattleFocusSpell(true);
    public static final BattleFocusSpell EVERNIGHT = new BattleFocusSpell(false);
    private final boolean flower;
    private BattleFocusSpell(boolean flower) { this.flower = flower; }
    @Override public int defaultSoulCost() {
        return flower ? SpellConfig.FLOWER_SOULS.get() : SpellConfig.EVERNIGHT_SOULS.get();
    }
    @Override public int defaultCastDuration() {
        return flower ? SpellConfig.FLOWER_CAST.get() : SpellConfig.EVERNIGHT_CAST.get();
    }
    @Override public int defaultSpellCooldown() {
        return flower ? SpellConfig.FLOWER_COOLDOWN.get() : SpellConfig.EVERNIGHT_COOLDOWN.get();
    }
    @Override public SpellType getSpellType() { return flower ? SpellType.NONE : SpellType.NECROMANCY; }
    @Override public List<Enchantment> acceptedEnchantments() { return List.of(ModEnchantments.POTENCY.get()); }
    public static float damagePerHit(double base, int potency) {
        return (float) (base * (1D + Math.max(0, potency) * 0.05D));
    }
    @Override public void useParticle(Level level, LivingEntity caster, ItemStack staff) {
        // Flower owns its entire windup visual; Evernight keeps Goety's default particles.
        if (!flower) ISpell.super.useParticle(level, caster, staff);
    }
    @Override public void useSpell(ServerLevel level, LivingEntity caster, ItemStack staff, int ticks, SpellStat stats) {
        if (flower) FlowerCastingEntity.refresh(level, caster, ticks, staff.getUseDuration());
    }
    @Override public void stopSpell(ServerLevel level, LivingEntity caster, ItemStack staff, ItemStack focus, int ticks, SpellStat stats) {
        if (flower) FlowerCastingEntity.finish(level, caster);
        // Goety's default is empty; delegating to it recurses under RevelationFix's redirect.
    }
    @Override public void SpellResult(ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        // Follow Goety's native spells: attributes/default stats plus effective focus
        // potency, including virtual enchantment levels supplied by staff spell power.
        int potency = stats.getPotency();
        if (WandUtil.enchantedFocus(caster)) potency += WandUtil.getPotencyLevel(caster);
        if (flower) FlowerArrowRain.spawn(level, caster,
                damagePerHit(SpellConfig.FLOWER_DAMAGE.get(), potency), SpellConfig.FLOWER_FINAL_HITS.get());
        else EvernightCast.spawn(level, caster, damagePerHit(SpellConfig.EVERNIGHT_DAMAGE.get(), potency), SpellConfig.EVERNIGHT_TOTAL_HITS.get());
    }
}
