package com.starfantasy.goety.combat;

import com.mojang.logging.LogUtils;
import com.starfantasy.goety.config.HaloConfig;
import com.starfantasy.goety.halo.HadesHaloContents;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class HaloPotionEffects {
    public static final int DURATION = 60;
    private static final Map<ResourceLocation, CachedEffects> CACHE = new ConcurrentHashMap<>();
    private static final Map<LivingEntity,Map<MobEffect,OwnedEffect>> OWNED=new java.util.WeakHashMap<>();
    private static final int[] ROMAN_VALUES = {100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!HaloConfig.SPEC.isLoaded()) return;
        List<Item> sources = HadesHaloContents.isHades(stack) ? HadesHaloContents.potionSources(stack) : List.of(stack.getItem());
        Map<MobEffect, Integer> levels = new LinkedHashMap<>();
        for (Item halo : sources) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(halo);
            if (id == null || !id.getNamespace().equals("starfantasy_goety")) continue;
            for (ResolvedEffect effect : effects(id)) {
                levels.merge(effect.type(), effect.amplifier() + 1, Math::max);
            }
        }
        if (levels.isEmpty()) return;

        var names = Component.empty().withStyle(ChatFormatting.BLUE);
        var entries = levels.entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            Component name = entry.getKey().getDisplayName();
            names.append(entry.getValue() == 1 ? name : Component.translatable(
                    "tooltip.starfantasy_goety.halo.potion_level", name, romanLevel(entry.getValue())));
            if (entries.hasNext()) names.append(Component.translatable("tooltip.starfantasy_goety.halo.potion_separator"));
        }
        tooltip.add(Component.translatable("tooltip.starfantasy_goety.halo.potion_effects", names).withStyle(ChatFormatting.GRAY));
    }

    private static String romanLevel(int level) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            result.append(ROMAN_SYMBOLS[i].repeat(level / ROMAN_VALUES[i]));
            level %= ROMAN_VALUES[i];
        }
        return result.toString();
    }

    public static void apply(Item halo, LivingEntity wearer) {
        apply(halo,wearer,false);
    }
    public static void applyInherited(Item halo,LivingEntity wearer){apply(halo,wearer,true);}
    private static void apply(Item halo,LivingEntity wearer,boolean inherited){
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
            if(wearer.addEffect(new MobEffectInstance(effect.type(), effect.duration(), effect.amplifier(),false,true,false))){
                MobEffectInstance applied=wearer.getEffect(effect.type());
                if(applied!=null&&applied.getAmplifier()==effect.amplifier())
                    OWNED.computeIfAbsent(wearer,k->new java.util.HashMap<>()).put(effect.type(),
                            new OwnedEffect(id,applied,effect.duration(),effect.amplifier(),inherited));
            }
        }
    }

    /** Remove only the short-lived contribution this halo actually applied, preserving stronger/longer external effects. */
    public static void removeOwned(Item halo,LivingEntity wearer,List<Item> remainingSources){
        if(wearer.level().isClientSide)return;
        var owned=OWNED.get(wearer);if(owned==null)return;
        ResourceLocation id=ForgeRegistries.ITEMS.getKey(halo);
        for(var entry:new ArrayList<>(owned.entrySet())){
            var contribution=entry.getValue();
            if(!contribution.inherited()||!contribution.source().equals(id))continue;
            MobEffectInstance active=wearer.getEffect(entry.getKey());
            owned.remove(entry.getKey());
            if(active!=contribution.instance()||active==null||active.isInfiniteDuration()
                    ||active.getAmplifier()!=contribution.amplifier()||active.getDuration()>contribution.duration())continue;
            ResourceLocation replacement=remainingSources.stream().map(ForgeRegistries.ITEMS::getKey).filter(java.util.Objects::nonNull)
                    .filter(key->effects(key).stream().anyMatch(effect->effect.type()==entry.getKey()&&effect.amplifier()>=active.getAmplifier()))
                    .findFirst().orElse(null);
            if(replacement!=null)owned.put(entry.getKey(),new OwnedEffect(replacement,active,contribution.duration(),contribution.amplifier(),true));
            else wearer.removeEffect(entry.getKey());
        }
        if(owned.isEmpty())OWNED.remove(wearer);
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
    private record OwnedEffect(ResourceLocation source,MobEffectInstance instance,int duration,int amplifier,boolean inherited){}

    private HaloPotionEffects() {}
}
