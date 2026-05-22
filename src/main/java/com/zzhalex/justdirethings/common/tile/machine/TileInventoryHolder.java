package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import com.zzhalex.justdirethings.capability.inventory.InventoryHolderItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileInventoryMachineBase;
import com.zzhalex.justdirethings.common.util.ItemStackKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileInventoryHolder extends TileInventoryMachineBase {

    public static final int SLOT_COUNT = 41;

    private final InternalItemHandler filterHandler = new InternalItemHandler(SLOT_COUNT);
    private final Map<ItemStackKey, List<Integer>> filteredCache = new HashMap<>();
    private final InventoryHolderItemHandler inventoryHolderHandler = new InventoryHolderItemHandler(this, getItemHandler());

    private boolean compareNbt;
    private boolean filtersOnly;
    private boolean compareCounts;
    private boolean automatedFiltersOnly;
    private boolean automatedCompareCounts;
    private boolean renderPlayer = true;
    private int renderedSlot = 27;

    public TileInventoryHolder() {
        super(SLOT_COUNT);
    }

    public InternalItemHandler getFilterHandler() {
        return filterHandler;
    }

    public List<Integer> getFilteredSlots(ItemStackKey key) {
        return filteredCache.get(key);
    }

    public boolean isCompareNbt() {
        return compareNbt;
    }

    public void setCompareNbt(boolean compareNbt) {
        this.compareNbt = compareNbt;
        getFilterState().setCompareNbt(compareNbt);
        rebuildFilterCache();
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

    public void addSavedItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        filterHandler.setStackInSlot(slot, getItemHandler().getStackInSlot(slot).copy());
        rebuildFilterCache();
        markDirtyClient();
    }

    public void saveSettings(boolean compareNbt, boolean filtersOnly, boolean compareCounts, boolean automatedFiltersOnly, boolean automatedCompareCounts, boolean renderPlayer, int renderedSlot) {
        this.compareNbt = compareNbt;
        this.filtersOnly = filtersOnly;
        this.compareCounts = compareCounts;
        this.automatedFiltersOnly = automatedFiltersOnly;
        this.automatedCompareCounts = automatedCompareCounts;
        this.renderPlayer = renderPlayer;
        setRenderedSlot(renderedSlot);
        getFilterState().setCompareNbt(compareNbt);
        rebuildFilterCache();
    }

    public void rebuildFilterCache() {
        filteredCache.clear();
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            ItemStack stack = filterHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStackKey key = new ItemStackKey(stack, compareNbt);
            filteredCache.computeIfAbsent(key, ignored -> new ArrayList<>()).add(i);
        }
    }

    public int allowedExtractAmount(int slot, int amount) {
        ItemStack filterStack = filterHandler.getStackInSlot(slot);
        if (filterStack.isEmpty()) {
            return amount;
        }
        if (automatedCompareCounts) {
            int desiredAmount = getAutomatedSlotLimit(slot);
            int amountHad = getItemHandler().getStackInSlot(slot).getCount();
            if (desiredAmount > amountHad) {
                return 0;
            }
            return Math.min(amount, amountHad - desiredAmount);
        }
        return 0;
    }

    public boolean isStackValidFilter(ItemStack testStack, int slot) {
        ItemStack filterStack = filterHandler.getStackInSlot(slot);
        if (filterStack.isEmpty()) {
            return !automatedFiltersOnly;
        }
        return new ItemStackKey(testStack, compareNbt).equals(new ItemStackKey(filterStack, compareNbt));
    }

    public int getAutomatedSlotLimit(int slot) {
        if (!automatedCompareCounts) {
            return -1;
        }
        ItemStack filterStack = filterHandler.getStackInSlot(slot);
        return filterStack.isEmpty() ? -1 : filterStack.getCount();
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

    public boolean hasPortableData() {
        if (compareNbt || filtersOnly || automatedFiltersOnly || compareCounts || automatedCompareCounts || renderedSlot != 27) {
            return true;
        }
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            if (!filterHandler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        for (int i = 0; i < getItemHandler().getSlots(); i++) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public NBTTagCompound writePortableData(NBTTagCompound compound) {
        writeMachineStateToNbt(compound);
        compound.setTag("Items", getItemHandler().serializeNBT());
        writeInventoryHolderSettings(compound);
        return compound;
    }

    public void readPortableData(NBTTagCompound compound) {
        readMachineStateFromNbt(compound);
        if (compound.hasKey("Items")) {
            getItemHandler().deserializeNBT(compound.getCompoundTag("Items"));
        }
        readInventoryHolderSettings(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeInventoryHolderSettings(compound);
        return compound;
    }

    private void writeInventoryHolderSettings(NBTTagCompound compound) {
        compound.setBoolean("CompareNbt", compareNbt);
        compound.setBoolean("FiltersOnly", filtersOnly);
        compound.setBoolean("CompareCounts", compareCounts);
        compound.setBoolean("AutomatedFiltersOnly", automatedFiltersOnly);
        compound.setBoolean("AutomatedCompareCounts", automatedCompareCounts);
        compound.setBoolean("RenderPlayer", renderPlayer);
        compound.setInteger("RenderedSlot", renderedSlot);
        compound.setBoolean("compareNBT", compareNbt);
        compound.setBoolean("filtersOnly", filtersOnly);
        compound.setBoolean("compareCounts", compareCounts);
        compound.setBoolean("automatedFiltersOnly", automatedFiltersOnly);
        compound.setBoolean("automatedCompareCounts", automatedCompareCounts);
        compound.setBoolean("renderPlayer", renderPlayer);
        compound.setInteger("renderedSlot", renderedSlot);
        compound.setTag("filteredItems", filterHandler.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readInventoryHolderSettings(compound);
    }

    private void readInventoryHolderSettings(NBTTagCompound compound) {
        compareNbt = false;
        filtersOnly = false;
        compareCounts = false;
        automatedFiltersOnly = false;
        automatedCompareCounts = false;
        renderPlayer = true;
        renderedSlot = 27;
        compareNbt = compound.getBoolean("CompareNbt");
        filtersOnly = compound.getBoolean("FiltersOnly");
        compareCounts = compound.getBoolean("CompareCounts");
        automatedFiltersOnly = compound.getBoolean("AutomatedFiltersOnly");
        automatedCompareCounts = compound.getBoolean("AutomatedCompareCounts");
        renderPlayer = !compound.hasKey("RenderPlayer") || compound.getBoolean("RenderPlayer");
        renderedSlot = compound.hasKey("RenderedSlot") ? compound.getInteger("RenderedSlot") : 27;
        if (compound.hasKey("compareNBT")) {
            compareNbt = compound.getBoolean("compareNBT");
        }
        if (compound.hasKey("filtersOnly")) {
            filtersOnly = compound.getBoolean("filtersOnly");
        }
        if (compound.hasKey("compareCounts")) {
            compareCounts = compound.getBoolean("compareCounts");
        }
        if (compound.hasKey("automatedFiltersOnly")) {
            automatedFiltersOnly = compound.getBoolean("automatedFiltersOnly");
        }
        if (compound.hasKey("automatedCompareCounts")) {
            automatedCompareCounts = compound.getBoolean("automatedCompareCounts");
        }
        if (compound.hasKey("renderPlayer")) {
            renderPlayer = compound.getBoolean("renderPlayer");
        }
        if (compound.hasKey("renderedSlot")) {
            renderedSlot = compound.getInteger("renderedSlot");
        }
        if (compound.hasKey("filteredItems")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("filteredItems"));
        }
        getFilterState().setCompareNbt(compareNbt);
        setRenderedSlot(renderedSlot);
        rebuildFilterCache();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventoryHolderHandler;
        }
        return super.getCapability(capability, facing);
    }
}
