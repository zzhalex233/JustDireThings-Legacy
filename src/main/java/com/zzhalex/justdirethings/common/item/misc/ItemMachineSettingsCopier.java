package com.zzhalex.justdirethings.common.item.misc;

import com.zzhalex.justdirethings.common.tile.base.MachineAreaState;
import com.zzhalex.justdirethings.common.tile.base.MachineFilterState;
import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.tile.base.TileFilteredMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class ItemMachineSettingsCopier extends Item {

    private static final String TAG_ROOT = "JDTMachineSettingsCopier";
    private static final String TAG_COPIED_MACHINE_DATA = "CopiedMachineData";
    private static final String TAG_COPY_AREA = "CopyArea";
    private static final String TAG_COPY_OFFSET = "CopyOffset";
    private static final String TAG_COPY_FILTER = "CopyFilter";
    private static final String TAG_COPY_REDSTONE = "CopyRedstone";

    private static final String KEY_X_RADIUS = "xRadiusDouble";
    private static final String KEY_Y_RADIUS = "yRadiusDouble";
    private static final String KEY_Z_RADIUS = "zRadiusDouble";
    private static final String KEY_X_OFFSET = "xOffset";
    private static final String KEY_Y_OFFSET = "yOffset";
    private static final String KEY_Z_OFFSET = "zOffset";
    private static final String KEY_FILTERED_ITEMS = "filteredItems";
    private static final String KEY_ALLOWLIST = "allowlist";
    private static final String KEY_COMPARE_NBT = "compareNBT";
    private static final String KEY_BLOCK_ITEM_FILTER = "blockitemfilter";
    private static final String KEY_REDSTONE_MODE = "redstoneMode";
    private static final String KEY_PULSED = "pulsed";
    private static final String KEY_RECEIVING_REDSTONE = "receivingRedstone";

    public ItemMachineSettingsCopier() {
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileMachineBase)) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                saveSettings((TileMachineBase) tileEntity, stack);
                player.sendStatusMessage(new TextComponentTranslation("justdirethings.settingscopied"), true);
                world.playSound(null, pos, net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else if (loadSettings((TileMachineBase) tileEntity, stack)) {
                player.sendStatusMessage(new TextComponentTranslation("justdirethings.settingspasted"), true);
                world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public boolean saveSettings(TileMachineBase machine, ItemStack stack) {
        if (machine == null || stack == null || stack.isEmpty()) {
            return false;
        }

        NBTTagCompound copiedData = new NBTTagCompound();
        if (getCopyArea(stack)) {
            saveAreaOnly(machine.getAreaState(), copiedData);
        }
        if (getCopyOffset(stack)) {
            saveOffsetOnly(machine.getAreaState(), copiedData);
        }
        if (getCopyFilter(stack)) {
            saveFilter(machine, copiedData);
        }
        if (getCopyRedstone(stack)) {
            saveRedstone(machine.getRedstoneState(), copiedData);
        }

        if (copiedData.getKeySet().isEmpty()) {
            return false;
        }

        getRootTag(stack).setTag(TAG_COPIED_MACHINE_DATA, copiedData);
        return true;
    }

    public boolean loadSettings(TileMachineBase machine, ItemStack stack) {
        if (machine == null || stack == null || stack.isEmpty() || !hasCopiedSettings(stack)) {
            return false;
        }

        NBTTagCompound copiedData = getCopiedMachineData(stack);
        if (getCopyArea(stack)) {
            loadAreaOnly(machine.getAreaState(), copiedData);
        }
        if (getCopyOffset(stack)) {
            loadOffsetOnly(machine.getAreaState(), copiedData);
        }
        if (getCopyFilter(stack)) {
            loadFilter(machine, copiedData);
        }
        if (getCopyRedstone(stack)) {
            loadRedstone(machine.getRedstoneState(), copiedData);
        }

        machine.markDirtyClient();
        return true;
    }

    public static void setSettings(ItemStack stack, boolean area, boolean offset, boolean filter, boolean redstone) {
        NBTTagCompound root = getRootTag(stack);
        root.setBoolean(TAG_COPY_AREA, area);
        root.setBoolean(TAG_COPY_OFFSET, offset);
        root.setBoolean(TAG_COPY_FILTER, filter);
        root.setBoolean(TAG_COPY_REDSTONE, redstone);
    }

    public static boolean getCopyArea(ItemStack stack) {
        return getBooleanSetting(stack, TAG_COPY_AREA);
    }

    public static boolean getCopyOffset(ItemStack stack) {
        return getBooleanSetting(stack, TAG_COPY_OFFSET);
    }

    public static boolean getCopyFilter(ItemStack stack) {
        return getBooleanSetting(stack, TAG_COPY_FILTER);
    }

    public static boolean getCopyRedstone(ItemStack stack) {
        return getBooleanSetting(stack, TAG_COPY_REDSTONE);
    }

    public static boolean hasCopiedSettings(ItemStack stack) {
        return !getCopiedMachineData(stack).getKeySet().isEmpty();
    }

    public static NBTTagCompound getCopiedMachineData(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getTagCompound() == null) {
            return new NBTTagCompound();
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(TAG_ROOT, Constants.NBT.TAG_COMPOUND)) {
            return new NBTTagCompound();
        }
        NBTTagCompound root = tag.getCompoundTag(TAG_ROOT);
        if (!root.hasKey(TAG_COPIED_MACHINE_DATA, Constants.NBT.TAG_COMPOUND)) {
            return new NBTTagCompound();
        }
        return root.getCompoundTag(TAG_COPIED_MACHINE_DATA);
    }

    private static void saveAreaOnly(MachineAreaState areaState, NBTTagCompound tag) {
        tag.setDouble(KEY_X_RADIUS, areaState.getXRadius());
        tag.setDouble(KEY_Y_RADIUS, areaState.getYRadius());
        tag.setDouble(KEY_Z_RADIUS, areaState.getZRadius());
    }

    private static void saveOffsetOnly(MachineAreaState areaState, NBTTagCompound tag) {
        tag.setInteger(KEY_X_OFFSET, areaState.getXOffset());
        tag.setInteger(KEY_Y_OFFSET, areaState.getYOffset());
        tag.setInteger(KEY_Z_OFFSET, areaState.getZOffset());
    }

    private static void saveFilter(TileMachineBase machine, NBTTagCompound tag) {
        MachineFilterState filterState = machine.getFilterState();
        tag.setBoolean(KEY_ALLOWLIST, filterState.isAllowList());
        tag.setBoolean(KEY_COMPARE_NBT, filterState.isCompareNbt());
        tag.setInteger(KEY_BLOCK_ITEM_FILTER, filterState.getBlockItemFilter());
        if (machine instanceof TileFilteredMachine) {
            ItemStackHandler filterHandler = ((TileFilteredMachine) machine).getFilterHandler();
            if (filterHandler != null) {
                tag.setTag(KEY_FILTERED_ITEMS, filterHandler.serializeNBT());
            }
        }
    }

    private static void saveRedstone(MachineRedstoneState redstoneState, NBTTagCompound tag) {
        tag.setInteger(KEY_REDSTONE_MODE, redstoneState.getMode().ordinal());
        tag.setBoolean(KEY_PULSED, redstoneState.isPulsed());
        tag.setBoolean(KEY_RECEIVING_REDSTONE, redstoneState.isReceivingRedstone());
    }

    private static void loadAreaOnly(MachineAreaState areaState, NBTTagCompound tag) {
        if (!tag.hasKey(KEY_X_RADIUS)) {
            return;
        }
        areaState.setArea(tag.getDouble(KEY_X_RADIUS), tag.getDouble(KEY_Y_RADIUS), tag.getDouble(KEY_Z_RADIUS));
    }

    private static void loadOffsetOnly(MachineAreaState areaState, NBTTagCompound tag) {
        if (!tag.hasKey(KEY_X_OFFSET)) {
            return;
        }
        areaState.setOffset(tag.getInteger(KEY_X_OFFSET), tag.getInteger(KEY_Y_OFFSET), tag.getInteger(KEY_Z_OFFSET));
    }

    private static void loadFilter(TileMachineBase machine, NBTTagCompound tag) {
        MachineFilterState filterState = machine.getFilterState();
        if (tag.hasKey(KEY_ALLOWLIST)) {
            filterState.setAllowList(tag.getBoolean(KEY_ALLOWLIST));
        }
        if (tag.hasKey(KEY_COMPARE_NBT)) {
            filterState.setCompareNbt(tag.getBoolean(KEY_COMPARE_NBT));
        }
        if (tag.hasKey(KEY_BLOCK_ITEM_FILTER)) {
            filterState.setBlockItemFilter(tag.getInteger(KEY_BLOCK_ITEM_FILTER));
        }
        if (machine instanceof TileFilteredMachine && tag.hasKey(KEY_FILTERED_ITEMS, Constants.NBT.TAG_COMPOUND)) {
            ItemStackHandler filterHandler = ((TileFilteredMachine) machine).getFilterHandler();
            if (filterHandler != null) {
                filterHandler.deserializeNBT(tag.getCompoundTag(KEY_FILTERED_ITEMS));
            }
        }
    }

    private static void loadRedstone(MachineRedstoneState redstoneState, NBTTagCompound tag) {
        if (!tag.hasKey(KEY_REDSTONE_MODE)) {
            return;
        }
        int mode = tag.getInteger(KEY_REDSTONE_MODE);
        MachineRedstoneState.RedstoneMode[] values = MachineRedstoneState.RedstoneMode.values();
        if (mode < 0 || mode >= values.length) {
            mode = MachineRedstoneState.RedstoneMode.IGNORED.ordinal();
        }
        redstoneState.setMode(values[mode]);
        redstoneState.setPulsed(tag.getBoolean(KEY_PULSED));
        redstoneState.setReceivingRedstone(tag.getBoolean(KEY_RECEIVING_REDSTONE));
    }

    private static boolean getBooleanSetting(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || stack.getTagCompound() == null) {
            return true;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(TAG_ROOT, Constants.NBT.TAG_COMPOUND)) {
            return true;
        }
        NBTTagCompound root = tag.getCompoundTag(TAG_ROOT);
        return !root.hasKey(key) || root.getBoolean(key);
    }

    private static NBTTagCompound getRootTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        if (!tag.hasKey(TAG_ROOT, Constants.NBT.TAG_COMPOUND)) {
            tag.setTag(TAG_ROOT, new NBTTagCompound());
        }
        return tag.getCompoundTag(TAG_ROOT);
    }
}
