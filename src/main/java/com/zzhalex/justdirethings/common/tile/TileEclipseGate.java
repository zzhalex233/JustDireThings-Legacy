package com.zzhalex.justdirethings.common.tile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEclipseGate extends TileEntity implements ITickable {

    private static final int MAX_LIFE = 100;

    private int lifetime;
    private IBlockState sourceBlock;

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        lifetime++;
        if (lifetime >= MAX_LIFE) {
            restoreSourceBlock();
        }
    }

    public void setSourceBlock(IBlockState sourceBlock) {
        this.sourceBlock = sourceBlock;
        markDirtyClient();
    }

    public IBlockState getSourceBlock() {
        return sourceBlock;
    }

    private void restoreSourceBlock() {
        if (sourceBlock == null) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            return;
        }
        if (!sourceBlock.getBlock().canPlaceBlockAt(world, pos)) {
            sourceBlock.getBlock().dropBlockAsItem(world, pos, sourceBlock, 0);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            return;
        }
        IBlockState adjustedState = sourceBlock.getActualState(world, pos);
        world.setBlockState(pos, adjustedState, 3);
        world.notifyNeighborsOfStateChange(pos, sourceBlock.getBlock(), true);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("lifetime", lifetime);
        if (sourceBlock != null) {
            compound.setInteger("sourceBlock", Block.getStateId(sourceBlock));
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        lifetime = compound.getInteger("lifetime");
        if (compound.hasKey("sourceBlock")) {
            sourceBlock = Block.getStateById(compound.getInteger("sourceBlock"));
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    private void markDirtyClient() {
        markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }
}
