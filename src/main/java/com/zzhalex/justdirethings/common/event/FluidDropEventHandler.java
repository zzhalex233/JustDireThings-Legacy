package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.recipe.custom.FluidDropDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooFluidRecipeRuntime;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
    private static final int LAVA_REPAIR_TICKS = 80;
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

        if (handleLavaRepair(entity, stack)) {
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

    private static boolean handleLavaRepair(EntityItem entity, ItemStack stack) {
        if (!(stack.getItem() instanceof ToggleableTool)) {
            clearLavaRepair(stack);
            return false;
        }

        ToggleableTool tool = (ToggleableTool) stack.getItem();
        if (!tool.supportsAbility(Ability.LAVAREPAIR)
                || !tool.hasInstalledAbility(stack, Ability.LAVAREPAIR)
                || !tool.getSetting(stack, Ability.LAVAREPAIR)) {
            clearLavaRepair(stack);
            return false;
        }

        World world = entity.world;
        BlockPos currentLavaPos = findCurrentSourceLava(entity);
        if (currentLavaPos != null) {
            setLavaRepairPos(stack, currentLavaPos);
            setLavaRepairTicks(stack, 0);
            entity.setPickupDelay(85);
            entity.onGround = true;
        }

        BlockPos lavaPos = getLavaRepairPos(stack);
        if (lavaPos == null || !isSourceLava(world, lavaPos)) {
            clearLavaRepair(stack);
            entity.onGround = false;
            return false;
        }

        if (!hasLavaRepairTicks(stack)) {
            return false;
        }

        if (entity.posY - lavaPos.getY() < 3.0D) {
            entity.motionY = 0.05D;
        } else {
            entity.motionY = 0.005D;
        }
        entity.motionX = 0.0D;
        entity.motionZ = 0.0D;
        entity.onGround = true;
        entity.velocityChanged = true;

        int ticks = getLavaRepairTicks(stack) + 1;
        setLavaRepairTicks(stack, ticks);
        if (ticks >= LAVA_REPAIR_TICKS) {
            world.setBlockState(lavaPos, Blocks.OBSIDIAN.getDefaultState(), 3);
            world.playSound(null, lavaPos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            repairItem(stack);
            clearLavaRepair(stack);
            entity.onGround = false;
            return true;
        }

        if (world instanceof WorldServer && ticks < LAVA_REPAIR_TICKS / 2) {
            spawnLavaRepairParticles((WorldServer) world, lavaPos, entity);
        }
        return true;
    }

    private static BlockPos findCurrentSourceLava(EntityItem entity) {
        for (BlockPos pos : candidateFluidPositions(entity)) {
            if (isSourceLava(entity.world, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSourceLava(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != Blocks.LAVA) {
            return false;
        }
        return state.getPropertyKeys().contains(BlockLiquid.LEVEL) && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static void repairItem(ItemStack stack) {
        if (stack.isItemStackDamageable()) {
            stack.setItemDamage(0);
        }
    }

    private static void spawnLavaRepairParticles(WorldServer world, BlockPos lavaPos, EntityItem entity) {
        for (int i = 0; i < 5; i++) {
            world.spawnParticle(
                    EnumParticleTypes.FLAME,
                    lavaPos.getX() + world.rand.nextDouble(),
                    lavaPos.getY() + 0.95D,
                    lavaPos.getZ() + world.rand.nextDouble(),
                    1,
                    entity.posX - (lavaPos.getX() + 0.5D),
                    entity.posY - lavaPos.getY(),
                    entity.posZ - (lavaPos.getZ() + 0.5D),
                    0.0D
            );
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

    private static boolean hasLavaRepairTicks(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(JDTDataKeys.LAVAREPAIR_FLOATING_TICKS);
    }

    private static int getLavaRepairTicks(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getInteger(JDTDataKeys.LAVAREPAIR_FLOATING_TICKS) : 0;
    }

    private static void setLavaRepairTicks(ItemStack stack, int ticks) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.LAVAREPAIR_FLOATING_TICKS, Math.max(0, ticks));
    }

    private static BlockPos getLavaRepairPos(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(JDTDataKeys.LAVAREPAIR_LAVA_X)
                || !tag.hasKey(JDTDataKeys.LAVAREPAIR_LAVA_Y)
                || !tag.hasKey(JDTDataKeys.LAVAREPAIR_LAVA_Z)) {
            return null;
        }
        return new BlockPos(
                tag.getInteger(JDTDataKeys.LAVAREPAIR_LAVA_X),
                tag.getInteger(JDTDataKeys.LAVAREPAIR_LAVA_Y),
                tag.getInteger(JDTDataKeys.LAVAREPAIR_LAVA_Z)
        );
    }

    private static void setLavaRepairPos(ItemStack stack, BlockPos pos) {
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setInteger(JDTDataKeys.LAVAREPAIR_LAVA_X, pos.getX());
        tag.setInteger(JDTDataKeys.LAVAREPAIR_LAVA_Y, pos.getY());
        tag.setInteger(JDTDataKeys.LAVAREPAIR_LAVA_Z, pos.getZ());
    }

    private static void clearLavaRepair(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(JDTDataKeys.LAVAREPAIR_FLOATING_TICKS);
        tag.removeTag(JDTDataKeys.LAVAREPAIR_LAVA_X);
        tag.removeTag(JDTDataKeys.LAVAREPAIR_LAVA_Y);
        tag.removeTag(JDTDataKeys.LAVAREPAIR_LAVA_Z);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
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
