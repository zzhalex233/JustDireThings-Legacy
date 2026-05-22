package com.zzhalex.justdirethings.common.tile;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.registry.ModRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileUpgradeStation extends TileEntity implements ITickable {

    public static final int SLOT_TEMPLATE = 0;
    public static final int SLOT_BASE = 1;
    public static final int SLOT_ADDITION = 2;
    public static final int SLOT_OUTPUT = 3;

    private boolean refreshingOutput;

    private final InternalItemHandler itemHandler = new InternalItemHandler(4) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == SLOT_OUTPUT) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return !stack.isEmpty() && ModRecipes.isValidUpgradeStationInput(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!refreshingOutput && slot != SLOT_OUTPUT) {
                refreshOutput();
            }
            TileUpgradeStation.this.markDirty();
        }
    };

    @Override
    public void update() {
        if (world != null && !world.isRemote) {
            refreshOutput();
        }
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        itemHandler.setStackInSlot(slot, stack);
    }

    public void refreshOutput() {
        ItemStack output = ModRecipes.getUpgradeStationOutput(
                itemHandler.getStackInSlot(SLOT_TEMPLATE),
                itemHandler.getStackInSlot(SLOT_BASE),
                itemHandler.getStackInSlot(SLOT_ADDITION)
        );

        refreshingOutput = true;
        itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        refreshingOutput = false;
        markDirty();
    }

    public boolean consumeInputsForOutput(ItemStack expectedOutput) {
        ItemStack currentOutput = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (currentOutput.isEmpty()) {
            currentOutput = ModRecipes.getUpgradeStationOutput(
                    itemHandler.getStackInSlot(SLOT_TEMPLATE),
                    itemHandler.getStackInSlot(SLOT_BASE),
                    itemHandler.getStackInSlot(SLOT_ADDITION)
            );
        }
        if (expectedOutput.isEmpty() || currentOutput.isEmpty() || currentOutput.getItem() != expectedOutput.getItem()) {
            refreshOutput();
            return false;
        }
        if (!ItemStack.areItemStackTagsEqual(currentOutput, expectedOutput)) {
            refreshOutput();
            return false;
        }

        shrinkInput(SLOT_TEMPLATE);
        shrinkInput(SLOT_BASE);
        shrinkInput(SLOT_ADDITION);
        refreshingOutput = true;
        itemHandler.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
        refreshingOutput = false;
        refreshOutput();
        markDirty();
        return true;
    }

    private void shrinkInput(int slot) {
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            if (stack.getCount() <= 0) {
                itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("tile.justdirethings.upgrade_station.name");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Items", itemHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Items")) {
            itemHandler.deserializeNBT(compound.getCompoundTag("Items"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemHandler;
        }
        return super.getCapability(capability, facing);
    }
}
