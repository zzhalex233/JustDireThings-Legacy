package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class TileClicker extends TileTimedMachineBase {

    private int clickTarget;
    private int clickType;
    private boolean sneaking;
    private boolean showFakePlayer = true;

    public TileClicker() {
        super(1);
    }

    public int getClickTarget() {
        return clickTarget;
    }

    public void setClickTarget(int clickTarget) {
        this.clickTarget = Math.max(0, Math.min(7, clickTarget));
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = Math.max(0, Math.min(2, clickType));
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public boolean isShowFakePlayer() {
        return showFakePlayer;
    }

    public void setShowFakePlayer(boolean showFakePlayer) {
        this.showFakePlayer = showFakePlayer;
    }

    @Override
    protected boolean performWork() {
        if (!(world instanceof WorldServer)) {
            return false;
        }

        BlockPos targetPos = MachineActionHelper.targetPos(this);
        EnumFacing facing = MachineActionHelper.getFacing(this);
        ItemStack heldStack = getItemHandler().getStackInSlot(0);
        if (heldStack.isEmpty()) {
            return false;
        }

        if (MachineActionHelper.canReplace(world, targetPos) && MachineActionHelper.canAttemptPlacement(heldStack)) {
            return MachineActionHelper.useHeldItemOnTarget((WorldServer) world, this, getItemHandler(), 0, targetPos, facing, true);
        }

        IBlockState state = world.getBlockState(targetPos);
        if (state.getBlock() == Blocks.AIR) {
            return false;
        }

        return MachineActionHelper.useHeldItemOnTarget((WorldServer) world, this, getItemHandler(), 0, targetPos, facing, false);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ClickTarget", clickTarget);
        compound.setInteger("ClickType", clickType);
        compound.setBoolean("Sneaking", sneaking);
        compound.setBoolean("ShowFakePlayer", showFakePlayer);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        setClickTarget(compound.getInteger("ClickTarget"));
        setClickType(compound.getInteger("ClickType"));
        sneaking = compound.getBoolean("Sneaking");
        showFakePlayer = !compound.hasKey("ShowFakePlayer") || compound.getBoolean("ShowFakePlayer");
    }

    public static class T1 extends TileClicker {
    }

    public static class T2 extends TileClicker {
        // PARITY STUB: Upstream ClickerT2BE adds powered area/filter click targeting.
    }
}
