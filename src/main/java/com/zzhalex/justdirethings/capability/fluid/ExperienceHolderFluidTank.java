package com.zzhalex.justdirethings.capability.fluid;

import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class ExperienceHolderFluidTank implements IFluidHandler {

    private static final int MILLIBUCKETS_PER_XP = 20;

    private final TileExperienceHolder experienceHolder;

    public ExperienceHolderFluidTank(TileExperienceHolder experienceHolder) {
        this.experienceHolder = experienceHolder;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] {
                new FluidTankProperties(currentFluid(), Integer.MAX_VALUE, true, true)
        };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0 || !isExperienceFluid(resource)) {
            return 0;
        }
        int amount = resource.amount - resource.amount % MILLIBUCKETS_PER_XP;
        if (amount <= 0) {
            return 0;
        }
        if (!doFill) {
            int room = Integer.MAX_VALUE - getFluidAmount();
            return Math.min(room - room % MILLIBUCKETS_PER_XP, amount);
        }
        int remainder = insertFluid(amount);
        int filled = amount - remainder;
        if (filled > 0) {
            experienceHolder.markDirtyClient();
        }
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || !isExperienceFluid(resource)) {
            return null;
        }
        return drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        int drained = maxDrain - maxDrain % MILLIBUCKETS_PER_XP;
        if (drained <= 0) {
            return null;
        }
        drained = Math.min(drained, getFluidAmount());
        if (drained <= 0) {
            return null;
        }
        FluidStack result = new FluidStack(getExperienceFluid(), drained);
        if (doDrain) {
            extractFluid(drained);
            experienceHolder.markDirtyClient();
        }
        return result;
    }

    private int getFluidAmount() {
        int exp = experienceHolder.getStoredExperience();
        if (exp > Integer.MAX_VALUE / MILLIBUCKETS_PER_XP) {
            return Integer.MAX_VALUE;
        }
        return exp * MILLIBUCKETS_PER_XP;
    }

    private FluidStack currentFluid() {
        return new FluidStack(getExperienceFluid(), getFluidAmount());
    }

    private int insertFluid(int amount) {
        int remaining = experienceHolder.addExperience(amount / MILLIBUCKETS_PER_XP);
        int excessFluid = amount % MILLIBUCKETS_PER_XP;
        return remaining * MILLIBUCKETS_PER_XP + excessFluid;
    }

    private int extractFluid(int amount) {
        int expNeeded = amount / MILLIBUCKETS_PER_XP;
        int unavailable = experienceHolder.subExperience(expNeeded);
        return unavailable * MILLIBUCKETS_PER_XP + amount % MILLIBUCKETS_PER_XP;
    }

    private static boolean isExperienceFluid(FluidStack stack) {
        return stack != null && stack.getFluid() == getExperienceFluid();
    }

    private static Fluid getExperienceFluid() {
        return ModFluids.getFluid("xp_fluid");
    }

    public NBTTagCompound writeToNbt() {
        return new NBTTagCompound();
    }
}
