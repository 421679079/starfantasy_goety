package com.starfantasy.goety.magic.focus;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.common.magic.spells.void_spells.VoidRiftSpell;
import com.Polarice3.Goety.utils.WandUtil;
import com.starfantasy.goety.config.SpellConfig;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class FinalArtSpell implements ISpell {
    public static final FinalArtSpell INSTANCE = new FinalArtSpell();
    @Override public SpellStat defaultStats() { return new VoidRiftSpell().defaultStats(); }
    @Override public int defaultSoulCost() { return SpellConfig.FINAL_ART_SOULS.get(); }
    @Override public int defaultCastDuration() { return SpellConfig.FINAL_ART_CAST.get(); }
    @Override public int defaultSpellCooldown() { return SpellConfig.FINAL_ART_COOLDOWN.get(); }
    @Override public SpellType getSpellType() { return SpellType.VOID; }
    @Override public List<Enchantment> acceptedEnchantments() {
        return List.of(ModEnchantments.POTENCY.get(), ModEnchantments.DURATION.get(),
                ModEnchantments.RANGE.get(), ModEnchantments.RADIUS.get());
    }
    @Override public void SpellResult(ServerLevel level, LivingEntity caster, ItemStack staff, SpellStat stats) {
        int potency = stats.getPotency();
        potency += WandUtil.getPotencyLevel(caster);
        int duration = stats.getDuration() * (WandUtil.getLevels(ModEnchantments.DURATION.get(), caster) + 1);
        double radius = 16D * (Mth.clamp(stats.getRadius()
                + WandUtil.getLevels(ModEnchantments.RADIUS.get(), caster), 0D, 64D) + 1D);
        int range = stats.getRange() + WandUtil.getRangeLevel(caster);
        float coreDamage = com.Polarice3.Goety.config.SpellConfig.RuptureDamage.get().floatValue() * WandUtil.damageMultiply();
        if (caster instanceof Mob mob && mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            coreDamage = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE) / 2F;
        }
        FinalArtEntity.spawn(level, caster, BattleFocusSpell.damagePerHit(coreDamage, potency),
                BattleFocusSpell.damagePerHit(SpellConfig.FINAL_ART_DAMAGE.get(), potency),
                SpellConfig.FINAL_ART_FINAL_HITS.get(), duration, radius, range);
    }
    private FinalArtSpell() {}
}
