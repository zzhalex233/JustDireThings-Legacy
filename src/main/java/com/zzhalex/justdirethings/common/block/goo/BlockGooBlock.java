package com.zzhalex.justdirethings.common.block.goo;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.tile.goo.TileGooBlock;
import com.zzhalex.justdirethings.registry.ModContentItems;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockGooBlock extends Block implements ITileEntityProvider {

    public static final PropertyBool ALIVE = PropertyBool.create("alive");

    private final int tier;

    public BlockGooBlock(String registryPath, int tier) {
        super(Material.CLAY);
        this.tier = tier;
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(SoundType.SLIME);
        setHardness(2.0F);
        setResistance(2.0F);
        setDefaultState(blockState.getBaseState().withProperty(ALIVE, false));
    }

    public int getTier() {
        return tier;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = playerIn.getHeldItem(hand);
        if (!state.getValue(ALIVE) && validRevivalItem(held)) {
            if (!worldIn.isRemote) {
                worldIn.setBlockState(pos, state.withProperty(ALIVE, true), 3);
                worldIn.playSound(null, pos, SoundType.SLIME.getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 0.5F);
                if (!playerIn.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
            return true;
        }
        return false;
    }

    public boolean validRevivalItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        switch (tier) {
            case 1:
                return item == Items.SUGAR || item == Items.ROTTEN_FLESH;
            case 2:
                return item == Items.NETHER_WART || item == Items.BLAZE_POWDER;
            case 3:
                return item == Items.CHORUS_FRUIT || item == Items.ENDER_PEARL;
            case 4:
                return isItem(item, "futuremc:sculk")
                        || isItem(item, "futuremc:sculk_catalyst")
                        || item == Items.NETHER_STAR
                        || item == Items.ENDER_EYE
                        || item == ModContentItems.getItem("time_crystal")
                        || item == ModContentItems.getItem("eclipsealloy_ingot");
            default:
                return false;
        }
    }

    private static boolean isItem(Item item, String registryName) {
        Item registered = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        return registered != null && item == registered;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        switch (tier) {
            case 2:
                return new TileGooBlock.Tier2();
            case 3:
                return new TileGooBlock.Tier3();
            case 4:
                return new TileGooBlock.Tier4();
            case 1:
            default:
                return new TileGooBlock.Tier1();
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, getMetaFromState(state));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ALIVE, (meta & 1) == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ALIVE) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ALIVE);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }
}
