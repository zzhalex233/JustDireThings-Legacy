package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class T2BlockSwapperParityTest {

    @Test
    void advancedBlockSwapperUsesOriginalAreaToAreaSwapStructure() throws IOException {
        String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockSwapper.java");

        assertTrue(contents.contains("protected int doSwapInternal()"),
                "BlockSwapper should expose a counted swap path so T2 can extract energy per actually swapped target");
        assertTrue(contents.contains("protected List<BlockPos> findSpotsToSwap()"),
                "BlockSwapper should mirror the upstream findSpotsToSwap area hook");
        assertTrue(contents.contains("protected BlockPos getStartingPoint()"),
                "BlockSwapper should translate local area coordinates from a starting point");
        assertTrue(contents.contains("protected BlockPos getWorldPos(BlockPos relativePos)"),
                "BlockSwapper should map local positions into partner world positions");
        assertTrue(contents.contains("protected BlockPos getRelativePos(BlockPos worldPos)"),
                "BlockSwapper should map world positions back to local area-relative positions");
        assertTrue(contents.contains("protected boolean isInBothAreas(BlockPos blockPos)"),
                "T2 BlockSwapper should reject blocks inside overlapping local/partner areas");
        assertTrue(contents.contains("protected boolean isInBothAreas(Vec3d vec3d)"),
                "T2 BlockSwapper should reject entities inside overlapping local/partner areas");
        assertTrue(contents.contains("partner.setAreaOnly"),
                "T2 BlockSwapper should mirror radius-only area changes to its partner like upstream");
        assertTrue(contents.contains("getEnergyCost(getAreaVolume())"),
                "T2 BlockSwapper should require enough FE for the full selected area before swapping");
        assertTrue(contents.contains("consumeEnergy(getEnergyCost(swapped), false)"),
                "T2 BlockSwapper should extract FE based on the number of blocks/entities actually swapped");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
