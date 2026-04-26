package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TileBlockSwapper extends TileMachineBase implements ITickable {

    public enum SwapEntityType {
        NONE,
        HOSTILE,
        PASSIVE,
        ADULT,
        CHILD,
        PLAYER,
        LIVING,
        ITEM,
        ALL
    }

    private BlockPos boundTo;
    private boolean swapBlocks = true;
    private int swapEntityType;
    private boolean partnerExists;

    public TileBlockSwapper() {
        setTickSpeed(20);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (!shouldRunTimedMachine()) {
            return;
        }

        boolean exists = getPartnerTile() != null;
        boolean changed = exists != partnerExists;
        partnerExists = exists;
        if (exists && (swapBlocks || swapEntityType != SwapEntityType.NONE.ordinal())) {
            changed |= doSwap();
        }
        if (changed) {
            markDirtyClient();
        }
    }

    public boolean handleConnection(BlockPos otherPos) {
        if (world == null || otherPos == null || otherPos.equals(pos)) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(otherPos);
        if (!(tileEntity instanceof TileBlockSwapper)) {
            return false;
        }

        TileBlockSwapper other = (TileBlockSwapper) tileEntity;
        if (otherPos.equals(boundTo)) {
            removePartnerConnection();
            return false;
        }

        if (boundTo != null) {
            removePartnerConnection();
        }
        if (other.boundTo != null) {
            other.removePartnerConnection();
        }

        setBoundTo(otherPos);
        other.setBoundTo(pos);
        return true;
    }

    public void removePartnerConnection() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner != null && pos.equals(partner.boundTo)) {
            partner.setBoundTo(null);
        }
        setBoundTo(null);
    }

    public BlockPos getBoundTo() {
        return boundTo;
    }

    public void setBoundTo(BlockPos boundTo) {
        this.boundTo = boundTo;
        this.partnerExists = boundTo != null && world != null && world.getTileEntity(boundTo) instanceof TileBlockSwapper;
        markDirtyClient();
    }

    public boolean doesPartnerExist() {
        partnerExists = getPartnerTile() != null;
        return partnerExists;
    }

    public boolean isSwapBlocks() {
        return swapBlocks;
    }

    public void setSwapBlocks(boolean swapBlocks) {
        this.swapBlocks = swapBlocks;
    }

    public int getSwapEntityType() {
        return swapEntityType;
    }

    public void setSwapEntityType(int swapEntityType) {
        SwapEntityType[] values = SwapEntityType.values();
        this.swapEntityType = Math.max(0, Math.min(values.length - 1, swapEntityType));
    }

    private TileBlockSwapper getPartnerTile() {
        if (world == null || boundTo == null) {
            return null;
        }
        TileEntity tileEntity = world.getTileEntity(boundTo);
        return tileEntity instanceof TileBlockSwapper ? (TileBlockSwapper) tileEntity : null;
    }

    private boolean doSwap() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner == null) {
            return false;
        }

        boolean changed = false;
        if (swapBlocks) {
            changed = swapTargetBlocks(partner);
        }
        if (swapEntityType != SwapEntityType.NONE.ordinal()) {
            changed |= swapEntities(partner);
        }
        return changed;
    }

    private boolean swapTargetBlocks(TileBlockSwapper partner) {
        BlockPos first = MachineActionHelper.targetPos(this);
        BlockPos second = MachineActionHelper.targetPos(partner);
        if (first.equals(second) || first.equals(pos) || second.equals(pos)) {
            return false;
        }
        IBlockState firstState = world.getBlockState(first);
        IBlockState secondState = world.getBlockState(second);
        if (firstState == secondState) {
            return false;
        }
        world.setBlockState(first, secondState, 3);
        world.setBlockState(second, firstState, 3);
        return true;
    }

    private boolean swapEntities(TileBlockSwapper partner) {
        BlockPos first = MachineActionHelper.targetPos(this);
        BlockPos second = MachineActionHelper.targetPos(partner);
        List<Entity> firstEntities = matchingEntities(first);
        List<Entity> secondEntities = matchingEntities(second);
        boolean changed = false;
        for (Entity entity : firstEntities) {
            entity.setPositionAndUpdate(second.getX() + 0.5D, second.getY(), second.getZ() + 0.5D);
            changed = true;
        }
        for (Entity entity : secondEntities) {
            entity.setPositionAndUpdate(first.getX() + 0.5D, first.getY(), first.getZ() + 0.5D);
            changed = true;
        }
        return changed;
    }

    private List<Entity> matchingEntities(BlockPos target) {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(target));
        List<Entity> matches = new ArrayList<>();
        SwapEntityType type = SwapEntityType.values()[swapEntityType];
        for (Entity entity : entities) {
            if (matches(type, entity)) {
                matches.add(entity);
            }
        }
        return matches;
    }

    private static boolean matches(SwapEntityType type, Entity entity) {
        switch (type) {
            case HOSTILE:
                return entity instanceof IMob;
            case PASSIVE:
                return entity instanceof EntityAnimal;
            case ADULT:
                return entity instanceof EntityAnimal && !((EntityAnimal) entity).isChild();
            case CHILD:
                return entity instanceof EntityAnimal && ((EntityAnimal) entity).isChild();
            case PLAYER:
                return entity instanceof EntityPlayer;
            case LIVING:
                return entity instanceof net.minecraft.entity.EntityLivingBase;
            case ITEM:
                return entity instanceof EntityItem;
            case ALL:
                return true;
            case NONE:
            default:
                return false;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (boundTo != null) {
            compound.setInteger("BoundX", boundTo.getX());
            compound.setInteger("BoundY", boundTo.getY());
            compound.setInteger("BoundZ", boundTo.getZ());
            compound.setBoolean("HasBoundTo", true);
        }
        compound.setBoolean("SwapBlocks", swapBlocks);
        compound.setInteger("SwapEntityType", swapEntityType);
        compound.setBoolean("PartnerExists", partnerExists);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.getBoolean("HasBoundTo")) {
            boundTo = new BlockPos(compound.getInteger("BoundX"), compound.getInteger("BoundY"), compound.getInteger("BoundZ"));
        } else {
            boundTo = null;
        }
        swapBlocks = !compound.hasKey("SwapBlocks") || compound.getBoolean("SwapBlocks");
        setSwapEntityType(compound.getInteger("SwapEntityType"));
        partnerExists = compound.getBoolean("PartnerExists");
    }

    public static class T1 extends TileBlockSwapper {
    }

    public static class T2 extends TileBlockSwapper {
        // PARITY STUB: Upstream BlockSwapperT2BE adds powered area/filter behavior.
    }
}
