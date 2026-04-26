package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ParadoxRuntimePlan {

    private static final int TICKS_PER_TARGET = 10;

    private final List<BlockTarget> blocksToRestore;
    private final List<EntityTarget> entitiesToRestore;
    private final int runtimeTicks;

    private ParadoxRuntimePlan(List<BlockTarget> blocksToRestore, List<EntityTarget> entitiesToRestore) {
        this.blocksToRestore = Collections.unmodifiableList(new ArrayList<>(blocksToRestore));
        this.entitiesToRestore = Collections.unmodifiableList(new ArrayList<>(entitiesToRestore));
        this.runtimeTicks = runtimeTicksFor(blocksToRestore.size(), entitiesToRestore.size());
    }

    public static ParadoxRuntimePlan fromSnapshot(BlockPos origin, ParadoxSnapshot snapshot) {
        List<BlockTarget> blocks = new ArrayList<>();
        List<EntityTarget> entities = new ArrayList<>();
        if (origin != null && snapshot != null) {
            for (ParadoxSnapshot.BlockEntry block : snapshot.getBlocks()) {
                blocks.add(new BlockTarget(origin.add(block.getRelativePos()), block.getStateTag()));
            }
            for (ParadoxSnapshot.EntityEntry entity : snapshot.getEntities()) {
                Vec3d relative = entity.getRelativePos();
                Vec3d absolute = new Vec3d(origin.getX() + relative.x, origin.getY() + relative.y, origin.getZ() + relative.z);
                entities.add(new EntityTarget(absolute, entity.getEntityData()));
            }
        }
        return new ParadoxRuntimePlan(blocks, entities);
    }

    public static int runtimeTicksFor(int blockCount, int entityCount) {
        return Math.max(0, blockCount + entityCount) * TICKS_PER_TARGET;
    }

    public List<BlockTarget> getBlocksToRestore() {
        return blocksToRestore;
    }

    public List<EntityTarget> getEntitiesToRestore() {
        return entitiesToRestore;
    }

    public int getRuntimeTicks() {
        return runtimeTicks;
    }

    public static final class BlockTarget {

        private final BlockPos absolutePos;
        private final NBTTagCompound stateTag;

        private BlockTarget(BlockPos absolutePos, NBTTagCompound stateTag) {
            this.absolutePos = absolutePos;
            this.stateTag = stateTag.copy();
        }

        public BlockPos getAbsolutePos() {
            return absolutePos;
        }

        public NBTTagCompound getStateTag() {
            return stateTag.copy();
        }
    }

    public static final class EntityTarget {

        private final Vec3d absolutePos;
        private final NBTTagCompound entityData;

        private EntityTarget(Vec3d absolutePos, NBTTagCompound entityData) {
            this.absolutePos = absolutePos;
            this.entityData = entityData.copy();
        }

        public Vec3d getAbsolutePos() {
            return absolutePos;
        }

        public NBTTagCompound getEntityData() {
            return entityData.copy();
        }
    }
}
