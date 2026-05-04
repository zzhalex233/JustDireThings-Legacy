package com.zzhalex.justdirethings.common.tile.machine;

import com.zzhalex.justdirethings.capability.inventory.FilterItemHandler;
import com.zzhalex.justdirethings.common.block.group.JDTBlockGroups;
import com.zzhalex.justdirethings.common.entity.group.JDTEntityGroups;
import com.zzhalex.justdirethings.common.portal.PortalDimensionTransferRules;
import com.zzhalex.justdirethings.common.portal.PortalDirectTeleporter;
import com.zzhalex.justdirethings.common.tile.base.MachineFilterHelper;
import com.zzhalex.justdirethings.common.tile.base.MachineRedstoneState;
import com.zzhalex.justdirethings.common.world.PortalChunkKeeper;
import com.zzhalex.justdirethings.common.tile.base.TileAdvancedMachine;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

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

    private static final int NO_BOUND_DIMENSION = Integer.MIN_VALUE;

    private BlockPos boundTo;
    private int boundDimension = NO_BOUND_DIMENSION;
    private boolean swapBlocks = true;
    private int swapEntityType;
    private boolean partnerExists;
    protected int lastBlockValidationCount;
    protected int lastEntitySwapCount;
    protected final List<BlockPos> thisValidationList = new ArrayList<>();
    protected final List<BlockPos> thatValidationList = new ArrayList<>();

    public TileBlockSwapper() {
        setTickSpeed(20);
        getRedstoneState().setMode(MachineRedstoneState.RedstoneMode.PULSE);
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }
        handleTicks();
        evaluateRedstoneControl();

        boolean exists = getPartnerTile() != null;
        boolean changed = exists != partnerExists;
        partnerExists = exists;
        if (boundTo != null && boundDimension != NO_BOUND_DIMENSION && boundDimension != getCurrentDimension()) {
            syncPartnerChunkTicket();
        }
        if (isRedstoneActive() && canRun() && exists && canSwap() && (swapBlocks || swapEntityType != SwapEntityType.NONE.ordinal())) {
            changed |= doSwap();
        }
        if (changed) {
            markDirtyClient();
        }
    }

    public boolean handleConnection(BlockPos otherPos) {
        return world != null && handleConnection(world.provider.getDimension(), otherPos);
    }

    public boolean handleConnection(int otherDimension, BlockPos otherPos) {
        if (world == null || otherPos == null || (otherDimension == getCurrentDimension() && otherPos.equals(pos))) {
            return false;
        }
        World otherWorld = getWorldForDimension(otherDimension);
        if (otherWorld == null) {
            return false;
        }
        TileEntity tileEntity = otherWorld.getTileEntity(otherPos);
        if (!(tileEntity instanceof TileBlockSwapper) || !isValidPartner(tileEntity)) {
            return false;
        }

        TileBlockSwapper other = (TileBlockSwapper) tileEntity;
        if (isPartnerNodeConnected(otherDimension, otherPos)) {
            removePartnerConnection();
            return false;
        }

        if (boundTo != null) {
            removePartnerConnection();
        }
        if (other.boundTo != null) {
            other.removePartnerConnection();
        }

        setBoundTo(otherDimension, otherPos);
        other.setBoundTo(getCurrentDimension(), pos);
        return true;
    }

    public void removePartnerConnection() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner != null && partner.isPartnerNodeConnected(getCurrentDimension(), pos)) {
            partner.setBoundTo(null);
        }
        clearPartnerChunkTicket();
        setBoundTo(null);
    }

    public BlockPos getBoundTo() {
        return boundTo;
    }

    public int getBoundDimension() {
        return boundDimension == NO_BOUND_DIMENSION && world != null ? world.provider.getDimension() : boundDimension;
    }

    public void setBoundTo(BlockPos boundTo) {
        if (boundTo == null) {
            setBoundTo(NO_BOUND_DIMENSION, null);
        } else {
            setBoundTo(getCurrentDimension(), boundTo);
        }
    }

    public void setBoundTo(int boundDimension, BlockPos boundTo) {
        this.boundTo = boundTo;
        this.boundDimension = boundTo == null ? NO_BOUND_DIMENSION : boundDimension;
        World partnerWorld = getPartnerWorld();
        this.partnerExists = boundTo != null && partnerWorld != null && partnerWorld.getTileEntity(boundTo) instanceof TileBlockSwapper;
        if (boundTo != null) {
            syncPartnerChunkTicket();
        } else {
            clearPartnerChunkTicket();
        }
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
        World partnerWorld = getPartnerWorld();
        if (partnerWorld == null || boundTo == null) {
            return null;
        }
        ensureChunkLoaded(partnerWorld, boundTo);
        TileEntity tileEntity = partnerWorld.getTileEntity(boundTo);
        return tileEntity instanceof TileBlockSwapper ? (TileBlockSwapper) tileEntity : null;
    }

    protected boolean isValidPartner(TileEntity tileEntity) {
        return tileEntity != null && tileEntity.getClass() == getClass();
    }

    protected boolean isPartnerNodeConnected(int dimension, BlockPos otherPos) {
        return boundTo != null && boundDimension == dimension && boundTo.equals(otherPos);
    }

    protected int getCurrentDimension() {
        return world == null ? 0 : world.provider.getDimension();
    }

    protected World getPartnerWorld() {
        if (world == null || boundTo == null) {
            return null;
        }
        int dimension = boundDimension == NO_BOUND_DIMENSION ? world.provider.getDimension() : boundDimension;
        return getWorldForDimension(dimension);
    }

    protected World getWorldForDimension(int dimension) {
        if (world != null && world.provider.getDimension() == dimension) {
            return world;
        }
        World targetWorld = DimensionManager.getWorld(dimension, true);
        if (targetWorld == null && DimensionManager.isDimensionRegistered(dimension)) {
            try {
                DimensionManager.initDimension(dimension);
                targetWorld = DimensionManager.getWorld(dimension, true);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return targetWorld;
    }

    protected boolean isSameDimension(TileBlockSwapper partner) {
        return partner != null && world != null && partner.world != null
                && world.provider.getDimension() == partner.world.provider.getDimension();
    }

    protected boolean doSwap() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner == null) {
            return false;
        }
        int swapped = doSwapInternal();
        if (swapped > 0) {
            playSwapSound(partner);
        }
        return swapped > 0;
    }

    protected int doSwapInternal() {
        TileBlockSwapper partner = getPartnerTile();
        if (partner == null) {
            return 0;
        }

        lastBlockValidationCount = 0;
        lastEntitySwapCount = 0;
        thisValidationList.clear();
        thatValidationList.clear();
        if (swapBlocks) {
            swapTargetBlocks(partner);
            validateSwappedBlocks(partner);
        }
        if (swapEntityType != SwapEntityType.NONE.ordinal()) {
            swapEntities(partner);
        }
        int swapped = getLastSwapCostTargetCount();
        thisValidationList.clear();
        thatValidationList.clear();
        return swapped;
    }

    protected int swapTargetBlocks(TileBlockSwapper partner) {
        FakePlayer fakePlayer = createProtectionFakePlayer(world, this);
        FakePlayer partnerFakePlayer = createProtectionFakePlayer(partner.world, partner);
        List<BlockSwapEntry> entries = new ArrayList<>();
        for (BlockPos first : findSpotsToSwap()) {
            BlockPos second = partner.getWorldPos(getRelativePos(first));
            BlockSwapEntry entry = createBlockSwapEntry(first, second, partner, fakePlayer, partnerFakePlayer);
            if (entry != null) {
                entries.add(entry);
            }
        }
        applyBlockSwaps(partner, entries);
        return entries.size();
    }

    protected BlockSwapEntry createBlockSwapEntry(BlockPos first, BlockPos second, TileBlockSwapper partner, FakePlayer fakePlayer, FakePlayer partnerFakePlayer) {
        if (partner == null || first == null || second == null || first.equals(second) || !isBlockPosValid(world, first) || !isBlockPosValid(partner.world, second)) {
            return null;
        }
        ensureChunkLoaded(world, first);
        ensureChunkLoaded(partner.world, second);
        if (!canBreakAndPlaceAt(world, first, fakePlayer) || !canBreakAndPlaceAt(partner.world, second, partnerFakePlayer)) {
            return null;
        }

        IBlockState firstState = world.getBlockState(first);
        IBlockState secondState = partner.world.getBlockState(second);
        if (firstState.getBlock() == Blocks.AIR && secondState.getBlock() == Blocks.AIR) {
            return null;
        }

        NBTTagCompound firstNbt = saveTileNbt(world.getTileEntity(first));
        NBTTagCompound secondNbt = saveTileNbt(partner.world.getTileEntity(second));
        return new BlockSwapEntry(first, second, firstState, secondState, firstNbt, secondNbt);
    }

    protected void applyBlockSwaps(TileBlockSwapper partner, List<BlockSwapEntry> entries) {
        if (partner == null || entries == null || entries.isEmpty()) {
            return;
        }

        for (BlockSwapEntry entry : entries) {
            world.removeTileEntity(entry.first);
            partner.world.removeTileEntity(entry.second);
        }

        for (BlockSwapEntry entry : entries) {
            boolean placedSecond = placeSwappedState(partner.world, entry.second, entry.firstState);
            if (placedSecond && !entry.firstNbt.isEmpty()) {
                restoreTileNbt(ensureTileEntity(partner.world, entry.second, entry.firstState), entry.second, entry.firstNbt);
            }
            thatValidationList.add(entry.second);
        }

        for (BlockSwapEntry entry : entries) {
            boolean placedFirst = placeSwappedState(world, entry.first, entry.secondState);
            if (placedFirst && !entry.secondNbt.isEmpty()) {
                restoreTileNbt(ensureTileEntity(world, entry.first, entry.secondState), entry.first, entry.secondNbt);
            }
            thisValidationList.add(entry.first);
        }

        for (BlockSwapEntry entry : entries) {
            spawnTeleportParticles(entry.first);
            partner.spawnTeleportParticles(entry.second);
        }
    }

    protected void validateSwappedBlocks(TileBlockSwapper partner) {
        for (BlockPos blockPos : thisValidationList) {
            validateBlock(blockPos);
        }
        for (BlockPos blockPos : thatValidationList) {
            partner.validateBlock(blockPos);
        }
        lastBlockValidationCount = thisValidationList.size() + thatValidationList.size();
    }

    @Nullable
    protected FakePlayer createProtectionFakePlayer(World targetWorld, TileBlockSwapper machine) {
        if (!(targetWorld instanceof WorldServer) || machine == null) {
            return null;
        }
        return MachineActionHelper.createFakePlayer((WorldServer) targetWorld, machine);
    }

    protected boolean canBreakAndPlaceAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        return canBreakAt(targetWorld, blockPos, fakePlayer) && canPlaceAt(targetWorld, blockPos, fakePlayer);
    }

    protected boolean canBreakAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        return targetWorld != null && blockPos != null && fakePlayer != null;
    }

    protected boolean canPlaceAt(World targetWorld, BlockPos blockPos, FakePlayer fakePlayer) {
        if (targetWorld == null || blockPos == null || fakePlayer == null) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(targetWorld, blockPos);
        BlockEvent.PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(fakePlayer, snapshot, EnumFacing.UP, EnumHand.MAIN_HAND);
        return !event.isCanceled();
    }

    protected int swapEntities(TileBlockSwapper partner) {
        List<Entity> entities = matchingEntities(getAABB());
        int swapped = 0;
        for (Entity entity : entities) {
            Vec3d remotePosition = partner.getWorldPos(getRelativePos(new Vec3d(entity.posX, entity.posY, entity.posZ)));
            if (moveEntityToPartnerWorld(entity, partner, remotePosition)) {
                swapped++;
            }
        }
        lastEntitySwapCount = swapped;
        return swapped;
    }

    protected boolean moveEntityToPartnerWorld(Entity entity, TileBlockSwapper partner, Vec3d remotePosition) {
        if (entity == null || entity.isDead || partner == null || partner.world == null) {
            return false;
        }
        ensureChunkLoaded(partner.world, new BlockPos(remotePosition.x, remotePosition.y, remotePosition.z));
        if (isSameDimension(partner)) {
            entity.setPositionAndUpdate(remotePosition.x, remotePosition.y, remotePosition.z);
            entity.fallDistance = 0.0F;
            return true;
        }
        if (!(world instanceof WorldServer) || !(partner.world instanceof WorldServer)) {
            return false;
        }

        int targetDimension = partner.world.provider.getDimension();
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            Entity moved = PortalDimensionTransferRules.requiresDirectTeleporter(player.world.provider.getDimension(), targetDimension)
                    ? player.changeDimension(targetDimension, new PortalDirectTeleporter(remotePosition))
                    : player.changeDimension(targetDimension);
            if (!(moved instanceof EntityPlayerMP)) {
                return false;
            }
            player = (EntityPlayerMP) moved;
            player.connection.setPlayerLocation(remotePosition.x, remotePosition.y, remotePosition.z, player.rotationYaw, player.rotationPitch);
            player.fallDistance = 0.0F;
            return true;
        }

        Entity moved = PortalDimensionTransferRules.requiresDirectTeleporter(entity.world.provider.getDimension(), targetDimension)
                ? entity.changeDimension(targetDimension, new PortalDirectTeleporter(remotePosition))
                : entity.changeDimension(targetDimension);
        if (moved == null) {
            return false;
        }
        moved.setPositionAndUpdate(remotePosition.x, remotePosition.y, remotePosition.z);
        moved.fallDistance = 0.0F;
        return true;
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
        if (entity == null || entity.isDead || !entity.isEntityAlive()) {
            return false;
        }
        if (entity instanceof MultiPartEntityPart) {
            return false;
        }
        if (JDTEntityGroups.isTeleportingNotSupported(entity)) {
            return false;
        }
        TileBlockSwapper partner = getPartnerTile();
        if (partner == null) {
            return false;
        }
        return isSameDimension(partner) || entity.isNonBoss();
    }

    protected boolean canRun() {
        return getOperationTicks() == 0 || getRedstoneState().isPulseMode();
    }

    protected boolean canSwap() {
        return true;
    }

    protected int getLastSwapCostTargetCount() {
        return lastBlockValidationCount + lastEntitySwapCount;
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
        return isBlockPosValid(world, blockPos);
    }

    protected boolean isBlockPosValid(World targetWorld, BlockPos blockPos) {
        if (targetWorld == null || blockPos == null) {
            return false;
        }
        int targetDimension = targetWorld.provider.getDimension();
        if (targetDimension == getCurrentDimension() && blockPos.equals(pos)) {
            return false;
        }
        if (boundTo != null && targetDimension == getBoundDimension() && blockPos.equals(boundTo)) {
            return false;
        }
        IBlockState state = targetWorld.getBlockState(blockPos);
        if (state.getBlock() != Blocks.AIR && JDTBlockGroups.isSwapperDenied(state.getBlock())) {
            return false;
        }
        return state.getBlock() == Blocks.AIR || state.getBlockHardness(targetWorld, blockPos) >= 0.0F;
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
        IBlockState adjustedState = state.getBlock().getActualState(state, world, blockPos);
        if (!adjustedState.equals(state)) {
            world.setBlockState(blockPos, adjustedState, 3);
        } else {
            world.markAndNotifyBlock(blockPos, world.getChunk(blockPos), state, state, 3);
        }
        world.notifyNeighborsOfStateChange(blockPos, state.getBlock(), false);
    }

    protected void ensureChunkLoaded(World targetWorld, BlockPos blockPos) {
        if (targetWorld != null && blockPos != null && !targetWorld.isRemote) {
            targetWorld.getChunk(blockPos);
        }
    }

    protected void syncPartnerChunkTicket() {
        if (world == null || world.isRemote || boundTo == null || boundDimension == NO_BOUND_DIMENSION || boundDimension == getCurrentDimension()) {
            return;
        }
        World partnerWorld = getPartnerWorld();
        if (partnerWorld == null) {
            return;
        }
        PortalChunkKeeper.track(getChunkTicketId(), partnerWorld, boundTo);
    }

    protected void clearPartnerChunkTicket() {
        UUID chunkTicketId = getChunkTicketId();
        if (chunkTicketId != null) {
            PortalChunkKeeper.clear(chunkTicketId);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncPartnerChunkTicket();
    }

    @Override
    public void invalidate() {
        clearPartnerChunkTicket();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        clearPartnerChunkTicket();
        super.onChunkUnload();
    }

    protected UUID getChunkTicketId() {
        if (world == null || pos == null) {
            return null;
        }
        String ticketKey = getClass().getName() + "|" + getCurrentDimension() + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
        return UUID.nameUUIDFromBytes(ticketKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

    private static boolean placeSwappedState(World world, BlockPos blockPos, IBlockState state) {
        boolean placed = world.setBlockState(blockPos, state, 51);
        return placed || world.getBlockState(blockPos).equals(state);
    }

    private static TileEntity ensureTileEntity(World world, BlockPos blockPos, IBlockState state) {
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity != null || !state.getBlock().hasTileEntity(state)) {
            return tileEntity;
        }

        tileEntity = state.getBlock().createTileEntity(world, state);
        if (tileEntity == null && state.getBlock() instanceof ITileEntityProvider) {
            tileEntity = ((ITileEntityProvider) state.getBlock()).createNewTileEntity(world, state.getBlock().getMetaFromState(state));
        }
        if (tileEntity != null) {
            world.setTileEntity(blockPos, tileEntity);
        }
        return tileEntity;
    }

    protected static final class BlockSwapEntry {
        private final BlockPos first;
        private final BlockPos second;
        private final IBlockState firstState;
        private final IBlockState secondState;
        private final NBTTagCompound firstNbt;
        private final NBTTagCompound secondNbt;

        private BlockSwapEntry(BlockPos first, BlockPos second, IBlockState firstState, IBlockState secondState, NBTTagCompound firstNbt, NBTTagCompound secondNbt) {
            this.first = first;
            this.second = second;
            this.firstState = firstState;
            this.secondState = secondState;
            this.firstNbt = firstNbt;
            this.secondNbt = secondNbt;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (boundTo != null) {
            compound.setInteger("BoundX", boundTo.getX());
            compound.setInteger("BoundY", boundTo.getY());
            compound.setInteger("BoundZ", boundTo.getZ());
            compound.setInteger("BoundDimension", getBoundDimension());
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
            boundDimension = compound.hasKey("BoundDimension") ? compound.getInteger("BoundDimension") : getCurrentDimension();
        } else {
            boundTo = null;
            boundDimension = NO_BOUND_DIMENSION;
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
        protected boolean canSwap() {
            return hasEnoughEnergy(getEnergyCost(getAreaVolume()));
        }

        @Override
        protected boolean doSwap() {
            TileBlockSwapper partner = getPartnerTile();
            if (partner == null) {
                return false;
            }
            if (!canSwap()) {
                return false;
            }
            int swapped = doSwapInternal();
            if (swapped > 0) {
                consumeEnergy(getEnergyCost(swapped), false);
                playSwapSound(partner);
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
        public boolean handleConnection(int otherDimension, BlockPos otherPos) {
            boolean connected = super.handleConnection(otherDimension, otherPos);
            if (connected) {
                updatePartnerArea();
            }
            return connected;
        }

        @Override
        protected boolean isBlockPosValid(BlockPos blockPos) {
            return isBlockPosValid(world, blockPos);
        }

        @Override
        protected boolean isBlockPosValid(World targetWorld, BlockPos blockPos) {
            if (!super.isBlockPosValid(targetWorld, blockPos)) {
                return false;
            }
            if (targetWorld == world && isInBothAreas(blockPos)) {
                return false;
            }
            IBlockState state = targetWorld.getBlockState(blockPos);
            if (state.getBlock() == Blocks.AIR) {
                return true;
            }
            ItemStack blockStack = getBlockStackForState(state);
            return matchesFilter(blockStack);
        }

        @Override
        protected boolean isValidEntity(Entity entity) {
            return super.isValidEntity(entity)
                    && !isInBothAreas(new Vec3d(entity.posX, entity.posY, entity.posZ))
                    && MachineFilterHelper.matchesEntityFilter(getFilterHandler(), getFilterState(), entity, world);
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
            if (!(partner instanceof T2) || !isSameDimension(partner)) {
                return false;
            }
            AxisAlignedBB thisArea = getAABB();
            AxisAlignedBB partnerArea = ((T2) partner).getAABB();
            return thisArea.contains(new Vec3d(blockPos)) && partnerArea.contains(new Vec3d(blockPos));
        }

        protected boolean isInBothAreas(Vec3d vec3d) {
            TileBlockSwapper partner = getPartnerTile();
            if (!(partner instanceof T2) || !isSameDimension(partner)) {
                return false;
            }
            AxisAlignedBB thisArea = getAABB();
            AxisAlignedBB partnerArea = ((T2) partner).getAABB();
            return thisArea.contains(vec3d) && partnerArea.contains(vec3d);
        }

        private ItemStack getBlockStackForState(IBlockState state) {
            if (state.getBlock() instanceof net.minecraft.block.BlockLiquid) {
                if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) {
                    return new ItemStack(net.minecraft.init.Items.WATER_BUCKET);
                }
                if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA) {
                    return new ItemStack(net.minecraft.init.Items.LAVA_BUCKET);
                }
            }
            return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
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
