package com.zzhalex.justdirethings.common.tile.base;

import net.minecraft.nbt.NBTTagCompound;

public class MachineFilterState {

    private boolean allowList = false;
    private boolean compareNbt;
    private int blockItemFilter = -1;

    public boolean isAllowList() {
        return allowList;
    }

    public void setAllowList(boolean allowList) {
        this.allowList = allowList;
    }

    public boolean isCompareNbt() {
        return compareNbt;
    }

    public void setCompareNbt(boolean compareNbt) {
        this.compareNbt = compareNbt;
    }

    public int getBlockItemFilter() {
        return blockItemFilter;
    }

    public void setBlockItemFilter(int blockItemFilter) {
        this.blockItemFilter = blockItemFilter;
    }

    public NBTTagCompound writeToNbt(NBTTagCompound tag) {
        tag.setBoolean("AllowList", allowList);
        tag.setBoolean("CompareNbt", compareNbt);
        tag.setInteger("BlockItemFilter", blockItemFilter);
        return tag;
    }

    public void readFromNbt(NBTTagCompound tag) {
        allowList = tag.hasKey("AllowList") && tag.getBoolean("AllowList");
        compareNbt = tag.getBoolean("CompareNbt");
        blockItemFilter = tag.hasKey("BlockItemFilter") ? tag.getInteger("BlockItemFilter") : -1;
    }
}
