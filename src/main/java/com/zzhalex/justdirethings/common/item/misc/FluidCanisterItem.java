package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidCanisterItem extends Item implements FluidBackedItem {

    public enum FillMode {
        NONE,
        JDT_ONLY,
        ALL;

        public FillMode next() {
            FillMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public static final int MAX_MB = 8000;

    public FluidCanisterItem() {
        setMaxStackSize(1);
        setTranslationKey(Reference.MOD_ID + ".fluid_canister");
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, null, this);
    }

    public static String getFluidName(ItemStack stack) {
        return getOrCreateTag(stack).getString(JDTDataKeys.FLUID_CANISTER_FLUID_NAME);
    }

    public static void setFluidName(ItemStack stack, String fluidName) {
        getOrCreateTag(stack).setString(JDTDataKeys.FLUID_CANISTER_FLUID_NAME, fluidName == null ? "" : fluidName);
    }

    public static int getFluidAmount(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.FLUID_CANISTER_AMOUNT);
    }

    public static int getFullness(ItemStack stack) {
        int fluidAmount = getFluidAmount(stack);
        if (fluidAmount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(fluidAmount / 1000.0D));
    }

    public static void setFluidAmount(ItemStack stack, int amount) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.FLUID_CANISTER_AMOUNT, Math.max(0, Math.min(MAX_MB, amount)));
        if (getFluidAmount(stack) == 0) {
            setFluidName(stack, "");
        }
    }

    @Override
    public int getStoredFluid(ItemStack stack) {
        return getFluidAmount(stack);
    }

    @Override
    public void setStoredFluid(ItemStack stack, int storedFluid) {
        setFluidAmount(stack, storedFluid);
    }

    @Override
    public int getFluidCapacity(ItemStack stack) {
        return MAX_MB;
    }

    @Override
    public Fluid getContainedFluid(ItemStack stack) {
        String fluidName = getFluidName(stack);
        return fluidName.isEmpty() ? null : FluidRegistry.getFluid(fluidName);
    }

    @Override
    public boolean canFillFluid(ItemStack stack, FluidStack resource) {
        return resource != null && resource.amount > 0 && (getContainedFluid(stack) == null || getContainedFluid(stack) == resource.getFluid());
    }

    @Override
    public void applyFilledAmount(ItemStack stack, FluidStack resource, int storedFluid) {
        if (resource != null && resource.getFluid() != null && getContainedFluid(stack) == null) {
            setFluidName(stack, resource.getFluid().getName());
        }
        setStoredFluid(stack, storedFluid);
    }

    public static FillMode getFillMode(ItemStack stack) {
        int ordinal = getOrCreateTag(stack).getInteger(JDTDataKeys.FLUID_CANISTER_FILL_MODE);
        FillMode[] values = FillMode.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FillMode.NONE;
        }
        return values[ordinal];
    }

    public static void setFillMode(ItemStack stack, FillMode fillMode) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.FLUID_CANISTER_FILL_MODE, fillMode == null ? 0 : fillMode.ordinal());
    }

    public static FillMode cycleFillMode(ItemStack stack) {
        FillMode next = getFillMode(stack).next();
        setFillMode(stack, next);
        return next;
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}
