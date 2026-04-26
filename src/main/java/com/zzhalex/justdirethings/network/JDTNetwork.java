package com.zzhalex.justdirethings.network;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.network.message.MessageMachineSetting;
import com.zzhalex.justdirethings.network.message.MessageParadoxState;
import com.zzhalex.justdirethings.network.message.MessagePortalGunLeftClick;
import com.zzhalex.justdirethings.network.message.MessagePortalFavorite;
import com.zzhalex.justdirethings.network.message.MessageSyncToolState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class JDTNetwork {

    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
    private static int nextId;
    private static boolean registered;

    private JDTNetwork() {
    }

    public static void registerMessages() {
        if (registered) {
            return;
        }

        nextId = 0;
        CHANNEL.registerMessage(MessageSyncToolState.Handler.class, MessageSyncToolState.class, nextId++, Side.CLIENT);
        CHANNEL.registerMessage(MessageMachineSetting.Handler.class, MessageMachineSetting.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalFavorite.Handler.class, MessagePortalFavorite.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalGunLeftClick.Handler.class, MessagePortalGunLeftClick.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageParadoxState.Handler.class, MessageParadoxState.class, nextId++, Side.CLIENT);
        registered = true;
    }

    public static SimpleNetworkWrapper getChannel() {
        return CHANNEL;
    }
}
