package com.zzhalex.justdirethings.common.goo;

import com.zzhalex.justdirethings.common.recipe.custom.GooCatalystRegistry;
import com.zzhalex.justdirethings.common.recipe.custom.JDTBlockStateSpec;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageCustomGooTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CustomGooRuntime {

    private static final double SYNC_RANGE = 96.0D;
    private static final Map<World, Map<BlockPos, TileGooBlock.Custom>> TILES = new IdentityHashMap<>();

    private CustomGooRuntime() {
    }

    public static TileGooBlock.Custom getOrCreate(World world, BlockPos pos) {
        if (world == null || pos == null || !isCustomGoo(world, pos) || world.getTileEntity(pos) != null) {
            return null;
        }

        BlockPos key = pos.toImmutable();
        Map<BlockPos, TileGooBlock.Custom> worldTiles = TILES.computeIfAbsent(world, ignored -> new LinkedHashMap<>());
        TileGooBlock.Custom tile = worldTiles.get(key);
        if (tile == null || tile.isInvalid()) {
            tile = new TileGooBlock.Custom();
            prepareTile(tile, world, key);
            worldTiles.put(key, tile);
        }
        return tile;
    }

    public static void tickWorld(World world) {
        Map<BlockPos, TileGooBlock.Custom> worldTiles = TILES.get(world);
        if (worldTiles == null || worldTiles.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, TileGooBlock.Custom>> iterator = worldTiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, TileGooBlock.Custom> entry = iterator.next();
            BlockPos pos = entry.getKey();
            TileGooBlock.Custom tile = entry.getValue();
            if (!isCustomGoo(world, pos) || world.getTileEntity(pos) != null || tile.isInvalid()) {
                iterator.remove();
                syncRemoval(world, pos);
                continue;
            }

            prepareTile(tile, world, pos);
            boolean aliveBefore = tile.isGooAlive();
            Map<EnumFacing, Integer> countersBefore = copySideMap(tile.sideCounters);
            Map<EnumFacing, Integer> durationsBefore = copySideMap(tile.sideDurations);
            tile.update();

            if (!isCustomGoo(world, pos)) {
                iterator.remove();
                syncRemoval(world, pos);
                continue;
            }
            if (!world.isRemote && shouldSync(tile, aliveBefore, countersBefore, durationsBefore)) {
                syncTile((WorldServer) world, pos, tile);
            }
            if (!world.isRemote && !tile.isGooAlive() && !hasActiveWork(tile)) {
                iterator.remove();
            }
        }
    }

    public static void applyClientSync(World world, BlockPos pos, NBTTagCompound tag, boolean remove) {
        if (remove) {
            remove(world, pos);
            return;
        }
        TileGooBlock.Custom tile = getOrCreate(world, pos);
        if (tile != null && tag != null) {
            tile.readFromNBT(tag);
            prepareTile(tile, world, pos);
        }
    }

    public static void pruneDeadRenderTiles(World world, Set<BlockPos> visiblePositions) {
        Map<BlockPos, TileGooBlock.Custom> worldTiles = TILES.get(world);
        if (worldTiles == null) {
            return;
        }
        Iterator<Map.Entry<BlockPos, TileGooBlock.Custom>> iterator = worldTiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, TileGooBlock.Custom> entry = iterator.next();
            TileGooBlock.Custom tile = entry.getValue();
            if (!isCustomGoo(world, entry.getKey()) || (!tile.isGooAlive() && !hasActiveWork(tile) && !visiblePositions.contains(entry.getKey()))) {
                iterator.remove();
            }
        }
    }

    public static Collection<TileGooBlock.Custom> renderTiles(World world, BlockPos center, int radius) {
        Map<BlockPos, TileGooBlock.Custom> worldTiles = TILES.get(world);
        if (worldTiles == null || worldTiles.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        int radiusSq = radius * radius;
        Collection<TileGooBlock.Custom> tiles = new ArrayList<>();
        for (Map.Entry<BlockPos, TileGooBlock.Custom> entry : worldTiles.entrySet()) {
            if (center.distanceSq(entry.getKey()) <= radiusSq && isCustomGoo(world, entry.getKey())) {
                tiles.add(entry.getValue());
            }
        }
        return tiles;
    }

    public static void clear(World world) {
        TILES.remove(world);
    }

    private static boolean isCustomGoo(World world, BlockPos pos) {
        if (world == null || pos == null || !world.isBlockLoaded(pos)) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        return GooCatalystRegistry.isCustomGoo(JDTBlockStateSpec.fromState(state));
    }

    private static void prepareTile(TileGooBlock.Custom tile, World world, BlockPos pos) {
        tile.setWorld(world);
        tile.setPos(pos.toImmutable());
        tile.validate();
    }

    private static void remove(World world, BlockPos pos) {
        Map<BlockPos, TileGooBlock.Custom> worldTiles = TILES.get(world);
        if (worldTiles != null) {
            worldTiles.remove(pos);
        }
    }

    private static Map<EnumFacing, Integer> copySideMap(Map<EnumFacing, Integer> source) {
        return new EnumMap<>(source);
    }

    private static boolean shouldSync(TileGooBlock tile, boolean aliveBefore, Map<EnumFacing, Integer> countersBefore, Map<EnumFacing, Integer> durationsBefore) {
        if (aliveBefore != tile.isGooAlive() || !durationsBefore.equals(tile.sideDurations)) {
            return true;
        }
        int interval = 60 * tile.counterReducer();
        for (EnumFacing facing : EnumFacing.values()) {
            int oldCounter = countersBefore.get(facing);
            int newCounter = tile.sideCounters.get(facing);
            if (oldCounter == newCounter) {
                continue;
            }
            if (oldCounter == -1 || newCounter == -1 || newCounter == 0 || newCounter % interval == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveWork(TileGooBlock tile) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (tile.getRemainingTimeFor(facing) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static void syncTile(WorldServer world, BlockPos pos, TileGooBlock tile) {
        JDTNetwork.getChannel().sendToAllAround(
                new MessageCustomGooTile(pos, tile.writeToNBT(new NBTTagCompound()), false),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SYNC_RANGE)
        );
    }

    private static void syncRemoval(World world, BlockPos pos) {
        if (world instanceof WorldServer) {
            JDTNetwork.getChannel().sendToAllAround(
                    new MessageCustomGooTile(pos, new NBTTagCompound(), true),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SYNC_RANGE)
            );
        }
    }
}
