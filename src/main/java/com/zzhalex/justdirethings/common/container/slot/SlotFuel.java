package com.zzhalex.justdirethings.common.container.slot;

import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotFuel extends SlotItemHandler {

    public SlotFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return FuelBurnHelper.getBurnTime(stack) > 0;
    }
}
