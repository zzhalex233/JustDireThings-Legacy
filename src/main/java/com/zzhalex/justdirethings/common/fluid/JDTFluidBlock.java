package com.zzhalex.justdirethings.common.fluid;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class JDTFluidBlock extends BlockFluidClassic {

    public JDTFluidBlock(JDTFluidDefinition definition, Fluid fluid) {
        super(fluid, Material.WATER);
        setRegistryName(new ResourceLocation(Reference.MOD_ID, definition.getBlockId()));
        setTranslationKey(Reference.MOD_ID + "." + definition.getBlockId());
        setRenderLayer(BlockRenderLayer.TRANSLUCENT);
    }
}
