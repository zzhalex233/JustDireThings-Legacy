package com.zzhalex.justdirethings.common.container.handler;

import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class PocketGeneratorFuelHandler extends ItemStackHandler {

    private final ItemStack generatorStack;
    private final PocketGeneratorItem generatorItem;

    public PocketGeneratorFuelHandler(ItemStack generatorStack, PocketGeneratorItem generatorItem) {
        super(1);
        this.generatorStack = generatorStack;
        this.generatorItem = generatorItem;
        setStackInSlot(0, generatorItem.getFuelStack(generatorStack));
    }

    @Override
    protected void onContentsChanged(int slot) {
        generatorItem.setFuelStack(generatorStack, getStackInSlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return FuelBurnHelper.getBurnTime(stack) > 0;
    }
}
