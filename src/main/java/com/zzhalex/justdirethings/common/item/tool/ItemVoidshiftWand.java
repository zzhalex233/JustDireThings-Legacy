package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ItemPoweredTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemVoidshiftWand extends ItemPoweredTool {

    public static final int DURABILITY = 200;
    public static final int ENERGY_CAPACITY = 10_000;

    public ItemVoidshiftWand() {
        super(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, 50);
        setMaxDamage(DURABILITY);
        addSupportedAbility(Ability.AIRBURST, new AbilityParams(1, 4, 1, 4));
        addSupportedAbility(Ability.VOIDSHIFT, new AbilityParams(1, 15, 1, 15));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(world, player, hand);
        return abilityResult != null ? abilityResult : super.onItemRightClick(world, player, hand);
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
