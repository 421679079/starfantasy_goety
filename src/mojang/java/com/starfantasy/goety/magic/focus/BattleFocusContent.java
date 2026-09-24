package com.starfantasy.goety.magic.focus;

import com.starfantasy.goety.magic.focus.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class BattleFocusContent {
    private static final String MODID = "starfantasy_goety";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<Item> BLOOMS_AND_PLUMES_FOCUS = ITEMS.register("blooms_and_plumes_focus", () -> new BattleFocusItem(BattleFocusSpell.FLOWER));
    public static final RegistryObject<Item> EVERNIGHT_FOCUS = ITEMS.register("evernight_focus", () -> new BattleFocusItem(BattleFocusSpell.EVERNIGHT));
    public static final RegistryObject<Item> FINAL_ART_FOCUS = ITEMS.register("final_art_focus", () -> new BattleFocusItem(FinalArtSpell.INSTANCE));
    public static final RegistryObject<EntityType<FinalArtEntity>> FINAL_ART_EFFECT = ENTITIES.register("final_art_effect",
            () -> EntityType.Builder.<FinalArtEntity>of(FinalArtEntity::new, MobCategory.MISC)
                    .sized(1, 1).noSave().noSummon().fireImmune().clientTrackingRange(16).updateInterval(1).build(MODID+":final_art_effect"));
    public static final RegistryObject<EntityType<FlowerCastingEntity>> FLOWER_CASTING = ENTITIES.register("flower_casting",
            () -> EntityType.Builder.<FlowerCastingEntity>of(FlowerCastingEntity::new, MobCategory.MISC)
                    .sized(.2F, .2F).noSave().noSummon().fireImmune().clientTrackingRange(16).updateInterval(1).build(MODID+":flower_casting"));
    public static final RegistryObject<EntityType<FlowerArrowEntity>> FLOWER_ARROW = ENTITIES.register("flower_arrow",
            () -> EntityType.Builder.<FlowerArrowEntity>of(FlowerArrowEntity::new, MobCategory.MISC)
                    .sized(.5F, .5F).noSave().noSummon().fireImmune().clientTrackingRange(16).updateInterval(1).build(MODID+":flower_arrow"));
    public static final RegistryObject<EntityType<FlowerBurstRibbonEntity>> FLOWER_BURST_RIBBON = ENTITIES.register("flower_burst_ribbon",
            () -> EntityType.Builder.<FlowerBurstRibbonEntity>of(FlowerBurstRibbonEntity::new, MobCategory.MISC)
                    .sized(1, 1).noSave().noSummon().fireImmune().clientTrackingRange(16).updateInterval(1).build(MODID+":flower_burst_ribbon"));
    public static final RegistryObject<EntityType<EvernightCageEntity>> EVERNIGHT_CAGE = ENTITIES.register("evernight_cage",
            () -> EntityType.Builder.<EvernightCageEntity>of(EvernightCageEntity::new, MobCategory.MISC)
                    .sized(1, 1).noSave().noSummon().fireImmune().clientTrackingRange(16).updateInterval(1).build(MODID+":evernight_cage"));
    public static final RegistryObject<SoundEvent> FLOWER_START = sound("flower_arrow_dance_start"), FLOWER_FINAL = sound("flower_arrow_dance_final"),
            EVERNIGHT_START = sound("evernight_start"), EVERNIGHT_CAGE_SOUND = sound("evernight_cage"), EVERNIGHT_BURST = sound("evernight_burst");
    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, name)));
    }
    public static final RegistryObject<SoundEvent> FINAL_ART_START = sound("final_art_start"),
            FINAL_ART_PULL = sound("final_art_pull");
    public static void init(IEventBus bus) { ITEMS.register(bus); ENTITIES.register(bus); SOUNDS.register(bus); }
    private BattleFocusContent() {}
}
