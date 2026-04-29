package com.zzhalex.justdirethings.common.item.fuel;

import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemDireFuelBlock extends ItemBlock implements BurnSpeedFuel {

    private final int burnTime;
    private final int burnSpeedMultiplier;

    public ItemDireFuelBlock(Block block, int burnTime, int burnSpeedMultiplier) {
        super(block);
        this.burnTime = burnTime;
        this.burnSpeedMultiplier = burnSpeedMultiplier;
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
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
