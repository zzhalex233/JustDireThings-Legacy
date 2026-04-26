package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

public class ItemJDTHoe extends ItemHoe {

    private final JDTToolTier tier;

    public ItemJDTHoe(String id, JDTToolTier tier) {
        super(tier.asVanillaMaterial());
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }
}
