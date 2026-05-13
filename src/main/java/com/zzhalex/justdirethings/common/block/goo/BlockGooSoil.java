package com.zzhalex.justdirethings.common.block.goo;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.tile.goo.TileGooSoil;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class BlockGooSoil extends BlockFarmland implements ITileEntityProvider {

    private final int tier;

    public BlockGooSoil(String registryPath, int tier) {
        super();
        this.tier = tier;
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(SoundType.GROUND);
        setHardness(2.0F);
        setResistance(2.0F);
        setTickRandomly(true);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        return direction == EnumFacing.UP && canSustainGooPlant(world, pos, plantable);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        if (!worldIn.isRemote) {
            for (int i = 0; i < tier; i++) {
                bonemealMe(worldIn, pos, rand);
            }
            if (tier >= 2) {
                autoHarvest(worldIn, pos);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote && tier >= 2 && pos.up().equals(fromPos)) {
            autoHarvest(worldIn, pos);
        }
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        entityIn.fall(fallDistance, 1.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return tier >= 3;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return tier >= 3 ? new TileGooSoil() : null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return tier >= 3 ? new TileGooSoil() : null;
    }

    public static void bonemealMe(World world, BlockPos soilPos, Random random) {
        IBlockState soilState = world.getBlockState(soilPos);
        if (!(soilState.getBlock() instanceof BlockGooSoil) || soilState.getValue(MOISTURE) < 7) {
            return;
        }

        BlockPos cropPos = soilPos.up();
        IBlockState cropState = world.getBlockState(cropPos);
        Block crop = cropState.getBlock();
        if (crop instanceof IGrowable) {
            IGrowable growable = (IGrowable) crop;
            if (growable.canGrow(world, cropPos, cropState, false)
                    && growable.canUseBonemeal(world, random, cropPos, cropState)) {
                growable.grow(world, random, cropPos, cropState);
            }
            return;
        }

        if (crop instanceof BlockReed || crop instanceof BlockCactus) {
            growStackingPlant(world, cropPos, cropState);
        }
    }

    private static boolean canSustainGooPlant(IBlockAccess world, BlockPos pos, IPlantable plantable) {
        IBlockState plantState = plantable.getPlant(world, pos.up());
        Block plantBlock = plantState.getBlock();
        EnumPlantType plantType = plantable.getPlantType(world, pos.up());
        return plantBlock instanceof BlockCactus
                || plantBlock instanceof BlockReed
                || plantBlock == Blocks.NETHER_WART
                || plantType == EnumPlantType.Crop
                || plantType == EnumPlantType.Plains
                || plantType == EnumPlantType.Desert
                || plantType == EnumPlantType.Beach
                || plantType == EnumPlantType.Nether;
    }

    private static void growStackingPlant(World world, BlockPos cropPos, IBlockState cropState) {
        PropertyInteger ageProperty = findAgeProperty(cropState);
        if (ageProperty == null) {
            return;
        }
        int age = cropState.getValue(ageProperty);
        if (age >= 15) {
            BlockPos top = cropPos;
            while (world.getBlockState(top.up()).getBlock() == cropState.getBlock()) {
                top = top.up();
            }
            if (world.isAirBlock(top.up())) {
                world.setBlockState(top.up(), cropState.getBlock().getDefaultState(), 3);
                world.setBlockState(cropPos, cropState.withProperty(ageProperty, 0), 4);
            }
        } else {
            world.setBlockState(cropPos, cropState.withProperty(ageProperty, age + 1), 4);
        }
    }

    private static PropertyInteger findAgeProperty(IBlockState state) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property instanceof PropertyInteger && "age".equals(property.getName())) {
                return (PropertyInteger) property;
            }
        }
        return null;
    }

    private static void autoHarvest(World world, BlockPos soilPos) {
        BlockPos cropPos = soilPos.up();
        IBlockState cropState = world.getBlockState(cropPos);
        Block crop = cropState.getBlock();
        if (crop instanceof BlockCrops && ((BlockCrops) crop).isMaxAge(cropState)) {
            dropOrTeleportDrops(world, soilPos, cropPos, cropState);
            world.destroyBlock(cropPos, false);
            world.setBlockState(cropPos, crop.getDefaultState(), 3);
        } else if (crop instanceof BlockNetherWart && cropState.getValue(BlockNetherWart.AGE) >= 3) {
            dropOrTeleportDrops(world, soilPos, cropPos, cropState);
            world.destroyBlock(cropPos, false);
            world.setBlockState(cropPos, crop.getDefaultState(), 3);
        } else if (crop instanceof BlockReed || crop instanceof BlockCactus) {
            harvestTallPlant(world, soilPos, cropPos, crop);
        }
    }

    private static void harvestTallPlant(World world, BlockPos soilPos, BlockPos cropPos, Block crop) {
        BlockPos second = cropPos.up();
        if (world.getBlockState(second).getBlock() != crop) {
            return;
        }
        BlockPos top = second;
        while (world.getBlockState(top.up()).getBlock() == crop) {
            top = top.up();
        }
        for (BlockPos current = top; current.getY() >= second.getY(); current = current.down()) {
            dropOrTeleportDrops(world, soilPos, current, world.getBlockState(current));
            world.destroyBlock(current, false);
        }
    }

    private static void dropOrTeleportDrops(World world, BlockPos soilPos, BlockPos cropPos, IBlockState cropState) {
        TileEntity tileEntity = world.getTileEntity(soilPos);
        java.util.List<ItemStack> drops = cropState.getBlock().getDrops(world, cropPos, cropState, 0);
        if (tileEntity instanceof TileGooSoil) {
            ((TileGooSoil) tileEntity).handleDrops(drops, cropPos);
        } else {
            for (ItemStack drop : drops) {
                Block.spawnAsEntity(world, cropPos, drop);
            }
        }
    }
}
