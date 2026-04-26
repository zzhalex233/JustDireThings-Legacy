package com.zzhalex.justdirethings.capability.inventory;

public class FilterItemHandler extends InternalItemHandler {

    public FilterItemHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}
