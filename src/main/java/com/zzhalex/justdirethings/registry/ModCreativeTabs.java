package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class ModCreativeTabs {

    public static final CreativeTabs JUST_DIRE_THINGS = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.PORTAL_GUN_V2);
        }
    };

    private ModCreativeTabs() {
    }
}
