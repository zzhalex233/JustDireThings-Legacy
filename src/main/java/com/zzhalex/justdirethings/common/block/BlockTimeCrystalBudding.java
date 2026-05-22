package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.config.JDTConfig;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import com.zzhalex.justdirethings.registry.ModContentBlocks;
import com.zzhalex.justdirethings.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Locale;
import java.util.Random;

public class BlockTimeCrystalBudding extends BlockTimeCrystalBlock {

    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 3);
    private static final EnumFacing[] DIRECTIONS = EnumFacing.values();

    public BlockTimeCrystalBudding() {
        super("time_crystal_budding_block");
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(JDTSoundTypes.AMETHYST);
        setHardness(1.5F);
        setResistance(1.5F);
        setHarvestLevel(null, -1);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(STAGE, 0));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, Math.max(0, Math.min(3, meta)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }

        int stage = state.getValue(STAGE);
        int advanceTo = JDTConfig.timeCrystalCustomDimensions
                ? canAdvanceToCustom(worldIn, state)
                : canAdvanceTo(worldIn, state);
        if (advanceTo != -1) {
            advance(worldIn, state, pos, advanceTo);
        }

        if (stage != 3 || rand.nextInt(5) != 0) {
            return;
        }

        EnumFacing direction = DIRECTIONS[rand.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.offset(direction);
        IBlockState targetState = worldIn.getBlockState(targetPos);
        Block nextBlock = getNextClusterBlock(targetState, direction);
        if (nextBlock == null) {
            return;
        }

        worldIn.setBlockState(targetPos, nextBlock.getDefaultState().withProperty(BlockTimeCrystalCluster.FACING, direction), 3);

        if (worldIn.rand.nextFloat() < 0.05F) {
            worldIn.setBlockState(pos, state.withProperty(STAGE, 0), 3);
            worldIn.playSound(null, pos, ModSounds.RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, 1.0F, 0.25F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(STAGE) == 3) {
            return;
        }

        int advanceTo = JDTConfig.timeCrystalCustomDimensions
                ? canAdvanceToCustom(worldIn, stateIn)
                : canAdvanceTo(worldIn, stateIn);
        if (advanceTo == -1) {
            return;
        }

        double r;
        double g;
        double b;
        if (advanceTo == 1) {
            r = 0.25D;
            g = 0.55D;
            b = 1.0D;
        } else if (advanceTo == 2) {
            r = 1.0D;
            g = 0.33D;
            b = 0.0D;
        } else if (advanceTo == 3) {
            r = 0.4D;
            g = 1.0D;
            b = 0.5D;
        } else {
            return;
        }

        for (int i = 0; i < 3; i++) {
            double offsetX = rand.nextBoolean() ? -0.5D + rand.nextDouble() * 0.5D : 1.0D + rand.nextDouble() * 0.5D;
            double offsetY = rand.nextBoolean() ? -0.5D + rand.nextDouble() * 0.5D : 1.0D + rand.nextDouble() * 0.5D;
            double offsetZ = rand.nextBoolean() ? -0.5D + rand.nextDouble() * 0.5D : 1.0D + rand.nextDouble() * 0.5D;
            JustDireThingsLegacy.proxy.spawnTimeCrystalChargeParticle(
                    worldIn,
                    pos.getX() + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + offsetZ,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    (float) r,
                    (float) g,
                    (float) b);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
        return false;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    public int canAdvanceToCustom(World world, IBlockState state) {
        int stage = state.getValue(STAGE);
        if (stage == 0 && dimensionAllowed(world, JDTConfig.timeCrystalStage1Dimensions)) {
            return 1;
        }
        if (stage == 1 && dimensionAllowed(world, JDTConfig.timeCrystalStage2Dimensions)) {
            return 2;
        }
        if (stage == 2 && dimensionAllowed(world, JDTConfig.timeCrystalStage3Dimensions)) {
            return 3;
        }
        return -1;
    }

    public int canAdvanceTo(World world, IBlockState state) {
        int stage = state.getValue(STAGE);
        DimensionType dimensionType = world.provider.getDimensionType();
        if (stage == 0 && dimensionType != DimensionType.NETHER && dimensionType != DimensionType.THE_END) {
            return 1;
        }
        if (stage == 1 && dimensionType == DimensionType.NETHER) {
            return 2;
        }
        if (stage == 2 && dimensionType == DimensionType.THE_END) {
            return 3;
        }
        return -1;
    }

    public void advance(World world, IBlockState state, BlockPos pos, int advanceTo) {
        world.setBlockState(pos, state.withProperty(STAGE, advanceTo), 3);
        world.playSound(null, pos, ModSounds.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 0.25F);
    }

    private Block getNextClusterBlock(IBlockState targetState, EnumFacing direction) {
        Block targetBlock = targetState.getBlock();
        if (canClusterGrowAtState(targetState)) {
            return ModContentBlocks.TIME_CRYSTAL_CLUSTER_SMALL;
        }
        if (targetBlock == ModContentBlocks.TIME_CRYSTAL_CLUSTER_SMALL && targetState.getValue(BlockTimeCrystalCluster.FACING) == direction) {
            return ModContentBlocks.TIME_CRYSTAL_CLUSTER_MEDIUM;
        }
        if (targetBlock == ModContentBlocks.TIME_CRYSTAL_CLUSTER_MEDIUM && targetState.getValue(BlockTimeCrystalCluster.FACING) == direction) {
            return ModContentBlocks.TIME_CRYSTAL_CLUSTER_LARGE;
        }
        if (targetBlock == ModContentBlocks.TIME_CRYSTAL_CLUSTER_LARGE && targetState.getValue(BlockTimeCrystalCluster.FACING) == direction) {
            return ModContentBlocks.TIME_CRYSTAL_CLUSTER;
        }
        return null;
    }

    private boolean canClusterGrowAtState(IBlockState state) {
        if (state.getBlock() == Blocks.AIR) {
            return true;
        }
        if ((state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER)
                && state.getPropertyKeys().contains(BlockLiquid.LEVEL)) {
            return state.getValue(BlockLiquid.LEVEL) == 0;
        }
        return false;
    }

    private boolean dimensionAllowed(World world, String[] allowedDimensions) {
        if (allowedDimensions == null || allowedDimensions.length == 0) {
            return false;
        }
        int dimensionId = world.provider.getDimension();
        String typeName = world.provider.getDimensionType().getName().toLowerCase(Locale.ROOT);
        String[] candidates = {
                String.valueOf(dimensionId),
                typeName,
                "minecraft:" + typeName,
                legacyDimensionName(dimensionId)
        };
        for (String allowed : allowedDimensions) {
            if (allowed == null) {
                continue;
            }
            String normalized = allowed.trim().toLowerCase(Locale.ROOT);
            for (String candidate : candidates) {
                if (candidate != null && normalized.equals(candidate)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String legacyDimensionName(int dimensionId) {
        if (dimensionId == 0) {
            return "minecraft:overworld";
        }
        if (dimensionId == -1) {
            return "minecraft:the_nether";
        }
        if (dimensionId == 1) {
            return "minecraft:the_end";
        }
        return null;
    }
}
