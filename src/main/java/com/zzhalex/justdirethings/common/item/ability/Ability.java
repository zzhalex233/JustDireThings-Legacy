package com.zzhalex.justdirethings.common.item.ability;

import com.zzhalex.justdirethings.Reference;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public enum Ability {
    MOBSCANNER(1, SettingType.TOGGLE, 10, 500, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    OREMINER(1, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    ORESCANNER(1, SettingType.TOGGLE, 10, 500, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    LAWNMOWER(1, SettingType.TOGGLE, 1, 50, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    SKYSWEEPER(1, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    TREEFELLER(1, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    LEAFBREAKER(1, SettingType.TOGGLE, 1, 50, UseType.USE_ON, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    RUNSPEED(1, SettingType.SLIDER, 1, 5, UseType.PASSIVE_TICK, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    WALKSPEED(1, SettingType.SLIDER, 1, 5, UseType.PASSIVE_TICK, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    STEPHEIGHT(1, SettingType.TOGGLE, 1, 5, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    JUMPBOOST(1, SettingType.SLIDER, 1, 5, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    MINDFOG(1, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    INVULNERABILITY(1, SettingType.SLIDER, 25, 5000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    POTIONARROW(1, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),

    SMELTER(2, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    SMOKER(2, SettingType.TOGGLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    HAMMER(2, SettingType.CYCLE, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    LAVAREPAIR(2, SettingType.TOGGLE, 0, 0, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    CAUTERIZEWOUNDS(2, SettingType.TOGGLE, 30, 1500, UseType.USE_COOLDOWN, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    AIRBURST(2, SettingType.SLIDER, 1, 250, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    SWIMSPEED(2, SettingType.SLIDER, 1, 5, UseType.PASSIVE_TICK, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    GROUNDSTOMP(2, SettingType.SLIDER, 25, 5000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    EXTINGUISH(2, SettingType.SLIDER, 25, 5000, UseType.PASSIVE_TICK_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    STUPEFY(2, SettingType.SLIDER, 25, 5000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    SPLASH(2, SettingType.TOGGLE, 20, 250, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    POLYMORPH_RANDOM(2, SettingType.TOGGLE, 10, 1000, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),

    DROPTELEPORT(3, SettingType.TOGGLE, 2, 100, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.RENDER),
    VOIDSHIFT(3, SettingType.SLIDER, 1, 50, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.RENDER),
    NEGATEFALLDAMAGE(3, SettingType.SLIDER, 1, 50, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    NIGHTVISION(3, SettingType.SLIDER, 1, 25, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    ELYTRA(3, SettingType.SLIDER, 1, 1000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    DECOY(3, SettingType.SLIDER, 25, 5000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    LINGERING(3, SettingType.TOGGLE, 50, 1000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    HOMING(3, SettingType.TOGGLE, 50, 2000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.TARGET),
    WATERBREATHING(3, SettingType.TOGGLE, 50, 500, UseType.PASSIVE_TICK, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),

    OREXRAY(4, SettingType.TOGGLE, 100, 5000, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    GLOWING(4, SettingType.TOGGLE, 100, 5000, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    INSTABREAK(4, SettingType.TOGGLE, 2, 250, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    ECLIPSEGATE(4, SettingType.SLIDER, 1, 250, UseType.USE_ON, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    DEATHPROTECTION(4, SettingType.SLIDER, 25, 450000, UseType.PASSIVE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    DEBUFFREMOVER(4, SettingType.SLIDER, 25, 50000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    EARTHQUAKE(4, SettingType.SLIDER, 25, 50000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    NOAI(4, SettingType.SLIDER, 25, 100000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    FLIGHT(4, SettingType.SLIDER, 1, 100, UseType.PASSIVE_TICK, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    LAVAIMMUNITY(4, SettingType.SLIDER, 1, 1000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    PHASE(4, SettingType.SLIDER, 1, 50000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    TIMEPROTECTION(4, SettingType.SLIDER, 1, 5000, UseType.PASSIVE, BindingType.CUSTOM_ONLY, CustomSettingType.NONE),
    POLYMORPH_TARGET(4, SettingType.TOGGLE, 10, 50000, UseType.USE, BindingType.LEFT_AND_CUSTOM, CustomSettingType.NONE),
    EPICARROW(4, SettingType.SLIDER, 25, 100000, UseType.USE_COOLDOWN, BindingType.CUSTOM_ONLY, CustomSettingType.NONE);

    public enum SettingType {
        TOGGLE,
        SLIDER,
        CYCLE
    }

    public enum CustomSettingType {
        NONE,
        RENDER,
        TARGET
    }

    public enum UseType {
        USE,
        USE_ON,
        USE_COOLDOWN,
        PASSIVE,
        PASSIVE_TICK,
        PASSIVE_COOLDOWN,
        PASSIVE_TICK_COOLDOWN;

        public boolean isPlayerTriggered() {
            return this == USE || this == USE_ON || this == USE_COOLDOWN;
        }

        public boolean usesCooldown() {
            return this == USE_COOLDOWN || this == PASSIVE_COOLDOWN || this == PASSIVE_TICK_COOLDOWN;
        }
    }

    public enum BindingType {
        NONE,
        CUSTOM_ONLY,
        LEFT_AND_CUSTOM
    }

    private static final Map<String, Ability> BY_ID;

    static {
        Map<String, Ability> byId = new LinkedHashMap<>();
        for (Ability ability : values()) {
            byId.put(ability.id, ability);
        }
        BY_ID = Collections.unmodifiableMap(byId);
    }

    private final int tier;
    private final String id;
    private final String translationKey;
    private final ResourceLocation cooldownIcon;
    private final SettingType settingType;
    private final int durabilityCost;
    private final int feCost;
    private final UseType useType;
    private final BindingType bindingType;
    private final CustomSettingType customSettingType;

    Ability(
            int tier,
            SettingType settingType,
            int durabilityCost,
            int feCost,
            UseType useType,
            BindingType bindingType,
            CustomSettingType customSettingType
    ) {
        this.tier = tier;
        this.id = name().toLowerCase(Locale.ROOT);
        this.translationKey = Reference.MOD_ID + ".ability." + id;
        this.cooldownIcon = new ResourceLocation(Reference.MOD_ID, "textures/gui/overlay/" + id + ".png");
        this.settingType = settingType;
        this.durabilityCost = durabilityCost;
        this.feCost = feCost;
        this.useType = useType;
        this.bindingType = bindingType;
        this.customSettingType = customSettingType;
    }

    public int getTier() {
        return tier;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public ResourceLocation getCooldownIcon() {
        return cooldownIcon;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public int getDurabilityCost() {
        return durabilityCost;
    }

    public int getFeCost() {
        return feCost;
    }

    public UseType getUseType() {
        return useType;
    }

    public BindingType getBindingType() {
        return bindingType;
    }

    public boolean isBindable() {
        return bindingType != BindingType.NONE;
    }

    public CustomSettingType getCustomSettingType() {
        return customSettingType;
    }

    public boolean hasCustomSetting() {
        return customSettingType != CustomSettingType.NONE;
    }

    public boolean requiresUseAction() {
        return useType == UseType.USE || useType == UseType.USE_COOLDOWN;
    }

    public boolean requiresUseOnAction() {
        return useType == UseType.USE_ON;
    }

    public boolean usesCooldown() {
        return useType.usesCooldown();
    }

    public boolean requiresUpgrade() {
        switch (this) {
            case LAVAREPAIR:
            case AIRBURST:
            case POLYMORPH_RANDOM:
            case VOIDSHIFT:
            case ECLIPSEGATE:
            case POLYMORPH_TARGET:
                return false;
            default:
                return true;
        }
    }

    public String getUpgradeItemId() {
        if (!requiresUpgrade()) {
            return "";
        }
        if (this == TIMEPROTECTION) {
            return "upgrade_time_protection";
        }
        return "upgrade_" + id;
    }

    public static Ability byId(String id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id.toLowerCase(Locale.ROOT));
    }
}
