package com.zzhalex.justdirethings.common.item.misc;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTotemOfDeathRecallTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void unboundTotemIsNotFoilAndHasNoLocation() {
        ItemTotemOfDeathRecall item = new ItemTotemOfDeathRecall();
        ItemStack stack = new ItemStack(item);

        assertFalse(ItemTotemOfDeathRecall.hasBoundLocation(stack));
        assertFalse(item.hasEffect(stack));
    }

    @Test
    void boundLocationPersistsInStackNbt() {
        ItemTotemOfDeathRecall item = new ItemTotemOfDeathRecall();
        ItemStack stack = new ItemStack(item);

        ItemTotemOfDeathRecall.setBoundLocation(stack, -1, 12.5D, 64.0D, -30.25D);
        ItemTotemOfDeathRecall.BoundLocation bound = ItemTotemOfDeathRecall.getBoundLocation(stack);

        assertTrue(ItemTotemOfDeathRecall.hasBoundLocation(stack));
        assertTrue(item.hasEffect(stack));
        assertEquals(-1, bound.getDimension());
        assertEquals(12.5D, bound.getPosition().x, 0.001D);
        assertEquals(64.0D, bound.getPosition().y, 0.001D);
        assertEquals(-30.25D, bound.getPosition().z, 0.001D);
    }

    @Test
    void boundTotemUsesBowAnimationLikeUpstream() {
        ItemTotemOfDeathRecall item = new ItemTotemOfDeathRecall();

        assertEquals(1, item.getItemStackLimit());
        assertEquals(72000, item.getMaxItemUseDuration(ItemStack.EMPTY));
        assertEquals(EnumAction.BOW, item.getItemUseAction(ItemStack.EMPTY));
    }
}
