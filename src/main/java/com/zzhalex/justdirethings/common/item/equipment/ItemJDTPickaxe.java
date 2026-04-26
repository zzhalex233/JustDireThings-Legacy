package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

public class ItemJDTPickaxe extends ItemPickaxe {

    private final JDTToolTier tier;

    public ItemJDTPickaxe(String id, JDTToolTier tier) {
        super(tier.asVanillaMaterial());
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }
}
