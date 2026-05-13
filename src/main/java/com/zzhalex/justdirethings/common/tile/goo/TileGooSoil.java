package com.zzhalex.justdirethings.common.tile.goo;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class TileGooSoil extends TileEntity {

    private static final String KEY_BOUND_INVENTORY = "boundinventory";
    private static final String KEY_BOUND_SIDE = "boundinventory_side";
    private static final String KEY_DIMENSION = "dimension";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";

    private BlockPos boundInventoryPos;
    private EnumFacing boundInventorySide;
    private int boundInventoryDimension;
    private IItemHandler attachedInventory;

    public void bindInventory(BlockPos inventoryPos, EnumFacing side, int dimension) {
        this.boundInventoryPos = inventoryPos;
        this.boundInventorySide = side == null ? EnumFacing.UP : side;
        this.boundInventoryDimension = dimension;
        invalidateHandler();
        markDirty();
    }

    public boolean hasBoundInventory() {
        return boundInventoryPos != null && boundInventorySide != null;
    }

    public void handleDrops(List<ItemStack> drops, BlockPos cropPos) {
        IItemHandler handler = getAttachedInventory();
        if (handler != null) {
            for (int i = drops.size() - 1; i >= 0; i--) {
                ItemStack remainder = insertStack(handler, drops.get(i));
                if (remainder.isEmpty()) {
                    drops.remove(i);
                } else {
                    drops.set(i, remainder);
                }
            }
        }
        for (ItemStack drop : drops) {
            net.minecraft.block.Block.spawnAsEntity(world, cropPos, drop);
        }
    }

    @Nullable
    private IItemHandler getAttachedInventory() {
        if (world == null || world.isRemote || !hasBoundInventory() || world.getMinecraftServer() == null) {
            return null;
        }
        if (attachedInventory != null) {
            return attachedInventory;
        }

        WorldServer boundWorld = world.getMinecraftServer().getWorld(boundInventoryDimension);
        if (boundWorld == null) {
            return null;
        }
        TileEntity tileEntity = boundWorld.getTileEntity(boundInventoryPos);
        if (tileEntity == null || !tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, boundInventorySide)) {
            return null;
        }
        attachedInventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, boundInventorySide);
        return attachedInventory;
    }

    public void invalidateHandler() {
        attachedInventory = null;
    }

    private static ItemStack insertStack(IItemHandler handler, ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, false);
        }
        return remainder;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (hasBoundInventory()) {
            NBTTagCompound bound = new NBTTagCompound();
            bound.setInteger(KEY_X, boundInventoryPos.getX());
            bound.setInteger(KEY_Y, boundInventoryPos.getY());
            bound.setInteger(KEY_Z, boundInventoryPos.getZ());
            bound.setInteger(KEY_DIMENSION, boundInventoryDimension);
            compound.setTag(KEY_BOUND_INVENTORY, bound);
            compound.setInteger(KEY_BOUND_SIDE, boundInventorySide.ordinal());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(KEY_BOUND_INVENTORY) && compound.hasKey(KEY_BOUND_SIDE)) {
            NBTTagCompound bound = compound.getCompoundTag(KEY_BOUND_INVENTORY);
            boundInventoryPos = new BlockPos(bound.getInteger(KEY_X), bound.getInteger(KEY_Y), bound.getInteger(KEY_Z));
            boundInventoryDimension = bound.getInteger(KEY_DIMENSION);
            int side = compound.getInteger(KEY_BOUND_SIDE);
            boundInventorySide = side >= 0 && side < EnumFacing.values().length ? EnumFacing.values()[side] : EnumFacing.UP;
            invalidateHandler();
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing);
    }
}
