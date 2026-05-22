package com.zzhalex.justdirethings.data.tool;

import com.zzhalex.justdirethings.data.JDTDataKeys;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public final class ToolStateIO {

    private ToolStateIO() {
    }

    public static NBTTagCompound write(ToolState state) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(JDTDataKeys.TOOL_ENABLED, state.isEnabled());
        tag.setTag(JDTDataKeys.INSTALLED_ABILITIES, writeStrings(state.getInstalledAbilities().toArray(new String[0])));
        tag.setTag(JDTDataKeys.LEFT_CLICK_ABILITIES, writeStrings(state.getLeftClickAbilities().toArray(new String[0])));
        tag.setTag(JDTDataKeys.ABILITY_VALUES, writeIntMap(state.getAbilityValues()));
        tag.setTag(JDTDataKeys.ABILITY_CUSTOM_SETTINGS, writeIntMap(state.getAbilityCustomSettings()));
        tag.setTag(JDTDataKeys.ABILITY_BINDING_MODES, writeIntMap(state.getAbilityBindingModes()));
        tag.setTag(JDTDataKeys.ABILITY_BINDINGS, writeBindings(state));
        return tag;
    }

    public static ToolState read(NBTTagCompound tag) {
        ToolState state = new ToolState();
        state.setEnabled(!tag.hasKey(JDTDataKeys.TOOL_ENABLED) || tag.getBoolean(JDTDataKeys.TOOL_ENABLED));
        readStrings(tag.getTagList(JDTDataKeys.INSTALLED_ABILITIES, Constants.NBT.TAG_STRING), state.getInstalledAbilities());
        readStrings(tag.getTagList(JDTDataKeys.LEFT_CLICK_ABILITIES, Constants.NBT.TAG_STRING), state.getLeftClickAbilities());
        readIntMap(tag.getCompoundTag(JDTDataKeys.ABILITY_VALUES), state.getAbilityValues());
        readIntMap(tag.getCompoundTag(JDTDataKeys.ABILITY_CUSTOM_SETTINGS), state.getAbilityCustomSettings());
        readIntMap(tag.getCompoundTag(JDTDataKeys.ABILITY_BINDING_MODES), state.getAbilityBindingModes());
        readBindings(tag.getTagList(JDTDataKeys.ABILITY_BINDINGS, Constants.NBT.TAG_COMPOUND), state);
        return state;
    }

    private static NBTTagList writeStrings(String[] values) {
        NBTTagList list = new NBTTagList();
        for (String value : values) {
            list.appendTag(new NBTTagString(value));
        }
        return list;
    }

    private static void readStrings(NBTTagList list, java.util.Collection<String> output) {
        for (int i = 0; i < list.tagCount(); i++) {
            output.add(list.getStringTagAt(i));
        }
    }

    private static NBTTagCompound writeIntMap(Map<String, Integer> values) {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            tag.setInteger(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    private static void readIntMap(NBTTagCompound input, Map<String, Integer> output) {
        for (String key : input.getKeySet()) {
            output.put(key, input.getInteger(key));
        }
    }

    private static NBTTagList writeBindings(ToolState state) {
        NBTTagList list = new NBTTagList();
        for (AbilityBinding binding : state.getAbilityBindings()) {
            list.appendTag(binding.writeToNbt());
        }
        return list;
    }

    private static void readBindings(NBTTagList list, ToolState state) {
        for (int i = 0; i < list.tagCount(); i++) {
            state.getAbilityBindings().add(AbilityBinding.readFromNbt(list.getCompoundTagAt(i)));
        }
    }

}
