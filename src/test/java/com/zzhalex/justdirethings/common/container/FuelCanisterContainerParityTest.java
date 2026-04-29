package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.registry.ModContentItems;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuelCanisterContainerParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void fuelCanisterUsesUpstreamStyleItemHandlerSlot() throws Exception {
        String container = Files.readString(Paths.get("src/main/java/com/zzhalex/justdirethings/common/container/ContainerFuelCanister.java"), StandardCharsets.UTF_8);

        assertTrue(container.contains("FuelCanisterHandler"),
                "Fuel Canister should use an item-handler slot like upstream FuelCanisterContainer");
        assertTrue(container.contains("SlotItemHandler"),
                "Fuel Canister input should be a real item-handler slot, not a temporary InventoryBasic slot");
        assertFalse(container.contains("InventoryBasic(\"fuel_canister\""),
                "Fuel Canister should not wait for GUI close to process a fake one-slot inventory");
    }

    @Test
    void handlerImmediatelyConvertsInsertedFuelIntoCanisterFuel() throws Exception {
        ItemStack canister = new ItemStack(new FuelCanisterItem());
        IItemHandler handler = newFuelCanisterHandler(canister);

        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.COAL, 2), false);

        assertTrue(remainder.isEmpty());
        assertTrue(handler.getStackInSlot(0).isEmpty(),
                "Inserted fuel should be consumed immediately by the handler, matching upstream onContentsChanged");
        assertEquals(3200, FuelCanisterItem.getFuelLevel(canister));
    }

    @Test
    void handlerRejectsContainerItemFuelLikeLavaBuckets() throws Exception {
        ItemStack canister = new ItemStack(new FuelCanisterItem());
        IItemHandler handler = newFuelCanisterHandler(canister);

        assertFalse(handler.isItemValid(0, new ItemStack(Items.LAVA_BUCKET)),
                "Original Fuel Canister accepts burnables but excludes fuels with crafting/container remainders");
    }

    @Test
    void justDireCoalItemsUseUpstreamBurnValuesAndSpeedMultiplier() throws Exception {
        ItemStack canister = new ItemStack(new FuelCanisterItem());
        IItemHandler handler = newFuelCanisterHandler(canister);
        Item coalT1 = ModContentItems.getItem("coal_t1");
        assertNotNull(coalT1);

        ItemStack remainder = handler.insertItem(0, new ItemStack(coalT1, 2), false);

        assertTrue(remainder.isEmpty());
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertEquals(9600, FuelCanisterItem.getFuelLevel(canister));
        assertEquals(2, FuelCanisterItem.getBurnSpeedMultiplier(canister));
    }

    @Test
    void justDireCoalBlocksCanFitInUpstreamSizedCanister() throws Exception {
        ItemStack canister = new ItemStack(new FuelCanisterItem());
        IItemHandler handler = newFuelCanisterHandler(canister);
        Item coalBlockT2 = ModContentItems.getItem("coalblock_t2");
        assertNotNull(coalBlockT2);

        ItemStack remainder = handler.insertItem(0, new ItemStack(coalBlockT2), false);

        assertTrue(remainder.isEmpty());
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertEquals(144000, FuelCanisterItem.getFuelLevel(canister));
        assertEquals(4, FuelCanisterItem.getBurnSpeedMultiplier(canister));
    }

    @Test
    void fuelCanisterSurvivesGenericForgeFurnaceContainerRemainderPath() {
        FuelCanisterItem item = new FuelCanisterItem();
        ItemStack canister = new ItemStack(item);
        FuelCanisterItem.setFuelLevel(canister, 1000);
        FuelCanisterItem.setBurnSpeed(canister, 4.0D);

        canister.shrink(1);
        ItemStack remainder = item.getContainerItem(canister);

        assertFalse(remainder.isEmpty(),
                "Forge furnaces shrink fuel before asking for the container item; the canister must still be returned");
        assertEquals(1, remainder.getCount());
        assertEquals(800, FuelCanisterItem.getFuelLevel(remainder));
        assertEquals(4, FuelCanisterItem.getBurnSpeedMultiplier(remainder));
        assertEquals(FuelCanisterItem.MINIMUM_TICKS_CONSUMED, item.getItemBurnTime(remainder));
    }

    private static IItemHandler newFuelCanisterHandler(ItemStack canister) throws Exception {
        Class<?> type = Class.forName("com.zzhalex.justdirethings.common.container.handler.FuelCanisterHandler");
        Constructor<?> constructor = type.getConstructor(int.class, ItemStack.class);
        return (IItemHandler) constructor.newInstance(1, canister);
    }
}
