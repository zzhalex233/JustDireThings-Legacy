package com.zzhalex.justdirethings.client.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorBlockStatePanelTest {

    @Test
    void sensorBlockStatePanelUsesFixedUpstreamLikeGeometry() {
        assertEquals(100, SensorBlockStatePanelLayout.PANEL_WIDTH);
        assertEquals(-101, SensorBlockStatePanelLayout.PANEL_CLICK_LEFT_OFFSET);
        assertEquals(90, SensorBlockStatePanelLayout.LIST_WIDTH);
        assertEquals(-95, SensorBlockStatePanelLayout.LIST_LEFT_OFFSET);
        assertEquals(5, SensorBlockStatePanelLayout.LIST_TOP_OFFSET);
        assertEquals(10, SensorBlockStatePanelLayout.LIST_BOTTOM_MARGIN);
        assertEquals(5, SensorBlockStatePanelLayout.SCROLLBAR_RIGHT_PADDING);
    }

    @Test
    void sensorBlockStatePanelTrimsLongLabelsToFitTheRowWidth() {
        String trimmed = SensorBlockStatePanelLayout.trimToWidth("minecraft:ultra_long_sensor_property_name", 20);

        assertEquals("minecraft:ultra_l...", trimmed);
        assertTrue(trimmed.length() <= 20);
    }

    @Test
    void sensorBlockStatePanelKeepsAnyLabelVisibleAndCenteredAsText() {
        assertEquals("ANY", SensorBlockStatePanelLayout.anyLabel());
        assertFalse(SensorBlockStatePanelLayout.anyLabel().isEmpty());
    }
}
