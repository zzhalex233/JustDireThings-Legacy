package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockTimeCrystalBlock extends Block {

    public BlockTimeCrystalBlock() {
        this("time_crystal_block");
    }

    protected BlockTimeCrystalBlock(String registryPath) {
        super(Material.ROCK);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(JDTSoundTypes.AMETHYST);
        setHardness(1.5F);
        setResistance(1.5F);
        setHarvestLevel("pickaxe", 0);
    }
}
