package com.starfantasy.goety.client;

import com.Polarice3.Goety.client.render.IceChunkRenderer;
import com.Polarice3.Goety.client.render.BlossomThornRenderer;
import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.registry.ApollyonParticleRegistry;
import com.starfantasy.goety.registry.ApollyonEntityRegistry;
import com.starfantasy.goety.registry.HadesEntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = StarFantasyGoetyMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(com.Polarice3.Goety.common.entities.ModEntityType.APOSTLE.get(),
                com.starfantasy.goety.client.apostle.ConfigurableApostleRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOSTLE_FANG.get(), ApostleFangRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOLLYON_FANG.get(), ApostleFangRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOSTLE_METEOR.get(), ApostleMeteorRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOSTLE_BEAM.get(), ApostleBeamRenderer::new);
        event.registerEntityRenderer(com.Polarice3.Goety.common.entities.ModEntityType.DEATH_ARROW.get(), ApostleDeathArrowRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOLLYON.get(), ApollyonGeoRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_STAR_ARROW.get(), ApollyonStarArrowRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_DEATH_ARROW.get(), ApollyonDeathArrowRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_CASTING_LIGHTNING.get(),
                ApollyonCastingLightningRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_CLEAVE_EFFECT.get(),
                ApollyonCleaveEffectRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.HADES_CLAW_SLASH.get(),
                HadesClawSlashRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.HADES_DIVE_RAY_LASER.get(),
                HadesDiveRayLaserRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_ICE_CHUNK.get(), IceChunkRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.APOSTLE_ICE_CHUNK.get(), IceChunkRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_SECTOR_EFFECT.get(),
                ApollyonSectorEffectRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_SUMMON.get(),
                ApollyonPageantSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_RISEN.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_ABHORRENT.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_DEFILER.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_DARK.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_GREAT_SHADOW.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_WITCH_KING.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_PYRE_LORD.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_PROFANE.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_CRUEL.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_TERRIBLE.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_GLORIOUS.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(ApollyonEntityRegistry.SUMMON_APOSTLE_ATROCIOUS.get(),
                DirectedApostleSummonRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_HALO.get(),
                ApollyonPageantHaloRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_FAMINE_WAVE.get(),
                ApollyonFamineWaveRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOSTLE.get(),
                ApollyonPageantApostleRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOSTLE_ILLUSION.get(),
                ApollyonPageantApostleRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_METEOR.get(),
                ApollyonPageantMeteorRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_MAGMA.get(),
                ApollyonPageantMagmaRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_GLORIOUS_SPHERE.get(),
                ApollyonGloriousSphereRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_BEAM.get(),
                ApollyonPageantBeamRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_OBSIDIAN_MONOLITH.get(),
                ApollyonPageantObsidianMonolithRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_ICE_CHUNK.get(),
                ApollyonPageantIceChunkRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_BLUE_ICE.get(),
                ApollyonPageantBlueIceRenderer::new);
        event.registerEntityRenderer(
                ApollyonEntityRegistry.APOLLYON_PAGEANT_THORN.get(),
                BlossomThornRenderer::new);
        event.registerEntityRenderer(HadesEntityRegistry.HADES.get(), HadesGeoRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ApollyonParticleRegistry.CASTING_SMOKE.get(),
                ApollyonCastingSmokeParticle.Provider::new);
        event.registerSpriteSet(ApollyonParticleRegistry.PROFANE_SPELL.get(),
                ApollyonProfaneSpellParticle.Provider::new);
    }
}
