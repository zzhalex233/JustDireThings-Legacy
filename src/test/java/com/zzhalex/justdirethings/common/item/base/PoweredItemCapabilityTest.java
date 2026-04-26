package com.zzhalex.justdirethings.common.item.base;

import com.zzhalex.justdirethings.common.item.misc.FluidCanisterItem;
import com.zzhalex.justdirethings.common.item.misc.PocketGeneratorItem;
import com.zzhalex.justdirethings.common.item.tool.ItemEclipsegateWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPolymorphicWand;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGun;
import com.zzhalex.justdirethings.common.item.tool.ItemPortalGunV2;
import com.zzhalex.justdirethings.common.item.tool.ItemTimeWand;
import com.zzhalex.justdirethings.common.item.tool.ItemVoidshiftWand;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PoweredItemCapabilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
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
}
