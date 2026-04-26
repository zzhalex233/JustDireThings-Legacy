package com.zzhalex.justdirethings.client.event;

import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientPortalGunInputHandlerTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void leftClickPacketIsOnlySentForClassicPortalGun() {
        assertTrue(ClientPortalGunInputHandler.shouldSendLeftClick(new ItemStack(new ItemPortalGun())));
        assertFalse(ClientPortalGunInputHandler.shouldSendLeftClick(new ItemStack(new ItemPortalGunV2())));
        assertFalse(ClientPortalGunInputHandler.shouldSendLeftClick(ItemStack.EMPTY));
    }
}
