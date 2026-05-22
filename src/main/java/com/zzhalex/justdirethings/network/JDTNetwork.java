package com.zzhalex.justdirethings.network;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.network.message.MessageExecuteAbility;
import com.zzhalex.justdirethings.network.message.MessageItemFlowParticle;
import com.zzhalex.justdirethings.network.message.MessageSyncAbilityCooldowns;
import com.zzhalex.justdirethings.network.message.MessageBlockStateFilter;
import com.zzhalex.justdirethings.network.message.MessageGhostSlot;
import com.zzhalex.justdirethings.network.message.MessageMachineSetting;
import com.zzhalex.justdirethings.network.message.MessageParadoxState;
import com.zzhalex.justdirethings.network.message.MessagePortalGunLeftClick;
import com.zzhalex.justdirethings.network.message.MessagePortalFavorite;
import com.zzhalex.justdirethings.network.message.MessagePortalGunFavorite;
import com.zzhalex.justdirethings.network.message.MessagePortalGunFavoriteChange;
import com.zzhalex.justdirethings.network.message.MessageStartElytraFlight;
import com.zzhalex.justdirethings.network.message.MessageSyncToolState;
import com.zzhalex.justdirethings.network.message.MessageToolBindingSetting;
import com.zzhalex.justdirethings.network.message.MessageToolRefreshSlots;
import com.zzhalex.justdirethings.network.message.MessageToolSlotSetting;
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
        CHANNEL.registerMessage(MessageBlockStateFilter.Handler.class, MessageBlockStateFilter.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageGhostSlot.Handler.class, MessageGhostSlot.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalFavorite.Handler.class, MessagePortalFavorite.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalGunFavorite.Handler.class, MessagePortalGunFavorite.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalGunFavoriteChange.Handler.class, MessagePortalGunFavoriteChange.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessagePortalGunLeftClick.Handler.class, MessagePortalGunLeftClick.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageParadoxState.Handler.class, MessageParadoxState.class, nextId++, Side.CLIENT);
        CHANNEL.registerMessage(MessageToolSlotSetting.Handler.class, MessageToolSlotSetting.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageToolBindingSetting.Handler.class, MessageToolBindingSetting.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageToolRefreshSlots.Handler.class, MessageToolRefreshSlots.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageExecuteAbility.Handler.class, MessageExecuteAbility.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageStartElytraFlight.Handler.class, MessageStartElytraFlight.class, nextId++, Side.SERVER);
        CHANNEL.registerMessage(MessageSyncAbilityCooldowns.Handler.class, MessageSyncAbilityCooldowns.class, nextId++, Side.CLIENT);
        CHANNEL.registerMessage(MessageItemFlowParticle.Handler.class, MessageItemFlowParticle.class, nextId++, Side.CLIENT);
        registered = true;
    }

    public static SimpleNetworkWrapper getChannel() {
        return CHANNEL;
    }
}
