package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.common.item.ability.Ability;
import com.zzhalex.justdirethings.common.item.base.EnergyBackedItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicWandParityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void blazejetWandMatchesOriginalDurabilityAndSupportedAbilities() {
        ItemBlazejetWand wand = new ItemBlazejetWand();

        assertEquals(ItemBlazejetWand.DURABILITY, wand.getMaxDamage());
        assertTrue(wand.supportsAbility(Ability.LAVAREPAIR));
        assertTrue(wand.supportsAbility(Ability.AIRBURST));
        assertFalse(new ItemStack(wand).hasCapability(CapabilityEnergy.ENERGY, null));
    }

    @Test
    void voidshiftWandExposesEnergyAndOriginalAbilitySet() {
        ItemVoidshiftWand wand = new ItemVoidshiftWand();
        ItemStack stack = new ItemStack(wand);

        assertEquals(ItemVoidshiftWand.DURABILITY, wand.getMaxDamage());
        assertEquals(ItemVoidshiftWand.ENERGY_CAPACITY, wand.getEnergyCapacity(stack));
        assertTrue(wand.supportsAbility(Ability.AIRBURST));
        assertTrue(wand.supportsAbility(Ability.VOIDSHIFT));
        assertTrue(stack.hasCapability(CapabilityEnergy.ENERGY, null));

        wand.setStoredEnergy(stack, ItemVoidshiftWand.ENERGY_CAPACITY + 1);
        assertEquals(ItemVoidshiftWand.ENERGY_CAPACITY, wand.getStoredEnergy(stack));
    }

    @Test
    void eclipsegateWandExposesLargerEnergyBufferAndEclipsegateAbility() {
        ItemEclipsegateWand wand = new ItemEclipsegateWand();
        ItemStack stack = new ItemStack(wand);

        assertEquals(ItemEclipsegateWand.DURABILITY, wand.getMaxDamage());
        assertEquals(ItemEclipsegateWand.ENERGY_CAPACITY, wand.getEnergyCapacity(stack));
        assertTrue(wand.supportsAbility(Ability.AIRBURST));
        assertTrue(wand.supportsAbility(Ability.VOIDSHIFT));
        assertTrue(wand.supportsAbility(Ability.ECLIPSEGATE));
        assertTrue(stack.hasCapability(CapabilityEnergy.ENERGY, null));

        wand.setStoredEnergy(stack, -100);
        assertEquals(0, wand.getStoredEnergy(stack));
    }

    @Test
    void polymorphicWandMatchesOriginalDurabilityAbilitiesAndFluidStorage() {
        ItemPolymorphicWand wand = new ItemPolymorphicWand();
        ItemStack stack = new ItemStack(wand);

        assertEquals(ItemPolymorphicWand.DURABILITY, wand.getMaxDamage());
        assertEquals(ItemPolymorphicWand.FLUID_CAPACITY, wand.getFluidCapacity(stack));
        assertTrue(wand.supportsAbility(Ability.LAVAREPAIR));
        assertTrue(wand.supportsAbility(Ability.POLYMORPH_RANDOM));
        assertTrue(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
        assertFalse(wand instanceof EnergyBackedItem);

        wand.setStoredFluid(stack, ItemPolymorphicWand.FLUID_CAPACITY + 100);
        assertEquals(ItemPolymorphicWand.FLUID_CAPACITY, wand.getStoredFluid(stack));
    }
}
