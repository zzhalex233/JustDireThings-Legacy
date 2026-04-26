package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ItemFluidPoweredTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ItemPolymorphicWandV2 extends ItemFluidPoweredTool {

    public ItemPolymorphicWandV2() {
        super(1_000_000, 10_000, 10_000, 50, 8_000);
        setTranslationKey(Reference.MOD_ID + ".polymorphic_wand_v2");
        addSupportedAbilities(Ability.POLYMORPH_RANDOM, Ability.POLYMORPH_TARGET);
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.POLYMORPHIC_WAND_ENERGY);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.POLYMORPHIC_WAND_ENERGY, Math.max(0, Math.min(getEnergyCapacity(stack), storedEnergy)));
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
