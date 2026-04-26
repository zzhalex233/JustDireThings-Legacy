package com.zzhalex.justdirethings.common.entity;

import com.zzhalex.justdirethings.common.util.TickAccelerationRules;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityTimeWand extends Entity {

    private static final DataParameter<Integer> TICK_LEVEL = EntityDataManager.createKey(EntityTimeWand.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> REMAINING_TIME = EntityDataManager.createKey(EntityTimeWand.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TOTAL_TIME = EntityDataManager.createKey(EntityTimeWand.class, DataSerializers.VARINT);

    private BlockPos acceleratedPos;

    public EntityTimeWand(World worldIn) {
        super(worldIn);
        setSize(0.25F, 0.25F);
        noClip = true;
    }

    public EntityTimeWand(World worldIn, BlockPos acceleratedPos) {
        this(worldIn);
        this.acceleratedPos = acceleratedPos;
        setPosition(acceleratedPos.getX() + 0.5D, acceleratedPos.getY() + 0.125D, acceleratedPos.getZ() + 0.5D);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TICK_LEVEL, 1);
        dataManager.register(REMAINING_TIME, TickAccelerationRules.initialDurationTicks());
        dataManager.register(TOTAL_TIME, TickAccelerationRules.initialDurationTicks());
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        move(MoverType.SELF, 0.0D, 0.0D, 0.0D);

        if (world.isRemote) {
            return;
        }

        if (!canSurvive()) {
            setDead();
            return;
        }

        doExtraTicks();
        setRemainingTime(getRemainingTime() - 1);
    }

    public boolean canSurvive() {
        if (acceleratedPos == null) {
            return false;
        }
        if (getRemainingTime() <= 0) {
            return false;
        }
        return !world.isAirBlock(acceleratedPos);
    }

    public void doExtraTicks() {
        if (acceleratedPos == null) {
            return;
        }

        IBlockState state = world.getBlockState(acceleratedPos);
        TileEntity tileEntity = world.getTileEntity(acceleratedPos);
        int extraTicks = TickAccelerationRules.extraTicksForLevel(getTickLevel());

        for (int tick = 0; tick < extraTicks; tick++) {
            if (tileEntity instanceof ITickable) {
                ((ITickable) tileEntity).update();
            }
            if (state.getBlock().getTickRandomly()) {
                state.getBlock().updateTick(world, acceleratedPos, state, world.rand);
            }
        }
    }

    public int getTickLevel() {
        return dataManager.get(TICK_LEVEL);
    }

    public void setTickLevel(int tickLevel) {
        dataManager.set(TICK_LEVEL, Math.max(1, tickLevel));
    }

    public int getRemainingTime() {
        return dataManager.get(REMAINING_TIME);
    }

    public void setRemainingTime(int remainingTime) {
        dataManager.set(REMAINING_TIME, Math.max(0, remainingTime));
    }

    public int getTotalTime() {
        return dataManager.get(TOTAL_TIME);
    }

    public void setTotalTime(int totalTime) {
        dataManager.set(TOTAL_TIME, Math.max(0, totalTime));
    }

    public void addBonusTime() {
        setRemainingTime(getRemainingTime() + TickAccelerationRules.bonusDurationTicks(getTotalTime(), getRemainingTime()));
    }

    public BlockPos getAcceleratedPos() {
        return acceleratedPos;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        setTickLevel(compound.getInteger("TickLevel"));
        setRemainingTime(compound.getInteger("RemainingTime"));
        setTotalTime(compound.getInteger("TotalTime"));
        if (compound.hasKey("AcceleratedPosX")) {
            acceleratedPos = new BlockPos(
                    compound.getInteger("AcceleratedPosX"),
                    compound.getInteger("AcceleratedPosY"),
                    compound.getInteger("AcceleratedPosZ")
            );
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("TickLevel", getTickLevel());
        compound.setInteger("RemainingTime", getRemainingTime());
        compound.setInteger("TotalTime", getTotalTime());
        if (acceleratedPos != null) {
            compound.setInteger("AcceleratedPosX", acceleratedPos.getX());
            compound.setInteger("AcceleratedPosY", acceleratedPos.getY());
            compound.setInteger("AcceleratedPosZ", acceleratedPos.getZ());
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0;
    }
}
