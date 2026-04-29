package com.zzhalex.justdirethings.common.item.fuel;

import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public final class FuelBurnHelper {

    private FuelBurnHelper() {
    }

    public static int getBurnTime(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof FuelCanisterItem) {
            return stack.getItem().getItemBurnTime(stack);
        }
        return TileEntityFurnace.getItemBurnTime(stack);
    }

    public static int getBurnSpeedMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        if (stack.getItem() instanceof BurnSpeedFuel) {
            return Math.max(1, ((BurnSpeedFuel) stack.getItem()).getBurnSpeedMultiplier(stack));
        }
        if (stack.getItem() instanceof FuelCanisterItem) {
            return FuelCanisterItem.getBurnSpeedMultiplier(stack);
        }
        return 1;
    }

    public static boolean hasContainerRemainder(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem().hasContainerItem(stack);
    }
}
