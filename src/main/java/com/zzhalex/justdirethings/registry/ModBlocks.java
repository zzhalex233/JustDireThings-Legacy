package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.block.BlockUpgradeStation;
import com.zzhalex.justdirethings.common.block.machine.BlockBlockBreaker;
import com.zzhalex.justdirethings.common.block.machine.BlockBlockPlacer;
import com.zzhalex.justdirethings.common.block.machine.BlockBlockSwapper;
import com.zzhalex.justdirethings.common.block.machine.BlockClicker;
import com.zzhalex.justdirethings.common.block.machine.BlockDropper;
import com.zzhalex.justdirethings.common.block.machine.BlockEnergyTransmitter;
import com.zzhalex.justdirethings.common.block.machine.BlockExperienceHolder;
import com.zzhalex.justdirethings.common.block.machine.BlockFluidCollector;
import com.zzhalex.justdirethings.common.block.machine.BlockFluidGenerator;
import com.zzhalex.justdirethings.common.block.machine.BlockFluidPlacer;
import com.zzhalex.justdirethings.common.block.machine.BlockGenerator;
import com.zzhalex.justdirethings.common.block.machine.BlockInventoryHolder;
import com.zzhalex.justdirethings.common.block.machine.BlockItemCollector;
import com.zzhalex.justdirethings.common.block.machine.BlockParadoxMachine;
import com.zzhalex.justdirethings.common.block.machine.BlockPlayerAccessor;
import com.zzhalex.justdirethings.common.block.machine.BlockSensor;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ModBlocks {

    public static final BlockUpgradeStation UPGRADE_STATION = new BlockUpgradeStation();
    public static final BlockInventoryHolder INVENTORY_HOLDER = new BlockInventoryHolder();
    public static final BlockGenerator GENERATOR_T1 = new BlockGenerator("generatort1");
    public static final BlockFluidGenerator GENERATOR_FLUID_T1 = new BlockFluidGenerator("generatorfluidt1");
    public static final BlockItemCollector ITEMCOLLECTOR = new BlockItemCollector("itemcollector");
    public static final BlockBlockBreaker BLOCK_BREAKER_T1 = new BlockBlockBreaker("blockbreakert1");
    public static final BlockBlockBreaker BLOCK_BREAKER_T2 = new BlockBlockBreaker("blockbreakert2");
    public static final BlockBlockPlacer BLOCK_PLACER_T1 = new BlockBlockPlacer("blockplacert1");
    public static final BlockBlockPlacer BLOCK_PLACER_T2 = new BlockBlockPlacer("blockplacert2");
    public static final BlockClicker CLICKER_T1 = new BlockClicker("clickert1");
    public static final BlockClicker CLICKER_T2 = new BlockClicker("clickert2");
    public static final BlockSensor SENSOR_T1 = new BlockSensor("sensort1");
    public static final BlockSensor SENSOR_T2 = new BlockSensor("sensort2");
    public static final BlockDropper DROPPER_T1 = new BlockDropper("droppert1");
    public static final BlockDropper DROPPER_T2 = new BlockDropper("droppert2");
    public static final BlockBlockSwapper BLOCK_SWAPPER_T1 = new BlockBlockSwapper("blockswappert1");
    public static final BlockBlockSwapper BLOCK_SWAPPER_T2 = new BlockBlockSwapper("blockswappert2");
    public static final BlockFluidPlacer FLUID_PLACER_T1 = new BlockFluidPlacer("fluidplacert1");
    public static final BlockFluidPlacer FLUID_PLACER_T2 = new BlockFluidPlacer("fluidplacert2");
    public static final BlockFluidCollector FLUID_COLLECTOR_T1 = new BlockFluidCollector("fluidcollectort1");
    public static final BlockFluidCollector FLUID_COLLECTOR_T2 = new BlockFluidCollector("fluidcollectort2");
    public static final BlockExperienceHolder EXPERIENCEHOLDER = new BlockExperienceHolder("experienceholder");
    public static final BlockEnergyTransmitter ENERGYTRANSMITTER = new BlockEnergyTransmitter("energytransmitter");
    public static final BlockPlayerAccessor PLAYERACCESSOR = new BlockPlayerAccessor("playeraccessor");
    public static final BlockParadoxMachine PARADOX_MACHINE = new BlockParadoxMachine("paradoxmachine");

    private ModBlocks() {
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(UPGRADE_STATION);
        event.getRegistry().register(GENERATOR_T1);
        event.getRegistry().register(GENERATOR_FLUID_T1);
        event.getRegistry().register(ITEMCOLLECTOR);
        event.getRegistry().register(BLOCK_BREAKER_T1);
        event.getRegistry().register(BLOCK_BREAKER_T2);
        event.getRegistry().register(BLOCK_PLACER_T1);
        event.getRegistry().register(BLOCK_PLACER_T2);
        event.getRegistry().register(CLICKER_T1);
        event.getRegistry().register(CLICKER_T2);
        event.getRegistry().register(SENSOR_T1);
        event.getRegistry().register(SENSOR_T2);
        event.getRegistry().register(DROPPER_T1);
        event.getRegistry().register(DROPPER_T2);
        event.getRegistry().register(BLOCK_SWAPPER_T1);
        event.getRegistry().register(BLOCK_SWAPPER_T2);
        event.getRegistry().register(FLUID_PLACER_T1);
        event.getRegistry().register(FLUID_PLACER_T2);
        event.getRegistry().register(FLUID_COLLECTOR_T1);
        event.getRegistry().register(FLUID_COLLECTOR_T2);
        event.getRegistry().register(INVENTORY_HOLDER);
        event.getRegistry().register(EXPERIENCEHOLDER);
        event.getRegistry().register(ENERGYTRANSMITTER);
        event.getRegistry().register(PLAYERACCESSOR);
        event.getRegistry().register(PARADOX_MACHINE);
    }
}
