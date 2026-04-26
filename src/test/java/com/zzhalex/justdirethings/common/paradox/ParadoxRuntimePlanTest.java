package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParadoxRuntimePlanTest {

    @Test
    void runtimePlanConvertsRelativeSnapshotEntriesToAbsoluteTargets() {
        ParadoxSnapshot snapshot = new ParadoxSnapshot();
        snapshot.addBlock(new BlockPos(1, 0, -2), namedTag("minecraft:stone"));
        snapshot.addEntity(new Vec3d(0.5D, 1.0D, -1.5D), namedTag("minecraft:zombie"));

        ParadoxRuntimePlan plan = ParadoxRuntimePlan.fromSnapshot(new BlockPos(10, 64, 20), snapshot);

        assertEquals(1, plan.getBlocksToRestore().size());
        assertEquals(1, plan.getEntitiesToRestore().size());
        assertEquals(new BlockPos(11, 64, 18), plan.getBlocksToRestore().get(0).getAbsolutePos());
        assertEquals(10.5D, plan.getEntitiesToRestore().get(0).getAbsolutePos().x, 0.001D);
        assertEquals(65.0D, plan.getEntitiesToRestore().get(0).getAbsolutePos().y, 0.001D);
        assertEquals(18.5D, plan.getEntitiesToRestore().get(0).getAbsolutePos().z, 0.001D);
        assertEquals(20, plan.getRuntimeTicks());
    }

    @Test
    void captureHelperRoundTripsAbsoluteTargetsBackIntoRuntimePlan() {
        ParadoxSnapshot snapshot = new ParadoxSnapshot();
        BlockPos origin = new BlockPos(10, 64, 20);

        ParadoxSnapshotCapture.captureBlock(snapshot, origin, new BlockPos(12, 65, 19), namedTag("minecraft:diamond_block"));
        ParadoxSnapshotCapture.captureEntity(snapshot, origin, new Vec3d(10.25D, 66.0D, 18.75D), namedTag("minecraft:skeleton"));

        ParadoxRuntimePlan plan = ParadoxRuntimePlan.fromSnapshot(origin, snapshot);

        assertEquals(new BlockPos(12, 65, 19), plan.getBlocksToRestore().get(0).getAbsolutePos());
        assertEquals(10.25D, plan.getEntitiesToRestore().get(0).getAbsolutePos().x, 0.001D);
        assertEquals(66.0D, plan.getEntitiesToRestore().get(0).getAbsolutePos().y, 0.001D);
        assertEquals(18.75D, plan.getEntitiesToRestore().get(0).getAbsolutePos().z, 0.001D);
    }

    private static NBTTagCompound namedTag(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        return tag;
    }
}
