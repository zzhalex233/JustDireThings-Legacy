package com.zzhalex.justdirethings.data.tool;

public final class AbilityCooldown {

    private final String abilityId;
    private final int remainingTicks;
    private final boolean active;

    public AbilityCooldown(String abilityId, int remainingTicks, boolean active) {
        this.abilityId = abilityId;
        this.remainingTicks = remainingTicks;
        this.active = active;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public int getRemainingTicks() {
        return remainingTicks;
    }

    public boolean isActive() {
        return active;
    }

}
