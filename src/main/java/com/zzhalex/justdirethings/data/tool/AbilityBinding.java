package com.zzhalex.justdirethings.data.tool;

import net.minecraft.nbt.NBTTagCompound;

public final class AbilityBinding {

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
        tag.setString("AbilityId", abilityId);
        tag.setInteger("KeyCode", keyCode);
        tag.setBoolean("MouseBinding", mouseBinding);
        tag.setBoolean("RequireEquipped", requireEquipped);
        return tag;
    }

    public static AbilityBinding readFromNbt(NBTTagCompound tag) {
        return new AbilityBinding(
                tag.getString("AbilityId"),
                tag.getInteger("KeyCode"),
                tag.getBoolean("MouseBinding"),
                tag.getBoolean("RequireEquipped")
        );
    }
}
