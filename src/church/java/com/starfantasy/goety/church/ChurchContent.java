package com.starfantasy.goety.church;

import com.starfantasy.library.structure.StructureProtection;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class ChurchContent {
    public static final String MODID = "starfantasy_goety";
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
    private static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<Item> UNDERWORLD_EYE = ITEMS.register("underworld_eye", () -> new UnderworldEyeItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final RegistryObject<net.minecraft.world.entity.EntityType<ChurchRiftEntity>> CHURCH_RIFT = ENTITIES.register("church_rift", () ->
            net.minecraft.world.entity.EntityType.Builder.<ChurchRiftEntity>of(ChurchRiftEntity::new, net.minecraft.world.entity.MobCategory.MISC)
                    .sized(1,1).clientTrackingRange(128).updateInterval(1).noSave().noSummon().fireImmune().build(MODID+":church_rift"));
    public static final RegistryObject<UnderworldAltarBlock> ALTAR = BLOCKS.register("underworld_altar", () ->
            new UnderworldAltarBlock(BlockBehaviour.Properties.copy(Blocks.CHISELED_POLISHED_BLACKSTONE)
                    .strength(-1, 3600000).noLootTable()));
    public static final RegistryObject<Item> ALTAR_ITEM = ITEMS.register("underworld_altar", () -> new BlockItem(ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<EternalRespawnAnchorBlock> ETERNAL_RESPAWN_ANCHOR = BLOCKS.register("eternal_respawn_anchor", () ->
            new EternalRespawnAnchorBlock(BlockBehaviour.Properties.copy(Blocks.RESPAWN_ANCHOR).lightLevel(state -> 15)));
    public static final RegistryObject<Item> ETERNAL_RESPAWN_ANCHOR_ITEM = ITEMS.register("eternal_respawn_anchor", () ->
            new BlockItem(ETERNAL_RESPAWN_ANCHOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<UnderworldAltarEntity>> ALTAR_ENTITY = BLOCK_ENTITIES.register("underworld_altar", () ->
            BlockEntityType.Builder.of(UnderworldAltarEntity::new, ALTAR.get()).build(null));
    public static final RegistryObject<StructureType<ChurchStructure>> CHURCH = STRUCTURES.register("church", () -> () -> ChurchStructure.CODEC);
    public static void init(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); BLOCK_ENTITIES.register(bus); STRUCTURES.register(bus); ENTITIES.register(bus);
        StructureProtection.register(new ResourceLocation(MODID, "church"), 64);
        com.starfantasy.library.structure.WorldgenProtection.register(new ResourceLocation(MODID, "church"));
    }
    private ChurchContent() {}
}
