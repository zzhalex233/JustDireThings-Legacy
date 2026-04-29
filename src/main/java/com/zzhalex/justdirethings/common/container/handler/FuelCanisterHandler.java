package com.zzhalex.justdirethings.common.container.handler;

import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class FuelCanisterHandler extends ItemStackHandler {

    private final ItemStack canisterStack;

    public FuelCanisterHandler(int size, ItemStack canisterStack) {
        super(size);
        this.canisterStack = canisterStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        ItemStack fuelStack = getStackInSlot(slot);
        if (!canisterStack.isEmpty() && !fuelStack.isEmpty()) {
            FuelCanisterItem.incrementFuel(canisterStack, fuelStack);
            if (fuelStack.isEmpty() || fuelStack.getCount() <= 0) {
                setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof FuelCanisterItem)
                && FuelBurnHelper.getBurnTime(stack) > 0
                && !FuelBurnHelper.hasContainerRemainder(stack);
    }
}
