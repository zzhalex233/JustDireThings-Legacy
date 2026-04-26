package com.zzhalex.justdirethings.client.gui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuiNineSliceTest {

    @Test
    void backgroundSlicesPreserveTopAndBottomBorderHeight() {
        List<GuiNineSlice.Slice> slices = GuiNineSlice.buildSlices(10, 20, 176, 166);

        assertEquals(9, slices.size());
        assertEquals(8, slices.get(0).targetHeight);
        assertEquals(8, slices.get(1).targetHeight);
        assertEquals(8, slices.get(2).targetHeight);
        assertEquals(8, slices.get(6).targetHeight);
        assertEquals(8, slices.get(7).targetHeight);
        assertEquals(8, slices.get(8).targetHeight);
    }

    @Test
    void backgroundSlicesStretchOnlyTheCenterAndEdges() {
        List<GuiNineSlice.Slice> slices = GuiNineSlice.buildSlices(10, 20, 176, 166);

        GuiNineSlice.Slice topEdge = slices.get(1);
        GuiNineSlice.Slice middle = slices.get(4);
        GuiNineSlice.Slice bottomEdge = slices.get(7);

        assertEquals(160, topEdge.targetWidth);
        assertEquals(8, topEdge.targetHeight);
        assertEquals(160, middle.targetWidth);
        assertEquals(150, middle.targetHeight);
        assertEquals(160, bottomEdge.targetWidth);
        assertEquals(8, bottomEdge.targetHeight);
    }
}
