package com.zzhalex.justdirethings.common.entity.group;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JDTEntityGroupsTest {

    @Test
    void dragonIsAlwaysDeniedAsPolymorphicTarget() {
        assertTrue(JDTEntityGroups.isPolymorphicTargetDenied("minecraft:ender_dragon"));
    }
}
