package com.zzhalex.justdirethings.data.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ToolState {

    private boolean enabled = true;
    private final Set<String> installedAbilities = new LinkedHashSet<>();
    private final Map<String, Integer> abilityValues = new LinkedHashMap<>();
    private final Map<String, Integer> abilityCustomSettings = new LinkedHashMap<>();
    private final Map<String, Integer> abilityBindingModes = new LinkedHashMap<>();
    private final List<AbilityBinding> abilityBindings = new ArrayList<>();
    private final List<String> leftClickAbilities = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getInstalledAbilities() {
        return installedAbilities;
    }

    public Map<String, Integer> getAbilityValues() {
        return abilityValues;
    }

    public Map<String, Integer> getAbilityCustomSettings() {
        return abilityCustomSettings;
    }

    public Map<String, Integer> getAbilityBindingModes() {
        return abilityBindingModes;
    }

    public List<AbilityBinding> getAbilityBindings() {
        return abilityBindings;
    }

    public List<String> getLeftClickAbilities() {
        return leftClickAbilities;
    }

    public boolean hasInstalledAbility(String abilityId) {
        return installedAbilities.contains(abilityId);
    }
}
