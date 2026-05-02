package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Collection;

public final class ModCreativeTabs {

    public static final CreativeTabs JUST_DIRE_THINGS = new CreativeTabs(Reference.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.PORTAL_GUN_V2);
        }

        @Override
        public void displayAllRelevantItems(NonNullList<ItemStack> items) {
            addItems(items, ModContentItems.blockItems());
            addItems(items, ModItems.machineBlockItems());
            addItems(items, ModContentItems.resourceItems());
            addItems(items, ModItems.generalItems());
            addItems(items, ModContentItems.templateItems());
            addFluidBuckets(items);
            addItems(items, ModEquipmentItems.toolItems());
            addItems(items, ModEquipmentItems.bowItems());
            addItems(items, ModEquipmentItems.armorItems());
            addItems(items, ModContentItems.upgradeItems());
        }
    };

    private ModCreativeTabs() {
    }

    private static void addItems(NonNullList<ItemStack> stacks, Collection<? extends Item> items) {
        for (Item item : items) {
            if (item != null) {
                stacks.add(new ItemStack(item));
            }
        }
    }

    private static void addFluidBuckets(NonNullList<ItemStack> stacks) {
        for (String id : ModFluids.coreFluidIds()) {
            Fluid fluid = ModFluids.getFluid(id);
            if (fluid == null) {
                continue;
            }
            ItemStack bucket = FluidUtil.getFilledBucket(new FluidStack(fluid, 1000));
            if (!bucket.isEmpty()) {
                stacks.add(bucket);
            }
        }
    }
}
