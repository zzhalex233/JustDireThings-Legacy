package com.zzhalex.justdirethings.capability;

import com.zzhalex.justdirethings.capability.inventory.InternalItemHandler;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InternalItemHandlerTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void handlerPersistsSingleSlotItem() {
        InternalItemHandler original = new InternalItemHandler(1);
        original.setStackInSlot(0, new ItemStack(Items.COAL));

        NBTTagCompound tag = original.serializeNBT();
        InternalItemHandler restored = new InternalItemHandler(1);
        restored.deserializeNBT(tag);

        assertFalse(restored.getStackInSlot(0).isEmpty());
        assertEquals(Items.COAL, restored.getStackInSlot(0).getItem());
    }
}
