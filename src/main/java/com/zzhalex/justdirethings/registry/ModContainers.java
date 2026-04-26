package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.client.gui.GuiFuelCanister;
import com.zzhalex.justdirethings.client.gui.GuiPocketGenerator;
import com.zzhalex.justdirethings.client.gui.GuiPotionCanister;
import com.zzhalex.justdirethings.client.gui.GuiUpgradeStation;
import com.zzhalex.justdirethings.client.gui.machine.GuiBlockBreaker;
import com.zzhalex.justdirethings.client.gui.machine.GuiBlockPlacer;
import com.zzhalex.justdirethings.client.gui.machine.GuiBlockSwapper;
import com.zzhalex.justdirethings.client.gui.machine.GuiClicker;
import com.zzhalex.justdirethings.client.gui.machine.GuiDropper;
import com.zzhalex.justdirethings.client.gui.machine.GuiEnergyTransmitter;
import com.zzhalex.justdirethings.client.gui.machine.GuiExperienceHolder;
import com.zzhalex.justdirethings.client.gui.machine.GuiFluidCollector;
import com.zzhalex.justdirethings.client.gui.machine.GuiFluidGenerator;
import com.zzhalex.justdirethings.client.gui.machine.GuiFluidPlacer;
import com.zzhalex.justdirethings.client.gui.machine.GuiGenerator;
import com.zzhalex.justdirethings.client.gui.machine.GuiInventoryHolder;
import com.zzhalex.justdirethings.client.gui.machine.GuiItemCollector;
import com.zzhalex.justdirethings.client.gui.machine.GuiParadoxMachine;
import com.zzhalex.justdirethings.client.gui.machine.GuiPlayerAccessor;
import com.zzhalex.justdirethings.client.gui.machine.GuiSensor;
import com.zzhalex.justdirethings.common.container.ContainerFuelCanister;
import com.zzhalex.justdirethings.common.container.ContainerPocketGenerator;
import com.zzhalex.justdirethings.common.container.ContainerPotionCanister;
import com.zzhalex.justdirethings.common.container.ContainerUpgradeStation;
import com.zzhalex.justdirethings.common.container.machine.ContainerBlockBreaker;
import com.zzhalex.justdirethings.common.container.machine.ContainerBlockPlacer;
import com.zzhalex.justdirethings.common.container.machine.ContainerBlockSwapper;
import com.zzhalex.justdirethings.common.container.machine.ContainerClicker;
import com.zzhalex.justdirethings.common.container.machine.ContainerDropper;
import com.zzhalex.justdirethings.common.container.machine.ContainerEnergyTransmitter;
import com.zzhalex.justdirethings.common.container.machine.ContainerExperienceHolder;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidCollector;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidGenerator;
import com.zzhalex.justdirethings.common.container.machine.ContainerFluidPlacer;
import com.zzhalex.justdirethings.common.container.machine.ContainerGenerator;
import com.zzhalex.justdirethings.common.container.machine.ContainerInventoryHolder;
import com.zzhalex.justdirethings.common.container.machine.ContainerItemCollector;
import com.zzhalex.justdirethings.common.container.machine.ContainerParadoxMachine;
import com.zzhalex.justdirethings.common.container.machine.ContainerPlayerAccessor;
import com.zzhalex.justdirethings.common.container.machine.ContainerSensor;
import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockBreaker;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockPlacer;
import com.zzhalex.justdirethings.common.tile.machine.TileBlockSwapper;
import com.zzhalex.justdirethings.common.tile.machine.TileClicker;
import com.zzhalex.justdirethings.common.tile.machine.TileDropper;
import com.zzhalex.justdirethings.common.tile.machine.TileEnergyTransmitter;
import com.zzhalex.justdirethings.common.tile.machine.TileExperienceHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidCollector;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidGenerator;
import com.zzhalex.justdirethings.common.tile.machine.TileFluidPlacer;
import com.zzhalex.justdirethings.common.tile.machine.TileGenerator;
import com.zzhalex.justdirethings.common.tile.machine.TileInventoryHolder;
import com.zzhalex.justdirethings.common.tile.machine.TileItemCollector;
import com.zzhalex.justdirethings.common.tile.machine.TileParadoxMachine;
import com.zzhalex.justdirethings.common.tile.machine.TilePlayerAccessor;
import com.zzhalex.justdirethings.common.tile.machine.TileSensor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public final class ModContainers {

    public static final int GUI_UPGRADE_STATION = 0;
    public static final int GUI_POCKET_GENERATOR = 1;
    public static final int GUI_GENERATOR = 2;
    public static final int GUI_FLUID_GENERATOR = 3;
    public static final int GUI_ITEM_COLLECTOR = 4;
    public static final int GUI_BLOCK_BREAKER = 5;
    public static final int GUI_BLOCK_PLACER = 6;
    public static final int GUI_CLICKER = 7;
    public static final int GUI_DROPPER = 8;
    public static final int GUI_SENSOR = 9;
    public static final int GUI_BLOCK_SWAPPER = 10;
    public static final int GUI_FLUID_COLLECTOR = 11;
    public static final int GUI_FLUID_PLACER = 12;
    public static final int GUI_INVENTORY_HOLDER = 13;
    public static final int GUI_EXPERIENCE_HOLDER = 14;
    public static final int GUI_ENERGY_TRANSMITTER = 15;
    public static final int GUI_PLAYER_ACCESSOR = 16;
    public static final int GUI_FUEL_CANISTER = 17;
    public static final int GUI_POTION_CANISTER = 18;
    public static final int GUI_PARADOX_MACHINE = 19;
    private static final IGuiHandler GUI_HANDLER = new Handler();

    private ModContainers() {
    }

    public static void registerGuiHandler() {
        NetworkRegistry.INSTANCE.registerGuiHandler(JustDireThingsLegacy.INSTANCE, GUI_HANDLER);
    }

    private static final class Handler implements IGuiHandler {

        @Override
        public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            if (id == GUI_UPGRADE_STATION) {
                if (!(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)) instanceof TileUpgradeStation)) {
                    return null;
                }

                TileUpgradeStation tile = (TileUpgradeStation) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
                return new ContainerUpgradeStation(player.inventory, tile);
            }

            if (id == GUI_POCKET_GENERATOR) {
                ItemStack pocketGenerator = findHeldPocketGenerator(player);
                if (pocketGenerator.isEmpty()) {
                    return null;
                }
                return new ContainerPocketGenerator(player.inventory, pocketGenerator);
            }

            if (id == GUI_FUEL_CANISTER) {
                ItemStack fuelCanister = findHeldItem(player, FuelCanisterItem.class);
                return fuelCanister.isEmpty() ? null : new ContainerFuelCanister(player.inventory, fuelCanister);
            }

            if (id == GUI_POTION_CANISTER) {
                ItemStack potionCanister = findHeldItem(player, PotionCanisterItem.class);
                return potionCanister.isEmpty() ? null : new ContainerPotionCanister(player.inventory, potionCanister);
            }

            if (id == GUI_GENERATOR) {
                TileGenerator tile = getMachineTile(world, x, y, z, TileGenerator.class);
                return tile == null ? null : new ContainerGenerator(player.inventory, tile);
            }

            if (id == GUI_FLUID_GENERATOR) {
                TileFluidGenerator tile = getMachineTile(world, x, y, z, TileFluidGenerator.class);
                return tile == null ? null : new ContainerFluidGenerator(player.inventory, tile);
            }

            if (id == GUI_ITEM_COLLECTOR) {
                TileItemCollector tile = getMachineTile(world, x, y, z, TileItemCollector.class);
                return tile == null ? null : new ContainerItemCollector(player.inventory, tile);
            }

            if (id == GUI_BLOCK_BREAKER) {
                TileBlockBreaker tile = getMachineTile(world, x, y, z, TileBlockBreaker.class);
                return tile == null ? null : new ContainerBlockBreaker(player.inventory, tile);
            }

            if (id == GUI_BLOCK_PLACER) {
                TileBlockPlacer tile = getMachineTile(world, x, y, z, TileBlockPlacer.class);
                return tile == null ? null : new ContainerBlockPlacer(player.inventory, tile);
            }

            if (id == GUI_CLICKER) {
                TileClicker tile = getMachineTile(world, x, y, z, TileClicker.class);
                return tile == null ? null : new ContainerClicker(player.inventory, tile);
            }

            if (id == GUI_DROPPER) {
                TileDropper tile = getMachineTile(world, x, y, z, TileDropper.class);
                return tile == null ? null : new ContainerDropper(player.inventory, tile);
            }

            if (id == GUI_SENSOR) {
                TileSensor tile = getMachineTile(world, x, y, z, TileSensor.class);
                return tile == null ? null : new ContainerSensor(player.inventory, tile);
            }

            if (id == GUI_BLOCK_SWAPPER) {
                TileBlockSwapper tile = getMachineTile(world, x, y, z, TileBlockSwapper.class);
                return tile == null ? null : new ContainerBlockSwapper(player.inventory, tile);
            }

            if (id == GUI_FLUID_COLLECTOR) {
                TileFluidCollector tile = getMachineTile(world, x, y, z, TileFluidCollector.class);
                return tile == null ? null : new ContainerFluidCollector(player.inventory, tile);
            }

            if (id == GUI_FLUID_PLACER) {
                TileFluidPlacer tile = getMachineTile(world, x, y, z, TileFluidPlacer.class);
                return tile == null ? null : new ContainerFluidPlacer(player.inventory, tile);
            }

            if (id == GUI_INVENTORY_HOLDER) {
                TileInventoryHolder tile = getMachineTile(world, x, y, z, TileInventoryHolder.class);
                return tile == null ? null : new ContainerInventoryHolder(player.inventory, tile);
            }

            if (id == GUI_EXPERIENCE_HOLDER) {
                TileExperienceHolder tile = getMachineTile(world, x, y, z, TileExperienceHolder.class);
                return tile == null ? null : new ContainerExperienceHolder(player.inventory, tile);
            }

            if (id == GUI_ENERGY_TRANSMITTER) {
                TileEnergyTransmitter tile = getMachineTile(world, x, y, z, TileEnergyTransmitter.class);
                return tile == null ? null : new ContainerEnergyTransmitter(player.inventory, tile);
            }

            if (id == GUI_PLAYER_ACCESSOR) {
                TilePlayerAccessor tile = getMachineTile(world, x, y, z, TilePlayerAccessor.class);
                return tile == null ? null : new ContainerPlayerAccessor(player.inventory, tile);
            }

            if (id == GUI_PARADOX_MACHINE) {
                TileParadoxMachine tile = getMachineTile(world, x, y, z, TileParadoxMachine.class);
                return tile == null ? null : new ContainerParadoxMachine(player.inventory, tile);
            }

            return null;
        }

        @Override
        public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
            if (id == GUI_UPGRADE_STATION) {
                if (!(world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)) instanceof TileUpgradeStation)) {
                    return null;
                }

                TileUpgradeStation tile = (TileUpgradeStation) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
                return new GuiUpgradeStation(player.inventory, tile);
            }

            if (id == GUI_POCKET_GENERATOR) {
                ItemStack pocketGenerator = findHeldPocketGenerator(player);
                if (pocketGenerator.isEmpty()) {
                    return null;
                }
                return new GuiPocketGenerator(player.inventory, new ContainerPocketGenerator(player.inventory, pocketGenerator));
            }

            if (id == GUI_FUEL_CANISTER) {
                ItemStack fuelCanister = findHeldItem(player, FuelCanisterItem.class);
                return fuelCanister.isEmpty() ? null : new GuiFuelCanister(player.inventory, new ContainerFuelCanister(player.inventory, fuelCanister));
            }

            if (id == GUI_POTION_CANISTER) {
                ItemStack potionCanister = findHeldItem(player, PotionCanisterItem.class);
                return potionCanister.isEmpty() ? null : new GuiPotionCanister(player.inventory, new ContainerPotionCanister(player.inventory, potionCanister));
            }

            if (id == GUI_GENERATOR) {
                TileGenerator tile = getMachineTile(world, x, y, z, TileGenerator.class);
                return tile == null ? null : new GuiGenerator(new ContainerGenerator(player.inventory, tile));
            }

            if (id == GUI_FLUID_GENERATOR) {
                TileFluidGenerator tile = getMachineTile(world, x, y, z, TileFluidGenerator.class);
                return tile == null ? null : new GuiFluidGenerator(new ContainerFluidGenerator(player.inventory, tile));
            }

            if (id == GUI_ITEM_COLLECTOR) {
                TileItemCollector tile = getMachineTile(world, x, y, z, TileItemCollector.class);
                return tile == null ? null : new GuiItemCollector(new ContainerItemCollector(player.inventory, tile));
            }

            if (id == GUI_BLOCK_BREAKER) {
                TileBlockBreaker tile = getMachineTile(world, x, y, z, TileBlockBreaker.class);
                return tile == null ? null : new GuiBlockBreaker(new ContainerBlockBreaker(player.inventory, tile));
            }

            if (id == GUI_BLOCK_PLACER) {
                TileBlockPlacer tile = getMachineTile(world, x, y, z, TileBlockPlacer.class);
                return tile == null ? null : new GuiBlockPlacer(new ContainerBlockPlacer(player.inventory, tile));
            }

            if (id == GUI_CLICKER) {
                TileClicker tile = getMachineTile(world, x, y, z, TileClicker.class);
                return tile == null ? null : new GuiClicker(new ContainerClicker(player.inventory, tile));
            }

            if (id == GUI_DROPPER) {
                TileDropper tile = getMachineTile(world, x, y, z, TileDropper.class);
                return tile == null ? null : new GuiDropper(new ContainerDropper(player.inventory, tile));
            }

            if (id == GUI_SENSOR) {
                TileSensor tile = getMachineTile(world, x, y, z, TileSensor.class);
                return tile == null ? null : new GuiSensor(new ContainerSensor(player.inventory, tile));
            }

            if (id == GUI_BLOCK_SWAPPER) {
                TileBlockSwapper tile = getMachineTile(world, x, y, z, TileBlockSwapper.class);
                return tile == null ? null : new GuiBlockSwapper(new ContainerBlockSwapper(player.inventory, tile));
            }

            if (id == GUI_FLUID_COLLECTOR) {
                TileFluidCollector tile = getMachineTile(world, x, y, z, TileFluidCollector.class);
                return tile == null ? null : new GuiFluidCollector(new ContainerFluidCollector(player.inventory, tile));
            }

            if (id == GUI_FLUID_PLACER) {
                TileFluidPlacer tile = getMachineTile(world, x, y, z, TileFluidPlacer.class);
                return tile == null ? null : new GuiFluidPlacer(new ContainerFluidPlacer(player.inventory, tile));
            }

            if (id == GUI_INVENTORY_HOLDER) {
                TileInventoryHolder tile = getMachineTile(world, x, y, z, TileInventoryHolder.class);
                return tile == null ? null : new GuiInventoryHolder(new ContainerInventoryHolder(player.inventory, tile));
            }

            if (id == GUI_EXPERIENCE_HOLDER) {
                TileExperienceHolder tile = getMachineTile(world, x, y, z, TileExperienceHolder.class);
                return tile == null ? null : new GuiExperienceHolder(new ContainerExperienceHolder(player.inventory, tile));
            }

            if (id == GUI_ENERGY_TRANSMITTER) {
                TileEnergyTransmitter tile = getMachineTile(world, x, y, z, TileEnergyTransmitter.class);
                return tile == null ? null : new GuiEnergyTransmitter(new ContainerEnergyTransmitter(player.inventory, tile));
            }

            if (id == GUI_PLAYER_ACCESSOR) {
                TilePlayerAccessor tile = getMachineTile(world, x, y, z, TilePlayerAccessor.class);
                return tile == null ? null : new GuiPlayerAccessor(new ContainerPlayerAccessor(player.inventory, tile));
            }

            if (id == GUI_PARADOX_MACHINE) {
                TileParadoxMachine tile = getMachineTile(world, x, y, z, TileParadoxMachine.class);
                return tile == null ? null : new GuiParadoxMachine(new ContainerParadoxMachine(player.inventory, tile));
            }

            return null;
        }

        private <T> T getMachineTile(World world, int x, int y, int z, Class<T> tileType) {
            net.minecraft.tileentity.TileEntity tileEntity = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
            return tileType.isInstance(tileEntity) ? tileType.cast(tileEntity) : null;
        }

        private ItemStack findHeldPocketGenerator(EntityPlayer player) {
            return findHeldItem(player, PocketGeneratorItem.class);
        }

        private ItemStack findHeldItem(EntityPlayer player, Class<?> itemType) {
            ItemStack mainHand = player.getHeldItemMainhand();
            if (!mainHand.isEmpty() && itemType.isInstance(mainHand.getItem())) {
                return mainHand;
            }

            ItemStack offHand = player.getHeldItemOffhand();
            if (!offHand.isEmpty() && itemType.isInstance(offHand.getItem())) {
                return offHand;
            }

            return ItemStack.EMPTY;
        }
    }
}
