package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParadoxSnapshotTest {

    @Test
    void snapshotRoundTripsBlockAndEntityEntries() {
        NBTTagCompound blockState = new NBTTagCompound();
        blockState.setString("Name", "minecraft:stone");

        NBTTagCompound entityData = new NBTTagCompound();
        entityData.setString("id", "minecraft:zombie");

        ParadoxSnapshot snapshot = new ParadoxSnapshot();
        snapshot.addBlock(new BlockPos(1, 2, 3), blockState);
        snapshot.addEntity(new Vec3d(0.25D, 1.5D, -2.0D), entityData);

        ParadoxSnapshot restored = ParadoxSnapshot.read(snapshot.writeToNbt());

        assertEquals(1, restored.getBlocks().size());
        assertEquals(new BlockPos(1, 2, 3), restored.getBlocks().get(0).getRelativePos());
        assertEquals("minecraft:stone", restored.getBlocks().get(0).getStateTag().getString("Name"));
        assertEquals(1, restored.getEntities().size());
        assertEquals(0.25D, restored.getEntities().get(0).getRelativePos().x, 0.001D);
        assertEquals(1.5D, restored.getEntities().get(0).getRelativePos().y, 0.001D);
        assertEquals(-2.0D, restored.getEntities().get(0).getRelativePos().z, 0.001D);
        assertEquals("minecraft:zombie", restored.getEntities().get(0).getEntityData().getString("id"));
    }
}
