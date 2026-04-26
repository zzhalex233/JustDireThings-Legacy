package com.zzhalex.justdirethings.compat.accessory;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessoryInventoryBridgeTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void emptyAccessoryBridgeCopiesNoStacks() {
        assertTrue(AccessoryInventoryBridge.empty().copyStacks().isEmpty());
    }

    @Test
    void itemHandlerBridgeExposesSlotsAndCopiesStacks() {
        ItemStackHandler handler = new ItemStackHandler(3);
        handler.setStackInSlot(0, new ItemStack(Items.DIAMOND));
        handler.setStackInSlot(2, new ItemStack(Items.REDSTONE, 4));

        AccessoryInventoryBridge bridge = AccessoryInventoryBridge.fromItemHandler(handler);

        assertEquals(3, bridge.getSlotCount());
        assertEquals(Items.DIAMOND, bridge.getStackInSlot(0).getItem());
        assertTrue(bridge.getStackInSlot(1).isEmpty());
        assertEquals(2, bridge.copyStacks().size());
    }
}
