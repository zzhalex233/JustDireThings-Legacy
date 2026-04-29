package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.common.event.FluidDropEventHandler;
import com.zzhalex.justdirethings.common.event.GooSoilEventHandler;
import com.zzhalex.justdirethings.common.world.PortalChunkKeeper;
import com.zzhalex.justdirethings.registry.ModContainers;
import com.zzhalex.justdirethings.registry.ModEntities;
import com.zzhalex.justdirethings.registry.ModTileEntities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ModTileEntities.register();
        ModContainers.registerGuiHandler();
        MinecraftForge.EVENT_BUS.register(FluidDropEventHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(GooSoilEventHandler.INSTANCE);
        PortalChunkKeeper.initialize();
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
