package com.zzhalex.justdirethings.client.jei;

import com.zzhalex.justdirethings.client.gui.base.GuiMachineBase;
import com.zzhalex.justdirethings.common.container.slot.SlotFilterItemHandler;
import com.zzhalex.justdirethings.network.JDTNetwork;
import com.zzhalex.justdirethings.network.message.MessageGhostSlot;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GhostFilterBasic implements IGhostIngredientHandler<GuiMachineBase> {

    @Override
    public <I> List<Target<I>> getTargets(GuiMachineBase gui, I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();
        if (!(ingredient instanceof ItemStack)) {
            return targets;
        }
        for (Slot slot : gui.getSlots()) {
            if (slot instanceof SlotFilterItemHandler) {
                targets.add(new FilterTarget<>(gui, slot));
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    private static final class FilterTarget<I> implements Target<I> {
        private final GuiMachineBase gui;
        private final Slot slot;

        private FilterTarget(GuiMachineBase gui, Slot slot) {
            this.gui = gui;
            this.slot = slot;
        }

        @Override
        public Rectangle getArea() {
            return new Rectangle(gui.getGuiLeftValue() + slot.xPos, gui.getGuiTopValue() + slot.yPos, 16, 16);
        }

        @Override
        public void accept(I ingredient) {
            if (!(ingredient instanceof ItemStack)) {
                return;
            }
            ItemStack stack = ((ItemStack) ingredient).copy();
            stack.setCount(1);
            JDTNetwork.getChannel().sendToServer(new MessageGhostSlot(gui.getWindowId(), slot.slotNumber, stack));
        }
    }
}
