package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.common.item.material.JDTArmorMaterial;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemJDTArmor extends ItemArmor {

    private final JDTArmorMaterial material;

    public ItemJDTArmor(String id, JDTArmorMaterial material, EntityEquipmentSlot slot) {
        super(material.asVanillaMaterial(), 0, slot);
        this.material = material;
        EquipmentItemSupport.configure(this, id);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return EquipmentItemSupport.matchesRepairItem(repair, material.getRepairStack()) || super.getIsRepairable(toRepair, repair);
    }
}
