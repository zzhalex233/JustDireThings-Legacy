package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.AbilityParams;
import com.zzhalex.justdirethings.common.item.base.AbilityExecutionHelper;
import com.zzhalex.justdirethings.common.item.base.ItemPoweredTool;
import com.zzhalex.justdirethings.common.item.base.LeftClickableTool;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemEclipsegateWand extends ItemPoweredTool implements LeftClickableTool {

    public static final int DURABILITY = 200;
    public static final int ENERGY_CAPACITY = 100_000;

    public ItemEclipsegateWand() {
        super(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, 50);
        setMaxDamage(DURABILITY);
        addSupportedAbility(Ability.AIRBURST, new AbilityParams(1, 8, 1, 8));
        addSupportedAbility(Ability.VOIDSHIFT, new AbilityParams(1, 30, 1, 30));
        addSupportedAbility(Ability.ECLIPSEGATE, new AbilityParams(1, 20, 1, 20));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ActionResult<ItemStack> settingsResult = openSettingsIfSneaking(world, player, hand);
        if (settingsResult != null) {
            return settingsResult;
        }
        ActionResult<ItemStack> abilityResult = AbilityExecutionHelper.tryExecuteRightClickAbility(world, player, hand);
        return abilityResult != null ? abilityResult : super.onItemRightClick(world, player, hand);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        EnumActionResult abilityResult = AbilityExecutionHelper.tryExecuteUseOnAbility(world, player, hand, pos, facing);
        return abilityResult == EnumActionResult.SUCCESS ? abilityResult : super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean isFireResistantDrop(ItemStack stack) {
        return true;
    }

    @Override
    public int getStoredEnergy(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.ECLIPSEGATE_WAND_ENERGY);
    }

    @Override
    public void setStoredEnergy(ItemStack stack, int storedEnergy) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.ECLIPSEGATE_WAND_ENERGY, Math.max(0, Math.min(getEnergyCapacity(stack), storedEnergy)));
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
