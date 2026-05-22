package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.PlayerAccessorItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class TilePlayerAccessor extends TileMachineBase implements net.minecraft.util.ITickable {

    public enum InventoryConnectionType {
        INVENTORY,
        ARMOR,
        OFFHAND
    }

    private final EnumMap<EnumFacing, InventoryConnectionType> inventoryConnectionTypes = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, PlayerAccessorItemHandler> playerHandlers = new EnumMap<>(EnumFacing.class);
    private final IItemHandler clientPlayerHandler = new ItemStackHandler(1);
    private EntityPlayerMP serverPlayer;
    private boolean checkedPlayer;

    public TilePlayerAccessor() {
        setTickSpeed(JDTConfig.playerAccessorValidationTime);
        for (EnumFacing facing : EnumFacing.values()) {
            inventoryConnectionTypes.put(facing, InventoryConnectionType.INVENTORY);
        }
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        if (getOperationTicks() == 0) {
            checkedPlayer = false;
            validatePlayer();
        }
    }

    public void updateSidedInventory(EnumFacing side, int type) {
        inventoryConnectionTypes.put(side, inventoryConnectionType(type));
        clearCache();
    }

    public void clearCache() {
        playerHandlers.clear();
        serverPlayer = null;
        checkedPlayer = false;
        if (world != null && !world.isRemote) {
            updateServerPlayer();
        }
        markDirtyClient();
    }

    public void validatePlayer() {
        EntityPlayerMP player = getServerPlayer();
        if (player != null && (player.isDead || !isValidDimension(player))) {
            clearCache();
        }
    }

    @Nullable
    public EntityPlayerMP getServerPlayer() {
        if (serverPlayer == null && !checkedPlayer) {
            updateServerPlayer();
        }
        return serverPlayer;
    }

    public void updateServerPlayer() {
        checkedPlayer = true;
        UUID ownerUuid = getOwnerUuid();
        MinecraftServer server = world == null ? null : world.getMinecraftServer();
        if (ownerUuid == null || server == null) {
            serverPlayer = null;
            return;
        }

        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(ownerUuid);
        if (!isValidDimension(player)) {
            player = null;
        }
        if (serverPlayer != player) {
            serverPlayer = player;
            markDirtyClient();
        }
    }

    public boolean isValidDimension(@Nullable EntityPlayerMP player) {
        if (player == null || player.isDead) {
            return false;
        }
        if (!JDTConfig.playerAccessorDimensionalBlacklisting) {
            return true;
        }
        DimensionType dimensionType = player.world.provider.getDimensionType();
        String dimensionName = dimensionType == null ? Integer.toString(player.dimension) : dimensionType.getName();
        String dimensionId = Integer.toString(player.dimension);
        String dimensionKey = getDimensionKey(player.dimension, dimensionName);
        for (String blacklistedDimension : JDTConfig.playerAccessorBlacklistedDimensions) {
            String blacklistEntry = blacklistedDimension == null ? "" : blacklistedDimension.trim();
            if (dimensionName.equalsIgnoreCase(blacklistEntry)
                    || dimensionId.equals(blacklistEntry)
                    || dimensionKey.equalsIgnoreCase(blacklistEntry)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public IItemHandler getPlayerHandler(@Nullable EnumFacing side) {
        if (side == null) {
            side = EnumFacing.UP;
        }
        EntityPlayerMP player = getServerPlayer();
        if (player == null) {
            return null;
        }
        PlayerAccessorItemHandler handler = playerHandlers.get(side);
        if (handler == null || handler.isPlayerInvalid()) {
            handler = new PlayerAccessorItemHandler(player, toHandlerInventoryType(getInventoryConnectionType(side)));
            playerHandlers.put(side, handler);
        }
        return handler;
    }

    public InventoryConnectionType getInventoryConnectionType(EnumFacing facing) {
        InventoryConnectionType type = inventoryConnectionTypes.get(facing);
        return type == null ? InventoryConnectionType.INVENTORY : type;
    }

    public int getInventoryConnectionTypeIndex(EnumFacing facing) {
        return getInventoryConnectionType(facing).ordinal();
    }

    public void setInventoryConnectionType(EnumFacing facing, int type) {
        updateSidedInventory(facing, type);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        for (Map.Entry<EnumFacing, InventoryConnectionType> entry : inventoryConnectionTypes.entrySet()) {
            compound.setInteger("sidedInventory_" + entry.getKey().getIndex(), entry.getValue().ordinal());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        for (EnumFacing facing : EnumFacing.values()) {
            String key = "sidedInventory_" + facing.getIndex();
            String legacyKey = "InventoryConnection" + facing.getIndex();
            if (compound.hasKey(key)) {
                inventoryConnectionTypes.put(facing, inventoryConnectionType(compound.getInteger(key)));
            } else if (compound.hasKey(legacyKey)) {
                inventoryConnectionTypes.put(facing, inventoryConnectionType(compound.getInteger(legacyKey)));
            } else {
                inventoryConnectionTypes.put(facing, InventoryConnectionType.INVENTORY);
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return world == null || world.isRemote || getServerPlayer() != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return world != null && world.isRemote ? (T) clientPlayerHandler : (T) getPlayerHandler(facing);
        }
        return super.getCapability(capability, facing);
    }

    private static InventoryConnectionType inventoryConnectionType(int index) {
        InventoryConnectionType[] values = InventoryConnectionType.values();
        if (index < 0 || index >= values.length) {
            return InventoryConnectionType.INVENTORY;
        }
        return values[index];
    }

    private static PlayerAccessorItemHandler.InventoryType toHandlerInventoryType(InventoryConnectionType type) {
        if (type == InventoryConnectionType.ARMOR) {
            return PlayerAccessorItemHandler.InventoryType.ARMOR;
        }
        if (type == InventoryConnectionType.OFFHAND) {
            return PlayerAccessorItemHandler.InventoryType.OFFHAND;
        }
        return PlayerAccessorItemHandler.InventoryType.INVENTORY;
    }

    private static String getDimensionKey(int dimensionId, String dimensionName) {
        if (dimensionId == 0) {
            return "minecraft:overworld";
        }
        if (dimensionId == -1) {
            return "minecraft:the_nether";
        }
        if (dimensionId == 1) {
            return "minecraft:the_end";
        }
        return dimensionName;
    }
}
