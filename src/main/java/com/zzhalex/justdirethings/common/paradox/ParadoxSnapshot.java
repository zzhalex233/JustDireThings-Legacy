package com.zzhalex.justdirethings.common.paradox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParadoxSnapshot {

    private static final String KEY_BLOCKS = "Blocks";
    private static final String KEY_ENTITIES = "Entities";
    private static final String KEY_POS = "Pos";
    private static final String KEY_STATE = "State";
    private static final String KEY_DATA = "Data";
    private static final String KEY_X = "X";
    private static final String KEY_Y = "Y";
    private static final String KEY_Z = "Z";

    private final List<BlockEntry> blocks = new ArrayList<>();
    private final List<EntityEntry> entities = new ArrayList<>();

    public void addBlock(BlockPos relativePos, NBTTagCompound stateTag) {
        if (relativePos == null || stateTag == null) {
            return;
        }
        blocks.add(new BlockEntry(relativePos, stateTag.copy()));
    }

    public void addEntity(Vec3d relativePos, NBTTagCompound entityData) {
        if (relativePos == null || entityData == null) {
            return;
        }
        entities.add(new EntityEntry(relativePos, entityData.copy()));
    }

    public List<BlockEntry> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public List<EntityEntry> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public boolean isEmpty() {
        return blocks.isEmpty() && entities.isEmpty();
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList blockList = new NBTTagList();
        for (BlockEntry block : blocks) {
            NBTTagCompound blockTag = new NBTTagCompound();
            blockTag.setTag(KEY_POS, writeBlockPos(block.getRelativePos()));
            blockTag.setTag(KEY_STATE, block.getStateTag().copy());
            blockList.appendTag(blockTag);
        }
        root.setTag(KEY_BLOCKS, blockList);

        NBTTagList entityList = new NBTTagList();
        for (EntityEntry entity : entities) {
            NBTTagCompound entityTag = new NBTTagCompound();
            entityTag.setTag(KEY_POS, writeVec3d(entity.getRelativePos()));
            entityTag.setTag(KEY_DATA, entity.getEntityData().copy());
            entityList.appendTag(entityTag);
        }
        root.setTag(KEY_ENTITIES, entityList);
        return root;
    }

    public static ParadoxSnapshot read(NBTTagCompound root) {
        ParadoxSnapshot snapshot = new ParadoxSnapshot();
        if (root == null) {
            return snapshot;
        }

        NBTTagList blockList = root.getTagList(KEY_BLOCKS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < blockList.tagCount(); i++) {
            NBTTagCompound blockTag = blockList.getCompoundTagAt(i);
            snapshot.addBlock(readBlockPos(blockTag.getCompoundTag(KEY_POS)), blockTag.getCompoundTag(KEY_STATE));
        }

        NBTTagList entityList = root.getTagList(KEY_ENTITIES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entityList.tagCount(); i++) {
            NBTTagCompound entityTag = entityList.getCompoundTagAt(i);
            snapshot.addEntity(readVec3d(entityTag.getCompoundTag(KEY_POS)), entityTag.getCompoundTag(KEY_DATA));
        }
        return snapshot;
    }

    private static NBTTagCompound writeBlockPos(BlockPos pos) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(KEY_X, pos.getX());
        tag.setInteger(KEY_Y, pos.getY());
        tag.setInteger(KEY_Z, pos.getZ());
        return tag;
    }

    private static BlockPos readBlockPos(NBTTagCompound tag) {
        return new BlockPos(tag.getInteger(KEY_X), tag.getInteger(KEY_Y), tag.getInteger(KEY_Z));
    }

    private static NBTTagCompound writeVec3d(Vec3d vec) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble(KEY_X, vec.x);
        tag.setDouble(KEY_Y, vec.y);
        tag.setDouble(KEY_Z, vec.z);
        return tag;
    }

    private static Vec3d readVec3d(NBTTagCompound tag) {
        return new Vec3d(tag.getDouble(KEY_X), tag.getDouble(KEY_Y), tag.getDouble(KEY_Z));
    }

    public static final class BlockEntry {

        private final BlockPos relativePos;
        private final NBTTagCompound stateTag;

        private BlockEntry(BlockPos relativePos, NBTTagCompound stateTag) {
            this.relativePos = relativePos;
            this.stateTag = stateTag;
        }

        public BlockPos getRelativePos() {
            return relativePos;
        }

        public NBTTagCompound getStateTag() {
            return stateTag.copy();
        }
    }

    public static final class EntityEntry {

        private final Vec3d relativePos;
        private final NBTTagCompound entityData;

        private EntityEntry(Vec3d relativePos, NBTTagCompound entityData) {
            this.relativePos = relativePos;
            this.entityData = entityData;
        }

        public Vec3d getRelativePos() {
            return relativePos;
        }

        public NBTTagCompound getEntityData() {
            return entityData.copy();
        }
    }
}
