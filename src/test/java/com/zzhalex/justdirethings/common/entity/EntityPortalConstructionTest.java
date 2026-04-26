package com.zzhalex.justdirethings.common.entity;

import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EntityPortalConstructionTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void portalCanBeConstructedWhileEntityBaseInitializesPosition() {
        assertDoesNotThrow(() -> new EntityPortal(null));
    }

    @Test
    void configuredPortalCanBeConstructedForProjectileImpact() {
        assertDoesNotThrow(() -> new EntityPortal(
                null,
                EnumFacing.NORTH,
                EnumFacing.Axis.Z,
                UUID.randomUUID(),
                true,
                UUID.randomUUID()
        ));
    }
}
