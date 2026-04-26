package com.zzhalex.justdirethings;

import com.zzhalex.justdirethings.client.ClientRegistration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistration.initialize();
    }
}
