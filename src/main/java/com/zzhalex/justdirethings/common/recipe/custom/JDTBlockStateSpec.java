package com.zzhalex.justdirethings.common.recipe.custom;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JDTBlockStateSpec {

    private final ResourceLocation blockId;
    private final Map<String, String> properties;

    public JDTBlockStateSpec(ResourceLocation blockId, Map<String, String> properties) {
        this.blockId = blockId;
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    public static JDTBlockStateSpec fromJson(JsonContext context, JsonObject json) {
        ResourceLocation blockName = resourceLocation(context, JsonUtils.getString(json, "Name"));
        Map<String, String> parsedProperties = new LinkedHashMap<>();
        if (json.has("Properties")) {
            JsonObject propertiesJson = JsonUtils.getJsonObject(json, "Properties");
            for (Map.Entry<String, com.google.gson.JsonElement> entry : propertiesJson.entrySet()) {
                parsedProperties.put(entry.getKey(), JsonUtils.getString(entry.getValue(), entry.getKey()));
            }
        }
        return new JDTBlockStateSpec(blockName, parsedProperties);
    }

    public static ResourceLocation resourceLocation(JsonContext context, String id) {
        return id.indexOf(':') >= 0 ? new ResourceLocation(id) : new ResourceLocation(context.getModId(), id);
    }

    public ResourceLocation getBlockId() {
        return blockId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean matches(IBlockState state) {
        ResourceLocation stateId = Block.REGISTRY.getNameForObject(state.getBlock());
        if (!blockId.equals(stateId)) {
            return false;
        }
        for (Map.Entry<String, String> expected : properties.entrySet()) {
            IProperty<?> property = findProperty(state, expected.getKey());
            if (property == null || !expected.getValue().equals(getPropertyValueName(state, property))) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(JDTBlockStateSpec state) {
        if (state == null || !blockId.equals(state.blockId)) {
            return false;
        }
        for (Map.Entry<String, String> expected : properties.entrySet()) {
            if (!expected.getValue().equals(state.properties.get(expected.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public IBlockState toBlockState() {
        Block block = Block.REGISTRY.getObject(blockId);
        ResourceLocation resolvedId = Block.REGISTRY.getNameForObject(block);
        if (block == null || resolvedId == null || !blockId.equals(resolvedId)) {
            return Blocks.AIR.getDefaultState();
        }

        IBlockState state = block.getDefaultState();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            state = withPropertyValue(state, property.getKey(), property.getValue());
        }
        return state;
    }

    public static JDTBlockStateSpec fromState(IBlockState state) {
        ResourceLocation id = Block.REGISTRY.getNameForObject(state.getBlock());
        Map<String, String> parsedProperties = new LinkedHashMap<>();
        for (IProperty<?> property : state.getPropertyKeys()) {
            parsedProperties.put(property.getName(), getPropertyValueName(state, property));
        }
        return new JDTBlockStateSpec(id, parsedProperties);
    }

    public NBTTagCompound writeToNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("Name", blockId == null ? "minecraft:air" : blockId.toString());
        NBTTagCompound propertiesTag = new NBTTagCompound();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            propertiesTag.setString(property.getKey(), property.getValue());
        }
        root.setTag("Properties", propertiesTag);
        return root;
    }

    public static JDTBlockStateSpec readFromNbt(NBTTagCompound root) {
        if (root == null || !root.hasKey("Name")) {
            return fromState(Blocks.AIR.getDefaultState());
        }
        Map<String, String> parsedProperties = new LinkedHashMap<>();
        NBTTagCompound propertiesTag = root.getCompoundTag("Properties");
        for (String key : propertiesTag.getKeySet()) {
            parsedProperties.put(key, propertiesTag.getString(key));
        }
        return new JDTBlockStateSpec(new ResourceLocation(root.getString("Name")), parsedProperties);
    }

    public JDTBlockStateSpec withoutProperty(String propertyName) {
        if (!properties.containsKey(propertyName)) {
            return this;
        }
        Map<String, String> filteredProperties = new LinkedHashMap<>(properties);
        filteredProperties.remove(propertyName);
        return new JDTBlockStateSpec(blockId, filteredProperties);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof JDTBlockStateSpec)) {
            return false;
        }
        JDTBlockStateSpec that = (JDTBlockStateSpec) other;
        return blockId.equals(that.blockId) && properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return 31 * blockId.hashCode() + properties.hashCode();
    }

    private static IProperty<?> findProperty(IBlockState state, String propertyName) {
        for (IProperty<?> property : state.getPropertyKeys()) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withPropertyValue(IBlockState state, String propertyName, String valueName) {
        IProperty property = findProperty(state, propertyName);
        if (property == null) {
            return state;
        }
        for (Object allowedValue : property.getAllowedValues()) {
            Comparable comparable = (Comparable) allowedValue;
            if (valueName.equals(property.getName(comparable))) {
                return state.withProperty(property, comparable);
            }
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String getPropertyValueName(IBlockState state, IProperty property) {
        return property.getName((Comparable) state.getValue(property));
    }
}
