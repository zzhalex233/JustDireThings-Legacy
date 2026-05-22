package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.config.JDTConfig;

import java.util.Locale;

public final class AbilityAvailability {

    private AbilityAvailability() {
    }

    public static boolean isAvailable(Ability ability) {
        if (ability == null) {
            return false;
        }
        if (JDTConfig.disabledAbilities == null) {
            return true;
        }
        for (String disabled : JDTConfig.disabledAbilities) {
            if (ability.getId().equals(normalize(disabled))) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String abilityId) {
        return abilityId == null ? "" : abilityId.trim().toLowerCase(Locale.ROOT);
    }
}
