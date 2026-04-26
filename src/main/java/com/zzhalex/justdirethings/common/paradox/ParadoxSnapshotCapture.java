package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ParadoxSnapshotCapture {

    private ParadoxSnapshotCapture() {
    }

    public static void captureBlock(ParadoxSnapshot snapshot, BlockPos origin, BlockPos absolutePos, NBTTagCompound stateTag) {
        if (snapshot == null || origin == null || absolutePos == null || stateTag == null) {
            return;
        }
        snapshot.addBlock(absolutePos.subtract(origin), stateTag);
    }

    public static void captureEntity(ParadoxSnapshot snapshot, BlockPos origin, Vec3d absolutePos, NBTTagCompound entityData) {
        if (snapshot == null || origin == null || absolutePos == null || entityData == null) {
            return;
        }
        snapshot.addEntity(
            new Vec3d(absolutePos.x - origin.getX(), absolutePos.y - origin.getY(), absolutePos.z - origin.getZ()),
            entityData
        );
    }
}
