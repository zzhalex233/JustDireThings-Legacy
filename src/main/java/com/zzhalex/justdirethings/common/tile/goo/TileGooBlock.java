package com.zzhalex.justdirethings.common.tile.goo;

import com.zzhalex.justdirethings.common.block.goo.BlockGooBlock;
import com.zzhalex.justdirethings.common.recipe.custom.GooFluidRecipeRuntime;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadDataRecipe;
import com.zzhalex.justdirethings.common.recipe.custom.GooSpreadTagDataRecipe;
import com.zzhalex.justdirethings.config.JDTConfig;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.block.SoundType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class TileGooBlock extends TileEntity implements ITickable {

    public final Map<EnumFacing, Integer> sideCounters = new EnumMap<>(EnumFacing.class);
    public final Map<EnumFacing, Integer> sideDurations = new EnumMap<>(EnumFacing.class);
    public final Map<IBlockState, IBlockState> outputCache = new java.util.HashMap<>();
    public final Map<IBlockState, Integer> durationCache = new java.util.HashMap<>();

    private final int tier;

    public TileGooBlock() {
        this(1);
    }

    protected TileGooBlock(int tier) {
        this.tier = tier;
        for (EnumFacing facing : EnumFacing.values()) {
            sideCounters.put(facing, -1);
            sideDurations.put(facing, -1);
        }
    }

    @Override
    public void update() {
        if (world == null) {
            return;
        }
        if (world.isRemote) {
            tickClient();
        } else {
            tickServer();
        }
    }

    public void tickClient() {
        tickCounters();
    }

    public void tickServer() {
        checkSides();
        tickCounters();
        markDirty();
    }

    public int counterReducer() {
        switch (tier) {
            case 2:
                return 2;
            case 3:
                return 5;
            case 4:
                return 10;
            case 1:
            default:
                return 1;
        }
    }

    public int getTier() {
        return tier;
    }

    public int getCraftingDuration(EnumFacing facing) {
        return sideDurations.get(facing);
    }

    public int getRemainingTimeFor(EnumFacing facing) {
        return sideCounters.get(facing);
    }

    public boolean updateSideCounter(EnumFacing facing, int newCounter) {
        int oldCounter = sideCounters.get(facing);
        sideCounters.put(facing, newCounter);
        if (oldCounter >= 0 && newCounter == -1 && world != null && world.isRemote) {
            spawnParticles(facing);
        }
        int duration = sideDurations.get(facing);
        return newCounter > 0 && duration > 0 && newCounter % (60 * counterReducer()) == 0;
    }

    public void tickCounters() {
        boolean updateClient = false;
        for (EnumFacing facing : EnumFacing.values()) {
            int counter = sideCounters.get(facing);
            if (counter > 0) {
                int nextCounter = Math.max(counter - counterReducer(), 0);
                boolean sideChanged = updateSideCounter(facing, nextCounter);
                if (nextCounter == 0 || sideChanged) {
                    updateClient = true;
                }
            }
        }
        if (updateClient && !world.isRemote) {
            markDirtyClient();
        }
    }

    public void checkSides() {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos targetPos = pos.offset(facing);
            IBlockState input = world.getBlockState(targetPos);
            IBlockState output = findOutput(input);
            int duration = findDuration(input);
            int sideCounter = sideCounters.get(facing);

            if (output != null && output.getBlock() != Blocks.AIR) {
                if (sideCounter == -1 && isAlive()) {
                    sideDurations.put(facing, duration);
                    updateSideCounter(facing, duration);
                    markDirtyClient();
                } else if (sideCounter == 0) {
                    setBlockToTarget(output, facing);
                }
            } else if (sideCounter != -1) {
                updateSideCounter(facing, -1);
                sideDurations.put(facing, -1);
                markDirtyClient();
            }
        }
    }

    public IBlockState findOutput(IBlockState input) {
        if (!outputCache.containsKey(input)) {
            populateCaches(input);
        }
        return outputCache.get(input);
    }

    public int findDuration(IBlockState input) {
        if (!durationCache.containsKey(input)) {
            populateCaches(input);
        }
        Integer duration = durationCache.get(input);
        return duration == null ? -1 : duration;
    }

    public void populateCaches(IBlockState input) {
        GooSpreadDataRecipe recipe = findGooSpreadRecipe(input);
        GooSpreadTagDataRecipe tagRecipe = recipe == null ? findGooSpreadTagRecipe(input) : null;
        outputCache.put(input, outputState(recipe, tagRecipe));
        durationCache.put(input, duration(recipe, tagRecipe));
    }

    @Nullable
    public GooSpreadDataRecipe findGooSpreadRecipe(IBlockState state) {
        return GooFluidRecipeRuntime.findGooSpreadRecipe(state, getTier());
    }

    @Nullable
    public GooSpreadTagDataRecipe findGooSpreadTagRecipe(IBlockState state) {
        return GooFluidRecipeRuntime.findGooSpreadTagRecipe(state, getTier());
    }

    public void setBlockToTarget(IBlockState output, EnumFacing facing) {
        IBlockState target = withFacing(output, facing);
        world.setBlockState(pos.offset(facing), target, 3);
        updateSideCounter(facing, -1);
        sideDurations.put(facing, -1);
        world.playSound(null, pos, SoundType.SLIME.getBreakSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
        killGoo();
        markDirtyClient();
    }

    public IBlockState getRenderStateFor(EnumFacing facing) {
        if (world == null || getRemainingTimeFor(facing) < 0) {
            return Blocks.AIR.getDefaultState();
        }
        return findOutput(world.getBlockState(pos.offset(facing)));
    }

    public void spawnParticles(EnumFacing side) {
        if (world == null || !world.isRemote) {
            return;
        }
        BlockPos startPos = pos.offset(side);
        IBlockState particleState = world.getBlockState(pos);
        int particleId = Block.getStateId(particleState);
        for (EnumFacing particleSide : EnumFacing.values()) {
            for (int i = 0; i < 16; i++) {
                double x = startPos.getX() + 0.5D + 0.6D * particleSide.getXOffset()
                        + (particleSide.getXOffset() == 0 ? world.rand.nextDouble() - 0.5D : 0.0D);
                double y = startPos.getY() + 0.5D + 0.6D * particleSide.getYOffset()
                        + (particleSide.getYOffset() == 0 ? world.rand.nextDouble() - 0.5D : 0.0D);
                double z = startPos.getZ() + 0.5D + 0.6D * particleSide.getZOffset()
                        + (particleSide.getZOffset() == 0 ? world.rand.nextDouble() - 0.5D : 0.0D);
                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, x, y, z, 0.0D, 0.0D, 0.0D, particleId);
            }
        }
    }

    private boolean isAlive() {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BlockGooBlock && state.getValue(BlockGooBlock.ALIVE);
    }

    private void killGoo() {
        if (!JDTConfig.gooCanDie) {
            return;
        }
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockGooBlock
                && state.getValue(BlockGooBlock.ALIVE)
                && world.rand.nextFloat() < JDTConfig.gooDeathChance) {
            world.setBlockState(pos, state.withProperty(BlockGooBlock.ALIVE, false), 3);
            world.playSound(null, pos, SoundType.SLIME.getBreakSound(), SoundCategory.BLOCKS, 1.0F, 0.25F);
        }
    }

    private static IBlockState outputState(GooSpreadDataRecipe recipe, GooSpreadTagDataRecipe tagRecipe) {
        if (recipe != null) {
            return recipe.getOutput().toBlockState();
        }
        return tagRecipe == null ? Blocks.AIR.getDefaultState() : tagRecipe.getOutput().toBlockState();
    }

    private static int duration(GooSpreadDataRecipe recipe, GooSpreadTagDataRecipe tagRecipe) {
        if (recipe != null) {
            return recipe.getCraftingDuration();
        }
        return tagRecipe == null ? -1 : tagRecipe.getCraftingDuration();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withFacing(IBlockState state, EnumFacing facing) {
        for (IProperty property : state.getPropertyKeys()) {
            if (!"facing".equals(property.getName())) {
                continue;
            }
            for (Object allowed : property.getAllowedValues()) {
                Comparable comparable = (Comparable) allowed;
                if (facing.getName().equals(property.getName(comparable))) {
                    return state.withProperty(property, comparable);
                }
            }
        }
        return state;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("sideCounters", writeSideMap(sideCounters, "counter"));
        compound.setTag("sideDurations", writeSideMap(sideDurations, "duration"));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readSideCounters(compound.getTagList("sideCounters", Constants.NBT.TAG_COMPOUND));
        readSideMap(compound.getTagList("sideDurations", Constants.NBT.TAG_COMPOUND), sideDurations, "duration");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    private void markDirtyClient() {
        markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private static NBTTagList writeSideMap(Map<EnumFacing, Integer> map, String valueKey) {
        NBTTagList list = new NBTTagList();
        for (EnumFacing facing : EnumFacing.values()) {
            NBTTagCompound side = new NBTTagCompound();
            side.setInteger("side", facing.ordinal());
            side.setInteger(valueKey, map.get(facing));
            list.appendTag(side);
        }
        return list;
    }

    private static void readSideMap(NBTTagList list, Map<EnumFacing, Integer> map, String valueKey) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound side = list.getCompoundTagAt(i);
            int ordinal = side.getInteger("side");
            if (ordinal >= 0 && ordinal < EnumFacing.values().length) {
                map.put(EnumFacing.values()[ordinal], side.getInteger(valueKey));
            }
        }
    }

    private void readSideCounters(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound side = list.getCompoundTagAt(i);
            int ordinal = side.getInteger("side");
            if (ordinal >= 0 && ordinal < EnumFacing.values().length) {
                updateSideCounter(EnumFacing.values()[ordinal], side.getInteger("counter"));
            }
        }
    }

    public static class Tier1 extends TileGooBlock {
        public Tier1() {
            super(1);
        }
    }

    public static class Tier2 extends TileGooBlock {
        public Tier2() {
            super(2);
        }
    }

    public static class Tier3 extends TileGooBlock {
        public Tier3() {
            super(3);
        }
    }

    public static class Tier4 extends TileGooBlock {
        public Tier4() {
            super(4);
        }
    }
}
