package com.starfantasy.goety.combat;

import com.mojang.logging.LogUtils;
import com.starfantasy.goety.config.HaloConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public final class HaloPotionEffects {
    public static final int DURATION = 60;
    private static final Map<ResourceLocation, CachedEffects> CACHE = new ConcurrentHashMap<>();

    public static void apply(Item halo, LivingEntity wearer) {
        if (wearer.level().isClientSide || !wearer.isAlive()) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(halo);
        if (id == null || !id.getNamespace().equals("starfantasy_goety")) {
            return;
        }
        for (ResolvedEffect effect : effects(id)) {
            MobEffectInstance active = wearer.getEffect(effect.type());
            if (active != null && active.getAmplifier() >= effect.amplifier()
                    && (active.isInfiniteDuration() || active.getAmplifier() > effect.amplifier()
                        || active.getDuration() > effect.refreshThreshold())) {
                continue;
            }
            wearer.addEffect(new MobEffectInstance(effect.type(), effect.duration(), effect.amplifier()));
        }
    }

    private static List<ResolvedEffect> effects(ResourceLocation halo) {
        var setting = HaloConfig.EFFECTS.get(halo.getPath());
        if (setting == null) {
            return List.of();
        }
        List<? extends String> configured = setting.get();
        CachedEffects cached = CACHE.get(halo);
        if (cached != null && cached.configured().equals(configured)) {
            return cached.resolved();
        }
        List<ResolvedEffect> resolved = new ArrayList<>();
        for (String value : configured) {
            HaloConfig.EffectEntry entry = HaloConfig.parse(value);
            if (entry == null) {
                continue;
            }
            MobEffect type = ForgeRegistries.MOB_EFFECTS.getValue(entry.id());
            if (type != null) {
                int amplifier = entry.level() - 1;
                int period = periodicInterval(type, amplifier);
                int duration = period == 0 ? DURATION : Math.max(period, ((40 + period - 1) / period) * period);
                int refreshInterval = period == 0 ? 0 : Math.max(period, duration / (2 * period) * period);
                resolved.add(new ResolvedEffect(type, amplifier, duration, duration - refreshInterval));
            } else {
                LogUtils.getLogger().warn("Unknown potion effect {} configured for halo {}", entry.id(), halo);
            }
        }
        CachedEffects result = new CachedEffects(List.copyOf(configured), List.copyOf(resolved));
        CACHE.put(halo, result);
        return result.resolved();
    }

    /** Keep duration-based vanilla effects on their native cadence when refreshed. */
    private static int periodicInterval(MobEffect effect, int amplifier) {
        int base = effect == MobEffects.REGENERATION ? 50
                : effect == MobEffects.POISON ? 25 : effect == MobEffects.WITHER ? 40 : 0;
        return base == 0 ? 0 : Math.max(1, base >> amplifier);
    }

    private record ResolvedEffect(MobEffect type, int amplifier, int duration, int refreshThreshold) {}
    private record CachedEffects(List<String> configured, List<ResolvedEffect> resolved) {}

    private HaloPotionEffects() {}
}
