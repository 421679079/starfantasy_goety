package com.starfantasy.goety.registry;

import com.starfantasy.goety.StarFantasyGoetyMod;
import com.starfantasy.goety.entity.ApollyonEntity;
import com.starfantasy.goety.entity.ApollyonServantEntity;
import com.starfantasy.goety.entity.ApollyonCleaveEffectEntity;
import com.starfantasy.goety.entity.ApollyonCastingLightningEntity;
import com.starfantasy.goety.entity.ApollyonDeathArrowEntity;
import com.starfantasy.goety.entity.ApollyonFamineWaveEntity;
import com.starfantasy.goety.entity.ApollyonFangEntity;
import com.starfantasy.goety.entity.ApollyonGloriousSphereEntity;
import com.starfantasy.goety.entity.ApollyonIceChunkEntity;
import com.starfantasy.goety.entity.ApollyonPageantApostleEntity;
import com.starfantasy.goety.entity.ApollyonPageantBeamEntity;
import com.starfantasy.goety.entity.ApollyonPageantBlueIceEntity;
import com.starfantasy.goety.entity.ApollyonPageantIceChunkEntity;
import com.starfantasy.goety.entity.ApollyonPageantHaloEntity;
import com.starfantasy.goety.entity.ApollyonPageantMagmaEntity;
import com.starfantasy.goety.entity.ApollyonPageantMeteorEntity;
import com.starfantasy.goety.entity.ApollyonPageantObsidianMonolithEntity;
import com.starfantasy.goety.entity.ApollyonPageantSummonEntity;
import com.starfantasy.goety.entity.ApollyonPageantThornEntity;
import com.starfantasy.goety.entity.ApollyonSectorEffectEntity;
import com.starfantasy.goety.entity.ApollyonStarArrowEntity;
import com.starfantasy.goety.entity.DirectedApostleSummonEntity;
import com.starfantasy.goety.entity.HadesClawSlashEntity;
import com.starfantasy.goety.entity.HadesDiveRayLaserEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registers this mod's Apollyon under the shared Star Fantasy content namespace. */
public final class ApollyonEntityRegistry {
    public static final String NAMESPACE = StarFantasyGoetyMod.CONTENT_NAMESPACE;

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES, NAMESPACE);

    public static final RegistryObject<EntityType<ApollyonEntity>> APOLLYON =
            ENTITY_TYPES.register("apollyon", () -> EntityType.Builder
                    .m_20704_(ApollyonEntity::new, MobCategory.MONSTER)
                    .m_20699_(0.6F, 1.95F)
                    .m_20702_(12)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon").toString()));

    public static final RegistryObject<EntityType<ApollyonServantEntity>> APOLLYON_SERVANT =
            ENTITY_TYPES.register("apollyon_servant", () -> EntityType.Builder
                    .m_20704_(ApollyonServantEntity::new, MobCategory.CREATURE)
                    .m_20699_(0.6F, 1.95F).m_20702_(12).m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_servant").toString()));

    public static final RegistryObject<EntityType<ApollyonStarArrowEntity>> APOLLYON_STAR_ARROW =
            ENTITY_TYPES.register("apollyon_star_arrow", () -> EntityType.Builder
                    .<ApollyonStarArrowEntity>m_20704_(ApollyonStarArrowEntity::new, MobCategory.MISC)
                    .m_20699_(0.5F, 0.5F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_star_arrow").toString()));

    public static final RegistryObject<EntityType<ApollyonFangEntity>> APOLLYON_FANG =
            ENTITY_TYPES.register("apollyon_fang", () -> EntityType.Builder
                    .<ApollyonFangEntity>m_20704_(ApollyonFangEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(1.0F, 1.6F)
                    .m_20702_(128)
                    .m_20717_(2)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_fang").toString()));

    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.ApostleMeteorEntity>> APOSTLE_METEOR =
            ENTITY_TYPES.register("apostle_meteor", () -> EntityType.Builder
                    .<com.starfantasy.goety.entity.ApostleMeteorEntity>m_20704_(com.starfantasy.goety.entity.ApostleMeteorEntity::new, MobCategory.MISC)
                    .m_20699_(.5F, .5F).m_20702_(128).m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle_meteor").toString()));
    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.ApostleFangEntity>> APOSTLE_FANG =
            ENTITY_TYPES.register("apostle_fang", () -> EntityType.Builder
                    .<com.starfantasy.goety.entity.ApostleFangEntity>m_20704_(com.starfantasy.goety.entity.ApostleFangEntity::new, MobCategory.MISC)
                    .m_20716_().m_20699_(1, 1.6F).m_20702_(128).m_20717_(2)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle_fang").toString()));
    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.ApostleBeamEntity>> APOSTLE_BEAM =
            ENTITY_TYPES.register("apostle_beam", () -> EntityType.Builder
                    .<com.starfantasy.goety.entity.ApostleBeamEntity>m_20704_(com.starfantasy.goety.entity.ApostleBeamEntity::new, MobCategory.MISC)
                    .m_20716_().m_20699_(.1F, .1F).m_20702_(128).m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle_beam").toString()));

    public static final RegistryObject<EntityType<ApollyonDeathArrowEntity>> APOLLYON_DEATH_ARROW =
            ENTITY_TYPES.register("apollyon_death_arrow", () -> EntityType.Builder
                    .<ApollyonDeathArrowEntity>m_20704_(ApollyonDeathArrowEntity::new, MobCategory.MISC)
                    .m_20699_(0.5F, 0.5F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_death_arrow").toString()));

    public static final RegistryObject<EntityType<ApollyonCastingLightningEntity>> APOLLYON_CASTING_LIGHTNING =
            ENTITY_TYPES.register("apollyon_casting_lightning", () -> EntityType.Builder
                    .<ApollyonCastingLightningEntity>m_20704_(ApollyonCastingLightningEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_casting_lightning").toString()));

    public static final RegistryObject<EntityType<ApollyonCleaveEffectEntity>> APOLLYON_CLEAVE_EFFECT =
            ENTITY_TYPES.register("apollyon_cleave_effect", () -> EntityType.Builder
                    .<ApollyonCleaveEffectEntity>m_20704_(
                            ApollyonCleaveEffectEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(
                            NAMESPACE, "apollyon_cleave_effect").toString()));

    public static final RegistryObject<EntityType<HadesClawSlashEntity>> HADES_CLAW_SLASH =
            ENTITY_TYPES.register("hades_claw_slash", () -> EntityType.Builder
                    .<HadesClawSlashEntity>m_20704_(HadesClawSlashEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(
                            NAMESPACE, "hades_claw_slash").toString()));

    public static final RegistryObject<EntityType<HadesDiveRayLaserEntity>> HADES_DIVE_RAY_LASER =
            ENTITY_TYPES.register("hades_dive_ray_laser", () -> EntityType.Builder
                    .<HadesDiveRayLaserEntity>m_20704_(
                            HadesDiveRayLaserEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(
                            NAMESPACE, "hades_dive_ray_laser").toString()));

    public static final RegistryObject<EntityType<ApollyonIceChunkEntity>> APOLLYON_ICE_CHUNK =
            ENTITY_TYPES.register("apollyon_ice_chunk", () -> EntityType.Builder
                    .<ApollyonIceChunkEntity>m_20704_(ApollyonIceChunkEntity::new, MobCategory.MISC)
                    .m_20719_()
                    .m_20699_(2.0F, 1.5F)
                    .m_20702_(10)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_ice_chunk").toString()));

    public static final RegistryObject<EntityType<com.starfantasy.goety.entity.ApostleIceChunkEntity>> APOSTLE_ICE_CHUNK =
            ENTITY_TYPES.register("apostle_ice_chunk", () -> EntityType.Builder
                    .<com.starfantasy.goety.entity.ApostleIceChunkEntity>m_20704_(com.starfantasy.goety.entity.ApostleIceChunkEntity::new, MobCategory.MISC)
                    .m_20719_().m_20699_(2.0F, 1.5F).m_20702_(10).m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle_ice_chunk").toString()));

    public static final RegistryObject<EntityType<ApollyonSectorEffectEntity>> APOLLYON_SECTOR_EFFECT =
            ENTITY_TYPES.register("apollyon_sector_effect", () -> EntityType.Builder
                    .<ApollyonSectorEffectEntity>m_20704_(ApollyonSectorEffectEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(20)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_sector_effect").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantSummonEntity>> APOLLYON_PAGEANT_SUMMON =
            ENTITY_TYPES.register("apollyon_pageant_summon", () -> EntityType.Builder
                    .<ApollyonPageantSummonEntity>m_20704_(ApollyonPageantSummonEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(2.0F, 4.1F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_pageant_summon").toString()));

    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_RISEN = registerDirectedApostleSummon("risen", 0);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_ABHORRENT = registerDirectedApostleSummon("abhorrent", 1);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_DEFILER = registerDirectedApostleSummon("defiler", 2);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_DARK = registerDirectedApostleSummon("dark", 3);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_GREAT_SHADOW = registerDirectedApostleSummon("great_shadow", 4);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_WITCH_KING = registerDirectedApostleSummon("witch_king", 5);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_PYRE_LORD = registerDirectedApostleSummon("pyre_lord", 6);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_PROFANE = registerDirectedApostleSummon("profane", 7);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_CRUEL = registerDirectedApostleSummon("cruel", 8);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_TERRIBLE = registerDirectedApostleSummon("terrible", 9);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_GLORIOUS = registerDirectedApostleSummon("glorious", 10);
    public static final RegistryObject<EntityType<DirectedApostleSummonEntity>>
            SUMMON_APOSTLE_ATROCIOUS = registerDirectedApostleSummon("atrocious", 11);

    public static final RegistryObject<EntityType<ApollyonPageantHaloEntity>> APOLLYON_PAGEANT_HALO =
            ENTITY_TYPES.register("apollyon_pageant_halo", () -> EntityType.Builder
                    .<ApollyonPageantHaloEntity>m_20704_(
                            ApollyonPageantHaloEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(
                            NAMESPACE, "apollyon_pageant_halo").toString()));

    public static final RegistryObject<EntityType<ApollyonFamineWaveEntity>> APOLLYON_FAMINE_WAVE =
            ENTITY_TYPES.register("apollyon_famine_wave", () -> EntityType.Builder
                    .<ApollyonFamineWaveEntity>m_20704_(ApollyonFamineWaveEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_famine_wave").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantApostleEntity>> APOSTLE =
            ENTITY_TYPES.register("apostle", () -> EntityType.Builder
                    .<ApollyonPageantApostleEntity>m_20704_(ApollyonPageantApostleEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.6F, 1.95F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantApostleEntity>> APOSTLE_ILLUSION =
            ENTITY_TYPES.register("apostle_illusion", () -> EntityType.Builder
                    .<ApollyonPageantApostleEntity>m_20704_(ApollyonPageantApostleEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.6F, 1.95F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apostle_illusion").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantObsidianMonolithEntity>>
            APOLLYON_PAGEANT_OBSIDIAN_MONOLITH = ENTITY_TYPES.register(
                    "apollyon_pageant_obsidian_monolith", () -> EntityType.Builder
                            .<ApollyonPageantObsidianMonolithEntity>m_20704_(
                                    ApollyonPageantObsidianMonolithEntity::new,
                                    MobCategory.MONSTER)
                            .m_20699_(2.0F, 4.1F)
                            .m_20702_(128)
                            .m_20717_(1)
                            .m_20712_(new ResourceLocation(
                                    NAMESPACE, "apollyon_pageant_obsidian_monolith")
                                    .toString()));

    public static final RegistryObject<EntityType<ApollyonPageantMeteorEntity>> APOLLYON_PAGEANT_METEOR =
            ENTITY_TYPES.register("apollyon_pageant_meteor", () -> EntityType.Builder
                    .<ApollyonPageantMeteorEntity>m_20704_(ApollyonPageantMeteorEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(1.5F, 1.5F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_pageant_meteor").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantMagmaEntity>> APOLLYON_PAGEANT_MAGMA =
            ENTITY_TYPES.register("apollyon_pageant_magma", () -> EntityType.Builder
                    .<ApollyonPageantMagmaEntity>m_20704_(ApollyonPageantMagmaEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(2.0F, 2.0F)
                    .m_20702_(128)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_pageant_magma").toString()));

    public static final RegistryObject<EntityType<ApollyonGloriousSphereEntity>> APOLLYON_GLORIOUS_SPHERE =
            ENTITY_TYPES.register("apollyon_glorious_sphere", () -> EntityType.Builder
                    .<ApollyonGloriousSphereEntity>m_20704_(ApollyonGloriousSphereEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_glorious_sphere").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantBeamEntity>> APOLLYON_PAGEANT_BEAM =
            ENTITY_TYPES.register("apollyon_pageant_beam", () -> EntityType.Builder
                    .<ApollyonPageantBeamEntity>m_20704_(ApollyonPageantBeamEntity::new, MobCategory.MISC)
                    .m_20716_()
                    .m_20699_(0.1F, 0.1F)
                    .m_20702_(256)
                    .m_20717_(1)
                    .m_20712_(new ResourceLocation(NAMESPACE, "apollyon_pageant_beam").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantIceChunkEntity>>
            APOLLYON_PAGEANT_ICE_CHUNK = ENTITY_TYPES.register(
                    "apollyon_pageant_ice_chunk", () -> EntityType.Builder
                            .<ApollyonPageantIceChunkEntity>m_20704_(
                                    ApollyonPageantIceChunkEntity::new, MobCategory.MISC)
                            .m_20719_()
                            .m_20699_(3.0F, 3.0F)
                            .m_20702_(256)
                            .m_20717_(1)
                            .m_20712_(new ResourceLocation(
                                    NAMESPACE, "apollyon_pageant_ice_chunk").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantBlueIceEntity>>
            APOLLYON_PAGEANT_BLUE_ICE = ENTITY_TYPES.register(
                    "apollyon_pageant_blue_ice", () -> EntityType.Builder
                            .<ApollyonPageantBlueIceEntity>m_20704_(
                                    ApollyonPageantBlueIceEntity::new, MobCategory.MISC)
                            .m_20699_(4.0F, 4.0F)
                            .m_20702_(256)
                            .m_20717_(1)
                            .m_20712_(new ResourceLocation(
                                    NAMESPACE, "apollyon_pageant_blue_ice").toString()));

    public static final RegistryObject<EntityType<ApollyonPageantThornEntity>>
            APOLLYON_PAGEANT_THORN = ENTITY_TYPES.register(
                    "apollyon_pageant_thorn", () -> EntityType.Builder
                            .<ApollyonPageantThornEntity>m_20704_(
                                    ApollyonPageantThornEntity::new, MobCategory.MISC)
                            .m_20699_(0.5F, 0.5F)
                            .m_20702_(128)
                            .m_20717_(1)
                            .m_20712_(new ResourceLocation(
                                    NAMESPACE, "apollyon_pageant_thorn").toString()));

    private ApollyonEntityRegistry() {
    }

    private static RegistryObject<EntityType<DirectedApostleSummonEntity>>
            registerDirectedApostleSummon(String titleName, int titleNumber) {
        String entityName = "summon_apostle_" + titleName;
        return ENTITY_TYPES.register(entityName, () -> EntityType.Builder
                .<DirectedApostleSummonEntity>m_20704_(
                        (type, level) -> new DirectedApostleSummonEntity(
                                type, level, titleNumber),
                        MobCategory.MISC)
                .m_20699_(2.0F, 1.95F)
                .m_20702_(10)
                .m_20717_(Integer.MAX_VALUE)
                .m_20712_(new ResourceLocation(NAMESPACE, entityName).toString()));
    }

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(ApollyonEntityRegistry::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(APOLLYON.get(), ApollyonEntity.createAttributes().m_22265_());
        event.put(APOLLYON_SERVANT.get(), ApollyonServantEntity.createAttributes().m_22265_());
        event.put(APOSTLE.get(),
                ApollyonPageantApostleEntity.createAttributes().m_22265_());
        event.put(APOSTLE_ILLUSION.get(),
                ApollyonPageantApostleEntity.createAttributes().m_22265_());
        event.put(APOLLYON_PAGEANT_OBSIDIAN_MONOLITH.get(),
                ApollyonPageantObsidianMonolithEntity.setCustomAttributes().m_22265_());
    }
}
