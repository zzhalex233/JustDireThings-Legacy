package com.zzhalex.justdirethings.common.container.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotFluidFuel extends SlotItemHandler {

    public SlotFluidFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        FluidStack contained = FluidUtil.getFluidContained(stack);
        return contained != null && contained.amount > 0 && contained.getFluid() == FluidRegistry.LAVA;
    }
}
