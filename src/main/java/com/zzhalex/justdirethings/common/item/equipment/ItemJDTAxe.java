package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

public class ItemJDTAxe extends ItemAxe {

    private final JDTToolTier tier;

    public ItemJDTAxe(String id, JDTToolTier tier, float attackDamage, float attackSpeed) {
        super(tier.asVanillaMaterial(), attackDamage, attackSpeed);
        this.tier = tier;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, tier.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }
}
