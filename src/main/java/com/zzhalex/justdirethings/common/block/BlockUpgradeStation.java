package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.common.tile.TileUpgradeStation;
import com.zzhalex.justdirethings.registry.ModContainers;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockUpgradeStation extends Block implements ITileEntityProvider {

    public BlockUpgradeStation() {
        super(Material.IRON);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, "upgrade_station"));
        setTranslationKey(Reference.MOD_ID + ".upgrade_station");
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setHardness(3.5F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }

        playerIn.openGui(JustDireThingsLegacy.INSTANCE, ModContainers.GUI_UPGRADE_STATION, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileUpgradeStation();
    }
}
