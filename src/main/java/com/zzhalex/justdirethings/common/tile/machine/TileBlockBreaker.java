package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ToggleableTool;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TileBlockBreaker extends TileInventoryMachineBase implements ITickable {

    private boolean sneaking;
    private final LinkedHashMap<BlockPos, BlockBreakingProgress> blockBreakingTracker = new LinkedHashMap<>();
    private Map.Entry<BlockPos, BlockBreakingProgress> currentBlock;

    public TileBlockBreaker() {
        super(1);
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        handleTicks();
        evaluateRedstoneControl();

        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        fakePlayer.setSneaking(sneaking);
        boolean changed = doBlockBreak(fakePlayer);
        onServerTick();
        fakePlayer.setSneaking(false);
        if (changed) {
            markDirtyClient();
        }
    }

    protected void onServerTick() {
    }

    protected boolean doBlockBreak(FakePlayer fakePlayer) {
        ItemStack tool = getTool();
        clearTrackerIfNeeded(tool, fakePlayer);
        if (tool.isEmpty()) {
            getRedstoneState().setPulsed(false);
            return false;
        }

        boolean changed = false;
        if (isRedstoneActive() && canRun() && blockBreakingTracker.isEmpty()) {
            for (BlockPos targetPos : findBlocksToMine(fakePlayer)) {
                IBlockState state = world.getBlockState(targetPos);
                if (state.getBlock() != Blocks.AIR && !blockBreakingTracker.containsKey(targetPos)) {
                    startMining(fakePlayer, targetPos, state, tool);
                    changed = true;
                }
            }
        }

        if (blockBreakingTracker.isEmpty() || !canMine()) {
            return changed;
        }

        if ((currentBlock == null || !blockBreakingTracker.containsKey(currentBlock.getKey())) && canRun()) {
            currentBlock = blockBreakingTracker.entrySet().iterator().next();
        }
        if (currentBlock != null && mineBlock(currentBlock.getKey(), tool, fakePlayer)) {
            removePosFromTracker(currentBlock.getKey(), currentBlock.getValue().breakerId);
            currentBlock = null;
            changed = true;
        }
        return changed;
    }

    protected boolean canMine() {
        return true;
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    protected List<BlockPos> findBlocksToMine(FakePlayer fakePlayer) {
        List<BlockPos> blocks = new ArrayList<>();
        BlockPos targetPos = MachineActionHelper.targetPos(this);
        if (isBlockValid(fakePlayer, targetPos)) {
            blocks.add(targetPos);
        }
        return blocks;
    }

    protected boolean isBlockValid(FakePlayer fakePlayer, BlockPos targetPos) {
        IBlockState state = world.getBlockState(targetPos);
        if (state.getBlock() == Blocks.AIR || state.getBlock() instanceof BlockLiquid) {
            return false;
        }
        if (targetPos.equals(pos) || blockBreakingTracker.containsKey(targetPos)) {
            return false;
        }
        if (state.getBlockHardness(world, targetPos) < 0.0F) {
            return false;
        }
        return world.isBlockModifiable(fakePlayer, targetPos)
                && MachineActionHelper.canBreakAndPlaceAt(world, targetPos, fakePlayer);
    }

    protected ItemStack getTool() {
        return getItemHandler().getStackInSlot(0);
    }

    protected void startMining(FakePlayer fakePlayer, BlockPos targetPos, IBlockState state, ItemStack tool) {
        alignFakePlayerForBreak(fakePlayer, targetPos, tool);
        int breakerId = fakePlayer.getEntityId() + generatePosHash() + blockBreakingTracker.size();
        blockBreakingTracker.put(targetPos, new BlockBreakingProgress(state, 0, -1, breakerId, getDestroyProgress(targetPos, tool, fakePlayer, state)));
    }

    protected boolean mineBlock(BlockPos targetPos, ItemStack tool, FakePlayer fakePlayer) {
        IBlockState state = world.getBlockState(targetPos);
        if (state.getBlock() == Blocks.AIR) {
            return true;
        }

        BlockBreakingProgress tracked = blockBreakingTracker.get(targetPos);
        if (tracked != null && !state.equals(tracked.blockState)) {
            return true;
        }

        if (tracked == null) {
            tracked = new BlockBreakingProgress(state, 0, -1, fakePlayer.getEntityId() + generatePosHash() + blockBreakingTracker.size(), getDestroyProgress(targetPos, tool, fakePlayer, state));
        }
        int updatedTicks = tracked.ticks + 1;
        float destroyProgress = tracked.destroyProgress * updatedTicks;
        int currentProgress = (int) (destroyProgress * 10.0F);
        int lastSentProgress = tracked.lastSentProgress;

        if (currentProgress != lastSentProgress && currentProgress < 10) {
            sendPackets(tracked.breakerId, targetPos, currentProgress);
            lastSentProgress = currentProgress;
        }
        blockBreakingTracker.put(targetPos, new BlockBreakingProgress(state, updatedTicks, lastSentProgress, tracked.breakerId, tracked.destroyProgress));

        if (destroyProgress >= 1.0F) {
            tryBreakBlock(tool, fakePlayer, targetPos, state);
            return true;
        }
        return false;
    }

    protected float getDestroyProgress(BlockPos targetPos, ItemStack tool, FakePlayer fakePlayer, IBlockState state) {
        float hardness = state.getBlockHardness(world, targetPos);
        if (hardness < 0.0F) {
            return 0.0F;
        }
        return getDestroySpeed(targetPos, tool, fakePlayer, state) / hardness / getMiningSpeedDivisor(fakePlayer, state);
    }

    protected float getDestroySpeed(BlockPos targetPos, ItemStack tool, FakePlayer fakePlayer, IBlockState state) {
        alignFakePlayerForBreak(fakePlayer, targetPos, tool);
        float toolSpeed = tool.isEmpty() ? 1.0F : tool.getDestroySpeed(state);
        if (toolSpeed > 1.0F) {
            int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, tool);
            if (efficiency > 0) {
                toolSpeed += efficiency * efficiency + 1;
            }
        }
        return ForgeEventFactory.getBreakSpeed(fakePlayer, state, toolSpeed, targetPos);
    }

    protected float getMiningSpeedDivisor(FakePlayer fakePlayer, IBlockState state) {
        return fakePlayer.canHarvestBlock(state) ? 30.0F : 100.0F;
    }

    protected boolean tryBreakBlock(ItemStack tool, FakePlayer fakePlayer, BlockPos targetPos, IBlockState state) {
        alignFakePlayerForBreak(fakePlayer, targetPos, tool);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, targetPos, state, fakePlayer);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        return breakBlock(fakePlayer, targetPos, tool, state);
    }

    protected boolean breakBlock(FakePlayer fakePlayer, BlockPos targetPos, ItemStack tool, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(targetPos);
        boolean canHarvest = state.getBlock().canHarvestBlock(world, targetPos, fakePlayer);
        state.getBlock().onBlockHarvested(world, targetPos, state, fakePlayer);
        boolean removed = state.getBlock().removedByPlayer(state, world, targetPos, fakePlayer, canHarvest);
        if (!removed) {
            return false;
        }

        state.getBlock().onPlayerDestroy(world, targetPos, state);
        if (canHarvest) {
            state.getBlock().harvestBlock(world, fakePlayer, targetPos, state, tileEntity, tool);
        }
        if (!tool.isEmpty() && state.getBlockHardness(world, targetPos) != 0.0F) {
            tool.onBlockDestroyed(world, state, targetPos, fakePlayer);
        }
        world.playEvent(2001, targetPos, Block.getStateId(state));
        return true;
    }

    protected void sendPackets(int breakerId, BlockPos targetPos, int progress) {
        world.sendBlockBreakProgress(breakerId, targetPos, progress);
    }

    protected void clearTrackerIfNeeded(ItemStack tool, FakePlayer fakePlayer) {
        if (blockBreakingTracker.isEmpty()) {
            return;
        }
        if (tool.isEmpty() || (!isRedstoneCurrentlyActive() && !getRedstoneState().isPulseMode())) {
            clearTracker(fakePlayer);
            currentBlock = null;
        }
    }

    protected void clearTracker(FakePlayer fakePlayer) {
        Iterator<Map.Entry<BlockPos, BlockBreakingProgress>> iterator = blockBreakingTracker.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, BlockBreakingProgress> entry = iterator.next();
            sendClearPacket(entry.getKey(), entry.getValue().breakerId);
            iterator.remove();
        }
    }

    protected void removePosFromTracker(BlockPos targetPos, int breakerId) {
        sendClearPacket(targetPos, breakerId);
        blockBreakingTracker.remove(targetPos);
    }

    protected void sendClearPacket(BlockPos targetPos, int breakerId) {
        sendPackets(breakerId, targetPos, -1);
    }

    protected int generatePosHash() {
        return pos.getX() + pos.getY() + pos.getZ();
    }

    protected void alignFakePlayerForBreak(FakePlayer fakePlayer, BlockPos targetPos, ItemStack tool) {
        EnumFacing facing = MachineActionHelper.getFacing(this);
        MachineActionHelper.alignFakePlayer(fakePlayer, targetPos, facing);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, tool);
    }

    protected boolean isRedstoneCurrentlyActive() {
        switch (getRedstoneState().getMode()) {
            case LOW:
                return !getRedstoneState().isReceivingRedstone();
            case HIGH:
                return getRedstoneState().isReceivingRedstone();
            case PULSE:
                return getRedstoneState().isPulsed();
            case IGNORED:
            default:
                return true;
        }
    }

    protected int getFortuneLevel(ItemStack tool) {
        return tool.isEmpty() ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Sneaking", sneaking);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        sneaking = compound.getBoolean("Sneaking");
    }

    public static class T1 extends TileBlockBreaker {
    }

    public static class T2 extends TileBlockBreaker implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
            getFilterState().setBlockItemFilter(0);
        }

        @Override
        public FilterItemHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        public int getStandardEnergyCost() {
            return 500;
        }

        @Override
        protected void onServerTick() {
            chargeItemStack(getItemHandler().getStackInSlot(0));
        }

        @Override
        protected boolean canMine() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected boolean tryBreakBlock(ItemStack tool, FakePlayer fakePlayer, BlockPos targetPos, IBlockState state) {
            if (consumeEnergy(getStandardEnergyCost(), false) < getStandardEnergyCost()) {
                return false;
            }
            return super.tryBreakBlock(tool, fakePlayer, targetPos, state);
        }

        @Override
        protected List<BlockPos> findBlocksToMine(FakePlayer fakePlayer) {
            List<BlockPos> blocks = new ArrayList<>();
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (isBlockValid(fakePlayer, targetPos)) {
                    blocks.add(targetPos);
                }
            }
            return blocks;
        }

        @Override
        protected boolean isBlockValid(FakePlayer fakePlayer, BlockPos targetPos) {
            if (!super.isBlockValid(fakePlayer, targetPos)) {
                return false;
            }
            IBlockState state = world.getBlockState(targetPos);
            return getFilterState().getBlockItemFilter() == 0
                    ? matchesBlockFilter(state, targetPos)
                    : matchesToolAwareDropFilter(state, targetPos, getTool());
        }

        protected boolean matchesToolAwareDropFilter(IBlockState state, BlockPos targetPos, ItemStack tool) {
            NonNullList<ItemStack> drops = NonNullList.create();
            state.getBlock().getDrops(drops, world, targetPos, state, getFortuneLevel(tool));
            if (drops.isEmpty()) {
                return matchesFilter(ItemStack.EMPTY);
            }
            boolean smelterActive = hasActiveSmelter(tool);
            for (ItemStack drop : drops) {
                ItemStack filterStack = smelterActive ? getSmeltedDrop(drop) : drop;
                if (matchesFilter(filterStack)) {
                    return true;
                }
            }
            return false;
        }

        protected boolean hasActiveSmelter(ItemStack tool) {
            if (tool.isEmpty() || !(tool.getItem() instanceof ToggleableTool)) {
                return false;
            }
            ToggleableTool toggleableTool = (ToggleableTool) tool.getItem();
            return toggleableTool.supportsAbility(Ability.SMELTER)
                    && toggleableTool.hasInstalledAbility(tool, Ability.SMELTER)
                    && toggleableTool.getSetting(tool, Ability.SMELTER);
        }

        protected ItemStack getSmeltedDrop(ItemStack drop) {
            ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(drop);
            return smelted.isEmpty() ? drop : smelted;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            return writeAdvancedMachineToNbt(compound);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            readAdvancedMachineFromNbt(compound);
        }
    }

    private static final class BlockBreakingProgress {
        private final IBlockState blockState;
        private final int ticks;
        private final int lastSentProgress;
        private final int breakerId;
        private final float destroyProgress;

        private BlockBreakingProgress(IBlockState blockState, int ticks, int lastSentProgress, int breakerId, float destroyProgress) {
            this.blockState = blockState;
            this.ticks = ticks;
            this.lastSentProgress = lastSentProgress;
            this.breakerId = breakerId;
            this.destroyProgress = destroyProgress;
        }
    }
}
