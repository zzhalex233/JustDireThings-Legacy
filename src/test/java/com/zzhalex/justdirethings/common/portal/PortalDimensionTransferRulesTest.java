package com.zzhalex.justdirethings.common.portal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortalDimensionTransferRulesTest {

    @Test
    void nonPlayerCrossDimensionEntitiesUseDirectTeleporter() {
        assertTrue(PortalDimensionTransferRules.requiresDirectTeleporter(0, -1));
    }

    @Test
    void sameDimensionEntitiesDoNotNeedDirectTeleporter() {
        assertFalse(PortalDimensionTransferRules.requiresDirectTeleporter(0, 0));
    }

    @Test
    void playersAlsoUseDirectTeleporterWhenCrossingDimensions() {
        assertTrue(PortalDimensionTransferRules.requiresDirectTeleporter(0, -1));
    }

    @Test
    void directTeleporterIsNotVanillaSoItDoesNotSearchForNetherPortals() {
        assertFalse(new PortalDirectTeleporter().isVanilla());
    }
}
