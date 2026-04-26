package com.zzhalex.justdirethings.common.item.tool;

import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ItemPortalGunTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void classicPortalGunStoresStableUuidOnTheStack() {
        ItemPortalGun portalGun = new ItemPortalGun();
        ItemStack stack = new ItemStack(portalGun);

        UUID first = portalGun.getOrCreatePortalGunId(stack);
        UUID second = portalGun.getOrCreatePortalGunId(stack);

        assertEquals(first, second);
        assertEquals(first.toString(), stack.getTagCompound().getString(JDTDataKeys.PORTAL_GUN_UUID));
    }

    @Test
    void classicPortalGunRepairsInvalidUuidData() {
        ItemPortalGun portalGun = new ItemPortalGun();
        ItemStack stack = new ItemStack(portalGun);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(JDTDataKeys.PORTAL_GUN_UUID, "not-a-uuid");

        UUID repaired = portalGun.getOrCreatePortalGunId(stack);

        assertNotEquals("not-a-uuid", repaired.toString());
        assertEquals(repaired.toString(), stack.getTagCompound().getString(JDTDataKeys.PORTAL_GUN_UUID));
    }

    @Test
    void classicPortalGunUsesV1EnergyDefaultsAndClampsStoredEnergy() {
        ItemPortalGun portalGun = new ItemPortalGun();
        ItemStack stack = new ItemStack(portalGun);

        assertEquals(JDTConfig.portalGunV1RfCapacity, portalGun.getEnergyCapacity(stack));

        portalGun.setStoredEnergy(stack, JDTConfig.portalGunV1RfCapacity + 1);
        assertEquals(JDTConfig.portalGunV1RfCapacity, portalGun.getStoredEnergy(stack));

        portalGun.setStoredEnergy(stack, -100);
        assertEquals(0, portalGun.getStoredEnergy(stack));
    }
}
