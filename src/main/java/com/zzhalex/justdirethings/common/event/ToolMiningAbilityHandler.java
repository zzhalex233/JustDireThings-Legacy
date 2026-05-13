package com.zzhalex.justdirethings.common.event;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.ability.AbilityMethods;
import com.zzhalex.justdirethings.common.item.base.PoweredEnergyCostHelper;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.util.MiningCollect;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class ToolMiningAbilityHandler {

    public static final ToolMiningAbilityHandler INSTANCE = new ToolMiningAbilityHandler();

    private static final int TOOL_MAX_BREAK_FERRICORE = 64;
    private static final int TOOL_MAX_BREAK_BLAZEGOLD = 128;
    private static final int TOOL_MAX_BREAK_CELESTIGEM = 192;
    private static final int TOOL_MAX_BREAK_ECLIPSEALLOY = 256;
    private static final int POWERED_BLOCK_BREAK_FE_COST = 50;

    private static boolean alreadyBreaking = false;
    private static BlockPos spawnDropsAtPos = BlockPos.ORIGIN;
    private static List<ItemStack> batchedDrops = null;

    private ToolMiningAbilityHandler() {
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null || player.world.isRemote || alreadyBreaking) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        World world = event.getWorld();
        BlockPos origin = event.getPos();
        IBlockState state = event.getState();
        if (!isCorrectToolForDrops(stack, world, origin, state, player)) {
            return;
        }

        Set<BlockPos> breakPositions = getBreakBlockPositions(stack, world, origin, player, state);
        if (!shouldHandleBreak(stack, breakPositions)) {
            return;
        }

        alreadyBreaking = true;
        spawnDropsAtPos = origin.toImmutable();
        batchedDrops = new ArrayList<>();
        boolean instaBreak = canInstaBreak(stack, world, breakPositions, player);
        try {
            for (BlockPos breakPos : breakPositions) {
                if (!canPayBlockBreak(stack, player)) {
                    break;
                }
                breakBlock(world, breakPos, player, stack, instaBreak);
            }
        } finally {
            alreadyBreaking = false;
            spawnDropsAtPos = BlockPos.ORIGIN;
            List<ItemStack> collectedDrops = batchedDrops;
            batchedDrops = null;
            if (collectedDrops != null && !collectedDrops.isEmpty()) {
                AbilityMethods.handleAbilityDrops(stack, world, origin, player, collectedDrops, breakPositions);
            }
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        BlockPos origin = event.getPos();
        if (origin == null) {
            return;
        }

        IBlockState state = event.getState();
        if (state == null || !isCorrectToolForDrops(stack, player.world, origin, state, player)) {
            return;
        }
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null && energyStorage.getEnergyStored() < POWERED_BLOCK_BREAK_FE_COST) {
            event.setNewSpeed(0.1F);
            return;
        }

        float baseSpeed = event.getNewSpeed();
        if (player.capabilities.isFlying && event.getNewSpeed() < event.getOriginalSpeed() * 5.0F) {
            ItemStack chest = player.getItemStackFromSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST);
            if (AbilityMethods.canUseAbilityAndDurability(chest, Ability.FLIGHT)) {
                baseSpeed *= 5.0F;
            }
        }

        Set<BlockPos> breakPositions = getBreakBlockPositions(stack, player.world, origin, player, state);
        if (breakPositions.isEmpty()) {
            if (baseSpeed != event.getNewSpeed()) {
                event.setNewSpeed(baseSpeed);
            }
            return;
        }

        float cumulativeDestroy = 0.0F;
        for (BlockPos pos : breakPositions) {
            float hardness = player.world.getBlockState(pos).getBlockHardness(player.world, pos);
            if (hardness > 0.0F) {
                cumulativeDestroy += hardness;
            }
        }
        if (cumulativeDestroy <= 0.0F) {
            return;
        }

        int radius = AbilityMethods.canUseAbilityAndDurability(stack, Ability.HAMMER) ? MiningCollect.getHammerRange(stack) : 1;
        float modifier = Math.max(1.0F, (float) breakPositions.size() / Math.max(1, radius));
        float averageDestroy = (cumulativeDestroy / breakPositions.size()) * modifier;
        float originalDestroy = Math.max(0.0001F, state.getBlockHardness(player.world, event.getPos()));
        float targetSpeed = baseSpeed * (originalDestroy / averageDestroy);
        if (canInstaBreak(stack, player.world, breakPositions, player)) {
            targetSpeed = 10000.0F;
        }
        event.setNewSpeed(targetSpeed);
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (!alreadyBreaking || event.getWorld().isRemote || event.getHarvester() == null) {
            return;
        }

        EntityPlayer player = event.getHarvester();
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ToggleableTool)) {
            return;
        }

        World world = event.getWorld();
        List<ItemStack> drops = copyDropsWithChance(event);
        event.getDrops().clear();
        event.setDropChance(0.0F);

        BlockPos dropPos = spawnDropsAtPos == null || spawnDropsAtPos.equals(BlockPos.ORIGIN)
                ? event.getPos()
                : spawnDropsAtPos;
        if (batchedDrops != null) {
            batchedDrops.addAll(drops);
        } else {
            Set<BlockPos> breakPositions = new HashSet<>();
            breakPositions.add(event.getPos().toImmutable());
            AbilityMethods.handleAbilityDrops(stack, world, dropPos, player, drops, breakPositions);
        }
    }

    private static Set<BlockPos> getBreakBlockPositions(ItemStack stack, World world, BlockPos origin, EntityPlayer player, IBlockState state) {
        Set<BlockPos> breakPositions = new HashSet<>();
        int maxBreak = getMaxBreak(stack);

        if (AbilityMethods.canUseAbilityAndDurability(stack, Ability.OREMINER)
                && isOreBlock(state)
                && isCorrectToolForDrops(stack, world, origin, state, player)) {
            breakPositions.addAll(findLikeBlocks(world, state, origin, maxBreak, 2));
        }
        if (AbilityMethods.canUseAbilityAndDurability(stack, Ability.TREEFELLER)
                && isLogBlock(state)
                && isCorrectToolForDrops(stack, world, origin, state, player)) {
            breakPositions.addAll(findLikeBlocks(world, state, origin, maxBreak, 2));
        }
        if (AbilityMethods.canUseAbilityAndDurability(stack, Ability.HAMMER)) {
            breakPositions.addAll(collectHammerBlocks(world, origin, player, stack));
        }

        breakPositions.add(origin.toImmutable());

        if (AbilityMethods.canUseAbilityAndDurability(stack, Ability.SKYSWEEPER)
                && isCorrectToolForDrops(stack, world, origin, state, player)) {
            Set<BlockPos> skyfall = new HashSet<>();
            for (BlockPos blockPos : breakPositions) {
                IBlockState above = world.getBlockState(blockPos.up());
                if (isFallingBlock(above)) {
                    skyfall.addAll(findBlocksSkyfall(world, blockPos.up(), maxBreak, EnumFacing.UP, maxBreak));
                }
            }
            breakPositions.addAll(skyfall);
        }

        return breakPositions;
    }

    private static boolean shouldHandleBreak(ItemStack stack, Set<BlockPos> breakPositions) {
        return breakPositions.size() > 1
                || AbilityMethods.canUseAbilityAndDurability(stack, Ability.SMELTER)
                || AbilityMethods.canUseAbilityAndDurability(stack, Ability.DROPTELEPORT);
    }

    private static boolean breakBlock(World world, BlockPos pos, EntityPlayer player, ItemStack tool, boolean instaBreak) {
        IBlockState state = world.getBlockState(pos);
        if (state.getMaterial() == Material.AIR || state.getBlock() == Blocks.AIR || state.getBlockHardness(world, pos) < 0.0F) {
            return false;
        }

        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(world, pos, state, player);
        if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
            return false;
        }

        boolean canHarvest = state.getBlock().canHarvestBlock(world, pos, player);
        state.getBlock().onBlockHarvested(world, pos, state, player);
        if (player.capabilities.isCreativeMode) {
            boolean removedCreative = state.getBlock().removedByPlayer(state, world, pos, player, false);
            if (removedCreative) {
                state.getBlock().onPlayerDestroy(world, pos, state);
                world.notifyBlockUpdate(pos, state, Blocks.AIR.getDefaultState(), 3);
                world.playEvent(2001, pos, Block.getStateId(state));
            }
            return removedCreative;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        boolean removed = state.getBlock().removedByPlayer(state, world, pos, player, canHarvest);
        if (!removed) {
            return false;
        }

        state.getBlock().onPlayerDestroy(world, pos, state);
        world.notifyBlockUpdate(pos, state, Blocks.AIR.getDefaultState(), 3);
        if (canHarvest) {
            state.getBlock().harvestBlock(world, player, pos, state, tileEntity, tool);
        }
        if (breakEvent.getExpToDrop() > 0) {
            state.getBlock().dropXpOnBlockBreak(world, pos, breakEvent.getExpToDrop());
        }
        if (state.getBlockHardness(world, pos) != 0.0F) {
            payBlockBreak(tool, player);
            if (instaBreak) {
                payInstantBreak(tool, player, state.getBlockHardness(world, pos));
            }
        }
        world.playEvent(2001, pos, Block.getStateId(state));
        return true;
    }

    private static Set<BlockPos> findLikeBlocks(World world, IBlockState state, BlockPos origin, int maxBreak, int radius) {
        Set<BlockPos> foundBlocks = new HashSet<>();
        Queue<BlockPos> blocksToCheck = new LinkedList<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();

        foundBlocks.add(origin.toImmutable());
        blocksToCheck.add(origin.toImmutable());

        while (!blocksToCheck.isEmpty()) {
            BlockPos posToCheck = blocksToCheck.poll();
            if (!checkedBlocks.add(posToCheck)) {
                continue;
            }

            for (BlockPos pos : BlockPos.getAllInBox(posToCheck.add(-radius, -radius, -radius), posToCheck.add(radius, radius, radius))) {
                BlockPos immutablePos = pos.toImmutable();
                if (world.getBlockState(immutablePos).getBlock() != state.getBlock()) {
                    continue;
                }
                if (foundBlocks.size() >= maxBreak) {
                    return foundBlocks;
                }
                if (foundBlocks.add(immutablePos) && !checkedBlocks.contains(immutablePos)) {
                    blocksToCheck.add(immutablePos);
                }
            }
        }
        return foundBlocks;
    }

    private static Set<BlockPos> collectHammerBlocks(World world, BlockPos origin, EntityPlayer player, ItemStack stack) {
        return MiningCollect.collect(player, origin, world, MiningCollect.getHammerRange(stack), stack, ToolMiningAbilityHandler::isValidHammerBlock);
    }

    private static boolean isValidHammerBlock(World world, BlockPos pos, EntityPlayer player, ItemStack stack, EnumFacing side) {
        IBlockState blockState = world.getBlockState(pos);
        if (world.getTileEntity(pos) != null) {
            return false;
        }
        if (!isCorrectToolForDrops(stack, world, pos, blockState, player)) {
            return false;
        }
        return MiningCollect.isNonAirBreakableBlock(world, pos);
    }

    private static Set<BlockPos> findBlocksSkyfall(World world, BlockPos start, int maxBreak, EnumFacing direction, int range) {
        Set<BlockPos> foundBlocks = new HashSet<>();
        foundBlocks.add(start.toImmutable());

        for (int i = 1; i < range; i++) {
            BlockPos posToCheck = start.offset(direction, i);
            if (!isFallingBlock(world.getBlockState(posToCheck))) {
                break;
            }
            foundBlocks.add(posToCheck.toImmutable());
            if (foundBlocks.size() >= maxBreak) {
                break;
            }
        }
        return foundBlocks;
    }

    private static boolean isCorrectToolForDrops(ItemStack stack, World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return stack.canHarvestBlock(state) || state.getBlock().canHarvestBlock(world, pos, player);
    }

    private static boolean isOreBlock(IBlockState state) {
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        if (item == Items.AIR) {
            return false;
        }

        ItemStack oreStack = new ItemStack(item, 1, block.getMetaFromState(state));
        ItemStack wildcardStack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
        if (hasOreDictionaryPrefix(oreStack, "ore") || hasOreDictionaryPrefix(wildcardStack, "ore")
                || hasOreDictionaryPrefix(oreStack, "cluster") || hasOreDictionaryPrefix(wildcardStack, "cluster")) {
            return true;
        }
        return block.getRegistryName() != null && block.getRegistryName().getPath().contains("_ore");
    }

    private static boolean isLogBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockLog || state.getMaterial() == Material.WOOD) {
            Item item = Item.getItemFromBlock(block);
            if (item != Items.AIR) {
                ItemStack stack = new ItemStack(item, 1, block.getMetaFromState(state));
                ItemStack wildcardStack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
                return hasOreDictionaryName(stack, "logWood") || hasOreDictionaryName(wildcardStack, "logWood") || block instanceof BlockLog;
            }
        }
        return false;
    }

    private static boolean isFallingBlock(IBlockState state) {
        return state.getBlock() instanceof BlockFalling;
    }

    private static boolean hasOreDictionaryPrefix(ItemStack stack, String prefix) {
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOreDictionaryName(ItemStack stack, String name) {
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            if (OreDictionary.getOreName(oreId).equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static List<ItemStack> copyDropsWithChance(BlockEvent.HarvestDropsEvent event) {
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            if (drop == null || drop.isEmpty()) {
                continue;
            }
            if (event.getDropChance() >= 1.0F || event.getWorld().rand.nextFloat() <= event.getDropChance()) {
                drops.add(drop.copy());
            }
        }
        return drops;
    }

    private static int getMaxBreak(ItemStack stack) {
        String path = stack.getItem().getRegistryName() == null ? "" : stack.getItem().getRegistryName().getPath();
        if (path.contains("eclipsealloy")) {
            return configuredMax(JDTConfig.toolMaxBreakEclipsealloy, TOOL_MAX_BREAK_ECLIPSEALLOY);
        }
        if (path.contains("celestigem")) {
            return configuredMax(JDTConfig.toolMaxBreakCelestigem, TOOL_MAX_BREAK_CELESTIGEM);
        }
        if (path.contains("blazegold")) {
            return configuredMax(JDTConfig.toolMaxBreakBlazegold, TOOL_MAX_BREAK_BLAZEGOLD);
        }
        return configuredMax(JDTConfig.toolMaxBreakFerricore, TOOL_MAX_BREAK_FERRICORE);
    }

    private static int configuredMax(int configured, int fallback) {
        return Math.max(1, configured > 0 ? configured : fallback);
    }

    private static boolean canPayBlockBreak(ItemStack stack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getEnergyStored() >= POWERED_BLOCK_BREAK_FE_COST;
        }
        return !stack.isItemStackDamageable() || stack.getMaxDamage() - stack.getItemDamage() >= 1;
    }

    private static void payBlockBreak(ItemStack stack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return;
        }
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            energyStorage.extractEnergy(PoweredEnergyCostHelper.afterUnbreakingDiscount(stack, POWERED_BLOCK_BREAK_FE_COST), false);
            return;
        }
        ItemStack before = stack.copy();
        stack.damageItem(1, player);
        if (stack.isEmpty() && !before.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, before, EnumHand.MAIN_HAND);
        }
    }

    private static boolean canInstaBreak(ItemStack stack, World world, Set<BlockPos> breakPositions, EntityPlayer player) {
        if (!AbilityMethods.canUseAbilityAndDurability(stack, Ability.INSTABREAK)) {
            return false;
        }
        if (!canInstaBreakAllTargets(stack, world, breakPositions, player)) {
            return false;
        }
        return canPayInstantBreak(stack, getCumulativeDestroySpeed(world, breakPositions));
    }

    private static boolean canInstaBreakAllTargets(ItemStack stack, World world, Set<BlockPos> breakPositions, EntityPlayer player) {
        for (BlockPos pos : breakPositions) {
            IBlockState state = world.getBlockState(pos);
            if (!isCorrectToolForDrops(stack, world, pos, state, player) || !isEffectiveToolForBlock(stack, state)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEffectiveToolForBlock(ItemStack stack, IBlockState state) {
        return stack.getDestroySpeed(state) > 1.0F;
    }

    private static float getCumulativeDestroySpeed(World world, Set<BlockPos> breakPositions) {
        float cumulativeDestroy = 0.0F;
        for (BlockPos pos : breakPositions) {
            float destroySpeed = world.getBlockState(pos).getBlockHardness(world, pos);
            if (destroySpeed > 0.0F) {
                cumulativeDestroy += destroySpeed;
            }
        }
        return cumulativeDestroy;
    }

    private static boolean canPayInstantBreak(ItemStack stack, float destroySpeed) {
        int cost = getInstantBreakCost(stack, destroySpeed);
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            return energyStorage.getEnergyStored() >= cost;
        }
        return !stack.isItemStackDamageable() || stack.getMaxDamage() - stack.getItemDamage() >= cost;
    }

    private static void payInstantBreak(ItemStack stack, EntityPlayer player, float destroySpeed) {
        if (player.capabilities.isCreativeMode) {
            return;
        }

        int cost = getInstantBreakCost(stack, destroySpeed);
        IEnergyStorage energyStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (energyStorage != null) {
            energyStorage.extractEnergy(PoweredEnergyCostHelper.afterUnbreakingDiscount(stack, cost), false);
            return;
        }

        ItemStack before = stack.copy();
        stack.damageItem(cost, player);
        if (stack.isEmpty() && !before.isEmpty()) {
            ForgeEventFactory.onPlayerDestroyItem(player, before, EnumHand.MAIN_HAND);
        }
    }

    private static int getInstantBreakCost(ItemStack stack, float destroySpeed) {
        int multiplier = Math.max(1, (int) destroySpeed);
        int cost = Math.max(Ability.INSTABREAK.getFeCost(), Ability.INSTABREAK.getFeCost() * multiplier);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        if (efficiency > 0) {
            cost -= (int) (cost * Math.min(1.0F, 0.1F * efficiency));
        }
        return Math.max(1, cost);
    }
}
