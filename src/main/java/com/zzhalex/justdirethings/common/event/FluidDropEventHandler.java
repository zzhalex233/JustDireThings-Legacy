package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooFluidRecipeRuntime;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FluidDropEventHandler {

    public static final FluidDropEventHandler INSTANCE = new FluidDropEventHandler();
    static final Map<FluidInputs, IBlockState> fluidCraftCache = new HashMap<>();

    private FluidDropEventHandler() {
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        List<Entity> entities = new ArrayList<>(event.world.loadedEntityList);
        for (Entity entity : entities) {
            if (entity instanceof EntityItem && !entity.isDead) {
                handleItemEntity((EntityItem) entity);
            }
        }
    }

    private static void handleItemEntity(EntityItem entity) {
        World world = entity.world;
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        for (BlockPos pos : candidateFluidPositions(entity)) {
            IBlockState sourceState = world.getBlockState(pos);
            if (!GooFluidRecipeRuntime.isSourceFluidBlock(sourceState)) {
                continue;
            }

            IBlockState output = findRecipe(sourceState, stack);
            if (output.getBlock() == Blocks.AIR) {
                continue;
            }
            if (world.setBlockState(pos, output, 3)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    entity.setDead();
                }
                if (world.provider.doesWaterVaporize() && output.getMaterial() == Material.WATER) {
                    world.setBlockToAir(pos);
                } else {
                    world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                return;
            }
        }
    }

    private static IBlockState findRecipe(IBlockState sourceState, ItemStack stack) {
        FluidInputs inputs = new FluidInputs(sourceState, stack.getItem());
        IBlockState cached = fluidCraftCache.get(inputs);
        if (cached != null) {
            return cached;
        }

        FluidDropDataRecipe recipe = GooFluidRecipeRuntime.findFluidDropRecipe(sourceState, stack);
        IBlockState output = recipe == null ? Blocks.AIR.getDefaultState() : recipe.getOutput().toBlockState();
        fluidCraftCache.put(inputs, output);
        return output;
    }

    private static List<BlockPos> candidateFluidPositions(EntityItem entity) {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(new BlockPos(entity.posX, entity.posY, entity.posZ));
        positions.add(new BlockPos(entity.posX, entity.posY + 0.05D, entity.posZ));
        positions.add(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY + 0.1D, entity.posZ));
        positions.add(new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY - 0.1D, entity.posZ));
        return new ArrayList<>(positions);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            clearCache();
        }
    }

    private static void clearCache() {
        fluidCraftCache.clear();
    }

    static final class FluidInputs {
        private final IBlockState blockState;
        private final Item item;

        private FluidInputs(IBlockState blockState, Item item) {
            this.blockState = blockState;
            this.item = item;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FluidInputs)) {
                return false;
            }
            FluidInputs that = (FluidInputs) other;
            return blockState.equals(that.blockState) && item == that.item;
        }

        @Override
        public int hashCode() {
            return 31 * blockState.hashCode() + System.identityHashCode(item);
        }
    }
}
