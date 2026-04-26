package com.zzhalex.justdirethings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceTest {

    @Test
    void modMetadataIsStable() {
        assertEquals("justdirethings", Reference.MOD_ID);
    }
}
