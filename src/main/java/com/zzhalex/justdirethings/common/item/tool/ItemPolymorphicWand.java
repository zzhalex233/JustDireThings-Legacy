package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ItemPolymorphicWand extends ItemToggleableTool implements FluidBackedItem {

    public static final int DURABILITY = 200;
    public static final int FLUID_CAPACITY = 2_000;

    public ItemPolymorphicWand() {
        setMaxDamage(DURABILITY);
        addSupportedAbilities(Ability.LAVAREPAIR, Ability.POLYMORPH_RANDOM);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, null, this);
    }

    @Override
    public int getStoredFluid(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POLYMORPHIC_WAND_FLUID);
    }

    @Override
    public void setStoredFluid(ItemStack stack, int storedFluid) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POLYMORPHIC_WAND_FLUID, Math.max(0, Math.min(getFluidCapacity(stack), storedFluid)));
    }

    @Override
    public int getFluidCapacity(ItemStack stack) {
        return FLUID_CAPACITY;
    }

    @Override
    public Fluid getContainedFluid(ItemStack stack) {
        ModFluids.bootstrap();
        return ModFluids.getFluid("polymorphic_fluid");
    }

    @Override
    public boolean canFillFluid(ItemStack stack, FluidStack resource) {
        return resource != null && resource.amount > 0 && resource.getFluid() == getContainedFluid(stack);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }
}
