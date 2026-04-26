package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.common.paradox.ParadoxSanitizer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParadoxSanitizerTest {

    @Test
    void restrictiveCopiesOnlyApprovedFields() {
        NBTTagCompound input = new NBTTagCompound();
        input.setString("id", "minecraft:zombie");
        input.setFloat("Health", 12.0F);
        input.setString("CustomName", "Bob");
        input.setTag("Inventory", new NBTTagList());
        input.setInteger("RandomFlag", 4);

        NBTTagCompound output = ParadoxSanitizer.restrictive(input);

        assertEquals("minecraft:zombie", output.getString("id"));
        assertEquals(12.0F, output.getFloat("Health"));
        assertTrue(output.hasKey("CustomName"));
        assertFalse(output.hasKey("Inventory"));
        assertFalse(output.hasKey("RandomFlag"));
    }

    @Test
    void denyInventoryRemovesEquipmentAndStorageFields() {
        NBTTagCompound input = new NBTTagCompound();
        input.setString("id", "minecraft:zombie_horse");
        input.setFloat("Health", 20.0F);
        input.setTag("ArmorItems", new NBTTagList());
        input.setTag("HandItems", new NBTTagList());
        input.setTag("Inventory", new NBTTagList());
        input.setTag("SaddleItem", new NBTTagCompound());

        NBTTagCompound output = ParadoxSanitizer.denyInventory(input);

        assertEquals("minecraft:zombie_horse", output.getString("id"));
        assertEquals(20.0F, output.getFloat("Health"));
        assertFalse(output.hasKey("ArmorItems"));
        assertFalse(output.hasKey("HandItems"));
        assertFalse(output.hasKey("Inventory"));
        assertFalse(output.hasKey("SaddleItem"));
    }
}
