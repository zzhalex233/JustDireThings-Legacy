package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemJDTWrench extends Item {

    private final JDTToolTier tier;

    public ItemJDTWrench(String id, JDTToolTier tier) {
        this.tier = tier;
        setMaxStackSize(1);
        setMaxDamage(Math.max(384, tier.asVanillaMaterial().getMaxUses()));
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }
}
