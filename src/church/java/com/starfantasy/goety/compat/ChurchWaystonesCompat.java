package com.starfantasy.goety.compat;

import com.mojang.logging.LogUtils;
import com.starfantasy.goety.church.ChurchContent;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/** Optional Waystones integration; the template's full anchor is the no-mod fallback. */
public final class ChurchWaystonesCompat {
    private static final ResourceLocation WAYSTONE = new ResourceLocation("waystones", "waystone");
    private ChurchWaystonesCompat() {}

    public static boolean available() {
        return ModList.get().isLoaded("waystones") && ForgeRegistries.BLOCKS.containsKey(WAYSTONE);
    }

    public static boolean place(ServerLevel level, BlockPos pos, Direction facing) {
        if (!available()) return false;
        Block block = ForgeRegistries.BLOCKS.getValue(WAYSTONE);
        var original = level.getBlockState(pos);
        boolean existing = original.is(block) && level.getBlockState(pos.above()).is(block);
        if (!existing && (!original.is(ChurchContent.ETERNAL_RESPAWN_ANCHOR.get())
                || !level.isEmptyBlock(pos.above())
                || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP))) return false;
        boolean cleared = false;
        try {
            Class<?> api = Class.forName("net.blay09.mods.waystones.api.WaystonesAPI");
            Class<?> styleType = Class.forName("net.blay09.mods.waystones.api.WaystoneStyle");
            Class<?> stoneType = Class.forName("net.blay09.mods.waystones.api.IWaystone");
            Class<?> mutableType = Class.forName("net.blay09.mods.waystones.api.IMutableWaystone");
            Class<?> managerType = Class.forName("net.blay09.mods.waystones.core.WaystoneManager");
            var place = api.getMethod("placeWaystone", Level.class, BlockPos.class, styleType);
            var lookup = api.getMethod("getWaystoneAt", ServerLevel.class, BlockPos.class);
            var setName = mutableType.getMethod("setName", String.class);
            var managerGet = managerType.getMethod("get", MinecraftServer.class);
            var update = managerType.getMethod("updateWaystone", stoneType);
            Object style = styleType.getConstructor(ResourceLocation.class).newInstance(WAYSTONE);
            // Resolve the complete optional API before replacing the fallback block.
            if (!existing) {
                if (!level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) return false;
                cleared = true;
            }
            Optional<?> result = (Optional<?>) (existing ? lookup.invoke(null, level, pos)
                    : place.invoke(null, level, pos, style));
            if (result.isEmpty()) return false;
            Object stone = result.get();
            setName.invoke(stone, "冥界教堂");
            update.invoke(managerGet.invoke(null, level.getServer()), stone);
            for (BlockPos half : new BlockPos[]{pos, pos.above()}) {
                var state = level.getBlockState(half);
                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                    level.setBlock(half, state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing), Block.UPDATE_CLIENTS);
                var entity = level.getBlockEntity(half);
                if (entity != null) entity.setChanged();
                level.sendBlockUpdated(half, state, level.getBlockState(half), Block.UPDATE_CLIENTS);
            }
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            LogUtils.getLogger().warn("Could not initialize optional church waystone at {}", pos, exception);
            return false;
        } finally {
            if (cleared && level.isEmptyBlock(pos)) level.setBlock(pos, original, Block.UPDATE_ALL);
        }
    }
}
