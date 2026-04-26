package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.compat.accessory.AccessoryInventoryBridge;
import com.zzhalex.justdirethings.common.tile.base.TileTimedMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TilePlayerAccessor extends TileTimedMachineBase {

    private static final int MACHINE_SLOTS = 54;
    private static final int ACCESSORY_SLOT_START = 40;

    public enum InventoryConnectionType {
        NORMAL,
        ARMOR,
        OFFHAND
    }

    private final EnumMap<EnumFacing, InventoryConnectionType> inventoryConnectionTypes = new EnumMap<>(EnumFacing.class);
    private String targetPlayerName = "";

    public TilePlayerAccessor() {
        super(MACHINE_SLOTS);
        for (EnumFacing facing : EnumFacing.values()) {
            inventoryConnectionTypes.put(facing, InventoryConnectionType.NORMAL);
        }
    }

    public String getTargetPlayerName() {
        return targetPlayerName;
    }

    @Override
    protected boolean performWork() {
        EntityPlayer player = findTargetPlayer();
        if (player == null) {
            if (!targetPlayerName.isEmpty()) {
                targetPlayerName = "";
                clearInventory();
                return true;
            }
            return false;
        }

        targetPlayerName = player.getName();
        for (int slot = 0; slot < 36 && slot < player.inventory.mainInventory.size(); slot++) {
            getItemHandler().setStackInSlot(slot, player.inventory.mainInventory.get(slot).copy());
        }
        for (int slot = 0; slot < 4 && slot < player.inventory.armorInventory.size(); slot++) {
            getItemHandler().setStackInSlot(36 + slot, player.inventory.armorInventory.get(slot).copy());
        }

        int index = ACCESSORY_SLOT_START;
        List<ItemStack> accessories = AccessoryInventoryBridge.forPlayer(player).copyStacks();
        for (ItemStack accessory : accessories) {
            if (index >= MACHINE_SLOTS) {
                break;
            }
            getItemHandler().setStackInSlot(index++, accessory.copy());
        }
        while (index < MACHINE_SLOTS) {
            getItemHandler().setStackInSlot(index++, ItemStack.EMPTY);
        }
        return true;
    }

    private EntityPlayer findTargetPlayer() {
        UUID ownerUuid = getOwnerUuid();
        EntityPlayer fallback = null;
        double closestDistance = Double.MAX_VALUE;

        for (EntityPlayer player : world.playerEntities) {
            if (ownerUuid != null && ownerUuid.equals(player.getUniqueID())) {
                return player;
            }
            double distance = player.getDistanceSq(pos);
            if (distance < closestDistance && distance <= 64.0D * 64.0D) {
                closestDistance = distance;
                fallback = player;
            }
        }
        return fallback;
    }

    private void clearInventory() {
        for (int slot = 0; slot < MACHINE_SLOTS; slot++) {
            getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public InventoryConnectionType getInventoryConnectionType(EnumFacing facing) {
        InventoryConnectionType type = inventoryConnectionTypes.get(facing);
        return type == null ? InventoryConnectionType.NORMAL : type;
    }

    public int getInventoryConnectionTypeIndex(EnumFacing facing) {
        return getInventoryConnectionType(facing).ordinal();
    }

    public void setInventoryConnectionType(EnumFacing facing, int type) {
        InventoryConnectionType[] values = InventoryConnectionType.values();
        int index = Math.max(0, Math.min(values.length - 1, type));
        inventoryConnectionTypes.put(facing, values[index]);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("TargetPlayerName", targetPlayerName);
        for (Map.Entry<EnumFacing, InventoryConnectionType> entry : inventoryConnectionTypes.entrySet()) {
            compound.setInteger("InventoryConnection" + entry.getKey().getIndex(), entry.getValue().ordinal());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        targetPlayerName = compound.getString("TargetPlayerName");
        for (EnumFacing facing : EnumFacing.values()) {
            setInventoryConnectionType(facing, compound.getInteger("InventoryConnection" + facing.getIndex()));
        }
    }
}
