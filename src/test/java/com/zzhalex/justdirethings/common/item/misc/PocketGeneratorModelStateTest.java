package com.zzhalex.justdirethings.common.item.misc;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PocketGeneratorModelStateTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void activeModelStateRequiresEnabledPowerSource() {
        PocketGeneratorItem item = new PocketGeneratorItem();
        ItemStack stack = new ItemStack(item);

        assertEquals(0, PocketGeneratorItem.getEnabledModelState(stack));

        item.setStoredEnergy(stack, 50);
        assertEquals(1, PocketGeneratorItem.getEnabledModelState(stack));

        item.setEnabled(stack, false);
        assertEquals(0, PocketGeneratorItem.getEnabledModelState(stack));

        item.setEnabled(stack, true);
        item.setStoredEnergy(stack, 0);
        stack.getTagCompound().setInteger("PocketGeneratorCounter", 4);
        assertEquals(1, PocketGeneratorItem.getEnabledModelState(stack));
    }
}
