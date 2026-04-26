package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileInventoryHolder extends TileInventoryMachineBase {

    public static final int SLOT_COUNT = 41;

    private boolean compareNbt;
    private boolean filtersOnly;
    private boolean compareCounts;
    private boolean automatedFiltersOnly;
    private boolean automatedCompareCounts;
    private boolean renderPlayer = true;
    private int renderedSlot = 27;

    public TileInventoryHolder() {
        super(41);
    }

    public boolean isCompareNbt() {
        return compareNbt;
    }

    public void setCompareNbt(boolean compareNbt) {
        this.compareNbt = compareNbt;
        getFilterState().setCompareNbt(compareNbt);
    }

    public boolean isFiltersOnly() {
        return filtersOnly;
    }

    public void setFiltersOnly(boolean filtersOnly) {
        this.filtersOnly = filtersOnly;
    }

    public boolean isCompareCounts() {
        return compareCounts;
    }

    public void setCompareCounts(boolean compareCounts) {
        this.compareCounts = compareCounts;
    }

    public boolean isAutomatedFiltersOnly() {
        return automatedFiltersOnly;
    }

    public void setAutomatedFiltersOnly(boolean automatedFiltersOnly) {
        this.automatedFiltersOnly = automatedFiltersOnly;
    }

    public boolean isAutomatedCompareCounts() {
        return automatedCompareCounts;
    }

    public void setAutomatedCompareCounts(boolean automatedCompareCounts) {
        this.automatedCompareCounts = automatedCompareCounts;
    }

    public boolean isRenderPlayer() {
        return renderPlayer;
    }

    public void setRenderPlayer(boolean renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    public int getRenderedSlot() {
        return renderedSlot;
    }

    public void setRenderedSlot(int renderedSlot) {
        this.renderedSlot = Math.max(0, Math.min(SLOT_COUNT - 1, renderedSlot));
    }

    public void copyFromPlayer(EntityPlayer player) {
        for (int i = 0; i < 36 && i < player.inventory.mainInventory.size(); i++) {
            getItemHandler().setStackInSlot(i, player.inventory.mainInventory.get(i).copy());
        }
        for (int i = 0; i < 4 && i < player.inventory.armorInventory.size(); i++) {
            getItemHandler().setStackInSlot(36 + i, player.inventory.armorInventory.get(i).copy());
        }
        ItemStack offhand = player.inventory.offHandInventory.isEmpty() ? ItemStack.EMPTY : player.inventory.offHandInventory.get(0).copy();
        getItemHandler().setStackInSlot(40, offhand);
    }

    public void copyToPlayer(EntityPlayer player) {
        for (int i = 0; i < 36 && i < player.inventory.mainInventory.size(); i++) {
            player.inventory.mainInventory.set(i, getItemHandler().getStackInSlot(i).copy());
        }
        for (int i = 0; i < 4 && i < player.inventory.armorInventory.size(); i++) {
            player.inventory.armorInventory.set(i, getItemHandler().getStackInSlot(36 + i).copy());
        }
        if (!player.inventory.offHandInventory.isEmpty()) {
            player.inventory.offHandInventory.set(0, getItemHandler().getStackInSlot(40).copy());
        }
    }

    public void swapWithPlayer(EntityPlayer player) {
        for (int i = 0; i < 36 && i < player.inventory.mainInventory.size(); i++) {
            ItemStack playerStack = player.inventory.mainInventory.get(i).copy();
            player.inventory.mainInventory.set(i, getItemHandler().getStackInSlot(i).copy());
            getItemHandler().setStackInSlot(i, playerStack);
        }
        for (int i = 0; i < 4 && i < player.inventory.armorInventory.size(); i++) {
            ItemStack playerStack = player.inventory.armorInventory.get(i).copy();
            player.inventory.armorInventory.set(i, getItemHandler().getStackInSlot(36 + i).copy());
            getItemHandler().setStackInSlot(36 + i, playerStack);
        }
        if (!player.inventory.offHandInventory.isEmpty()) {
            ItemStack playerStack = player.inventory.offHandInventory.get(0).copy();
            player.inventory.offHandInventory.set(0, getItemHandler().getStackInSlot(40).copy());
            getItemHandler().setStackInSlot(40, playerStack);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("CompareNbt", compareNbt);
        compound.setBoolean("FiltersOnly", filtersOnly);
        compound.setBoolean("CompareCounts", compareCounts);
        compound.setBoolean("AutomatedFiltersOnly", automatedFiltersOnly);
        compound.setBoolean("AutomatedCompareCounts", automatedCompareCounts);
        compound.setBoolean("RenderPlayer", renderPlayer);
        compound.setInteger("RenderedSlot", renderedSlot);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        compareNbt = compound.getBoolean("CompareNbt");
        filtersOnly = compound.getBoolean("FiltersOnly");
        compareCounts = compound.getBoolean("CompareCounts");
        automatedFiltersOnly = compound.getBoolean("AutomatedFiltersOnly");
        automatedCompareCounts = compound.getBoolean("AutomatedCompareCounts");
        renderPlayer = !compound.hasKey("RenderPlayer") || compound.getBoolean("RenderPlayer");
        renderedSlot = compound.hasKey("RenderedSlot") ? compound.getInteger("RenderedSlot") : 27;
    }
}
