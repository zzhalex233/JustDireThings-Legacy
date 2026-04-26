package com.zzhalex.justdirethings.common.item.tool;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPortalGunV2Test {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void fullnessTracksStoredFluidByThreshold() {
        ItemPortalGunV2 item = new ItemPortalGunV2();
        ItemStack stack = new ItemStack(item);

        assertEquals(0, ItemPortalGunV2.getFullness(stack));

        item.setStoredFluid(stack, 1);
        assertEquals(1, ItemPortalGunV2.getFullness(stack));

        item.setStoredFluid(stack, 3000);
        assertEquals(2, ItemPortalGunV2.getFullness(stack));

        item.setStoredFluid(stack, item.getFluidCapacity());
        assertEquals(3, ItemPortalGunV2.getFullness(stack));
    }
}
