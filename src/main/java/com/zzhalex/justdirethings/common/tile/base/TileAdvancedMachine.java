package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface TileAdvancedMachine extends TileFilteredMachine {

    int ADVANCED_FILTER_SLOTS = 9;
    int ADVANCED_ENERGY_CAPACITY = 100000;
    int ITEM_CHARGE_PER_TICK = 5000;

    default TileMachineBase getMachine() {
        return (TileMachineBase) this;
    }

    int getStandardEnergyCost();

    default ItemStackHandler getFilterHandler() {
        return null;
    }

    default void configureAdvancedMachine() {
        MachineEnergyState energy = getMachine().getEnergyState();
        energy.setCapacity(ADVANCED_ENERGY_CAPACITY);
        energy.setMaxReceive(ADVANCED_ENERGY_CAPACITY);
        energy.setMaxExtract(ADVANCED_ENERGY_CAPACITY);
        getMachine().getFilterState().setAllowList(false);
        EnumFacing facing = EnumFacing.byIndex(getMachine().getDirection());
        if (facing != null) {
            getMachine().getAreaState().setOffset(facing.getXOffset(), facing.getYOffset(), facing.getZOffset());
        }
    }

    default boolean hasEnoughEnergy(int amount) {
        return getMachine().getEnergyState().extractEnergy(amount, true) >= amount;
    }

    default int consumeEnergy(int amount, boolean simulate) {
        return getMachine().getEnergyState().extractEnergy(amount, simulate);
    }

    default boolean spendStandardEnergy() {
        return consumeEnergy(getStandardEnergyCost(), false) >= getStandardEnergyCost();
    }

    default void chargeItemStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return;
        }
        IEnergyStorage itemEnergy = stack.getCapability(CapabilityEnergy.ENERGY, null);
        if (itemEnergy == null || !itemEnergy.canReceive()) {
            return;
        }
        int accepted = itemEnergy.receiveEnergy(ITEM_CHARGE_PER_TICK, true);
        if (accepted <= 0) {
            return;
        }
        int extracted = consumeEnergy(accepted, false);
        if (extracted > 0) {
            itemEnergy.receiveEnergy(extracted, false);
        }
    }

    default List<BlockPos> getAreaPositionsNearestFirst() {
        AxisAlignedBB area = getMachine().getAreaState().createArea(getMachine().getPos());
        List<BlockPos> positions = new ArrayList<>();
        for (int x = (int) Math.floor(area.minX); x <= (int) Math.floor(area.maxX - 0.0001D); x++) {
            for (int y = (int) Math.floor(area.minY); y <= (int) Math.floor(area.maxY - 0.0001D); y++) {
                for (int z = (int) Math.floor(area.minZ); z <= (int) Math.floor(area.maxZ - 0.0001D); z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        positions.sort(Comparator.comparingDouble(pos -> pos.distanceSq(getMachine().getPos())));
        return positions;
    }

    default BlockPos getOffsetTargetPos() {
        MachineAreaState area = getMachine().getAreaState();
        return getMachine().getPos().add(area.getXOffset(), area.getYOffset(), area.getZOffset());
    }

    default boolean matchesFilter(ItemStack stack) {
        return MachineFilterHelper.matchesFilter(getFilterHandler(), getMachine().getFilterState(), stack);
    }

    default boolean matchesFilterStack(ItemStack filter, ItemStack stack) {
        return MachineFilterHelper.matchesFilterStack(getMachine().getFilterState(), filter, stack);
    }

    default boolean matchesBlockFilter(IBlockState state, BlockPos pos) {
        if (state.getBlock().isAir(state, getMachine().getWorld(), pos)) {
            return matchesFilter(ItemStack.EMPTY);
        }
        ItemStack blockStack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        return matchesFilter(blockStack);
    }

    default boolean matchesDropFilter(IBlockState state, BlockPos pos, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, getMachine().getWorld(), pos, state, fortune);
        if (drops.isEmpty()) {
            return matchesFilter(ItemStack.EMPTY);
        }
        for (ItemStack drop : drops) {
            if (matchesFilter(drop)) {
                return true;
            }
        }
        return false;
    }

    default boolean matchesFluidFilter(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        if (fluid == FluidRegistry.WATER) {
            return matchesFilter(new ItemStack(Items.WATER_BUCKET));
        }
        if (fluid == FluidRegistry.LAVA) {
            return matchesFilter(new ItemStack(Items.LAVA_BUCKET));
        }
        Block block = fluid.getBlock();
        return block != null && matchesFilter(new ItemStack(block));
    }

    default NBTTagCompound writeAdvancedMachineToNbt(NBTTagCompound compound) {
        ItemStackHandler filterHandler = getFilterHandler();
        if (filterHandler != null) {
            compound.setTag("AdvancedFilters", filterHandler.serializeNBT());
        }
        return compound;
    }

    default void readAdvancedMachineFromNbt(NBTTagCompound compound) {
        ItemStackHandler filterHandler = getFilterHandler();
        if (filterHandler != null && compound.hasKey("AdvancedFilters")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("AdvancedFilters"));
        }
    }
}
