package com.zzhalex.justdirethings.common.container.slot;

import com.zzhalex.justdirethings.common.tile.machine.TileFluidGenerator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotFluidFuel extends SlotItemHandler {

    public SlotFluidFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(stack);
        if (fluidHandler == null) {
            return false;
        }
        FluidStack contained = fluidHandler.drain(1000, false);
        return TileFluidGenerator.isRefinedFuel(contained);
    }
}
