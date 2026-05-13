package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.capability.item.StackItemCapabilityProvider;
import com.zzhalex.justdirethings.common.item.tooltip.TooltipHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class ItemPoweredTool extends ItemToggleableTool implements EnergyBackedItem {

    private final int energyCapacity;
    private final int maxReceive;
    private final int maxExtract;
    private final int blockBreakFeCost;

    protected ItemPoweredTool() {
        this(100000, 100000, 100000, 50);
    }

    protected ItemPoweredTool(int energyCapacity, int maxReceive, int maxExtract, int blockBreakFeCost) {
        this.energyCapacity = energyCapacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.blockBreakFeCost = blockBreakFeCost;
    }

    public int getEnergyCapacity() {
        return energyCapacity;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public int getBlockBreakFeCost() {
        return blockBreakFeCost;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new StackItemCapabilityProvider(stack, this, null);
    }

    public int getEnergyCapacity(ItemStack stack) {
        return energyCapacity;
    }

    public int getMaxReceive(ItemStack stack) {
        return maxReceive;
    }

    public int getMaxExtract(ItemStack stack) {
        return maxExtract;
    }

    public boolean hasSufficientPower(int storedEnergy) {
        return storedEnergy >= blockBreakFeCost;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return isEnergyBarVisible(stack) || super.showDurabilityBar(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return isEnergyBarVisible(stack) ? getEnergyDurabilityForDisplay(stack) : super.getDurabilityForDisplay(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return isEnergyBarVisible(stack) ? getEnergyBarColor(stack) : super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TooltipHelper.appendFEText(stack, tooltip);
    }
}
