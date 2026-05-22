package com.zzhalex.justdirethings.common.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public final class FluidDisplayHelper {

    private FluidDisplayHelper() {
    }

    public static String getLocalizedName(String fluidName, int amount) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        return fluid == null || amount <= 0 ? "" : new FluidStack(fluid, amount).getLocalizedName();
    }

    public static String getLocalizedName(Fluid fluid, int amount) {
        return fluid == null || amount <= 0 ? "" : new FluidStack(fluid, amount).getLocalizedName();
    }
}
