package com.zzhalex.justdirethings.data.tool;

import net.minecraft.nbt.NBTTagCompound;

public final class AbilityBinding {

    private static final String KEY_ABILITY_ID = "AbilityId";
    private static final String KEY_KEY_CODE = "KeyCode";
    private static final String KEY_MOUSE_BINDING = "MouseBinding";
    private static final String KEY_REQUIRE_EQUIPPED = "RequireEquipped";
    private static final String KEY_ALLOW_INVENTORY_ACTIVATION = "AllowInventoryActivation";

    private final String abilityId;
    private final int keyCode;
    private final boolean mouseBinding;
    private final boolean requireEquipped;

    public AbilityBinding(String abilityId, int keyCode, boolean mouseBinding, boolean requireEquipped) {
        this.abilityId = abilityId;
        this.keyCode = keyCode;
        this.mouseBinding = mouseBinding;
        this.requireEquipped = requireEquipped;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public boolean isMouseBinding() {
        return mouseBinding;
    }

    public boolean isRequireEquipped() {
        return requireEquipped;
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(KEY_ABILITY_ID, abilityId);
        tag.setInteger(KEY_KEY_CODE, keyCode);
        tag.setBoolean(KEY_MOUSE_BINDING, mouseBinding);
        tag.setBoolean(KEY_REQUIRE_EQUIPPED, requireEquipped);
        if (!requireEquipped) {
            tag.setBoolean(KEY_ALLOW_INVENTORY_ACTIVATION, true);
        }
        return tag;
    }

    public static AbilityBinding readFromNbt(NBTTagCompound tag) {
        boolean requireEquipped = !tag.hasKey(KEY_REQUIRE_EQUIPPED)
                || tag.getBoolean(KEY_REQUIRE_EQUIPPED)
                || !tag.getBoolean(KEY_ALLOW_INVENTORY_ACTIVATION);
        return new AbilityBinding(
                tag.getString(KEY_ABILITY_ID),
                tag.getInteger(KEY_KEY_CODE),
                tag.getBoolean(KEY_MOUSE_BINDING),
                requireEquipped
        );
    }
}
