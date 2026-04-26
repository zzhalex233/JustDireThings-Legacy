package com.zzhalex.justdirethings.common.world;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PortalChunkKeeperTest {

    @Test
    void portalChunkKeyUsesBlockPositionChunk() {
        assertEquals("0,0", PortalChunkKeeper.chunkKey(new BlockPos(1, 64, 1)));
    }

    @Test
    void retrackingPortalReplacesThePreviousChunkKey() {
        UUID portalId = UUID.randomUUID();

        PortalChunkKeeper.track(portalId, new BlockPos(1, 64, 1));
        PortalChunkKeeper.track(portalId, new BlockPos(40, 64, 1));

        assertEquals(Collections.singleton("2,0"), PortalChunkKeeper.getTrackedChunks(portalId));
        PortalChunkKeeper.clear(portalId);
    }
}
