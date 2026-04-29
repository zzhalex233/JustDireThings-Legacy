package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import com.zzhalex.justdirethings.registry.ModContainers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class FuelCanisterItem extends Item {

    public static final int MAX_FUEL = 10000000;
    public static final int MINIMUM_TICKS_CONSUMED = 200;

    public FuelCanisterItem() {
        setMaxStackSize(1);
        setTranslationKey(Reference.MOD_ID + ".fuel_canister");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldStack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_FUEL_CANISTER, world, 0, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, heldStack);
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        return getFuelLevel(itemStack) >= MINIMUM_TICKS_CONSUMED ? MINIMUM_TICKS_CONSUMED : 0;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int fuelLevel = getFuelLevel(stack);
        if (GuiScreen.isShiftKeyDown()) {
            tooltip.add(TextFormatting.DARK_RED + I18n.format(
                    "justdirethings.fuelcanisteramt",
                    TooltipHelper.formatNumber(fuelLevel)
            ));
        } else {
            tooltip.add(TextFormatting.DARK_RED + I18n.format(
                    "justdirethings.fuelcanisteritemsamt",
                    TooltipHelper.formatNumber(fuelLevel / (double) MINIMUM_TICKS_CONSUMED)
            ));
            TooltipHelper.appendShiftForInfo(stack, tooltip);
        }
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack copy = new ItemStack(this);
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null) {
            copy.setTagCompound(tag.copy());
        }
        decrementFuel(copy);
        return copy;
    }

    public static int getFuelLevel(ItemStack stack) {
        return getOrCreateTag(stack).getInteger(JDTDataKeys.FUEL_CANISTER_FUEL_LEVEL);
    }

    public static void setFuelLevel(ItemStack stack, int fuelLevel) {
        getOrCreateTag(stack).setInteger(JDTDataKeys.FUEL_CANISTER_FUEL_LEVEL, Math.max(0, Math.min(MAX_FUEL, fuelLevel)));
    }

    public static double getBurnSpeed(ItemStack stack) {
        NBTTagCompound tag = getOrCreateTag(stack);
        return tag.hasKey(JDTDataKeys.FUEL_CANISTER_BURN_SPEED) ? tag.getDouble(JDTDataKeys.FUEL_CANISTER_BURN_SPEED) : 1.0D;
    }

    public static int getBurnSpeedMultiplier(ItemStack stack) {
        return Math.max(1, (int) Math.round(getBurnSpeed(stack)));
    }

    public static void setBurnSpeed(ItemStack stack, double burnSpeed) {
        getOrCreateTag(stack).setDouble(JDTDataKeys.FUEL_CANISTER_BURN_SPEED, Math.max(1.0D, burnSpeed));
    }

    public static void decrementFuel(ItemStack stack) {
        setFuelLevel(stack, getFuelLevel(stack) - MINIMUM_TICKS_CONSUMED);
    }

    public static void incrementFuel(ItemStack canister, ItemStack fuelStack) {
        int fuelPerPiece = FuelBurnHelper.getBurnTime(fuelStack);
        if (fuelPerPiece <= 0) {
            return;
        }

        int currentFuel = getFuelLevel(canister);
        double currentBurnSpeed = getBurnSpeed(canister);
        int fuelMultiplier = FuelBurnHelper.getBurnSpeedMultiplier(fuelStack);
        int totalAddedFuel = 0;

        while (!fuelStack.isEmpty() && currentFuel + totalAddedFuel + fuelPerPiece <= MAX_FUEL) {
            totalAddedFuel += fuelPerPiece;
            fuelStack.shrink(1);
        }

        if (totalAddedFuel <= 0) {
            return;
        }

        setFuelLevel(canister, currentFuel + totalAddedFuel);
        setBurnSpeed(canister, PocketGeneratorMath.weightedBurnMultiplier(currentFuel, currentBurnSpeed, totalAddedFuel, fuelMultiplier));
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
