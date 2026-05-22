package com.zzhalex.justdirethings.common.item.block;

import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemBlockInventoryHolder extends ItemBlock {

    private static final String TAG_MACHINE_DATA = "JDTInventoryHolderData";

    public ItemBlockInventoryHolder(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, net.minecraft.block.state.IBlockState newState) {
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (!placed || world.isRemote || !stack.hasTagCompound()) {
            return placed;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(TAG_MACHINE_DATA)) {
            return placed;
        }
        if (world.getTileEntity(pos) instanceof TileInventoryHolder) {
            TileInventoryHolder tile = (TileInventoryHolder) world.getTileEntity(pos);
            tile.readPortableData(tag.getCompoundTag(TAG_MACHINE_DATA));
            tile.setOwnerUuid(player == null ? null : player.getUniqueID());
            tile.markDirtyClient();
        }
        return true;
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = super.getNBTShareTag(stack);
        return tag == null ? null : tag.copy();
    }

    public static void writePortableData(ItemStack stack, TileInventoryHolder tile) {
        if (stack.isEmpty() || tile == null || !tile.hasPortableData()) {
            return;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        root.setTag(TAG_MACHINE_DATA, tile.writePortableData(new NBTTagCompound()));
    }
}
