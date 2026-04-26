package com.zzhalex.justdirethings.common.item.misc;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCreatureCatcherTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void capturedEntityMarkerPersistsOnStack() {
        ItemStack stack = new ItemStack(new ItemCreatureCatcher());
        NBTTagCompound entityData = new NBTTagCompound();
        entityData.setString("CustomName", "Baa");

        assertFalse(ItemCreatureCatcher.hasEntity(stack));

        ItemCreatureCatcher.setCapturedEntity(stack, "minecraft:sheep", entityData);

        assertTrue(ItemCreatureCatcher.hasEntity(stack));
        assertEquals("minecraft:sheep", ItemCreatureCatcher.getCapturedEntityId(stack));
        assertEquals("Baa", stack.getTagCompound()
                .getCompoundTag(ItemCreatureCatcher.TAG_CAPTURED_ENTITY_DATA)
                .getString("CustomName"));
    }

    @Test
    void capturedEntityStackFactoryCopiesEntityData() {
        NBTTagCompound entityData = new NBTTagCompound();
        entityData.setString("id", "minecraft:sheep");
        entityData.setString("CustomName", "Baa");

        ItemStack stack = ItemCreatureCatcher.createCapturedStack("minecraft:sheep", entityData);
        entityData.setString("CustomName", "Changed");

        assertTrue(ItemCreatureCatcher.hasEntity(stack));
        assertEquals("minecraft:sheep", ItemCreatureCatcher.getCapturedEntityId(stack));
        assertEquals("Baa", ItemCreatureCatcher.getCapturedEntityData(stack).getString("CustomName"));

        NBTTagCompound copiedData = ItemCreatureCatcher.getCapturedEntityData(stack);
        copiedData.setString("CustomName", "Changed Again");

        assertEquals("Baa", ItemCreatureCatcher.getCapturedEntityData(stack).getString("CustomName"));
    }
}
