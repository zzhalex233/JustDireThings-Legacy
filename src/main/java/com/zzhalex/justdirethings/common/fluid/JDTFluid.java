package com.zzhalex.justdirethings.common.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class JDTFluid extends Fluid {

    private final JDTFluidDefinition definition;

    public JDTFluid(JDTFluidDefinition definition, ResourceLocation still, ResourceLocation flowing) {
        super(definition.getId(), still, flowing);
        this.definition = definition;
        setColor(definition.getColor());
        setLuminosity(definition.getLuminosity());
        setDensity(definition.getDensity());
        setViscosity(definition.getViscosity());
        setTemperature(definition.getTemperature());
        setUnlocalizedName(definition.getId());
    }

    public JDTFluidDefinition getDefinition() {
        return definition;
    }
}
