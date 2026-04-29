package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TileSensor extends TileInventoryMachineBase implements ITickable {

    private int signalStrength;
    private int senseTarget;
    private boolean strongSignal = true;
    private int senseAmount;
    private int equality;

    public TileSensor() {
        super(1);
        setTickSpeed(20);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (world.getTotalWorldTime() % Math.max(1, getTickSpeed()) != 0L) {
            return;
        }

        if (!canSense()) {
            return;
        }

        int matches = countTargets(MachineActionHelper.targetPos(this));
        int nextSignal = passesComparison(matches) ? (strongSignal ? 15 : Math.min(15, Math.max(1, matches))) : 0;
        if (nextSignal != signalStrength) {
            signalStrength = nextSignal;
            markDirtyClient();
            world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
            world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        }
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public int getSenseTarget() {
        return senseTarget;
    }

    public void setSenseTarget(int senseTarget) {
        this.senseTarget = Math.max(0, Math.min(8, senseTarget));
    }

    public boolean isStrongSignal() {
        return strongSignal;
    }

    public void setStrongSignal(boolean strongSignal) {
        this.strongSignal = strongSignal;
    }

    public int getSenseAmount() {
        return senseAmount;
    }

    public void setSenseAmount(int senseAmount) {
        this.senseAmount = Math.max(0, senseAmount);
    }

    public int getEquality() {
        return equality;
    }

    public void setEquality(int equality) {
        this.equality = Math.max(0, Math.min(2, equality));
    }

    protected boolean canSense() {
        return true;
    }

    protected int countTargets(BlockPos targetPos) {
        if (senseTarget == 0 || senseTarget == 1) {
            boolean air = world.isAirBlock(targetPos);
            if (senseTarget == 1) {
                return air ? 1 : 0;
            }
            if (air || !matchesFilter(world.getBlockState(targetPos))) {
                return 0;
            }
            return 1;
        }

        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(targetPos));
        int count = 0;
        for (Entity entity : entities) {
            if (matchesEntityTarget(entity)) {
                count++;
            }
        }
        return count;
    }

    protected boolean matchesFilter(IBlockState state) {
        ItemStack filter = getItemHandler().getStackInSlot(0);
        if (filter.isEmpty()) {
            return true;
        }
        ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        if (blockStack.isEmpty()) {
            return false;
        }
        if (getFilterState().isCompareNbt()) {
            return ItemStack.areItemStacksEqual(filter, blockStack);
        }
        return filter.getItem() == blockStack.getItem() && filter.getMetadata() == blockStack.getMetadata();
    }

    protected boolean matchesEntityTarget(Entity entity) {
        switch (senseTarget) {
            case 2:
                return entity instanceof IMob;
            case 3:
                return entity instanceof EntityAnimal;
            case 4:
                return entity instanceof EntityAnimal && !((EntityAnimal) entity).isChild();
            case 5:
                return entity instanceof EntityAnimal && ((EntityAnimal) entity).isChild();
            case 6:
                return entity instanceof EntityPlayer;
            case 7:
                return entity instanceof EntityLivingBase;
            case 8:
                return entity instanceof EntityItem;
            default:
                return false;
        }
    }

    protected boolean passesComparison(int matches) {
        if (senseAmount <= 0) {
            return matches > 0;
        }
        switch (equality) {
            case 1:
                return matches < senseAmount;
            case 2:
                return matches == senseAmount;
            case 0:
            default:
                return matches > senseAmount;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("SignalStrength", signalStrength);
        compound.setInteger("SenseTarget", senseTarget);
        compound.setBoolean("StrongSignal", strongSignal);
        compound.setInteger("SenseAmount", senseAmount);
        compound.setInteger("Equality", equality);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        signalStrength = compound.getInteger("SignalStrength");
        setSenseTarget(compound.getInteger("SenseTarget"));
        strongSignal = !compound.hasKey("StrongSignal") || compound.getBoolean("StrongSignal");
        senseAmount = compound.getInteger("SenseAmount");
        equality = compound.getInteger("Equality");
    }

    public static class T1 extends TileSensor {
    }

    public static class T2 extends TileSensor implements TileAdvancedMachine {

        public T2() {
            configureAdvancedMachine();
        }

        @Override
        public int getStandardEnergyCost() {
            return 2;
        }

        @Override
        protected boolean canSense() {
            int cost = getEnergyCost();
            return consumeEnergy(cost, false) >= cost;
        }

        public int getEnergyCost() {
            return Math.max(1, getAreaPositionsNearestFirst().size()) * getStandardEnergyCost();
        }

        @Override
        protected int countTargets(BlockPos targetPos) {
            if (getSenseTarget() == 0 || getSenseTarget() == 1) {
                int count = 0;
                for (BlockPos pos : getAreaPositionsNearestFirst()) {
                    boolean air = world.isAirBlock(pos);
                    if (getSenseTarget() == 1) {
                        if (air) {
                            count++;
                        }
                    } else if (!air && matchesFilter(world.getBlockState(pos))) {
                        count++;
                    }
                }
                return count;
            }

            int count = 0;
            for (Entity entity : world.getEntitiesWithinAABB(Entity.class, getAreaState().createArea(pos))) {
                if (matchesEntityTarget(entity)) {
                    count++;
                }
            }
            return count;
        }
    }
}
