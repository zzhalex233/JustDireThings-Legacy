package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.FluidBackedItem;
import com.zzhalex.justdirethings.common.item.base.FluidPickupHelper;
import com.zzhalex.justdirethings.common.item.base.ItemToggleableTool;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPolymorphicWand extends ItemToggleableTool implements FluidBackedItem {

    public static final int DURABILITY = 200;
    public static final int FLUID_CAPACITY = 2_000;

    public ItemPolymorphicWand() {
        setMaxDamage(DURABILITY);
        addSupportedAbilities(Ability.LAVAREPAIR, Ability.POLYMORPH_RANDOM);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (FluidPickupHelper.pickupSourceFluid(world, player, stack, rayTrace(world, player, true), getContainedFluid(stack))) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.DARK_AQUA + I18n.format(
                "justdirethings.polymorphicfluidamt",
                TooltipHelper.formatNumber(getStoredFluid(stack)),
                TooltipHelper.formatNumber(getFluidCapacity(stack))
        ));
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
