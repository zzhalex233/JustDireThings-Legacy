package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TileClicker extends TileTimedMachineBase {

    public enum ClickTarget {
        BLOCK,
        AIR,
        HOSTILE,
        PASSIVE,
        ADULT,
        CHILD,
        PLAYER,
        LIVING
    }

    private int clickTarget;
    private int clickType;
    private boolean sneaking;
    private boolean showFakePlayer;
    private int maxHoldTicks = 1;
    private final List<BlockPos> positionsToClick = new ArrayList<>();
    private final List<EntityLivingBase> entitiesToClick = new ArrayList<>();

    public TileClicker() {
        super(1);
    }

    public int getClickTarget() {
        return clickTarget;
    }

    public void setClickTarget(int clickTarget) {
        this.clickTarget = Math.max(0, Math.min(ClickTarget.values().length - 1, clickTarget));
        clearQueuedTargets();
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = Math.max(0, Math.min(2, clickType));
        clearQueuedTargets();
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
        clearQueuedTargets();
    }

    public boolean isShowFakePlayer() {
        return showFakePlayer;
    }

    public void setShowFakePlayer(boolean showFakePlayer) {
        this.showFakePlayer = showFakePlayer;
        clearQueuedTargets();
    }

    public int getMaxHoldTicks() {
        return maxHoldTicks;
    }

    public void setMaxHoldTicks(int maxHoldTicks) {
        this.maxHoldTicks = Math.max(1, Math.min(1200, maxHoldTicks));
        clearQueuedTargets();
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        evaluateRedstoneControl();
        onServerTick();

        boolean activeRedstone = isRedstoneActive();
        boolean changed = doClick(activeRedstone);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    protected boolean performWork() {
        if (world == null || world.isRemote) {
            return false;
        }
        return doClick(isRedstoneActive());
    }

    @Override
    protected boolean canRun() {
        if (clickType == 2 && world instanceof WorldServer) {
            return getClickFakePlayer().isHandActive() || getOperationTicks() == 0 || getRedstoneState().isPulseMode();
        }
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    protected boolean doClick(boolean activeRedstone) {
        if (!(world instanceof WorldServer)) {
            return false;
        }
        ItemStack clickStack = getItemHandler().getStackInSlot(0);
        if (clearTrackerIfNeeded(clickStack, activeRedstone)) {
            positionsToClick.clear();
            entitiesToClick.clear();
            return false;
        }
        if (!canClick()) {
            return false;
        }

        ClickTarget target = getClickTargetMode();
        if (target == ClickTarget.BLOCK || target == ClickTarget.AIR) {
            if (activeRedstone && canRun() && positionsToClick.isEmpty()) {
                positionsToClick.addAll(findSpotsToClick());
            }
            if (positionsToClick.isEmpty() || !canRun()) {
                return false;
            }
            return clickBlock(positionsToClick.remove(0));
        }

        if (activeRedstone && canRun() && entitiesToClick.isEmpty()) {
            entitiesToClick.addAll(findEntitiesToClick(getClickAABB()));
        }
        if (entitiesToClick.isEmpty()) {
            return false;
        }
        if (!canRun() && !(clickType == 2 && getClickFakePlayer().isHandActive())) {
            return false;
        }
        return clickEntity(entitiesToClick.remove(0));
    }

    protected boolean clearTrackerIfNeeded(ItemStack itemStack, boolean activeRedstone) {
        if (positionsToClick.isEmpty() && entitiesToClick.isEmpty()) {
            return false;
        }
        if (!isStackValid(itemStack)) {
            return true;
        }
        if (!canClick()) {
            return true;
        }
        return !activeRedstone && !getRedstoneState().isPulseMode();
    }

    protected boolean isStackValid(ItemStack itemStack) {
        return true;
    }

    protected boolean canClick() {
        return true;
    }

    protected FakePlayer getClickFakePlayer() {
        return MachineActionHelper.createFakePlayer((WorldServer) world, this);
    }

    protected void clearQueuedTargets() {
        positionsToClick.clear();
        entitiesToClick.clear();
    }

    protected ClickTarget getClickTargetMode() {
        return ClickTarget.values()[Math.max(0, Math.min(ClickTarget.values().length - 1, clickTarget))];
    }

    protected boolean clickBlock(BlockPos targetPos) {
        if (!isBlockPosValidForClick(targetPos)) {
            return false;
        }
        if (getClickTargetMode() == ClickTarget.AIR) {
            return clickAir(targetPos);
        }

        EnumFacing facing = MachineActionHelper.getFacing(this);
        ItemStack heldStack = getItemHandler().getStackInSlot(0);
        boolean replaceable = MachineActionHelper.canReplace(world, targetPos);
        if (replaceable && MachineActionHelper.canAttemptPlacement(heldStack)) {
            return MachineActionHelper.useHeldItemOnTarget((WorldServer) world, this, getItemHandler(), 0, targetPos, facing, true);
        }
        IBlockState state = world.getBlockState(targetPos);
        if (state.getBlock() == Blocks.AIR) {
            return false;
        }
        return MachineActionHelper.useHeldItemOnTarget((WorldServer) world, this, getItemHandler(), 0, targetPos, facing, false);
    }

    protected boolean clickAir(BlockPos targetPos) {
        if (clickType == 1) {
            return false;
        }
        ItemStack heldStack = getItemHandler().getStackInSlot(0);
        if (heldStack.isEmpty()) {
            return false;
        }

        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, heldStack.copy());
        ActionResult<ItemStack> result = fakePlayer.getHeldItem(EnumHand.MAIN_HAND).getItem().onItemRightClick(world, fakePlayer, EnumHand.MAIN_HAND);
        getItemHandler().setStackInSlot(0, result.getResult());
        fakePlayer.setSneaking(false);
        return result.getType() == EnumActionResult.SUCCESS;
    }

    protected boolean clickEntity(EntityLivingBase entity) {
        ItemStack heldStack = getItemHandler().getStackInSlot(0);
        FakePlayer fakePlayer = MachineActionHelper.createFakePlayer((WorldServer) world, this);
        MachineActionHelper.alignFakePlayer(fakePlayer, entity.getPosition(), MachineActionHelper.getFacing(this));
        fakePlayer.setSneaking(sneaking);
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, heldStack.copy());

        boolean clicked;
        if (clickType == 1) {
            fakePlayer.attackTargetEntityWithCurrentItem(entity);
            clicked = true;
        } else {
            clicked = fakePlayer.interactOn(entity, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS;
        }

        getItemHandler().setStackInSlot(0, fakePlayer.getHeldItem(EnumHand.MAIN_HAND));
        fakePlayer.setSneaking(false);
        return clicked;
    }

    protected List<BlockPos> findSpotsToClick() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos targetPos = MachineActionHelper.targetPos(this);
        if (isBlockPosValidForClick(targetPos)) {
            positions.add(targetPos);
        }
        return positions;
    }

    protected boolean isBlockPosValidForClick(BlockPos targetPos) {
        IBlockState state = world.getBlockState(targetPos);
        ClickTarget target = getClickTargetMode();
        if (target == ClickTarget.BLOCK && state.getBlock() == Blocks.AIR) {
            return false;
        }
        return target != ClickTarget.AIR || state.getBlock() == Blocks.AIR;
    }

    protected AxisAlignedBB getClickAABB() {
        return new AxisAlignedBB(MachineActionHelper.targetPos(this));
    }

    protected List<EntityLivingBase> findEntitiesToClick(AxisAlignedBB area) {
        List<EntityLivingBase> matches = new ArrayList<>();
        for (EntityLivingBase entity : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (isValidEntityForClick(entity)) {
                matches.add(entity);
            }
        }
        return matches;
    }

    protected boolean isValidEntityForClick(EntityLivingBase entity) {
        ClickTarget target = getClickTargetMode();
        switch (target) {
            case HOSTILE:
                return entity instanceof IMob;
            case PASSIVE:
                return entity instanceof EntityAnimal;
            case ADULT:
                return entity instanceof EntityAnimal && !((EntityAnimal) entity).isChild();
            case CHILD:
                return entity instanceof EntityAnimal && ((EntityAnimal) entity).isChild();
            case PLAYER:
                return entity instanceof EntityPlayer;
            case LIVING:
                return true;
            case BLOCK:
            case AIR:
            default:
                return false;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ClickTarget", clickTarget);
        compound.setInteger("ClickType", clickType);
        compound.setBoolean("Sneaking", sneaking);
        compound.setBoolean("ShowFakePlayer", showFakePlayer);
        compound.setInteger("MaxHoldTicks", maxHoldTicks);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setClickTarget(compound.getInteger("ClickTarget"));
        setClickType(compound.getInteger("ClickType"));
        sneaking = compound.getBoolean("Sneaking");
        showFakePlayer = compound.getBoolean("ShowFakePlayer");
        maxHoldTicks = compound.hasKey("MaxHoldTicks") ? Math.max(1, compound.getInteger("MaxHoldTicks")) : 1;
    }

    public static class T1 extends TileClicker {
    }

    public static class T2 extends TileClicker implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
        }

        @Override
        public FilterItemHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        public int getStandardEnergyCost() {
            return 250;
        }

        @Override
        protected void onServerTick() {
            chargeItemStack(getItemHandler().getStackInSlot(0));
        }

        @Override
        protected boolean canClick() {
            return hasEnoughEnergy(getStandardEnergyCost());
        }

        @Override
        protected boolean clickBlock(BlockPos targetPos) {
            if (!super.clickBlock(targetPos)) {
                return false;
            }
            consumeEnergy(getStandardEnergyCost(), false);
            return true;
        }

        @Override
        protected boolean clickEntity(EntityLivingBase entity) {
            if (!super.clickEntity(entity)) {
                return false;
            }
            consumeEnergy(getStandardEnergyCost(), false);
            return true;
        }

        @Override
        protected List<BlockPos> findSpotsToClick() {
            List<BlockPos> positions = new ArrayList<>();
            for (BlockPos targetPos : getAreaPositionsNearestFirst()) {
                if (isBlockPosValidForClick(targetPos)) {
                    positions.add(targetPos);
                }
            }
            positions.sort(Comparator.comparingDouble(targetPos -> targetPos.distanceSq(pos)));
            return positions;
        }

        @Override
        protected boolean isBlockPosValidForClick(BlockPos targetPos) {
            if (!super.isBlockPosValidForClick(targetPos)) {
                return false;
            }
            IBlockState state = world.getBlockState(targetPos);
            return state.getBlock() == Blocks.AIR || matchesBlockFilter(state, targetPos);
        }

        @Override
        protected AxisAlignedBB getClickAABB() {
            return getAreaState().createArea(pos);
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
}
