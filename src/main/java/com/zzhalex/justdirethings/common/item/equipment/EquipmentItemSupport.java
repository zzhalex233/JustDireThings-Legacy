package com.zzhalex.justdirethings.common.item.equipment;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

final class EquipmentItemSupport {

    private EquipmentItemSupport() {
    }

    static void configure(Item item, String id) {
        item.setRegistryName(Reference.MOD_ID, id);
        item.setTranslationKey(Reference.MOD_ID + "." + id);
        item.setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
    }

    static boolean matchesRepairItem(ItemStack repairStack, ItemStack expectedRepairStack) {
        return !repairStack.isEmpty() && !expectedRepairStack.isEmpty() && ItemStack.areItemsEqual(repairStack, expectedRepairStack);
    }
}
