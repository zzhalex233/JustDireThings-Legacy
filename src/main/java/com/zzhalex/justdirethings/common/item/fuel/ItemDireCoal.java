package com.zzhalex.justdirethings.common.item.fuel;

import com.zzhalex.justdirethings.common.item.ItemSimpleContent;
import net.minecraft.item.ItemStack;

public class ItemDireCoal extends ItemSimpleContent implements BurnSpeedFuel {

    private final int burnTime;
    private final int burnSpeedMultiplier;

    public ItemDireCoal(int burnTime, int burnSpeedMultiplier) {
        this.burnTime = burnTime;
        this.burnSpeedMultiplier = burnSpeedMultiplier;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        return burnTime;
    }

    @Override
    public int getBurnSpeedMultiplier(ItemStack stack) {
        return burnSpeedMultiplier;
    }
}
