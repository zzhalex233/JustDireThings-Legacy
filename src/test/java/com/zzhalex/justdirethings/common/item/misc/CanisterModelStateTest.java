package com.zzhalex.justdirethings.common.item.misc;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanisterModelStateTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void fluidCanisterFullnessTracksBuckets() {
        FluidCanisterItem item = new FluidCanisterItem();
        ItemStack stack = new ItemStack(item);

        assertEquals(0, FluidCanisterItem.getFullness(stack));

        FluidCanisterItem.setFluidAmount(stack, 1);
        assertEquals(1, FluidCanisterItem.getFullness(stack));

        FluidCanisterItem.setFluidAmount(stack, 1001);
        assertEquals(2, FluidCanisterItem.getFullness(stack));

        FluidCanisterItem.setFluidAmount(stack, FluidCanisterItem.MAX_MB - 1);
        assertEquals(8, FluidCanisterItem.getFullness(stack));
    }

    @Test
    void potionCanisterFullnessTracksQuarterSteps() {
        PotionCanisterItem item = new PotionCanisterItem();
        ItemStack stack = new ItemStack(item);

        assertEquals(0, PotionCanisterItem.getFullness(stack));

        PotionCanisterItem.setPotionAmount(stack, 250);
        assertEquals(1, PotionCanisterItem.getFullness(stack));

        PotionCanisterItem.setPotionAmount(stack, 251);
        assertEquals(2, PotionCanisterItem.getFullness(stack));

        PotionCanisterItem.setPotionAmount(stack, 501);
        assertEquals(3, PotionCanisterItem.getFullness(stack));

        PotionCanisterItem.setPotionAmount(stack, PotionCanisterItem.MAX_MB);
        assertEquals(4, PotionCanisterItem.getFullness(stack));
    }
}
