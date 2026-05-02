package com.zzhalex.justdirethings.audit;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class T2ClickerParityTest {

    @Test
    void clickerKeepsOriginalBlockAirEntityTargetStructure() throws IOException {
        String contents = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileClicker.java");

        assertTrue(contents.contains("enum ClickTarget"),
                "Clicker should model upstream block/air/entity target modes explicitly");
        assertTrue(contents.contains("protected boolean clickEntity(EntityLivingBase entity)"),
                "Clicker should have an entity-click path, not just block right-clicking");
        assertTrue(contents.contains("protected boolean clickAir(BlockPos targetPos)"),
                "Clicker should keep the upstream air-click path");
        assertTrue(contents.contains("protected List<EntityLivingBase> findEntitiesToClick(AxisAlignedBB area)"),
                "Clicker should gather living entities from its target AABB");
        assertTrue(contents.contains("protected AxisAlignedBB getClickAABB()"),
                "Clicker should expose the T1/T2 target AABB hook");
        assertTrue(contents.contains("getAreaState().createArea(pos)"),
                "Clicker T2 should scan entities/blocks over the configured advanced area");
        assertTrue(contents.contains("positionsToClick") && contents.contains("entitiesToClick"),
                "Clicker should keep the upstream queued block/entity traversal lists");
        assertTrue(contents.contains("remove(0)") || contents.contains("removeFirst()"),
                "Clicker should advance through queued targets instead of re-clicking the first valid target forever");
    }

    @Test
    void t2PoweredToolMachinesChargeHeldItemsEveryServerTick() throws IOException {
        String timedBase = read("src/main/java/com/zzhalex/justdirethings/common/tile/base/TileTimedMachineBase.java");
        String clicker = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileClicker.java");
        String breaker = read("src/main/java/com/zzhalex/justdirethings/common/tile/machine/TileBlockBreaker.java");

        assertTrue(timedBase.contains("protected void onServerTick()"),
                "Timed machines should expose a shared per-server-tick hook instead of charging tools from performWork");
        assertTrue(timedBase.indexOf("onServerTick();") < timedBase.indexOf("shouldRunTimedMachine()"),
                "The per-server-tick hook should run before timed/redstone work gating, matching upstream T2 charging");
        assertTrue(clicker.contains("protected void onServerTick()") && clicker.contains("chargeItemStack(getItemHandler().getStackInSlot(0))"),
                "Clicker T2 should charge its held item every server tick like upstream");
        assertTrue(clicker.contains("protected boolean canRun()") && clicker.contains("clickType == 2") && clicker.contains("isHandActive()"),
                "Clicker should keep the upstream long-hold click exception so channeling-style interactions keep running");
        assertTrue(breaker.contains("protected void onServerTick()") && breaker.contains("chargeItemStack(getItemHandler().getStackInSlot(0))"),
                "Block Breaker T2 should charge its tool every server tick like upstream");
    }

    private static String read(String path) throws IOException {
        return Files.readString(path(path), StandardCharsets.UTF_8);
    }

    private static Path path(String path) {
        return Paths.get(path);
    }
}
