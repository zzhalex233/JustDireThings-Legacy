package com.zzhalex.justdirethings.common.fluid;

public final class JDTFluidDefinition {

    private final String id;
    private final int color;
    private final int luminosity;
    private final int density;
    private final int viscosity;
    private final int temperature;

    public JDTFluidDefinition(String id, int color, int luminosity, int density, int viscosity, int temperature) {
        this.id = id;
        this.color = color;
        this.luminosity = luminosity;
        this.density = density;
        this.viscosity = viscosity;
        this.temperature = temperature;
    }

    public String getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public int getLuminosity() {
        return luminosity;
    }

    public int getDensity() {
        return density;
    }

    public int getViscosity() {
        return viscosity;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getBlockId() {
        return id + "_block";
    }
}
