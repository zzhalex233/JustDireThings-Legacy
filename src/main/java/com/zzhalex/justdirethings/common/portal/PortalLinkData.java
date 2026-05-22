package com.zzhalex.justdirethings.common.portal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PortalLinkData {

    public static final int MAX_FAVORITES = 12;

    private static final String KEY_FAVORITE_INDEX = "FavoriteIndex";
    private static final String KEY_STAY_OPEN = "StayOpen";
    private static final String KEY_PORTAL_GUN_UUID = "PortalGunUuid";
    private static final String KEY_PREVIOUS = "Previous";
    private static final String KEY_FAVORITES = "Favorites";

    private int favoriteIndex;
    private boolean stayOpen;
    private UUID portalGunUuid;
    private PortalDestination previous = PortalDestination.EMPTY;
    private final List<PortalDestination> favorites = new ArrayList<>(MAX_FAVORITES);

    public PortalLinkData() {
        for (int slot = 0; slot < MAX_FAVORITES; slot++) {
            favorites.add(PortalDestination.EMPTY);
        }
    }

    public int getFavoriteIndex() {
        return favoriteIndex;
    }

    public void setFavoriteIndex(int favoriteIndex) {
        this.favoriteIndex = clampFavoriteIndex(favoriteIndex);
    }

    public boolean isStayOpen() {
        return stayOpen;
    }

    public void setStayOpen(boolean stayOpen) {
        this.stayOpen = stayOpen;
    }

    public UUID getPortalGunUuid() {
        return portalGunUuid;
    }

    public void setPortalGunUuid(UUID portalGunUuid) {
        this.portalGunUuid = portalGunUuid;
    }

    public PortalDestination getPrevious() {
        return previous;
    }

    public void setPrevious(PortalDestination previous) {
        this.previous = previous == null ? PortalDestination.EMPTY : previous;
    }

    public List<PortalDestination> getFavorites() {
        return Collections.unmodifiableList(favorites);
    }

    public PortalDestination getFavorite(int slot) {
        if (!isValidSlot(slot)) {
            return PortalDestination.EMPTY;
        }
        return favorites.get(slot);
    }

    public void setFavorite(int slot, PortalDestination destination) {
        if (!isValidSlot(slot)) {
            return;
        }
        favorites.set(slot, destination == null ? PortalDestination.EMPTY : destination);
    }

    public void clearFavorite(int slot) {
        setFavorite(slot, PortalDestination.EMPTY);
    }

    public PortalDestination getSelectedFavorite() {
        return getFavorite(getFavoriteIndex());
    }

    public static NBTTagCompound write(PortalLinkData data) {
        PortalLinkData safe = data == null ? new PortalLinkData() : data;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(KEY_FAVORITE_INDEX, safe.getFavoriteIndex());
        tag.setBoolean(KEY_STAY_OPEN, safe.isStayOpen());
        if (safe.getPortalGunUuid() != null) {
            tag.setString(KEY_PORTAL_GUN_UUID, safe.getPortalGunUuid().toString());
        }
        if (!safe.getPrevious().isEmpty()) {
            tag.setTag(KEY_PREVIOUS, safe.getPrevious().writeToNbt());
        }

        NBTTagList favoritesTag = new NBTTagList();
        for (int slot = 0; slot < MAX_FAVORITES; slot++) {
            favoritesTag.appendTag(safe.getFavorite(slot).writeToNbt());
        }
        tag.setTag(KEY_FAVORITES, favoritesTag);
        return tag;
    }

    public static PortalLinkData read(NBTTagCompound tag) {
        PortalLinkData data = new PortalLinkData();
        if (tag == null) {
            return data;
        }

        data.setFavoriteIndex(tag.getInteger(KEY_FAVORITE_INDEX));
        data.setStayOpen(tag.getBoolean(KEY_STAY_OPEN));
        if (tag.hasKey(KEY_PORTAL_GUN_UUID, Constants.NBT.TAG_STRING)) {
            try {
                data.setPortalGunUuid(UUID.fromString(tag.getString(KEY_PORTAL_GUN_UUID)));
            } catch (IllegalArgumentException ignored) {
                data.setPortalGunUuid(null);
            }
        }
        if (tag.hasKey(KEY_PREVIOUS, Constants.NBT.TAG_COMPOUND)) {
            data.setPrevious(PortalDestination.read(tag.getCompoundTag(KEY_PREVIOUS)));
        }

        NBTTagList favoritesTag = tag.getTagList(KEY_FAVORITES, Constants.NBT.TAG_COMPOUND);
        for (int slot = 0; slot < Math.min(MAX_FAVORITES, favoritesTag.tagCount()); slot++) {
            data.setFavorite(slot, PortalDestination.read(favoritesTag.getCompoundTagAt(slot)));
        }
        return data;
    }

    private static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < MAX_FAVORITES;
    }

    private static int clampFavoriteIndex(int favoriteIndex) {
        return Math.max(0, Math.min(MAX_FAVORITES - 1, favoriteIndex));
    }

    public static final class PortalDestination {

        public static final PortalDestination EMPTY = new PortalDestination("", 0, 0.0D, 0.0D, 0.0D, EnumFacing.NORTH, true);

        private static final String KEY_NAME = "Name";
        private static final String KEY_DIMENSION = "Dimension";
        private static final String KEY_X = "X";
        private static final String KEY_Y = "Y";
        private static final String KEY_Z = "Z";
        private static final String KEY_FACING = "Facing";
        private static final String KEY_EMPTY = "Empty";

        private final String name;
        private final int dimension;
        private final double x;
        private final double y;
        private final double z;
        private final EnumFacing facing;
        private final boolean empty;

        public PortalDestination(String name, int dimension, double x, double y, double z, EnumFacing facing) {
            this(name, dimension, x, y, z, facing, false);
        }

        private PortalDestination(String name, int dimension, double x, double y, double z, EnumFacing facing, boolean empty) {
            this.name = name == null ? "" : name;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing == null ? EnumFacing.NORTH : facing;
            this.empty = empty;
        }

        public String getName() {
            return name;
        }

        public int getDimension() {
            return dimension;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public EnumFacing getFacing() {
            return facing;
        }

        public boolean isEmpty() {
            return empty;
        }

        public NBTTagCompound writeToNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(KEY_EMPTY, empty);
            if (empty) {
                return tag;
            }
            tag.setString(KEY_NAME, name);
            tag.setInteger(KEY_DIMENSION, dimension);
            tag.setDouble(KEY_X, x);
            tag.setDouble(KEY_Y, y);
            tag.setDouble(KEY_Z, z);
            tag.setString(KEY_FACING, facing.getName());
            return tag;
        }

        public static PortalDestination read(NBTTagCompound tag) {
            if (tag == null || tag.isEmpty() || tag.getBoolean(KEY_EMPTY)) {
                return EMPTY;
            }

            EnumFacing facing = EnumFacing.byName(tag.getString(KEY_FACING));
            return new PortalDestination(
                    tag.getString(KEY_NAME),
                    tag.getInteger(KEY_DIMENSION),
                    tag.getDouble(KEY_X),
                    tag.getDouble(KEY_Y),
                    tag.getDouble(KEY_Z),
                    facing == null ? EnumFacing.NORTH : facing
            );
        }

        public static PortalDestination fromPlayer(EntityPlayer player, String name) {
            return new PortalDestination(
                    name,
                    player.world.provider.getDimension(),
                    player.posX,
                    player.posY,
                    player.posZ,
                    facingFromPlayer(player)
            );
        }

        public static EnumFacing facingFromPlayer(EntityPlayer player) {
            float pitch = player.rotationPitch;
            if (pitch < -45.0F) {
                return EnumFacing.UP;
            }
            if (pitch > 45.0F) {
                return EnumFacing.DOWN;
            }
            return player.getHorizontalFacing();
        }
    }
}
