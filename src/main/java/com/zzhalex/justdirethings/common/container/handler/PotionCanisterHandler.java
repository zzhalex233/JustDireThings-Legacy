package com.zzhalex.justdirethings.common.container.handler;

import com.zzhalex.justdirethings.capability.item.StackItemInventoryHandler;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class PotionCanisterHandler extends StackItemInventoryHandler {

    private final ItemStack canisterStack;
    private boolean filling;

    public PotionCanisterHandler(ItemStack canisterStack, String nbtKey, int size) {
        super(canisterStack, nbtKey, size);
        this.canisterStack = canisterStack;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (filling) {
            return;
        }

        ItemStack input = getStackInSlot(slot);
        if (input.isEmpty() || !(input.getItem() instanceof ItemPotion)) {
            return;
        }

        ItemStack consumed = input.copy();
        if (!PotionCanisterItem.tryFillFromPotionItem(canisterStack, consumed)) {
            return;
        }

        filling = true;
        try {
            setStackInSlot(slot, new ItemStack(Items.GLASS_BOTTLE));
        } finally {
            filling = false;
        }
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof ItemPotion || stack.getItem() == Items.GLASS_BOTTLE;
    }
}
