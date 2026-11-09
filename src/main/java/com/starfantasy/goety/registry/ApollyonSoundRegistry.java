package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ApollyonSoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.Keys.SOUND_EVENTS, StarFantasyGoetyMod.MODID);

    public static final RegistryObject<SoundEvent> APOLLYON_BGM = SOUNDS.register(
            "apollyon_bgm",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "apollyon_bgm")));

    public static final RegistryObject<SoundEvent> DEATH_EXPLOSION = SOUNDS.register(
            "death_explosion",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "death_explosion")));
    public static final RegistryObject<SoundEvent> ASCIAN_KING = SOUNDS.register("ascian_king",
            () -> SoundEvent.m_262824_(new ResourceLocation(StarFantasyGoetyMod.MODID,"ascian_king")));
    public static final RegistryObject<SoundEvent> FIRE_BURST = SOUNDS.register("fire_burst",
            () -> SoundEvent.m_262824_(new ResourceLocation(StarFantasyGoetyMod.MODID,"fire_burst")));
    public static final RegistryObject<SoundEvent> CHAOS_METEOR = SOUNDS.register("chaos_meteor",
            () -> SoundEvent.m_262824_(new ResourceLocation(StarFantasyGoetyMod.MODID,"chaos_meteor")));
    public static final RegistryObject<SoundEvent> ICE_BOLT_START = SOUNDS.register("ice_bolt_start",
            () -> SoundEvent.m_262824_(new ResourceLocation(StarFantasyGoetyMod.MODID,"ice_bolt_start")));
    public static final RegistryObject<SoundEvent> ICE_BOLT_HIT = SOUNDS.register("ice_bolt_hit",
            () -> SoundEvent.m_262824_(new ResourceLocation(StarFantasyGoetyMod.MODID,"ice_bolt_hit")));
    public static final RegistryObject<SoundEvent> CAST_PROFANE = SOUNDS.register(
            "cast_profane",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "cast_profane")));
    public static final RegistryObject<SoundEvent> CAST_GLORIOUS = SOUNDS.register(
            "cast_glorious",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "cast_glorious")));
    public static final RegistryObject<SoundEvent> CAST_OBSIDIAN = SOUNDS.register(
            "cast_obsidian",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "cast_obsidian")));
    public static final RegistryObject<SoundEvent> CAST_HADES = SOUNDS.register(
            "cast_hades",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "cast_hades")));
    public static final RegistryObject<SoundEvent> SUMMON_HADES = SOUNDS.register(
            "summon_hades",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "summon_hades")));
    public static final RegistryObject<SoundEvent> ATTACK_HADES = SOUNDS.register(
            "attack_hades",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "attack_hades")));
    public static final RegistryObject<SoundEvent> SUMMON_APOSTLE = SOUNDS.register(
            "summon_apostle",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "summon_apostle")));
    public static final RegistryObject<SoundEvent> SUMMON_START = SOUNDS.register(
            "summon_start",
            () -> SoundEvent.m_262824_(new ResourceLocation(
                    StarFantasyGoetyMod.MODID, "summon_start")));

    private ApollyonSoundRegistry() {
    }

    public static void init(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
