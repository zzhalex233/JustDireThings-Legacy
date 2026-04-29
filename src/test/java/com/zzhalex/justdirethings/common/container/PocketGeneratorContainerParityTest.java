package com.zzhalex.justdirethings.common.container;

import com.zzhalex.justdirethings.common.item.misc.FuelCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class PocketGeneratorContainerParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void pocketGeneratorFuelSlotIsBackedDirectlyByTheGeneratorStack() throws Exception {
        String container = Files.readString(Paths.get("src/main/java/com/zzhalex/justdirethings/common/container/ContainerPocketGenerator.java"), StandardCharsets.UTF_8);

        assertTrue(container.contains("PocketGeneratorFuelHandler"),
                "Pocket Generator should use a stack-backed item handler like upstream ComponentItemHandler");
        assertTrue(container.contains("SlotItemHandler"),
                "Pocket Generator fuel input should be a live item-handler slot");
        assertFalse(container.contains("InventoryBasic(\"pocket_generator\""),
                "Pocket Generator should not wait for GUI close before writing fuel into the item NBT");
    }

    @Test
    void pocketGeneratorContainerSynchronizesRuntimeFieldsToClientGui() throws Exception {
        String container = Files.readString(Paths.get("src/main/java/com/zzhalex/justdirethings/common/container/ContainerPocketGenerator.java"), StandardCharsets.UTF_8);

        assertTrue(container.contains("detectAndSendChanges"),
                "Pocket Generator GUI must sync live burn and FE fields instead of reading stale client stack NBT");
        assertTrue(container.contains("updateProgressBar"),
                "Pocket Generator GUI must receive 1.12 container field updates on the client");
        assertTrue(container.contains("FIELD_ENERGY"), "Pocket Generator GUI should sync stored FE");
        assertTrue(container.contains("FIELD_COUNTER"), "Pocket Generator GUI should sync remaining burn ticks");
        assertTrue(container.contains("FIELD_MAX_BURN"), "Pocket Generator GUI should sync the burn duration");
        assertTrue(container.contains("FIELD_FUEL_MULTIPLIER"), "Pocket Generator GUI should sync the fuel multiplier");
    }

    @Test
    void pocketGeneratorFuelHandlerWritesInsertedFuelToItemStackImmediately() throws Exception {
        PocketGeneratorItem pocketGeneratorItem = new PocketGeneratorItem();
        ItemStack pocketGenerator = new ItemStack(pocketGeneratorItem);
        IItemHandler handler = newPocketGeneratorFuelHandler(pocketGenerator, pocketGeneratorItem);

        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.COAL), false);

        assertTrue(remainder.isEmpty());
        ItemStack storedFuel = pocketGeneratorItem.getFuelStack(pocketGenerator);
        assertFalse(storedFuel.isEmpty());
        assertEquals(Items.COAL, storedFuel.getItem());
        assertEquals(1, storedFuel.getCount());
    }

    @Test
    void pocketGeneratorStillUsesContainerItemAndBurnMultiplierPathForFuelCanisters() throws Exception {
        PocketGeneratorItem pocketGeneratorItem = new PocketGeneratorItem();
        ItemStack canister = new ItemStack(new FuelCanisterItem());
        FuelCanisterItem.setFuelLevel(canister, 1000);
        FuelCanisterItem.setBurnSpeed(canister, 3.0D);

        assertEquals(3, com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper.getBurnSpeedMultiplier(canister));
        assertEquals(FuelCanisterItem.MINIMUM_TICKS_CONSUMED, com.zzhalex.justdirethings.common.item.fuel.FuelBurnHelper.getBurnTime(canister));
        assertEquals(800, FuelCanisterItem.getFuelLevel(canister.getItem().getContainerItem(canister)));

        String itemSource = Files.readString(Paths.get("src/main/java/com/zzhalex/justdirethings/common/item/misc/PocketGeneratorItem.java"), StandardCharsets.UTF_8);
        assertTrue(itemSource.contains("FuelBurnHelper.getBurnSpeedMultiplier(fuelStack)"),
                "Pocket Generator should use the shared burn multiplier path for Fuel Canister stacks");
        assertTrue(itemSource.contains("getContainerItem(fuelStack)"),
                "Pocket Generator should consume Fuel Canister stacks through the generic container-item fuel path");
    }

    private static IItemHandler newPocketGeneratorFuelHandler(ItemStack generatorStack, PocketGeneratorItem item) throws Exception {
        Class<?> type = Class.forName("com.zzhalex.justdirethings.common.container.handler.PocketGeneratorFuelHandler");
        Constructor<?> constructor = type.getConstructor(ItemStack.class, PocketGeneratorItem.class);
        return (IItemHandler) constructor.newInstance(generatorStack, item);
    }
}
