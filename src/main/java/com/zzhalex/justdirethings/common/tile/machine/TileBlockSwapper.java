package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TileBlockSwapper extends TileMachineBase implements ITickable {

    public enum SwapEntityType {
        NONE,
        HOSTILE,
        PASSIVE,
        ADULT,
        CHILD,
        PLAYER,
        LIVING,
        ITEM,
        ALL
    }

    private BlockPos boundTo;
    private boolean swapBlocks = true;
    private int swapEntityType;
    private boolean partnerExists;

    public TileBlockSwapper() {
        setTickSpeed(20);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        if (!shouldRunTimedMachine()) {
            return;
        }

        boolean exists = getPartnerTile() != null;
        boolean changed = exists != partnerExists;
        partnerExists = exists;
        if (exists && (swapBlocks || swapEntityType != SwapEntityType.NONE.ordinal())) {
            changed |= doSwap();
        }
        if (changed) {
            markDirtyClient();
        }
    }

    public boolean handleConnection(BlockPos otherPos) {
        if (world == null || otherPos == null || otherPos.equals(pos)) {
            return false;
        }
        TileEntity tileEntity = world.getTileEntity(otherPos);
        if (!(tileEntity instanceof TileBlockSwapper)) {
            return false;
        }

        TileBlockSwapper other = (TileBlockSwapper) tileEntity;
        if (otherPos.equals(boundTo)) {
            removePartnerConnection();
            return false;
        }

        if (boundTo != null) {
            removePartnerConnection();
        }
        if (other.boundTo != null) {
            other.removePartnerConnection();
        }

        setBoundTo(otherPos);
        other.setBoundTo(pos);
        return true;
    }

    public void removePartnerConnection() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner != null && pos.equals(partner.boundTo)) {
            partner.setBoundTo(null);
        }
        setBoundTo(null);
    }

    public BlockPos getBoundTo() {
        return boundTo;
    }

    public void setBoundTo(BlockPos boundTo) {
        this.boundTo = boundTo;
        this.partnerExists = boundTo != null && world != null && world.getTileEntity(boundTo) instanceof TileBlockSwapper;
        markDirtyClient();
    }

    public boolean doesPartnerExist() {
        partnerExists = getPartnerTile() != null;
        return partnerExists;
    }

    public boolean isSwapBlocks() {
        return swapBlocks;
    }

    public void setSwapBlocks(boolean swapBlocks) {
        this.swapBlocks = swapBlocks;
    }

    public int getSwapEntityType() {
        return swapEntityType;
    }

    public void setSwapEntityType(int swapEntityType) {
        SwapEntityType[] values = SwapEntityType.values();
        this.swapEntityType = Math.max(0, Math.min(values.length - 1, swapEntityType));
    }

    protected TileBlockSwapper getPartnerTile() {
        if (world == null || boundTo == null) {
            return null;
        }
        TileEntity tileEntity = world.getTileEntity(boundTo);
        return tileEntity instanceof TileBlockSwapper ? (TileBlockSwapper) tileEntity : null;
    }

    protected boolean doSwap() {
        return doSwapInternal() > 0;
    }

    protected int doSwapInternal() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner == null) {
            return 0;
        }

        int changed = 0;
        if (swapBlocks) {
            changed += swapTargetBlocks(partner);
        }
        if (swapEntityType != SwapEntityType.NONE.ordinal()) {
            changed += swapEntities(partner);
        }
        return changed;
    }

    protected int swapTargetBlocks(TileBlockSwapper partner) {
        int swapped = 0;
        for (BlockPos first : findSpotsToSwap()) {
            BlockPos second = partner.getWorldPos(getRelativePos(first));
            if (swapSingleBlock(first, second, partner)) {
                swapped++;
            }
        }
        if (swapped > 0) {
            playSwapSound(partner);
        }
        return swapped;
    }

    protected boolean swapSingleBlock(BlockPos first, BlockPos second, TileBlockSwapper partner) {
        if (first.equals(second) || !isBlockPosValid(first) || !partner.isBlockPosValid(second)) {
            return false;
        }

        IBlockState firstState = world.getBlockState(first);
        IBlockState secondState = world.getBlockState(second);
        if (firstState.getBlock() == Blocks.AIR && secondState.getBlock() == Blocks.AIR) {
            return false;
        }
        if (firstState.equals(secondState)) {
            return false;
        }

        NBTTagCompound firstNbt = saveTileNbt(world.getTileEntity(first));
        NBTTagCompound secondNbt = saveTileNbt(world.getTileEntity(second));

        world.removeTileEntity(first);
        partner.world.removeTileEntity(second);

        boolean placedFirst = world.setBlockState(first, secondState, 3);
        boolean placedSecond = partner.world.setBlockState(second, firstState, 3);
        if (!placedFirst || !placedSecond) {
            return false;
        }

        restoreTileNbt(world.getTileEntity(first), first, secondNbt);
        restoreTileNbt(partner.world.getTileEntity(second), second, firstNbt);
        validateBlock(first);
        partner.validateBlock(second);
        spawnTeleportParticles(first);
        partner.spawnTeleportParticles(second);
        return true;
    }

    protected int swapEntities(TileBlockSwapper partner) {
        List<Entity> entities = matchingEntities(getAABB());
        int swapped = 0;
        for (Entity entity : entities) {
            Vec3d remotePosition = partner.getWorldPos(getRelativePos(new Vec3d(entity.posX, entity.posY, entity.posZ)));
            entity.setPositionAndUpdate(remotePosition.x, remotePosition.y, remotePosition.z);
            swapped++;
        }
        if (swapped > 0) {
            playSwapSound(partner);
        }
        return swapped;
    }

    protected List<Entity> matchingEntities(AxisAlignedBB target) {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, target);
        List<Entity> matches = new ArrayList<>();
        SwapEntityType type = SwapEntityType.values()[swapEntityType];
        for (Entity entity : entities) {
            if (matches(type, entity) && isValidEntity(entity)) {
                matches.add(entity);
            }
        }
        return matches;
    }

    protected boolean isValidEntity(Entity entity) {
        return true;
    }

    protected static boolean matches(SwapEntityType type, Entity entity) {
        switch (type) {
            case HOSTILE:
                return entity instanceof IMob;
            case PASSIVE:
                return entity instanceof EntityAnimal;
            case ADULT:
                return entity instanceof EntityAnimal && !((EntityAnimal) entity).isChild();
            case CHILD:
                return entity instanceof EntityAnimal && ((EntityAnimal) entity).isChild();
            case PLAYER:
                return entity instanceof EntityPlayer;
            case LIVING:
                return entity instanceof net.minecraft.entity.EntityLivingBase;
            case ITEM:
                return entity instanceof EntityItem;
            case ALL:
                return true;
            case NONE:
            default:
                return false;
        }
    }

    protected boolean isBlockPosValid(BlockPos blockPos) {
        if (world == null || blockPos == null) {
            return false;
        }
        if (blockPos.equals(pos) || blockPos.equals(boundTo)) {
            return false;
        }
        IBlockState state = world.getBlockState(blockPos);
        return state.getBlock() == Blocks.AIR || state.getBlockHardness(world, blockPos) >= 0.0F;
    }

    protected AxisAlignedBB getAABB() {
        return new AxisAlignedBB(MachineActionHelper.targetPos(this));
    }

    protected List<BlockPos> findSpotsToSwap() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos blockPos = MachineActionHelper.targetPos(this);
        if (isBlockPosValid(blockPos)) {
            positions.add(blockPos);
        }
        return positions;
    }

    protected BlockPos getStartingPoint() {
        return MachineActionHelper.targetPos(this);
    }

    protected BlockPos getWorldPos(BlockPos relativePos) {
        return getStartingPoint().add(relativePos.getX(), relativePos.getY(), relativePos.getZ());
    }

    protected BlockPos getRelativePos(BlockPos worldPos) {
        return worldPos.subtract(getStartingPoint());
    }

    protected Vec3d getWorldPos(Vec3d relativePos) {
        BlockPos startingPoint = getStartingPoint();
        return new Vec3d(
                startingPoint.getX() + relativePos.x,
                startingPoint.getY() + relativePos.y,
                startingPoint.getZ() + relativePos.z
        );
    }

    protected Vec3d getRelativePos(Vec3d worldPos) {
        BlockPos startingPoint = getStartingPoint();
        return new Vec3d(
                worldPos.x - startingPoint.getX(),
                worldPos.y - startingPoint.getY(),
                worldPos.z - startingPoint.getZ()
        );
    }

    protected void validateBlock(BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        if (!state.getBlock().canPlaceBlockAt(world, blockPos)) {
            world.destroyBlock(blockPos, true);
        } else {
            world.notifyNeighborsOfStateChange(blockPos, state.getBlock(), false);
        }
    }

    protected void playSwapSound(TileBlockSwapper partner) {
        world.playSound(null, pos, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundCategory.BLOCKS, 0.33F, 1.0F);
        if (partner.world != world || !partner.pos.equals(pos)) {
            partner.world.playSound(null, partner.pos, SoundEvents.ENTITY_SHULKER_TELEPORT, SoundCategory.BLOCKS, 0.33F, 1.0F);
        }
    }

    protected void spawnTeleportParticles(BlockPos blockPos) {
        if (!(world instanceof WorldServer)) {
            return;
        }
        WorldServer worldServer = (WorldServer) world;
        for (int i = 0; i < 5; i++) {
            worldServer.spawnParticle(
                    EnumParticleTypes.PORTAL,
                    blockPos.getX() + world.rand.nextDouble(),
                    blockPos.getY() - 0.5D + world.rand.nextDouble(),
                    blockPos.getZ() + world.rand.nextDouble(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static NBTTagCompound saveTileNbt(TileEntity tileEntity) {
        NBTTagCompound tag = new NBTTagCompound();
        if (tileEntity != null) {
            tileEntity.writeToNBT(tag);
        }
        return tag;
    }

    private static void restoreTileNbt(TileEntity tileEntity, BlockPos blockPos, NBTTagCompound tag) {
        if (tileEntity == null || tag == null || tag.getKeySet().isEmpty()) {
            return;
        }
        tag.setInteger("x", blockPos.getX());
        tag.setInteger("y", blockPos.getY());
        tag.setInteger("z", blockPos.getZ());
        tileEntity.readFromNBT(tag);
        tileEntity.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (boundTo != null) {
            compound.setInteger("BoundX", boundTo.getX());
            compound.setInteger("BoundY", boundTo.getY());
            compound.setInteger("BoundZ", boundTo.getZ());
            compound.setBoolean("HasBoundTo", true);
        }
        compound.setBoolean("SwapBlocks", swapBlocks);
        compound.setInteger("SwapEntityType", swapEntityType);
        compound.setBoolean("PartnerExists", partnerExists);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.getBoolean("HasBoundTo")) {
            boundTo = new BlockPos(compound.getInteger("BoundX"), compound.getInteger("BoundY"), compound.getInteger("BoundZ"));
        } else {
            boundTo = null;
        }
        swapBlocks = !compound.hasKey("SwapBlocks") || compound.getBoolean("SwapBlocks");
        setSwapEntityType(compound.getInteger("SwapEntityType"));
        partnerExists = compound.getBoolean("PartnerExists");
    }

    public static class T1 extends TileBlockSwapper {
    }

    public static class T2 extends TileBlockSwapper implements TileAdvancedMachine {

        private final FilterItemHandler filterHandler = new FilterItemHandler(ADVANCED_FILTER_SLOTS);

        public T2() {
            configureAdvancedMachine();
        }

        @Override
        public FilterItemHandler getFilterHandler() {
            return filterHandler;
        }

        @Override
        public int getStandardEnergyCost() {
            return 50;
        }

        @Override
        protected boolean doSwap() {
            if (!hasEnoughEnergy(getEnergyCost(getAreaVolume()))) {
                return false;
            }
            int swapped = doSwapInternal();
            if (swapped > 0) {
                consumeEnergy(getEnergyCost(swapped), false);
            }
            return swapped > 0;
        }

        public int getEnergyCost(int targets) {
            return Math.max(0, targets) * getStandardEnergyCost();
        }

        protected int getAreaVolume() {
            AxisAlignedBB area = getAABB();
            int width = Math.max(1, (int) Math.abs(Math.floor(area.maxX) - Math.floor(area.minX)));
            int height = Math.max(1, (int) Math.abs(Math.floor(area.maxY) - Math.floor(area.minY)));
            int depth = Math.max(1, (int) Math.abs(Math.floor(area.maxZ) - Math.floor(area.minZ)));
            return width * height * depth;
        }

        public void setAreaOnly(double xRadius, double yRadius, double zRadius) {
            getAreaState().setArea(xRadius, yRadius, zRadius);
            markDirtyClient();
        }

        public void updatePartnerArea() {
            if (getPartnerTile() instanceof T2) {
                T2 partner = (T2) getPartnerTile();
                partner.setAreaOnly(
                        getAreaState().getXRadius(),
                        getAreaState().getYRadius(),
                        getAreaState().getZRadius()
                );
            }
        }

        @Override
        public boolean handleConnection(BlockPos otherPos) {
            boolean connected = super.handleConnection(otherPos);
            if (connected) {
                updatePartnerArea();
            }
            return connected;
        }

        @Override
        protected boolean isBlockPosValid(BlockPos blockPos) {
            if (!super.isBlockPosValid(blockPos) || isInBothAreas(blockPos)) {
                return false;
            }
            IBlockState state = world.getBlockState(blockPos);
            return state.getBlock() == Blocks.AIR || matchesBlockFilter(state, blockPos);
        }

        @Override
        protected boolean isValidEntity(Entity entity) {
            return !isInBothAreas(new Vec3d(entity.posX, entity.posY, entity.posZ));
        }

        @Override
        protected AxisAlignedBB getAABB() {
            return getAreaState().createArea(pos);
        }

        @Override
        protected List<BlockPos> findSpotsToSwap() {
            List<BlockPos> positions = new ArrayList<>();
            for (BlockPos blockPos : getAreaPositionsNearestFirst()) {
                if (isBlockPosValid(blockPos)) {
                    positions.add(blockPos);
                }
            }
            positions.sort(Comparator.comparingDouble(blockPos -> blockPos.distanceSq(pos)));
            return positions;
        }

        @Override
        protected BlockPos getStartingPoint() {
            return getOffsetTargetPos();
        }

        protected boolean isInBothAreas(BlockPos blockPos) {
            TileBlockSwapper partner = getPartnerTile();
            if (!(partner instanceof T2)) {
                return false;
            }
            AxisAlignedBB thisArea = getAABB();
            AxisAlignedBB partnerArea = ((T2) partner).getAABB();
            return thisArea.contains(new Vec3d(blockPos)) && partnerArea.contains(new Vec3d(blockPos));
        }

        protected boolean isInBothAreas(Vec3d vec3d) {
            TileBlockSwapper partner = getPartnerTile();
            if (!(partner instanceof T2)) {
                return false;
            }
            AxisAlignedBB thisArea = getAABB();
            AxisAlignedBB partnerArea = ((T2) partner).getAABB();
            return thisArea.contains(vec3d) && partnerArea.contains(vec3d);
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            return writeAdvancedMachineToNbt(compound);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            readAdvancedMachineFromNbt(compound);
        }
    }
}
