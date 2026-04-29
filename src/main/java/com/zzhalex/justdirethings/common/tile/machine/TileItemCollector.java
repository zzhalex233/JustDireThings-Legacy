package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.MachineFilterHelper;
import com.zzhalex.justdirethings.common.tile.base.TileFilteredMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class TileItemCollector extends TileMachineBase implements ITickable, TileFilteredMachine {

    public static final int FILTER_SLOT_COUNT = 9;

    private final FilterItemHandler filterHandler = new FilterItemHandler(FILTER_SLOT_COUNT);
    private boolean respectPickupDelay;
    private boolean showParticles = true;

    public TileItemCollector() {
        getFilterState().setAllowList(false);
        getAreaState().setArea(2.0D, 2.0D, 2.0D);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        evaluateRedstoneControl();
        if (!getRedstoneState().isPulseMode() && world.getTotalWorldTime() % 10L != 0L) {
            return;
        }
        if (!isRedstoneActive()) {
            return;
        }

        AxisAlignedBB area = getAreaState().createArea(pos);
        List<EntityItem> entityItems = world.getEntitiesWithinAABB(EntityItem.class, area);
        IItemHandler attachedInventory = getAttachedInventory();
        if (attachedInventory == null) {
            return;
        }

        for (EntityItem entityItem : entityItems) {
            if (respectPickupDelay && entityItem.cannotPickup()) {
                continue;
            }
            if (!matchesFilter(entityItem.getItem())) {
                continue;
            }
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(attachedInventory, entityItem.getItem(), false);
            if (leftover.isEmpty()) {
                entityItem.setDead();
            } else {
                entityItem.setItem(leftover);
            }
        }
        markDirtyClient();
    }

    private IItemHandler getAttachedInventory() {
        EnumFacing facing = MachineActionHelper.getFacing(this);
        TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
    }

    public FilterItemHandler getFilterHandler() {
        return filterHandler;
    }

    public boolean matchesFilter(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return MachineFilterHelper.matchesFilter(filterHandler, getFilterState(), stack);
    }

    public boolean isRespectPickupDelay() {
        return respectPickupDelay;
    }

    public void setRespectPickupDelay(boolean respectPickupDelay) {
        this.respectPickupDelay = respectPickupDelay;
    }

    public boolean isShowParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean showParticles) {
        this.showParticles = showParticles;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("RespectPickupDelay", respectPickupDelay);
        compound.setBoolean("ShowParticles", showParticles);
        compound.setTag("Filters", filterHandler.serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        respectPickupDelay = compound.getBoolean("RespectPickupDelay");
        showParticles = !compound.hasKey("ShowParticles") || compound.getBoolean("ShowParticles");
        if (compound.hasKey("Filters")) {
            filterHandler.deserializeNBT(compound.getCompoundTag("Filters"));
        }
    }
}
