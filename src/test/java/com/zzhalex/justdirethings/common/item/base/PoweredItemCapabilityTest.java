package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.TestForgeCapabilities;
import com.zzhalex.justdirethings.common.item.misc.FluidCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTAxe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTArmor;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTBow;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTHoe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTPaxel;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTPickaxe;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTShovel;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTSword;
import com.zzhalex.justdirethings.common.item.misc.PotionCanisterItem;
import com.zzhalex.justdirethings.common.item.material.JDTArmorMaterial;
import com.zzhalex.justdirethings.common.item.material.JDTToolTier;
import com.zzhalex.justdirethings.common.item.tool.ItemEclipsegateWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.item.tool.ItemTimeWand;
import com.zzhalex.justdirethings.common.item.tool.ItemVoidshiftWand;
import com.zzhalex.justdirethings.registry.ModFluids;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoweredItemCapabilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
        TestForgeCapabilities.registerStandardCapabilities();
    }

    @Test
    void portalGunAndTimeWandExposeEnergyAndFluidCapabilities() {
        ItemStack portalGun = new ItemStack(new ItemPortalGunV2());
        ItemStack classicPortalGun = new ItemStack(new ItemPortalGun());
        ItemStack timeWand = new ItemStack(new ItemTimeWand());
        ItemStack voidshiftWand = new ItemStack(new ItemVoidshiftWand());
        ItemStack eclipsegateWand = new ItemStack(new ItemEclipsegateWand());
        ItemStack polymorphicWand = new ItemStack(new ItemPolymorphicWand());

        assertTrue(portalGun.hasCapability(CapabilityEnergy.ENERGY, null), "Portal Gun V2 should expose FE storage");
        assertTrue(portalGun.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null),
                "Portal Gun V2 should expose portal fluid storage");
        assertTrue(classicPortalGun.hasCapability(CapabilityEnergy.ENERGY, null),
                "Classic Portal Gun should expose FE storage");

        assertTrue(timeWand.hasCapability(CapabilityEnergy.ENERGY, null), "Time Wand should expose FE storage");
        assertTrue(timeWand.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null),
                "Time Wand should expose time fluid storage");

        assertTrue(voidshiftWand.hasCapability(CapabilityEnergy.ENERGY, null),
                "Voidshift Wand should expose FE storage");
        assertTrue(eclipsegateWand.hasCapability(CapabilityEnergy.ENERGY, null),
                "Eclipsegate Wand should expose FE storage");
        assertTrue(polymorphicWand.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null),
                "Polymorphic Wand should expose polymorphic fluid storage");
    }

    @Test
    void pocketGeneratorAndFluidCanisterExposeChargeAndFluidHandlers() {
        ItemStack pocketGenerator = new ItemStack(new PocketGeneratorItem());
        ItemStack fluidCanister = new ItemStack(new FluidCanisterItem());

        assertTrue(pocketGenerator.hasCapability(CapabilityEnergy.ENERGY, null), "Pocket Generator should expose FE storage");
        assertTrue(fluidCanister.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null),
                "Fluid Canister should expose a fluid handler");
    }

    @Test
    void celestigemEquipmentExposesOriginalModEnergyCapability() {
        for (ItemStack stack : createToolSet("celestigem", JDTToolTier.CELESTIGEM, JDTArmorMaterial.CELESTIGEM, 450)) {
            assertEnergyCapability(stack, 10_000);
        }
    }

    @Test
    void eclipseAlloyEquipmentExposesOriginalModEnergyCapability() {
        for (ItemStack stack : createToolSet("eclipsealloy", JDTToolTier.ECLIPSEALLOY, JDTArmorMaterial.ECLIPSEALLOY, 600)) {
            assertEnergyCapability(stack, 500_000);
        }
    }

    @Test
    void lowerTierEquipmentDoesNotExposeEnergyCapability() {
        ItemStack ferricoreSword = new ItemStack(new ItemJDTSword("ferricore_sword", JDTToolTier.FERRICORE));
        ItemStack blazegoldHelmet = new ItemStack(new ItemJDTArmor("blazegold_helmet", JDTArmorMaterial.BLAZEGOLD, EntityEquipmentSlot.HEAD));

        assertFalse(ferricoreSword.hasCapability(CapabilityEnergy.ENERGY, null));
        assertFalse(blazegoldHelmet.hasCapability(CapabilityEnergy.ENERGY, null));
    }

    @Test
    void bowsExposeOriginalModPotionCanisterItemHandler() {
        for (ItemStack bowStack : createBows()) {
            IItemHandler handler = bowStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            assertNotNull(handler, bowStack.getItem().getRegistryName() + " should expose a tool item handler");
            assertEquals(1, handler.getSlots(), "Original BaseBow exposes one tool-content slot");
            ItemStack canister = new ItemStack(new PotionCanisterItem());

            ItemStack remainder = handler.insertItem(0, canister, false);

            assertTrue(remainder.isEmpty(), "Bow item handler should accept a potion canister");
            assertEquals(canister.getItem(), handler.getStackInSlot(0).getItem());
            assertTrue(bowStack.hasTagCompound() && bowStack.getTagCompound().hasKey("ToolContents"),
                    "Inserted canister should be persisted on the bow stack");
        }
    }

    @Test
    void timeWandAcceptsTimeFluidThroughItemFluidCapability() {
        ModFluids.bootstrap();
        ItemTimeWand timeWand = new ItemTimeWand();
        ItemStack stack = new ItemStack(timeWand);
        Fluid timeFluid = ModFluids.getFluid("time_fluid");

        assertTrue(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null),
                "Time Wand should expose an item fluid handler");
        assertNotNull(timeFluid, "Time Fluid must be registered for the Time Wand handler");
        assertTrue(timeWand.canFillFluid(stack, new FluidStack(timeFluid, 1000)));
        timeWand.applyFilledAmount(stack, new FluidStack(timeFluid, 1000), 1000);
        assertEquals(1000, timeWand.getStoredFluid(stack));
    }

    @Test
    void portalGunV2UsesOriginalModEnergyDefaults() {
        ItemPortalGunV2 portalGun = new ItemPortalGunV2();
        ItemStack stack = new ItemStack(portalGun);

        assertEnergyCapability(stack, 1_000_000);
    }

    @Test
    void fluidPoweredWandsUseSharedSourceFluidPickupPathLikeOriginalMod() throws Exception {
        assertUsesFluidPickup("src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemTimeWand.java");
        assertUsesFluidPickup("src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPortalGunV2.java");
        assertUsesFluidPickup("src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWand.java");
        assertUsesFluidPickup("src/main/java/com/zzhalex/justdirethings/common/item/tool/ItemPolymorphicWandV2.java");
    }

    private static List<ItemStack> createBows() {
        return Arrays.asList(
                new ItemStack(new ItemJDTBow("bow_ferricore", JDTToolTier.FERRICORE, 250)),
                new ItemStack(new ItemJDTBow("bow_blazegold", JDTToolTier.BLAZEGOLD, 450)),
                new ItemStack(new ItemJDTBow("bow_celestigem", JDTToolTier.CELESTIGEM, 450)),
                new ItemStack(new ItemJDTBow("bow_eclipsealloy", JDTToolTier.ECLIPSEALLOY, 450))
        );
    }

    private static List<ItemStack> createToolSet(String prefix, JDTToolTier toolTier, JDTArmorMaterial armorMaterial, int bowDurability) {
        return Arrays.asList(
                new ItemStack(new ItemJDTSword(prefix + "_sword", toolTier)),
                new ItemStack(new ItemJDTPickaxe(prefix + "_pickaxe", toolTier)),
                new ItemStack(new ItemJDTShovel(prefix + "_shovel", toolTier)),
                new ItemStack(new ItemJDTAxe(prefix + "_axe", toolTier, 5.0F, -3.0F)),
                new ItemStack(new ItemJDTHoe(prefix + "_hoe", toolTier)),
                new ItemStack(new ItemJDTPaxel(prefix + "_paxel", toolTier, 5.0F, -2.8F)),
                new ItemStack(new ItemJDTBow("bow_" + prefix, toolTier, bowDurability)),
                new ItemStack(new ItemJDTArmor(prefix + "_helmet", armorMaterial, EntityEquipmentSlot.HEAD)),
                new ItemStack(new ItemJDTArmor(prefix + "_chestplate", armorMaterial, EntityEquipmentSlot.CHEST)),
                new ItemStack(new ItemJDTArmor(prefix + "_leggings", armorMaterial, EntityEquipmentSlot.LEGS)),
                new ItemStack(new ItemJDTArmor(prefix + "_boots", armorMaterial, EntityEquipmentSlot.FEET))
        );
    }

    private static void assertEnergyCapability(ItemStack stack, int capacity) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY, null);

        assertNotNull(storage, stack.getItem().getRegistryName() + " should expose FE storage");
        assertTrue(storage.canReceive(), stack.getItem().getRegistryName() + " should accept FE");
        assertTrue(storage.canExtract(), stack.getItem().getRegistryName() + " should allow FE use");
        assertEquals(capacity, storage.getMaxEnergyStored());
        assertEquals(capacity, storage.receiveEnergy(capacity, false));
        assertEquals(capacity, storage.getEnergyStored());
    }

    private static void assertUsesFluidPickup(String path) throws Exception {
        String source = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
        assertTrue(source.contains("FluidPickupHelper.pickupSourceFluid"),
                path + " should reuse the original-mod source-fluid pickup behavior");
    }
}
