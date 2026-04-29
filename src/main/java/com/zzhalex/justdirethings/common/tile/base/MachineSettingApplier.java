package com.zzhalex.justdirethings.common.tile.base;

import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockBreaker;
import com.zzhalex.justdirethings.common.tile.machine.TileClicker;
import com.zzhalex.justdirethings.common.tile.machine.TileDropper;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public final class MachineSettingApplier {

    private MachineSettingApplier() {
    }

    public static boolean apply(TileMachineBase machine, String key, int value) {
        return apply(machine, key, value, null);
    }

    public static boolean apply(TileMachineBase machine, String key, int value, EntityPlayer player) {
        if (machine == null || key == null) {
            return false;
        }

        switch (key) {
            case MachineSettingKeys.TICK_SPEED:
                machine.setTickSpeed(value);
                return true;
            case MachineSettingKeys.REDSTONE_MODE:
                machine.getRedstoneState().setMode(redstoneMode(value));
                return true;
            case MachineSettingKeys.DIRECTION:
                machine.setDirection(value);
                return true;
            case MachineSettingKeys.RENDER_AREA:
                machine.getAreaState().setRenderArea(value != 0);
                return true;
            case MachineSettingKeys.X_RADIUS_TENTHS:
                machine.getAreaState().setArea(value / 10.0D, machine.getAreaState().getYRadius(), machine.getAreaState().getZRadius());
                syncBlockSwapperPartnerArea(machine);
                return true;
            case MachineSettingKeys.Y_RADIUS_TENTHS:
                machine.getAreaState().setArea(machine.getAreaState().getXRadius(), value / 10.0D, machine.getAreaState().getZRadius());
                syncBlockSwapperPartnerArea(machine);
                return true;
            case MachineSettingKeys.Z_RADIUS_TENTHS:
                machine.getAreaState().setArea(machine.getAreaState().getXRadius(), machine.getAreaState().getYRadius(), value / 10.0D);
                syncBlockSwapperPartnerArea(machine);
                return true;
            case MachineSettingKeys.X_OFFSET:
                machine.getAreaState().setOffset(value, machine.getAreaState().getYOffset(), machine.getAreaState().getZOffset());
                return true;
            case MachineSettingKeys.Y_OFFSET:
                machine.getAreaState().setOffset(machine.getAreaState().getXOffset(), value, machine.getAreaState().getZOffset());
                return true;
            case MachineSettingKeys.Z_OFFSET:
                machine.getAreaState().setOffset(machine.getAreaState().getXOffset(), machine.getAreaState().getYOffset(), value);
                return true;
            case MachineSettingKeys.FILTER_ALLOWLIST:
                machine.getFilterState().setAllowList(value != 0);
                return true;
            case MachineSettingKeys.FILTER_COMPARE_NBT:
                machine.getFilterState().setCompareNbt(value != 0);
                return true;
            case MachineSettingKeys.FILTER_BLOCK_ITEM:
                machine.getFilterState().setBlockItemFilter(value);
                return true;
            case MachineSettingKeys.RESPECT_PICKUP_DELAY:
                if (machine instanceof TileItemCollector) {
                    ((TileItemCollector) machine).setRespectPickupDelay(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.SWAP_BLOCKS:
                if (machine instanceof TileBlockSwapper) {
                    ((TileBlockSwapper) machine).setSwapBlocks(value == 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.SWAP_ENTITY_TYPE:
                if (machine instanceof TileBlockSwapper) {
                    ((TileBlockSwapper) machine).setSwapEntityType(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.STORE_EXPERIENCE:
                if (machine instanceof TileExperienceHolder && player != null) {
                    ((TileExperienceHolder) machine).storeExperience(player, value <= 0 ? 1 : value);
                    return true;
                }
                return false;
            case MachineSettingKeys.EXTRACT_EXPERIENCE:
                if (machine instanceof TileExperienceHolder && player != null) {
                    ((TileExperienceHolder) machine).extractExperience(player, value <= 0 ? 1 : value);
                    return true;
                }
                return false;
            case MachineSettingKeys.TARGET_EXPERIENCE:
                if (machine instanceof TileExperienceHolder) {
                    ((TileExperienceHolder) machine).setTargetExperience(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.OWNER_ONLY:
                if (machine instanceof TileExperienceHolder) {
                    ((TileExperienceHolder) machine).setOwnerOnly(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.COLLECT_EXPERIENCE:
                if (machine instanceof TileExperienceHolder) {
                    ((TileExperienceHolder) machine).setCollectExperience(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.SHOW_PARTICLES:
                if (machine instanceof TileExperienceHolder) {
                    ((TileExperienceHolder) machine).setShowParticles(value != 0);
                    return true;
                }
                if (machine instanceof TileEnergyTransmitter) {
                    ((TileEnergyTransmitter) machine).setShowParticles(value != 0);
                    return true;
                }
                if (machine instanceof TileItemCollector) {
                    ((TileItemCollector) machine).setShowParticles(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.FILTER_ONLY:
                if (machine instanceof TileInventoryHolder) {
                    ((TileInventoryHolder) machine).setFiltersOnly(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.COMPARE_COUNTS:
                if (machine instanceof TileInventoryHolder) {
                    ((TileInventoryHolder) machine).setCompareCounts(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.AUTOMATED_FILTER_ONLY:
                if (machine instanceof TileInventoryHolder) {
                    ((TileInventoryHolder) machine).setAutomatedFiltersOnly(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.AUTOMATED_COMPARE_COUNTS:
                if (machine instanceof TileInventoryHolder) {
                    ((TileInventoryHolder) machine).setAutomatedCompareCounts(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.RENDER_PLAYER:
            case MachineSettingKeys.SHOW_FAKE_PLAYER:
                if (machine instanceof TileClicker) {
                    ((TileClicker) machine).setShowFakePlayer(value != 0);
                    return true;
                }
                if (machine instanceof TileInventoryHolder) {
                    ((TileInventoryHolder) machine).setRenderPlayer(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.SNEAKING:
                if (machine instanceof TileClicker) {
                    ((TileClicker) machine).setSneaking(value != 0);
                    return true;
                }
                if (machine instanceof TileBlockBreaker) {
                    ((TileBlockBreaker) machine).setSneaking(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.CLICK_TARGET:
                if (machine instanceof TileClicker) {
                    ((TileClicker) machine).setClickTarget(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.CLICK_TYPE:
                if (machine instanceof TileClicker) {
                    ((TileClicker) machine).setClickType(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.PICKUP_DELAY:
                if (machine instanceof TileDropper) {
                    ((TileDropper) machine).setPickupDelay(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.DROP_COUNT:
                if (machine instanceof TileDropper) {
                    ((TileDropper) machine).setDropCount(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.SEND_INVENTORY:
                if (machine instanceof TileInventoryHolder && player != null) {
                    ((TileInventoryHolder) machine).copyFromPlayer(player);
                    return true;
                }
                return false;
            case MachineSettingKeys.PULL_INVENTORY:
                if (machine instanceof TileInventoryHolder && player != null) {
                    ((TileInventoryHolder) machine).copyToPlayer(player);
                    return true;
                }
                return false;
            case MachineSettingKeys.SWAP_INVENTORY:
                if (machine instanceof TileInventoryHolder && player != null) {
                    ((TileInventoryHolder) machine).swapWithPlayer(player);
                    return true;
                }
                return false;
            case MachineSettingKeys.INVENTORY_CONNECTION_UP:
                return setPlayerAccessorSide(machine, EnumFacing.UP, value);
            case MachineSettingKeys.INVENTORY_CONNECTION_DOWN:
                return setPlayerAccessorSide(machine, EnumFacing.DOWN, value);
            case MachineSettingKeys.INVENTORY_CONNECTION_NORTH:
                return setPlayerAccessorSide(machine, EnumFacing.NORTH, value);
            case MachineSettingKeys.INVENTORY_CONNECTION_SOUTH:
                return setPlayerAccessorSide(machine, EnumFacing.SOUTH, value);
            case MachineSettingKeys.INVENTORY_CONNECTION_WEST:
                return setPlayerAccessorSide(machine, EnumFacing.WEST, value);
            case MachineSettingKeys.INVENTORY_CONNECTION_EAST:
                return setPlayerAccessorSide(machine, EnumFacing.EAST, value);
            case MachineSettingKeys.SENSOR_TARGET:
                if (machine instanceof TileSensor) {
                    ((TileSensor) machine).setSenseTarget(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.STRONG_WEAK_REDSTONE:
                if (machine instanceof TileSensor) {
                    ((TileSensor) machine).setStrongSignal(value != 0);
                    return true;
                }
                return false;
            case MachineSettingKeys.SENSE_AMOUNT:
                if (machine instanceof TileSensor) {
                    ((TileSensor) machine).setSenseAmount(value);
                    return true;
                }
                return false;
            case MachineSettingKeys.EQUALITY:
                if (machine instanceof TileSensor) {
                    ((TileSensor) machine).setEquality(value);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean setPlayerAccessorSide(TileMachineBase machine, EnumFacing side, int value) {
        if (machine instanceof TilePlayerAccessor) {
            ((TilePlayerAccessor) machine).setInventoryConnectionType(side, value);
            return true;
        }
        return false;
    }

    private static void syncBlockSwapperPartnerArea(TileMachineBase machine) {
        if (machine instanceof TileBlockSwapper.T2) {
            ((TileBlockSwapper.T2) machine).updatePartnerArea();
        }
    }

    private static MachineRedstoneState.RedstoneMode redstoneMode(int ordinal) {
        MachineRedstoneState.RedstoneMode[] values = MachineRedstoneState.RedstoneMode.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return MachineRedstoneState.RedstoneMode.IGNORED;
        }
        return values[ordinal];
    }
}
