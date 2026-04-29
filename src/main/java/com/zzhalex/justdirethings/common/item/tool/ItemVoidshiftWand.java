package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.ItemPoweredTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemVoidshiftWand extends ItemPoweredTool {

    public static final int DURABILITY = 200;
    public static final int ENERGY_CAPACITY = 10_000;

    public ItemVoidshiftWand() {
        super(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, 50);
        setMaxDamage(DURABILITY);
        addSupportedAbilities(Ability.AIRBURST, Ability.VOIDSHIFT);
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.VOIDSHIFT_WAND_ENERGY);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.VOIDSHIFT_WAND_ENERGY, Math.max(0, Math.min(getEnergyCapacity(stack), storedEnergy)));
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
