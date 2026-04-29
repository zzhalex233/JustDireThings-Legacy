package com.zzhalex.justdirethings.registry;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModTileEntities {

    private ModTileEntities() {
    }

    public static void register() {
        GameRegistry.registerTileEntity(TileUpgradeStation.class, new ResourceLocation(Reference.MOD_ID, "upgrade_station"));
        GameRegistry.registerTileEntity(TileGooBlock.Tier1.class, new ResourceLocation(Reference.MOD_ID, "gooblock_tier1"));
        GameRegistry.registerTileEntity(TileGooBlock.Tier2.class, new ResourceLocation(Reference.MOD_ID, "gooblock_tier2"));
        GameRegistry.registerTileEntity(TileGooBlock.Tier3.class, new ResourceLocation(Reference.MOD_ID, "gooblock_tier3"));
        GameRegistry.registerTileEntity(TileGooBlock.Tier4.class, new ResourceLocation(Reference.MOD_ID, "gooblock_tier4"));
        GameRegistry.registerTileEntity(TileGenerator.class, new ResourceLocation(Reference.MOD_ID, "generatort1"));
        GameRegistry.registerTileEntity(TileFluidGenerator.class, new ResourceLocation(Reference.MOD_ID, "generatorfluidt1"));
        GameRegistry.registerTileEntity(TileItemCollector.class, new ResourceLocation(Reference.MOD_ID, "itemcollectorbe"));
        GameRegistry.registerTileEntity(TileBlockBreaker.T1.class, new ResourceLocation(Reference.MOD_ID, "blockbreakert1"));
        GameRegistry.registerTileEntity(TileBlockBreaker.T2.class, new ResourceLocation(Reference.MOD_ID, "blockbreakert2"));
        GameRegistry.registerTileEntity(TileBlockPlacer.T1.class, new ResourceLocation(Reference.MOD_ID, "blockplacert1"));
        GameRegistry.registerTileEntity(TileBlockPlacer.T2.class, new ResourceLocation(Reference.MOD_ID, "blockplacert2"));
        GameRegistry.registerTileEntity(TileClicker.T1.class, new ResourceLocation(Reference.MOD_ID, "clickert1"));
        GameRegistry.registerTileEntity(TileClicker.T2.class, new ResourceLocation(Reference.MOD_ID, "clickert2"));
        GameRegistry.registerTileEntity(TileSensor.T1.class, new ResourceLocation(Reference.MOD_ID, "sensort1be"));
        GameRegistry.registerTileEntity(TileSensor.T2.class, new ResourceLocation(Reference.MOD_ID, "sensort2be"));
        GameRegistry.registerTileEntity(TileDropper.T1.class, new ResourceLocation(Reference.MOD_ID, "droppert1"));
        GameRegistry.registerTileEntity(TileDropper.T2.class, new ResourceLocation(Reference.MOD_ID, "droppert2"));
        GameRegistry.registerTileEntity(TileBlockSwapper.T1.class, new ResourceLocation(Reference.MOD_ID, "blockswappert1"));
        GameRegistry.registerTileEntity(TileBlockSwapper.T2.class, new ResourceLocation(Reference.MOD_ID, "blockswappert2"));
        GameRegistry.registerTileEntity(TileFluidPlacer.T1.class, new ResourceLocation(Reference.MOD_ID, "fluidplacert1"));
        GameRegistry.registerTileEntity(TileFluidPlacer.T2.class, new ResourceLocation(Reference.MOD_ID, "fluidplacert2"));
        GameRegistry.registerTileEntity(TileFluidCollector.T1.class, new ResourceLocation(Reference.MOD_ID, "fluidcollectort1"));
        GameRegistry.registerTileEntity(TileFluidCollector.T2.class, new ResourceLocation(Reference.MOD_ID, "fluidcollectort2"));
        GameRegistry.registerTileEntity(TileInventoryHolder.class, new ResourceLocation(Reference.MOD_ID, "inventory_holder"));
        GameRegistry.registerTileEntity(TileExperienceHolder.class, new ResourceLocation(Reference.MOD_ID, "experienceholder"));
        GameRegistry.registerTileEntity(TileEnergyTransmitter.class, new ResourceLocation(Reference.MOD_ID, "energytransmitter"));
        GameRegistry.registerTileEntity(TilePlayerAccessor.class, new ResourceLocation(Reference.MOD_ID, "playeraccessorbe"));
        GameRegistry.registerTileEntity(TileParadoxMachine.class, new ResourceLocation(Reference.MOD_ID, "paradoxmachine"));
    }
}
