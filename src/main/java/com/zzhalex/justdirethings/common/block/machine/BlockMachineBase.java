package com.zzhalex.justdirethings.common.block.machine;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.item.equipment.ItemJDTWrench;
import com.zzhalex.justdirethings.common.item.misc.ItemMachineSettingsCopier;
import com.zzhalex.justdirethings.common.tile.base.TileMachineBase;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BlockMachineBase extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = BlockDirectional.FACING;

    private final int guiId;

    protected BlockMachineBase(String registryPath, int guiId) {
        super(Material.IRON);
        this.guiId = guiId;
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setHardness(3.5F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
        IBlockState baseState = blockState.getBaseState();
        setDefaultState(baseState.getPropertyKeys().contains(FACING) ? baseState.withProperty(FACING, EnumFacing.NORTH) : baseState);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (!heldStack.isEmpty() && (heldStack.getItem() instanceof ItemJDTWrench || heldStack.getItem() instanceof ItemMachineSettingsCopier)) {
            return false;
        }
        if (worldIn.isRemote) {
            return true;
        }

        playerIn.openGui(JustDireThingsLegacy.INSTANCE, guiId, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileMachineBase) {
            TileMachineBase machine = (TileMachineBase) tileEntity;
            if (state.getPropertyKeys().contains(FACING)) {
                machine.setDirection(state.getValue(FACING).getIndex());
            }
            if (placer instanceof EntityPlayer) {
                machine.setOwnerUuid(((EntityPlayer) placer).getUniqueID());
            }
            machine.markDirtyClient();
        }
    }

    @Override
    public IBlockState getStateForPlacement(
            World worldIn,
            BlockPos pos,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ,
            int meta,
            EntityLivingBase placer
    ) {
        IBlockState state = getDefaultState();
        return state.getPropertyKeys().contains(FACING) ? state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)) : state;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        return state.getPropertyKeys().contains(FACING) ? state.withProperty(FACING, EnumFacing.byIndex(meta)) : state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getPropertyKeys().contains(FACING) ? state.getValue(FACING).getIndex() : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.getPropertyKeys().contains(FACING) ? state.withProperty(FACING, rot.rotate(state.getValue(FACING))) : state;
    }

    public IBlockState direRotate(IBlockState state, World world, BlockPos pos, Rotation rotation) {
        return direRotate(state, rotation);
    }

    public IBlockState direRotate(IBlockState state, Rotation rotation) {
        if (!state.getPropertyKeys().contains(FACING)) {
            return state.withRotation(rotation);
        }
        List<EnumFacing> directions = new ArrayList<>(FACING.getAllowedValues());
        int currentDirectionIndex = directions.indexOf(state.getValue(FACING));
        int nextDirectionIndex = (currentDirectionIndex + 1) % directions.size();
        return state.withProperty(FACING, directions.get(nextDirectionIndex));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.getPropertyKeys().contains(FACING) ? state.withRotation(mirrorIn.toRotation(state.getValue(FACING))) : state;
    }

    @Nullable
    @Override
    public abstract TileEntity createNewTileEntity(World worldIn, int meta);
}
