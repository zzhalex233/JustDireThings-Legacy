package com.zzhalex.justdirethings.common.block;

import com.zzhalex.justdirethings.Reference;
import com.zzhalex.justdirethings.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockSimpleContent extends Block {

    public BlockSimpleContent(String registryPath, Material material, SoundType soundType, float hardness, float resistance, int harvestLevel) {
        super(material);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, registryPath));
        setTranslationKey(Reference.MOD_ID + "." + registryPath);
        setCreativeTab(ModCreativeTabs.JUST_DIRE_THINGS);
        setSoundType(soundType);
        setHardness(hardness);
        setResistance(resistance);
        setHarvestLevel("pickaxe", harvestLevel);
    }
}
