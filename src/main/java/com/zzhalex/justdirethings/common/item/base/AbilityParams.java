package com.zzhalex.justdirethings.common.item.base;

public final class AbilityParams {

    public final int minSlider;
    public final int maxSlider;
    public final int increment;
    public final int defaultValue;
    public final int activeCooldown;
    public final int cooldown;

    public AbilityParams(int minSlider, int maxSlider, int increment) {
        this(minSlider, maxSlider, increment, maxSlider);
    }

    public AbilityParams(int minSlider, int maxSlider, int increment, int defaultValue) {
        this(minSlider, maxSlider, increment, defaultValue, -1, -1);
    }

    public AbilityParams(int minSlider, int maxSlider, int increment, int defaultValue, int activeCooldown, int cooldown) {
        this.minSlider = minSlider;
        this.maxSlider = maxSlider;
        this.increment = increment;
        this.defaultValue = defaultValue;
        this.activeCooldown = activeCooldown;
        this.cooldown = cooldown;
    }
}
