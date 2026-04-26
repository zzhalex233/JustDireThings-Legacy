package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.common.world.PortalChunkKeeper;
import com.zzhalex.justdirethings.registry.ModContainers;
import com.zzhalex.justdirethings.registry.ModEntities;
import com.zzhalex.justdirethings.registry.ModTileEntities;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ModTileEntities.register();
        ModContainers.registerGuiHandler();
        PortalChunkKeeper.initialize();
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
